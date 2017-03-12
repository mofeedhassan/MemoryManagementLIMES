package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.CoarsenedEdge;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.EdgeComparatorByWieght;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedNode;

public abstract class HEV implements Coarsener, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6134526121747893524L;
	
	public Map<Integer,Integer> coarsenedNodeWeights = new HashMap<>();
	public Map<Integer,Map<Integer,Integer>> coarsenedEdgesWeights = new HashMap<>();
	public Map<Integer,Set<Integer>> coarsenedNodesContainedNodes = new HashMap<>();
	public Map<Integer,Integer> isolatedNodeWeights = new HashMap<>();
	public Map<Integer,Boolean> matching = new HashMap<>();
	double reduction_factor = 0.9;
	int allFineGraphSize =0;
	protected static int level = 0 ;
	boolean ascending =  true; // for soring purposes
	
	public HEV(MGraph3 g)
	{
		loadAll(g);
		allFineGraphSize =  coarsenedNodeWeights.size() + g.isolatedNodeWeights.size();
	}
	
	public HEV(MGraph3 g, double reductionFactor)
	{
		loadAll(g);
		reduction_factor= reductionFactor;
		allFineGraphSize =  coarsenedNodeWeights.size();
	}
	
	public abstract MGraph3 coarsen();
	
	public void clearAll()
	{
		coarsenedNodeWeights.clear();
		coarsenedEdgesWeights.clear();
		coarsenedNodesContainedNodes.clear();
		isolatedNodeWeights.clear();
	}
	public void loadAll(MGraph3 g)
	{
		for (Integer nodeId : g.nodeWeights.keySet()) {
			coarsenedNodeWeights.put(nodeId, g.nodeWeights.get(nodeId));
			
			HashSet<Integer> containeNodes= new HashSet<>();
			containeNodes.add(nodeId);
			coarsenedNodesContainedNodes.put(nodeId, containeNodes);
		}
		for (Integer soucreId : g.edgesWeights.keySet()) {
			Map<Integer,Integer> targetEdge = new HashMap<>(g.edgesWeights.get(soucreId));
			coarsenedEdgesWeights.put(soucreId, targetEdge);
		}
		
		initializeMatching();
		isolatedNodeWeights = g.isolatedNodeWeights;

	}

	protected void initializeMatching()
	   { //works ony with the nodes exist in the coarsening map
		   matching.clear();
		   for (Integer match : coarsenedNodeWeights.keySet()) {
				matching.put(match, false);
			}
	   }
	
	protected void serializecCoarsenedGraph(int level)
    {
        try{
        	// Serialize data object to a file
        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("MGraph3CoarsenedNodesWeights"+level+".ser"));
        	out.writeObject(coarsenedNodeWeights);
        	out.close();
        	
        	out = new ObjectOutputStream(new FileOutputStream("MGraph3CoarsenedEdgesWeights"+level+".ser"));
        	out.writeObject(coarsenedEdgesWeights);
        	out.close();
        	
        	out = new ObjectOutputStream(new FileOutputStream("MGraph3CoarsenedNodesContainedNodes"+level+".ser"));
        	out.writeObject(coarsenedNodesContainedNodes);
        	out.close();
        	
        	out = new ObjectOutputStream(new FileOutputStream("MGraph3IsolatedNodes"+level+".ser"));
        	out.writeObject(isolatedNodeWeights);
        	out.close();
        	
        	
        }
        catch(IOException e){System.out.println(e.getMessage());}
    }
	

   
   private void deSerializecCoarsenedGraph(Map<Integer, Integer> nodeWeights, Map<Integer, Map<Integer, Integer>> edgesWeights ,Map<Integer, Integer> isolatedNodeWeights,int level)
    {
    	try{ 
			  FileInputStream inputFileStream = new FileInputStream("MGraph3CoarsenedNodesWeights"+level+".ser");
		      ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
		      coarsenedEdgesWeights = (Map<Integer, Map<Integer, Integer>>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close(); 
		      
		      inputFileStream = new FileInputStream("MGraph3CoarsenedEdgesWeights"+level+".ser");
		      objectInputStream = new ObjectInputStream(inputFileStream);
		      coarsenedEdgesWeights = (Map<Integer, Map<Integer, Integer>>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close();
		      
		      inputFileStream = new FileInputStream("MGraph3CoarsenedNodesContainedNodes"+level+".ser");
		      objectInputStream = new ObjectInputStream(inputFileStream);
		      coarsenedNodesContainedNodes = (Map<Integer, Set<Integer>>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close();
		      
		      inputFileStream = new FileInputStream("MGraph3IsolatedNodes"+level+".ser");
		      objectInputStream = new ObjectInputStream(inputFileStream);
		      isolatedNodeWeights = (Map<Integer, Integer>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close();
			}
		catch(IOException e){System.out.println(e.getMessage());} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
   
/*	@Override
	public coarsenedGraph getCorsenedGraph(coarsenedGraph g) {
		//node,setedges,setnodes,
		List<CoarsenedEdge> list=new ArrayList<CoarsenedEdge>(g.getAllEdges());
		Collections.sort(list, new EdgeComparatorByWieght()); //sort edges
		Collections.reverse(list);
		for (CoarsenedEdge edge : list) {
			Map<CoarsenedEdge, Set<coarsenedNode>> edgeNodes = g.getEdgeNodeMap();
			Set<coarsenedNode> nodes = edgeNodes.get(edge); // edge.getSource()
			System.out.println(nodes.size());
			coarsenedNode s = null;
			coarsenedNode t =  null;
			for (coarsenedNode coarsenedNode : nodes) { 
				if(coarsenedNode.getId()==edge.getSource())//merge o the source
					s = coarsenedNode;
				else
					t=coarsenedNode;
			}
			s.mergeTo(t);
		}
		return null;
	}*/
	
	@Override
	public coarsenedGraph getCorsenedGraph(coarsenedGraph g) {
		//node,setedges,setnodes,
		List<CoarsenedEdge> list=new ArrayList<CoarsenedEdge>(g.getAllEdges());
		Collections.sort(list, new EdgeComparatorByWieght()); //sort edges
		Collections.reverse(list);
		Map<CoarsenedEdge,Integer> mappedEdges =  new HashMap<>();
		for (CoarsenedEdge coarsenedEdge : list) {
			mappedEdges.put(coarsenedEdge, coarsenedEdge.getWeight());
		}
		System.out.println(mappedEdges);
		for (CoarsenedEdge edge : list) {
			if(mappedEdges.containsKey(edge))
			{
				System.out.println(((CoarsenedEdge) edge).getSource()+":"+((CoarsenedEdge) edge).getTarget());
				int s= ((CoarsenedEdge) edge).getSource();
				int t = ((CoarsenedEdge) edge).getTarget();
				
				if(!g.matching.get(s) && !g.matching.get(t))
				{
					Set<Integer> sourceClusters= g.coarsenedNodes.get(s);
					Set<Integer> targetClusters= g.coarsenedNodes.get(t);
					sourceClusters.addAll(targetClusters);
					
					int newWeight = g.allClusters.get(s)+g.allClusters.get(t);
					g.allClusters.put(s, newWeight);
					g.coarsenedNodes.remove(t);
					g.matching.put(s, true);
					g.matching.put(t, true);
					Set<Integer> sourceNieghbors =  g.neighborhood.get(s);
					Set<Integer> targetNieghbors =  g.neighborhood.get(t);
					sourceNieghbors.remove(t);
					targetNieghbors.remove(s);
					
					for (Integer neighbor : sourceNieghbors) {
						if(targetNieghbors.contains(neighbor))//common node
						{
							int total = mappedEdges.get(new CoarsenedEdge(s, neighbor, 0)) + mappedEdges.get(new CoarsenedEdge(t, neighbor, 0)) ;
							targetNieghbors.remove(neighbor);
							mappedEdges.remove(new CoarsenedEdge(t, neighbor, 0));
							mappedEdges.put(new CoarsenedEdge(s, neighbor, total), total);
							g.neighborhood.get(s).add(neighbor);//no need as it is already a neighbor
							g.neighborhood.get(neighbor).add(s);//same as above
						}
					}
					for (Integer neighbor : targetNieghbors) {
						int w =  mappedEdges.get(new CoarsenedEdge(t, neighbor, 0));
						mappedEdges.put(new CoarsenedEdge(s, neighbor, w), w);
						mappedEdges.remove(new CoarsenedEdge(t, neighbor, 0));
						g.neighborhood.get(neighbor).remove(t);
						g.neighborhood.get(s).add(neighbor);
						g.neighborhood.get(neighbor).add(s);
					}
					g.allClusters.remove(t);
					mappedEdges.remove(new CoarsenedEdge(s, t, 0));
					mappedEdges.remove(new CoarsenedEdge(t, s, 0));

					g.neighborhood.remove(t);
					g.neighborhood.get(s).remove(t);
					
				}
			}
			
			System.out.println(g.neighborhood);
			/*Map<CoarsenedEdge, Set<coarsenedNode>> edgeNodes = g.getEdgeNodeMap();
			Set<coarsenedNode> nodes = edgeNodes.get(edge); // edge.getSource()
			System.out.println(nodes.size());
			coarsenedNode s = null;
			coarsenedNode t =  null;
			for (coarsenedNode coarsenedNode : nodes) { 
				if(coarsenedNode.getId()==edge.getSource())//merge o the source
					s = coarsenedNode;
				else
					t=coarsenedNode;
			}
			s.mergeTo(t);*/
		}
		return null;
	}
	

}
