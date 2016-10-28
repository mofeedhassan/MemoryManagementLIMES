package de.uni_leipzig.simba.GeoCache.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.simba.GeoCache.MultipleCaches.GeoHR3Cached;
import de.uni_leipzig.simba.GeoCache.cache.AbstractCache;
import de.uni_leipzig.simba.GeoCache.cache.CacheFactory;
import de.uni_leipzig.simba.GeoCache.io.FileOperations;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GeoSquare;

public class CacheTester {
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
		
		cache = CacheFactory.createCache(type, size, evictCount); 
		tester(type);
	}
	public static void tester(String type)
	{
		if(type.equals("TimedLru"))
			TimedLruTester();
		else if(type.equals("SLru"))
			SLruTester();
		else if(type.equals("TimedSLru"))
			TimedSLruTester();
	}
	public static void TimedLruTester()
	{
		initCacheCounter();
		List<Item> data = readDataItems(dataFile);
		String info="";
		Item item=null;
		for(int i=0;i<data.size();i++)
		{
			item = data.get(i);
			if(cache.contains(item.key)) // if exists in the cache
        	{
        		info = (String)cache.get(item.key); //read it from cache
        		cacheHit++;
        	}
    		else //not exist in cache
    		{//get it and insert it in the cache
    			cacheMiss++;
    			cache.put(item.key, item.value);
    		}
		}
		showResults();
	}
	public static void SLruTester()
	{
		initCacheCounter();
		List<Item> data = readDataItems(dataFile);
		String info="";
		Item item=null;
		for(int i=0;i<data.size();i++)
		{
			item = data.get(i);
			if(cache.contains(item.key)) // if exists in the cache
        	{
        		info = (String)cache.get(item.key); //read it from cache
        		cacheHit++;
        	}
    		else //not exist in cache
    		{//get it and insert it in the cache
    			cacheMiss++;
    			cache.put(item.key, item.value);
    		}
		}
		showResults();
	}
	public static void TimedSLruTester()
	{
		initCacheCounter();
		List<Item> data = readDataItems(dataFile);
		String info="";
		Item item=null;
		for(int i=0;i<data.size();i++)
		{
			item = data.get(i);
			if(cache.contains(item.key)) // if exists in the cache
        	{
        		info = (String)cache.get(item.key); //read it from cache
        		cacheHit++;
        	}
    		else //not exist in cache
    		{//get it and insert it in the cache
    			cacheMiss++;
    			cache.put(item.key, item.value);
    		}
		}
		showResults();
	}
	/*public static Map<String, String> readData (String file)
	{
		Map<String, String> data = new HashMap<String, String>();
		FileOperations op = new FileOperations(file);
		List<String> lines = op.readFile();
		for (String line : lines) {
			String[] info = line.split("\t");
			data.put(info[0], info[1]);
		}
		return data;
	}*/
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
	public static void initCacheCounter()
	{
		cacheHit=0;
		cacheMiss=0;
	}
	public static void showResults()
	{
		System.out.println("Number of Hit: "+ cacheHit+", number of misses: "+ cacheMiss);
	}

}
