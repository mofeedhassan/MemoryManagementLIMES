package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

public class CoarsnersFactory {

	public static Coarsener createCoarsner(CoarsnerType type)
	{
		Coarsener coarsner = null;
		if(type.equals(CoarsnerType.HEV))
			return new HEV();
		else
			return null;
	}
}
