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
			return new int[] {0, 0, actionPointsToSpend, 0};
		
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
		if (distanceToAttackRange > 0) {
			if (DivideCeil(distanceToAttackRange, healer.unitClass.speed) >= actionPointsToSpend)
				return new int[] {0, 0, 0, 0};
		}
		else
			distanceToAttackRange = 0;
		
		int actionPointsToSpendOnHealing = actionPointsToSpend - distanceToAttackRange;
		int actionPointsSpent = 0;
		int damageHealed = 0;
		int revived = 0;
		int initialHp = healed.hp;
		if (initialHp == 0) revived = 1;
		for (actionPointsSpent = 0; actionPointsSpent < actionPointsToSpendOnHealing; actionPointsSpent++) {
			int healAmount = gameState.GetHealAmount(healer, healed);
			if (healed.unitClass.maxHP - healed.hp > healAmount) {
				damageHealed += healAmount;
			}
			else {
				actionPointsSpent--;
				break;
			}
		}
		return new int[] {damageHealed, actionPointsToSpendOnHealing - actionPointsSpent, actionPointsSpent, revived};
	}
	
	/** 
	 * Tries to calculate a strategy consisting of 0-many chained actions that will kill and capture an enemy unit.
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
		ArrayList<UnitAction> actionsToRange = MoveUnitToRange(gameState, attacker, defenderPosition, attacker.unitClass.attack.range, actionPointsToSpend);
		if (actionsToRange.size() == 0 && attackerPosition.distance(defenderPosition) <= attacker.unitClass.attack.range)
			return actions;
		actionPointsToSpend -= actionsToRange.size();
		actions.addAll(actionsToRange);
		
		Position currentPosition = attackerPosition;
		if (actionsToRange.size() > 0)
			currentPosition = actionsToRange.get(actionsToRange.size()-1).to;
		
		//If unit only could move a little but still isn't in range
		if (currentPosition.distance(defenderPosition) <= attacker.unitClass.attack.range)
			return actions;
		
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
		if (actionPointsToSpend == 0)
			return beforeCapture;
		
		ArrayList<UnitAction> moveTo = MoveTo(gameState, attacker, currentPosition, defenderPosition, actionPointsToSpend);
		actionPointsToSpend -= moveTo.size();
		if (moveTo.size() > 0) {
			currentPosition = moveTo.get(moveTo.size()-1).to;
			actions.addAll(moveTo);
		}
		
		//Due to the possibility of path being blocked, costing extra action points to go around, it's possible not to be able to reach
		if (currentPosition == defenderPosition)
			return actions;
		else 
			return beforeCapture;
	}
	
	/** 
	 * Tries to calculate a strategy consisting of 0-many chained actions that will heal a friendly unit back to as close to full health without overhealing
	 * @param gameState
	 * @param healerPosition
	 * @param targetPosition
	 * @param actionPointsToSpend	How many action points should be used on this strategy
	 * @return						List of actions that contains moving into heal range, and healing for as many actionpoints as provided
	 */
	
	public static ArrayList<UnitAction> GetHealTargetStrategy(GameState gameState, Position healerPosition, Position targetPosition, int actionPointsToSpend) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Unit healer = gameState.units[healerPosition.x][healerPosition.y];
		Unit target = gameState.units[targetPosition.x][targetPosition.y];

		//while not in attack range and have more AP to spend, move towards attack range
		ArrayList<UnitAction> actionsToRange = MoveUnitToRange(gameState, healer, targetPosition, healer.unitClass.heal.range, actionPointsToSpend);
		
		//If unit couldn't move at all
		if (actionsToRange.size() == 0 && healerPosition.distance(targetPosition) <= healer.unitClass.heal.range)
			return actions;
		actionPointsToSpend -= actionsToRange.size();
		actions.addAll(actionsToRange);
		
		Position currentPosition = healerPosition;
		if (actionsToRange.size() > 0)
			currentPosition = actionsToRange.get(actionsToRange.size()-1).to;
		
		//If unit only could move a little but still isn't in range
		if (currentPosition.distance(targetPosition) <= healer.unitClass.heal.range)
			return actions;
		
		//Is there action points left to attack unit?
		if (actionPointsToSpend == 0) 				
			return actions;
		
		int targetHp = target.hp;
		while (actionPointsToSpend > 0) {
			int healAmount = gameState.GetHealAmount(healer, target);
			if (target.unitClass.maxHP - targetHp > healAmount) {
				actions.add(new UnitAction(currentPosition, targetPosition, UnitActionType.HEAL));
				actionPointsToSpend--;
				targetHp += healAmount;
			}
			else break;
		}
		
		return actions;
	}
	
	
	/**
	 * Returns a series of actions that will move a unit to a position with x amount of actionpoints.
	 * @param gameState
	 * @param unit 					Unit to move
	 * @param to					Where to move unit
	 * @param actionPointsToSpend	How many action points is the algorithm allowed to spend
	 * @return
	 */
	public static ArrayList<UnitAction> MoveTo(GameState gameState, Unit unit, Position from, Position to, int actionPointsToSpend) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Position currentPosition = from;
		
		while (currentPosition != to && actionPointsToSpend > 0) {
			UnitAction newAction = MoveTowardsTarget(gameState, unit, currentPosition, to, currentPosition.distance(to));
			if (newAction == null)
				return actions;
			currentPosition = newAction.to;
			actions.add(newAction);
			actionPointsToSpend--;
		}

		return actions;
	}
	

	/**
	 * Moves a unit 1 action toward a target
	 * @param gameState
	 * @param movingUnit		Unit to move
	 * @param from				The current location of the unit to move
	 * @param towards			Which position to move the unit towards
	 * @param maxMoveDistance	How far should the unit move
	 * @return
	 */
	public static UnitAction MoveTowardsTarget(GameState gameState, Unit movingUnit, Position from, Position towards, int maxMoveDistance) {
		ArrayList<Position> availablePositions = getAvailableMovePositions(gameState, movingUnit, from, maxMoveDistance);
		if (availablePositions.size() == 0) {
			//All spots within range are taken by other units
			return null;
		}
		Position closestPosition = GetClosestPositionToPoint(gameState, availablePositions, towards);
		
		//If it couldn't find a spot closer to the target, only further away
		if (from.distance(towards) < closestPosition.distance(towards))
			return null;
		return new UnitAction(from, closestPosition, UnitActionType.MOVE);
	}
	
	/**
	 * Calculates a series of actions that moves a unit just into attack range, but no further
	 * @param gameState
	 * @param moving				Unit to move
	 * @param attackSpot			Spot of unit to attack
	 * @param actionPointsToSpend	How many action points is the algorithm allowed to use
	 * @return
	 */
	public static ArrayList<UnitAction> MoveUnitToRange(GameState gameState, Unit moving, Position attackSpot, int range, int actionPointsToSpend) {
		ArrayList<UnitAction> actions = new ArrayList<UnitAction>();
		Position currentPosition = gameState.GetUnitPosition(moving);
		while(currentPosition.distance(attackSpot) > range && actionPointsToSpend > 0) {
			int moveDistance = currentPosition.distance(attackSpot) - range;
			int moveRange = (moveDistance < moving.unitClass.speed) ? moveDistance : moving.unitClass.speed;
			UnitAction newAction = MoveTowardsTarget(gameState, moving, currentPosition, attackSpot, moveRange);
			if (newAction == null) {
				return actions;
			}
			//System.out.println("Moving unit from: " + currentPosition.x + "," + currentPosition.y + " to: " + newAction.to.x + "," + newAction.to.y);
			actions.add(newAction);
			currentPosition = newAction.to;
			actionPointsToSpend--;
		}
		
		if (currentPosition.distance(attackSpot) > range)
			return actions;
		else return new ArrayList<UnitAction>();
		
		//TODO: Improvements: Do something in case the last move doesn't bring you in LOS of unit you want to attack
	}
	
	/**
	 * Divides a / b Ints and returns the ceiling value. Used primarily for calculating action point cost of moves.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int DivideCeil(int a, int b) {
		return (int) Math.ceil((double)a / (double)b);
	}
	
	/**
	 * Calculates the positions a unit can move to within a maximum move distance. Is more efficient than the GameState function, as it only checks inside move range
	 * @param gameState
	 * @param movingUnit		Unit to move
	 * @param unitPosition		Current position of unit to move
	 * @param maxMoveDistance	How far around the unit should the algorithm check
	 * @return
	 */
	public static ArrayList<Position> getAvailableMovePositions(GameState gameState, Unit movingUnit, Position unitPosition, int maxMoveDistance) {
		if (maxMoveDistance > movingUnit.unitClass.speed) maxMoveDistance = movingUnit.unitClass.speed;
		int speed = movingUnit.unitClass.speed - (movingUnit.unitClass.speed - maxMoveDistance);
		ArrayList<Position> availablePositions = new ArrayList<Position>();
		for (int x = unitPosition.x-speed; x < unitPosition.x+speed; x++) {
			for (int y = unitPosition.y-speed; y < unitPosition.y+speed; y++) {
				if (x < 0 || x >= gameState.map.width || y < 0 || y >= gameState.map.height) 
					continue;
				Position pos = new Position(x, y);
				/*
				System.out.println("CHECK POS: " + pos);
				System.out.println("CHECK POS IN RANGE " + (pos.distance(unitPosition ) <= speed));
				System.out.println("CHECK UNIT " + gameState.units[x][y]);
				*/
				if (gameState.units[x][y] == null && pos.distance(unitPosition) <= speed)
					availablePositions.add(pos);
			}
		}
		return availablePositions;
	}
	
	/**
	 * Finds the closest unit among an array of units to a point only factoring distance
	 * @param gameState
	 * @param units		Units to test
	 * @param pos		Position
	 * @return
	 */
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
	
	/**
	 * Finds the closest unit among an array of units to a point using movement speed. Least amount of action points used to move = closest
	 * @param gameState
	 * @param units			Units to test
	 * @param pos			Position
	 * @return
	 */
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
	
	/**
	 * Finds closest position to another position
	 * @param gameState
	 * @param positions		List of positions to test
	 * @param pos			Position to test against
	 * @return
	 */
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
	 * Calculates from 2 arrays of units, which combination of attacker from the attacker array, and target from the target array, that give the highest damage output.
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
	
	/**
	 * Calculates from 2 arrays of units, which combination of healer from the healer array, and target from the target array, that give the highest healing - Can prioritize reviving units
	 * @param gameState
	 * @param healers 			List of all potential healers
	 * @param potentialTargets	List of all potential targets for healers
	 * @param prioritizeRevive	Should the algorithm prioritize reviving dead units over maximum healing
	 * @return					index 0: Best healer (null if 0 healing was done) - Index 1: Target for best healer
	 */
	public static Unit[] CalculateBestHealingOnTargets(GameState gameState, ArrayList<Unit> healers, ArrayList<Unit> potentialTargets, boolean prioritizeRevive) {
		int bestHealing = 0;
		int nrOfAPSpent = Integer.MAX_VALUE;
		boolean didBestRevive = false;
		Unit bestHealer = null;
		Unit bestTarget = null;
		
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		for (Unit target : potentialTargets) {
			for (Unit healer : healers) {
				if (target == healer) continue;
				//TODO: Killed in least amount of AP should be prioritized over all others
				int[] result = BehaviourHelper.CalculateMaxHealing(gameState, healer, target, gameState.APLeft);
				if (result[3] == 1 && prioritizeRevive) {
					if (!didBestRevive) {
						bestHealing = result[0];
						nrOfAPSpent = result[1];
						bestHealer = healer;
						bestTarget = target;
						didBestRevive = true;
					}
					else {
						if (didBestRevive) {
							if (result[0] > bestHealing) {
								bestHealing = result[0];
								nrOfAPSpent = result[1];
								bestHealer = healer;
								bestTarget = target;
							}
							else if (result[0] == bestHealing) {
								if (result[1] < nrOfAPSpent) {
									nrOfAPSpent = result[1];
									bestHealer = healer;
									bestTarget = target;
								}
							}
						}
					}
				}				
				else if (result[0] > bestHealing) {
					bestHealing = result[0];
					nrOfAPSpent = result[1];
					bestHealer = healer;
					bestTarget = target;
				}
				else if (result[0] == bestHealing) {
					if (result[1] < nrOfAPSpent) {
						nrOfAPSpent = result[1];
						bestHealer = healer;
						bestTarget = target;
					}
				}
			}
		}
		
		if (bestHealing == 0) {
			bestHealer = null;
			bestTarget = null;
		}
		
		return new Unit[] {bestHealer, bestTarget};
	}
	
	/**
	 * Returns all damaged units from a team
	 * @param gameState
	 * @param isPlayer1
	 * @return
	 */
	public static ArrayList<Unit> GetDamagedUnits(GameState gameState, boolean isPlayer1) {
		ArrayList<Unit> damagedUnits = new ArrayList<Unit>();
		
		for (Unit unit : gameState.GetAllUnitsFromTeam(isPlayer1)) {
			if (unit.hp < unit.unitClass.maxHP)
				damagedUnits.add(unit);
		}
		
		return damagedUnits;
	}
	
	/**
	 * Returns all dead units on a team
	 * @param gameState
	 * @param isPlayer1
	 * @return
	 */
	public static ArrayList<Unit> GetDeadUnits(GameState gameState, boolean isPlayer1) {
		ArrayList<Unit> deadUnits = new ArrayList<Unit>();
		
		for (Unit unit : gameState.GetAllUnitsFromTeam(isPlayer1)) {
			if (unit.hp == 0)
				deadUnits.add(unit);
		}
		
		return deadUnits;
	}
}
