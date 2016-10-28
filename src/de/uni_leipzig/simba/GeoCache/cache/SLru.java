package de.uni_leipzig.simba.GeoCache.cache;

import java.util.LinkedList;
import java.util.List;

/**
 * @author rspeck
 */
public class SLru extends AbstractCache {

    protected final List<Object> m_access       = new LinkedList<Object>();
    protected final List<Object> m_accessSeg    = new LinkedList<Object>();

    /** percentage of cache memory to use for segment */
    private final float          SEGMENT_BORDER = 0.5f;
    /** # of elements in segment */
    private final int            SEGMENT_SIZE   = (int) (m_cacheMaxSize * SEGMENT_BORDER);

    /**
     * Constructor calls super constructor with same parameters.
     */
    public SLru(int size, int evictCount) {
        super(size, evictCount);
    }

    /**
     * Evicts the 1st object form m_access list m_evictCount times
     */
    @Override
    protected void evict() {
        for (int i = 0; i < m_evictCount; i++) {

            if (!m_access.isEmpty()) {
                Object o = m_access.remove(0);
                m_cacheMap.remove(o);
            }

            else if (!m_accessSeg.isEmpty()) {
                Object o = m_accessSeg.remove(0);
                m_cacheMap.remove(o/*m_accessSeg.remove(0)*/);
            }
        }
    }

    @Override
    protected void hitAccess(Object key) {

        if (!m_access.remove(key))//if it is in m_access then: remove it, return true, continue adding it to m_accessSeg
            m_accessSeg.remove(key);//if it is not in m_access then: return false,remove it from m_accessSeg, continue adding it to m_accessSeg making it more recent in it

        m_accessSeg.add(key);

        if (m_accessSeg.size() > SEGMENT_SIZE)// in case of exceeding the size return an item from m_accessSeg to m_access
            m_access.add(m_accessSeg.remove(0));

    }

    @Override
    protected void putAccess(Object key) {
        m_access.add(key);
    }

    @Override
    public List<Object> removeValues(Object value) {

        List<Object> removed = super.removeValues(value);
        m_access.removeAll(removed);
        m_accessSeg.removeAll(removed);

        return removed;
    }

    @Override
    public boolean test() {
        return (size() > m_cacheMaxSize || (m_access.size() + m_accessSeg.size()) > m_cacheMaxSize) ? false : true;
    }
    @Override
    public boolean contains(Object key)
    {
    	//return m_cacheMap.containsKey(key);
    	return super.contains(key);
    }
    @Override
    public String toString() {
        return super.toString() + "\n" + m_access.toString();
    }
}