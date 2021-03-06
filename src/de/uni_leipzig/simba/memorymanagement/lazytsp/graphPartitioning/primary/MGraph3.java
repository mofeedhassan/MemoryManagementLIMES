package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners.HEV;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners.HEVSortedNodes;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public class MGraph3 {

	private static final float reduction_factor = 0.5f;
	//fine graph structures
	public Map<Integer,Integer> nodeWeights = new HashMap<>();
	public Map<Integer,Map<Integer,Integer>> edgesWeights = new HashMap<>();
	public Map<Integer,Integer> isolatedNodeWeights = new HashMap<>();

	//coarsened graph
	
	public Map<Integer,Integer> coarsenedNodeWeights = null;
	public Map<Integer,Map<Integer,Integer>> coarsenedEdgesWeights = null;
	public Map<Integer,Set<Integer>> coarsenedNodesContainedNodes = null;
	public Map<Integer,Integer> isolatedCoarsenedNodeWeights = null;

	
	//coarsening level counter 
	private static int level = 0 ; //fine graph
	public MGraph3()	{}
	
	 /*public coarsenedGraph getCorsenedGraphEdgeOrder() {
		 
		 int merge_count= (int)(reduction_factor * nodeWeights.size());
		 
 
		 while(merge_count > 0)
		 {
			 level++;
			 TreeSet<PEdge> edesSet = new TreeSet<>();
			 for (Integer source : coarsenedEdgesWeights.keySet()) {
				 for (Integer target : coarsenedEdgesWeights.get(source).keySet()) {
					 edesSet.add(new PEdge(source, target, coarsenedEdgesWeights.get(source).get(target)));
				}
				 
			}
			 
			 initializeMatching();		 
			 
			 for (PEdge pEdge : edesSet.descendingSet()) {
					int source = pEdge.source;
					int target = pEdge.target;
					
					if((coarsenedNodeWeights.containsKey(source) && coarsenedNodeWeights.containsKey(target)) && (!matching.get(source) && !matching.get(target)))
					{
						//merge the nodes of target into nodes of source
						Set<Integer> targetContainedNodes = coarsenedNodesContainedNodes.get(target);
						coarsenedNodesContainedNodes.get(source).addAll(targetContainedNodes);
						coarsenedNodesContainedNodes.remove(target);
						
						//node weights are updated is updated first NODE={node1, node 2, node 3,...}
						int newWeight = coarsenedNodeWeights.get(source)+coarsenedNodeWeights.get(target); //calc. new weight
						coarsenedNodeWeights.put(source, newWeight); //update source node as coarsened
						coarsenedNodeWeights.remove(target); //remove target node as it is merged into source
						
						//mark both as matched
						matching.put(source, true);
						matching.put(target, true);
						
						//updae edges
						Map<Integer,Integer> sourceNieghbors = coarsenedEdgesWeights.get(source);
						Map<Integer,Integer> targetNieghbors = coarsenedEdgesWeights.get(target);
						
						//remove common edge
						sourceNieghbors.remove(target);
						targetNieghbors.remove(source);
						
						
						for (Integer targetNieghbor : targetNieghbors.keySet()) {
							int newEdgeWeight =-1;
							if(sourceNieghbors.keySet().contains(targetNieghbor)) //target Neighbor is common node
							{
								newEdgeWeight = sourceNieghbors.get(targetNieghbor) + targetNieghbors.get(targetNieghbor);
								sourceNieghbors.put(targetNieghbor, newEdgeWeight);
							}
							else //not common node
							{
								newEdgeWeight = targetNieghbors.get(targetNieghbor);
								sourceNieghbors.put(targetNieghbor, newEdgeWeight);
							}
							coarsenedEdgesWeights.get(targetNieghbor).put(source,newEdgeWeight); // add the source as a neighbor to the target's neighbors
							coarsenedEdgesWeights.get(targetNieghbor).remove(target); // remove the target itself from its neighbors'
						}
						coarsenedEdgesWeights.remove(target);
					}
					
			 }
			 
			 if(coarsenedNodeWeights.size() <= (int)(reduction_factor * nodeWeights.size()))//considering the isolated nodes
			 {
				 //sort the coarsened nodes in decending order
				 List<Entry<Integer, Integer>> coarsenedNodesSortedList = entriesSortedByValuesDesc(coarsenedNodeWeights);
				 List<Entry<Integer, Integer>> isolatedSortedList = entriesSortedByValuesDesc(isolatedCoarsenedNodeWeights);
				 
				 // get node id of the max weight and its weight
				 int compareToCoarsenedNodeId = coarsenedNodesSortedList.get(0).getKey();
				 int compareToCoarsenedNodeWeight = coarsenedNodesSortedList.get(0).getValue();
				 
				 int isolatedIndex=0;
				 int isolatedNodeId=-1;
				 
				 //iterate over coarsened nodes except the max one
				 for(int i=1; i< coarsenedNodesSortedList.size() ;i++)
				 {
					 // get the compared coarsened nodes starting with the second max one
					 int nodeId = coarsenedNodesSortedList.get(i).getKey();
					 int nodeWeight = coarsenedNodesSortedList.get(i).getValue();
					 
					 //add isolated node as long they get close to the max coarsened node size
					 while((compareToCoarsenedNodeWeight - coarsenedNodeWeights.get(nodeId)) > 5 && isolatedSortedList.size() > 0) // 5 is a margin
					 {
						 //get isolated node key
						 isolatedNodeId = isolatedSortedList.get(0).getKey();
						 
						 //add isolated Node to the magnified coarsened node
						 coarsenedNodesContainedNodes.get(nodeId).add(isolatedNodeId);
						 
						 //remove the isolated node from the coarsened nodes map
						 coarsenedNodesContainedNodes.remove(isolatedNodeId);
						 
						 //update the weight
						 int newWeight = coarsenedNodeWeights.get(nodeId)+isolatedSortedList.get(0).getValue();
						 coarsenedNodeWeights.put(nodeId,newWeight );
						 
						 //add an edge with 0 weight to the new node (isolated)
						 //coarsenedEdgesWeights.get(nodeId).put(isolatedNodeId, 0);
						 edgesWeights.get(nodeId).put(isolatedNodeId, 0);

						 
						 //remove the isolated node
						 isolatedSortedList.remove(0);
						 
					 }
				 }
				 // add the rest of isolated nodes to the smallest one in case something is left
				 int smallestCoarsenedNodeId = coarsenedNodesSortedList.get(coarsenedNodesSortedList.size()-1).getKey();// the smallest coarsened node
				 
				 for (Entry<Integer, Integer> isolatedNodeInfo : isolatedSortedList) {
					//get isolated node key
					 isolatedNodeId = isolatedSortedList.get(isolatedIndex).getKey();
					 
					 //add isolated Node to the magnified coarsened node
					 coarsenedNodesContainedNodes.get(smallestCoarsenedNodeId).add(isolatedNodeId);
					 
					 //remove the isolated node from the coarsened nodes map
					 coarsenedNodesContainedNodes.remove(isolatedNodeId);
					 
					 //update the weight
					 int newWeight = coarsenedNodeWeights.get(smallestCoarsenedNodeId)+isolatedSortedList.get(isolatedIndex).getValue();
					 coarsenedNodeWeights.put(smallestCoarsenedNodeId,newWeight );
					 //coarsenedNodeWeights.put(smallestCoarsenedNodeId,newWeight );
					 
					 //add an edge with 0 weight to the new node (isolated)
					 coarsenedEdgesWeights.get(smallestCoarsenedNodeId).put(isolatedNodeId, 0);
					 
				}
				
	
				 break;
			 }
			 
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }
			return null;
		}*/
	 
	 
	 
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
	    				//add nodes to fine graph
	    				nodeWeights.put(source.id, source.getWeight());
	    				nodeWeights.put(target.id, target.getWeight());
	    				
	    				//add edges to fine graph
	    				if(edgesWeights.containsKey(source.id))
	    					edgesWeights.get(source.id).put(target.id, edgegWeight);
	    				else
	    				{
	    					Map<Integer,Integer> edgeTargetWeight = new HashMap<>();
		    				edgeTargetWeight.put(target.id, edgegWeight);
		    				edgesWeights.put(source.id, edgeTargetWeight);

	    				}

	    				if(edgesWeights.containsKey(target.id))
	    					edgesWeights.get(target.id).put(source.id, edgegWeight);
	    				else
	    				{
	    					Map<Integer,Integer> edgeTargetWeight = new HashMap<>();
		    				edgeTargetWeight.put(source.id, edgegWeight);
		    				edgesWeights.put(target.id, edgeTargetWeight);

	    				}

	    			}
	    			
	    		}
	    		if (!nodeWeights.containsKey(source.id)) //has no edge for next nodes and no edge to previous nodes => isolated
    			{
	    			isolatedNodeWeights.put(source.id, source.getWeight());
    				
    				Map<Integer, Integer> isolatedEdge = new HashMap<>(); //is it necessary
    				isolatedEdge.put(-1, edgegWeight);
					edgesWeights.put(source.id,isolatedEdge);
    			}
	    	}
	    }
	    
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
	        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("MGraphNodesWeights.ser"));
	        	out.writeObject(nodeWeights);
	        	out.close();
	        	out = new ObjectOutputStream(new FileOutputStream("MGraphEdgesWeights.ser"));
	        	out.writeObject(edgesWeights);
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
				  FileInputStream inputFileStream = new FileInputStream("MGraphNodesWeights.ser");
			      ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
			      nodeWeights = (Map<Integer, Integer>)objectInputStream.readObject();
			      objectInputStream.close();
			      inputFileStream.close(); 
			      
			      inputFileStream = new FileInputStream("MGraphEdgesWeights.ser");
			      objectInputStream = new ObjectInputStream(inputFileStream);
			      edgesWeights = (Map<Integer, Map<Integer, Integer>>)objectInputStream.readObject();
			      objectInputStream.close();
			      inputFileStream.close();
				}
			catch(IOException e){System.out.println(e.getMessage());} 
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    public void displayGraph(Map<Integer,Integer> nodesWeights , Map<Integer,Map<Integer,Integer>> edgesWeights )
	    {
	    	StringBuilder nodeInfo=new StringBuilder();
	    	for (Integer node : nodesWeights.keySet()) {
	    		Map<Integer,Integer> edges = edgesWeights.get(node);
	    		
				nodeInfo=new StringBuilder(); //new string
				nodeInfo.append(node+"|"+nodesWeights.get(node)+" : "); // add node number and its weight
				if(edges!=null)
				{
					for (Integer edge : edges.keySet()) {
					nodeInfo.append(node+" --> "+edge+"|"+edges.get(edge)+"," );
					}
				}
				else//isolated nodes
					nodeInfo.append(node+" --> -1|"+0+"," ); //not used any more as next loop do it instead
					
				
				System.out.println(nodeInfo);
			}
	    	for (Integer node : isolatedNodeWeights.keySet()) {
	    		Map<Integer,Integer> edges = edgesWeights.get(node);
	    		
				nodeInfo=new StringBuilder(); //new string
				nodeInfo.append(node+"|"+isolatedNodeWeights.get(node)+" : "); // add node number and its weight
				if(edges!=null)
				{
					for (Integer edge : edges.keySet()) {
					nodeInfo.append(node+" --> "+edge+"|"+edges.get(edge)+"," );
					}
				}
				else//isolated nodes
					nodeInfo.append(node+" --> -1|"+0+"," );
					
				
				System.out.println(nodeInfo);
			}
	    	System.out.println();
	    }
}
