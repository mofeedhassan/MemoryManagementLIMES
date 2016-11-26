package de.uni_leipzig.simba.memorymanagement.parallel;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


import de.uni_leipzig.simba.memorymanagement.datacache.FIFOSimple;
import de.uni_leipzig.simba.memorymanagement.indexing.HR3IndexItem;
import de.uni_leipzig.simba.memorymanagement.indexing.Hr3Indexer;

@RunWith (Parameterized.class)
public class FIFOSimpleCacheTest {

	FIFOSimple cache = null;
	int cacheSize;
	int evictCount;
	int cacheCapcity;
	int maxElements;
	ExecutorService pool = null;
	List<Future<String>> futures =null;
	
	public FIFOSimpleCacheTest(int size, int theCapcity, int evictCount,int mElements)
	{
		this.cacheSize=size;
		this.evictCount=evictCount;
		this.cacheCapcity=theCapcity;
		maxElements =mElements;
	}
	
	@Parameters
	public static Collection cacheInitializers()
	{
		return java.util.Arrays.asList(new Integer[][]{{100,100,1,10}});
	}
	@Test
	public void test() {
		Task[] tasks = {new Task(),new Task(),new Task(),new Task(),new Task(),new Task(),new Task(),new Task(),new Task()};
		for (Task task : tasks) {
			futures.add( pool.submit(task));
		}
		
		for (Future<String> future: futures) {
			try {
				System.out.println(future.get()+":"+cache.getHits()+":"+cache.getMisses());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	@Before
	public void initializeEnvironment()
	{
		cache = new FIFOSimple(cacheSize, evictCount, cacheCapcity);
		pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}
	private class Task implements Callable
	{

		@Override
		public Object call() throws Exception {
			Random rand = new Random();
			int action;
			for(int i=1;i<maxElements;i++)
			{
				action= rand.nextInt();
				if(action%2==0)
					cache.get(new HR3IndexItem(i*i, String.valueOf(i)), new Hr3Indexer(1, 1));
				else
					cache.deleteData(new HR3IndexItem(i*i, String.valueOf(i)));
			}
			
			return null;
		}
		
	}

}
