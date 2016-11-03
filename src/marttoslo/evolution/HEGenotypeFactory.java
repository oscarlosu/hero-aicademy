package marttoslo.evolution;

public class HEGenotypeFactory implements IGenotypeFactory {
	@Override
	public HEGenotype Build() {
		return new HEGenotype();
	}

}
