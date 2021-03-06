package marttoslo.portfolio.behaviours;

import java.util.ArrayList;

import action.Action;
import action.DropAction;
import game.GameState;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.PortfolioController.BehaviourType;
import model.Card;
import model.Position;
import model.SquareType;

public class SpawnDefense extends Behaviour {
	
	Card[] unitsToSpawn = new Card[]{
			Card.ARCHER, Card.WIZARD, Card.NINJA
	};
	
	private BehaviourType fallbackBehaviour;
	public SpawnDefense(BehaviourType fallback) {
		fallbackBehaviour = fallback;
	}
	
	@Override
	public ArrayList<Action> GetActions(boolean isPlayer1, GameState gameState) {
		ArrayList<Action> actions = new ArrayList<Action>();
		ArrayList<Card> unitsOnHand = gameState.GetCardsFromHand(isPlayer1, unitsToSpawn);
		if (unitsOnHand.size() == 0)
			return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
		
		//TODO: Prioritized list or implement seeded random for selecting unit to spawn?
		
		Card unit = unitsOnHand.get(0);
		
		//TODO: How to select spawn location?
		
		SquareType deploySquareType = (isPlayer1) ? SquareType.DEPLOY_1 : SquareType.DEPLOY_2;
		ArrayList<Position> deploySquares = gameState.GetSquarePositions(deploySquareType);
		for (Position pos : deploySquares) {
			if (gameState.units[pos.x][pos.y] == null) {
				actions.add(new DropAction(unit, pos));

				return actions;
			}
		}

		return PortfolioController.GetActions(gameState, isPlayer1, fallbackBehaviour);
	}

}
