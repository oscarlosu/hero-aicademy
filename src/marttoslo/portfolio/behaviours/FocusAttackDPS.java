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

public class FocusAttackDPS extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = gameState.GetAllUnits(!isPlayer1);
		if (enemyUnits.size() == 0) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, Card.ARCHER, Card.WIZARD, Card.NINJA);
		if (friendlyAttackUnits.size() == 0)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		HashMap<Unit, Position> positions = new HashMap<Unit, Position>();
		
		//Find the the enemy that I can kill quickest, that has the biggest value
		ArrayList<Unit> targets = new ArrayList<Unit>();
		ArrayList<Unit> altTargets = new ArrayList<Unit>();
		for (Unit unit : enemyUnits) {
			if (unit.unitClass.card == Card.ARCHER ||unit.unitClass.card == Card.WIZARD || unit.unitClass.card == Card.NINJA) 
				targets.add(unit);
			else if (unit.unitClass.card == Card.KNIGHT)
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
		
		Position targetPosition = positions.get(bestTarget);
		Position attackerPosition = positions.get(bestAttacker);
		
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, targetPosition, gameState.APLeft, false));
		
		return actions;
	}

}
