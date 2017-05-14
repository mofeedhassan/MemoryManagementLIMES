package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKL;

import java.util.Random;

public class KLFM {

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
	      	int diff = p.sizes[0] - p.sizes[1];
	      	// partition selection
	      	if ((diff > 0 && p.parts[0].size > 0) || p.parts[1].size == 0) 
	      			{
	      				B = p.parts[0];
	      				p.sizes[0]--;
	      				p.sizes[1]++;
	      			} 
	      	else 
					{
				        B = p.parts[1];
					    p.sizes[0]++;
					    p.sizes[1]--;
			  		 }
	      	
	      	Node N=null;
	      	// get best node & swap
	      	N  = B.bestNode();
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
			
			int l = n.edges.length;
			
			if( l > max)
				max=l;
		}
		
		System.out.println("The partitions' nodes");
		for (Node nn : g.nodes) {
			System.out.println(nn.id+" in "+ nn.part);
		}
		
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
	
	/*public Results klfm(Graph g)
	{
		Results res = new Results();
		Partitions p = partitionGraph(g);
		
		int cut1 = g.calcCut();
		int cut2= cut1+1;
		
		int min = 1000000;
		
		int[] PARTS = new int[g.nodes.length];
		
		int iteration=0;
		while(cut1 < cut2)
		{
			cut2=cut1;
			int count=0;
			while(count < g.nodes.length)
			{
				Buckets b;
				int diff = p.sizes[0]-p.sizes[1];
				if(diff > 0  && p.parts[0].side > 0 || p.parts[1].side == 0)
				{
					b=p.parts[0];
					p.sizes[0]--;
					p.sizes[1]++;
				}
				else
				{
					b = p.parts[1];
					p.sizes[0]++;
					p.sizes[1]--;
				}
				Node n = b.bestNode();
				n.part = (n.part+1)%2;
				n.lock = true;
				n.gain=n.calcGain();
				
				// update neigbbors
				Node n2=null;
				for (Edge e : n.edges) {
					if(e.n1.equals(n))
						n2=e.n2;
					else
						n2=e.n1;
					if(n2.lock == false)
					{
						p.parts[n2.part].updateNode(n2);
					}
					else
					{
						n2.gain = n2.calcGain();
					}
				}
				
				// calculate cut value (fix? maxes loop O(N*E) )
			    cut1 = g.calcCut();
			    
			    // save iteration best
			    if (cut1 < cut2) { 
			    	cut2 = cut1; 
			      }
			      // save global best
			      if (cut1 < min) { 
			        min = cut1; 
			        // save partition
			        for (int i=0; i <  g.nodes.length; i++) {
						PARTS[i] = n.part;
					}
      

			      }
			      count++;
			    }
				
					

			}
		}
		p = partitionGraph(g)
				  17 Bewertungen / 2.9 Sterne von 5
				  Cut = g.calcCut()
				  CUT = Cut + 1

				  min = 1000000
				  PARTS = make( []int, len(g.nodes) )
				  
				  iter = 0
				  for Cut < CUT { // while finding improvements
				    CUT = Cut
				    cnt = 0
				    for cnt < len(g.nodes) { // while unlocked nodes
				      var B *Buckets
				      diff = p.sizes[0]-p.sizes[1]
				      // partition selection
				      if (diff > 0 && p.parts[0].size>0) || p.parts[1].size == 0 {
				        B = p.parts[0]
				        p.sizes[0]--
				        p.sizes[1]++
				      } else {
				        B = p.parts[1]
				        p.sizes[0]++
				        p.sizes[1]--
				      }

				      // get best node & swap
				      N  = B.bestNode()
				      N.part = (N.part+1)%2
				      N.lock = true
				      N.CalcGain()

				      // update neigbbors
				      for _,e = range N.edges {
				        var N2 *Node
				        if e.n1 == N {
				          N2 = e.n2
				        } else {
				          N2 = e.n1
				        }
				        if N2.lock == false {
				          p.parts[N2.part].updateNode(N2)
				        } else {
				          N2.CalcGain()
				        }
				      }

				      // calculate cut value (fix? maxes loop O(N*E) )
				      cut = g.calcCut()
				      
				      // save iteration best
				      if cut < Cut { 
				        Cut = cut 
				      }
				      // save global best
				      if cut < min { 
				        min = cut 
				        // save partition
				        for i,n = range g.nodes {
				          PARTS[i] = n.part 
				        }
				      }
				      cnt++
				    }
				    
				    // roll back
				    for i,_ = range g.nodes {
				      g.nodes[i].part = PARTS[i]
				    }
				    
				    // refill gain buckets
				    p.fillPartitions(g)
				    iter++
				  }
				  return min,PARTS
	}
*/
}
