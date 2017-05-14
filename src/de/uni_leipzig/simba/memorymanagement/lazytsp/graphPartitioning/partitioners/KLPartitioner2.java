package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners;

import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;

public class KLPartitioner2 implements Partitioner{

	int[] coarsenedNodeID_KLIDMappings;
	int[] nodeGain;
	int[] nodePartition;
	boolean[] nodeLock;
	int[][] degreeMatrix;
	int[][] adjancyMatrix;
	int maxNeighborCount=4;
	int numberOfNodes =0;
	boolean ignoringIsolated =false;
	
	public KLPartitioner2(MGraph3 g, boolean ignoreIsolated )
	{	
		this.ignoringIsolated=ignoreIsolated;
		if(ignoreIsolated)
			numberOfNodes = g.coarsenedEdgesWeights.keySet().size();
		else
			numberOfNodes = g.coarsenedEdgesWeights.keySet().size()+g.isolatedNodeWeights.size();
		
		coarsenedNodeID_KLIDMappings= new int[numberOfNodes];
		nodeGain= new int[numberOfNodes];
		nodePartition= new int[numberOfNodes];
		nodeLock= new boolean[numberOfNodes];
		
		
		int i=0;
		for (Integer nodeId : g.coarsenedEdgesWeights.keySet()) {
			coarsenedNodeID_KLIDMappings[i++] = nodeId;
			nodeGain[i++]=0;
			nodePartition[i++]=0;
			nodeLock[i++]=false;
		}
		if(!ignoreIsolated)
			for (Integer nodeId : g.isolatedNodeWeights.keySet()) {
				coarsenedNodeID_KLIDMappings[i++] = nodeId;
				nodeGain[i++]=0;
				nodePartition[i++]=0;
				nodeLock[i++]=false;
			}
		
		
		createDegreeMatrix(g);
		createAdjacencyMatrix(g);
		
		
	}
	
	private void createDegreeMatrix(MGraph3 g)
	{
		degreeMatrix =  new int[numberOfNodes][numberOfNodes];
		
		for(int i=0;i<numberOfNodes;i++)
			for(int j=0;j<numberOfNodes;j++)
			if(i==j)
			{
				if(g.coarsenedEdgesWeights.containsKey(coarsenedNodeID_KLIDMappings[i]))// node exist in coarsened list and MAY be connected
				{
					Map<Integer,Integer> neighbors =  g.coarsenedEdgesWeights.get(coarsenedNodeID_KLIDMappings[i]);
					degreeMatrix[i][j] = neighbors.keySet().size();
				}
			}
			else
				degreeMatrix[i][j]=0;
	}
	
	private void createAdjacencyMatrix(MGraph3 g)
	{
		adjancyMatrix =  new int[numberOfNodes][numberOfNodes];
		
		for(int i=0;i<numberOfNodes;i++)
			for(int j=0;j<numberOfNodes;j++)
			if(i==j)
			{
				if(g.coarsenedEdgesWeights.containsKey(coarsenedNodeID_KLIDMappings[i]))// node exist in coarsened list and MAY be connected
				{
					Map<Integer,Integer> neighbors =  g.coarsenedEdgesWeights.get(coarsenedNodeID_KLIDMappings[i]);
					if(neighbors==null) //has no neighbors (isolated)
						adjancyMatrix[i][j]= adjancyMatrix[i][i] = 0;
					else
						adjancyMatrix[i][j] = adjancyMatrix[j][i] = neighbors.get(j);//put the weight in the matrix
				}
			}
			else
				adjancyMatrix[i][j]=0;
	}
	@Override
	public MGraph3 getPartitionedGraph(MGraph3 g) {
		// TODO Auto-generated method stub
		
		return null;
	}
	
	public int calculateGain (int nodeId)
	{
		int externalCost=0,InnerCoast=0;
		for(int i=0;i<numberOfNodes;i++)
		{
			if(adjancyMatrix[nodeId][i] > 0)// an edge found
			{
				if(nodePartition[nodeId] == nodePartition[i])// in the same partition
					InnerCoast+=adjancyMatrix[nodeId][i];
				else
					externalCost+=adjancyMatrix[nodeId][i];
			}
				
		}
		return externalCost-InnerCoast;
	}
	
	public int calculateSwap(int nodeId)
	{
		int pairNode=-1;
		for(int i=0;i<numberOfNodes;i++)
		{
			if(adjancyMatrix[nodeId][i] > 0)// an edge found
			{
				pairNode = i;
				break;
			}
		}
		
		int c = calculateGain(nodeId);
		int c2 = calculateGain(pairNode);
		return c + c2 - adjancyMatrix[nodeId][pairNode];
	}
	public int calculateCut()
	{
		int cut=0;
		for(int i=0;i<numberOfNodes;i++)
		{
			for(int j=0;j<numberOfNodes;j++)
			{
				if(i != j && nodePartition[i] != nodePartition[j])
					cut+= adjancyMatrix[i][j];
			}
		}
	  return cut;
	}

}
