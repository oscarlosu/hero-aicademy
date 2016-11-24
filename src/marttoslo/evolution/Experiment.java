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
import pacman.game.util.IO;


public class Experiment {
	public static int budget = 1000; // 4 sec for AI's
	public static int gamesToPlay = 1;
	
	public static boolean useThreads = false;
	public static boolean enableGfx = true;
	public static boolean saveToFile = true;
	
	public static void main(String[] args) {
		GameState.RANDOMNESS = true;
		if(useThreads) {
			threadedExperiment();
		} else {
			sequentialExperiment();
		}
	}
	
	public static void sequentialExperiment() {
		long timestamp = System.currentTimeMillis();
		int p1 = 0;
		int p2 = 0;
		int draws = 0;
		double avgTurns = 0;
		ExperimentResultsCollection resCol = new ExperimentResultsCollection();
		for(int i = 0; i <  gamesToPlay; ++i) {
			ExperimentResults r = runExperiment();
			if(r.winnerIndex == 0) draws++;
			else if(r.winnerIndex == 1) p1++;
			else if(r.winnerIndex == 2) p2++;
			
			avgTurns += r.turns;
			resCol.collection.add(r);
			
			System.out.println("Game ended. Result: " + r.winnerIndex);
		}
		
		System.out.println("Player 1: " + p1);
		System.out.println("Player 2: " + p2);
		System.out.println("Draws: " + draws);
		System.out.println("Avg turns: " + (avgTurns / (double)gamesToPlay));
		
		System.out.println("Done!");
		
		if(saveToFile) {
			resCol.SaveToFile("results_" + timestamp + ".json");
		}
	}
	
	public static void threadedExperiment() {
		try {
			long startTime = System.currentTimeMillis();
			
			ExecutorService executor = Executors.newWorkStealingPool();		
			// Submit games to executor
			List<Future<ExperimentResults>> futures = new ArrayList<Future<ExperimentResults>>();
			for(int i = 0; i < gamesToPlay; ++i) {
				futures.add(executor.submit(() -> { return runExperiment(); }));
			}
			int p1 = 0;
			int p2 = 0;
			int draws = 0;
			int gamesCompleted = 0;
			float avgTurns = 0;
			ExperimentResults r;
			while(gamesCompleted < gamesToPlay) {
				long endTime = System.currentTimeMillis();
				System.out.println("Main thread checking status." + " Elapsed time: " + ((endTime - startTime) / 1000.0f) + "s");
				for(int i = 0; i < futures.size(); ++i) {					
					Future<ExperimentResults> f = futures.get(i);
					if(f.isCancelled()) {
						// Error in thread
						endTime = System.currentTimeMillis();
						System.out.println("Task failed. Submitting new task." + " Elapsed time: " + ((endTime - startTime) / 1000.0f) + "s");
						futures.set(i, executor.submit(() -> { return runExperiment(); }));
					} else if(f.isDone()) {
						// Completed successfully?
						try {
							System.out.println("Trying to get Future.");
							r = f.get();
							if(r.winnerIndex == 1) p1++;
							else if(r.winnerIndex == 2) p2++;
							else if(r.winnerIndex == 0) draws++;
							gamesCompleted++;
							avgTurns += r.turns;
							endTime = System.currentTimeMillis();
							System.out.println(r.toString());
						} catch (ExecutionException e) {
							System.out.println("Exception on Future.get().");
							e.printStackTrace();
						}
					}
				}				
				Thread.sleep(5000);
			}
			
			
			System.out.println("Player 1: " + p1);
			System.out.println("Player 2: " + p2);
			System.out.println("Draws: " + draws);
			System.out.println("Avg turns: " + (avgTurns / (float)gamesCompleted));
			
			// Stop executor
			ConcurrentUtils.stop(executor);
			System.out.println("Done!");
			
			
			
			long endTime = System.currentTimeMillis();
			System.out.println("Elapsed time: " + ((endTime - startTime) / 1000.0f) + "s");
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
			gameArgs.gfx = enableGfx;
			Game game = new Game(null, gameArgs, seed);
			
			// Run game
			game.run();
			
			// Save results
			ExperimentResults results = new ExperimentResults();
			results.winnerIndex = game.state.getWinner();
			results.turns = game.state.turn;
			results.seed = seed;
			results.crystalWin = game.state.wasCrystalWin();
			results.unitWin = results.winnerIndex != 0 && !game.state.wasCrystalWin();
			// Coevolution stats
			results.co_generations.addAll(((OnlineCoevolution)p2).generations);
			results.co_sumChampionHostFindGen = ((OnlineCoevolution)p2).sumChampionHostFindGen;
			results.co_sumChampionParasiteFindGen = ((OnlineCoevolution)p2).sumChampionParasiteFindGen;
			results.co_championHostFitnesses.addAll(((OnlineCoevolution)p2).championHostFitnesses);
			results.co_championParasiteFitnesses.addAll(((OnlineCoevolution)p2).championParasiteFitnesses);
			// Online Evolution stats
			results.oe_generations.addAll(((OnlineEvolution)p1).generations);
			results.oe_sumChampionHostFindGen = ((OnlineEvolution)p1).sumChampionFindGen;
			results.oe_championFitnesses.addAll(((OnlineEvolution)p1).championFitnesses);
			
			return results;
	}
}
