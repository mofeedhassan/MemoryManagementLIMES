package de.uni_leipzig.simba.GeoCache.cache;

public class CacheFactory {
	static AbstractCache cache=null;
	public static AbstractCache createCache(String type,int size, int evictCount)
	{
		if(type.equals("Lru"))
			cache= new Lru(size, evictCount);
		else if(type.equals("SLru"))
			cache= new SLru(size, evictCount);
		else if (type.equals("Lfu"))
			cache= new Lfu(size, evictCount);
		else if (type.equals("LfuDA"))
			cache =  new LfuDA(size, evictCount);
		else if (type.equals("Fifo"))
			cache = new Fifo(size, evictCount);
		else if(type.equals("Fifo2ndChance"))
			cache= new Fifo2ndChance(size, evictCount);
		else if(type.equals("GDStar"))
			cache= new GDStar(size, evictCount);
		else if(type.equals("SGDStar"))
			cache= new SGDStar(size, evictCount);
		else if(type.equals("ELru"))
			cache= new ELru(size, evictCount);
		else if(type.equals("ExLru"))
			cache= new ExLru(size, evictCount);
		else if(type.equals("TimedLru"))
			cache= new TimedLru(size, evictCount);
		else if(type.equals("TimedSLru"))
			cache= new TimedSLru(size, evictCount);
		else
			cache = new Fifo(size, evictCount);
		
		
		return cache;
	}

}
