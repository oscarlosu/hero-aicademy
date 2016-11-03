package marttoslo.evolution;

import java.util.ArrayList;

import marttoslo.heuristics.HeuristicEvaluator;

public class HEGenotype extends AbstractGenotype {

	@Override
	public void BuildRandom(double lower, double upper, double geneMutationChance) {
		for(HeuristicEvaluator.ParameterName param : HeuristicEvaluator.ParameterName.values()) {
			// Product mutation?
		}
		
	}

	@Override
	public void BuildGenotype(IPhenotype phenotype) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void BuildGenotype(ArrayList<Double> genes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IPhenotype BuildPhenotype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void Serialize(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Deserialize(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Mutate(double lower, double upper, double geneMutationChance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractGenotype Crossover(AbstractGenotype other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList GetGenes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(AbstractGenotype o) throws ClassCastException {
		HEGenotype other = (HEGenotype) o;
		return (int)Math.signum(this.getFitness() - o.getFitness());	
	}


}
