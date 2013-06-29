package comp303.fivehundred.ai;

import comp303.fivehundred.model.Bid;
import comp303.fivehundred.model.Hand;
import comp303.fivehundred.util.Constants;

/**
 * @author Jean-Benoit
 * Enters a valid but random bid. Passes a configurable number of times.
 * If the robot does not pass, it uses a universal probability
 * distribution across all bids permitted.
 */
public class RandomBiddingStrategy implements IBiddingStrategy
{
	private static final double A_HUNDRED = 100.0;
	private static final double ONE_HALF = 0.5;
	private double aPassingPercentage;
	
	/**
	 * Builds a robot that passes 50% of the time and bids randomly otherwise.
	 */
	public RandomBiddingStrategy()
	{
		
		aPassingPercentage = ONE_HALF;
	}
	
	/** 
	 * Builds a robot that passes the specified percentage number of the time.
	 * @param pPassFrequency A percentage point (e.g., 50 for 50%) of the time the robot 
	 * will pass. Must be between 0 and 100 inclusive. 
	 * Whether the robot passes is determined at random.
	 */
	public RandomBiddingStrategy(int pPassFrequency)
	{
		assert pPassFrequency >= 0 && pPassFrequency <= A_HUNDRED;
		aPassingPercentage = (double)pPassFrequency / A_HUNDRED;
	}
	
	@Override
	public Bid selectBid(Bid[] pPreviousBids, Hand pHand)
	{
		assert pPreviousBids.length <= 3;
		assert pHand.size() == Constants.HAND_SIZE;
		Bid newBid;
		Bid maxBid = Bid.max(pPreviousBids);
		if(Math.random()<aPassingPercentage)
		{
			newBid = new Bid();
		}
		else if(maxBid.isPass())
		{
			newBid = new Bid((int)(Math.random()*(Constants.MAX_BIDDING_INDEX)));
		}
		else if(maxBid.toIndex() == Constants.MAX_BIDDING_INDEX)
		{
			newBid = new Bid();
		}
		else
		{
			newBid = new Bid((int)(Math.random()*(Constants.MAX_BIDDING_INDEX-maxBid.toIndex()))+maxBid.toIndex()+1);
		}
		
		return newBid;
	}

}
