package marttoslo.evolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class ArtificialEvolution {
	// Evolution params
	private int eliteSize = 40;
	private int offspringSize = 60;
	private int populationSize = 100;	
	// Mutation
	private double mutationChance = 0.2;
	private double geneMutationChance = 0.2;
	private double mutationFactorLowerBound = 0.5;
	private double mutationFactorUpperBound = 1.5;
	// Initialisation mutation
	private double initGeneMutationChance = 1.0;
	private double initMutationFactorLowerBound = 0.25;
	private double initMutationFactorUpperBound = 1.75;
	// Evaluation
	private int evaluationTrials = 3;
	private double pillsWeight = 0.7;
	private double scoreWeight = 0.3;
	public static int MAX_LEVEL = 4;
	public static int MAX_SCORE = 82180;
	// Stats
	private double avgFitness;
	private double bestFitness;
	private double worstFitness;
	
	private ArrayList<AbstractGenotype> elite;
	private ArrayList<AbstractGenotype> population;
	
	private IGenotypeFactory genotypeFactory;
	
	public static void main(String args[]) {
		// Parameter setup
		int generations = 100;
		String filename = "/data/HeuristicEvaluatorParams.json";
		IGenotypeFactory gFactory = new HEGenotypeFactory();
		
		// Run evolution		
		System.out.println("Evolving: (" + generations + " generations)");
		
		double startTime = System.currentTimeMillis() / 1000.0;
		
		ArtificialEvolution ae = new ArtificialEvolution(gFactory);
		ae.Evolve(generations, filename);
		
		double endTime = System.currentTimeMillis() / 1000.0;
		
		System.out.println("Total execution time: " + (endTime-startTime) + "s");
		System.out.println("Done");
	}
	
	public ArtificialEvolution(IGenotypeFactory genotypeFactory) {
		this.genotypeFactory = genotypeFactory;
		populationSize = eliteSize + offspringSize;
	}
	
	public void Evolve(int generations, String championFilename) {
		InitializePopulation();
		Evaluate();
		for(int i = 0; i < generations; ++i) {
			// Perform generation step			
			Replacement();
			Evaluate();
			// Save best individual
			AbstractGenotype best = elite.get(0);
			best.Serialize(championFilename);
			// Stats
			System.out.println("Generation: " + i + " Best fitness: " + bestFitness + " Avg fitness: " + avgFitness + " Worst fitness: " + worstFitness);			
		}
	}
	
	private void InitializePopulation() {
		population = new ArrayList<AbstractGenotype>();
		for(int i = 0; i < populationSize; ++i) {
			AbstractGenotype genotype = genotypeFactory.Build();
			genotype.BuildRandom(initMutationFactorLowerBound, initMutationFactorUpperBound, initGeneMutationChance);
			population.add(genotype);
		}
	}
		
	private void Evaluate() {		
		// Evaluation
		avgFitness = 0;
		for(int i = 0; i < population.size(); ++i) {
			// Evaluate individual
			AbstractGenotype genotype = population.get(i);
			IPhenotype phenotype = genotype.BuildPhenotype();
			double fitness = phenotype.Evaluate();
			genotype.setFitness(fitness);
			// Accumulate population fitness for stats
			avgFitness += fitness;
		}			
	}
	
	private void Replacement() {		
		// Selection - save elite, remove scum
		// Sort population
		elite = new ArrayList<AbstractGenotype>();
		// Descending order
		Collections.sort(population, Collections.reverseOrder());		
		// Remove worst individuals
		elite = new ArrayList<AbstractGenotype>(population.subList(0, eliteSize));
		population.clear();
		
		// Stats
		avgFitness = avgFitness / (double)populationSize;
		bestFitness = elite.get(0).getFitness();
		worstFitness = elite.get(elite.size() - 1).getFitness();		
		
		// Offspring
		Random rng = new Random();				
		for(int i = 0; i < offspringSize; ++i) {
			// Crossover - fitness proportionate reproduction rights
			int parent1Index = selectFitnessProportionate(rng, elite.size());
			int parent2Index = selectFitnessProportionate(rng, elite.size());
			AbstractGenotype parent1 = elite.get(parent1Index);
			AbstractGenotype parent2 = elite.get(parent2Index);
			AbstractGenotype child = (AbstractGenotype) parent1.Crossover(parent2);
			// Mutation
			if(rng.nextDouble() < mutationChance) {
				child.Mutate(mutationFactorLowerBound, mutationFactorUpperBound, geneMutationChance);
			}
			// Add to new population
			population.add(child);
		}
		// Add elite to new population
		population.addAll(elite);
		
		// Shuffle population to remove any bias from ordering due to algorithm	
		Collections.shuffle(population, rng);
	}
	
	public int selectFitnessProportionate(Random rng, int eliteSize) {
		return (int)(sampleBoundedHalfNormal(rng) * (eliteSize - 1));
	}
	
	public static double sampleBoundedHalfNormal(Random rng) {
		// Debug
		int counter = 0;
		// 3 times the standard deviation ~ 99.7% of population
		double sdx3 = 3.0;
		// Sample until we get a valid result
		double sample = 0;
		do {
			sample = Math.abs(rng.nextGaussian()) / sdx3; // ~ range [0, 1]
			counter++;
		} while(sample > 1);
		
		// Debug
		if(counter > 2) {
			System.out.println("sampleBoundedHalfNormal required " + counter + " attempts");
		}
		
		return sample;
	}

}
