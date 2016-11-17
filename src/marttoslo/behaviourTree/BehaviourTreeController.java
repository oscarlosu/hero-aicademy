package marttoslo.behaviourTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/*
 * Allows a human player to play the game using the arrow key of the keyboard.
 */
public class BehaviourTreeController
{
	public enum Script {
		Attack
	}
	
	private HashMap<Script, Node> trees;
    private static final String TREE_FOLDER = "Trees";
    private static final String PACKAGE_NAME = "behaviourTree.nodes.";
    
    public BehaviourTreeController()
    {
    	trees = new HashMap<Script, Node>();
    }
    
    public void LoadTrees() {
    	File folder = new File(TREE_FOLDER);
    	File[] listOfFiles = folder.listFiles();
    	
    	for (File file : listOfFiles) {
    		String fileNameWithoutExtension = file.getName().replaceFirst("[.][^.]+$", "");
    		System.out.println("Parsing " + fileNameWithoutExtension);
    		Script script = Script.valueOf(fileNameWithoutExtension);
    		Node tree = ParseTreeFromFile(file, fileNameWithoutExtension);
    		PrintTree(tree, 0);
    		trees.put(script, tree);
    	}
    }
    
    public Node ParseTreeFromFile(File file, String rootName) {
    	Node root = null;
    	try {
			JsonReader jsonReader = new JsonReader(new FileReader(file));
			root = GetNodeFromName(rootName);
			root = handleObject(jsonReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return root;
    }
    
    /**
     * Handle an Object. Consume the first token which is BEGIN_OBJECT. Within
     * the Object there could be array or non array tokens. We write handler
     * methods for both. Noe the peek() method. It is used to find out the type
     * of the next token without actually consuming it.
     *
     * @param reader
     * @throws IOException
     */
    private Node handleObject(JsonReader reader) throws IOException
    {
    	Node newNode = null;
    	ArrayList<Node> children = new ArrayList<Node>();
        reader.beginObject();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.BEGIN_ARRAY)) {
                children = GetChildren(reader);
                newNode.children = children;
            }
            else {
            	Node n = handleNonArrayToken(reader, token);
            	if (n != null) {
                    newNode = n;   
            	}        		
            }
        }
        return newNode;
    }
 
    /**
     * Handle a json array. The first token would be JsonToken.BEGIN_ARRAY.
     * Arrays may contain objects or primitives.
     *
     * @param reader
     * @throws IOException
     */
    private ArrayList<Node> GetChildren(JsonReader reader) throws IOException
    {
    	ArrayList<Node> children = new ArrayList<>();
        reader.beginArray();
        while (true) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.END_ARRAY)) {
                reader.endArray();
                break;
            } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
                children.add(handleObject(reader));
            } else if (token.equals(JsonToken.END_OBJECT)) {
                reader.endObject();
            } else {
            	System.out.println("WTF?");
                handleNonArrayToken(reader, token);
            }
        }
        return children;
    }
 
    /**
     * Handle non array non object tokens
     *
     * @param reader
     * @param token
     * @throws IOException
     */
    private Node handleNonArrayToken(JsonReader reader, JsonToken token) throws IOException
    {
        if (token.equals(JsonToken.NAME)) {
        	String tokenName = reader.nextName();
        	String typeName = "";
        	if (tokenName.equals("name")) {
        		typeName = reader.nextString();
        		Node newNode = GetNodeFromName(typeName);
                return newNode;
        	}
        	else if (tokenName.equals("children"))
        		return null;
        	else reader.skipValue();
        }
        else {
        	reader.skipValue();
        	reader.skipValue();
        }
    	return null;
    }
    
    private Node GetNodeFromName(String name) {
    	Class<?> c;
		try {
			c = Class.forName(PACKAGE_NAME + name);
	    	Constructor<?> ctor = c.getConstructor();
	    	Object newInstance = ctor.newInstance(new Object[] { });
			Node object = (Node) newInstance;
	    	return object;
		} catch (Exception e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			return null;
		}
    }
    
    private void PrintTree(Node node, int index) {
    	for (int i = 0; i < index; i++)
    		System.out.print("\t");
    	index++;
    	System.out.println(node.getClass().getSimpleName());
    	for (Node child : node.children) {
    		PrintTree(child, index);
    	}
    }
}