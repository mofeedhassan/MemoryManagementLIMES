package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Edge;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.indexing.IndexItem;

public class coarsenedGraph implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2923842106173007611L;
	Set<coarsenedNode> allNodes;
    Set<CoarsenedEdge> allEdges;
    Map<CoarsenedEdge, Set<coarsenedNode>> edgeNodeMap;
    Map<coarsenedNode, Set<CoarsenedEdge>> nodeEdgeMap;
    Map<String, coarsenedNode> nodeMap;
    
    public Map<Integer,Boolean> matching = new HashMap<>();
    public Map<Integer,Integer> allClusters = new HashMap<>();
    public Map<Integer,HashSet<Integer>> coarsenedNodes = new HashMap<>();
    public Map<Integer,HashSet<Integer>> neighborhood = new HashMap<>();
     
    
    Map<Cluster,Set<ClusterEdge>> fineGraph = new HashMap<>();
    
    public final Map<Cluster,Set<ClusterEdge>> getFineGraph()
    {return fineGraph;}

    public coarsenedGraph() {
        allNodes = new HashSet<>();
        allEdges = new HashSet<>();
        edgeNodeMap = new HashMap<>();
        nodeEdgeMap = new HashMap<>();
        nodeMap = new HashMap<>();
    }
    
    public void createFineGraph(Map<Integer, Cluster> clusters)
    {
/*    	Object[] c =  clusters.values().toArray();
		int edgegWeight=0;

    	for(int s=0;s<c.length;s++)
    		for(int t=0;t<c.length;t++)
    		{
    			Cluster source = clusters.get(s);
    			Cluster target = clusters.get(t);
    			
    			if(source.id != target.id && (edgegWeight = getSimilarity(source, target))!=0 )
    			{
    					ClusterEdge clusteredge = new ClusterEdge(source,target,edgegWeight);
    					if(!fineGraph.containsKey(source))// new to add
        					fineGraph.put(source, new HashSet<ClusterEdge>(){{add(clusteredge);}});
    					else
    						fineGraph.get(source).add(clusteredge);
   				
    			}
    			
    		}*/

    	Object[] c =  clusters.values().toArray();
		int edgegWeight=0;

    	for(int s=0;s<c.length;s++)
    	{
			Cluster source = clusters.get(s);
    		for(int t=s+1;t<c.length;t++)
    		{
    			Cluster target = clusters.get(t);
    			
    			if((edgegWeight = getSimilarity(source, target))!=0 )
    			{
    					ClusterEdge clusteredgeS = new ClusterEdge(source,target,edgegWeight);
    					ClusterEdge clusteredgeT = new ClusterEdge(target,source,edgegWeight);

    					if(!fineGraph.containsKey(source))// new to add
        					fineGraph.put(source, new HashSet<ClusterEdge>(){/**
								 * 
								 */
								private static final long serialVersionUID = -7056225933317896831L;

							{add(clusteredgeS);}});
    					else
    						fineGraph.get(source).add(clusteredgeS);
    					
    					if(!fineGraph.containsKey(target))// new to add
        					fineGraph.put(target, new HashSet<ClusterEdge>(){/**
								 * 
								 */
								private static final long serialVersionUID = -8615080658684193617L;

							{add(clusteredgeT);}});
    					else
    						fineGraph.get(target).add(clusteredgeT);
   				
    			}
    			
    		}
    	}
    	
    	serializeFineGraph();
    }
    private void serializeFineGraph()
    {
        try{
        	// Serialize data object to a file
        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("finegraph.ser"));
        	out.writeObject(fineGraph);
        	out.close();
        }
        catch(IOException e){System.out.println(e.getMessage());}
    }
    
    private void deSerializeFineGraph()
    {
    	try{ 
			FileInputStream inputFileStream = new FileInputStream("finegraph.ser");
		      ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
		      fineGraph = (Map<Cluster, Set<ClusterEdge>>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close(); 
			}
		catch(IOException e){System.out.println(e.getMessage());} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void initializeGraph(Map<Integer, Cluster> clusters)
    {
    	//create fine graph (nodes,edges)
    	createFineGraph(clusters);
    	//foreach cluster
    	for (Cluster c :fineGraph.keySet()) {
    		
    		Set<ClusterEdge> clusterEdges = fineGraph.get(c);// get cluster's edges
    		coarsenedNode cn=new coarsenedNode(c,new HashSet<>()); //create coarsened node with empty edges
    		//iterate over the edges
    		for (ClusterEdge clusterEdge : clusterEdges) {
    			//create coarsened edges
        		CoarsenedEdge ce = new CoarsenedEdge(clusterEdge.source.id, clusterEdge.target.id, clusterEdge.weight);
        		
        		cn.getOuterEdges().add(ce); //add new edge (represents outer edge in fine graph as each node represents a cluster)
        		Set<coarsenedNode> edgeNodes = null;
        		if((edgeNodes = edgeNodeMap.get(ce))== null)
        			edgeNodes = new HashSet<>();
    			edgeNodes.add(cn);

    			edgeNodeMap.put(ce, edgeNodes); // add edge to mapp with empty related nodes
        		allEdges.add(ce);// add to list of cedges
        		
        		HashSet<Integer> neighbors = neighborhood.get(clusterEdge.source.id);
      		  if(neighbors==null)
      			neighbors = new HashSet<>();
    		  neighbors.add(clusterEdge.target.id);
    		  neighborhood.put(clusterEdge.source.id, neighbors);

			}
    		
    		  //System.out.println(cn.Id+":"+cn.outerEdges);
    		  allNodes.add(cn); //add to list of cnodes
    		  nodeEdgeMap.put(cn, cn.getOuterEdges());// not much important as each node knows its edges, rethink about its existence
    		  
    		  matching.put(c.id, false);
    		  HashSet<Integer> carsenedNodesClusters = new HashSet<>();
    		  carsenedNodesClusters.add(c.id);
    		  coarsenedNodes.put(c.id, carsenedNodesClusters);
    		  allClusters.put(c.id, c.getWeight());
		}
    	
    	/*Object[] c =  clusters.values().toArray();
    	for(int s=0; s<c.length; s++)
    		for(int t=0; t<c.length; t++)
    		{
    			Cluster source = clusters.get(s);
    			Cluster target = clusters.get(t);
    			if(source.id != target.id)// not to itself
    			{
        			//new nodes are created only if they are source and the calculated weight is re-used when it is
        			// a target node for another one
        			// the target node inserted before in the nodes list and weight is calculated before, just copy it
        			if(t < s)
        			{
        				
        			}
        			else // need to calculate the target node is not inserted yet
        			{
        				int edgegWeight = getSimilarity(source, target);
        				CoarsenedEdge ce1 = new CoarsenedEdge(source.id, target.id, edgegWeight);
        				//CoarsenedEdge ce2 = new CoarsenedEdge( target.id,source.id, edgegWeight);
        				Set<CoarsenedEdge> edges = new HashSet<CoarsenedEdge>(){{ add(ce1); }};
        				coarsenedNode cn = new coarsenedNode(source, edges);
        			    allNodes.add(cn);
        			    allEdges.add(ce1);
        			    
        			    Map<Edge, Set<coarsenedNode>> edgeNodeMap;
        			    Map<CoarsenedEdge, Set<CoarsenedEdge>> nodeEdgeMap;
        				
        			}
    			}
    		}*/
    }
    
    public int getSimilarity(Cluster c1, Cluster c2) {
        int sim = 0;
        TreeSet<Node> n1 = new TreeSet<>(c1.nodes);
        TreeSet<Node> n2 = new TreeSet<>(c2.nodes);
        n1.retainAll(n2);
        for (Node n : n1) {
            sim = sim + n.getWeight();
        }
        return sim;
    }
    
    public void addNode(Map<Integer, Cluster> clusters) {
    	
    }
    
    public void addNode(coarsenedNode a) {
        if (nodeMap.containsKey(a.getId())) {
        } else {
        	coarsenedNode n = new coarsenedNode();
            allNodes.add(n);
            nodeMap.put(String.valueOf(a.getId()), n);
            nodeEdgeMap.put(a, new HashSet<CoarsenedEdge>());
        }
    }

    public void addEdge(coarsenedNode a, coarsenedNode b, int weight) {
/*        Edge e = new Edge(a, b, weight);
        allEdges.add(e);
        Set<coarsenedNode> nodes = new HashSet<>();
        nodes.add(a);
        nodes.add(b);
        allNodes.addAll(nodes);
        edgeNodeMap.put(e, nodes);
        if (!nodeEdgeMap.containsKey(a.toString())) {
            nodeEdgeMap.put(a.getItem().getId().toString(), new HashSet<Edge>());
        }
        if (!nodeEdgeMap.containsKey(b.toString())) {
            nodeEdgeMap.put(b.getItem().getId().toString(), new HashSet<Edge>());
        }
        nodeEdgeMap.get(a.getItem().getId().toString()).add(e);
        nodeEdgeMap.get(b.getItem().getId().toString()).add(e);*/
        //System.out.println(nodeEdgeMap);
    }

    public void addEdge(IndexItem a, IndexItem b) {
     //   addEdge(new coarsenedNode(a), new coarsenedNode(b), a.getSize() * b.getSize());
    }

    public Set<coarsenedNode> getNodes(Edge e) {
        return getEdgeNodeMap().get(e);
    }

    public Set<CoarsenedEdge> getEdges(coarsenedNode n) {
        return nodeEdgeMap.get(n.getId());
    }

    public Set<CoarsenedEdge> getAllEdges() {
        return allEdges;
    }

    public Set<coarsenedNode> getAllNodes() {
        return allNodes;
    }
    
    public String toString()
    {
    	StringBuffer result = new StringBuffer();
    	for (coarsenedNode coarsenedNode : allNodes) {
			result.append(coarsenedNode.Id+":");
			Set<CoarsenedEdge> edges = nodeEdgeMap.get(coarsenedNode);
			for (CoarsenedEdge e: edges)
			{
				result.append("["+e.getSource() + "-->" +e.getTarget()+",W = "+e.getWeight()+"],");
			}
			result.append("\n");
		}
        
        return result.toString();
    }

	public Map<CoarsenedEdge, Set<coarsenedNode>> getEdgeNodeMap() {
		return edgeNodeMap;
	}
}


