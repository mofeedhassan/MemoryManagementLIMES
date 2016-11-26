package de.uni_leipzig.simba.memorymanagement.Index.graphclustering;

import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.datacache.Fifo2ChanceCache;
import de.uni_leipzig.simba.memorymanagement.datacache.FifoCache;
import de.uni_leipzig.simba.memorymanagement.datacache.LfuCache;
import de.uni_leipzig.simba.memorymanagement.datacache.LfuDACache;
import de.uni_leipzig.simba.memorymanagement.datacache.LruCache;
import de.uni_leipzig.simba.memorymanagement.datacache.SLruCache;
import de.uni_leipzig.simba.memorymanagement.datacache.SimpleCache;
import de.uni_leipzig.simba.memorymanagement.datacache.TimedLruCache;
import de.uni_leipzig.simba.memorymanagement.datacache.TimedSLruCache;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;
import de.uni_leipzig.simba.memorymanagement.structure.ClusteringType;

public class ClusteringFactory {

	public static Clustering createClustering(ClusteringType clusteringType)
	{
		if(clusteringType.equals(ClusteringType.EDGEGREEDY))
			return new EdgeGreedyClustering();
		else if (clusteringType.equals(ClusteringType.NODEGREEDY))
			return new NodeGreedyClustering();
		else if(clusteringType.equals(ClusteringType.HYBRIDGREEDY))
			return new HybridGreedyClustering();
		else if(clusteringType.equals(ClusteringType.SIMPLEEDGE))
			return new SimpleEdgeClustering();
		else
			return new NaiveClustering();

	}
}
