package comp303.fivehundred.ai;
import java.util.ArrayList;
import java.util.Iterator;

import comp303.fivehundred.model.Bid;
import comp303.fivehundred.model.Hand;
import comp303.fivehundred.util.Card;
import comp303.fivehundred.util.Card.Suit;
import comp303.fivehundred.util.Constants;

/**
 * @author Amjad Al-Rikabi
 *
 */
public class BasicBiddingStrategy implements IBiddingStrategy
{
	public int aStrongCardPointsSpades;
	public int aStrongCardPointsClubs;
	public int aStrongCardPointsDiamonds;
	public int aStrongCardPointsHearts;
	public int aTotalStrongCardPoints;
	public int aNumberOfCardPointsSpades;
	public int aNumberOfCardPointsClubs;
	public int aNumberOfCardPointsDiamonds;
	public int aNumberOfCardPointsHearts;
	public int aBidsEstimateSpades;
	public int aBidsEstimateClubs;
	public int aBidsEstimateDiamonds;
	public int aBidsEstimateHearts;

	/**
	 * Produces a valid bid, i.e., a between 6 and 10 for any suit or
	 * no trump, that is higher that the last bid.
	 * 
	 * The idea is to associate each of the strongest cards in a suit with a point count 
	 * (e.g., high joker = 4, low joker = 3, and so on down to queen = 1 and everything else, 0).
	 * The rules to implement this strategy are left open, but we offer the following advice:
	 * 
	 * • Check each suit one by one. Tally up the points in that suit (counting the converse jacks). 
	 *   If the suit meets certain thresholds (e.g., >4 cards, >8 points), count the suit as biddable.
	 *   
	 * • Check whether the contract could be made in no trump (e.g., >X points distributed in the 4 suits).
	 * 
	 * • Add a certain number of points if the suit is very long (e.g., +1 per card above 5 in a suit).
	 * 
	 * • Add a certain number of points if the partner has already bid the suit.
	 * 
	 * • Remove a certain number of points if an opponent has already bid the suit.
	 * 
	 * • Selected the biddable suit with the highest point count.
	 * 
	 * • Determine the strength of the contract (6-10) based on thresholds on the point count.
	 * 
	 * • If there is a resulting bid and it’s higher that the highest bid, bid that. Otherwise, pass.
	 * 
	 * • Make sure to design your algorithm in a modular fashion that will facilitate experimentation, i.e., with helper methods and constants.
	 * 
	 * @param pPreviousBids All the previous bids for this hand, in order. The
	 * size of the array is the number of bids already entered (between 0 and 3).
	 * @param pHand The cards in the hand of the player entering the bid.
	 * @return A valid bid (higher than the last bid, or pass).
	 * @pre pPreviousBids.length <= 3
	 * @pre pHand.size() == 10
	 */

	public Bid selectBid(Bid[] pPreviousBids, Hand pHand)
	{
		assert pPreviousBids.length <= Constants.LARGEST_NUMBER_OF_BIDS;
		assert pHand.size() == Constants.HAND_SIZE;
		int aHighBidIndex = -1;

		//Get Highest Previous Bid
		if (!Bid.max(pPreviousBids).isPass())
		{ 
			aHighBidIndex = Bid.max(pPreviousBids).toIndex();
		}

		//Check for Jokers
		int aJokerPoints = getPointsForJokersInHand(pHand);
		//Get the Strong Card Points for Every Suit and no Trumps
		getStrongCardPoints(pHand);
		//Get the Number Of Card Points for Each Suit
		getNumberOfCardPointsForEachSuit(pHand);
		//Check Partner's Bid for Suit
		Suit aPartnerSuit = getPartnerSuit(pPreviousBids); //Have to check that it is not NULL value
		//Adjust for Extra Points for Long Suits
		adjustExtraPointsForLongSuits();
		//Estimate the number of Bids in every suit:
		getEstimateBids(aJokerPoints, aPartnerSuit);

		//Construct Bids and Add to Bids ArrayList
		ArrayList<Bid> aBidsList = getBidsList(aHighBidIndex);

		//Should we play a noTrump:
		int aBidsEstimateForNoTrumps = Math.round(((float)aJokerPoints)/Constants.JOKER_POINTS_DIVISOR_NOTRUMP +
				((float)aTotalStrongCardPoints)/Constants.TOTAL_STRONG_CARDS_POINTS_DIVISOR_NOTRUMP);

		//Check if noTrumps is feasible
		if(aBidsEstimateForNoTrumps >= Constants.LOWEST_BID)
		{
			if(aBidsEstimateForNoTrumps >= Constants.HIGHEST_BID)
			{
				aBidsEstimateForNoTrumps = Constants.HIGHEST_BID;
			}
			Bid aNoTrumpBid = new Bid(aBidsEstimateForNoTrumps, null);
			if(aNoTrumpBid.toIndex() > aHighBidIndex)
			{
				aBidsList.add(aNoTrumpBid);
			}
		}

		//Check that we have a an empty bidsList
		if (aBidsList.isEmpty())
		{
			Bid aPassBid = new Bid();
			return aPassBid;
		}
		else //We have eligible bids in the BidsList
		{
			Bid [] aArrayBids = new Bid[aBidsList.size()];
			int i = 0;

			for(Bid aIndexBid : aBidsList)
			{
				aArrayBids[i] = aIndexBid;
				i++;
			}
			return Bid.max(aArrayBids);
		}

	}

	/**
	 * @param pHighBidIndex
	 * @return ArrayList<Bid>
	 */
	public ArrayList<Bid> getBidsList(int pHighBidIndex)
	{
		ArrayList<Bid> aBidsList = new ArrayList<Bid>();
		if (aBidsEstimateSpades >= Constants.LOWEST_BID)
		{
			Bid aSpadesBid = new Bid(aBidsEstimateSpades, Suit.SPADES);
			//Bid aSpadesBid = new Bid(0);
			if(aSpadesBid.toIndex() > pHighBidIndex)
			{
				aBidsList.add(aSpadesBid);
			}
		}

		if (aBidsEstimateClubs >= Constants.LOWEST_BID)
		{
			Bid aClubsBid = new Bid(aBidsEstimateClubs, Suit.CLUBS);
			//Bid aClubsBid = new Bid(1);
			if(aClubsBid.toIndex() > pHighBidIndex)
			{
				aBidsList.add(aClubsBid);
			}
		}

		if (aBidsEstimateDiamonds >= Constants.LOWEST_BID)
		{
			Bid aDiamondsBid = new Bid(aBidsEstimateDiamonds, Suit.DIAMONDS);
			//Bid aDiamondsBid = new Bid(2);
			if(aDiamondsBid.toIndex() > pHighBidIndex)
			{
				aBidsList.add(aDiamondsBid);	
			}
		}

		if (aBidsEstimateHearts >= Constants.LOWEST_BID)
		{
			Bid aHeartsBid = new Bid(aBidsEstimateHearts, Suit.HEARTS);
			//Bid aHeartsBid = new Bid(3);
			if(aHeartsBid.toIndex() > pHighBidIndex)
			{
				aBidsList.add(aHeartsBid);
			}
		}
		return aBidsList;
	}

	/**
	 * @param pJokerPoints
	 * @param pPartnerSuit
	 */
	public void getEstimateBids(int pJokerPoints, Suit pPartnerSuit)
	{
		aBidsEstimateSpades = estimateBids(Suit.SPADES, pJokerPoints, aStrongCardPointsSpades, 
				aNumberOfCardPointsSpades, pPartnerSuit);
		aBidsEstimateClubs = estimateBids(Suit.CLUBS, pJokerPoints, aStrongCardPointsClubs, 
				aNumberOfCardPointsClubs, pPartnerSuit);
		aBidsEstimateDiamonds = estimateBids(Suit.DIAMONDS, pJokerPoints, aStrongCardPointsDiamonds, 
				aNumberOfCardPointsDiamonds, pPartnerSuit);
		aBidsEstimateHearts = estimateBids(Suit.HEARTS, pJokerPoints, aStrongCardPointsHearts, 
				aNumberOfCardPointsHearts, pPartnerSuit);
	}

	/**
	 * adjustExtraPointsForLongSuits()
	 */
	public void adjustExtraPointsForLongSuits()
	{
		aNumberOfCardPointsSpades = addExtraPointsForLongSuits(aNumberOfCardPointsSpades);
		aNumberOfCardPointsClubs = addExtraPointsForLongSuits(aNumberOfCardPointsClubs);
		aNumberOfCardPointsDiamonds = addExtraPointsForLongSuits(aNumberOfCardPointsDiamonds);
		aNumberOfCardPointsHearts = addExtraPointsForLongSuits(aNumberOfCardPointsHearts);
	}
	
/**
 * @param pHand
 */
	public void getNumberOfCardPointsForEachSuit(Hand pHand)
	{
		aNumberOfCardPointsSpades = getNumberOfCardPoints(pHand, Suit.SPADES);
		aNumberOfCardPointsClubs = getNumberOfCardPoints(pHand, Suit.CLUBS);
		aNumberOfCardPointsDiamonds = getNumberOfCardPoints(pHand, Suit.DIAMONDS);
		aNumberOfCardPointsHearts = getNumberOfCardPoints(pHand, Suit.HEARTS);	
	}

	/**
	 * @param pHand
	 */
	public void getStrongCardPoints(Hand pHand)
	{
		aStrongCardPointsSpades = getStrongCardPointsInSuit(pHand, Suit.SPADES);
		aStrongCardPointsClubs = getStrongCardPointsInSuit(pHand, Suit.CLUBS);
		aStrongCardPointsDiamonds = getStrongCardPointsInSuit(pHand, Suit.DIAMONDS);
		aStrongCardPointsHearts = getStrongCardPointsInSuit(pHand, Suit.HEARTS);
		aTotalStrongCardPoints = aStrongCardPointsHearts + aStrongCardPointsDiamonds + aStrongCardPointsClubs + aStrongCardPointsSpades;
	}

	/**
	 * @param pCurrentSuit
	 * @param pJokerPoints
	 * @param pStrongCardPointsInSuit
	 * @param pNumberOfCardPointsInSuit
	 * @param pPartnerSuit
	 * @return int
	 */
	public int estimateBids(Suit pCurrentSuit, int pJokerPoints, int pStrongCardPointsInSuit, 
			int pNumberOfCardPointsInSuit, Suit pPartnerSuit)
	{
		float afBids = 0;
		afBids += ((float) pJokerPoints)/Constants.JOKER_POINTS_DIVISOR_ESTIMATE;
		afBids += ((float) pStrongCardPointsInSuit)/Constants.STRONG_CARDS_POINTS_DIVISOR_ESTIMATE;
		afBids += ((float) pNumberOfCardPointsInSuit)/Constants.LONG_SUIT_POINTS_DIVISOR_ESTIMATE;
		if ((pPartnerSuit != null) && (pPartnerSuit.equals(pCurrentSuit)) )
		{
			afBids += Constants.PARTNER_BID_SAME_SUIT_SCORE;
		}

		if (afBids > Constants.HIGHEST_BID) 
		{
			afBids = Constants.HIGHEST_BID;
		}
		return Math.round(afBids);
	}

	/**
	 * Returns a score for the number of Jokers present in the hand.
	 * @param pHand current Hand.
	 * @return Points for the number of Jokers in the hand.
	 */

	public int getPointsForJokersInHand(Hand pHand)
	{
		int aJokerPoints = 0;
		Iterator<Card> aCardItr = pHand.iterator();

		while(aCardItr.hasNext()) 
		{
			Card aCard = aCardItr.next();
			if(aCard.isJoker())
			{
				aJokerPoints += Constants.JOKER_POINTS;
			}
		}

		return aJokerPoints;
	}

	/**
	 * Returns the weight of the Number of Card Point depending on the length of the suit.
	 * @param pNumberOfCardPointsSuit
	 * @return New Score for NumberOfCardPointsSuit
	 */
	public int addExtraPointsForLongSuits(int pNumberOfCardPointsSuit)
	{
		//LOWER RANGE OF LONG SUITS
		if (pNumberOfCardPointsSuit >= Constants.LONG_SUIT_LOWER_RANGE_LB &&  pNumberOfCardPointsSuit <= Constants.LONG_SUIT_LOWER_RANGE_UB)
		{
			return Constants.LONG_SUIT_LOWER_RANGE_SCORE + pNumberOfCardPointsSuit;
		}
		//Middle range of long suits
		else if (pNumberOfCardPointsSuit >= Constants.LONG_SUIT_MIDDLE_RANGE_LB && pNumberOfCardPointsSuit <= Constants.LONG_SUIT_MIDDLE_RANGE_UB)
		{
			return Constants.LONG_SUIT_MIDDLE_RANGE_SCORE + pNumberOfCardPointsSuit;
		}
		//Upper range of long suits
		else if (pNumberOfCardPointsSuit >= Constants.LONG_SUIT_UPPER_RANGE_LB)
		{
			return Constants.LONG_SUIT_UPPER_RANGE_SCORE + pNumberOfCardPointsSuit;
		}

		return pNumberOfCardPointsSuit;
	}

	/**
	 * Returns Partner Suit (or NULL if there is no partner established).
	 * @param pPreviousBids
	 * @return Suit of the Partner (or null if no Partner)
	 */
	public Suit getPartnerSuit(Bid[] pPreviousBids)
	{
		if (pPreviousBids.length < 2)
		{
			return null;
		}
		if(pPreviousBids.length == 2)
		{
			if (pPreviousBids[0].isPass())
			{
				return null;
			}
			return pPreviousBids[0].getSuit();
		}
		else //if (pPreviousBids.length == 3)
		{
			if (pPreviousBids[1].isPass())
			{
				return null;
			}
			return pPreviousBids[1].getSuit();
		}
	}

	/**
	 * @param pHand
	 * @param pSuit
	 * @return
	 */
	public int getNumberOfCardPoints(Hand pHand, Suit pSuit)
	{
		int aCounter = 0;
		int aMultiplier = 1;

		Iterator<Card> aHandItr = pHand.iterator();

		while(aHandItr.hasNext()) 
		{
			Card aCard = aHandItr.next();
			//Check that we don't point to the Joker
			if (aCard.isJoker())
			{
				continue;
			}

			if(aCard.getEffectiveSuit(pSuit).equals(pSuit))
			{
				aCounter++;
			}
		}
		return aMultiplier * aCounter;
	}

	/**
	 * @param pHand
	 * @param pSuit
	 * @return
	 */
	public int getStrongCardPointsInSuit(Hand pHand, Suit pSuit)
	{
		int aPoints = 0;
		Iterator<Card> aHandItr = pHand.iterator();

		while(aHandItr.hasNext()) 
		{
			Card aCard = aHandItr.next();

			//Check that we don't point to the Joker
			if (aCard.isJoker())
			{
				continue;
			}

			boolean aWeHaveSameEffectiveSuit = aCard.getSuit().equals(pSuit);
			if(aWeHaveSameEffectiveSuit)
			{
				if(aCard.getRank().equals(Card.Rank.ACE))
				{
					aPoints += Constants.ACE_POINTS;
				}
				if(aCard.getRank().equals(Card.Rank.KING))
				{
					aPoints += Constants.KING_POINTS;
				}
				if(aCard.getRank().equals(Card.Rank.QUEEN))
				{
					aPoints += Constants.QUEEN_POINTS;
				}
				if(aCard.getRank().equals(Card.Rank.JACK))
				{
					aPoints += Constants.JACK_POINTS;
				}
				if(aCard.getRank().equals(Card.Rank.TEN))
				{
					aPoints += Constants.TEN_POINTS;
				}
			}
		}
		return aPoints;
	}
}
