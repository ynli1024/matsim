/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.jbischoff.parking.routing;

import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.contrib.dvrp.trafficmonitoring.VrpTravelTimeModules;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteFactory;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import com.google.inject.name.Named;

import playground.jbischoff.parking.ParkingUtils;

/**
 * @author  jbischoff
 *
 */
public class WithinDayParkingRouter implements ParkingRouter {

	private LeastCostPathCalculator pathCalculator; 
	@Inject
	private Network network;
	
	private TravelTime travelTime;
	@Inject
	WithinDayParkingRouter(@Named(TransportMode.car) TravelTime travelTime) {
	this.travelTime = travelTime;
	}

	@Override
	public NetworkRoute getRouteFromParkingToDestination(NetworkRoute originalIntendedRoute, double departureTime, Id<Link> startLinkId) {
		 ;
		TravelDisutility travelDisutility = new TimeAsTravelDisutility(travelTime);
		pathCalculator = new Dijkstra(network, travelDisutility, travelTime);
		Link startLink = this.network.getLinks().get(startLinkId);
		Link endLink = this.network.getLinks().get(originalIntendedRoute.getEndLinkId());
		
		Path path = this.pathCalculator.calcLeastCostPath(startLink.getToNode(), endLink.getFromNode(), 
				departureTime, null, null) ;
		NetworkRoute carRoute = new LinkNetworkRouteImpl(startLinkId, endLink.getId());
		carRoute.setLinkIds(startLink.getId(), NetworkUtils.getLinkIds( path.links), endLink.getId());
		carRoute.setTravelTime( path.travelTime );
		double distance = RouteUtils.calcDistance(carRoute, 1.0, 1.0, network);
		carRoute.setDistance(distance);
		
		return carRoute;
	}

}