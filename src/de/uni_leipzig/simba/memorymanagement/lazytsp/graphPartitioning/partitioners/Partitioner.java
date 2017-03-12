package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public interface Partitioner {
	public MGraph3 getPartitionedGraph(MGraph3 g);

}
