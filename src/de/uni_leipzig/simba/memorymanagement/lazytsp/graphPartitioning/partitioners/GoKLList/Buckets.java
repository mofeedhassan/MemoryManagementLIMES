package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKLList;

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
	

	// insert node into the positive or negative bucket
	public void insertNode(Node n)
	{
		
		Map<Integer,BList> side;// = new HashMap<Integer,BList>();
		int pos;
		
		if(n.gain < 0)
		{
			side = negative;
			pos = -1 * n.gain;
			System.out.println(" Negative Buckets ");
		}
		else
		{
			side = positive;
			pos = n.gain;
			System.out.println(" Positive Buckets ");

		}
		
		//does it already exists
		BList b1 =  side.get(pos);
		
		if(b1 == null)
		{
			System.out.println("does not exist insert new nod in the BList stack ");

			side.put(pos,new BList());
			b1=side.get(pos);
			b1.gain = n.gain;
		}
		
		System.out.println(" MaxB after = "+maxB);
		// keep track of the maximum recorded gain
		if(b1.gain >  maxB)
			maxB = b1.gain;
		
		System.out.println(" MaxB after = "+maxB);

		
		//for such gain value (pos) add to the stack another node ahieves this value (stack contains node with similar gain)
		b1.nodes.push(n);
		System.out.println(" Pushed in stack ");
		size++;
		System.out.println(" size = "+size);

		
	}

	
	//find the best node [[need to b checked again]]
	public Node bestNode()
	{
		System.out.println("Best node of partition "+ side);
		Node n=null;
		//for(;maxB > -1 * negative.size(); maxB--) //
		System.out.println(" Partition "+side+" MaxB " + maxB);
		for(;maxB > -1 * negative.size(); maxB--) //
		{
			BList b1;
			int position=-999;
			if(maxB < 0 )// if it is negative
			{
				position = -1 * maxB; // get its position and make it negative as it was
				b1 = negative.get(position); //retrieve the bucket containing nodes scoring this gain
				System.out.println(" Negative Buckets ");

			}
			else
			{
				b1 = positive.get(maxB);
				System.out.println(" Positive Buckets ");
			}
			
			if(b1 != null && b1.nodes != null && b1.nodes.size() > 0)
			{
				System.out.println(" Stack of "+ position);
				for (Object element : b1.nodes) {
					System.out.println(element);
				}
				n = (Node) b1.nodes.pop(); // remove the top node
				System.out.println("Poped as best node is "+ n);
				size--;
				System.out.println(" Bucket "+ side + " has size "+ size);

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
			System.out.println(" Negative Buckets ");

		}
		else
		{
			side = positive;
			position = n.gain;
			System.out.println(" Positive Buckets ");
		}
		
		BList bl = side.get(position);
		System.out.println(" BList inside the bucket ="+bl.gain);
		System.out.println(" With Stack of ");
		for (Object element : bl.nodes) {
			System.out.println(element);
		}
		
		bl.nodes.remove(n);
		n.gain = n.calcGain();
		System.out.println("calculate new gain and inset the node in the BList inside the partition");
		insertNode(n);
		size--;
	}
	
	/*	side int
	  size int
	  pos []*BList
	  neg []*BList
	  maxB  int // max bucket size 
*/
	
	/*	func (b *Buckets) insertNode(n *Node) { 
	  var side []*BList
	  var pos int
	  if n.gain < 0 {
	    side = b.neg
	    pos = -1 * n.gain
	  } else {
	    side = b.pos
	    pos =  n.gain
	  }
	  
	   bl := side[pos]
if bl == nil {
side[pos] = new(BList)
bl = side[pos]
bl.gain = n.gain
}

if bl.gain > b.maxB {
b.maxB = bl.gain
}
bl.pushNode(n)
b.size++
	  */
	
	
	/*	func (b *Buckets) bestNode() (n *Node) {
	  for ; b.maxB > -len(b.neg); b.maxB-- {
	    var bl *BList
	    if b.maxB < 0 {
	      pos := -1 * b.maxB
	      bl = b.neg[pos]
	    } else {
	      bl = b.pos[b.maxB]
	    }
	    if bl != nil && bl.nodes != nil && bl.nodes.Len() > 0 {
	      n = bl.popNode()
	      b.size--
	      break;
	    }
	  }
	  return
	}*/
	
/*	func (b *Buckets) updateNode( n *Node ) {
		  var side []*BList
		  var pos int
		  if n.gain < 0 {
		    side = b.neg
		    pos = -1 * n.gain
		  } else {
		    side = b.pos
		    pos =  n.gain
		  }
		  bl := side[pos]
		  bl.rmvNode(n)
		  n.CalcGain()
		  b.insertNode(n)  
		  b.size-- // readjust for insertNode(n) size++ op
		}*/

}
