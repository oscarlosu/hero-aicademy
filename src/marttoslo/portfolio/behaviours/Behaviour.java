package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;

public abstract class Behaviour {
	
	public abstract ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState);
}