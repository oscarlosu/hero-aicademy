package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;

public abstract class Behaviour {
	
	public abstract ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState);
	public static double GetAverageRunTime() {
		return runTime/runCount;
	}
	
	protected void Start() {
		runCount++;
		startTime = System.currentTimeMillis();
	}
	
	protected void Stop() {
		runTime += System.currentTimeMillis() - startTime;
	}

	protected static int runCount = 0;
	protected static double runTime = 0;
	protected static long startTime = 0;
}