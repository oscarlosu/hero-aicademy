package marttoslo.portfolio.behaviours;

import java.util.ArrayList;
import java.util.List;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Position;
import model.Unit;

public class FocusAttackCrystal extends Behaviour {

	private BehaviourType fallbackBehaviour;
	public FocusAttackCrystal(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> crystals = gameState.GetAllUnitsOfType(!isPlayer1, false, Card.CRYSTAL);
		if (crystals.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, false, Card.ARCHER, Card.WIZARD, Card.NINJA, Card.CLERIC, Card.KNIGHT);
		if (friendlyAttackUnits.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		int bestDamage = 0;
		int nrOfAPSpent = Integer.MAX_VALUE;
		int nrOfAPSpentOnAttack = 0;
		Unit bestAttacker = null;
		Unit crystalAttacked = null;
		
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		for (Unit crystal : crystals) {
			for (Unit attacker : friendlyAttackUnits) {
				
				int[] result = BehaviourHelper.CalculateMaxDamage(gameState, attacker, crystal, gameState.APLeft);
				if (result[0] > bestDamage) {
					bestDamage = result[0];
					nrOfAPSpent = result[1];
					nrOfAPSpentOnAttack = result[2];
					bestAttacker = attacker;
					crystalAttacked = crystal;
				}
				else if (result[0] == bestDamage) {
					if (result[1] < nrOfAPSpent) {
						nrOfAPSpent = result[1];
						nrOfAPSpentOnAttack = result[2];
						bestAttacker = attacker;
						crystalAttacked = crystal;
					}
				}
			}
		}
		
		if (bestAttacker == null || crystalAttacked == null)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		List<Position> assaultSquares = gameState.map.assaultSquares;
		Unit closestUnit = null;
		Position chosenAssaultSquare = null;
		int closestDistance = Integer.MAX_VALUE;
		
		for (Position assPos : assaultSquares) {
			for (Unit unit : friendlyAttackUnits) {
				int distance = gameState.GetUnitPosition(unit).distance(assPos);
				if (distance < closestDistance && !unit.equals(bestAttacker)) {
					closestDistance = distance;
					chosenAssaultSquare = assPos;
					closestUnit = unit;
				}
			}
		}

		Position crystalPosition = gameState.GetUnitPosition(crystalAttacked);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);

		//If there's already a unit standing on an assaultSquare, we don't have enough AP or no available units return pure attack moves
		if (closestDistance == 0 || closestUnit == null) {
			actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, crystalPosition, gameState.APLeft, false));
			return actions;
		}

		int actionPointsLeftToAttackAfterMoving = 0;
		actionPointsLeftToAttackAfterMoving = nrOfAPSpentOnAttack - BehaviourHelper.DivideCeil(closestDistance, closestUnit.unitClass.speed);
		int damageWithAssaultBonus = 0;
		for (int i = actionPointsLeftToAttackAfterMoving; i > 0; i--) {
			damageWithAssaultBonus += bestAttacker.damage(gameState, attackerPosition, crystalAttacked, crystalPosition) + 300;
		}
		if (damageWithAssaultBonus > bestDamage) {
			actions.addAll(BehaviourHelper.MoveTo(gameState, closestUnit, gameState.GetUnitPosition(closestUnit), chosenAssaultSquare, gameState.APLeft));
		}

		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, crystalPosition, gameState.APLeft-actions.size(), false));
		
		return actions;
	}

}
