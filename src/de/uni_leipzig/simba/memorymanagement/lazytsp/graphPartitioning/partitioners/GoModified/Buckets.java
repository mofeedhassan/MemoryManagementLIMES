package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoModified;

import java.util.HashMap;
import java.util.Map;
// iteration of calculating the nodes gain, the bucket is either calculated to part 0 or part 1
public class Buckets {

	public int side; // the partition it belongs to
	public int size; // total number of nodes remaining in the buckets
	//stored the individual buckets in two arrays, one for the negative buckets and one for the non-negative buckets.
	public Map<Integer,BList> positive; //gain:BList
	public Map<Integer,BList> negative;
	public int maxB;//the size of the highest value bucket  for constant time lookup of the associated bucket when finding the best node for swapping.
	public int minB;
	
							

	// insert node into the positive or negative bucket
	public void insertNode(Node n)
	{
		
		Map<Integer,BList> side;// = new HashMap<Integer,BList>();
		int pos;
		
		if(n.gain < 0)
		{
			side = negative;
			pos = -1 * n.gain;
			//System.out.println(" Negative Buckets ");
		}
		else
		{
			side = positive;
			pos = n.gain;
			//System.out.println(" Positive Buckets ");

		}
		
		//does it already exists
		BList b1 =  side.get(pos);
		
		if(b1 == null)
		{
			//System.out.println("does not exist insert new nod in the BList stack ");

			side.put(pos,new BList());
			b1=side.get(pos);
			b1.gain = n.gain;
		}
		
		//System.out.println(" MaxB after = "+maxB);
		// keep track of the maximum recorded gain
		if(b1.gain >  maxB)
			maxB = b1.gain;
		if(b1.gain <  minB)
			minB = b1.gain;
		
		//System.out.println(" MaxB after = "+maxB);

		
		//for such gain value (pos) add to the stack another node ahieves this value (stack contains node with similar gain)
		b1.nodes.push(n);
		//System.out.println(" Pushed in stack ");
		size++;
		//System.out.println(" size = "+size);

		
	}

	
	//find the best node [[need to b checked again]]
	public Node bestNode()
	{
		//System.out.println("Best node of partition "+ side);
		Node n=null;
		//for(;maxB > -1 * negative.size(); maxB--) //
		//System.out.println(" Partition "+side+" MaxB " + maxB);
		for(;maxB >= minB; maxB--) //
		{
			BList b1;
			int position=-999;
			if(maxB < 0 )// if it is negative
			{
				position = -1 * maxB; // get its position and make it negative as it was
				b1 = negative.get(position); //retrieve the bucket containing nodes scoring this gain
				//System.out.println(" Negative Buckets ");

			}
			else
			{
				b1 = positive.get(maxB);
				//System.out.println(" Positive Buckets ");
			}
			
			//System.out.println("Retrieved BList with gain = "+b1.gain);
			if(b1 != null && b1.nodes != null && b1.nodes.size() > 0)
			{
				
				/*System.out.println(" Stack of "+ position);
				for (Object element : b1.nodes) {
					System.out.println(((Node)element).id);
				}*/
				n = (Node) b1.nodes.pop(); // remove the top node
				//System.out.println("Poped as best node is "+ n);
				size--;
				//System.out.println(" Bucket "+ side + " has size "+ size);

				break;
			}
		}
		return n; // give it back
	}
	
	public void updateNode (Node n)
	{
		Map<Integer,BList> side;
		int position;
		
		if(n.gain < 0)
		{
			side = negative;
			position = -1 * n.gain;
			//System.out.println(" Negative Buckets ");

		}
		else
		{
			side = positive;
			position = n.gain;
			//System.out.println(" Positive Buckets ");
		}
		
		BList bl = side.get(position);
		/*System.out.println(" BList inside the bucket ="+bl.gain);
		System.out.println(" With Stack of ");
		for (Object element : bl.nodes) {
			System.out.println(element);
		}*/
		
		bl.nodes.remove(n);//remove the node from its BList inner stack (<gain,BList> an entry in this bucket) as after recalculating
							//its gain it will be inserted in the same Bucket but with new entry gain
		n.gain = n.calcGain();
		//System.out.println("calculate new gain and insert the node in the BList inside the partition");
		insertNode(n); //add the node with its new gain as an entry to the Buckets here (this will cause the size to increment although we just reposition it)
		size--; // cancel the insertNode() increment effect ( it is repositioning)
	}
}
