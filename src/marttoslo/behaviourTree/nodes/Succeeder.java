package marttoslo.behaviourTree.nodes;

import java.util.ArrayList;

import marttoslo.behaviourTree.Node;
import marttoslo.behaviourTree.NodeState;

public class Succeeder extends Node {
	ArrayList<Node> children = new ArrayList<Node>();
	
	public Succeeder() {
	}
	@Override
	public NodeState Process() {
		children.get(0).Process();
		return NodeState.SUCCESS;
	}
	@Override
	public void Init() {
		// TODO Auto-generated method stub
		
	}
}
