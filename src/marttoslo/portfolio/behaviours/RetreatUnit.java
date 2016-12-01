package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import game.GameState;
import marttoslo.helpers.BehaviourHelper;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Position;
import model.Unit;

public class RetreatUnit extends Behaviour {
		
	private BehaviourType fallbackBehaviour;
	public RetreatUnit(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Unit> units = gameState.GetAllUnitsFromTeam(isPlayer1);
		if (units.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		Unit unitToAdvance = units.get(PortfolioController.random.nextInt(units.size()));
		Position unitPosition = gameState.GetUnitPosition(unitToAdvance);
		
		int x = (isPlayer1) ? 0: gameState.map.width-1 ;
		int y = PortfolioController.random.nextInt(gameState.map.height);
		Position moveTowards = new Position(x, y);
		
		actions.add(BehaviourHelper.MoveTowardsTarget(gameState, unitToAdvance, unitPosition, moveTowards, unitToAdvance.unitClass.speed));
		
		return actions;
	}

}
