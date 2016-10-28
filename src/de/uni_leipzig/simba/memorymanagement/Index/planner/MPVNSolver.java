package de.uni_leipzig.simba.memorymanagement.Index.planner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;


/**
 * @author mofeed
 * Max Path from the Visited Nodes Solver
 *
 */
public class MPVNSolver implements PathFinder{

    double epsilon = 10d;
    public int clustersSize=9;
    private double maxEdgeWeight = -Double.MIN_VALUE;
    private int maxICluster=1;
	int maxJCluster=-1;
    
    /**
     * Dependency matrix between clusters
     *
     * @param clusters
     * @return
     */
    public double[][] getMatrix(Map<Integer, Cluster> clusters) {

    	clustersSize = clusters.keySet().size();
        //initialize with heavy edges
        double[][] matrix = new double[clusters.keySet().size()][clusters.keySet().size()];
        for (int i = 0; i < clusters.keySet().size(); i++) {
            for (int j = 0; j < clusters.keySet().size(); j++) {
                if (i != j) {
                    matrix[i][j] = epsilon;
                }
            }
        }

        for (int i : clusters.keySet()) {
            Cluster a = clusters.get(i);
        	//System.out.println(a.nodes.size()+":"+a.edges.size());

            for (int j : clusters.keySet()) {
                if (i < j) {
                    int weight = 0;
                    Cluster b = clusters.get(j);
//                	System.out.println(b.nodes.size()+":"+b.edges.size());
                    for (Node n : a.nodes) {
                        if (b.nodes.contains(n)) {
                            weight = weight + n.getWeight();
                        }
                    }
                    if (weight > 0) {
                        matrix[i][j] = 1d / weight;
                        matrix[j][i] = matrix[i][j];
                        if(weight > maxEdgeWeight)
                        {
                        	maxEdgeWeight = weight;
                        	setMaxICluster(i);
                        	maxJCluster = j;
                        }
                    }
                }
            }
        }
        return matrix;
    }
    
    public int[] getMPRNPath(double[][] matrix)
    {
    	Set<Integer>  workingNodes = new HashSet<Integer>();
    	int[] path = new int[clustersSize];
    	int p=0;
    	List<Integer> execludedNodes = new ArrayList<Integer>();
    	
    	//initialize the sets
    	workingNodes.add(getMaxICluster());
    	path[p++]=getMaxICluster();
    	
    	// for all nodes in the clusters graph starting from count 1 as initial value is already added at pos 0
    	for(int i=1;i<clustersSize;i++)
    	{
    		execludedNodes = new ArrayList<Integer>();
    		// initialize variable to hold the weight,working set node on border and new candidate(reached by max edge weight) succeeded to be next in the path
    		double maxCandidateWeight =0;
    		int maxCandidate=-1;
    		int maxCandidateBorderNode=-1;
    		
    		//for each node in the working set
    		for (int node : workingNodes) 
    		{
    			//initialize to save the max of (1) weight between the (2)node and the (3)neighbor overall nodes in working set
	    		double maxWeight=0;
	    		int maxNeighbor=-1;
	    		int maxBordeNode =-1;
	    		
	    		// iterate over the neighbors of the node in the matrix
				for(int neighbor=0; neighbor<clustersSize; neighbor++)
					if(matrix[node][neighbor] >  maxWeight)
					{
						maxWeight=matrix[node][neighbor];
						maxNeighbor=neighbor;
						maxBordeNode = node;
					}
				
				//if the max neighbor of this node is greater than the overall max candidates neighbor
				if(maxWeight > maxCandidateWeight)
				{
					maxCandidateWeight = maxWeight;
					maxCandidate=maxNeighbor;
					maxCandidateBorderNode=maxBordeNode;
				}
				
				if(maxWeight == 0) // all the row zeros
					execludedNodes.add(node);
    		}
    		
    		for (Integer execludedNode : execludedNodes) {
    			workingNodes.remove(execludedNode);
			}
    		//clear the x,y and y,x entries in the matrix as removing the edge
    		matrix[maxCandidateBorderNode][maxCandidate]=0;
    		matrix[maxCandidate][maxCandidateBorderNode]=0;
    		//check if there is an edge between the new added node to working set and another node in it other than the border node
    		//that added it, so exclude such edge
    		for (Integer node : workingNodes) {
				if(matrix[maxCandidate][node] > 0)
				{
					matrix[maxCandidate][node]=0;
					matrix[node][maxCandidate]=0;
				}
			}
    		// add new node to working set
    		workingNodes.add(maxCandidate);
    		//add the candidate with max edge weight to go through into the list
    		path[p++] = maxCandidate;
    	}
       
    
    	return path;
    }
    
  /*   private int[] getPath(double[][] matrix)
   {
    	Object[] sortedClusters=  null;//sortingDistances(matrix);
    	int[] path = new int[sortedClusters.length];
    	for (int i = 0; i < sortedClusters.length; i++) {
    		path[i] = (int)sortedClusters[i];
    	}
    	return path;    	
    }*/
    
	@Override
	public int[] getPath(Map<Integer, Cluster> clusters) {
		double[][] matrix = getMatrix(clusters);
		return getMPRNPath(matrix);
	}

	/**
	 * @return the maxICluster
	 */
	public int getMaxICluster() {
		return maxICluster;
	}

	/**
	 * @param maxICluster the maxICluster to set
	 */
	public void setMaxICluster(int maxICluster) {
		this.maxICluster = maxICluster;
	}

}
