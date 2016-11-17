package ai.evolution;

import java.util.ArrayList;
import java.util.Random;

import action.Action;

public class WeakGenome extends Genome {

	public WeakGenome() {
		super();
		actions = new ArrayList<Action>();
		value = 0;
		visits = 0;
	}
	
	public WeakGenome(Random rng) {
		super(rng);
		actions = new ArrayList<Action>();
		value = 0;
		visits = 0;
	}

	public double fitness() {
		return value;
	}

}
