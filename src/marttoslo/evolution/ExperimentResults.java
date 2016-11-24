package marttoslo.evolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import pacman.game.util.IO;

public class ExperimentResults {
	@Expose
	public int winnerIndex;
	@Expose
	public long seed;
	@Expose
	public int turns;
	@Expose
	public boolean crystalWin;
	@Expose
	public boolean unitWin;
	
	// Coevolution stats
	@Expose
	public List<Double> co_generations = new ArrayList<Double>();
	@Expose
	public double co_sumChampionHostFindGen;
	@Expose
	public double co_sumChampionParasiteFindGen;
	
	// Evolution stats
	@Expose
	public List<Double> oe_generations = new ArrayList<Double>();
	@Expose
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
