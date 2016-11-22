package marttoslo.evolution;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import action.Action;
import action.SingletonAction;
import ai.AI;
import ai.HeuristicAI;
import ai.evaluation.IStateEvaluator;
import ai.evolution.AiVisualizor;
import ai.evolution.Genome;
import game.GameState;
import ui.UI;

public class OnlineCoevolution implements AI, AiVisualizor {

	public int popSize;
	public int budget;
	public double mutRate;
	public IStateEvaluator evaluator;
	public int evalSubPopSize;
	
	public List<Double> generations;
	public List<Double> bestVisits;
	
	public List<Action> actions;
	
	public List<Genome> hostPopulation;
	public List<Genome> parasitePopulation;
	public Map<Integer, Double> fitnesses;
	public List<List<Action>> bestHostActions;
	public List<List<Action>> bestParasiteActions;
	
	private OnlineCoevolutionVisualizer visualizer;
	private Random random;
	
	private boolean stepped;
	private long seed = System.currentTimeMillis();
	
	public OnlineCoevolution(int popSize, int evalSubPopSize, double mutRate, int budget, IStateEvaluator evaluator, boolean stepped) {
		super();
		this.popSize = popSize;
		this.mutRate = mutRate;
		this.budget = budget;
		this.evaluator = evaluator;
		this.evalSubPopSize = evalSubPopSize;
		hostPopulation = new ArrayList<Genome>();
		parasitePopulation = new ArrayList<Genome>();
		actions = new ArrayList<Action>();
		random = new Random(seed);
		this.generations = new ArrayList<Double>();
		this.bestVisits = new ArrayList<Double>();
		this.fitnesses = new HashMap<Integer, Double>();
		this.bestHostActions = new ArrayList<List<Action>>();
		this.bestParasiteActions = new ArrayList<List<Action>>();
//		this.table = new SharedStateTable();
//		this.newcomers = new ArrayList<Genome>();
//		this.useHistory = useHistory;
		this.stepped = stepped;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
		random = new Random(seed);
	}
	
	public long getSeed() {
		return seed;
	}
	
	@Override
	public void enableVisualization(UI ui) {
		this.visualizer = new OnlineCoevolutionVisualizer(ui, this);
	}

	@Override
	public Action act(GameState state, long ms) {
		if (actions.isEmpty()) {
			search(state);			
			// Wait for keypress
			if(stepped) {
				Scanner s=new Scanner(System.in);
				s.nextLine();
				s.close();
			}		
		}
			

		//table.clear();
		final Action next = actions.get(0);
		actions.remove(0);		
		return next;
	}
	
	private void search(GameState state) {
		ThreadMXBean thx = ManagementFactory.getThreadMXBean();
		thx.setThreadCpuTimeEnabled(true);
		double start = thx.getCurrentThreadCpuTime() / 1e6;
//		long startSystem = System.currentTimeMillis();
		
		
		
		
		fitnesses.clear();
		bestHostActions.clear();
		bestParasiteActions.clear();
		
		GameState clone = new GameState(state.map);
		clone.imitate(state);
		initPopulation(hostPopulation, clone);
		// Simulate turn with heuristic AI before initializing parasites
		clone.imitate(state);
		AI standInPlayer = new HeuristicAI();
		List<Action> simActions = new ArrayList<Action>();
		for(int i = 0; i < clone.APLeft; ++i) {
			simActions.add(standInPlayer.act(clone, 0));
		}
		simActions.add(SingletonAction.endTurnAction);
		clone.update(simActions);
		initPopulation(parasitePopulation, clone);
		
		
		
		int g = 0;
		clone.imitate(state);
		while (thx.getCurrentThreadCpuTime() / 1e6 < start + budget) {

			g++;
			clone.imitate(state);
			// Test population
			evaluateAndSort(clone);

			// Replace worst individuals with offspring from best
			reproduce(clone, hostPopulation);
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// This will produce invalid moves
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			clone.update(hostPopulation.get(0).actions);
			reproduce(clone, parasitePopulation); 
			
			// TODO: Only if needed?!
			fitnesses.put(g, hostPopulation.get(0).fitness());
			bestHostActions.add(clone(hostPopulation.get(0).actions));
			bestParasiteActions.add(clone(parasitePopulation.get(0).actions));
			
		}
		double end = thx.getCurrentThreadCpuTime() / 1e6;
//		long endSystem = System.currentTimeMillis();
//		System.out.println(Thread.currentThread().getName() + " RHCA real cpu time: " + (end - start) + " system time: " + (endSystem - startSystem));
		
		if (visualizer != null){
			visualizer.p1 = state.p1Turn;
			visualizer.update();
			while(visualizer.rendering){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}
		
		actions = hostPopulation.get(0).actions;
		
		generations.add((double)g);
		bestVisits.add((double)(hostPopulation.get(0).visits));
	}

	private void reproduce(GameState state, List<Genome> sortedPopulation) {
		GameState clone = new GameState(state.map);
		// Select best 2 individuals
		Genome p1 = sortedPopulation.get(0);
		Genome p2 = sortedPopulation.get(1);
		for(int i = 2; i < popSize; ++i) {
			// Crossover
			Genome child = sortedPopulation.get(i);
			clone.imitate(state);
			child.crossover(p1, p2, clone);
			// Mutation
			if (random.nextFloat() < mutRate) {
				clone.imitate(state);
				child.mutate(clone);
			}
		}
	}
	
	private void evaluateAndSort(GameState state) {
		// Clear value and visit count from individuals
		for(Genome host : hostPopulation) {
			host.value = 0;
			host.visits = 0;
		}
		for(Genome parasite : parasitePopulation) {
			parasite.value = 0;
			parasite.visits = 0;
		}
		// Evaluate
		GameState clone = new GameState(state.map);
		boolean hostIsPlayer1;
		for(Genome host : hostPopulation) {
			for(int i = 0; i < evalSubPopSize; ++i) {
				// Select random parasite
				int index = random.nextInt(parasitePopulation.size());
				Genome parasite = parasitePopulation.get(index);
				// Make copy of game state for simulation
				clone.imitate(state);
				hostIsPlayer1 = clone.p1Turn;
				// Play host turn and then parasite turn
				clone.update(host.actions);
				clone.update(parasite.actions);
				// Evaluate
				double hostScore = evaluator.eval(clone, hostIsPlayer1);				
				double parasiteScore = evaluator.eval(clone, !hostIsPlayer1);
				
				host.value += hostScore;
				host.visits++;
				parasite.value = parasiteScore;
				parasite.visits++;
			}
		}		
		
		// Sort hosts descending
		Collections.sort(hostPopulation);
//		for(int i = 0; i < hostPopulation.size(); ++i) {
//			Genome g = hostPopulation.get(i);
//			System.out.println(i + " value " + g.value + " visits " + g.visits + " fitness " + g.fitness());
//		}
		// Sort parasites descending
		Collections.sort(parasitePopulation);		
	}
	
	private List<Action> clone(List<Action> other) {
		List<Action> actions = new ArrayList<Action>();
		actions.addAll(other);
		return actions;
	}
	
	private void initPopulation(List<Genome> population, GameState state) {

		population.clear();
		final GameState clone = new GameState(state.map);

		for (int i = 0; i < popSize; i++) {
			clone.imitate(state);
			final Genome genome = new AvgGenome(random);
			genome.random(clone);
			population.add(genome);
		}

	}

	@Override
	public void init(GameState state, long ms) {
		actions = new ArrayList<Action>();
	}

	@Override
	public AI copy() {
		if (visualizer!=null){
			OnlineCoevolution evo = new OnlineCoevolution(popSize, evalSubPopSize, mutRate, budget, evaluator.copy(), stepped);
			return evo;
		}
		
		return new OnlineCoevolution(popSize, evalSubPopSize, mutRate, budget, evaluator.copy(), stepped);
	}

	@Override
	public String header() {
		String name = title()+"\n";
		name += "Pop. size = " + popSize + "\n";
		name += "Budget = " + budget + "ms.\n";
		name += "Mut. rate = " + mutRate + "\n";
		name += "State evaluator = " + evaluator.title() + "\n";
		return name;
	}

	@Override
	public String title() {
		return "RHCA";
	}

}
