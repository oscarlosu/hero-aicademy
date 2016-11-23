package marttoslo.helpers;

import java.util.ArrayList;

import action.Action;
import action.UnitAction;
import action.UnitActionType;
import game.GameState;
import model.Direction;
import model.Position;
import model.Unit;

public class BehaviourHelper {

	/**
	 * Calculates how much damage can be done with the amount of given action points available
	 * @param gameState
	 * @param attacker
	 * @param defender
	 * @param actionPointsToSpend	Default to 5
	 * @return	index 0: How much damage was dealt - index 1: how many action points was spent
	 */
	public static int[] CalculateMaxDamage(GameState gameState, Position attackerPosition, Position defenderPosition, int actionPointsToSpend) {
		Unit attacker = gameState.units[attackerPosition.x][attackerPosition.y];
		Unit defender = gameState.units[defenderPosition.x][defenderPosition.y];
		int distanceToAttackRange = attackerPosition.distance(defenderPosition) - attacker.unitClass.attack.range;
		
		if (distanceToAttackRange / attacker.unitClass.speed >= actionPointsToSpend)
			return new int[] {0, actionPointsToSpend};
		
		int actionPointsToSpendOnAttacking = actionPointsToSpend - distanceToAttackRange;
		int actionPointsSpent = 0;
		int damage = 0;
		for (actionPointsSpent = 0; actionPointsSpent < actionPointsToSpendOnAttacking; actionPointsSpent++) {
			damage += attacker.damage(gameState, attackerPosition, defender, defenderPosition);
			if (damage > defender.hp) {
				damage = defender.hp;
				break;
			}
		}
			return new int[] {damage, actionPointsToSpendOnAttacking - actionPointsSpent};
	}
	
	public static ArrayList<UnitAction> GetAttackTargetUntilDeadAndCaptureStrategy(GameState gameState, Position attackerPosition, Position defenderPosition, int actionPointsToSpend) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Unit attacker = gameState.units[attackerPosition.x][attackerPosition.y];
		Unit defender = gameState.units[defenderPosition.x][defenderPosition.y];
		int distanceToAttackRange = attackerPosition.distance(defenderPosition) - attacker.unitClass.attack.range;
		
		//while not in attack range and have more AP to spend, move towards attack range
		Position currentPosition = attackerPosition;
		while (distanceToAttackRange > 0 && actionPointsToSpend > 0) {
			UnitAction newAction = MoveTowardsTarget(gameState, currentPosition, defenderPosition);
			actions.add(newAction);
			currentPosition = newAction.to;
			actionPointsToSpend--;
		}
		
		//Is there action points left to attack unit?
		if (actionPointsToSpend == 0) 				
			return actions;
		
		int defenderHp = defender.hp;
		while (actionPointsToSpend > 0 && defenderHp > 0) {
			actions.add(new UnitAction(currentPosition, defenderPosition, UnitActionType.ATTACK));
			actionPointsToSpend--;
			defenderHp -= attacker.damage(gameState, currentPosition, defender, defenderPosition);
		}

		//Is there action points left to capture unit? If not, we stop here
		if (currentPosition.distance(defenderPosition) / attacker.unitClass.speed < actionPointsToSpend) 
			return actions;
		
		ArrayList<UnitAction> beforeCapture = new ArrayList<UnitAction>(actions);
		
		while (currentPosition != defenderPosition && actionPointsToSpend > 0) {
			UnitAction newAction = MoveTowardsTarget(gameState, currentPosition, defenderPosition);
			currentPosition = newAction.to;
			actions.add(newAction);
			actionPointsToSpend--;
		}
		
		//Due to the possibility of path being blocked, costing extra action points to go around, it's possible not to be able to reach
		if (currentPosition == defenderPosition)
			return actions;
		else 
			return beforeCapture;
		
	}
	
	public static UnitAction MoveTowardsTarget(GameState gameState, Position from, Position towards) {
		Unit movingUnit = gameState.units[from.x][from.y];
		Direction direction = from.getDirection(towards);
		Position to = new Position(from.x + direction.x * movingUnit.unitClass.speed, from.y +  direction.y * movingUnit.unitClass.speed);
		if (gameState.units[to.x][to.y] == null) {
			return new UnitAction(from, to, UnitActionType.MOVE);
		}
		else return MoveAroundObstacle(gameState, from, towards, to);
	}
	
	public static UnitAction MoveAroundObstacle(GameState gameState, Position from, Position goal, Position Obstacle) {
		ArrayList<Position> freePositions = new ArrayList<Position>();
		if (gameState.units[from.x-1][from.y] == null)
			freePositions.add(new Position(from.x-1, from.y));

		if (gameState.units[from.x+1][from.y] == null)
			freePositions.add(new Position(from.x+1, from.y));

		if (gameState.units[from.x][from.y-1] == null)
			freePositions.add(new Position(from.x, from.y-1));

		if (gameState.units[from.x][from.y+1] == null)
			freePositions.add(new Position(from.x, from.y+1));
		
		if (freePositions.size() == 0) return null;
		
		Position bestPosition = null;
		int shortestDistance = Integer.MAX_VALUE;
		for (Position pos : freePositions) {
			if (pos.distance(goal) < shortestDistance) {
				bestPosition = pos;
				shortestDistance = pos.distance(goal);
			}
		}
		return new UnitAction(from, bestPosition, UnitActionType.MOVE);
	}
}
