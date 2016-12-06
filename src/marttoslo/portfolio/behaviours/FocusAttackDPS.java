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

public class FocusAttackDPS extends Behaviour {

	private BehaviourType fallbackBehaviour;
	public FocusAttackDPS(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = gameState.GetAllUnitsFromTeam(!isPlayer1);
		if (enemyUnits.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, false, Card.ARCHER, Card.WIZARD, Card.NINJA);
		if (friendlyAttackUnits.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
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
				return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
			else 
				targets = altTargets;
		}
		
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
