package marttoslo.portfolio;

import java.util.ArrayList;

import action.Action;
import game.GameState;
import marttoslo.portfolio.PortfolioController.BehaviourType;

public class SmartAction extends Action {

	public int cost;
	private ArrayList<Action> actions;
	private BehaviourType behaviourType;
	private int index = 0;
	
	public SmartAction(BehaviourType behaviourType) {
		this.behaviourType = behaviourType;
	}
	
	public Action Next(GameState gameState, boolean isPlayer1) {
		if (actions == null) {
			actions = PortfolioController.GetActions(gameState, isPlayer1, behaviourType);
		}
		if (HasNext()) {
			Action action = actions.get(index);
			index++;
			return action;
		}
		else {
			return null;
		}
	}
	
	public boolean HasNext() {
		return (index < actions.size()-1);
	}
	
	public void Reset() {
		actions = null;
		index = 0;
	}
	
	public void updateStateAndReset(GameState state) {
		final boolean p1Turn = state.p1Turn;
		while (!state.isTerminal && p1Turn == state.p1Turn && HasNext()) {
			state.update(Next(state, p1Turn));
		}
		Reset();
	}
}
