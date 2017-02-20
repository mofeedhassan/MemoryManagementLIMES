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
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public class MGraph2 {
	private static final float reduction_factor = 0.5f;
	//fine graph structures
	public Map<Integer,Integer> nodeWeights = new HashMap<>();
	public Map<Integer,Map<Integer,Integer>> edgesWeights = new HashMap<>();
	public Map<Integer,Integer> isolatedNodeWeights = new HashMap<>();

	//coarsened graph
	
	public Map<Integer,Integer> coarsenedNodeWeights = new HashMap<>();
	public Map<Integer,Map<Integer,Integer>> coarsenedEdgesWeights = new HashMap<>();
	public Map<Integer,Set<Integer>> coarsenedNodesContainedNodes = new HashMap<>();
	public Map<Integer,Integer> isolatedCoarsenedNodeWeights = new HashMap<>();


	
	//matching map
	public Map<Integer,Boolean> matching = new HashMap<>();
	
	//coarsening level counter 
	private static int level = 0 ; //fine graph
	public MGraph2()	{}
	
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
		}
	 
	 
	 public coarsenedGraph getCorsenedGraphNodeOrder() {
		 
		 int merge_count= (int)(reduction_factor * nodeWeights.size());
		 int reductedSize = (int)(reduction_factor * nodeWeights.size());
 
		 while(merge_count > 0 && !coarsenedEdgesWeights.isEmpty())
		 {
			 
			 level++;
			 //sort connected coarsened nodes by their weights
			 List<Entry<Integer, Integer>> nodesSortedList = entriesSortedByValues(coarsenedNodeWeights);
			 
			 initializeMatching();		 
			 
			 for (Entry<Integer, Integer> node : nodesSortedList) {
					
				 if(!matching.get(node.getKey())) //not matched yet with a  previous node
				 {
					 int nodeId = node.getKey();
					 //get its neighbor nodes
					 Map<Integer, Integer> neighborsEdgesWeights = coarsenedEdgesWeights.get(nodeId);
					 if(neighborsEdgesWeights.containsKey(-1)) //isolated node
						 continue; //skip it
					 int maxNeighbor = -1;
					 int maxedgeWeight=-1;
					 //find the neighbor connected by the edge with max. weight
					 for (Integer neighbor : neighborsEdgesWeights.keySet()) {
						if(neighborsEdgesWeights.get(neighbor) > maxedgeWeight && !matching.get(neighbor)) //neighbor is not matched yet with a  previous node
						{
							maxNeighbor = neighbor;
							maxedgeWeight = neighborsEdgesWeights.get(neighbor);
						}
					}
					 //maxNeighbor  source
					 //nodeId target
					 if(maxNeighbor != -1) //  node to merge is found always merge to the neighbor as it is definitely larger than the node if it is available (match=false)
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
/*					 else  if(neighborsEdgesWeights.size()==0)// has no neigbor - isolated node
					 {
						 int nextNodeInSizeIndex = nodesSortedList.indexOf(node);
						 int nextNodeInSizeId = nodesSortedList.get(nextNodeInSizeIndex+1).getKey();
						 
						//merge the nodes of target into nodes of source
							Set<Integer> targetContainedNodes = coarsenedNodesContainedNodes.get(nodeId);
							coarsenedNodesContainedNodes.get(nextNodeInSizeId).addAll(targetContainedNodes);
							coarsenedNodesContainedNodes.remove(nodeId);
							
							//for the isolated node add in th fine graph a 0 weight edge
							edgesWeights.get(nextNodeInSizeId).put(nodeId, 0);
							
							//node weights are updated is updated first NODE={node1, node 2, node 3,...}
							int newWeight = coarsenedNodeWeights.get(nextNodeInSizeId) + coarsenedNodeWeights.get(nodeId); //calc. new weight
							coarsenedNodeWeights.put(nextNodeInSizeId, newWeight); //update source node as coarsened
							coarsenedNodeWeights.remove(nodeId); //remove target node as it is merged into source
							
							//mark both as matched
							matching.put(nextNodeInSizeId, true);
							matching.put(nodeId, true);
						 
					 }*/
				 }
			 }
			 merge_count--;
			 serializecCoarsenedGraph(level);
		 }//while
		 
		// merge count is done but not enough to reduce the size to the target size due to existence of isolated nodes, then merge them spcificallyy
		 if(reductedSize < coarsenedNodeWeights.size())
		 {
			 Map<Integer,Integer> sortedIsolatedNodesWeights = new HashMap<>();
			 for (Integer nodeId : coarsenedEdgesWeights.keySet()) {
				if(coarsenedEdgesWeights.get(nodeId).containsKey(-1))//isolated node
					sortedIsolatedNodesWeights.put(nodeId, coarsenedNodeWeights.get(nodeId));
			}
			 
			 List<Entry<Integer, Integer>> nodesSortedList = entriesSortedByValuesDesc(coarsenedNodeWeights);
			 List<Entry<Integer, Integer>> nodesIsolatedSortedList = entriesSortedByValues(sortedIsolatedNodesWeights);
			 int  maxWeight = nodesSortedList.get(0).getValue();
			 int isolatedNodeIndex=0;
			 for (Entry<Integer, Integer> coarsenedNode : nodesSortedList) {
				 int coarsenedNodeId = coarsenedNode.getKey();
				while(coarsenedNodeWeights.get(coarsenedNode.getKey()) < (maxWeight-5) && isolatedNodeIndex < nodesIsolatedSortedList.size())
				{
					int id = nodesIsolatedSortedList.get(isolatedNodeIndex).getKey();
					int weight = nodesIsolatedSortedList.get(isolatedNodeIndex).getValue();
					
					coarsenedNodesContainedNodes.get(coarsenedNodeId).add(id);//add its id to the containednode
					coarsenedNodeWeights.put(coarsenedNodeId, coarsenedNodeWeights.get(coarsenedNodeId)+weight);//update the weight
					
					//matching.put(id, true);
					
					isolatedNodeIndex++;
				}
				if(merge_count <= 0)
					break;
			}
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
	    		if (!nodeWeights.containsKey(source.id)) //has no edge for next nodes and no edge to previous nodes => isolated
    			{
    				nodeWeights.put(source.id, source.getWeight());
    				coarsenedNodeWeights.put(source.id, source.getWeight());
    				
    				Map<Integer, Integer> isolatedEdge = new HashMap<>();
    				isolatedEdge.put(-1, edgegWeight);
					edgesWeights.put(source.id,isolatedEdge);
					
					Map<Integer, Integer> isolatedCoarsenedEdge = new HashMap<>();
					isolatedCoarsenedEdge.put(-1, edgegWeight);
					coarsenedEdgesWeights.put(source.id,isolatedCoarsenedEdge);
    				matching.put(source.id, false);

    			}
	    		
	    		
	    		//add node to contained nodes list, initially each one conatins itself
	    		Set<Integer> containedNode =  new HashSet<>();
	    		containedNode.add(source.id);
	    		
	    		coarsenedNodesContainedNodes.put(source.id, containedNode);
	    	}
/*	    	//for disconnected nodes
	    	for(int s=0;s<c.length;s++) 
	    	{
	    		int clusterId=clusters.get(s).id;
	    		if(!nodeWeights.containsKey(clusterId))
	    		{
	    			nodeWeights.put(clusterId, clusters.get(s).getWeight());
	    			coarsenedNodeWeights.put(clusterId, clusters.get(s).getWeight());
	    			isolatedCoarsenedNodeWeights.put(clusterId, clusters.get(s).getWeight());
	    			isolatedNodeWeights.put(clusterId, clusters.get(s).getWeight());

	    		}
	    	}*/
	    	
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
	   
	   
	   static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValuesDesc(Map<K,V> map) 
	   {

		   List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		   Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() 
		   {
	           @Override
	           public int compare(Entry<K,V> e1, Entry<K,V> e2) 
	           {
	               return e2.getValue().compareTo(e1.getValue());
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
	        	out = new ObjectOutputStream(new FileOutputStream("MGraphIsolatedNodeWeights"+level+".ser"));
	        	out.writeObject(isolatedNodeWeights);
	        	out.close();
	        	
	        	
	        }
	        catch(IOException e){System.out.println(e.getMessage());}
	    }
	   
	   private void deSerializecCoarsenedGraph(Map<Integer, Integer> nodeWeights, Map<Integer, Map<Integer, Integer>> edgesWeights ,Map<Integer, Integer> isolatedNodeWeights,int level)
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
			      
			      inputFileStream = new FileInputStream("MGraphIsolatedNodeWeights"+level+".ser");
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
