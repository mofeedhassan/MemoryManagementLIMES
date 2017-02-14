package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;

public class SortedSolvertmp implements PathFinder{
	double epsilon = 10d;
    int clustersSize=12;

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
                    }
                }
            }
        }
        //printMatrix(matrix);
        return matrix;
    }
    // returns the edges sorted by weight with each edge associated with its nodes
    public Object[] sortingDistances(double[][] matrix){
    	//int[] sortedClusters = new int [clustersSize];
    	ArrayList<Integer> sortedClusters = new ArrayList<Integer>();
    	List<Double> simWeights = new ArrayList<Double>();
    	Map<Double,ArrayList<Integer>> edgeNodes = new HashMap<Double, ArrayList<Integer>>();
    	double maxDistance = Double.MIN_VALUE;
    	
    	for(int i=0;i<clustersSize;i++)
    		for(int j=0; j< clustersSize ; j++)
    		{
    			double weight = matrix[i][j];
    			if(i < j)
    			{
        			if(!simWeights.contains(weight))
        			{
        				simWeights.add(weight);
        				ArrayList clusters = new ArrayList<Integer>();
        				clusters.add(i); clusters.add(j);
        				edgeNodes.put(weight, clusters );
        			}
        			else // it is contained before
        			{
        				ArrayList clusters = edgeNodes.get(weight);
        				if(!clusters.contains(i))
        				{
        					clusters.add(i);
        					edgeNodes.put(weight, clusters );
        				}
        				if(!clusters.contains(j))
        				{
        					clusters.add(j);
        					edgeNodes.put(weight, clusters );
        				}
        				
        			}

    			}
    		}
    	Collections.sort(simWeights,Collections.reverseOrder());
    	int j = 0;
    	for (double simWeight : simWeights) {
			ArrayList<Integer> clusters = edgeNodes.get(simWeight);
			for (Integer cluster : clusters) {
				if(!sortedClusters.contains(cluster))
					sortedClusters.add(cluster);
			}
		}
    	return sortedClusters.toArray();
    }

    private int[] getPath(double[][] matrix)
    {
    	Object[] sortedClusters=  sortingDistances(matrix);
    	int[] path = new int[sortedClusters.length];
    	for (int i = 0; i < sortedClusters.length; i++) {
    		path[i] = (int)sortedClusters[i];
    	}
    	return path;    	
    }
	@Override
	public int[] getPath(Map<Integer, Cluster> clusters) {
		double[][] matrix = getMatrix(clusters);
		return getPath(matrix);
	}
}
