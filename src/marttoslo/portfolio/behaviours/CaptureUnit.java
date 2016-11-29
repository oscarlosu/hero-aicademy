package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import model.Card;
import model.Unit;

public class CaptureUnit extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = BehaviourHelper.GetDeadUnits(gameState, !isPlayer1);
		if (enemyUnits.size() == 0) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		ArrayList<Unit> friendlyUnits = gameState.GetAllUnitsOfType(isPlayer1, Card.ARCHER, Card.WIZARD, Card.NINJA, Card.KNIGHT);
		if (friendlyUnits.size() == 0)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
		int bestApUsed = Integer.MAX_VALUE;
		Unit closestUnit = null;
		Unit selectedDeadUnit = null;
		
		//TODO: Prioritize knights to capture. Perhaps even at a bigger cost?
		
		for (Unit deadUnit : enemyUnits) {
			Unit closeUnit = BehaviourHelper.GetClosestUnitToPointUsingSpeed(gameState, friendlyUnits, gameState.GetUnitPosition(deadUnit));
			int apUsed = BehaviourHelper.DivideCeil(gameState.GetUnitPosition(closeUnit).distance(gameState.GetUnitPosition(deadUnit)), closeUnit.unitClass.speed);
			if (apUsed <= bestApUsed) {
				bestApUsed = apUsed;
				closestUnit = closeUnit;
				selectedDeadUnit = deadUnit;
			}
		}
		
		if (bestApUsed > gameState.APLeft)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
		actions.addAll(BehaviourHelper.MoveTo(gameState, closestUnit, gameState.GetUnitPosition(selectedDeadUnit), gameState.APLeft));
		
		return actions;
	}

}
