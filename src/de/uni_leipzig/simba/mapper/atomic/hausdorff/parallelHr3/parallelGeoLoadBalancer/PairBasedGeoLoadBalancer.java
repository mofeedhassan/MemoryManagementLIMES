/**
 * 
 */
package de.uni_leipzig.simba.mapper.atomic.hausdorff.parallelHr3.parallelGeoLoadBalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.uni_leipzig.simba.mapper.atomic.hausdorff.GeoSquare;

/**
 * @author sherif
 *
 */
public class PairBasedGeoLoadBalancer extends GeoLoadBalancer{
	private static final Logger logger = Logger.getLogger(PairBasedGeoLoadBalancer.class.getName());

	/**
	 * @param maxThreadNr
	 *@author sherif
	 */
	public PairBasedGeoLoadBalancer(int maxThreadNr) {
		super(maxThreadNr);
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.mapper.atomic.hausdorff.parallelHr3.parallelGeoLoadBalancer.GeoLoadBalancer#getBalancedLoad(java.util.Map, int)
	 */
	@Override
	public List<Multimap<GeoSquare, GeoSquare>> getBalancedLoad(Multimap<GeoSquare, GeoSquare> tasks) {
		logger.info("Starting PairBasedGeoLoadBalancer ...");
		long start = System.currentTimeMillis();
		
		// Compute tasks' complexity
		computeTasksComplexity(tasks);
//		computeTasksComplexityParallel(tasks); //TODO run time more than normal execution
		
		// sort tasks descending
		start = System.currentTimeMillis();
		Set<Entry<Map<GeoSquare, GeoSquare>, Long>> set = tasks2Complexity.entrySet();
		List<Entry<Map<GeoSquare, GeoSquare>, Long>> sortedTasks = new ArrayList<Entry<Map<GeoSquare, GeoSquare>, Long>>(set);
		Collections.sort( sortedTasks, new Comparator<Map.Entry<Map<GeoSquare, GeoSquare>, Long>>(){
			public int compare( Map.Entry<Map<GeoSquare, GeoSquare>, Long> o1, Map.Entry<Map<GeoSquare, GeoSquare>, Long> o2 ){
				return (o2.getValue()).compareTo( o1.getValue() );
			}
		} );
//		System.out.println("\t Sorting time: " + (System.currentTimeMillis() - start) + "ms");

		// initialize result
		start = System.currentTimeMillis();
		for(int threadId = 0 ; threadId < maxThreadNr ; threadId++ ){
			Multimap<GeoSquare, GeoSquare> initial = HashMultimap.create();
			balancedTaskBlocks.add(initial);
		}

		// distribute tasks  
		int threadId = 0;
		
		// in case of odd number of tasks, just add the first one to the first thread block
		if(sortedTasks.size()%2 != 0){
			Entry<Map<GeoSquare, GeoSquare>, Long> firstEntry = sortedTasks.get(0);
			for( Entry<GeoSquare, GeoSquare>  e : firstEntry.getKey().entrySet()){
				balancedTaskBlocks.get(threadId).put(e.getKey(), e.getValue());
			}
			sortedTasks.remove(0);
		}
		threadId = 1;
		for(int i = 0 ; i < sortedTasks.size()/2 ; i++){
			Entry<Map<GeoSquare, GeoSquare>, Long> leftTask = sortedTasks.get(i);
			Entry<Map<GeoSquare, GeoSquare>, Long> rightTask = sortedTasks.get(sortedTasks.size()-1-i);
			for( Entry<GeoSquare, GeoSquare>  e : leftTask.getKey().entrySet()){
				balancedTaskBlocks.get(threadId).put(e.getKey(), e.getValue());
			}
			for( Entry<GeoSquare, GeoSquare>  e : rightTask.getKey().entrySet()){
				balancedTaskBlocks.get(threadId).put(e.getKey(), e.getValue());
			}
			threadId = (threadId + 1) % maxThreadNr;
		}
//		System.out.println("\t distribute tasks time: " + (System.currentTimeMillis() - start) + "ms");
		return balancedTaskBlocks;
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.mapper.atomic.hausdorff.parallelHr3.parallelGeoLoadBalancer.GeoLoadBalancer#getName()
	 */
	@Override
	public String getName() {
		return "PairBasedGeoLoadBalancer";
	}



}
