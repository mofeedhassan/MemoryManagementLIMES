/**
 * 
 */
package de.uni_leipzig.simba.memorymanagement.datacache;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.memorymanagement.indexing.IndexItem;
import de.uni_leipzig.simba.memorymanagement.indexing.Indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
/**
 * @author mofeed
 *
 */
public class FIFOSimple extends AbstractCache {
    static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("LIMES");
	protected Map<Object, Object> m_cacheMap = new HashMap<Object, Object>();
    protected Queue<Object> m_access = new LinkedList<Object>();

    protected final int m_cacheMaxSize;
    protected final int m_evictCount;
    protected int m_hits;
    protected int m_misses;
    int currentSize;//current number of resources in the cache now
    int capacity;//max number of resources to store in the cache
    
    ////////////////CONSTRUCTORS
    public FIFOSimple(int size, int evictCount) {
        m_cacheMaxSize = size;
        m_evictCount = evictCount;
        currentSize=m_hits = m_misses = 0;
    }
    public FIFOSimple(int size, int evictCount, int theCapcity) {
        m_cacheMaxSize = size;
        m_evictCount = evictCount;
        capacity = theCapcity;
        currentSize=m_hits = m_misses = 0;
    }


	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.DataCache#getData(de.uni_leipzig.simba.memorymanagement.indexing.IndexItem, de.uni_leipzig.simba.memorymanagement.indexing.Indexer, java.lang.String)
	 */
    //This method acts like load item into cache and works with LOAD command
	@Override
	public Cache getData(IndexItem index, Indexer indexer, String load) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load:"+ System.currentTimeMillis());
		if(load.equals("Load"))
		{
			if(!m_cacheMap.containsKey(index))
			{
        		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load: did not find data with key ="+index+" cache :"+System.currentTimeMillis());
				put(index, indexer.get(index));
			}
			else
				logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load: cache contains value : "+ index+":"+ System.currentTimeMillis());
		}
		else 
			logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load: non Load Command :"+ System.currentTimeMillis());
        return null; ////cast the retrieved object to unbox the cache required
	}
	
    @Override
    public Object put(Object key, Object val) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":put():"+ System.currentTimeMillis());

       IndexItem removed= null;
       List<IndexItem> removedItems=null;
       if (m_cacheMaxSize > 0) {
    	   
    	   int hypercubeSize= ((IndexItem) key).getSize();
		   logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":put(): hypersize cube= "+hypercubeSize+":"+ System.currentTimeMillis());

		    putCache(hypercubeSize);
       	    putAccess(key);// lock m_access only

	   		return m_cacheMap.put(key, val);
       }
       return null;
   }
    private synchronized void putCache(int hypercubeSize)
    {
    	   List<IndexItem> removedItems=null;
		   while ((currentSize + hypercubeSize) > capacity)
		   {
 		    logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":put(): while full cache :"+ System.currentTimeMillis());
			   removedItems = (List<IndexItem>)evict();  
			   updateCurrentSize(removedItems);
		   }
    }
	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#getData(de.uni_leipzig.simba.memorymanagement.indexing.IndexItem, de.uni_leipzig.simba.memorymanagement.indexing.Indexer)
	 */
	@Override
	public Cache getData(IndexItem index, Indexer indexer) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData():"+ System.currentTimeMillis());
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData(): get hypercube with index"+index+" :"+ System.currentTimeMillis());

        Object value = m_cacheMap.get(index);
        if (value != null)// that is hit
            m_hits++; // added by me to suit the wrapping needed
        else // added by me to suit the wrapping needed
        {
            m_misses++;
    		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData(): missed hypercube with index"+index+" :"+ System.currentTimeMillis());
            put(index, indexer.get(index));// put the IndexItem and its values from the indexer
            value = m_cacheMap.get(index);// after it is added
        }

        return (Cache)value; ////cast the retrieved object to unbox the cache required
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#deleteData(de.uni_leipzig.simba.memorymanagement.indexing.IndexItem)
	 */
	@Override
	public synchronized void deleteData(IndexItem index) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":deleteData():"+ System.currentTimeMillis());
		List<Object> removed = removeValues(index);
		if(removed!=null)
			log4j.debug("removed :" + index.toString());

	}
	@Override
	public synchronized List<Object> removeValues(Object key) {
   		logger.info(Thread.currentThread().getName()+getClass().getName()+":removeValues():with key= "+key+":" +System.currentTimeMillis());

    	List<Object> removed = new ArrayList<Object>();
    	int hypercubeSize= ((IndexItem) key).getSize();
    	
    	if(m_cacheMap.remove(key)!=null)
    	{
    		removed.add(key);
    		currentSize = currentSize - hypercubeSize;
    	}
   		logger.info(Thread.currentThread().getName()+getClass().getName()+":removeValues(): value removed in removeValues with key= "+key+":" +System.currentTimeMillis());

        return removed;
    }

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#getHits()
	 */
	@Override
	public int getHits() {
		return m_hits;
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#getMisses()
	 */
	@Override
	public int getMisses() {
		return m_misses;
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#hitAccess(java.lang.Object)
	 */
	@Override
	protected void hitAccess(Object key) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#evict()
	 */
	@Override
	protected Object evict() {
   		logger.info(Thread.currentThread().getName()+getClass().getName()+":evict():" +System.currentTimeMillis());
		List<Object> removed =null;
        for (int i = 0; i < m_evictCount && !m_access.isEmpty(); i++) {
            removed.add(m_access.poll());
       		logger.info(Thread.currentThread().getName()+getClass().getName()+":evict():"+removed.get(removed.size()-1)+":" +System.currentTimeMillis());
            m_cacheMap.remove(removed);
       		logger.info(Thread.currentThread().getName()+getClass().getName()+":evict():remove form m_cacheMap:" +System.currentTimeMillis());
        }	
        return removed;
	}
	
	protected void updateCurrentSize(List<IndexItem> removedItems) {
        for (int i = 0; i < removedItems.size(); i++) {
            currentSize-=removedItems.get(i).getSize();
        }
 		logger.info(Thread.currentThread().getName()+":updateCurrentSize(): update the cache current size with evicted items: "+System.currentTimeMillis());
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache#putAccess(java.lang.Object)
	 */
	@Override
	protected void putAccess(Object key) {
		m_access.add(key);
	}

}
