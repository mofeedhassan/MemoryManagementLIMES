/**
 * 
 */
package de.uni_leipzig.simba.GeoCache.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author mofeed
 *
 */
public class ExLru extends Lru{
	protected final Map<String, Integer> keyIndices = new HashMap<String, Integer>(); 
	/**
     * Constructor calls super constructor with same parameters.
     */
	public ExLru(int size, int evictCount) {
		super(size, evictCount);
		// TODO Auto-generated constructor stub
	}

    /**
     * Evicts the 1st object or m_evictCount objects form m_access list.
     */
    @Override
    protected void evict() {
        Iterator<Object> it = m_access.iterator();
        for (int i = 0; i < m_evictCount; i++)
            if (it.hasNext()) {
                Object o = it.next();
                m_cacheMap.remove(o);
                keyIndices.remove(o); // reomve the key from the indices keeper
                it.remove();
            }
    }

    @Override
    protected void hitAccess(Object key) {
        int index = keyIndices.get(key); // get the current index of this key in m_access
        if (index >= 0)
            m_access.remove(index); //remove it from the list
        else
            log4j.error("Was it realy a hit?");
        m_access.add(key);
        keyIndices.put(key.toString(), m_access.size()-1);// update the new index at the end of m_access
    }

    @Override
    protected void putAccess(Object key) {
    	int index;
    	if(keyIndices.containsKey(key))
    	{
    		log4j.error("Key in cache!");
    		index = keyIndices.get(key); // get the current index of this key in m_access
    		m_access.remove(index);
    	}
        m_access.add(key);
        keyIndices.put(key.toString(), m_access.size()-1);// update the new index at the end of m_access [[There is a problem here as other entries in this map should be updated too reflecting their new position]]
    }

    @Override
    public List<Object> removeValues(Object value) {
        List<Object> removed = super.removeValues(value);
        m_access.removeAll(removed);
        keyIndices.clear();
        return removed;
    }
}
