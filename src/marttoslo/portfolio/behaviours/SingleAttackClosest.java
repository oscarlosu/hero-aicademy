package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import action.UnitAction;
import action.UnitActionType;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Position;
import model.Unit;

public class SingleAttackClosest extends Behaviour {

	private BehaviourType fallbackBehaviour;
	public SingleAttackClosest(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> enemyUnits = gameState.GetAllUnitsFromTeam(!isPlayer1);
		if (enemyUnits.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, false, Card.ARCHER, Card.WIZARD, Card.NINJA, Card.CLERIC, Card.KNIGHT);
		if (friendlyAttackUnits.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		//Find closest pair
		Unit[] bestPair = BehaviourHelper.GetClosestPairOfUnits(gameState, friendlyAttackUnits, enemyUnits);
		
		Unit bestAttacker = bestPair[0];
		Unit bestTarget = bestPair[1];

		if (bestAttacker == null) {
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		}

		Position targetPosition = gameState.GetUnitPosition(bestTarget);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);
		
		if (targetPosition.distance(attackerPosition) < bestAttacker.unitClass.attack.range) {
			actions.add(new UnitAction(attackerPosition, targetPosition, UnitActionType.ATTACK));
			return actions;
		}
		
		ArrayList<UnitAction> moveTowards = new ArrayList<UnitAction>(BehaviourHelper.MoveUnitToRange(gameState, bestAttacker, targetPosition, bestAttacker.unitClass.attack.range, 1));
		if (moveTowards.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		else {
			actions.add(moveTowards.get(0));
			return actions;
		}
	}

}
