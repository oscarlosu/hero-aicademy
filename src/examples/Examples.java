package examples;

import ai.AI;
import ai.GreedyActionAI;
import ai.GreedyTurnAI;
import ai.RandomAI;
import ai.evaluation.HeuristicEvaluator;
import ai.evaluation.RolloutEvaluator;
import ai.evolution.OnlineEvolution;
import ai.evolution.OnlineIslandEvolution;
import ai.mcts.Mcts;
import ai.util.RAND_METHOD;
import model.DECK_SIZE;
import game.Game;
import game.GameArguments;
import game.GameState;
import marttoslo.evolution.OnlineCoevolution;

public class Examples {

	public static void main(String[] args) {
		
		//humanVsHuman();
		humanVsAI();
		//noGfx();
		
	}

	private static void noGfx() {
		
		AI p1 = new RandomAI(RAND_METHOD.BRUTE);
		AI p2 = new RandomAI(RAND_METHOD.BRUTE);
		
		GameArguments gameArgs = new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.gfx = false; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		
	}

	private static void humanVsAI() {
		
		int budget = 4000; // 4 sec for AI's
		boolean stepped = false;
		boolean setSeed = true;
		long seed = System.currentTimeMillis();
		
		//AI p1 = new OnlineCoevolution(100, 30, 0.3, budget, new HeuristicEvaluator(false));
		//AI p1 = null;
		//AI p2 = new RandomAI(RAND_METHOD.BRUTE);
		//AI p2 = new GreedyActionAI(new HeuristicEvaluator(false));
		//AI p2 = new GreedyTurnAI(new HeuristicEvaluator(false), budget);
		
		// Online Evolution
		AI p1 = new OnlineEvolution(true, 100, 0.1, 0.5, budget, new HeuristicEvaluator(false), stepped);	
		if(setSeed) {
			((OnlineEvolution)p1).setSeed(seed);
		}
		// RHCA
		AI p2 = new OnlineCoevolution(100, 30, 0.3, budget, new HeuristicEvaluator(false), stepped);
		if(setSeed) {
			((OnlineCoevolution)p2).setSeed(seed);
		}
		// MCTS
		// Seed policy for mcts
//		AI policy = new RandomAI(RAND_METHOD.TREE);
//		if(setSeed) {
//			((RandomAI)policy).setSeed(seed);
//		}
//		AI p2 = new Mcts(budget, new RolloutEvaluator(1, 1, policy, new HeuristicEvaluator(false)), stepped);
		
		GameState.RANDOMNESS = !setSeed;
		GameArguments gameArgs = new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.budget = budget; 
		Game game = new Game(null, gameArgs, seed);
		
		
		game.run();
		
		System.out.println("Game seed was: " + seed);
		
	}

	private static void humanVsHuman() {
		
		AI p1 = null;
		AI p2 = null;
		
		Game game = new Game(null, new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD));
		game.run();
		
	}

}
