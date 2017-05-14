package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKLList;

public class Edge {

	public int id;
	public int w;
	public Node n1;
	public Node n2;
	 /*id int
	  w  int
	  n1,n2 *Node*/
	@Override
	public boolean equals(Object obj) {
		return ( this.id == ((Edge) obj).id);
	}
}
