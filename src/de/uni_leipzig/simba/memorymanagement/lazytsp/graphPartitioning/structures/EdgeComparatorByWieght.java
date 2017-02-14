package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures;

import java.util.Comparator;

public class EdgeComparatorByWieght implements Comparator<CoarsenedEdge> {

	@Override
	public int compare(CoarsenedEdge o1, CoarsenedEdge o2) {
		if(o1.getWeight() > o2.getWeight()) return +1;
        else if(o1.getWeight() < o2.getWeight()) return -1;
        else return 0;
	}

}

