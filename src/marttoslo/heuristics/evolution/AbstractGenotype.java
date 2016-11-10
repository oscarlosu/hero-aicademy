package marttoslo.heuristics.evolution;

import java.util.ArrayList;

public abstract class AbstractGenotype implements Comparable<AbstractGenotype> {
	private double fitness;
	
	public abstract void BuildRandom();
	
	public abstract double Evaluate();
	
	public abstract void Serialize(String filename);
	
	public abstract void Deserialize(String filename);
	
	public abstract void Mutate(double lower, double upper, double geneMutationChance);
	
	public abstract AbstractGenotype Crossover(AbstractGenotype other);
	
	public abstract ArrayList<Double> GetGenes();
	
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	
}
