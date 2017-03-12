package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;

public class CoarsnersFactory {

	public static Coarsener createCoarsner(CoarsnerType type, MGraph3 graph)
	{
		Coarsener coarsner = null;
		if(type.equals(CoarsnerType.HEVSNODE))
			return new HEVSortedNodes(graph);
		else if(type.equals(CoarsnerType.HEVSEDGE))
			return new HEVSortedEdges(graph);
		else
			return null;
	}
}
