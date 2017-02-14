package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary;

import java.io.Serializable;
import java.util.Objects;

public class PEdge implements Serializable,Comparable<PEdge>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4962788905606032165L;
	public int source;
	public int target;
	public int weight;
	
	public PEdge(){}
	public PEdge(int s, int t, int w)
	{
		source=s;
		target=t;
		weight=w;
	}
	public int hasCommonNode(PEdge other)
	{
		if(this.source == other.source)
			return this.source;
		if(this.target == other.target)
			return this.target;
		if(this.source == other.target)
			return this.source;
		if(this.target == other.source)
			return this.target;
		return -1;
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof PEdge)
		{
			PEdge o = (PEdge) other;
			if(((this.source == o.source) && (this.target == o.target)) || ((this.source == o.target) && (this.target == o.source)))
				return true;
			
			return false;
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		return 31*(source+target)-37*Math.min(source,target);
		//return (int) (37*(source+target)*(Math.pow(source, target)+Math.pow(target, source)));
    }
	@Override
	public int compareTo(PEdge o) {
		if(this.weight > o.weight) return 1;
		if(this.weight ==  o.weight) return 0;
		return -1;
	}
}
