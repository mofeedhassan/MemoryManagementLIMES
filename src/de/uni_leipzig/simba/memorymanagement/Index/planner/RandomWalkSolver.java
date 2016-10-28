package de.uni_leipzig.simba.memorymanagement.Index.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matheclipse.core.reflection.system.Array;

import com.hp.hpl.jena.graph.Graph;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;

public class RandomWalkSolver implements PathFinder{

	public static int clustersSize;
	/**
	 * Dependency matrix between clusters
	 *
	 * @param clusters
	 * @return
	 */
    /*private List<List<Integer>> getAjacencies(Map<Integer, Cluster> clusters) {

		clustersSize = clusters.size();
		List<List<Integer>> grpah = new ArrayList<List<Integer>>();

		for (int i : clusters.keySet()) {
			Cluster a = clusters.get(i);
			List<Integer> neighbor = new ArrayList<Integer>();

			for (int j : clusters.keySet())
				if(i!=j)
				{
					Cluster b = clusters.get(j);
					for (Node n : a.nodes) {
						if (b.nodes.contains(n)) {
							neighbor.add(j);
							break;
						}
					}
				}
			grpah.add(i, neighbor);
		}
		
		return grpah;
    }
    
    public int[] getRandomWalkPath(List<List<Integer>> graph)
    {
    	int size = clustersSize;
    	int[] path = new int[size];
    	int p=0;
    	Random rand = new Random();
    	int  currentNode = rand.nextInt(size);//start node
    	int neighborNode=-1;
    	path[p] = currentNode;
    	
    	while(p<clustersSize-1)
    	{
    		List<Integer> neighbors = graph.get(currentNode);
    		if(neighbors.size()>0)
    		{
    			int neighborIndex =  rand.nextInt(neighbors.size());
    			neighborNode= neighbors.get(neighborIndex);
    			path[++p] = neighborNode;
    			
    			for (Integer neighbor : neighbors) {
					int currentNodeIndexAtNeighborhood = graph.get(neighbor).indexOf(currentNode);
					graph.get(neighbor).remove(currentNodeIndexAtNeighborhood);
				}
    			graph.remove(currentNode);
    			size--;
    			currentNode = neighborNode;
    			
    		}
    		else
    		{
    			graph.remove(p);
    			currentNode = rand.nextInt(size);
    		}
    	}
    	
    	
    	
    	return path;
    }*/
	
	private Map<Integer,List<Integer>> getAjacencies(Map<Integer, Cluster> clusters) {

		clustersSize = clusters.size();
		Map<Integer,List<Integer>> grpah = new LinkedHashMap<Integer, List<Integer>>();

		for (int i : clusters.keySet()) {
			Cluster a = clusters.get(i);
			List<Integer> neighbor = new ArrayList<Integer>();

			for (int j : clusters.keySet())
				if(i!=j)
				{
					Cluster b = clusters.get(j);
					for (Node n : a.nodes) {
						if (b.nodes.contains(n)) {
							neighbor.add(j);
							break;
						}
					}
				}
			grpah.put(i, neighbor);
		}
		
		return grpah;
    }
    
    public int[] getRandomWalkPath(Map<Integer,List<Integer>> graph)
    {
    	int size = clustersSize; // get the size of the graph to randomize the initial node based on
    	int[] path = new int[size]; 
    	int p=0; // index to the path
    	
    	Random rand = new Random();
    	int  currentNode = rand.nextInt(size);//randomize initial node
    	int neighborNode=-1; // index of the neighbor to be the next visted node
    	path[p] = currentNode; // add the initial node to the path
    	
    	while(p<clustersSize-1) // as long as not all nodes added to the path (-1 as initial node is added already)
    	{
    		System.out.println("current Node: "+currentNode);
    		List<Integer> neighbors = graph.get(currentNode); // get the neighbor of the current visted node
    		if(/*neighbors!= null && */neighbors.size()>0) // condition removed as the while in else won't allow new randomization out of the available IDs && when it has no empty neighbor list
    		{
    			System.out.println("neihbors: "+neighbors);
    			int neighborIndex =  rand.nextInt(neighbors.size()); // randomize the index of a neighbor node to be the next node to visit
    			neighborNode= neighbors.get(neighborIndex); // get the next node to be visited
    			System.out.println("neighbor Node: "+neighborNode);
    			
    			for (Integer neighbor : neighbors) {// remove the current node from the neighbor lists of its neighbors , this sometimes make a neighbor list empty for future nodes
					int currentNodeIndexAtNeighborhood = graph.get(neighbor).indexOf(currentNode);
					graph.get(neighbor).remove(currentNodeIndexAtNeighborhood);
				}
    			graph.remove(currentNode); // remove current node from the graph as it is visited and we are leaving now
    			System.out.println("remove current node Node: "+currentNode);
    			
    			currentNode = neighborNode; // assign the current one to be the new one 
    			path[++p] = currentNode; // add the current newly visited node to the path
    			
    		}
    		else // its neighbor list was emptied by a previous visiting node
    		{
    			System.out.println("current node has no negihbor or removed");
    			
    			if(/*neighbors!= null &&*/ neighbors.size()== 0) // It has empty neighbors list because of previously visited node and being end of single path
    			{
    				graph.remove(currentNode); //remove this dead end node from the graph
    				System.out.println("remove current node Node: "+currentNode);
    			}
    			currentNode = rand.nextInt(size); // randomize for new initial node
    			while(!graph.containsKey(currentNode)) // as long as the new node does not exist (visited node)
    				currentNode = rand.nextInt(size);
    			path[++p] = currentNode; // new initial node add to the path
    			System.out.println("current node Node: "+currentNode);

    		}
    	}
    	
    	
    	
    	return path;
    }

	@Override
	public int[] getPath(Map<Integer, Cluster> clusters) {
		Map<Integer,List<Integer>> graph= getAjacencies(clusters);
		
		return getRandomWalkPath(graph);
	}

}
