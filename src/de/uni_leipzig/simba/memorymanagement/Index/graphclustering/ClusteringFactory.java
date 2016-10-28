package de.uni_leipzig.simba.memorymanagement.Index.graphclustering;

import de.uni_leipzig.simba.memorymanagement.datacache.CacheType;
import de.uni_leipzig.simba.memorymanagement.datacache.ClusteringType;
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

public class ClusteringFactory {

	public static Clustering createClustering(ClusteringType clusteringType)
	{
		if(clusteringType.equals(ClusteringType.EdgeGreedy))
			return new EdgeGreedyClustering();
		else if (clusteringType.equals(ClusteringType.NodeGreedy))
			return new NodeGreedyClustering();
		else if(clusteringType.equals(ClusteringType.HybridGreedy))
			return new HybridGreedyClustering();
		else if(clusteringType.equals(ClusteringType.SimpleEdge))
			return new SimpleEdgeClustering();
		else
			return new NaiveClustering();

	}
}
