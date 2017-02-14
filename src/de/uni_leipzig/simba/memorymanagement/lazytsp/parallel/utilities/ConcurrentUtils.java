package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author mofeed
 * It provides utilites to be used by the parallel processing threads and executors
 */
public class ConcurrentUtils {
	private static int timeBeforeTermination = 60 ;
	public static  void setTimeBeforeTermination(int time)
	{timeBeforeTermination = time;}
	//This method shutdown the given executor with a termination time waiting
	 public static void stop(ExecutorService executor) {
	        try {
	            executor.shutdown();
	            executor.awaitTermination(timeBeforeTermination, TimeUnit.SECONDS);
	        }
	        catch (InterruptedException e) {
	            System.err.println("termination interrupted");
	        }
	        finally {
	            if (!executor.isTerminated()) {
	                System.err.println("killing non-finished tasks");
	            }
	            executor.shutdownNow();
	        }
	    }
	 	//This puts the exec into sleep for x seconds
	    public static void sleep(int seconds) {
	        try {
	            TimeUnit.SECONDS.sleep(seconds);
	        } catch (InterruptedException e) {
	            throw new IllegalStateException(e);
	        }
	    }
}
