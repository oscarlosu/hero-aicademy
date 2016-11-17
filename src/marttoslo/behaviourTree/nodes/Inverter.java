package marttoslo.behaviourTree.nodes;

import java.util.ArrayList;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Inverter extends Node {
	
	public Inverter() {
		
	}
	@Override
	public NodeState Process() {
		if(children.get(0).Process() == NodeState.SUCCESS) {
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
