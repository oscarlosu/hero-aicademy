package marttoslo.helpers;

import java.util.ArrayList;

import action.UnitAction;
import action.UnitActionType;
import game.GameState;
import model.Position;
import model.Unit;

public class BehaviourHelper {

	/**
	 * Calculates how much damage can be done with the amount of given action points available
	 * @param gameState
	 * @param attacker
	 * @param defender
	 * @param actionPointsToSpend	Default to 5
	 * @return	index 0: How much damage was dealt - index 1: How many action points was spent on attacking - index 2: How many action points was spent in total - index 3: 0/1 if unit was killed
	 */
	public static int[] CalculateMaxDamage(GameState gameState, Unit attacker, Unit defender, int actionPointsToSpend) {
		Position attackerPosition = gameState.GetUnitPosition(attacker);
		Position defenderPosition = gameState.GetUnitPosition(defender);
		int distanceToAttackRange = attackerPosition.distance(defenderPosition) - attacker.unitClass.attack.range;
		
		if (DivideCeil(distanceToAttackRange, attacker.unitClass.speed) >= actionPointsToSpend)
			return new int[] {0, actionPointsToSpend};
		
		int actionPointsToSpendOnAttacking = actionPointsToSpend - distanceToAttackRange;
		int actionPointsSpent = 0;
		int damage = 0;
		int wasKilled = 0;
		for (actionPointsSpent = 0; actionPointsSpent < actionPointsToSpendOnAttacking; actionPointsSpent++) {
			damage += attacker.damage(gameState, attackerPosition, defender, defenderPosition);
			if (damage > defender.hp) {
				damage = defender.hp;
				wasKilled = 1;
				break;
			}
		}
		return new int[] {damage, actionPointsToSpendOnAttacking - actionPointsSpent, actionPointsSpent, wasKilled};
	}
	
	/**
	 * Calculates how much healing can be done with the amount of given action points available
	 * @param gameState
	 * @param attacker
	 * @param defender
	 * @param actionPointsToSpend	Default to 5
	 * @return	index 0: How much healing was done - index 1: How many action points was spent on healing - index 2: How many action points was spent in total - Index 3: Revived dead unit
	 */
	public static int[] CalculateMaxHealing(GameState gameState, Unit healer, Unit healed, int actionPointsToSpend) {
		Position healerPosition = gameState.GetUnitPosition(healer);
		Position healedPosition = gameState.GetUnitPosition(healed);
		int distanceToAttackRange = healerPosition.distance(healedPosition) - healer.unitClass.attack.range;
		
		if (DivideCeil(distanceToAttackRange, healer.unitClass.speed) >= actionPointsToSpend)
			return new int[] {0, actionPointsToSpend};
		
		int actionPointsToSpendOnHealing = actionPointsToSpend - distanceToAttackRange;
		int actionPointsSpent = 0;
		int damageHealed = 0;
		int revived = 0;
		int initialHp = healed.hp;
		if (initialHp == 0) revived = 1;
		for (actionPointsSpent = 0; actionPointsSpent < actionPointsToSpendOnHealing; actionPointsSpent++) {
			if (healed.unitClass.maxHP - healed.hp > 100) {
				damageHealed += gameState.GetHealAmount(healer, healed);
				break;
			}
		}
		return new int[] {damageHealed, actionPointsToSpendOnHealing - actionPointsSpent, actionPointsSpent, revived};
	}
	
	/**
	 * 
	 * @param gameState
	 * @param attackerPosition
	 * @param defenderPosition
	 * @param actionPointsToSpend	How many action points should be used on this strategy
	 * @param shouldCapture			Should the strategy try to capture the unit after killing it?
	 * @return
	 */
	
	public static ArrayList<UnitAction> GetAttackTargetUntilDeadAndCaptureStrategy(GameState gameState, Position attackerPosition, Position defenderPosition, int actionPointsToSpend, boolean shouldCapture) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Unit attacker = gameState.units[attackerPosition.x][attackerPosition.y];
		Unit defender = gameState.units[defenderPosition.x][defenderPosition.y];
		
		//while not in attack range and have more AP to spend, move towards attack range
		ArrayList<UnitAction> actionsToRange = MoveUnitToAttackRange(gameState, attacker, defenderPosition, actionPointsToSpend);
		actionPointsToSpend -= actionsToRange.size();
		actions.addAll(actionsToRange);
		Position currentPosition = actionsToRange.get(actionsToRange.size()-1).to;
		
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
		if (DivideCeil(currentPosition.distance(defenderPosition), attacker.unitClass.speed) < actionPointsToSpend
				|| !shouldCapture) 
			return actions;
		
		ArrayList<UnitAction> beforeCapture = new ArrayList<UnitAction>(actions);
		ArrayList<UnitAction> moveTo = MoveTo(gameState, attacker, defenderPosition, actionPointsToSpend);
		actionPointsToSpend -= moveTo.size();
		currentPosition = moveTo.get(moveTo.size()-1).to;
		actions.addAll(moveTo);
		
		//Due to the possibility of path being blocked, costing extra action points to go around, it's possible not to be able to reach
		if (currentPosition == defenderPosition)
			return actions;
		else 
			return beforeCapture;
	}
	
	public static ArrayList<UnitAction> MoveTo(GameState gameState, Unit unit, Position to, int actionPointsToSpend) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Position currentPosition = gameState.GetUnitPosition(unit);
		
		while (currentPosition != to && actionPointsToSpend > 0) {
			UnitAction newAction = MoveTowardsTarget(gameState, unit, currentPosition, to, currentPosition.distance(to));
			currentPosition = newAction.to;
			actions.add(newAction);
			actionPointsToSpend--;
		}
		
		return actions;
	}
	
	public static UnitAction MoveTowardsTarget(GameState gameState, Unit movingUnit, Position from, Position towards, int maxMoveDistance) {
		ArrayList<Position> availablePositions = getAvailableMovePositions(gameState, movingUnit, from, maxMoveDistance);
		Position closestPosition = GetClosestPositionToPoint(gameState, availablePositions, towards);
		return new UnitAction(from, closestPosition, UnitActionType.MOVE);
	}
	
	public static ArrayList<UnitAction> MoveUnitToAttackRange(GameState gameState, Unit moving, Position attackSpot, int actionPointsToSpend) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Position currentPosition = gameState.GetUnitPosition(moving);
		
		while(currentPosition.distance(attackSpot) > moving.unitClass.attack.range && actionPointsToSpend > 0) {
			int moveDistance = currentPosition.distance(attackSpot) - moving.unitClass.attack.range;
			UnitAction newAction = MoveTowardsTarget(gameState, moving, currentPosition, attackSpot, moveDistance);
			actions.add(newAction);
			currentPosition = newAction.to;
			actionPointsToSpend--;
		}
		
		if (currentPosition.distance(attackSpot) > moving.unitClass.attack.range)
			return actions;
		else return new ArrayList<UnitAction>();
		
		//TODO: Improvements: Do something in case the last move doesn't bring you in LOS of unit you want to attack
	}
	
	public static int DivideCeil(int a, int b) {
		return (int) Math.ceil((double)a / (double)b);
	}
	
	public static ArrayList<Position> getAvailableMovePositions(GameState gameState, Unit movingUnit, Position unitPosition, int maxMoveDistance) {
		int speed = movingUnit.unitClass.speed - (movingUnit.unitClass.speed - maxMoveDistance);
		ArrayList<Position> availablePositions = new ArrayList<Position>();
		for (int x = unitPosition.x-speed; x < unitPosition.x+speed; x++) {
			for (int y = unitPosition.y-speed; y < unitPosition.y+speed; y++) {
				if (x < 0 || x >= gameState.map.width || y < 0 || y >= gameState.map.height) 
					continue;
				Position pos = new Position(x, y);
				if (gameState.units[x][y] == null && pos.distance(unitPosition) <= speed)
					availablePositions.add(pos);
			}
		}
		return availablePositions;
	}
	
	public static Unit GetClosestUnitToPoint(GameState gameState, ArrayList<Unit> units, Position pos) {
		int closestDistance = Integer.MAX_VALUE;
		Unit closestUnit = null;
		for (Unit unit : units) {
			Position unitPos = gameState.GetUnitPosition(unit);
			if (unitPos.distance(pos) < closestDistance) {
				closestDistance = unitPos.distance(pos);
				closestUnit = unit;
			}
		}
		return closestUnit;
	}
	
	public static Unit GetClosestUnitToPointUsingSpeed(GameState gameState, ArrayList<Unit> units, Position pos) {
		int bestMoveCost = Integer.MAX_VALUE;
		Unit closestUnit = null;
		for (Unit unit : units) {
			Position unitPos = gameState.GetUnitPosition(unit);
			int moveCost = DivideCeil(unitPos.distance(pos), unit.unitClass.speed);
			if (moveCost < bestMoveCost) {
				bestMoveCost = moveCost;
				closestUnit = unit;
			}
		}
		return closestUnit;
	}
	
	public static Position GetClosestPositionToPoint(GameState gameState, ArrayList<Position> positions, Position pos) {
		int closestDistance = Integer.MAX_VALUE;
		Position closestPos = null;
		for (Position p : positions) {
			if (p.distance(pos) < closestDistance) {
				closestDistance = p.distance(pos);
				closestPos = p;
			}
		}
		return closestPos;
	}
	
	
	/**
	 * 
	 * @param gameState
	 * @param attackers 		List of all potential attacking units
	 * @param potentialTargets	List of all potential targets for attacking units
	 * @param prioritizeKills	Should the algorithm prioritize attackers that can kill a unit, over doing most damage?
	 * @return					index 0: Best attacker (null if 0 damage can be dealt) - Index 1: Target for best attacker
	 */
	public static Unit[] CalculateBestAttackOnTargets(GameState gameState, ArrayList<Unit> attackers, ArrayList<Unit> potentialTargets, boolean prioritizeKills) {
		int bestDamage = 0;
		int nrOfAPSpent = Integer.MAX_VALUE;
		boolean didBestKill = false;
		Unit bestAttacker = null;
		Unit bestTarget = null;
		
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		for (Unit target : potentialTargets) {
			for (Unit attacker : attackers) {
				
				//TODO: Killed in least amount of AP should be prioritized over all others
				int[] result = BehaviourHelper.CalculateMaxDamage(gameState, attacker, target, gameState.APLeft);
				if (result[3] == 1 && prioritizeKills) {
					if (!didBestKill) {
						bestDamage = result[0];
						nrOfAPSpent = result[1];
						bestAttacker = attacker;
						bestTarget = target;
						didBestKill = true;
					}
					else {
						if (didBestKill) {
							if (result[0] > bestDamage) {
								bestDamage = result[0];
								nrOfAPSpent = result[1];
								bestAttacker = attacker;
								bestTarget = target;
							}
							else if (result[0] == bestDamage) {
								if (result[1] < nrOfAPSpent) {
									nrOfAPSpent = result[1];
									bestAttacker = attacker;
									bestTarget = target;
								}
							}
						}
					}
				}				
				else if (result[0] > bestDamage) {
					bestDamage = result[0];
					nrOfAPSpent = result[1];
					bestAttacker = attacker;
					bestTarget = target;
				}
				else if (result[0] == bestDamage) {
					if (result[1] < nrOfAPSpent) {
						nrOfAPSpent = result[1];
						bestAttacker = attacker;
						bestTarget = target;
					}
				}
			}
		}
		
		if (bestDamage == 0) {
			bestAttacker = null;
			bestTarget = null;
		}
		
		return new Unit[] {bestAttacker, bestTarget};
	}
	
	public static ArrayList<Unit> GetDamagedUnits(GameState gameState, boolean isPlayer1) {
		ArrayList<Unit> damagedUnits = new ArrayList<Unit>();
		
		for (Unit unit : gameState.GetAllUnits(isPlayer1)) {
			if (unit.hp < unit.unitClass.maxHP)
				damagedUnits.add(unit);
		}
		
		return damagedUnits;
	}
	
	public static ArrayList<Unit> GetDeadUnits(GameState gameState, boolean isPlayer1) {
		ArrayList<Unit> deadUnits = new ArrayList<Unit>();
		
		for (Unit unit : gameState.GetAllUnits(isPlayer1)) {
			if (unit.hp == 0)
				deadUnits.add(unit);
		}
		
		return deadUnits;
	}
}
