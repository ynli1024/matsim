/* *********************************************************************** *
 * project: org.matsim.*
 * DijkstraForSelectNodes.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

// Zus�tzlich Punkte innerhalb des aufgespannten Polygons finden?
// -> http://www.coding-board.de/board/showthread.php?t=23953 : 
// Herausfinden, ob ein beliebiger Punkt innerhalb eines Polygons liegt

package playground.christoph.knowledge.nodeselection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.experimental.network.Network;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.utils.misc.Time;

public class DijkstraForSelectNodes {
	
	private static final Logger log = Logger.getLogger(DijkstraForSelectNodes.class);
	
	// Traffic network
	Network network;
	
	// mapping between nodes and dijkstraNodes
	HashMap<NodeImpl, DijkstraNode> dijkstraNodeMap;
	
	// CostCalculator for the Dijkstra Algorithm
	TravelCost costCalculator = new FreespeedTravelTimeCost();

	// time for the cost calculator
	double time = Time.UNDEFINED_TIME;
	
	// initial length of the priority queue - value taken from Dijkstra.java
	private static final int INITIAL_CAPACITY = 500;
	
	// all nodes of the network
	Map<Id, NodeImpl> networkNodesMap;
	
	
	// List of nodes, sorted by their distance to the startnode.
	private final Comparator<DijkstraNode> shortestDistanceComparator = new Comparator<DijkstraNode>()
	{
		public int compare(DijkstraNode a, DijkstraNode b)
	    {
			// note that this trick doesn't work for huge distances, close to Integer.MAX_VALUE
	        double diff = a.getMinDist() - b.getMinDist();
	        
	        // distance from b is bigger than distance from a
	        if (diff < 0) return -1;

	        // distance from a is bigger than distance from b
	        else if (diff > 0) return 1;
	        
	        // same distance
	        else return 0;
	    }
	};
	    
	   
	// List of nodes, sorted by their distance to the startnode.
	private final PriorityQueue<DijkstraNode> unvisitedNodes = new PriorityQueue<DijkstraNode>(INITIAL_CAPACITY, shortestDistanceComparator);
	
	
	public DijkstraForSelectNodes(Map<Id, NodeImpl> networkNodesMap)
	{
		this.networkNodesMap = networkNodesMap;
		
		dijkstraNodeMap = new HashMap<NodeImpl, DijkstraNode>();
		DijkstraNode.setNodeMap(dijkstraNodeMap);
	}
	
	public DijkstraForSelectNodes(Network network, Map<Id, NodeImpl> networkNodesMap)
	{
		this.network = network;
		this.networkNodesMap = networkNodesMap;
		
		dijkstraNodeMap = new HashMap<NodeImpl, DijkstraNode>();
		DijkstraNode.setNodeMap(dijkstraNodeMap);
		initDijkstraNodes();	
	}
	
	public void setNetwork(Network network)
	{
		this.network = network;
	}
	
	public void setNetworkNodes(Map<Id, NodeImpl> networkNodesMap)
	{
		this.networkNodesMap = networkNodesMap;
	}
	
	protected void initDijkstraNodes()
	{		
		// clear map
		dijkstraNodeMap.clear();
		
		// iterate over Array or Iteratable 
		for (NodeImpl node : networkNodesMap.values())
		{		
			DijkstraNode dijkstraNode = new DijkstraNode(node);
			
			// fill DijkstraNodeMap
			dijkstraNodeMap.put(node, dijkstraNode);
		}
		
	}

	// initialize
	private void init(DijkstraNode start)
	{
		// reset nodes
		resetDijkstraNodes();

	    unvisitedNodes.clear();
	 	        
	    // add source
	    setMinDistance(start, 0.0);
	    unvisitedNodes.add(start);
	}
	   
	
	// Search shortest Path for a Route. Break if distance has been found.
	public void executeRoute(NodeImpl start, NodeImpl end)
	{	
		DijkstraNode startNode = dijkstraNodeMap.get(start);
		init(startNode);
	        
		DijkstraNode endNode = dijkstraNodeMap.get(end);
		
	    // the current node
	    DijkstraNode node;
	        
	    // extract the node with the shortest distance
	    while ((node = unvisitedNodes.poll()) != null)
	    {
	    	// catch possible error
	    	assert !isVisited(node);
	            
	        // reached destination node -> break
	        if (node.getNode().equals(endNode.getNode())) break;
	            
	        node.setVisited(true);
	           
	        relaxOutgoingNode(node);
	    }
	}

	// Search shortest Fowardpaths to all nodes within the network.
	public void executeForwardNetwork(NodeImpl start)
	{		
		DijkstraNode startNode = dijkstraNodeMap.get(start);
		init(startNode);
		
	    // the current node
	    DijkstraNode node;
	    
	    // extract the node with the shortest distance
	    while ((node = unvisitedNodes.poll()) != null)
	    {
	    	// catch possible error
	    	assert !isVisited(node);
	    	
	        node.setVisited(true);
	           
	        relaxOutgoingNode(node);
	    }
	}
	
	// Search shortest Backwardpaths to all nodes within the network.
	public void executeBackwardNetwork(NodeImpl start)
	{		
		DijkstraNode startNode = dijkstraNodeMap.get(start);
		init(startNode);
		
	    // the current node
	    DijkstraNode node;
	    
	    // extract the node with the shortest distance
	    while ((node = unvisitedNodes.poll()) != null)
	    {
	    	// catch possible error
	    	assert !isVisited(node);
	    	
	        node.setVisited(true);
	           
	        relaxIngoingNode(node);
	    }
	}
	
	/*
	 * Return Map with the distances to every node.
	 * Is called from outside this class, so return nodes instead of
	 * DijkstraNodes.
	 */
	public Map<NodeImpl, Double> getMinDistances()
	{
		Map<NodeImpl, Double> minDistances = new HashMap<NodeImpl, Double>();
		
		for (DijkstraNode node : dijkstraNodeMap.values()) 
		{
			// add the node and its shortest path to the map
			minDistances.put(node.getNode(), node.getMinDist());
		}
		
		return minDistances;
	}
	
	// expand ingoing node
	private void relaxIngoingNode(DijkstraNode node)
	{		
		ArrayList<LinkImpl> ingoingLinks = node.ingoingLinks();
		for (int i = 0; i < ingoingLinks.size(); i++)
		{
			// get current link
			LinkImpl link = ingoingLinks.get(i);
			
			// get destination node
			DijkstraNode fromNode = dijkstraNodeMap.get(link.getFromNode());
			
			// if node has not been visited
			if (!fromNode.isVisited())
			{			
				double shortDist = node.getMinDist() + getLinkCost(link, time); 
	            
				// Found new shortest path to the destination node?
				if (shortDist < fromNode.getMinDist())
				{
					// store new "shortest" distance
					setMinDistance(fromNode, shortDist);
		                                
					// store new "previous" node
					setPreviousNode(fromNode, node);
				}

			}
		}        
	}
	
	// expand outgoing node
	private void relaxOutgoingNode(DijkstraNode node)
	{		
		ArrayList<LinkImpl> outgoingLinks = node.outgoingLinks();
		for (int i = 0; i < outgoingLinks.size(); i++)
		{
			// get current link
			LinkImpl link = outgoingLinks.get(i);
			
			// get destination node
			DijkstraNode toNode = dijkstraNodeMap.get(link.getToNode());
			
			// if node has not been visited
			if (!toNode.isVisited())
			{			
				double shortDist = node.getMinDist() + getLinkCost(link, time); 
	            
				// Found new shortest path to the destination node?
				if (shortDist < toNode.getMinDist())
				{
					// store new "shortest" distance
					setMinDistance(toNode, shortDist);
		                                
					// store new "previous" node
					setPreviousNode(toNode, node);
				}

			}
		}        
	}

	private boolean isVisited(DijkstraNode node)
	{
		return node.isVisited();
	}

	// for external call
	public double getMinDistance(DijkstraNode node)
	{
		return node.getMinDist(); 
	}

	/*
	 * Store the "shortest" distances to every node.
	 * Also actualize the Queue (remove node and add it again to put it at the right place in the Queue).
	 * If the node was not already in the queue: add it.
	 */
	private void setMinDistance(DijkstraNode node, double distance)
	{
		unvisitedNodes.remove(node);

		node.setMinDist(distance);

		// add node to the queue
		unvisitedNodes.add(node);
	}
	
	/*
	 * Get cost of the given link. This can be for example its length or the time to pass the link.
	 */
	protected double getLinkCost(LinkImpl link, double time)
	{   
		return costCalculator.getLinkTravelCost(link, time);
	}

	public void setCostCalculator(TravelCost calculator)
	{
		costCalculator = calculator;
	}
	
	public TravelCost getCostCalculator()
	{
		return costCalculator;
	}
	
	public void setCalculationTime(double time)
	{
		this.time = time;
	}
	
	public double getCalculationTime()
	{
		return time;
	}
	
	public DijkstraNode getPreviousNode(DijkstraNode node)
	{
		return node.getPrevNode();
	}
	
	
	private void setPreviousNode(DijkstraNode a, DijkstraNode b)
	{
		a.setPrevNode(b);
	}
	

	/* 
	 * Reset nodes.
	 * visited -> false
	 * mindist -> Double.MAX_DOUBLE
	 */
	private void resetDijkstraNodes()
	{
		for (DijkstraNode node : dijkstraNodeMap.values()) 
		{	
			node.reset();
		}
	}	// resetDijkstraNodes
	
}	// class DijkstraForSelectNodes


/*
 * Internal data structure with information about a node, the "distance" to it and its previous node.
 */
class DijkstraNode
{
	static private HashMap<NodeImpl, DijkstraNode> dijkstraNodeMap;
	
	NodeImpl node = null;
	DijkstraNode prevNode = null;
	boolean visited;
	double minDist;

	static void setNodeMap(HashMap<NodeImpl, DijkstraNode> map)
	{
		dijkstraNodeMap = map;
	}
	
	protected DijkstraNode(NodeImpl node) 
	{
		this.node = node;
		reset();
	}
	
	protected NodeImpl getNode()
	{
		return node;
	}
	
	protected void setPrevNode(DijkstraNode node)
	{
		prevNode = node;
	}
	
	protected DijkstraNode getPrevNode() 
	{
		return prevNode;
	}
	
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public double getMinDist() 
	{
		return minDist;
	}

	public void setMinDist(double minDist) 
	{
		this.minDist = minDist;
	}
	
	protected void reset()
	{
		visited = false;
		minDist = Double.MAX_VALUE;
	}

	protected ArrayList<DijkstraNode> ingoingNodes()
	{
		ArrayList<DijkstraNode> ingoingNodes = new ArrayList<DijkstraNode>();
		
		Map<Id, NodeImpl> myMap = (Map<Id, NodeImpl>)node.getInNodes();
		
		Iterator nodeIterator = myMap.values().iterator();
		while(nodeIterator.hasNext())
		{
			NodeImpl node = (NodeImpl)nodeIterator.next();

			// zugeh�rigen DijsktraNode aus der Map holen
			DijkstraNode dijkstraNode = dijkstraNodeMap.get(node);
			ingoingNodes.add(dijkstraNode);
		}
		return ingoingNodes;
	}
	
	protected ArrayList<DijkstraNode> outgoingNodes()
	{
		ArrayList<DijkstraNode> outgoingNodes = new ArrayList<DijkstraNode>();
		
		Map<Id, NodeImpl> myMap = (Map<Id, NodeImpl>)node.getOutNodes();
		
		Iterator nodeIterator = myMap.values().iterator();
		while(nodeIterator.hasNext())
		{
			NodeImpl node = (NodeImpl)nodeIterator.next();

			// zugeh�rigen DijsktraNode aus der Map holen
			DijkstraNode dijkstraNode = dijkstraNodeMap.get(node);
			outgoingNodes.add(dijkstraNode);
		}
		return outgoingNodes;
	}

	protected ArrayList<LinkImpl> ingoingLinks()
	{
		ArrayList<LinkImpl> ingoingLinks = new ArrayList<LinkImpl>();
		
		Map<Id, LinkImpl> myMap = (Map<Id, LinkImpl>)node.getInLinks();
		
		Iterator linkIterator = myMap.values().iterator();
		while(linkIterator.hasNext())
		{
			LinkImpl link = (LinkImpl)linkIterator.next();
			ingoingLinks.add(link);
		}
		return ingoingLinks;
	}
	
	protected ArrayList<LinkImpl> outgoingLinks()
	{
		ArrayList<LinkImpl> outgoingLinks = new ArrayList<LinkImpl>();
		
		Map<Id, LinkImpl> myMap = (Map<Id, LinkImpl>)node.getOutLinks();
		
		Iterator linkIterator = myMap.values().iterator();
		while(linkIterator.hasNext())
		{
			LinkImpl link = (LinkImpl)linkIterator.next();
			outgoingLinks.add(link);
		}
		return outgoingLinks;
	}	
	
}	// class DijkstraNode
