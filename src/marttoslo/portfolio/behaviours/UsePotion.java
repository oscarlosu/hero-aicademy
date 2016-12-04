package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import action.DropAction;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Unit;

public class UsePotion extends Behaviour {

	Card[] priotiizedListOfUnits = new Card[] {
		Card.ARCHER, Card.WIZARD, Card.CLERIC, Card.NINJA, Card.KNIGHT	
	};
	private Card itemToEquip = Card.REVIVE_POTION;

	private BehaviourType fallbackBehaviour;
	public UsePotion(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Card> potions = gameState.GetCardsFromHand(isPlayer1, itemToEquip);
		if (potions.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);

		ArrayList<Unit> units = new ArrayList<Unit>();
		ArrayList<Unit> deadUnits = BehaviourHelper.GetDeadUnits(gameState, isPlayer1);
		if (deadUnits.size() > 0)
			units = deadUnits;
		else {
			units = BehaviourHelper.GetDamagedUnits(gameState, isPlayer1);
		}
		if (units.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		Unit bestTarget = BehaviourHelper.GetLowestHpUnit(gameState, isPlayer1);
		if (bestTarget == null)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		actions.add(new DropAction(itemToEquip, gameState.GetUnitPosition(bestTarget)));
		return actions;
	}

}
