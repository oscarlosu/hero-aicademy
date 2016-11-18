package marttoslo.evolution;

import java.util.ArrayList;
import java.util.List;

public class ExperimentResults {
	public int p1Wins = 0;
	public int p2Wins = 0;
	public int draws = 0;
	
	public void printResults() {
		int gamesPlayed = p1Wins + p2Wins + draws;
		System.out.println("Experiment results:");
		System.out.println("Games played: " + gamesPlayed);
		System.out.println("Player 1 won: " + p1Wins);
		System.out.println("Player 2 won: " + p2Wins);
		System.out.println("Draws: " + draws);
	}
}
