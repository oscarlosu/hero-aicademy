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

public class FocusAttackLowHP extends Behaviour {
	
	private BehaviourType fallbackBehaviour;
	public FocusAttackLowHP(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}

	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = BehaviourHelper.GetDamagedUnits(gameState, !isPlayer1);
		if (enemyUnits.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, false, Card.ARCHER, Card.WIZARD, Card.NINJA);
		if (friendlyAttackUnits.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		//Find the the enemy that I can kill quickest, that has the biggest value
		
		Unit[] bestPair = BehaviourHelper.CalculateBestAttackOnTargets(gameState, friendlyAttackUnits, enemyUnits, true);
		
		Unit bestAttacker = bestPair[0];
		Unit bestTarget = bestPair[1];

		if (bestAttacker == null) {
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		}
		
		Position targetPosition = gameState.GetUnitPosition(bestTarget);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);
		
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, targetPosition, gameState.APLeft, false));
		
		return actions;
	}

}
