package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.tests;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Clustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.EdgeGreedyClustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Graph;
import de.uni_leipzig.simba.memorymanagement.indexing.HR3IndexItem;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.coarseners.HEV;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.ClusterEdge;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures.coarsenedGraph;

public class coarsenedNodeTest {
	
	
	public  static Map<Integer, Cluster> initializClusters()
	{
 /*       Graph g = new Graph();
        HR3IndexItem i1 = new HR3IndexItem(5, "11A");
        HR3IndexItem i2 = new HR3IndexItem(2, "12A");
        HR3IndexItem i3 = new HR3IndexItem(3, "13A");
        HR3IndexItem i4 = new HR3IndexItem(1, "14A");
        HR3IndexItem i5 = new HR3IndexItem(3, "15A");
        HR3IndexItem i6 = new HR3IndexItem(3, "16A");

        g.addEdge(i1, i2);
        g.addEdge(i1, i6);
        g.addEdge(i2, i4);
        g.addEdge(i2, i6);
        g.addEdge(i3, i6);
        g.addEdge(i5, i6);
        
        Clustering gc = new EdgeGreedyClustering();
        Map<Integer, Cluster> clusters = gc.cluster(g, 10);
        try{
        	// Serialize data object to a file
        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("clusters.ser"));
        	out.writeObject(clusters);
        	out.close();
        }
        catch(IOException e){System.out.println(e.getMessage());}*/
		Map<Integer, Cluster> clusters=null;
		try{ 
			FileInputStream inputFileStream = new FileInputStream("clusters.ser");
		      ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
		      clusters = (Map<Integer, Cluster>)objectInputStream.readObject();
		      objectInputStream.close();
		      inputFileStream.close(); 
			}
		catch(IOException e){System.out.println(e.getMessage());} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return clusters;

	}
	
	public static void main(String[] args)
	{
		Map<Integer, Cluster> clusters = initializClusters();
        //System.out.println(clusters);

		coarsenedGraph g = new coarsenedGraph();
    	g.initializeGraph(clusters);

		Map<Cluster,Set<ClusterEdge>> fineGraph = g.getFineGraph();
    	for (Cluster cl : fineGraph.keySet()) {
    		String edges="";
    		for (ClusterEdge e : fineGraph.get(cl)) {
    			edges+=" edge id = "+e.id+"| W= "+e.weight+"),";
			}
        	System.out.println("Node = "+cl.id + ", edges["+edges+"]");

		}
    	System.out.println(g);
    	HEV coarsen =  new HEV();
    	g=coarsen.getCorsenedGraph(g);
    	System.out.println(g);
		
	}
}
