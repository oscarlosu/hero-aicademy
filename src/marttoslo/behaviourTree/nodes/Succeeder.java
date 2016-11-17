package marttoslo.behaviourTree.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Succeeder extends Node {
	ArrayList<Node> children = new ArrayList<Node>();
	
	public Succeeder() {
	}
	@Override
	public NodeState Process(HashMap<String, Object> context) {
		children.get(0).Process(context);
		return NodeState.SUCCESS;
	}
	@Override
	public void Init() {
		// TODO Auto-generated method stub
		
	}
}
