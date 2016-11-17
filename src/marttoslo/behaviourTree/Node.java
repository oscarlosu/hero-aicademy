package marttoslo.behaviourTree;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Node {
	
	public abstract void Init();
	public abstract NodeState Process(HashMap<String, Object> context);
	public ArrayList<Node> children = new ArrayList<>();
}
