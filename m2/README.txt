
The game starts from the main() in GameFrame.class in the package comp303.fivehundred.engine
There is also a player class that initializes IRobotPlayer objects to play either as human users or any of the AI personalities.
A GUI is launched displaying the log of the logger object as it is collected and returning statistics at the end of each round.
The BasicBiddingStrategy is used by Basic AI to choose a bid. It considers what the partner has played (when available), the 
Strong Cards in the hand, the number of cards for each suit, the weight of number of cards in each suit in order to recommend a Pass or a bid of a certain number of noTrumps or any other suit. A lot of testing was used in order to reach the ideal numerical values and constant weights for the assessment of each of these variables so that Basic AI performs well in its Bidding process.
Basic Player AI then plays at each trick the lowest card in the hand or the lowest card that can win as outlined by the Professor.