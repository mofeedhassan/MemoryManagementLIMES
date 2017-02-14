package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Clustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.EdgeGreedyClustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Graph;
import de.uni_leipzig.simba.memorymanagement.indexing.HR3IndexItem;

public class Main {

	public static void main(String[] args) {
	/////MGraph
		testMGraph();
	///////////////////////PGraph tests	
//		testPGraph();
		/////////////////run to initialize the serialized file for clusters forming new graph
		//initializeSerializeCluster("graph2");

		
	}
	public static void testMGraph()
	{
		Map<Integer, Cluster> clusters =  clusterDSerialize("graph1.ser");
        MGraph pg = new MGraph();
        pg.createFineGraph(clusters);;
        
        pg.displayGraph(pg.nodeWeights, pg.edgesWeights);
        
        pg.displayGraph(pg.coarsenedNodeWeights, pg.coarsenedEdgesWeights);
        
       // pg.getCorsenedGraphEdgeOrder();

        pg.getCorsenedGraphNodeOrder();
        
        
        pg.displayGraph(pg.nodeWeights, pg.edgesWeights);
        
        pg.displayGraph(pg.coarsenedNodeWeights, pg.coarsenedEdgesWeights);

	}
	public static void testPGraph()
	{
		Map<Integer, Cluster> clusters =  clusterDSerialize("graph1.ser");
		PGraph pg = new PGraph();
        pg.initializeGraph(clusters);
        
        pg.displayFineGraph();
        
        
		PGraph dummy = new PGraph();
		dummy.getCorsenedGraph(pg);
	}
	
	public static void initializeSerializeCluster(String graphName)
	{
		Graph g = new Graph();
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
        clusterSerialize(graphName+".ser", clusters);
	}
	public static Map<Integer, Cluster> clusterDSerialize(String serializationFile )
	{
		Map<Integer, Cluster> clusters =null;
		try{ 
			FileInputStream inputFileStream = new FileInputStream(serializationFile); //clusters.ser
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
	
	public static void clusterSerialize(String serializationFile , Map<Integer, Cluster> clusters)
	{
		try{ 
			FileOutputStream FileOutputStream = new FileOutputStream(serializationFile); //clusters.ser
		      ObjectOutputStream objectOutputStream = new ObjectOutputStream(FileOutputStream);
		      objectOutputStream.writeObject(clusters);
		      objectOutputStream.close();
		      FileOutputStream.close(); 
			}
		catch(IOException e){System.out.println(e.getMessage());}
	}
	
	
	public void testEdgeHash()
	{
		Map<PEdge,Integer> testhash = new HashMap<>();
		PEdge e1 = new PEdge();
		e1.source =1 ;
		e1.target =2;
		
		PEdge e2 = new PEdge();
		e2.source =2 ;
		e2.target =1;
		
		testhash.put(e1, 1);
		testhash.put(e2, 3);
		
		for (PEdge e : testhash.keySet()) {
			System.out.println(testhash.get(e));

		}
	}

}
