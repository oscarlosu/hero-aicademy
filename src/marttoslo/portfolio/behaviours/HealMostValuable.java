package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Position;
import model.Unit;

public class HealMostValuable extends Behaviour {

	private BehaviourType fallbackBehaviour;
	public HealMostValuable(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}

	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		
		ArrayList<Unit> friendlyLowHPUnits = BehaviourHelper.GetDamagedUnits(gameState, isPlayer1);
		if (friendlyLowHPUnits.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		ArrayList<Unit> healers = gameState.GetAllUnitsOfType(isPlayer1, Card.CLERIC);
		if (healers.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		//Find teammates that can be healed, prioritizing other healers
		ArrayList<Unit> targets = new ArrayList<Unit>();
		ArrayList<Unit> altTargets = new ArrayList<Unit>();
		for (Unit unit : friendlyLowHPUnits) {
			if (unit.unitClass.card == Card.CLERIC) 
				targets.add(unit);
			else
				altTargets.add(unit);
		}
		if (targets.size() == 0) {
			if (targets.size() == 0)
				return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
			else 
				targets = altTargets;
		}
		
		Unit[] bestPair = BehaviourHelper.CalculateBestHealingOnTargets(gameState, healers, friendlyLowHPUnits, true);
		
		Unit bestHealer = bestPair[0];
		Unit bestTarget = bestPair[1];
		
		if (bestHealer == null) {
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		}

		Position targetPosition = gameState.GetUnitPosition(bestTarget);
		Position healerPosition = gameState.GetUnitPosition(bestHealer);
		
		actions.addAll(BehaviourHelper.GetHealTargetStrategy(gameState, healerPosition, targetPosition, gameState.APLeft));
		
		return actions;
	}

}
