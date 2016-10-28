package de.uni_leipzig.simba.GeoCache.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.simba.GeoCache.cache.Lru;

import de.uni_leipzig.simba.mapper.atomic.hausdorff.GeoSquare;
/*
 * 
 * */
public class ELru extends AbstractCache {
    protected final Map<String, GeoSquare> m_access = new HashMap<String, GeoSquare>();

	public ELru(int size, int evictCount) {
		super(size, evictCount);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void hitAccess(Object key) {
		
	}

	@Override
	protected void evict() {
      
		
	}

	@Override
	protected void putAccess(Object key) {
       
		
	}
	
	 @Override
	    public List<Object> removeValues(Object value) {
	     return null;
	    }

	    @Override
	    public int size() {
	        return m_cacheMap.size();
	    }

	    /**
	     * return false, if we have an error.
	     */
	    @Override
	    public boolean test() {
	        if (size() > m_cacheMaxSize || m_access.size() > size() || m_access.size() > m_cacheMaxSize)
	            return false;
	        return true;
	    }

	    @Override
	    public String toString() {
	        return m_cacheMap.toString() + "\n" + m_access.toString();
	    }
	    
	    @Override
	    public boolean contains(Object key) {
	    	// TODO Auto-generated method stub
	    	return super.contains(key);
	    }

}
