package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;

public class FinalFallback extends Behaviour {
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		return new ArrayList<Action>();
	}

}
