package marttoslo.evolution.portfolio;

import java.util.ArrayList;
import java.util.List;

import action.Action;
import marttoslo.portfolio.PortfolioController.BehaviourType;

public class BehaviourActionsPair {
	public BehaviourType behaviour;
	public List<Action> actions;
	
	public BehaviourActionsPair(BehaviourType b, List<Action> a) {
		behaviour = b;
		actions = new ArrayList<Action>();
		actions.addAll(a);
	}
}
