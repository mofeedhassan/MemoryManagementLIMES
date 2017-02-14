package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures;

import java.util.HashSet;

public class CEdge{
	public int source;
	public int target;
	public int weight;
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof CEdge)
		{
			CEdge o = (CEdge) other;
			if((this.source == o.source) && (this.target == o.target) || (this.source == o.target) && (this.target == o.source))
				return true;
			return false;

		}
		return false;
	}
}
