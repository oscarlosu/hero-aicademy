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

public class FocusAttackHealer extends Behaviour {

	private BehaviourType fallbackBehaviour;
	public FocusAttackHealer(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> healers = gameState.GetAllUnitsOfType(!isPlayer1, true, Card.CLERIC);
		if (healers.size() == 0) 
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
//		for (Unit u : healers) {
//			System.out.print(gameState.GetUnitPosition(u) + " - ");
//		}
//		System.out.println();
		ArrayList<Unit> friendlyAttackUnits = gameState.GetAllUnitsOfType(isPlayer1, false, Card.ARCHER, Card.WIZARD, Card.NINJA);
		if (friendlyAttackUnits.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
				
		//Priotitizes damage - Would rather spend more action points on using Unit that can deal more damage
		Unit[] bestPair = BehaviourHelper.CalculateBestAttackOnTargets(gameState, friendlyAttackUnits, healers, true);
				
		Unit bestAttacker = bestPair[0];
		Unit healerAttacked = bestPair[1];
		
		if (bestAttacker == null) {
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		}
		
		Position healerPosition = gameState.GetUnitPosition(healerAttacked);
		Position attackerPosition = gameState.GetUnitPosition(bestAttacker);
		actions.addAll(BehaviourHelper.GetAttackTargetUntilDeadAndCaptureStrategy(gameState, attackerPosition, healerPosition, gameState.APLeft, true));
		
		return actions;
	}

}
