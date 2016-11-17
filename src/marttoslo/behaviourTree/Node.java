package marttoslo.behaviourTree;

import java.util.ArrayList;

public abstract class Node {
	
	public abstract void Init();
	public abstract NodeState Process();
	public ArrayList<Node> children = new ArrayList<>();
}
