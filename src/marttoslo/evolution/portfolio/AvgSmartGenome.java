package marttoslo.evolution.portfolio;

import java.util.ArrayList;
import java.util.Random;

import action.Action;
import ai.evolution.Genome;
import marttoslo.portfolio.SmartAction;

public class AvgSmartGenome extends SmartGenome {

	public AvgSmartGenome() {
		super();
		actions = new ArrayList<SmartAction>();
		value = 0;
		visits = 0;
	}
	
	public AvgSmartGenome(Random rng) {
		super(rng);
		actions = new ArrayList<SmartAction>();
		value = 0;
		visits = 0;
	}

	public double fitness() {
		return avgValue();
	}

}
