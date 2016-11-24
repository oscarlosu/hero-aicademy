package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;

public abstract class Behaviour {

	public Behaviour fallbackBehaviour;
	public abstract ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState);
	public double GetAverageRunTime() {
		return runTime/runCount;
	}

	protected static int runCount = 0;
	protected static double runTime = 0;
	
}