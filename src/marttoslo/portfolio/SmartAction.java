package marttoslo.portfolio;

import action.Action;

public class SmartAction extends Action {

	public int cost;
	private Action[] actions;
	private int index = 0;
	
	public SmartAction(Action... actions) {
		this.actions = actions;
		cost = actions.length;
	}
	
	public Action Next() {
		if (!HasNext())
			return null;
		index++;
		return actions[index];
	}
	
	public boolean HasNext() {
		return (index+1 < actions.length-1);
	}
}
