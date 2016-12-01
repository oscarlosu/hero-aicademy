package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import action.DropAction;
import game.GameState;
import marttoslo.heuristics.HeuristicEvaluator;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Unit;

public class EquipScroll extends Behaviour {

	Card[] priotiizedListOfUnits = new Card[] {
		Card.ARCHER, Card.WIZARD, Card.NINJA, Card.CLERIC, Card.KNIGHT	
	};
	private Card itemToEquip = Card.SCROLL;
	
	private BehaviourType fallbackBehaviour;
	public EquipScroll(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Card> equipment = gameState.GetCardsFromHand(isPlayer1, itemToEquip);
		if (equipment.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		Card itemToEquip = equipment.get(PortfolioController.random.nextInt(equipment.size()));

		Unit theBest = null;
		HeuristicEvaluator evaluator = new HeuristicEvaluator(false);
		for (Card unit : priotiizedListOfUnits) {
			//Get all units on board
			ArrayList<Unit> units = new ArrayList<Unit>();
			units = gameState.GetAllUnitsOfType(isPlayer1, unit);

			if (units.size() == 0) {
				continue;
			}
			double bestValue = -10000.0;
			for (Unit u : units) {
				double evalValue = evaluator.evalEquip(u, itemToEquip, gameState);
				if (evalValue > bestValue) {
					bestValue = evalValue;
					theBest = u;
				}
			}
		}
		
		if (theBest == null)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		actions.add(new DropAction(itemToEquip, gameState.GetUnitPosition(theBest)));
		return actions;
	}

}
