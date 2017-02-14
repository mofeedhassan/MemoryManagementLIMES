package de.uni_leipzig.simba.memorymanagement.datacache;

import de.uni_leipzig.simba.memorymanagement.structure.CacheType;

public class DataCacheFactory {
	private static int cacheSize=Integer.MAX_VALUE;
	private static int evictCount=1;
	private static int capacity=20;

	
	public static DataCache createCache(CacheType cacheType, int size, int evCount, int Capacity)
	{
		cacheSize= size;
		evictCount = evCount;
		capacity = Capacity;
		return createCache(cacheType);
	}
	
	public static DataCache createCache(CacheType cacheType, int Capacity)
	{
		capacity = Capacity;
		return createCache(cacheType);
	}
	
	public static DataCache createCache(CacheType cacheType)
	{
		if(cacheType.equals(CacheType.LRU))
			return new LruCache(cacheSize, evictCount, capacity);
		else if (cacheType.equals(CacheType.SLRU))
			return new SLruCache(cacheSize, evictCount, capacity);
		else if(cacheType.equals(CacheType.LFU))
			return new LfuCache(cacheSize, evictCount, capacity);
		else if(cacheType.equals(CacheType.LFUDA))
			return new LfuDACache(cacheSize, evictCount);
		else if(cacheType.equals(CacheType.FIFO2ND))
			return new Fifo2ChanceCache(cacheSize, evictCount, capacity);
		else if(cacheType.equals(CacheType.TIMEDLRU))
			return new TimedLruCache(cacheSize, evictCount, capacity);
		else if (cacheType.equals(CacheType.TIMEDSLRU))
			return new TimedSLruCache(cacheSize, evictCount, capacity);
		else if (cacheType.equals(CacheType.SIMPLE))
			return new SimpleCache(capacity);
		else if (cacheType.equals(CacheType.FIFOSIMPLE))
			return new FIFOSimple(cacheSize, evictCount, capacity);
		else
			return new FifoCache(cacheSize, evictCount, capacity);

	}
}
