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
				
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		Unit[] bestPair = BehaviourHelper.CalculateBestAttackOnTargets(gameState, friendlyAttackUnits, healers, true);
				
		Unit bestAttacker = bestPair[0];
		Unit healerAttacked = bestPair[1];
		
		if (bestAttacker == null) {
			return fallbackBehaviour.GetActions(isPlayer1, gameState);
		}
		
		Position healerPosition = gameState.GetUnitPosition(healerAttacked);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);
		
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, healerPosition, gameState.APLeft, true));
		
		return actions;
	}

}
