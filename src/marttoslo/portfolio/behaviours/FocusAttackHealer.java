package marttoslo.portfolio.behaviours;

import java.util.ArrayList;
import java.util.HashMap;

import action.Action;
import action.UnitAction;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import model.Card;
import model.Position;
import model.Unit;

public class FocusAttackHealer extends Behaviour {

	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> healers = gameState.GetAllUnitsOfType(!isPlayer1, Card.CLERIC);
		if (healers.size() == 0) 
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, Card.ARCHER, Card.WIZARD, Card.NINJA);
		if (friendlyAttackUnits.size() == 0)
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		
		int bestDamage = 0;
		int nrOfAPSpent = Integer.MAX_VALUE;
		Unit bestAttacker = null;
		Unit healerAttacked = null;
		HashMap<Unit, Position> positions = new HashMap<Unit, Position>();
		
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		for (Unit healer : healers) {
			Position healerPos = gameState.GetUnitPosition(healer);
			positions.put(healer, healerPos);
			for (Unit attacker : friendlyAttackUnits) {
				Position attackerPos = gameState.GetUnitPosition(attacker);
				positions.put(healer, attackerPos);
				
				int[] result = BehaviourHelper.CalculateMaxDamage(gameState, attackerPos, healerPos, gameState.ACTION_POINTS);
				if (result[0] > bestDamage) {
					bestDamage = result[0];
					nrOfAPSpent = result[1];
					bestAttacker = attacker;
					healerAttacked = healer;
				}
				else if (result[0] == bestDamage) {
					if (result[1] < nrOfAPSpent) {
						nrOfAPSpent = result[1];
						bestAttacker = attacker;
						healerAttacked = healer;
					}
				}
			}
		}
				
		Position healerPosition = positions.get(healerAttacked);
		Position attackerPosition = positions.get(bestAttacker);
		
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, healerPosition, gameState.ACTION_POINTS, true));
		
		return actions;
	}

}
