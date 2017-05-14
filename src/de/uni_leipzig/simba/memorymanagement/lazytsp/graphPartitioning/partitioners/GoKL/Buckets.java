package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKL;

public class Buckets {

	public int side; // the partition it belongs to
	public int size; // total number of nodes remaining in the buckets
	//stored the individual buckets in two arrays, one for the negative buckets and one for the non-negative buckets.
	public BList[] positive;
	public BList[] negative;
	public int maxB;//the size of the highest value bucket  for constant time lookup of the associated bucket when finding the best node for swapping.
	
/*	side int
	  size int
	  pos []*BList
	  neg []*BList
	  maxB  int // max bucket size 
*/
	// insert node into the bucket
	public void insertNode(Node n)
	{
		BList[] side;
		int pos;
		
		if(n.gain < 0)
		{
			side = negative;
			pos = -1 * n.gain;
		}
		else
		{
			side = positive;
			pos = n.gain;
		}
		
		BList b1 =  side[pos];
		
		if(b1 == null)
		{
			side[pos] = new BList();
			b1=side[pos];
			b1.gain = n.gain;
		}
		
		if(b1.gain >  maxB)
			maxB = b1.gain;
		
		b1.nodes.push(n);
		size++;
		
	}
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
	
	//find the best node [[need to b checked again]]
	public Node bestNode()
	{
		Node n=null;
		for(;maxB > -1 * negative.length; maxB--) //
		{
			BList b1;
			int position;
			if(maxB < 0 )// if it is negative
			{
				position = -1 * maxB; // get its position
				b1 = negative[position]; //retrieve it
			}
			else
			{
				b1 = positive[maxB];
			}
			
			if(b1 != null && b1.nodes != null && b1.nodes.size() > 0)
			{
				n = (Node) b1.nodes.pop();
				size--;
				break;
			}
		}
		return n;
	}
	
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
	
	public void updateNode (Node n)
	{
		BList[] side;
		int position;
		
		if(n.gain < 0)
		{
			side = negative;
			position = -1 * n.gain;
		}
		else
		{
			side = positive;
			position = n.gain;
		}
		
		BList bl = side[position];
		bl.nodes.remove(n);
		n.gain = n.calcGain();
		insertNode(n);
		size--;
	}
	
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
