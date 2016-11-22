package marttoslo.evolution;

import java.util.ArrayList;
import java.util.List;

public class ExperimentResults {
	public int winnerIndex;
	public long seed;
	public int turns;
	
	public String toString() {
		String s = "Result " + winnerIndex + " Turns " + turns + " (seed = " + seed + ")";
		return s;
	}

}
