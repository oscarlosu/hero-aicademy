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

public class FocusAttackLowHP extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = BehaviourHelper.GetDamagedUnits(gameState, !isPlayer1);
		if (enemyUnits.size() == 0) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, Card.ARCHER, Card.WIZARD, Card.NINJA);
		if (friendlyAttackUnits.size() == 0)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
		//Find the the enemy that I can kill quickest, that has the biggest value
		
		Unit[] bestPair = BehaviourHelper.CalculateBestAttackOnTargets(gameState, friendlyAttackUnits, enemyUnits, true);
		
		Unit bestAttacker = bestPair[0];
		Unit bestTarget = bestPair[1];

		if (bestAttacker == null) {
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		}
		
		Position targetPosition = gameState.GetUnitPosition(bestTarget);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);
		
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, targetPosition, gameState.APLeft, false));
		
		return actions;
	}

}
