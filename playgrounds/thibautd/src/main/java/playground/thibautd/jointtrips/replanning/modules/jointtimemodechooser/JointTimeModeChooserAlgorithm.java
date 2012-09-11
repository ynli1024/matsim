/* *********************************************************************** *
 * project: org.matsim.*
 * JointTimeModeChooserAlgorithm.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.jointtrips.replanning.modules.jointtimemodechooser;

import java.util.Random;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.planomat.costestimators.DepartureDelayAverageCalculator;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.thibautd.jointtrips.config.JointTimeModeChooserConfigGroup;
import playground.thibautd.jointtrips.population.JointPlan;
import playground.thibautd.router.TripRouterFactory;
import playground.thibautd.router.controler.MultiLegRoutingControler;
import playground.thibautd.tsplanoptimizer.framework.Solution;
import playground.thibautd.tsplanoptimizer.framework.TabuSearchRunner;
import playground.thibautd.tsplanoptimizer.timemodechooser.traveltimeestimation.EstimatorTripRouterFactory;
import playground.thibautd.tsplanoptimizer.timemodechooser.traveltimeestimation.FixedRouteNetworkRoutingModule;
import playground.thibautd.tsplanoptimizer.timemodechooser.traveltimeestimation.FixedTransitRouteRoutingModule;

/**
 * @author thibautd
 */
public class JointTimeModeChooserAlgorithm implements PlanAlgorithm {
	private final MultiLegRoutingControler controler;
	private final DepartureDelayAverageCalculator delay;
	private final StatisticsCollector statsCollector;
	private final Random random;

	JointTimeModeChooserAlgorithm(
			final Random random,
			final StatisticsCollector statsCollector,
			final Controler controler,
			final DepartureDelayAverageCalculator delay ) {
		this.random = random;
		this.statsCollector = statsCollector;
		this.controler = (MultiLegRoutingControler) controler;
		this.delay = delay;
	}

	@Override
	public void run(final Plan plan) {
		JointPlan jointPlan = (JointPlan) plan;

		TripRouterFactory tripRouterFactory =
			getAndTuneTripRouterFactory(
					plan,
					delay,
					controler );

		if (statsCollector != null) {
			statsCollector.notifyTripRouterFactory( tripRouterFactory );
		}

		ScoringFunctionFactory scoringFunctionFactory = controler.getScoringFunctionFactory();
		JointTimeModeChooserConfigGroup config = (JointTimeModeChooserConfigGroup)
			controler.getConfig().getModule( JointTimeModeChooserConfigGroup.GROUP_NAME );
		JointTimeModeChooserConfigBuilder builder =
			new JointTimeModeChooserConfigBuilder(
					random,
					jointPlan,
					config,
					scoringFunctionFactory,
					tripRouterFactory,
					config.isDebugMode() ? controler.getControlerIO().getIterationPath( controler.getIterationNumber() ) : null);

		// first run without synchro
		builder.setInitialSolution( null );
		builder.setIsSynchronizing( false  );
		Solution bestSolution = TabuSearchRunner.runTabuSearch( builder );

		if (jointPlan.getClique().getMembers().size() > 1) {
			// then start at the found solution and synchronize
			builder.setInitialSolution( bestSolution );
			builder.setIsSynchronizing( true  );
			bestSolution = TabuSearchRunner.runTabuSearch( builder );
		}

		// two goals here:
		// 1- the side effect: getRepresentedPlan sets the plan to the represented state
		// 2- the obvious check
		if (bestSolution.getRepresentedPlan() != plan) {
			throw new RuntimeException( "the returned plan is not the input plan" );
		}
	}

	private static TripRouterFactory getAndTuneTripRouterFactory(
			final Plan plan,
			final DepartureDelayAverageCalculator delay,
			final MultiLegRoutingControler controler ) {
		return new EstimatorTripRouterFactory(
				plan,
				((PopulationImpl) controler.getPopulation()).getFactory(),
				controler.getNetwork(),
				controler.getTravelTimeCalculator(),
				controler.getTravelDisutilityFactory(),
				controler.getLeastCostPathCalculatorFactory(),
				((PopulationFactoryImpl) ((PopulationImpl) controler.getPopulation()).getFactory()).getModeRouteFactory(),
				controler.getScenario().getTransitSchedule(),
				controler.getConfig().plansCalcRoute(),
				controler.getConfig().planCalcScore(),
				delay,
				controler.getTripRouterFactory());
	}
}
