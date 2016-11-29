package marttoslo.evolution.portfolio;

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
import marttoslo.evolution.AvgGenome;
import marttoslo.portfolio.SmartAction;
import ui.UI;

public class OnlineCoevolutionPortfolio implements AI, AiVisualizor {

	public int popSize;
	public int budget;
	public double mutRate;
	public IStateEvaluator evaluator;
	public int evalSubPopSize;
	
	public List<Double> generations;
	public List<Double> bestVisits;
	
	public List<Action> actions;
	
	public double sumChampionHostFindGen;
	public int championHostFindGen;
	public Genome championHost;
	public List<Double> championHostFitnesses;
	public double sumChampionParasiteFindGen;
	public int championParasiteFindGen;
	public SmartGenome championParasite;
	public List<Double> championParasiteFitnesses;
	
	public List<Genome> hostPopulation;
	public List<SmartGenome> parasitePopulation;
	public Map<Integer, Double> hostFitnesses;
	public Map<Integer, Double> parasiteFitnesses;
	public List<List<Action>> bestHostActions;
	public List<List<Action>> bestParasiteActions;
	public List<List<SmartAction>> bestParasiteSmartActions;
	
	private OnlineCoevolutionPortfolioVisualizer visualizer;
	private Random random;
	
	private boolean stepped;
	private long seed = System.currentTimeMillis();
	
	public OnlineCoevolutionPortfolio(int popSize, int evalSubPopSize, double mutRate, int budget, IStateEvaluator evaluator, boolean stepped) {
		super();
		this.popSize = popSize;
		this.mutRate = mutRate;
		this.budget = budget;
		this.evaluator = evaluator;
		this.evalSubPopSize = evalSubPopSize;
		hostPopulation = new ArrayList<Genome>();
		parasitePopulation = new ArrayList<SmartGenome>();
		actions = new ArrayList<Action>();
		random = new Random(seed);
		this.generations = new ArrayList<Double>();
		this.bestVisits = new ArrayList<Double>();
		this.hostFitnesses = new HashMap<Integer, Double>();
		this.parasiteFitnesses = new HashMap<Integer, Double>();
		this.bestHostActions = new ArrayList<List<Action>>();
		this.bestParasiteActions = new ArrayList<List<Action>>();
		this.bestParasiteSmartActions = new ArrayList<List<SmartAction>>();
//		this.table = new SharedStateTable();
//		this.newcomers = new ArrayList<Genome>();
//		this.useHistory = useHistory;
		this.stepped = stepped;
		
		championHostFitnesses = new ArrayList<Double>();
		championParasiteFitnesses = new ArrayList<Double>();
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
		this.visualizer = new OnlineCoevolutionPortfolioVisualizer(ui, this);
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
		
		
		
		
		hostFitnesses.clear();
		parasiteFitnesses.clear();
		bestHostActions.clear();
		bestParasiteActions.clear();
		
		GameState clone = new GameState(state.map);
		clone.imitate(state);
		initPopulation(hostPopulation, clone);
		initSmartPopulation(parasitePopulation);
		
		
		
		int g = 0;
		clone.imitate(state);
		while (thx.getCurrentThreadCpuTime() / 1e6 < start + budget) {

			g++;
			clone.imitate(state);
			// Test population
			evaluateAndSort(clone);

			// Replace worst individuals with offspring from best
			reproduce(clone, hostPopulation);
			reproduceSmart(parasitePopulation);
			
			// TODO: Only if needed?!
			if(championHost == null || hostPopulation.get(0) != championHost) {
				championHost = hostPopulation.get(0);
				championHostFindGen = g;
			}
			if(championParasite == null || parasitePopulation.get(0) != championParasite) {
				championParasite = parasitePopulation.get(0);
				championParasiteFindGen = g;
			}
			hostFitnesses.put(g, hostPopulation.get(0).fitness());
			parasiteFitnesses.put(g, parasitePopulation.get(0).fitness());
			bestHostActions.add(clone(hostPopulation.get(0).actions));
			bestParasiteSmartActions.add(cloneSmart(parasitePopulation.get(0).actions));
			// Extract actions from parasite champion's SmartActions
			clone.imitate(state);
			List<Action> parasiteActions = new ArrayList<Action>();
			boolean isPlayer1 = clone.p1Turn;
			for(int i = 0; i < championParasite.actions.size() && clone.APLeft > 0 && !clone.isTerminal; ++i) {
				SmartAction sa = championParasite.actions.get(i);
				sa.InitActions(clone, isPlayer1);
				for(Action a = sa.Next(clone, clone.p1Turn); sa.HasNext() && clone.APLeft > 0 && !clone.isTerminal; a = sa.Next(clone, clone.p1Turn)) {
					clone.update(a);
					parasiteActions.add(a);
				}
				sa.Reset();
			}
			bestParasiteActions.add(parasiteActions);		
			
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
		sumChampionHostFindGen += championHostFindGen;
		sumChampionParasiteFindGen += championParasiteFindGen;
		championHostFitnesses.add(hostPopulation.get(0).fitness());
		championParasiteFitnesses.add(parasitePopulation.get(0).fitness());
	}

	private void reproduce(GameState state, List<Genome> pop) {
		GameState clone = new GameState(state.map);
		// Select best 2 individuals
		Genome p1 = pop.get(0);
		Genome p2 = pop.get(1);
		for(int i = 2; i < popSize; ++i) {
			// Crossover
			Genome child = new AvgGenome(random);
			clone.imitate(state);
			child.crossover(p1, p2, clone);
			// Mutation
			if (random.nextFloat() < mutRate) {
				clone.imitate(state);
				child.mutate(clone);
			}
			// Replace with new individual
			pop.set(i, child);
		}
	}
	
	private void reproduceSmart(List<SmartGenome> pop) {
		// Select best 2 individuals
		SmartGenome p1 = pop.get(0);
		SmartGenome p2 = pop.get(1);
		for(int i = 2; i < popSize; ++i) {
			// Crossover
			SmartGenome child = new AvgSmartGenome(random);
			child.crossover(p1, p2);
			// Mutation
			if (random.nextFloat() < mutRate) {
				child.mutate();
			}
			// Replace with new individual
			pop.set(i, child);
		}
	}
	
	private void evaluateAndSort(GameState state) {
		// Clear value and visit count from individuals
		for(Genome host : hostPopulation) {
			host.value = 0;
			host.visits = 0;
		}
		for(SmartGenome parasite : parasitePopulation) {
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
				SmartGenome parasite = parasitePopulation.get(index);
				// Make copy of game state for simulation
				clone.imitate(state);
				hostIsPlayer1 = clone.p1Turn;
				// Play host turn and then parasite turn
				clone.update(host.actions);
				for(int j = 0; j < parasite.actions.size() && clone.APLeft > 0 && !clone.isTerminal; ++j) {
					parasite.actions.get(j).updateStateAndReset(clone);
				}
				
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
	
	private List<SmartAction> cloneSmart(List<SmartAction> other) {
		List<SmartAction> actions = new ArrayList<SmartAction>();
		actions.addAll(other);
		return actions;
	}
	
	private void initPopulation(List<Genome> hostPopulation2, GameState state) {

		hostPopulation2.clear();
		final GameState clone = new GameState(state.map);

		for (int i = 0; i < popSize; i++) {
			clone.imitate(state);
			final Genome genome = new AvgGenome(random);
			genome.random(clone);
			hostPopulation2.add(genome);
		}

	}
	
	private void initSmartPopulation(List<SmartGenome> hostPopulation2) {
		hostPopulation2.clear();

		for (int i = 0; i < popSize; i++) {
			final SmartGenome genome = new AvgSmartGenome(random);
			genome.random(GameState.ACTION_POINTS);
			hostPopulation2.add(genome);
		}

	}

	@Override
	public void init(GameState state, long ms) {
		actions = new ArrayList<Action>();
	}

	@Override
	public AI copy() {
		if (visualizer!=null){
			OnlineCoevolutionPortfolio evo = new OnlineCoevolutionPortfolio(popSize, evalSubPopSize, mutRate, budget, evaluator.copy(), stepped);
			return evo;
		}
		
		return new OnlineCoevolutionPortfolio(popSize, evalSubPopSize, mutRate, budget, evaluator.copy(), stepped);
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
		return "RHCA Portfolio";
	}

}
