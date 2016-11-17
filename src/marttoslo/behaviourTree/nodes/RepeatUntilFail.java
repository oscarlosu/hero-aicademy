package marttoslo.behaviourTree.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class RepeatUntilFail extends Node {
	ArrayList<Node> children = new ArrayList<Node>();
	
	public RepeatUntilFail() {
	}
	@Override
	public NodeState Process(HashMap<String, Object> context) {
		for(int i = 0; i < children.size(); ++i) {
			if(children.get(i).Process(context) == NodeState.FAILURE) {
				return NodeState.SUCCESS;
			}
		}
		return NodeState.FAILURE;
	}
	@Override
	public void Init() {
		// TODO Auto-generated method stub
		
	}
}
