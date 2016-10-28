package de.uni_leipzig.simba.GeoCache.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TimedLru extends Lru{
	BiMap<String, Integer> keyIndices = HashBiMap.create ();
	int cacheTime = 0; //with each cache access it is incremented except for eviction
	int evictIndex = 0; // it depends on the fact that each item inserted in cache is recorded in keyIndices based on cacheTime in that moment which increases sequentially
	public TimedLru(int size, int evictCount) {
		super(size, evictCount);
		// TODO Auto-generated constructor stub
	}

	/**
     * Evicts the 1st object or m_evictCount objects form m_access list.
     */
    @Override
    protected void evict() {
    	int evictCount = m_evictCount;
		BiMap<Integer,String> inverseKeyIndices = keyIndices.inverse();

        while(evictCount !=0 && inverseKeyIndices.size() > 0) // for the number of elements to be evicted, second cond. in case of evict > existing elemtn nr.
        {
        	if(inverseKeyIndices.containsKey(evictIndex)) // if the entry of the required index exists
        	{
        		m_cacheMap.remove(inverseKeyIndices.get(evictIndex));// remove the item from the cache
        		keyIndices.remove( inverseKeyIndices.get(evictIndex)); // remove it from the key indices
        		evictCount--;
        	}
    		evictIndex++; // update to the next evicted item index
        }
        /*Iterator<Object> it = m_access.iterator();
        for (int i = 0; i < m_evictCount; i++)
            if (it.hasNext()) {
                Object o = it.next();
                m_cacheMap.remove(o);
                it.remove();
            }*/
    }
    /*
     * This method updates cache recently used indices in case of cache hit situation.
     * It should update the existing item's index to new one reflecting its new usage moment
     * key : the key of the stored item in cache (lat:long) 
     * */
    @Override
    protected void hitAccess(Object key) {
     	if(keyIndices.containsKey(key))
    	{
    		keyIndices.put(key.toString(), cacheTime);// update the index of this cache key
    		cacheTime++; //update cache timing
    	}
    	else
            log4j.error("Was it realy a hit?");
    	
        /*int index = keyIndices.get(key); // get the current index of this key in m_access
        if (index >= 0)
            m_access.remove(index); //remove it from the list
        else
            log4j.error("Was it realy a hit?");
        m_access.add(key);
        keyIndices.put(key.toString(), m_access.size()-1);// update the new index at the end of m_access
*/    }

    /* 
     * This method updates cache recently used indices in case of cache put situation. (new item)
     * It should add the new item along with its insertion timing as index in indices list
     * The insertion of the item in the cache happens in the caller method 
     */
    /* 
     * @key: the key of the item in the cache (lat:long) 
     */
    @Override
    protected void putAccess(Object key) {
    	if(keyIndices.containsKey(key))
    	{
    		log4j.error("Key in cache!");
    		keyIndices.remove(key); // remove its entry from keys:indices table
    	}
    	keyIndices.put(key.toString(), cacheTime); // Add it with its new timing
    	cacheTime++;
	
    	/*int index;
    	if(keyIndices.containsKey(key))
    	{
    		log4j.error("Key in cache!");
    		index = keyIndices.get(key); // get the current index of this key in m_access
    		m_access.remove(index);
    	}
        m_access.add(key);
        keyIndices.put(key.toString(), m_access.size()-1);// update the new index at the end of m_access [[There is a problem here as other entries in this map should be updated too reflecting their new position]]
*/    }

    @Override
    public List<Object> removeValues(Object value) {
        List<Object> removed = super.removeValues(value);
        m_access.removeAll(removed);
        keyIndices.clear();
        return removed;
    }
}
