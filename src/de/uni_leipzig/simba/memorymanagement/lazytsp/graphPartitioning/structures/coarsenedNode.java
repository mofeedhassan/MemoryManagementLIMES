package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Edge;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.indexing.IndexItem;
/**
 * This class represents a single node in the coarsened graph.
 * Its size varies depending on the level of coarsening where in each phase it is merged with other coarsenedNode forming new one.
 * Initially (fine graph) each coarsened node contains set of clusters where each cluster represents a task
 * Its inner edges and outer edges are also represented in Set<>
 * IndexItem--in-->Node--in (as goup) --> Clauster -- in (as group) -->CoarsenedNode
 * @author mofeed
 *
 */
public class coarsenedNode implements Comparable, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3195897007680919725L;
	int Id;
	//counter to assign sequentially identifying numbers to each created coarsened node in the fine graph
	// if node 0 is merged into node 1 then node 0 will removed from graph and node 1 will represent both and so on
	static int idCounter=0; 
	int weight=0; // the total nodes weights inside the coarsened node SUM(W(Node))
	Set<Cluster> containedNodes; // set of clusters (graph node) inside the coarsened node indexItem 
    //Set<Edge> containedEdges; //set of edges between the 
    Map<CoarsenedEdge, Set<coarsenedNode>> edgeNodeMap;
    Map<String, Set<CoarsenedEdge>> nodeEdgeMap;
    Set<CoarsenedEdge> outerEdges; //set of edges going out from the coarsened node

    
    public coarsenedNode()
    {
    	containedNodes= new HashSet<>();
    	outerEdges= new HashSet<>();
        edgeNodeMap = new HashMap<>();
        nodeEdgeMap = new HashMap<>();
 //   	Id=idCounter++;
    }

/*    public coarsenedNode(Cluster c)
    {
    	this();
    	weight = c.getWeight();
    	containedNodes.add(c);
    }*/
    
    public coarsenedNode(Cluster c, Set<CoarsenedEdge> outerEdges)
    {
    	this();
    	Id = c.id;
    	weight = c.getWeight();
    	containedNodes.add(c);
    	this.outerEdges.addAll(outerEdges);
    }
    
    public int getWeight(){return weight;}
    
    /**
     * It returns the set of clusters (graph node in our case). It is useful in finding path between
     * such returned clusters or even with merged ones
     * @return set of clusters exist in the coarsened node
     */
    public Set<Cluster> getContainedNodes()
    {
    	return containedNodes;
    }
    
    public Set<CoarsenedEdge> getOuterEdges()
    {
    	return outerEdges;
    }

    /**
     * It merges the passed node in to the method caller  node. It adds all the passed nodes clusters into
     * this caller clusters list. Afterwards an iteration is done on the passed nod outer edges where the common
     * target edges are merged with new weight = total of edges then all the edges merged and unmerged (as no common target nodes)
     * are added to the list of outer edges.
     * what about the in between edges? they don't matter, any way path is created by passing the set of clusters only,
     * so you can remove it
     * @param node
     */

    
    public void mergeTo(coarsenedNode node)
    {
    	containedNodes.addAll(node.getContainedNodes());
    	Map<CoarsenedEdge,CoarsenedEdge> merdgedEdgesList =  new HashMap<>();//key=replaced edge, value= new merged edge
    	// iterate to find the common node targets to merge the common edges
    	for (CoarsenedEdge ownerEdge : outerEdges) 
    		for (CoarsenedEdge otherEdge : node.getOuterEdges())
    			if(ownerEdge.targetId == otherEdge.targetId) // common target - merge the edge
    			{
    				int mergedWeight = ownerEdge.getWeight()+otherEdge.getWeight();
    				CoarsenedEdge mergedEdge =  new CoarsenedEdge(this.Id, ownerEdge.targetId, mergedWeight);
    				merdgedEdgesList.put(ownerEdge,mergedEdge);
    				break; // no more search it is not hypergraph
    			}
    	for (CoarsenedEdge replacedEdge : merdgedEdgesList.keySet()) {
    		outerEdges.remove(replacedEdge);
    		outerEdges.add(merdgedEdgesList.get(replacedEdge)); // add the equivalent merged edge
		}
    }

    
    public int getId()
    {return Id;}
	@Override
	public int compareTo(Object o) {
		if (o instanceof coarsenedNode) {
            if(Id == getId())
                     {
                return 0;
            }            
        }
        return -1;
	}
	
	/*    public Set<Node> getNodes(Edge e) {
    return edgeNodeMap.get(e);
}

public Set<Edge> getEdges(Node n) {
    return nodeEdgeMap.get(n.getItem().getId().toString());
}*/
/*    public Set<Edge> getContainedEdges()
    {
    	return containedEdges;
    }*/
	
    //TODO return list of outer edges coming out of this node
/*    public void addCoarsenedNode(coarsenedNode node,Set<Edge> edges)
    {
    	containedNodes.add(node);
    	containedEdges.addAll(edges);
    	nodeEdgeMap.put(node.getItem().getId(), edges);
    	for (Edge edge : edges) {
    		Set<Node> edgeNodes = edgeNodeMap.get(edge);
    		if(edgeNodes==null)
    			edgeNodes = new HashSet<>();
    		edgeNodes.add(node);
			edgeNodeMap.put(edge, edgeNodes);
		}
    	weight+=node.getWeight();
    }*/
    
/*    public void addEdge(coarsenedNode a, coarsenedNode b, int weight) {
        Edge e = new Edge(a, b, weight);
        containedEdges.add(e);
        Set<Node> nodes = new HashSet<>();
        nodes.add(a);
        nodes.add(b);
        containedNodes.addAll(nodes);
        edgeNodeMap.put(e, nodes);
        if (!nodeEdgeMap.containsKey(a.toString())) {
            nodeEdgeMap.put(a.getItem().getId().toString(), new HashSet<Edge>());
        }
        if (!nodeEdgeMap.containsKey(b.toString())) {
            nodeEdgeMap.put(b.getItem().getId().toString(), new HashSet<Edge>());
        }
        nodeEdgeMap.get(a.getItem().getId().toString()).add(e);
        nodeEdgeMap.get(b.getItem().getId().toString()).add(e);
        //System.out.println(nodeEdgeMap);
    }*/
    
    
}
