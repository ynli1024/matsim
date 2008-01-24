package playground.jhackney.scoring;

import org.matsim.plans.Plan;
import org.matsim.scoring.ScoringFunction;
import org.matsim.scoring.ScoringFunctionFactory;

public class SNScoringFunctionFactory03 implements ScoringFunctionFactory {

	private final ScoringFunctionFactory factory;

	public SNScoringFunctionFactory03(final ScoringFunctionFactory factory) {
			this.factory = factory;
	}

	public ScoringFunction getNewScoringFunction(final Plan plan) {
		return new SNScoringFunction03(plan, this.factory.getNewScoringFunction(plan));
	}

}
