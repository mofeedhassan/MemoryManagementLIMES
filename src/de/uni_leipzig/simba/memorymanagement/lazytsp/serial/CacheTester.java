package de.uni_leipzig.simba.memorymanagement.testTSPCaching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.GeoCache.io.FileOperations;
import de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache;
import de.uni_leipzig.simba.memorymanagement.datacache.CacheType;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCacheFactory;

public class CacheTester {
    public static Logger          log4j      = Logger.getLogger(CacheTester.class);

	static AbstractCache cache=null;
	static String type="";
	static int size=0;
	static int evictCount =0;
	static String dataFile ="";
	static int cacheHit =0;
	static int cacheMiss =0; 
	public static void main(String[] args) {
		type = args[0];
		size = Integer.parseInt(args[1]);
		evictCount = Integer.parseInt(args[2]);
		dataFile = args[3];
		log4j.info("The parameters recieved are:"+type+":Cache size -> "+size+": evict count -> "+evictCount+":"+dataFile);
		cache=(AbstractCache)DataCacheFactory.createCache(CacheType.valueOf(type), size, evictCount,1000);
		log4j.info("Cache is created.............");
		tester(type);
	}

	public static void tester(String type)
	{
		List<Item> data = readDataItems(dataFile);
		String info="";
		Item item=null;
		Random rand = new Random();
		int index=0;
		for(int i=0;i<20;i++)
		{
			index= rand.nextInt(5) + 1;
			log4j.info("The traget item's index is: "+ index);
			item = data.get(index); //check here ??
			log4j.info("The item information is: "+ item.key+":"+item.key);
			if(cache.contains(item.key)) // if exists in the cache
        	{
        	//	cache.get(item.key, new Index); //read it from cache
        		cacheHit++;
        	}
    		else //not exist in cache
    		{//get it and insert it in the cache
    			cacheMiss++;
    			cache.put(item.key, item.value); //check here??
    		}
			log4j.info("Cache current items:"+cache.keys());
			
		}
		showResults();
	}

	public static Map<String, String> readData (String file)
	{
		Map<String, String> data = new LinkedHashMap<String, String>();
		FileOperations op = new FileOperations(file);
		List<String> lines = op.readFile();
		for (String line : lines) {
			String[] info = line.split("\t");
			data.put(info[0], info[1]);
		}
		return data;
	}
	public static List<Item> readDataItems (String file)
	{
		List<Item> data = new ArrayList<Item>();
		Item newItem=null;
		FileOperations op = new FileOperations(file);
		List<String> lines = op.readFile();
		for (String line : lines) {
			String[] info = line.split("\t");
			newItem = new Item(info[0], info[1]);
			data.add(newItem);
		}
		return data;
	}
	public static void showResults()
	{
		System.out.println("Number of Hit: "+ cacheHit+", number of misses: "+ cacheMiss);
	}

}
