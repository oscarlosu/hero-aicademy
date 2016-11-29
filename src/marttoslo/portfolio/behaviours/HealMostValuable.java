package marttoslo.portfolio.behaviours;

import java.util.ArrayList;
import java.util.HashMap;

import action.Action;
import action.UnitAction;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import model.Card;
import model.Position;
import model.Unit;

public class HealMostValuable extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		/*
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
		
		
		
		Unit[] bestPair = BehaviourHelper.CalculateBestAttackOnTargets(gameState, friendlyAttackUnits, enemyUnits, true);
		
		Unit bestAttacker = bestPair[0];
		Unit bestTarget = bestPair[1];

		if (bestAttacker == null) {
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		}

		Position targetPosition = gameState.GetUnitPosition(bestTarget);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);
		
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, targetPosition, gameState.APLeft, false));
		*/
		return actions;
	}

}
