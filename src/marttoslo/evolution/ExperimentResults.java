package marttoslo.evolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExperimentResults {
	public int winnerIndex;
	public long seed;
	public int turns;
	public boolean crystalWin;
	public boolean unitWin;
	
	// Coevolution stats
	public List<Double> co_generations;
	public double co_sumChampionHostFindGen;
	public double co_sumChampionParasiteFindGen;
	
	// Evolution stats
	public List<Double> oe_generations;
	public double oe_sumChampionHostFindGen;
	
	
	public String toString() {
		String s = "Result " + winnerIndex + " Turns " + turns + " (seed = " + seed + ")";
		return s;
	}
	
	public double avgCoGenerations() {
		double avg = 0;
		for(int i = 0, il = co_generations.size(); i < il; ++i) {
			avg += co_generations.get(i);
		}
		return (avg / (double)co_generations.size());
	}
	
	public double avgCoHostChampionHostFindGen() {
		return (co_sumChampionHostFindGen / (double)turns);
	}
	
	public double avgCoHostChampionParasiteFindGen() {
		return (co_sumChampionParasiteFindGen / (double)turns);
	}

}
