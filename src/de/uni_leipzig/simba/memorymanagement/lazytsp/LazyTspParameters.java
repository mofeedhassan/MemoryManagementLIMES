package de.uni_leipzig.simba.memorymanagement.lazytsp;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Clustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.ClusteringFactory;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCacheFactory;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SolverFactory;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;
import de.uni_leipzig.simba.memorymanagement.structure.ClusteringType;
import de.uni_leipzig.simba.memorymanagement.structure.SolverType;

public class LazyTspParameters {
	private final DataCache dataCache;
	private final PathFinder solver;
	private final Clustering cluster;
	private final Double threshold;
	private final Integer repetition;
	// constants
	private static final Double thresholdDefault = 0.95;
	private static final Integer repetitionDefault = 1;
	
	private LazyTspParameters(DataCache dataCache, PathFinder solver, Clustering cluster, Double threshold, Integer repetition )
	{
		this.dataCache=dataCache;
		this.solver=solver;
		this.cluster=cluster;
		this.threshold=threshold;
		this.repetition=repetition;

	}
	public DataCache getDataCache(){return dataCache;}
	public PathFinder getSolver(){return solver; }
	public Clustering getClustering(){return cluster;}
	public Double getThreshold(){return threshold;}
	/**
	 * Parameters builder class
	 * @author mofeed
	 *
	 */
	public static class LazyTspParametersBuilder
	{
		private DataCache dataCache;
		private PathFinder solver;
		private Clustering cluster;
		private Double threshold;
		private Integer repetition;
		public LazyTspParametersBuilder()
		{
			dataCache= DataCacheFactory.createCache(CacheType.DEFAULT);
			solver= SolverFactory.createSolver(SolverType.DEFAULT);
			cluster= ClusteringFactory.createClustering(ClusteringType.DEFAULT);
			threshold=thresholdDefault;
			repetition=repetitionDefault;
		}
		public LazyTspParametersBuilder setCache(CacheType cache, int capacity)
		{
			dataCache = DataCacheFactory.createCache(cache,capacity);
			return this;
		}
		public LazyTspParametersBuilder setSolver(SolverType solver, int optimizationTime, int iterations)
		{
			this.solver= SolverFactory.createSolver(solver);
			
			return this;
		}
		public LazyTspParametersBuilder setCluster(ClusteringType cluster)
		{
			this.cluster= ClusteringFactory.createClustering(cluster);
			return this;
		}
		public LazyTspParametersBuilder setThreshold(Double threhold)
		{
			this.threshold= threhold;
			return this;
		}
		public LazyTspParametersBuilder setRepetition(Integer repetition)
		{
			this.repetition= repetition;
			return this;
		}
		public LazyTspParameters creatParameters()
		{
			return new LazyTspParameters(dataCache, solver, cluster, threshold,repetition);
		}
	}
	
	
}
