package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary;

import java.util.Comparator;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.CoarsenedEdge;

public class PEdgeComparatorByWieght implements Comparator<PEdge> {

	@Override
	public int compare(PEdge o1, PEdge o2) {
		if(o1.weight > o2.weight) return +1;
        else if(o1.weight < o2.weight) return -1;
        else return 0;
	}

}
