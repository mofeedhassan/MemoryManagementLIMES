package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoModified;

import java.util.Random;

public class Graph {
	public Node[] nodes;
	public Edge[] edges;
	public int maxN; //maximum neighbor count to the graph so that I can bound the number of gain buckets I need to [-maxN:maxN].
	
	public Graph(int nSize, int eSize)
	{
		nodes= new Node[nSize];
		edges =  new Edge[eSize];
	}
	
/*	nodes []Node
	  edges []Edge
	  maxN int*/
	
	//calculate the total edge cut between the Bi-partitions in the graph
	public int calcCut()
	{
		int cut=0;
		for (Edge edge : edges) {
			if(edge.n1.part != edge.n2.part)
				cut+= edge.w;
		}
		return cut;
	}
	
/*	func (g *Graph) calcCut() int {
		  var cut int
		  for _,e := range g.edges {
		    if e.n1.part != e.n2.part {
		      cut += e.w
		    }
		  }
		  return cut
		}*/
	
/*	public Partitions partitionGraph(Graph g)
	{
		//randomly assign nodes
		int max=0;
		float r;
		Node n=null;
		for (int i=0; i< nodes.length ; i++) {
			r = randomFloat();
			n=nodes[i];
			
			if(r < 0.5)
			{
				n.part=0;
			} else
			{
				n.part =1;
			}
			int l = nodes.length;
			if( l > max)
				max=l;
		}
		
		maxN = max+1;
		for (Node node : nodes) {
			node.gain = node.calcGain();
		}
		Partitions p = new Partitions();
		p.fillPartitions(g);

		return p;
	}
	
	private float randomFloat()
	{
		float min=0,max=1.0f;
		Random rand = new Random();

		float finalX = rand.nextFloat() * (max - min) + min;
		return finalX;
	}
*/
}
