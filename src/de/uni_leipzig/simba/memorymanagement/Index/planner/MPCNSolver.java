package de.uni_leipzig.simba.memorymanagement.Index.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;

/**
 * @author mofeed
 * Max Path from the Current Nodes Solver
 *
 */
public class MPCNSolver implements PathFinder{

	double epsilon = 10d;
	public int clustersSize=9;
	private double maxEdgeWeight = -Double.MIN_VALUE;
	private int maxICluster=1;
	int maxJCluster=-1;
	double[][] matrix;
	int NrOfProseccors =2;

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


	/**
	 * This method takes a node id and find out the neighbor node with max edge weight
	 * @param node
	 * @return 
	 */
	private Map<Integer,Integer> getMaxNieghbor(int node)
	{
		Map<Integer,Integer> maxNeighborPairs = new HashMap<Integer, Integer>();

		double maxWeight=0;
		int maxNeighbor=-1;
		int maxBordeNode =-1;
		for(int neighbor=0; neighbor<clustersSize; neighbor++)
			if(matrix[node][neighbor] >  maxWeight && matrix[node][neighbor] > 0 )
			{
				maxWeight=matrix[node][neighbor];
				maxNeighbor=neighbor;
				maxBordeNode = node;
			}
		if(maxWeight==0)
			return null;
		maxNeighborPairs.put(maxBordeNode, maxNeighbor) ;
		return maxNeighborPairs;
	}

	/**
	 * initialize the primary working set with n nodes where n = processor number
	 * @param matrix
	 * @return
	 */
	private Set<Integer> initializeWorkingSet(double[][] matrix)
	{
		Set<Integer> initialWorkingSet = new HashSet<Integer>();

		initialWorkingSet.add(getMaxICluster());

		for(int i=1;i<NrOfProseccors;i++)
		{
			//execludedNodes = new ArrayList<Integer>();
			// initialize variable to hold the weight,working set node on border and new candidate(reached by max edge weight) succeeded to be next in the path
			double maxCandidateWeight =0;
			int maxCandidate=-1;
			int maxCandidateBorderNode=-1;

			//for each node in the working set
			for (int node : initialWorkingSet) 
			{
				//initialize to save the max of (1) weight between the (2)node and the (3)neighbor overall nodes in working set
				double maxWeight=0;
				int maxNeighbor=-1;
				int maxBordeNode =-1;

				// iterate over the neighbors of the node in the matrix
				for(int neighbor=0; neighbor<clustersSize; neighbor++)
					if(matrix[node][neighbor] >  maxWeight && !initialWorkingSet.contains(neighbor))
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
			}

			// add new node to working set
			if(maxCandidate != -1)
			{
				for (Integer node : initialWorkingSet) { // cut edges between the nodes inside the primary initialization
					matrix[node][maxCandidate] = 0;
					matrix[maxCandidate][node]=0;
				}
				initialWorkingSet.add(maxCandidate);
			}
			//add the candidate with max edge weight to go through into the list
		}

		return initialWorkingSet;
	}


	public int[] getMPRNPath(double[][] matrix)
	{
		this.matrix = matrix;
		int[] path = new int[clustersSize];
		
		Set<Integer>  primaryWorkingNodes = new HashSet<Integer>();// the working nodes on processors
		Stack<Integer> secondaryWorkingNodes = new Stack<Integer>();// to save previously worked node, to get them back for other routes through them
		
		primaryWorkingNodes = initializeWorkingSet(matrix);// initialize the primary set based on number of processors
		
		int p =0;
		//add the initialized nodes to the path
		for (Integer node : primaryWorkingNodes) {
			path[p++]=node;
		}
		
		//start with shifted index
		for(int i=primaryWorkingNodes.size();i<clustersSize;i++)
		{
			// initialize variable to hold the weight,working set node on border and new candidate(reached by max edge weight) succeeded to be next in the path
			double maxCandidateWeight =0;
			int maxCandidate=-1;
			int maxCandidateBorderNode=-1;
			boolean givePush=false;

			//for each node in the working set
			for (int node : primaryWorkingNodes) 
			{ 
				Map<Integer,Integer> nodeNeighbor = getMaxNieghbor(node);// get max neighbor of the node

				if(nodeNeighbor == null) // has no neighbor , for some reason all around nodes are set to zero
				{
					if(secondaryWorkingNodes.size() > 0)
					{
						List<Integer> execluding = new ArrayList<Integer>();
						for(int index=secondaryWorkingNodes.size()-1; index >=0 ; index--)
						{
							int secondaryNode = secondaryWorkingNodes.get(index);
							nodeNeighbor = getMaxNieghbor(secondaryNode);// it must have unvisited neighbor otherwise it won't be added from the first place,it can if others zeros his neighbors
							if(nodeNeighbor!=null)// no neighbors, all its neighbors are zeros
							{
								double neighborEdgeWeight = matrix[secondaryNode][nodeNeighbor.get(secondaryNode)];

								//if the max neighbor of this node is greater than the overall max candidates neighbor
								if( neighborEdgeWeight > maxCandidateWeight) 
								{
									maxCandidateWeight = neighborEdgeWeight;
									maxCandidate=nodeNeighbor.get(secondaryNode);
									maxCandidateBorderNode=node;

								}
							}
							else 
								execluding.add(secondaryWorkingNodes.get(index));
							/*int secondaryNode = secondaryWorkingNodes.get(index);
							nodeNeighbor = getMaxNieghbor(secondaryNode);// it must have unvisited neighbor otherwise it won't be added from the first place,it can if others zeros his neighbors
							if(nodeNeighbor!=null)// all its neighbors are zeros
							{
								double neighborEdgeWeight = matrix[node][nodeNeighbor.get(secondaryNode)];

								//if the max neighbor of this node is greater than the overall max candidates neighbor
								if( neighborEdgeWeight!=0 && neighborEdgeWeight > maxCandidateWeight) 
								{
									maxCandidateWeight = neighborEdgeWeight;
									maxCandidate=nodeNeighbor.get(node);
									maxCandidateBorderNode=node;

								}
							}
							else 
								execluding.add(secondaryWorkingNodes.get(index));*/
						}
						for (Integer n : execluding) {
							secondaryWorkingNodes.remove(n);
						}
						// this means the node had no neighbor and all secondary working set have no neighbor to the degree they all are excluded and no candidate is provided of course
						if(secondaryWorkingNodes.isEmpty()) 
							givePush=true;
						
/*						int secondaryNode = secondaryWorkingNodes.pop();
						nodeNeighbor = getMaxNieghbor(secondaryNode);// it must have unvisited neighbor otherwise it won't be added from the first place,it can if others zeros his neighbors
						if(nodeNeighbor!=null)// all its neighbors are zeros
						{
							double neighborEdgeWeight = matrix[node][nodeNeighbor.get(secondaryNode)];

							//if the max neighbor of this node is greater than the overall max candidates neighbor
							if( neighborEdgeWeight!=0 && neighborEdgeWeight > maxCandidateWeight) 
							{
								maxCandidateWeight = neighborEdgeWeight;
								maxCandidate=nodeNeighbor.get(node);
								maxCandidateBorderNode=node;

							}
							secondaryWorkingNodes.push(secondaryNode); // as it has more neighbors push it again, to reach them from OR if a connected node to it was visited from another rout so it will
							//put the edge between both to zero in the loop of the "secondaryWorkingNodes" after this loop
						}*/
					}
				}
				else // has a neighbor with max edge weight found
				{
					double neighborEdgeWeight = matrix[node][nodeNeighbor.get(node)];
					//if the max neighbor of this node is greater than the overall max candidates neighbor
					if( neighborEdgeWeight > maxCandidateWeight)
					{
						maxCandidateWeight = neighborEdgeWeight;
						maxCandidate=nodeNeighbor.get(node);
						maxCandidateBorderNode=node;
					}

				}
			}

			
			//Removing the edge between x and y by clearing the (x,y) and (y,x) weights data in the matrix
			// if it does not have connected edge (came from secondary node) no problem it is anyway 0
			matrix[maxCandidateBorderNode][maxCandidate]=0;
			matrix[maxCandidate][maxCandidateBorderNode]=0;
			printMatrix(matrix);
			
			//check if there is an edge between the new added node to working set and another node in it other than the border node
			//that added it, so exclude such edge
			for (Integer node : primaryWorkingNodes) {
				if(matrix[maxCandidate][node] > 0)
				{
					matrix[maxCandidate][node]=0;
					matrix[node][maxCandidate]=0;
				}
			}

			for (Integer node : secondaryWorkingNodes) {
				if(matrix[maxCandidate][node] > 0)
				{
					matrix[maxCandidate][node]=0;
					matrix[node][maxCandidate]=0;
				}
			}
			printMatrix(matrix);

			// add the new node to the primary working set
			primaryWorkingNodes.add(maxCandidate);
			primaryWorkingNodes.remove(maxCandidateBorderNode);

			//matrix[maxCandidate][maxCandidateBorderNode]=matrix[maxCandidateBorderNode][maxCandidate]=0;

			if(getMaxNieghbor(maxCandidateBorderNode)!=null)//still the node in the border and a member of working set has some edges to unvisited nodes
				secondaryWorkingNodes.add(maxCandidateBorderNode);
			//add the candidate with max edge weight to go through into the list
			path[p++] = maxCandidate;

		}

		return path;
	}

	private void printMatrix(double[][] matrix)
	{
		for(int i=0;i<clustersSize;i++)
		{
			for(int j=0;j<clustersSize;j++)
				System.out.print(matrix[i][j]+"\t");
			System.out.println();
		}
		System.out.println("------------------------------------------------------");
	}
	@Override
	public int[] getPath(Map<Integer, Cluster> clusters) {
		matrix = getMatrix(clusters);
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
