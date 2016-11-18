package marttoslo.evolution;

import java.util.ArrayList;
import java.util.List;

import ai.AI;
import ai.evaluation.HeuristicEvaluator;
import ai.evolution.OnlineEvolution;
import game.Game;
import game.GameArguments;
import game.GameState;
import model.DECK_SIZE;

public class Experiment implements Runnable {
	public static int nCores = 8;
	public static int budget = 1000; // 4 sec for AI's
	public static int doneCheckInterval = budget; // main thread checks if done every budget ms
	public static int gamesToPlay = 10;
	
	public ExperimentResults results;
	
	public static void main(String[] args) {
		try {
			ExperimentResults results = new ExperimentResults();
			List<Thread> threads = new ArrayList<Thread>();
			for(int i = 0; i < nCores; ++i) {
				Thread t = new Thread(new Experiment(results));
				threads.add(t);
				t.start();
			}
			
			while(!allThreadsDone(threads)) {			
				Thread.sleep(doneCheckInterval);				
			}
			results.printResults();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean allThreadsDone(List<Thread> threads) {
		for(int i = 0; i < threads.size(); ++i) {
			if(threads.get(i).isAlive()) {
				return false;
			}
		}
		return true;
	}
	
	public Experiment(ExperimentResults results) {
		this.results = results;
	}
	
	@Override
	public void run() {
		System.out.println("Thread " + Thread.currentThread().getName() + " started.");
		while(!isDone()) {
			long seed = (long)(System.currentTimeMillis() * Math.random());
			// Init players
			// Online Evolution
			AI p1 = new OnlineEvolution(true, 100, 0.1, 0.5, budget, new HeuristicEvaluator(false), false);	
			((OnlineEvolution)p1).setSeed(seed);
			// RHCA
			AI p2 = new OnlineCoevolution(100, 30, 0.3, budget, new HeuristicEvaluator(false), false);
			((OnlineCoevolution)p2).setSeed(seed);
			// Init game
			GameState.RANDOMNESS = false;
			GameArguments gameArgs = new GameArguments(false, p1, p2, "a", DECK_SIZE.STANDARD);
			gameArgs.budget = budget;
			gameArgs.gfx = false;
			Game game = new Game(null, gameArgs, seed);
			
			// Run game
			game.run();
			
			// Save results
			saveResults(game.state.getWinner());
			System.out.println("Thread " + Thread.currentThread().getName() + " completed a game. Winner: " + game.state.getWinner());
		}
		System.out.println("Thread " + Thread.currentThread().getName() + " ended.");
	}
	/*
	 * winnerIndex = 0 -> draw
	 * winnerIndex = 1 -> player 1 won
	 * winnerIndex = 2 -> player 2 won
	 */
	public synchronized void saveResults(int winnerIndex) {
		if(winnerIndex == 0) results.draws++;
		else if(winnerIndex == 1) results.p1Wins++;
		else if(winnerIndex == 2) results.p2Wins++;
		else System.out.println("Error. WinnerIndex =  " + winnerIndex);
	}
	
	public synchronized boolean isDone() {
		return (results.draws + results.p1Wins + results.p2Wins) >= Experiment.gamesToPlay;
	}
}
