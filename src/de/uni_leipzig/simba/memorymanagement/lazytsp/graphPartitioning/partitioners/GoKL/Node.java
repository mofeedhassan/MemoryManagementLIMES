package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKL;

public class Node {

	public int id; // node id
	public Edge[] edges; // the edges connected to the node
	public int gain; // the gain of swapping the node
	public int part; // the partition the node belongs to
	public boolean lock; // flag if the node is locked or not
	
	/*  edges []*Edge
	  // KL/FM stuff
	  gain int
	  part int
	  lock bool*/  
	
	//calculates the gain that can be achieved when put the node out of the partition
	public int calcGain()
	{
		int icost=0,ecost=0;
		
		for (Edge edge : edges) //iterate over edges
		{
			Node nbar; // assigned to the node on the other side of the edge
			if(this.equals(edge.n2)) 
				nbar= edge.n1;
			else
				nbar=edge.n2;
			 
			if(this.part == nbar.part)// both nodes are in the same parition -> innercost
				icost+=edge.w;
			else // both nodes are in different parition -> outercost
				ecost+=edge.w;
		}
		
		return ecost - icost; // the gain
	}
	
	//calculate the gain of swapping the two nodes
	public int calcSwap(Node nbar)
	{
		Edge e=null;
		//iterate over edges
	
		for (Edge edge : edges) {
			if(edge.n1.equals(nbar) || edge.n2.equals(nbar))
			{
				e=edge;
				break;
			}
		}
		
/*		int weight=0;
		if(e!=null)
			weight=e.w;*/
		
		int nodeGain_1 = this.calcGain();
		int nodeGain_2 = nbar.calcGain();
		
		return nodeGain_1 + nodeGain_2 + e.w;
				
	}
	
	
/*	 func (n *Node) calcGain() int {
		  var ecost,icost int
		  for _,e := range n.edges {
		    var n2 *Node
		    if n == e.n2 { n2 = e.n1 } else { n2 = e.n2 }
		    if n.part == n2.part {
		      icost += e.w
		    } else {
		      ecost += e.w
		    }
		  }
		  return ecost - icost
		}*/
	
	
	
	/*func (n *Node) calcSwap(n2 *Node) int {
		  var E *Edge
		  for _,e := range n.edges {
		    if e.n1 == n2 || e.n2 == n2 {
		      E = e
		      break
		    }
		  }
		  c,c2 := n.calcGain(),n2.calcGain()
		  return c + c2 - E.w
		}*/
	@Override
	public boolean equals(Object obj) {
		return ( this.id == ((Node) obj).id);
	}
}
