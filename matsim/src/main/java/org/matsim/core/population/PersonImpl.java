/* *********************************************************************** *
 * project: org.matsim.*
 * Person.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.core.population;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Customizable;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.scenario.CustomizableUtils;
/**
 * Default implementation of {@link Person} interface.
 */
public final class PersonImpl implements Person {

	private List<Plan> plans = new ArrayList<Plan>(6);
	private Id<Person> id;

	private Plan selectedPlan = null;

	private Customizable customizableDelegate;
	private boolean locked;

	/* deliberately package */ PersonImpl(final Id<Person> id) {
		this.id = id;
	}

	@Override
	public final Plan getSelectedPlan() {
		return this.selectedPlan;
	}

	@Override
	public boolean addPlan(final Plan plan) {
		plan.setPerson(this);
		// Make sure there is a selected plan if there is at least one plan
		if (this.selectedPlan == null) this.selectedPlan = plan;
		return this.plans.add(plan);
	}

	@Override
	public final void setSelectedPlan(final Plan selectedPlan) {
		if (selectedPlan != null && !plans.contains( selectedPlan )) {
			throw new IllegalStateException("The plan to be set as selected is not null nor stored in the person's plans");
		}
		this.selectedPlan = selectedPlan;
	}

	@Override
	public Plan createCopyOfSelectedPlanAndMakeSelected() {
		Plan oldPlan = this.getSelectedPlan();
		if (oldPlan == null) {
			return null;
		}
		Plan newPlan = PopulationUtils.createPlan(oldPlan.getPerson());
		PopulationUtils.copyFromTo(oldPlan, newPlan);
		this.getPlans().add(newPlan);
		this.setSelectedPlan(newPlan);
		return newPlan;
	}

	@Override
	public Id<Person> getId() {
		return this.id;
	}

	public void setId(final Id<Person> id) {
		// Not on interface. Only to be used for demand generation.
		// yyyy This method is dangerous, since it allows to change the ID of the person while it remains under the old ID in the map.
		// I think that it can be removed once the copy stuff is sorted out.  kai, may'16
		testForLocked() ;
		this.id = id;
	}

	@Override
	public final String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[id=").append(this.getId()).append("]");
		b.append("[nof_plans=").append(this.getPlans() == null ? "null" : this.getPlans().size()).append("]");
		return b.toString();
	}

	@Override
	public boolean removePlan(final Plan plan) {
		boolean result = this.getPlans().remove(plan);
		if ((this.getSelectedPlan() == plan) && result) {
			this.setSelectedPlan(new RandomPlanSelector<Plan, Person>().selectPlan(this));
		}
		return result;
	}

	@Override
	public List<Plan> getPlans() {
		return this.plans;
	}


	@Override
	public Map<String, Object> getCustomAttributes() {
		if (this.customizableDelegate == null) {
			this.customizableDelegate = CustomizableUtils.createCustomizable();
		}
		return this.customizableDelegate.getCustomAttributes();
	}

	final void setLocked() {
		this.locked = true ;
		
		// note that this does NOT lock the add/remove plans logic, but just some fields. kai, dec'15
//		for ( Plan plan : this.plans ) {
//				((PlanImpl)plan).setLocked() ;
				// does not really do that much since it only affects the initial plan(s). kai, dec'15
//		}
	}
	private void testForLocked() {
		if ( this.locked ) {
			throw new RuntimeException("too late to do this") ;
		}
	}


}
