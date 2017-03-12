package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.PEdge;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.Sorters;

public class HEVSortedEdges extends HEV {

	private final int margin = 5;

	public HEVSortedEdges(MGraph3 g) {
		super(g);
	}

	@Override
	public MGraph3 coarsen()
	{
		return sortedCoarsening();
	}
	
	public MGraph3 sortedCoarsening() {
		
		MGraph3 results= new MGraph3();
	 
		 int merge_count= (int)(reduction_factor * allFineGraphSize);
		 int reductedSize = (int)(reduction_factor * allFineGraphSize);

		 while(merge_count > 0 && !coarsenedEdgesWeights.isEmpty())
		 {
			 
			 level++;
			 //sort connected coarsened nodes by their weights
			 TreeSet<PEdge> edesSet = new TreeSet<>();
			 for (Integer source : coarsenedEdgesWeights.keySet()) {
				 for (Integer target : coarsenedEdgesWeights.get(source).keySet()) {
					 edesSet.add(new PEdge(source, target, coarsenedEdgesWeights.get(source).get(target)));
				}
				 
			}
			 
			 initializeMatching();		 
			 
			 for (PEdge pEdge : edesSet.descendingSet()) {
					int source = pEdge.source;
					int target = pEdge.target;
					
					if((coarsenedNodeWeights.containsKey(source) && coarsenedNodeWeights.containsKey(target)) && (!matching.get(source) && !matching.get(target)))
					{
						//merge the nodes of target into nodes of source
						Set<Integer> targetContainedNodes = coarsenedNodesContainedNodes.get(target);
						coarsenedNodesContainedNodes.get(source).addAll(targetContainedNodes);
						coarsenedNodesContainedNodes.remove(target);
						
						//node weights are updated is updated first NODE={node1, node 2, node 3,...}
						int newWeight = coarsenedNodeWeights.get(source)+coarsenedNodeWeights.get(target); //calc. new weight
						coarsenedNodeWeights.put(source, newWeight); //update source node as coarsened
						coarsenedNodeWeights.remove(target); //remove target node as it is merged into source
						
						//mark both as matched
						matching.put(source, true);
						matching.put(target, true);
						
						//updae edges
						Map<Integer,Integer> sourceNieghbors = coarsenedEdgesWeights.get(source);
						Map<Integer,Integer> targetNieghbors = coarsenedEdgesWeights.get(target);
						
						//remove common edge
						sourceNieghbors.remove(target);
						targetNieghbors.remove(source);
						
						
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
							coarsenedEdgesWeights.get(targetNieghbor).put(source,newEdgeWeight); // add the source as a neighbor to the target's neighbors
							coarsenedEdgesWeights.get(targetNieghbor).remove(target); // remove the target itself from its neighbors'
						}
						coarsenedEdgesWeights.remove(target);
					}
					
			 }
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }//while
		 
		// merge count is done but not enough to reduce the size to the target size due to existence of isolated nodes, then merge them with coarsened
		 //nodes, the same tech. is used with sorted nodes and is used here too as isolated nodes have no edges so we depend on the nodes' weights
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

}
