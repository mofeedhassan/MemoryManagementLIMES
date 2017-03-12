package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.tests;

import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.KLPartitioner;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph3;

public class KLPartitionerTest {
	MGraph3 g = new MGraph3();
	KLPartitioner kl = new KLPartitioner(g, false);
	
	
	private void initializeGraph()
	{
		//g.coarsenedEdgesWeights.put(key, value)
	}
	
}
