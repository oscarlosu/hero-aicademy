package marttoslo.heuristics.evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ai.AI;
import ai.GreedyActionAI;
import game.Game;
import game.GameArguments;
import marttoslo.heuristics.HeuristicEvaluator;
import marttoslo.heuristics.HeuristicEvaluator.ParameterName;
import model.DECK_SIZE;

public class HEGenotype extends AbstractGenotype {

	private HashMap<ParameterName, Double> genes;
	
	
	public static double ScaleParamMin;
	public static double ScaleParamMax;	
	
	public static double ValueParamMin;
	public static double ValueParamMax;
	
	
	@Override
	public void BuildRandom() {
		Random rng = new Random();
		for(HeuristicEvaluator.ParameterName param : HeuristicEvaluator.ParameterName.values()) {
			genes.put(param, rng.nextDouble());
		}
		
	}
	
	@Override
	public double Evaluate() {
		AI p1 = new GreedyActionAI(new marttoslo.heuristics.HeuristicEvaluator(false));
		AI p2 = new GreedyActionAI(new ai.evaluation.HeuristicEvaluator(false));
		
		GameArguments gameArgs = new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.gfx = false; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		return 0;
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
	
	public HeuristicEvaluator buildPhenotype() {
		return null;
	}
}
