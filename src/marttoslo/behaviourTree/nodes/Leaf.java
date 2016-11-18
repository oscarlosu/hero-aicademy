package marttoslo.behaviourTree.nodes;

import java.util.HashMap;

import marttoslo.behaviourTree.BehaviourTreeController;
import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Leaf extends Node {

	protected static final int MIN_DISTANCE = 10;	//if a ghost is this close, run away
	protected BehaviourTreeController controller;
	
	@Override
	public void Init() {
		this.controller = (BehaviourTreeController) controller;
	}

	@Override
	public NodeState Process(HashMap<String, Object> context) {
		// TODO Auto-generated method stub
		return null;
	}
}