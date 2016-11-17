package marttoslo.behaviourTree.nodes;

import java.util.ArrayList;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Selector extends Node {
	ArrayList<Node> children = new ArrayList<Node>();
	
	public Selector() {
	}
	@Override
	public NodeState Process() {
		for(int i = 0; i < children.size(); ++i) {
			if(children.get(i).Process() == NodeState.SUCCESS) {
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
