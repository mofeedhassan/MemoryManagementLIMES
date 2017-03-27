package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoModified;

import java.util.HashMap;

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
		//insert nodes info in the partition
		sizes[n.part]++;
		Buckets b = parts[n.part];
		//insert the node into the bucket in parts[i]
		b.insertNode(n);
	}
	
	public void fillPartitions (Graph g)
	{
		/*parts = new Buckets[2];
		sizes = new int[2];*/
		for (int i=0; i<2; i++) {
			parts[i] = new Buckets();
			parts[i].side =i;
			parts[i].positive = new HashMap<>();// BList[g.maxN];
			parts[i].negative = new HashMap<>(); //new BList[g.maxN];
		}
		
		for(int i=0; i<g.nodes.length ; i++ )
		{
			g.nodes[i].lock = false;
			insertNode(g.nodes[i]);
			
		}
	}


}
