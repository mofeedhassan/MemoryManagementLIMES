package de.uni_leipzig.simba.GeoCache.MultipleCaches;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import de.uni_leipzig.simba.GeoCache.cache.AbstractCache;
import de.uni_leipzig.simba.GeoCache.cache.CacheFactory;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GeoHR3;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GeoIndex;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GeoSquare;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.PolygonIndex;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.PolygonReader;
import de.uni_leipzig.simba.measures.pointsets.SetMeasureFactory;
import de.uni_leipzig.simba.measures.pointsets.hausdorff.CentroidIndexedHausdorff;
import de.uni_leipzig.simba.measures.pointsets.hausdorff.IndexedHausdorff;
import de.uni_leipzig.simba.measures.pointsets.hausdorff.NaiveHausdorff;
/*
 * This class Apply different caching techniques over the generated blocks of HR3 algorithms.
 * The aim is to calculate the amount of data transfer (related to the block size) that is 
 * done every time a block is moved from the hard disk to memory according to different caching techniques
 * 
 * 
 * */
/**
 * @author mofeed
 *
 */
public class GeoHR3Cached {
	static AbstractCache cache=null;
	static GeoHR3 geoHr3 = new GeoHR3(0.5f, GeoHR3.DEFAULT_GRANULARITY, SetMeasureFactory.getType(new NaiveHausdorff()));
	static Map<String, Set<String>> computed = new HashMap<String, Set<String>>();
    static Mapping m = new Mapping();
    //cache parameters
    static int cacheSize=0,evictNumber=0;
    static String cacheType;
    //logger variables
    static Logger log = Logger.getLogger(GeoHR3Cached.class.getName());
    static FileHandler fh;
    static boolean logIt = true;

    static float d;
    static long startTime=0,TotalRunTime=0; // to calculate the total time of the run for single run configuration (run() method timing)
    // TotalCacheTime: total time of cache checks + probably the transfer time in case of misses (TotalHitsTime + TotalMissesTime)
    //TotalHitsTime: total time of cache hits for all source/target squares => (check cache)
    //TotalMissesTime: total time of cache misses for all source/target squares => (check cache + transfer data)
    //TotalCalcTime: total time of caching and distances calculations in each iteration (cache times + calc. time)
	static long TotalCacheTime = 0, TotalHitsTime = 0, TotalMissesTime =0,TotalCalcTime =0;
    float distanceThreshold=0f;
    float angularThreshold =0;
    public static void initializeLogger(String file)
    {
    	if(logIt)
    	{
	    	log.setLevel(Level.INFO);
			try {  
	
		        // This block configure the logger with handler and formatter  
		        fh = new FileHandler(file);
		        PatternLayout layout = new PatternLayout();
		        FileAppender appender = new FileAppender(layout,file, false);
		        log.addAppender(appender);
		        SimpleFormatter formatter = new SimpleFormatter();  
		        fh.setFormatter(formatter);  
		        // the following statement is used to log any messages  
	
		    } catch (SecurityException e) {  
		        e.printStackTrace();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }
    	}
    	else
    		log.setLevel(Level.OFF);
    }
   
	private static void evaluatePSO2GHR3(String inputFile, int polyStart, int PolyEnd, int PolyInc) {
		String result = new String();
		result = "\nRepeat\tPolyNr\tGeoHR3Time\tGeoHR3Indexing\t" +
				"2Threads\tindexingTime\ttasksCreationTime\tloadBalancerTime\t" +
				"4Threads\tindexingTime\ttasksCreationTime\tloadBalancerTime\t" +
				"8Threads\tindexingTime\ttasksCreationTime\tloadBalancerTime\t" +
				"16Threads\n\tindexingTime\ttasksCreationTime\tloadBalancerTime\n";
		log.debug("-------------------- Cached GeoHR3 --------------------");
		for(int polyNr = polyStart ; polyNr <= PolyEnd ; polyNr += PolyInc ){ //731922
			for(int repeat = 1 ; repeat <= 1 ; repeat++){
				System.out.println("============== REPEAT " + repeat + " ==============");
				//GeoHR3 geoHr3 = new GeoHR3(0.5f, GeoHR3.DEFAULT_GRANULARITY, SetMeasureFactory.getType(new NaiveHausdorff()));
				startTime = System.currentTimeMillis();
				GeoHR3Cached g = new GeoHR3Cached();
				g.run(inputFile, polyNr, cacheType);
				TotalRunTime = System.currentTimeMillis() - startTime;
				result = result + repeat + "\t" + polyNr + "\t" + TotalRunTime + "\t" + geoHr3.indexingTime + "\t" ;
				result += "\n";
			}
			System.out.println(result);
		}
		
		System.out.println("Final Results:\n" + result);
		log.info(result);
		log.info("Total run time in millisec: "+TotalRunTime + " Total claculation time: "+ TotalCalcTime + " Total cache time: "+ TotalCacheTime);
	}
  
	public static void main(String[] args) {
		//BlockingModule generator = BlockingFactory.getBlockingModule(property2, p.op, threshold, granularity);
		String data = args[0]; //file contains data
		String logFile =args[1]; // file for log information
		cacheType = args[2]; // type of cahe to be used {FIFO, LRU,...}
		cacheSize = Integer.parseInt(args[3]); // The size of the cache {10,10^2 10^3,10^4,10^5}
		evictNumber = Integer.parseInt(args[4]);// 1
        log.info("Running GeoHR3 with cache "+cacheType);  
		initializeLogger(logFile);
		evaluatePSO2GHR3(data, 100000, 100000, 50000);
		

	}   
	public void run(String inputFile,int polyNr,String cacheType)
	{
		// read the polygons data from  file
		log.debug("Reading polygon data from file with polygon number = "+ polyNr+"\n");
		Set<Polygon> sourcePolygonSet = PolygonReader.readPolygons(inputFile, polyNr);
		/*long pointsCounter = 0;
		for (Polygon polygon : sourcePolygonSet) {
			pointsCounter+=polygon.size();
		}
		log.info("Number of Polygons to read= "+ polyNr);
		log.info("Number of points in this dataset = "+ pointsCounter);*/
		//for each polygon set create indexing's squares
		log.debug("Indexing source polygons\n");
		GeoIndex source = geoHr3.assignSquares(sourcePolygonSet);
		log.debug("Indexing target polygons\n");
        GeoIndex target = geoHr3.assignSquares(sourcePolygonSet);
        
        //initialize the measure
    	Mapping m = new Mapping();
    	log.debug("Setting the measure");
    	  double d; //float d;
        if (geoHr3.setMeasure instanceof CentroidIndexedHausdorff) {
            ((CentroidIndexedHausdorff) geoHr3.setMeasure).computeIndexes(sourcePolygonSet, sourcePolygonSet);
        } else if (geoHr3.setMeasure instanceof IndexedHausdorff) {
            PolygonIndex targetIndex = new PolygonIndex();
            targetIndex.index(sourcePolygonSet);
            ((IndexedHausdorff) geoHr3.setMeasure).targetIndex = targetIndex;
        }
        //create cache to use with specific size and evict count
        log.info("Creating cache object of type "+cacheType);
		cache = CacheFactory.createCache(cacheType, cacheSize, evictNumber);
        //for cache hit and misses
        int cacheHit=0;
        int cacheMiss=0;
        float totalTransferSize=0;
        int iteration =0;
        long startCacheTime =0, startCalcTime=0;
		if(cache!=null)
		{
			log.debug("cache object is created successfully");
			// run the computations against the caching and calculates the number of hits and missed and overall time for each
			//Iterate over each source's squares one by one to calculate similarities with target's squares belongs to each
			//with each hit or miss in the cache calculate the size of transferred data according to the size of each square (block)
			GeoSquare g1= null;
			log.debug("Iterate over the indexed squares of the source polygons");
	        for (Integer latIndex : source.squares.keySet()) {
	            for (Integer longIndex : source.squares.get(latIndex).keySet()) {
	            	///////////////////////////////////Source//////////////////////////////////////////////////////////
	            	startCalcTime = System.currentTimeMillis(); // save start time of the whole calculation for the picked source square
	            	String key = latIndex+":"+longIndex; //create the key for the source's square based on lat and lon indices
	            	log.debug("Created caching key for the source square is: "+key);
                	startCacheTime = System.currentTimeMillis();// save the start time of the caching check and transfer processes for source square
	            	//get the source square
	            	log.debug("Check if the cache contains the key");
	            	if(cache.contains(key)) // if exists in the cache
	            	{
	            		log.debug("Square is cached, get it");
	            		g1 = (GeoSquare)cache.get(key); //read it from cache
	            		log.debug("Record cache hit");
	            		cacheHit++;
	            		TotalHitsTime += System.currentTimeMillis() - startCacheTime;//Accumulate the HitsTime in case of hit
	            	}
            		else //not exist in cache
            		{//get it and insert it in the cache
            			log.debug("Square is not cached, get it with its points from source squares");
            			g1 = source.getSquare(latIndex, longIndex);
            			log.debug("Record cache miss");
            			cacheMiss++;
            			log.debug("Add it to cache with an attached key");
            			cache.put(key, g1);
            			log.debug("Accumulate the total transfered data sizes by the size of transfered square ");
            			TotalMissesTime += System.currentTimeMillis() - startCacheTime; // Accumulate the missesTime in case of miss
            			totalTransferSize += g1.size();
            		}
	            	/////////////////////////////////Target//////////////////////////////////////////////////////////
	            	// get targets' square indices related to the current source's square
	            	log.debug("Get from target the set of squares indices (lat:long)to compare to the source one");
	            	Set<List<Integer>> squares = geoHr3.getSquaresToCompare(latIndex, longIndex, target);
	          	    //for each target's square
	            	log.debug("Iterate over set of target squares");
	                for (List<Integer> squareIndex : squares) {
	                	GeoSquare g2 = null; //initialize target's square
	                	//initialize the key
	                	log.debug("Get a target square indices (lat:long)for creating key");
	                	key ="";
	                	key= squareIndex.get(0)+":"+squareIndex.get(1);//target's square lat:long
	                	log.debug("Check if the target square is cached");
	                	startCacheTime = System.currentTimeMillis();//save the start time of the caching check and transfer processes for each target square
	                	if(cache.contains(key)) // if exists in the cache
		            	{
	                		log.debug("Target square is in cache");
	                		log.debug("Get it from cache");
		            		g2 = (GeoSquare)cache.get(key); //read it from cache
		            		log.debug("Record cache hit");
		            		cacheHit++;
		            		TotalHitsTime += System.currentTimeMillis() - startCacheTime; //Accumulate the HitsTime in case of hit
		            	}
	            		else //not exist in cache
	            		{//get it and insert it in the cache
	            			log.debug("Target square is not cached");
	            			log.debug("Add Target square to cache associated with created key (lat:long)");
	            			g2 = source.getSquare(latIndex, longIndex);
	            			cache.put(key, g2);
	            			log.debug("Record cache miss");
	            			cacheMiss++;
	            			log.debug("Accumulate the total transfered data sizes by the size of transfered square ");
	            			TotalMissesTime += System.currentTimeMillis() - startCacheTime; // Accumulate the missesTime in case of miss
	            			totalTransferSize += g2.size();//(Float) tBlocksSizes.get(squareIndex.get(0), squareIndex.get(1));
	            		}
	                	
	                    // only run if the hyper-cube actually exists
	                    //do the calculations
	                	
	                	log.debug("Calculate distances between polygons in source and target squares");
	                    for (Polygon a : g1.elements) {
	                        for (Polygon b : g2.elements) {
	                            if (!computed.containsKey(a.uri)) {
	                                computed.put(a.uri, new HashSet<String>());
	                            }
	                            if (!computed.get(a.uri).contains(b.uri)) {
	                                //add subset condition
	                                    d = geoHr3.setMeasure.computeDistance(a, b, distanceThreshold);
	                                    if (d <= distanceThreshold) { 
	                                    	log.debug("Add the URIs plus the distance into mapping");
	                                        m.add(a.uri, b.uri, 1/(1+d));
	                                    }
	                            }
	                            log.debug("Add the two URIs to computed set");
	                            computed.get(a.uri).add(b.uri);
	                        }
	                    }
	                }
	              //calculate calculation time of the whole picked source square (cache+distance) and accumulate the total calculation time for all source squares
	                TotalCalcTime += System.currentTimeMillis() - startCalcTime;
	            }
	        }
	        //Total time of cache processes hits + misses
	        TotalCacheTime = TotalHitsTime + TotalMissesTime ;
	       log.info("Cache hits value: "+cacheHit+","+"Cache misses value: "+cacheMiss+" Total transfer size: "+totalTransferSize);
	       log.info(m);
		}
	}
	/**
	 *@author mofeed
	 * This method calculates for set of Blocks (set of  geoSquare) the total size of it
	 * @param squaresSet: The GeoIndex object contains set of squares of the source/target
	 * @return MultiKeyMap with two keys represent the latIndex and LongIndex for each block and the value represents its size
	 */
	public MultiKeyMap getSquareSize(GeoIndex squaresSet)
	{
		MultiKeyMap sqauresSizes = null;
		if(squaresSet.squares.size()!=0)// not empty
		{
			sqauresSizes = new MultiKeyMap();
			float squareSize;
			for (Integer latIndex : squaresSet.squares.keySet()) {
	            for (Integer longIndex : squaresSet.squares.get(latIndex).keySet()) {
	            	GeoSquare gSquare = squaresSet.getSquare(latIndex, longIndex);
	            	squareSize= gSquare.size();
	            	sqauresSizes.put(latIndex, longIndex, squareSize);
	            }
	            
			}
		}
		return sqauresSizes;
	}
}
