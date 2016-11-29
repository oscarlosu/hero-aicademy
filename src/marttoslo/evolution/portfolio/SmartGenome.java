package marttoslo.evolution.portfolio;

import game.GameState;
import marttoslo.portfolio.PortfolioController;
import marttoslo.portfolio.SmartAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.evolution.Genome;
import ai.util.ActionPruner;
import ai.util.ComplexActionComparator;

public abstract class SmartGenome implements Comparable<SmartGenome> {

	private ActionPruner pruner;
	private ComplexActionComparator comparator;

	public static Random random;
	public List<SmartAction> actions;
	public double value;
	public int visits;

	
	public SmartGenome() {
		super();
		pruner = new ActionPruner();
		comparator = new ComplexActionComparator();
		actions = new ArrayList<SmartAction>();
		random = new Random();
		value = 0;
		visits = 0;
	}
	
	public SmartGenome(Random rng) {
		super();
		pruner = new ActionPruner();
		comparator = new ComplexActionComparator();
		actions = new ArrayList<SmartAction>();
		random = rng;
		value = 0;
		visits = 0;
	}

	public  void random(int ap) {
		actions.clear();
		visits = 0;
		value = 0;		
		for(int i = 0; i < ap; ++i){
			actions.add(PortfolioController.GetRandomSmartAction(random));
		}
		// SingletonAction.endTurnAction
	}

	public void crossover(SmartGenome a, SmartGenome b) {
		actions.clear();
		visits = 0;
		value = 0;
		for (int i = 0; i < Math.max(a.actions.size(), b.actions.size()); i++) {
			actions.add(random.nextBoolean() ? a.actions.get(i) : b.actions.get(i));
		}
	}

	public void mutate() {
		if (actions.isEmpty())
			return;
		
		final int mutIdx = random.nextInt(actions.size());
		actions.set(mutIdx,  PortfolioController.GetRandomSmartAction(random));	
	}

	public int compareTo(SmartGenome other) {
		if (fitness() == other.fitness())
			return 0;
		if (fitness() > other.fitness())
			return -1;
		return 1;
	}

	public abstract double fitness();
	
	public double avgValue() {
		if (visits == 0)
			return 0;
		return value / visits;
	}

	@Override
	public String toString() {
		return "SmartGenome [value=" + value + ", visits="+ visits + ", fitness=" + fitness() + "]";
	}
	
}
