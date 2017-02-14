package de.uni_leipzig.simba.memorymanagement.lazytsp;

import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.measures.MeasureFactory;
import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Clustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.ClusteringFactory;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCacheFactory;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SolverFactory;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;
import de.uni_leipzig.simba.memorymanagement.structure.ClusteringType;
import de.uni_leipzig.simba.memorymanagement.structure.MeasureType;
import de.uni_leipzig.simba.memorymanagement.structure.RuningPartType;
import de.uni_leipzig.simba.memorymanagement.structure.SolverType;
/**
 * 
 * @author mofeed
 * class contains the components required to run the lazyTSP run
 */
public class LazyTspComponents {
	private final DataCache dataCache;
	private final PathFinder solver;
	private final Clustering cluster;
	private final Double threshold;
	private final Integer repetition;
	private final Integer capacity;
	private final Integer cores;
	private final RuningPartType part;
	private final Measure measure;
	// constants
	private static final Double thresholdDefault = 0.95;
	private static final Integer repetitionDefault = 1;
	private static final Integer capacityDefault = 100;

	private LazyTspComponents(DataCache dataCache, PathFinder solver, Clustering cluster, Double threshold, Integer repetition,Integer capacity,Integer cores, RuningPartType part,Measure measure )
	{
		this.dataCache=dataCache;
		this.solver=solver;
		this.cluster=cluster;
		this.threshold=threshold;
		this.repetition=repetition;
		this.capacity=capacity;
		this.cores=cores;
		this.part=part;
		this.measure=measure;
	}
	
	public DataCache getDataCache(){return dataCache;}
	public PathFinder getSolver(){return solver; }
	public Clustering getClustering(){return cluster;}
	public Double getThreshold(){return threshold;}
	public Integer getRepetition(){return repetition;}
	public Integer getCapacity(){return capacity;}
	public RuningPartType getPart(){return part;}
	public Measure getMeasure(){return measure;}


	/**
	 * Parameters builder class
	 * @author mofeed
	 *
	 */
	public static class LazyTspComponentsBuilder
	{
		private DataCache dataCache;
		private PathFinder solver;
		private Clustering cluster;
		private Double threshold;
		private Integer capacity;
		private Integer repetition;
		private Integer cores;
		private RuningPartType part;
		private Measure measure;

		
		public LazyTspComponentsBuilder()
		{
			dataCache= DataCacheFactory.createCache(CacheType.DEFAULT);
			solver= SolverFactory.createSolver(SolverType.DEFAULT);
			cluster= ClusteringFactory.createClustering(ClusteringType.DEFAULT);
			threshold=thresholdDefault;
			repetition=repetitionDefault;
			capacity= capacityDefault;
			cores = Runtime.getRuntime().availableProcessors();
			measure = new EuclideanMetric();
		}
		public LazyTspComponentsBuilder setCache(CacheType cache, int capacity)
		{
			dataCache = DataCacheFactory.createCache(cache,capacity);
			return this;
		}
		public LazyTspComponentsBuilder setSolver(SolverType solver, double solverParameter)
		{
			this.solver= SolverFactory.createSolver(solver,solverParameter);
			
			return this;
		}
		public LazyTspComponentsBuilder setSolver(SolverType solver)
		{
			this.solver= SolverFactory.createSolver(solver);
			
			return this;
		}
		public LazyTspComponentsBuilder setCluster(ClusteringType cluster)
		{
			this.cluster= ClusteringFactory.createClustering(cluster);
			return this;
		}
		public LazyTspComponentsBuilder setThreshold(Double threhold)
		{
			this.threshold= threhold;
			return this;
		}
		
		public LazyTspComponentsBuilder setMeasure(String measure)
		{
			this.measure= MeasureFactory.getMeasure(measure);
			return this;
		}
		
		public LazyTspComponentsBuilder setCapacity(Integer capacity)
		{
			this.capacity= capacity;
			return this;
		}
		public LazyTspComponentsBuilder setRepetition(Integer repetition)
		{
			this.repetition= repetition;
			return this;
		}
		public LazyTspComponentsBuilder setCore(Integer cores)
		{
			this.cores= cores;
			return this;
		}
		public LazyTspComponentsBuilder setPart(RuningPartType part)
		{
			this.part= part;
			return this;
		}
		public LazyTspComponents creatParameters()
		{
			return new LazyTspComponents(dataCache, solver, cluster, threshold,repetition,capacity,cores,part,measure);
		}
	}
	
	
}
