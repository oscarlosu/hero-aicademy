package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Unit;

public class CaptureUnit extends Behaviour {
	
	private BehaviourType fallbackBehaviour;
	public CaptureUnit(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = BehaviourHelper.GetDeadUnits(gameState, !isPlayer1);
		if (enemyUnits.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		ArrayList<Unit> friendlyUnits = gameState.GetAllUnitsOfType(isPlayer1, Card.ARCHER, Card.WIZARD, Card.NINJA, Card.KNIGHT);
		if (friendlyUnits.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
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
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		actions.addAll(BehaviourHelper.MoveTo(gameState, closestUnit, gameState.GetUnitPosition(selectedDeadUnit), gameState.APLeft));
		
		return actions;
	}

}
