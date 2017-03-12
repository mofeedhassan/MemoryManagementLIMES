package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.Sorters;

public class HEVSortedNodes extends HEV {

	private final int margin = 5;

	public HEVSortedNodes(MGraph3 g) {
		super(g);
	}

	@Override
	public MGraph3 coarsen()
	{
		return sortedCoarseningDelayingIsolatedNodes();
	}
	public MGraph3 sortedCoarsening() {
		
		MGraph3 results= new MGraph3();
	 
		 int merge_count= (int)(reduction_factor * allFineGraphSize);
		 int reductedSize = (int)(reduction_factor * allFineGraphSize);

		 while(merge_count > 0 && !coarsenedEdgesWeights.isEmpty())
		 {
			 
			 level++;
			 //sort connected coarsened nodes by their weights
			 List<Entry<Integer, Integer>> nodesSortedList = Sorters.entriesSortedByValues(coarsenedNodeWeights, ascending);
			 initializeMatching();		 
			 
			 for (Entry<Integer, Integer> node : nodesSortedList) {
					
				 if(!matching.get(node.getKey())) //not matched yet with a  previous node
				 {
					 int nodeId = node.getKey();
					 //get its neighbor nodes
					 Map<Integer, Integer> neighborsEdgesWeights = coarsenedEdgesWeights.get(nodeId);
					 if(neighborsEdgesWeights.containsKey(-1)) //isolated node
						 continue; //skip it
					 int maxNeighbor = -1;
					 int maxedgeWeight=-1;
					 //find the neighbor connected by the edge with max. weight
					 for (Integer neighbor : neighborsEdgesWeights.keySet()) {
						if(neighborsEdgesWeights.get(neighbor) > maxedgeWeight && !matching.get(neighbor)) //neighbor is not matched yet with a  previous node
						{
							maxNeighbor = neighbor;
							maxedgeWeight = neighborsEdgesWeights.get(neighbor);
						}
					}
					 //maxNeighbor  source
					 //nodeId target
					 if(maxNeighbor != -1) //  node to merge is found always merge to the neighbor as it is definitely larger than the node if it is available (match=false)
					 {
						//merge the nodes of target into nodes of source
							Set<Integer> targetContainedNodes = coarsenedNodesContainedNodes.get(nodeId);
							coarsenedNodesContainedNodes.get(maxNeighbor).addAll(targetContainedNodes);
							coarsenedNodesContainedNodes.remove(nodeId);
							
							//node weights are updated is updated first NODE={node1, node 2, node 3,...}
							int newWeight = coarsenedNodeWeights.get(maxNeighbor) + coarsenedNodeWeights.get(nodeId); //calc. new weight
							coarsenedNodeWeights.put(maxNeighbor, newWeight); //update source node as coarsened
							coarsenedNodeWeights.remove(nodeId); //remove target node as it is merged into source
							
							//mark both as matched
							matching.put(maxNeighbor, true);
							matching.put(nodeId, true);
							
							//updae edges
							Map<Integer,Integer> sourceNieghbors = coarsenedEdgesWeights.get(maxNeighbor);
							Map<Integer,Integer> targetNieghbors = coarsenedEdgesWeights.get(nodeId);
							
							//remove common edge
							sourceNieghbors.remove(nodeId);
							targetNieghbors.remove(maxNeighbor);
							
							for (Integer targetNieghbor : targetNieghbors.keySet()) {
								int newEdgeWeight =-1;
								if(sourceNieghbors.keySet().contains(targetNieghbor)) //target Neighbor is common node
								{
									newEdgeWeight = sourceNieghbors.get(targetNieghbor) + targetNieghbors.get(targetNieghbor);
									sourceNieghbors.put(targetNieghbor, newEdgeWeight);
								}
								else //not common node
								{
									newEdgeWeight = targetNieghbors.get(targetNieghbor);
									sourceNieghbors.put(targetNieghbor, newEdgeWeight);
								}
								coarsenedEdgesWeights.get(targetNieghbor).put(maxNeighbor,newEdgeWeight); // add the source as a neighbor to the target's neighbors
								coarsenedEdgesWeights.get(targetNieghbor).remove(nodeId); // remove the target itself from its neighbors'
							}
							
							coarsenedEdgesWeights.remove(nodeId);

					 }
				 }
			 }
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }//while
		 
		// merge count is done but not enough to reduce the size to the target size due to existence of isolated nodes, then merge them spcificallyy
		 if(reductedSize < coarsenedNodeWeights.size() + isolatedNodeWeights.size())
		 {
			 
			 List<Entry<Integer, Integer>> nodesSortedList = Sorters.entriesSortedByValues(coarsenedNodeWeights,!ascending);
			 List<Entry<Integer, Integer>> nodesIsolatedSortedList = Sorters.entriesSortedByValues(isolatedNodeWeights,ascending);
			 int  maxWeight = nodesSortedList.get(0).getValue();
			 
			 int isolatedNodeIndex=0;
			 for (Entry<Integer, Integer> coarsenedNode : nodesSortedList) {
				 int coarsenedNodeId = coarsenedNode.getKey();
				 //cond1: did not get close to the max size, cond2: more isolated nodes
				while(coarsenedNodeWeights.get(coarsenedNode.getKey()) < (maxWeight-margin) && isolatedNodeIndex < nodesIsolatedSortedList.size())
				{
					int id = nodesIsolatedSortedList.get(isolatedNodeIndex).getKey();
					int weight = nodesIsolatedSortedList.get(isolatedNodeIndex).getValue();
					
					coarsenedNodesContainedNodes.get(coarsenedNodeId).add(id);//add its id to the contained node
					coarsenedNodeWeights.put(coarsenedNodeId, coarsenedNodeWeights.get(coarsenedNodeId)+weight);//update the weight

					isolatedNodeIndex++;
				}
				//cond1: te reduction size goal is met cond2: no more nodes
				if(reductedSize >= coarsenedNodeWeights.size() + isolatedNodeWeights.size() || isolatedNodeIndex >= nodesIsolatedSortedList.size())
					break;
			}
		 }
		 
		 serializecCoarsenedGraph(++level);

		 results.coarsenedNodeWeights = coarsenedNodeWeights;
		 results.coarsenedEdgesWeights=coarsenedEdgesWeights;
		 results.coarsenedNodesContainedNodes=coarsenedNodesContainedNodes;
		 results.isolatedNodeWeights=isolatedNodeWeights;

		 return results;
	}

public MGraph3 sortedCoarseningDelayingIsolatedNodes() {
		
		MGraph3 results= new MGraph3();
	 
		 int merge_count= (int)(reduction_factor * allFineGraphSize);
		 int reductedSize = (int)(reduction_factor * allFineGraphSize);

		 while(merge_count > 0 && !coarsenedEdgesWeights.isEmpty())
		 {
			 
			 level++;
			 //sort connected coarsened nodes by their weights
			 List<Entry<Integer, Integer>> nodesSortedList = Sorters.entriesSortedByValues(coarsenedNodeWeights, ascending);
			 initializeMatching();		 
			 
			 for (Entry<Integer, Integer> node : nodesSortedList) {
					
				 if(!matching.get(node.getKey())) //not matched yet with a  previous node
				 {
					 int nodeId = node.getKey();
					 //get its neighbor nodes
					 Map<Integer, Integer> neighborsEdgesWeights = coarsenedEdgesWeights.get(nodeId);
					 if(neighborsEdgesWeights.containsKey(-1)) //isolated node
						 continue; //skip it
					 int maxNeighbor = -1;
					 int maxedgeWeight=-1;
					 //find the neighbor connected by the edge with max. weight
					 for (Integer neighbor : neighborsEdgesWeights.keySet()) {
						if(neighborsEdgesWeights.get(neighbor) > maxedgeWeight && !matching.get(neighbor)) //neighbor is not matched yet with a  previous node
						{
							maxNeighbor = neighbor;
							maxedgeWeight = neighborsEdgesWeights.get(neighbor);
						}
					}
					 //maxNeighbor  source
					 //nodeId target
					 if(maxNeighbor != -1) //  node to merge is found always merge to the neighbor as it is definitely larger than the node if it is available (match=false)
					 {
						//merge the nodes of target into nodes of source
							Set<Integer> targetContainedNodes = coarsenedNodesContainedNodes.get(nodeId);
							coarsenedNodesContainedNodes.get(maxNeighbor).addAll(targetContainedNodes);
							coarsenedNodesContainedNodes.remove(nodeId);
							
							//node weights are updated is updated first NODE={node1, node 2, node 3,...}
							int newWeight = coarsenedNodeWeights.get(maxNeighbor) + coarsenedNodeWeights.get(nodeId); //calc. new weight
							coarsenedNodeWeights.put(maxNeighbor, newWeight); //update source node as coarsened
							coarsenedNodeWeights.remove(nodeId); //remove target node as it is merged into source
							
							//mark both as matched
							matching.put(maxNeighbor, true);
							matching.put(nodeId, true);
							
							//updae edges
							Map<Integer,Integer> sourceNieghbors = coarsenedEdgesWeights.get(maxNeighbor);
							Map<Integer,Integer> targetNieghbors = coarsenedEdgesWeights.get(nodeId);
							
							//remove common edge
							sourceNieghbors.remove(nodeId);
							targetNieghbors.remove(maxNeighbor);
							
							for (Integer targetNieghbor : targetNieghbors.keySet()) {
								int newEdgeWeight =-1;
								if(sourceNieghbors.keySet().contains(targetNieghbor)) //target Neighbor is common node
								{
									newEdgeWeight = sourceNieghbors.get(targetNieghbor) + targetNieghbors.get(targetNieghbor);
									sourceNieghbors.put(targetNieghbor, newEdgeWeight);
								}
								else //not common node
								{
									newEdgeWeight = targetNieghbors.get(targetNieghbor);
									sourceNieghbors.put(targetNieghbor, newEdgeWeight);
								}
								coarsenedEdgesWeights.get(targetNieghbor).put(maxNeighbor,newEdgeWeight); // add the source as a neighbor to the target's neighbors
								coarsenedEdgesWeights.get(targetNieghbor).remove(nodeId); // remove the target itself from its neighbors'
							}
							
							coarsenedEdgesWeights.remove(nodeId);

					 }
				 }
			 }
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }//while
 
	 
		 serializecCoarsenedGraph(++level);

		 results.coarsenedNodeWeights = coarsenedNodeWeights;
		 results.coarsenedEdgesWeights=coarsenedEdgesWeights;
		 results.coarsenedNodesContainedNodes=coarsenedNodesContainedNodes;
		 results.isolatedNodeWeights=isolatedNodeWeights;

		 return results;
	}


}
