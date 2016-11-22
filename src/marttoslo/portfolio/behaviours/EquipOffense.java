package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import action.DropAction;
import game.GameState;
import marttoslo.heuristics.HeuristicEvaluator;
import model.Card;
import model.CardSet;
import model.Unit;

public class EquipOffense extends Behaviour {

	Card[] priotiizedListOfUnits = new Card[] {
		Card.ARCHER, Card.WIZARD, Card.CLERIC, Card.NINJA, Card.KNIGHT	
	};
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		boolean hasSword = false;
		CardSet hand = null;
		if (isPlayer1)
			hand = gameState.p1Hand;
		else 
			hand = gameState.p2Hand;
		for (int i : hand.cards) {
			if (hand.get(i) == Card.RUNEMETAL) {
				hasSword = true;
				break;
			}
		}
		if (!hasSword) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);

		Unit theBest = null;
		HeuristicEvaluator evaluator = new HeuristicEvaluator(false);
		for (Card unit : priotiizedListOfUnits) {
			//Get all units on board
			ArrayList<Unit> units = new ArrayList<Unit>();
			units = gameState.GetAllUnitsOfType(unit, isPlayer1);

			if (units.size() == 0) {
				continue;
			}
			double bestValue = -10000.0;
			for (Unit u : units) {
				double evalValue = evaluator.evalEquip(u, Card.RUNEMETAL, gameState);
				if (evalValue > bestValue) {
					bestValue = evalValue;
					theBest = u;
				}
			}
		}
		
		if (theBest == null)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
		actions.add(new DropAction(Card.RUNEMETAL, gameState.GetUnitPosition(theBest)));
		return actions;
	}

}
