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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
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
	protected ConcurrentHashMap<Object, Object> m_cacheMap = new ConcurrentHashMap<>();
	//protected Map<Object, Object> m_cacheMap = new HashMap<Object, Object>();
	protected ConcurrentLinkedQueue<Object> m_access = new ConcurrentLinkedQueue<>();

	//protected Queue<Object> m_access = new LinkedList<Object>();

	protected final int m_cacheMaxSize;
	protected final int m_evictCount;
	/*protected int m_hits;
	protected int m_misses;*/
	
	protected AtomicInteger m_hits = new AtomicInteger(0);
	protected AtomicInteger m_misses= new AtomicInteger(0);
	
	//int currentSize;//current number of resources in the cache now
	private AtomicInteger currentSize = new AtomicInteger(0);
	int capacity;//max number of resources to store in the cache

	////////////////CONSTRUCTORS
	public FIFOSimple(int size, int evictCount) {
		m_cacheMaxSize = size;
		m_evictCount = evictCount;
	}
	public FIFOSimple(int size, int evictCount, int theCapcity) {
		m_cacheMaxSize = size;
		m_evictCount = evictCount;
		capacity = theCapcity;
	}


	/**
	 * It works to retrieve a value from the cache or insert the value in case its absence
	 * It counts hits and misses
	 */
	@Override
	public Cache getData(IndexItem index, Indexer indexer) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData():"+ System.currentTimeMillis());
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData(): get hypercube with index "+index+" :"+ System.currentTimeMillis());

		Object value = m_cacheMap.get(index);
		if (value != null)// that is hit
		{
			//m_hits++; // added by me to suit the wrapping needed
			m_hits.incrementAndGet();
			System.out.println("Hits= "+m_hits);
		}
		else // added by me to suit the wrapping needed
		{
			m_misses.incrementAndGet();
			System.out.println("Misses= "+m_misses);
			logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData(): missed hypercube with index"+index+" :"+ System.currentTimeMillis());
			put(index, indexer.get(index));// put the IndexItem and its values from the indexer
			//value = m_cacheMap.get(index);// after it is added - modified by the next statement as the value exist why ask the map again for it
			value = indexer.get(index);
		}

		return (Cache)value; ////cast the retrieved object to unbox the cache required
	}
	/**
	 * It loads item into cache in case its absence and works with LOAD command
	 * It returns null as no care what is the loaded value in the call statement of this method
	 */

	@Override
	public Cache getData(IndexItem index, Indexer indexer, String load) {

		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load:"+ System.currentTimeMillis());
		if(!m_cacheMap.containsKey(index))
		{ 
			logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load: did not find data with key ="+index+" cache :"+System.currentTimeMillis());
			put(index, indexer.get(index));
		}
		else
			logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":getData() with Load: cache contains value : "+ index+":"+ System.currentTimeMillis());


		//return (Cache) m_cacheMap.get(index); ////cast the retrieved object to unbox the cache required
		return null; // as the main objective of this function is loading the index and no care about what is it (not used in the call statement of this method_
	}

	@Override
	public Object put(Object key, Object val) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":put():"+ System.currentTimeMillis());

/*		IndexItem removed= null;
		List<IndexItem> removedItems=null;*/
		
		if (m_cacheMaxSize > 0) {

			int hypercubeSize= ((IndexItem) key).getSize();
			logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":put(): hypersize cube= "+hypercubeSize+":"+ System.currentTimeMillis());

			putCache(hypercubeSize);
			putAccess(key);// lock m_access only

			return m_cacheMap.putIfAbsent(key, val);
		}
		return null;
	}

	private /*synchronized*/ void putCache(int hypercubeSize)
	{
		List<IndexItem> removedItems=null;
		while ((currentSize.get() + hypercubeSize) > capacity)
		{
			logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":put(): while full cache :"+ System.currentTimeMillis());
			removedItems = (List<IndexItem>)evict();  
			updateCurrentSize(removedItems);
		}
	}

	@Override
	protected synchronized Object evict() {
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

	protected synchronized void updateCurrentSize(List<IndexItem> removedItems) {
		for (int i = 0; i < removedItems.size(); i++) {
			currentSize.addAndGet(-removedItems.get(i).getSize());
		}
		logger.info(Thread.currentThread().getName()+":updateCurrentSize(): update the cache current size with evicted items: "+System.currentTimeMillis());
	}

	@Override
	protected synchronized void putAccess(Object key) {
		m_access.add(key);
	}

	/**
	 * It removes all data related to the given index
	 */
	@Override
	public void deleteData(IndexItem index) {
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":deleteData():"+ System.currentTimeMillis());
		List<Object> removed = removeValues(index);
		if(removed!=null)
			logger.info("removed :" + index.toString());

	}

	@Override
	public synchronized List<Object> removeValues(Object key) {

		logger.info(Thread.currentThread().getName()+getClass().getName()+":removeValues():with key= "+key+":" +System.currentTimeMillis());
		List<Object> removed = new ArrayList<Object>();
		int hypercubeSize= ((IndexItem) key).getSize();

		if(m_cacheMap.remove(key)!=null)
		{
			removed.add(key);
			currentSize.addAndGet(-hypercubeSize) ;
		}
		logger.info(Thread.currentThread().getName()+getClass().getName()+":removeValues(): value removed in removeValues with key= "+key+":" +System.currentTimeMillis());

		return removed;
	}



	@Override
	public int getHits() {
		return m_hits.get();
	}


	@Override
	public int getMisses() {
		return m_misses.get();
	}


	@Override
	protected void hitAccess(Object key) {
		// TODO Auto-generated method stub

	}




}
