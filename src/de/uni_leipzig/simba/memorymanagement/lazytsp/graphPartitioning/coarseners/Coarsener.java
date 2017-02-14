package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public interface Coarsener {
	public coarsenedGraph getCorsenedGraph(coarsenedGraph g);
}
