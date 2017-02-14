package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.CoarsenedEdge;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.EdgeComparatorByWieght;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedNode;

public class HEV implements Coarsener{

/*	@Override
	public coarsenedGraph getCorsenedGraph(coarsenedGraph g) {
		//node,setedges,setnodes,
		List<CoarsenedEdge> list=new ArrayList<CoarsenedEdge>(g.getAllEdges());
		Collections.sort(list, new EdgeComparatorByWieght()); //sort edges
		Collections.reverse(list);
		for (CoarsenedEdge edge : list) {
			Map<CoarsenedEdge, Set<coarsenedNode>> edgeNodes = g.getEdgeNodeMap();
			Set<coarsenedNode> nodes = edgeNodes.get(edge); // edge.getSource()
			System.out.println(nodes.size());
			coarsenedNode s = null;
			coarsenedNode t =  null;
			for (coarsenedNode coarsenedNode : nodes) { 
				if(coarsenedNode.getId()==edge.getSource())//merge o the source
					s = coarsenedNode;
				else
					t=coarsenedNode;
			}
			s.mergeTo(t);
		}
		return null;
	}*/
	
	@Override
	public coarsenedGraph getCorsenedGraph(coarsenedGraph g) {
		//node,setedges,setnodes,
		List<CoarsenedEdge> list=new ArrayList<CoarsenedEdge>(g.getAllEdges());
		Collections.sort(list, new EdgeComparatorByWieght()); //sort edges
		Collections.reverse(list);
		Map<CoarsenedEdge,Integer> mappedEdges =  new HashMap<>();
		for (CoarsenedEdge coarsenedEdge : list) {
			mappedEdges.put(coarsenedEdge, coarsenedEdge.getWeight());
		}
		System.out.println(mappedEdges);
		for (CoarsenedEdge edge : list) {
			if(mappedEdges.containsKey(edge))
			{
				System.out.println(((CoarsenedEdge) edge).getSource()+":"+((CoarsenedEdge) edge).getTarget());
				int s= ((CoarsenedEdge) edge).getSource();
				int t = ((CoarsenedEdge) edge).getTarget();
				
				if(!g.matching.get(s) && !g.matching.get(t))
				{
					Set<Integer> sourceClusters= g.coarsenedNodes.get(s);
					Set<Integer> targetClusters= g.coarsenedNodes.get(t);
					sourceClusters.addAll(targetClusters);
					
					int newWeight = g.allClusters.get(s)+g.allClusters.get(t);
					g.allClusters.put(s, newWeight);
					g.coarsenedNodes.remove(t);
					g.matching.put(s, true);
					g.matching.put(t, true);
					Set<Integer> sourceNieghbors =  g.neighborhood.get(s);
					Set<Integer> targetNieghbors =  g.neighborhood.get(t);
					sourceNieghbors.remove(t);
					targetNieghbors.remove(s);
					
					for (Integer neighbor : sourceNieghbors) {
						if(targetNieghbors.contains(neighbor))//common node
						{
							int total = mappedEdges.get(new CoarsenedEdge(s, neighbor, 0)) + mappedEdges.get(new CoarsenedEdge(t, neighbor, 0)) ;
							targetNieghbors.remove(neighbor);
							mappedEdges.remove(new CoarsenedEdge(t, neighbor, 0));
							mappedEdges.put(new CoarsenedEdge(s, neighbor, total), total);
							g.neighborhood.get(s).add(neighbor);//no need as it is already a neighbor
							g.neighborhood.get(neighbor).add(s);//same as above
						}
					}
					for (Integer neighbor : targetNieghbors) {
						int w =  mappedEdges.get(new CoarsenedEdge(t, neighbor, 0));
						mappedEdges.put(new CoarsenedEdge(s, neighbor, w), w);
						mappedEdges.remove(new CoarsenedEdge(t, neighbor, 0));
						g.neighborhood.get(neighbor).remove(t);
						g.neighborhood.get(s).add(neighbor);
						g.neighborhood.get(neighbor).add(s);
					}
					g.allClusters.remove(t);
					mappedEdges.remove(new CoarsenedEdge(s, t, 0));
					mappedEdges.remove(new CoarsenedEdge(t, s, 0));

					g.neighborhood.remove(t);
					g.neighborhood.get(s).remove(t);
					
				}
			}
			
			System.out.println(g.neighborhood);
			/*Map<CoarsenedEdge, Set<coarsenedNode>> edgeNodes = g.getEdgeNodeMap();
			Set<coarsenedNode> nodes = edgeNodes.get(edge); // edge.getSource()
			System.out.println(nodes.size());
			coarsenedNode s = null;
			coarsenedNode t =  null;
			for (coarsenedNode coarsenedNode : nodes) { 
				if(coarsenedNode.getId()==edge.getSource())//merge o the source
					s = coarsenedNode;
				else
					t=coarsenedNode;
			}
			s.mergeTo(t);*/
		}
		return null;
	}
	

}
