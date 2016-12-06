package marttoslo.portfolio;

import java.util.HashMap;

public class BehaviourStatistics {

	public int totalRunCount = 0;
	public double runTime = 0;
	
	private HashMap<Integer, Integer> turnRunCount = new HashMap<Integer, Integer>();

	public double GetAverageRunTime() {
		return runTime/totalRunCount;
	}
	
	public void Run(int round) {
		totalRunCount++;
		Integer turnCount = 0;
		if (turnRunCount.containsKey(round)) {
			turnCount = turnRunCount.get(round);
			turnCount++;
			turnRunCount.replace(round, turnCount);
		}
		else {
			turnCount++;
			turnRunCount.put(round, turnCount);
		}
			
	}
	
	public int GetTurnCount(int round) {
		if (turnRunCount.containsKey(round))
				return turnRunCount.get(round);
		else
			return 0;
	}
}
