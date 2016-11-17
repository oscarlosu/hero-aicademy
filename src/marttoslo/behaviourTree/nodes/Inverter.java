package marttoslo.behaviourTree.nodes;

import java.util.HashMap;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Inverter extends Node {
	
	public Inverter() {
		
	}
	@Override
	public NodeState Process(HashMap<String, Object> context) {
		if(children.get(0).Process(context) == NodeState.SUCCESS) {
			return NodeState.FAILURE;
		} else {
			return NodeState.SUCCESS;
		}		
	}
	@Override
	public void Init() {
		// TODO Auto-generated method stub
		
	}
	
}
