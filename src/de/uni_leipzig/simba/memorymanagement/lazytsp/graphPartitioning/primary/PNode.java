package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PNode implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 1901422624966587664L;
	public int id;
	public int weight;
	// its neighborhood is stored by the graph itself
	public PNode(){}
	public PNode(int id){ this.id=id;}
	public PNode(int id, int weight){ this.id=id; this.weight=weight;}
	public PNode(int id, int weight,Set<Integer> neighbors){ this.id=id; this.weight=weight;}

	@Override
	public boolean equals(Object other) {
			if(other instanceof PNode)
			{
				PNode o = (PNode) other;
				return (this.id == o.id);
			}
			return false;
	}
}
