package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import model.Card;
import model.Position;
import model.Unit;

public class HealMostValuable extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		
		ArrayList<Unit> friendlyLowHPUnits = BehaviourHelper.GetDamagedUnits(gameState, isPlayer1);
		if (friendlyLowHPUnits.size() == 0) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		ArrayList<Unit> healers = gameState.GetAllUnitsOfType(isPlayer1, Card.CLERIC);
		if (healers.size() == 0)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
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
				return fallbackBehaviour.GetActions(isPlayer1, gameState);
			else 
				targets = altTargets;
		}
		
		Unit[] bestPair = BehaviourHelper.CalculateBestHealingOnTargets(gameState, healers, friendlyLowHPUnits, true);
		
		Unit bestHealer = bestPair[0];
		Unit bestTarget = bestPair[1];
		
		if (bestHealer == null) {
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		}

		Position targetPosition = gameState.GetUnitPosition(bestTarget);
		Position healerPosition = gameState.GetUnitPosition(bestHealer);
		
		actions.addAll(BehaviourHelper.GetHealTargetStrategy(gameState, healerPosition, targetPosition, gameState.APLeft));
		
		return actions;
	}

}
