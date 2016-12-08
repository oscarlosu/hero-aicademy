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
	public List<Double> co_championHostFindGen = new ArrayList<Double>();
	@Expose
	public List<Double> co_championHostFitnesses = new ArrayList<Double>();
	@Expose
	public List<Double> co_championParasiteFindGen = new ArrayList<Double>();
	@Expose
	public List<Double> co_championParasiteFitnesses = new ArrayList<Double>();
	
	// Evolution stats
	@Expose
	public List<Double> oe_generations = new ArrayList<Double>();
	@Expose
	public List<Double> oe_championHostFindGen = new ArrayList<Double>();
	@Expose
	public List<Double> oe_championFitnesses = new ArrayList<Double>();
	
	
	public String toString() {
		String s = "Result " + winnerIndex + " Turns " + turns + " (seed = " + seed + ")";
		return s;
	}

}
