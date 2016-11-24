package marttoslo.portfolio.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import model.Card;
import model.Position;
import model.Unit;

public class FocusAttackCrystal extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> crystals = gameState.GetAllUnitsOfType(!isPlayer1, Card.CRYSTAL);
		if (crystals.size() == 0) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, Card.ARCHER, Card.WIZARD, Card.NINJA, Card.CLERIC, Card.KNIGHT);
		if (friendlyAttackUnits.size() == 0)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
		int bestDamage = 0;
		int nrOfAPSpent = Integer.MAX_VALUE;
		int nrOfAPSpentOnAttack = 0;
		Unit bestAttacker = null;
		Unit crystalAttacked = null;
		HashMap<Unit, Position> positions = new HashMap<Unit, Position>();
		
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		for (Unit crystal : crystals) {
			Position crystalPos = gameState.GetUnitPosition(crystal);
			positions.put(crystal, crystalPos);
			for (Unit attacker : friendlyAttackUnits) {
				Position attackerPos = gameState.GetUnitPosition(attacker);
				positions.put(crystal, attackerPos);
				
				int[] result = BehaviourHelper.CalculateMaxDamage(gameState, attackerPos, crystalPos, gameState.ACTION_POINTS);
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
		
		List<Position> assaultSquares = gameState.map.assaultSquares;
		Unit closestUnit = null;
		Position chosenAssaultSquare = null;
		int closestDistance = Integer.MAX_VALUE;
		
		for (Position assPos : assaultSquares) {
			for (Unit unit : friendlyAttackUnits) {
				int distance = positions.get(unit).distance(assPos);
				if (distance < closestDistance && unit != bestAttacker) {
					closestDistance = distance;
					chosenAssaultSquare = assPos;
					closestUnit = unit;
				}
			}
		}

		Position crystalPosition = positions.get(crystalAttacked);
		Position attackerPosition = positions.get(bestAttacker);
		int actionPointsLeftToAttackAfterMoving = nrOfAPSpentOnAttack - closestDistance;

		//If there's already a unit standing on an assaultSquare or we don't have enough action points to move one there and attack return only attack actions
		if (closestDistance == 0 || actionPointsLeftToAttackAfterMoving <= 0) {
			actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, crystalPosition, gameState.ACTION_POINTS, false));
			return actions;
		}
		
		int damageWithAssaultBonus = 0;
		for (int i = actionPointsLeftToAttackAfterMoving; i < 0; i--) {
			damageWithAssaultBonus += bestAttacker.damage(gameState, attackerPosition, crystalAttacked, crystalPosition) + 300;
		}
		if (damageWithAssaultBonus > bestDamage) {
			actions.addAll(BehaviourHelper.MoveTo(gameState, closestUnit, attackerPosition, chosenAssaultSquare, gameState.ACTION_POINTS));
		}

		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, crystalPosition, gameState.ACTION_POINTS-actions.size(), false));
		
		return actions;
	}

}
