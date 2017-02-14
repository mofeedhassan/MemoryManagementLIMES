package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.ClusterEdge;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.CoarsenedEdge;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.EdgeComparatorByWieght;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedNode;

public class PGraph {
	//structures work with coarsened nodes
    public Map<Integer,Boolean> coarsenedGraphmatching = new HashMap<>();//boolean checkers if a node is matched yet
    public Map<Integer,Cluster> allClusters = null; // the key will represent each fine graph node id
    public Map<Integer,HashSet<Integer>> coarsenedNodeContainedNodes = new HashMap<>();//for each coarsened nodes which nodes are included
    public Map<Integer,Integer> coarsenedNodeWeights = new HashMap<>();//for each coarsened nodes which nodes are included
    
    public Map<Integer,HashSet<Integer>> coarsenedNeighborhood = new HashMap<>();// initialized in createFineGraph
    public Map<Integer,Set<PEdge>> coarsenedNodeEdges = new HashMap<>(); //holds clusters neighborhood for fine graph (can be replaced where each node knows its fine neighbors

    public List<PEdge> coarsenedEdges = new ArrayList<>(); // initialized in creatFineGraph()
    //structures work with fine graph nodes only
    public Map<Integer,Set<PEdge>> fineGraphNodeEdges = new HashMap<>(); //holds clusters neighborhood for fine graph (can be replaced where each node knows its fine neighbors
    public Map<Integer,PNode> fineGraphNodes = new HashMap<>(); //holds clusters neighborhood for fine graph (can be replaced where each node knows its fine neighbors
    public Map<Integer,HashSet<Integer>> neighborhood = new HashMap<>();// initialized in createFineGraph


    public coarsenedGraph getCorsenedGraph(PGraph g) {
		//node,setedges,setnodes,
		List<PEdge> list=new ArrayList<PEdge>(g.coarsenedEdges);
		
		Collections.sort(list, new PEdgeComparatorByWieght()); //sort edges desc
		Collections.reverse(list);
		
		for (PEdge pEdge : list) 
		{
			int s =  pEdge.source;
			int t =  pEdge.target;
			if(!g.coarsenedGraphmatching.get(s) && !g.coarsenedGraphmatching.get(t))
			{
				//both will be coarsened (merged) to the source node
				
				//node containment is updated first NODE={node1, node 2, node 3,...}
				HashSet<Integer> containedNodes =  g.coarsenedNodeContainedNodes.get(t);
				g.coarsenedNodeContainedNodes.get(s).addAll(containedNodes); //nerge contained nodes into source
				g.coarsenedNodeContainedNodes.remove(t); // remove the target
				System.out.println(g.coarsenedNodeContainedNodes.values());
				
				//node weights are updated is updated first NODE={node1, node 2, node 3,...}
				int newWeight = g.coarsenedNodeWeights.get(s)+g.coarsenedNodeWeights.get(t); //calc. new weight
				System.out.println(g.coarsenedNodeWeights.get(s));
				g.coarsenedNodeWeights.put(s, newWeight); //update source node as coarsened
				g.coarsenedNodeContainedNodes.remove(t); //remove target node as it is merged into source
				g.coarsenedNodeWeights.remove(t);
				System.out.println(g.coarsenedNodeWeights.values());

				
				// mark both as matched
				g.coarsenedGraphmatching.put(s, true);
				g.coarsenedGraphmatching.put(t, true);
				
				//update the neighborhood in the coarsened neighboring map
				
				Set<Integer> sourceNieghbors =  g.coarsenedNeighborhood.get(s);
				Set<Integer> targetNieghbors =  g.coarsenedNeighborhood.get(t);
				sourceNieghbors.remove(t);
				targetNieghbors.remove(s);
				
				Set<PEdge> sEdges = g.coarsenedNodeEdges.get(s); // get source edges
				Set<PEdge> tEdges = g.coarsenedNodeEdges.get(t); // get target edges
				
				Set<PEdge> removedEdges = new HashSet<>();
				Set<PEdge> addedEdges = new HashSet<>();
				Map<PEdge,Integer> updateCommon= new HashMap<>();

				for (PEdge et : tEdges) // for each edge in the target node to be merged into the source node
				{
					if(sEdges.contains(et))//in between edge
					{
						removedEdges.add(et);

					}
					else // not common edge
					{

						for (PEdge es : sEdges) {
							
							//	get target-commonnode weight
							int tWeight = et.weight;
							
							int commonNode;
							//check if there is a common node betwen source and target edges but not the target itself t = 4, s =3  [2]---[4]----[3]
							if(tEdges.contains(es))//in between edge
							{
								removedEdges.add(es);

							}
							else
							if((commonNode = et.hasCommonNode(es)) != -1 && commonNode != t) 
							{
								//update source-commonnode weight
								updateCommon.put(es, es.weight+tWeight);
								//remove the target-commonnode edge (rpresents double edge for the coarsened node)
								removedEdges.add(et);
							} 
							else // edge to alien node
							{
								addedEdges.add(new PEdge(s, et.target, tWeight)); // add edge to source between it and the alien node
								removedEdges.add(et);
								addedEdges.add(new PEdge(et.target, s, tWeight)); // add to the alien neighborhood the source

							}
						}
					}
				}
				for (PEdge e : removedEdges) {
					g.coarsenedNodeEdges.get(e.source).remove(e);
					g.coarsenedNodeEdges.get(e.target).remove(e);
					g.coarsenedNeighborhood.get(e.source).remove(e.target);
					g.coarsenedNeighborhood.get(e.target).remove(e.source);

				}
				
				for (PEdge e : addedEdges) {
					g.coarsenedNodeEdges.get(e.source).add(e);
					g.coarsenedNodeEdges.get(e.target).add(e);
					g.coarsenedNeighborhood.get(e.source).add(e.target);
					g.coarsenedNeighborhood.get(e.target).add(e.source);

				}
				for (PEdge e : updateCommon.keySet()) {
					System.out.println( updateCommon.get(e));
					int ss = e.source;
					int tt = e.target;
					e.weight = updateCommon.get(e);
					g.coarsenedNodeEdges.get(ss).add(e);
					g.coarsenedNodeEdges.get(tt).add(e);
					//update the edge weights
					/*for (PEdge ssEdge : g.coarsenedNodeEdges.get(ss)) {
						if(ssEdge.equals(e))
							ssEdge.weight = updateCommon.get(e);
						System.out.println(ssEdge.weight);
					}
					
					for (PEdge ttEdge : g.coarsenedNodeEdges.get(tt)) {
						if(ttEdge.equals(e))
							ttEdge.weight = updateCommon.get(e);
					}*/
				}
				
				g.coarsenedNodeEdges.remove(t); // remove the merged node into the other
				g.coarsenedNodeEdges.remove(t);
				
				for (Integer  nodeId : g.coarsenedNodeEdges.keySet()) {
					for (PEdge e : g.coarsenedNodeEdges.get(nodeId)) {
						System.out.println(nodeId+":"+e.source+":"+e.target+":"+e.weight);
					}
				}
				
/*				for (PEdge et : tEdges) // for each edge in the target node to be merged into the source node
				{
					if(sEdges.contains(et))//in between edge
					{
						g.coarsenedNodeEdges.get(s).remove(et); //remove it from source
						g.coarsenedNodeEdges.get(t).remove(et); //remove it from target
					}
					else // not common edge
					{

						for (PEdge es : sEdges) {
							
							//	get target-commonnode weight
							int tWeight = et.weight;

							if(et.target == es.target)//common node
							{
								//update source-commonnode weight
								es.weight += tWeight;
							} 
							else // edge to alien node
							{
								g.coarsenedNodeEdges.get(s).add(new PEdge(s, et.target, tWeight)); // add edge to source between it and the alien node
								g.coarsenedNodeEdges.get(et.target).remove(et); //remove the target from the alien node neighborhood
								g.coarsenedNodeEdges.get(et.target).add(new PEdge(et.target, s, tWeight)); // add to the alien neighborhood the source

							}
						}
					}
				}*/
			
			}
			
		}
		return null;
	}
    
    private void displayMap(Map<Integer,Set<PEdge>> m)
    {
    	for (Object key : m.keySet()) {
			System.out.println(key+":"+m.get(key));
		}
    }
    /**
     * This method initializes the graph structures based on the given group of clusters
     * @param clusters
     */
    public void initializeGraph(Map<Integer, Cluster> clusters)
    {
    	allClusters = clusters;
    	//create fine graph (nodes,edges)
    	createFineGraph(clusters);
    	
    	
    	for (Integer node :fineGraphNodeEdges.keySet()) {
    		//record the neighborhood
    		HashSet<Integer> neighbors = (HashSet<Integer>) getNeighbors(node);
    		neighborhood.put(node, neighbors);
    		coarsenedNeighborhood.put(node, neighbors);
    		
    		//initialize coarsened node with teir initial contained node ids --> clusters 
    		
    		HashSet<Integer> containedNode = coarsenedNodeContainedNodes.get(node);
    		if(containedNode==null)
    		{
    			containedNode =  new HashSet<>();
    			containedNode.add(node);
    			coarsenedNodeContainedNodes.put(node,containedNode );
    		}
    		else
    			coarsenedNodeContainedNodes.get(node).add(node);
    		
    		//initialize matching flags
    		coarsenedGraphmatching.put(node, false);
    	}
    	
    	
    }
    
    /**
     * It creates the fine graph by mappings the nodes and the edges between the clusters.
     * In addition, it calculates the weights for the nodes and edges
     * 
     * @param clusters
     */
    public void createFineGraph(Map<Integer, Cluster> clusters)
    {
    	Object[] c =  clusters.values().toArray();
		int edgegWeight=0;

    	for(int s=0;s<c.length;s++)
    	{
			Cluster source = clusters.get(s);
    		for(int t=s+1;t<c.length;t++)
    		{
    			Cluster target = clusters.get(t);
    			
    			if((edgegWeight = getSimilarity(source, target))!=0 ) // similarity -> edge
    			{
    					PEdge sEdge = new PEdge(source.id,target.id,edgegWeight);
    					PEdge csEdge = new PEdge(source.id,target.id,edgegWeight); // create a deep copy for the coarsened
    					coarsenedEdges.add(sEdge); // added once as interchanging the source and target won't affect due to equal overriding
    					//ClusterEdge clusteredgeT = new ClusterEdge(target,source,edgegWeight);

    					if(!fineGraphNodeEdges.containsKey(source.id))// create new node and add the edge to it
    					{
    						PNode node = new PNode(source.id, source.getWeight()); // create new node;
    						fineGraphNodes.put(source.id, node); // add it to list of nodes in graph
    						coarsenedNodeWeights.put(source.id, source.getWeight()); //add it to the node-weight coarsened structure
    						
    						//create set of edges for the node and add to fine node<->edges
    						HashSet<PEdge> sEdges = new HashSet<PEdge>();
    						sEdges.add(sEdge);
    						fineGraphNodeEdges.put(source.id,sEdges);
    						
    						//create set of edges for the node and add to coarsened node<->edges
    						HashSet<PEdge> csEdges = new HashSet<PEdge>();
    						csEdges.add(csEdge);
    						
    						coarsenedNodeEdges .put(source.id, csEdges);
    					}
    					else // already source node exists
    					{
    						fineGraphNodeEdges.get(source.id).add(sEdge); // add its out-edge to fine graph
    						coarsenedNodeEdges.get(source.id).add(csEdge); // add its out-edge to coarsened graph

    					}
    					
    					PEdge tEdge= new PEdge(target.id,source.id,edgegWeight);
    					PEdge ctEdge= new PEdge(target.id,source.id,edgegWeight);

    					
    					if(!fineGraphNodes.containsKey(target.id))// new to add
    					{
    						PNode node = new PNode(target.id, target.getWeight()); // create new node
    						
    						fineGraphNodes.put(target.id, node); // add it to list of nodes in grapg
    						coarsenedNodeWeights.put(target.id, target.getWeight()); //add it to the node-weight coarsened structure
    						
    						//create set of edges for the node and add to fine node<->edges
    						HashSet<PEdge> tEdges = new HashSet<PEdge>();
    						tEdges.add(tEdge);
    						fineGraphNodeEdges.put(target.id,tEdges);  
    						
    						HashSet<PEdge> ctEdges = new HashSet<PEdge>(); // add deep copy to the coarsened structure'
    						ctEdges.add(ctEdge);
    						
    						coarsenedNodeEdges .put(target.id, ctEdges);
    						}
    					else
    					{
    						fineGraphNodeEdges.get(target.id).add(tEdge);
    						coarsenedNodeEdges.get(target.id).add(ctEdge); // add its out-edge to coarsened graph

    					}
   				
    			}
    			
    		}
    	}
    }
    

   
    
    
    /**
     * It returns set of neighbor nodes of a specific given node
     * @param nodeId the node id to get its neighbors
     * @return
     */
    public Set<Integer> getNeighbors(int nodeId)
    {
    	Set<Integer> neighbors = new HashSet<>();
    	Set<PEdge> edges = fineGraphNodeEdges.get(nodeId);
    	for (PEdge pEdge : edges) {
    		neighbors.add(pEdge.target);
		}
    	return neighbors;
    }
    
    /**
     * Calculates the similarities between two clusters. 
     * i.e. total size of the common Index Items between the two cluster
     * It represents the weight of the edge between them
     * @param c1 the first cluster
     * @param c2 the second cluster
     * @return similarity
     */
    private int getSimilarity(Cluster c1, Cluster c2) {
        int sim = 0;
        TreeSet<Node> n1 = new TreeSet<>(c1.nodes);
        TreeSet<Node> n2 = new TreeSet<>(c2.nodes);
        n1.retainAll(n2);
        for (Node n : n1) {
            sim = sim + n.getWeight();
        }
        return sim;
    }
    /**
     * it serializes the fine graph basic information into files
     */
    private void serializeFineGraph()
    {
        try{
        	// Serialize data object to a file
        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("finegraphNodes.ser"));
        	out.writeObject(fineGraphNodes);
        	out.close();
        	out = new ObjectOutputStream(new FileOutputStream("finegraphNodeEdges.ser"));
        	out.writeObject(fineGraphNodeEdges);
        	out.close();
        	
        	
        }
        catch(IOException e){System.out.println(e.getMessage());}
    }
    /**
     * it reads the fine graph serialized before
     */
    private void deSerializeFineGraph()
    {
    	try{ 
			  FileInputStream inputFileStream = new FileInputStream("finegraphNodes.ser");
		      ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
		      fineGraphNodes = (Map<Integer, PNode>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close(); 
		      
		      inputFileStream = new FileInputStream("finegraphNodeEdges.ser");
		      objectInputStream = new ObjectInputStream(inputFileStream);
		      fineGraphNodeEdges = (Map<Integer, Set<PEdge>>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close();
			}
		catch(IOException e){System.out.println(e.getMessage());} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void displayFineGraph()
    {
    	StringBuilder nodeInfo=new StringBuilder();;
    	for (Integer node : fineGraphNodes.keySet()) {
			Set<PEdge> edges = fineGraphNodeEdges.get(node);
			nodeInfo.delete(0, nodeInfo.length());
			nodeInfo.append(node+"|"+fineGraphNodes.get(node).weight+" : ");
			for (PEdge pEdge : edges) {
				nodeInfo.append(pEdge.source+" --> "+pEdge.target+"|"+pEdge.weight+"," );
			}
			System.out.println(nodeInfo);
		}
    }
}
