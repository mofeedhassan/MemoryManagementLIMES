package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoModified;

import java.util.Random;
import java.util.logging.Logger;

public class KLFM {

	static Logger logger = Logger.getLogger("LIMES"); 

public Results klfm(Graph g)
{
	  Partitions p = partitionGraph(g);
	  int Cut = g.calcCut();
	  int CUT = Cut + 1;

	  int min = 1000000;
	  int[] PARTS = new int[g.nodes.length];
	  
	  int iter = 0;
	  while (Cut < CUT) 
	  { // while finding improvements
	    CUT = Cut;
	    int cnt = 0;
	    
	    while (cnt < g.nodes.length) 
        { // while unlocked nodes
	    	Buckets B; 
	      	int diff = p.sizes[0] - p.sizes[1];// check which partition is greater
	      	// partition selection
	      	if ((diff > 0 && p.parts[0].size > 0) || p.parts[1].size == 0) 
	      			{
	      				B = p.parts[0];
	      				//update partitions counters
	      				p.sizes[0]--;
	      				p.sizes[1]++;
	      			} 
	      	else 
					{
				        B = p.parts[1];
	      				//update partitions counters
					    p.sizes[0]++;
					    p.sizes[1]--;
			  		 }
	      	
	      	/*for(int i=0;i < p.sizes.length; i++)
	      		System.out.println("parttiotion "+i+" has " + p.sizes[i]);*/
	      	
	      	Node N=null;
	      	// get best node & swap
	      	N  = B.bestNode();
	/*      	if(N!=null)
	      	{*/
	      		N.part = (N.part+1)%2;
		      	N.lock = true;
		      	N.gain = N.calcGain();

		      	// update neigbbors
		      	
		      	for( Edge e : N.edges) 
	             {
		      		Node N2= null;
		        	if (e.n1.equals(N) )
		        	{
		        		N2 = e.n2;
		        	} 
		        	else 
		        	{
		        		N2 = e.n1;
		        	}

		        	if (N2.lock == false )
		        	{
		        		p.parts[N2.part].updateNode(N2);
		        	} 
		        	else 
		        	{
		        		N2.gain=N2.calcGain();
		        	}
		      	 }
	//      	}
	      	

	         // calculate cut value (fix? maxes loop O(N*E) )
	         int cut = g.calcCut();
	      
	         // save iteration best
	         if (cut < Cut )
	         { 
	        	 Cut = cut; 
	         }
	         // save global best
	         if (cut < min) 
             { 
	           min = cut; 
	           // save partition
	           for(int i = 0 ; i < g.nodes.length; i++)// i,n = range g.nodes 
               {
	        	   Node n = g.nodes[i];
	               PARTS[i] = n.part; 
	           }
	         }
	         cnt++;
	       }
	    
	       // roll back
		    for (int i=0; i < g.nodes.length ; i++) {
				g.nodes[i].part = PARTS[i];
			}
	    
	       // refill gain buckets
	       p.fillPartitions(g);
	       iter++;
	    }
	  
	    Results res = new Results();
	    res.MIN = min;
	    res.parts = PARTS;
	    
	    return res;
}
	
	public Partitions partitionGraph(Graph g)
	{
		//randomly assign nodes
		int max=0;
		float r;
		Node n=null;
		
		for (int i=0; i < g.nodes.length ; i++) {
			r = randomFloat();
			n=g.nodes[i];
			
			if(r < 0.5)
			{
				n.part=0;
			} else
			{
				n.part =1;
			}
			
			//System.out.println("node "+ n.id + " is in partition "+ n.part);
			int l = n.edges.length;
			
			if( l > max)
				max=l;
		}
		/*System.out.println("the max neigborhood = "+ max);
		System.out.println("The partitions' nodes");
		for (Node nn : g.nodes) {
			System.out.println(nn.id+" in "+ nn.part);
		}*/
		
		g.maxN = max+1;
		for (Node node : g.nodes) {
			node.gain = node.calcGain();
		}
		
		Partitions p = new Partitions();
		p.fillPartitions(g);

		return p;
	}
	
	/*	func partitionGraph( g *Graph ) (p *Partitions) {
	  // randomly assign nodes
	  max = 0
	  for i,_ = range g.nodes {
	    r = rand.Float64()
	    n = &(g.nodes[i])
	    if r < 0.5 {
	      n.part = 0;
	    } else {
	      n.part = 1;
	    }
	    if l=len(n.edges); l > max {
	      max = l
	    }
	  }
	  g.maxN = max+1 // make sure we have enough gain buckets spots
	  for i,_ = range g.nodes {
	    g.nodes[i].CalcGain()
	  }
	  
	  // make partitions
	  p = new( Partitions )
	  p.fillPartitions(g)
	  
	  return p
	}*/
	
	private float randomFloat()
	{
		float min=0,max=1.0f;
		Random rand = new Random();

		float finalX = rand.nextFloat() * (max - min) + min;
		return finalX;
	}
	
	public class Results
	{
		int MIN;
		int[] parts;
	}
	
	
}
