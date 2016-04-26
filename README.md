READ ME for Pete Domokos cw-temple

Escape:

 Even though this coursework involves a static situation, I wanted to write code that could be improved upon naturally and could be applied to a situation where
  a) the exit location shifts mid journey 
  b) the proportion of gold squares shifts more drastically from game to game, or even during a game
  c) the golds shift mid-game

  Below, I try to explain how I have done that.

example a) 

I split the entire escape journey into sub-journeys between the top 25% of gold nodes. I did this in the method getTop25PCGolds. This figure of 25% can easily be adjusted for optimization, and would be especially useful in a case where the proportion of gold squares changed more drastically from game to game, because it can be adjusted. 
Had there been more time, I would have:
	i) factored the ratio of gold squares to non-gold squares into this formula
	ii) identified clusters of golds (ie locations where there is lots of gold in close proximity), then filtered them to get the the top x% of clusters, and then made the sub-journeys go from cluster to cluster. This would have obviously has a separate simple method that collects all the gold within each cluster. 

example b)

I have deliberately put in an adjustable formula, with constant multipliers, which makes a dynamic decision at the time about which gold is the best to go to. (To be trully dynamic, all the relevant paths would be re-assessed in case they have changed, but teh priniciple is there nevertheless.) This formulaic approach means program is adaptable to a potentially changing situation. The formula is in the method calcNodeValue, which is called by the method calcBestMove. This is called everytime a sub-journey is complete. 
Had there been more time, I would have:
	i) adjusted the formula, using the clusters that I wold have created(see example a, so each cluster would have a move value )
	ii) played around with the constant multipliers in teh formula in calcNodeValue. The idea of these is that they can give different weights to 3 different factors - 1) proximity to current location, 2) amount of Gold   and 3) proximity to exit.

The biggest benefit of having the multipliers in the formula is that I can play around to get the right balance ( and could write a program to adjust the multipliers automatically). That is because we want to get golds that are near to George, but we also want to gradually be heading towards the exit, so ideally a weighted decision needs to be taken. I didn't have time to play around with these or to automate the multiplier adjustments.

 Explore:

 Whilst I settled on using random moves to get out of corners and loops when George is stuck, I also wrote methods that tracked teh steps and then noticed if he was stuck. ie if the steps were repeating. It then 
 	1st) backtracks to get away ie heads in the opposite direction for a few steps away from the orb instead of towards
 	2nd) 'blacklists those set of steps that were repeating so that he wil not get stuck again

 I left this out in teh end because there a few remaining instances when he was still getting stuck, and I went for the reliability of randomness! Had there been more time, I would definitely have sorted out the bug and used this. I have included this file separately in the extras folder in this repo. (it wont run now because I have changed many of the names and structures, but you will get the idea).

 I have included many of teh paper designs and working that I used in order to explore the project. This includes analysis of teh shortest path algorithms. This work was done in depth because it enabled me to work towards a design that would be more extensible and adaptable, even though the time-constraints have meant i haven't been able to maximise these ideas.


Github:
My VCS stopped functioning with Intellij on the night of the deadline/ early hours after. This version is therefore titled cw-temple2, and is far better than the version that was left as cw-temple. ie it works, uses java 8, splits all actions into many separate methods, and is adaptable (see point 1 above). It also has teh 'extras' folder, with paper designs and the alternative code.