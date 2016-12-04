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

public class EquipSword extends Behaviour {

	Card[] priotiizedListOfUnits = new Card[] {
		Card.ARCHER, Card.WIZARD, Card.CLERIC, Card.NINJA, Card.KNIGHT	
	};
	private Card itemToEquip = Card.RUNEMETAL;

	private BehaviourType fallbackBehaviour;
	public EquipSword(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Card> swords = gameState.GetCardsFromHand(isPlayer1, itemToEquip);
		if (swords.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);

		Unit theBest = null;
		HeuristicEvaluator evaluator = new HeuristicEvaluator(false);
		for (Card unit : priotiizedListOfUnits) {
			//Get all units on board
			ArrayList<Unit> units = new ArrayList<Unit>();
			units = gameState.GetAllUnitsOfType(isPlayer1, false, unit);

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
