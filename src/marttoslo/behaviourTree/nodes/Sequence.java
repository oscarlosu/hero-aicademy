package marttoslo.behaviourTree.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Sequence extends Node {
	ArrayList<Node> children = new ArrayList<Node>();
	
	public Sequence() {
	}
	@Override
	public NodeState Process(HashMap<String, Object> context) {
		for(int i = 0; i < children.size(); ++i) {
			if(children.get(i).Process(context) == NodeState.FAILURE) {
				return NodeState.FAILURE;
			}
		}
		return NodeState.SUCCESS;
	}
	@Override
	public void Init() {
		// TODO Auto-generated method stub
		
	}

}
