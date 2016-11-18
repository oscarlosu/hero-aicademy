package marttoslo.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.winterbe.java8.samples.concurrent.ConcurrentUtils;

import ai.AI;
import ai.evaluation.HeuristicEvaluator;
import ai.evolution.OnlineEvolution;
import game.Game;
import game.GameArguments;
import game.GameState;
import model.DECK_SIZE;


public class Experiment {
	public static int budget = 1000; // 4 sec for AI's
	public static int gamesToPlay = 4;
	
	public static ExperimentResults results;
	
	public static void main(String[] args) {
		GameState.RANDOMNESS = false;
		try {
			long startTime = System.currentTimeMillis();
			
			ExecutorService executor = Executors.newWorkStealingPool();		
			// Make list with requested number of tasks
			List<Callable<ExperimentResults>> callables = new ArrayList<Callable<ExperimentResults>>();
			for(int i = 0; i < gamesToPlay; ++i) {
				callables.add(() -> { return runExperiment(); });
			}
			// Submit tasks
			List<Future<ExperimentResults>> futures = executor.invokeAll(callables);
			// Wait and output results
			int p1 = 0;
			int p2 = 0;
			int draws = 0;
			for(Future<ExperimentResults> f : futures) {
				ExperimentResults r;
				try {
					r = f.get();
					if(r.winnerIndex == 1) p1++;
					else if(r.winnerIndex == 2) p2++;
					else if(r.winnerIndex == 0) draws++;
					else System.out.println("Error. WinnerIndex = " + r.winnerIndex);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
			}
			
			System.out.println("Player 1: " + p1);
			System.out.println("Player 2: " + p2);
			System.out.println("Draws: " + draws);
			
			// Stop executor
			ConcurrentUtils.stop(executor);
			System.out.println("Done!");
			
			
			
			long endTime = System.currentTimeMillis();
			System.out.println("Elapsed time: " + (endTime - startTime));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static ExperimentResults runExperiment() {
			long seed = (long)(System.currentTimeMillis() * Math.random());
			
			// Init players
			// Online Evolution
			AI p1 = new OnlineEvolution(true, 100, 0.1, 0.5, budget, new HeuristicEvaluator(false), false);	
			((OnlineEvolution)p1).setSeed(seed);
			// RHCA
			AI p2 = new OnlineCoevolution(100, 30, 0.3, budget, new HeuristicEvaluator(false), false);
			((OnlineCoevolution)p2).setSeed(seed);
			
			// Init game			
			GameArguments gameArgs = new GameArguments(false, p1, p2, "a", DECK_SIZE.STANDARD);
			gameArgs.budget = budget;
			gameArgs.gfx = false;
			Game game = new Game(null, gameArgs, seed);
			
			// Run game
			game.run();
			
			// Save results
			ExperimentResults results = new ExperimentResults();
			results.winnerIndex = game.state.getWinner();
			return results;
	}
}
