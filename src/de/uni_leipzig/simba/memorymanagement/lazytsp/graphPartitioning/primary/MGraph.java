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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.tdb.store.Hash;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public class MGraph {
	private static final float reduction_factor = 0.5f;
	//fine graph structures
	public Map<Integer,Integer> nodeWeights = new HashMap<>();
	public Map<Integer,Map<Integer,Integer>> edgesWeights = new HashMap<>();
	
	//coarsened graph
	
	public Map<Integer,Integer> coarsenedNodeWeights = new HashMap<>();
	public Map<Integer,Map<Integer,Integer>> coarsenedEdgesWeights = new HashMap<>();
	public Map<Integer,Set<Integer>> coarsenedNodesContainedNodes = new HashMap<>();
	
	//matching map
	public Map<Integer,Boolean> matching = new HashMap<>();
	
	//coarsening level counter 
	private static int level = 0 ; //fine graph
	public MGraph()	{}
	
	 public coarsenedGraph getCorsenedGraphEdgeOrder() {
		 
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
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }
			return null;
		}
	 
	 
	 public coarsenedGraph getCorsenedGraphNodeOrder() {
		 
		 int merge_count= (int)(reduction_factor * nodeWeights.size());
		 
 
		 while(merge_count > 0)
		 {
			 level++;
			 List<Entry<Integer, Integer>> nodesSortedList = entriesSortedByValues(coarsenedNodeWeights);
			 
			 initializeMatching();		 
			 
			 for (Entry<Integer, Integer> node : nodesSortedList) {
					
				 if(!matching.get(node.getKey())) //not matched yet with a  previous node
				 {
					 int nodeId = node.getKey();
					// int nodeWeight = node.getValue();
					 
					 Map<Integer, Integer> neighborsEdgesWeights = coarsenedEdgesWeights.get(nodeId);
					 int maxNeighbor = -1;
					 int maxedgeWeight=-1;
					 
					 for (Integer neighbor : neighborsEdgesWeights.keySet()) {
						if(neighborsEdgesWeights.get(neighbor) > maxedgeWeight && !matching.get(neighbor)) //neighbor is not matched yet with a  previous node
						{
							maxNeighbor = neighbor;
							maxedgeWeight = neighborsEdgesWeights.get(neighbor);
						}
					}
					 //maxNeighbor  source
					 //nodeId target
					 if(maxNeighbor != -1) //  node to merge is found always merge o the neighbor as it is definitely larger than the node if it is available (match=false)
					 {
						//merge the nodes of target into nodes of source
							Set<Integer> targetContainedNodes = coarsenedNodesContainedNodes.get(nodeId);
							coarsenedNodesContainedNodes.get(maxNeighbor).addAll(targetContainedNodes);
							coarsenedNodesContainedNodes.remove(nodeId);
							
							//node weights are updated is updated first NODE={node1, node 2, node 3,...}
							int newWeight = coarsenedNodeWeights.get(maxNeighbor) + coarsenedNodeWeights.get(nodeId); //calc. new weight
							coarsenedNodeWeights.put(maxNeighbor, newWeight); //update source node as coarsened
							coarsenedNodeWeights.remove(nodeId); //remove target node as it is merged into source
							
							//mark both as matched
							matching.put(maxNeighbor, true);
							matching.put(nodeId, true);
							
							//updae edges
							Map<Integer,Integer> sourceNieghbors = coarsenedEdgesWeights.get(maxNeighbor);
							Map<Integer,Integer> targetNieghbors = coarsenedEdgesWeights.get(nodeId);
							
							//remove common edge
							sourceNieghbors.remove(nodeId);
							targetNieghbors.remove(maxNeighbor);
							
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
								coarsenedEdgesWeights.get(targetNieghbor).put(maxNeighbor,newEdgeWeight); // add the source as a neighbor to the target's neighbors
								coarsenedEdgesWeights.get(targetNieghbor).remove(nodeId); // remove the target itself from its neighbors'
							}
							
							coarsenedEdgesWeights.remove(nodeId);

					 }
				 }
			 }
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }
			return null;
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
	    				//add nodes to fine graph
	    				nodeWeights.put(source.id, source.getWeight());
	    				nodeWeights.put(target.id, target.getWeight());
	    				
	    				//add nodes to coarsened graph
	    				coarsenedNodeWeights.put(source.id, source.getWeight());
	    				coarsenedNodeWeights.put(target.id, target.getWeight());
	    				
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

	    				
	    				//add nodes to coarsened graph
	    				
	    				if(coarsenedEdgesWeights.containsKey(source.id))
	    					coarsenedEdgesWeights.get(source.id).put(target.id, edgegWeight);
	    				else
	    				{
	    					Map<Integer,Integer> edgeTargetWeight = new HashMap<>();
		    				edgeTargetWeight.put(target.id, edgegWeight);
		    				coarsenedEdgesWeights.put(source.id, edgeTargetWeight);

	    				}

	    				
	    				if(coarsenedEdgesWeights.containsKey(target.id))
	    					coarsenedEdgesWeights.get(target.id).put(source.id, edgegWeight);
	    				else
	    				{
	    					Map<Integer,Integer> edgeTargetWeight = new HashMap<>();
		    				edgeTargetWeight.put(source.id, edgegWeight);
		    				coarsenedEdgesWeights.put(target.id, edgeTargetWeight);

	    				}
	    				
	    				matching.put(source.id, false);
	    				matching.put(target.id, false);

	    			}
	    			
	    		}
	    		Set<Integer> containedNode =  new HashSet<>();
	    		containedNode.add(source.id);
	    		
	    		coarsenedNodesContainedNodes.put(source.id, containedNode);
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
	   private void initializeMatching()
	   { //works ony with the nodes exist in the coarsening map
		   matching.clear();
		   for (Integer match : coarsenedNodeWeights.keySet()) {
				matching.put(match, false);
			}
	   }
	   
	   static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) 
	   {

		   List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		   Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() 
		   {
	           @Override
	           public int compare(Entry<K,V> e1, Entry<K,V> e2) 
	           {
	               return e1.getValue().compareTo(e2.getValue());
	           }
		   }
				   );

		   return sortedEntries;
	   }
	   
	   
	   private void serializecCoarsenedGraph(int level)
	    {
	        try{
	        	// Serialize data object to a file
	        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("MGraphNodesWeights"+level+".ser"));
	        	out.writeObject(nodeWeights);
	        	out.close();
	        	out = new ObjectOutputStream(new FileOutputStream("MGraphEdgesWeights"+level+".ser"));
	        	out.writeObject(edgesWeights);
	        	out.close();
	        	
	        	
	        }
	        catch(IOException e){System.out.println(e.getMessage());}
	    }
	   
	   private void deSerializecCoarsenedGraph(Map<Integer, Integer> nodeWeights, Map<Integer, Map<Integer, Integer>> edgesWeights ,int level)
	    {
	    	try{ 
				  FileInputStream inputFileStream = new FileInputStream("MGraphNodesWeights"+level+".ser");
			      ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
			      nodeWeights = (Map<Integer, Integer>)objectInputStream.readObject();
			      objectInputStream.close();
			      inputFileStream.close(); 
			      
			      inputFileStream = new FileInputStream("MGraphEdgesWeights"+level+".ser");
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
				for (Integer edge : edges.keySet()) {
					nodeInfo.append(node+" --> "+edge+"|"+edges.get(edge)+"," );
				}
				System.out.println(nodeInfo);
			}
	    	System.out.println();
	    }
}
