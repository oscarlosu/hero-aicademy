package marttoslo.portfolio;

import java.util.ArrayList;
import java.util.List;

import action.Action;
import game.GameState;
import marttoslo.evolution.portfolio.BehaviourActionsPair;
import marttoslo.portfolio.PortfolioController.BehaviourType;

public class SmartAction extends Action {

	public int cost;
	private ArrayList<Action> actions;
	private BehaviourType behaviourType;
	private int index = 0;
	
	public SmartAction(BehaviourType behaviourType) {
		this.behaviourType = behaviourType;
	}
	
	
	public void InitActions(GameState gameState, boolean isPlayer1) {
		Reset();
		//System.out.println("INIT " + isPlayer1);
		actions = PortfolioController.GetActions(gameState, isPlayer1, behaviourType);
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
		if (actions != null)
			if (index < actions.size())
				return true;
		return false;
	}
	
	public void Reset() {
		actions = null;
		index = 0;
	}
	
	public void updateStateAndReset(GameState state) {
		final boolean p1Turn = state.p1Turn;
		InitActions(state, p1Turn);
		while (!state.isTerminal && p1Turn == state.p1Turn && HasNext()) {
			Action nextAction = Next(state, p1Turn);
			state.update(nextAction);
		}
		Reset();
	}
	
	public BehaviourActionsPair updateStateAndResetWithPair(GameState state, boolean isPlayer1) {
		List<Action> actions = new ArrayList<Action>();
		InitActions(state, isPlayer1);
		state.p1Turn = isPlayer1;
		while (!state.isTerminal && isPlayer1 == state.p1Turn && HasNext()) {
			Action nextAction = Next(state, isPlayer1);
			state.update(nextAction);
			
			actions.add(nextAction);
		}
		state.p1Turn = !isPlayer1;
		Reset();
		
		return new BehaviourActionsPair(behaviourType, actions);
	}
}
