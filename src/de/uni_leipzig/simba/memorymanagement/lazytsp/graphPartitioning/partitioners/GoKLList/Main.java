package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKLList;

import java.util.logging.Logger;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKL.KLFM.Results;

public class Main {

	static Logger logger = Logger.getLogger("LIMES"); 

	static Graph g = new Graph(5, 7);//nodes,edges
	public static void main(String[] args) {
		initGraph();
		Display();
		
		KLFM k = new KLFM();
		de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoKLList.KLFM.Results res = k.klfm(g);
		System.out.println(res.MIN);
		System.out.println(res.parts);

	}
	
	private static void Display()
	{
		for (Node n : g.nodes) {
			System.out.println(n.id + ":" + n.part + ":" + n.gain);
			for (Edge e : n.edges) {
				System.out.println(e.id + ":" + e.w);
			}
			System.out.println("======================================");
		}
	}
	
	private static void initGraph()
	{
		initNodes();
		initEdges();
		initNodesEdges();
	}
	
	private static void initEdges()
	{
		for(int i=0;i<g.edges.length; i++)
		{
			g.edges[i] = new Edge();
			g.edges[i].id = i;
		}
		g.edges[0].n1 = g.nodes[0];
		g.edges[0].n2 = g.nodes[1];
		g.edges[0].w = 18;
		
		g.edges[1].n1 = g.nodes[1];
		g.edges[1].n2 = g.nodes[2];
		g.edges[1].w = 14;
		
		g.edges[2].n1 = g.nodes[2];
		g.edges[2].n2 = g.nodes[4];
		g.edges[2].w = 15;
		
		g.edges[3].n1 = g.nodes[3];
		g.edges[3].n2 = g.nodes[4];
		g.edges[3].w = 3;
		
		g.edges[4].n1 = g.nodes[0];
		g.edges[4].n2 = g.nodes[3];
		g.edges[4].w = 17;
		
		g.edges[5].n1 = g.nodes[2];
		g.edges[5].n2 = g.nodes[3];
		g.edges[5].w = 2;
		
		g.edges[6].n1 = g.nodes[0];
		g.edges[6].n2 = g.nodes[2];
		g.edges[6].w = 1;
				
	}
	private static void initNodes()
	{
		for(int i=0;i<g.nodes.length; i++)
		{
			g.nodes[i] = new Node();
			g.nodes[i].id = i;
			g.nodes[i].gain=0;
			g.nodes[i].lock =false;
			g.nodes[i].part=-1;
		}
		g.nodes[0].edges = new Edge[3];
		g.nodes[1].edges = new Edge[2];
		g.nodes[2].edges = new Edge[4];
		g.nodes[3].edges = new Edge[3];
		g.nodes[4].edges = new Edge[2];
	}
	
	private static void initNodesEdges()
	{
		g.nodes[0].edges[0] = g.edges[0];
		g.nodes[0].edges[1] = g.edges[4];
		g.nodes[0].edges[2] = g.edges[6];
		
		g.nodes[1].edges[0] = g.edges[0];
		g.nodes[1].edges[1] = g.edges[1];

		g.nodes[2].edges[0] = g.edges[1];
		g.nodes[2].edges[1] = g.edges[2];
		g.nodes[2].edges[2] = g.edges[5];
		g.nodes[2].edges[3] = g.edges[6];

		g.nodes[3].edges[0] = g.edges[3];
		g.nodes[3].edges[1] = g.edges[4];
		g.nodes[3].edges[2] = g.edges[5];
		
		g.nodes[4].edges[0] = g.edges[2];
		g.nodes[4].edges[1] = g.edges[3];
	}
}
