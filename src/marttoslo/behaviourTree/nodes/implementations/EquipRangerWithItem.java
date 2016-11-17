package marttoslo.behaviourTree.nodes.implementations;

import java.util.HashMap;

import action.SingletonAction;
import game.GameState;
import marttoslo.behaviourTree.ContextKeys;
import marttoslo.behaviourTree.NodeState;
import marttoslo.behaviourTree.nodes.Leaf;
import model.Card;

public class EquipRangerWithItem extends Leaf {
	
	@Override
	public NodeState Process(HashMap<String, Object> context) {
		Card equipment = (Card)context.get(ContextKeys.ITEM_TO_EQUIP);
		GameState gameState = (GameState)context.get(ContextKeys.GAMESTATE);
		/*
		ArrayList<Unit> rangers = new ArrayList<Unit>();
		
		for (int x = 0; x < gameState.map.width; x++)
			for (int y = 0; y < gameState.map.height; y++)
				if (gameState.units[x][y] != null
						&& gameState.units[x][y].p1Owner == gameState.p1Turn
						&& gameState.units[x][y].hp > 0
						&& gameState.units[x][y].unitClass.card != Card.CRYSTAL)
					positions.add(SingletonAction.positions[x][y]);
					*/
		return null;
	}
}
