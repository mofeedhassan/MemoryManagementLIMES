package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKL;

public class Partitions {

	Buckets[] parts;
	int[] sizes;
	/*parts []*Buckets  // an array[2] of lists, each is a list of buckets
	  sizes []int  // to keep track of the sum of bucket sizes
*/
	public Partitions()
	{
		parts = new Buckets[2];
		sizes = new int[2];
	}
	
	public void insertNode(Node n)
	{
		sizes[n.part]++;
		Buckets b = parts[n.part];
		b.insertNode(n);
	}
	
	public void fillPartitions (Graph g)
	{
		parts = new Buckets[2];
		sizes = new int[2];
		for (int i=0; i<2; i++) {
			parts[i] = new Buckets();
			parts[i].side =i;
			parts[i].positive = new BList[g.maxN];
			parts[i].negative = new BList[g.maxN];
		}
		
		for(int i=0; i<g.nodes.length ; i++ )
		{
			g.nodes[i].lock = false;
			insertNode(g.nodes[i]);
			
		}
	}

	
/*	func (p *Partitions) fillPartitions( g *Graph ) {
		  p.parts = make([]*Buckets,2)
		  p.sizes = make([]int,2)
		  for i,_ := range p.parts {
		    p.parts[i] = new(Buckets)
		    p.parts[i].side = i
		    p.parts[i].pos = make( []*BList, g.maxN )
		    p.parts[i].neg = make( []*BList, g.maxN )
		  }
		  for i,_ := range g.nodes {
		    g.nodes[i].lock = false
		    p.insertNode(&(g.nodes[i]))
		  }
		  
		}*/
	
	

	
	/*func (p *Partitions) insertNode( n *Node ) {
		  p.sizes[n.part]++
		  B := p.parts[n.part]
		  B.insertNode(n)
		}*/
}
