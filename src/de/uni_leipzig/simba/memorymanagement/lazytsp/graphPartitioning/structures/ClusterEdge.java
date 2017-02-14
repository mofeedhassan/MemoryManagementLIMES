package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;

public class ClusterEdge {
	public String id="";
	public Cluster source;
	public Cluster target;
	public int weight=0;
	
	public ClusterEdge(Cluster s, Cluster t, int w)
	{
		this.source=s;
		this.target=t;
		this.weight=w;
		this.id= s.id+"-"+t.id;
	}
	
	@Override
	public String toString() {
		return "Edge No: "+id+" ("+source.id+","+target.id+")";
	}
}
