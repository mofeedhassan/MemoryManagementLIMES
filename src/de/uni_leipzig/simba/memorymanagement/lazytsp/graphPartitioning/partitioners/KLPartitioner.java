package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners;

import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public class KLPartitioner implements Partitioner {

	int[] cNodeID_KLIDMappings; // this array uses the normal indexing instead of the coarsened nodes indexing to be used b KL
	//for example, a coarsened node 5 contains 1,3,4 can't be used directly with Id 5 and the absence of 1,3 and 4 in the matix, so it should be renamed
	//as node 0 for example so the matrix has no odds. This array carries the mappings that node 5 in coarsened graph is named here as 0
	int[][] degreeMatrix;
	int[][] adjancyMatrix;
	int numberOfNodes;
	boolean ignoringIsolated=false;
	MGraph3 originalCoarsenedGraph;
	
	public KLPartitioner(MGraph3 g, boolean ignoreIsolated )
	{	
		this.ignoringIsolated=ignoreIsolated;
		if(ignoreIsolated)
			numberOfNodes = g.coarsenedEdgesWeights.keySet().size();
		else
			numberOfNodes = g.coarsenedEdgesWeights.keySet().size()+g.isolatedNodeWeights.size();
		
		cNodeID_KLIDMappings= new int[numberOfNodes];
		int i=0;
		for (Integer nodeId : g.coarsenedEdgesWeights.keySet()) {
			cNodeID_KLIDMappings[i++] = nodeId;
		}
		if(!ignoreIsolated)
			for (Integer nodeId : g.isolatedNodeWeights.keySet()) {
				cNodeID_KLIDMappings[i++] = nodeId;
			}
		
		createDegreeMatrix(g);
		createAdjacencyMatrix(g);
		
		originalCoarsenedGraph = g;
	}
	
	public void initialPartitioning()
	{
		for(int i =0 ; i< cNodeID_KLIDMappings.length ; i ++)
		{
			int n_id = cNodeID_KLIDMappings[i];
			
		}
	}
	
	@Override
	public MGraph3 getPartitionedGraph(MGraph3 g) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private coarsenedGraph KL(MGraph3 g)
	{
		return null;
	}
	
	private void createDegreeMatrix(MGraph3 g)
	{
		degreeMatrix =  new int[numberOfNodes][numberOfNodes];
		
		for(int i=0;i<numberOfNodes;i++)
			for(int j=0;j<numberOfNodes;j++)
			if(i==j)
			{
				if(g.coarsenedEdgesWeights.containsKey(cNodeID_KLIDMappings[i]))// node exist in coarsened list and MAY be connected
				{
					Map<Integer,Integer> neighbors =  g.coarsenedEdgesWeights.get(cNodeID_KLIDMappings[i]);
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
				if(g.coarsenedEdgesWeights.containsKey(cNodeID_KLIDMappings[i]))// node exist in coarsened list and MAY be connected
				{
					Map<Integer,Integer> neighbors =  g.coarsenedEdgesWeights.get(cNodeID_KLIDMappings[i]);
					if(neighbors==null) //has no neighbors (isolated)
						adjancyMatrix[i][j]= adjancyMatrix[i][i] = 0;
					else
						adjancyMatrix[i][j] = adjancyMatrix[j][i] = neighbors.get(j);//put the weight in the matrix
				}
			}
			else
				adjancyMatrix[i][j]=0;
	}

}
