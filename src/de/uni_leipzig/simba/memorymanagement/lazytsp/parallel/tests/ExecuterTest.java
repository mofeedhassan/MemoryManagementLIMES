package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.tests;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.ConcurrentUtils;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.SortedSolvertmp;

public class ExecuterTest {
	public static int NUM_JOBS_TO_CREATE =10;
	public static int matrixSize =12;
	public static int id =-1;
	public static int count=4;


	synchronized public static void IncCount() {
		count++;
	}
	synchronized public static void decCount() {
		count--;
	}
	private static double[][] createClustersMatrix()
	{

		double[][] matrix = new double[matrixSize][matrixSize];
		matrix[0][1]=17;
		matrix[0][3]=11;

		matrix[1][0]=17;
		matrix[1][6]=8;
		matrix[1][2]=20;

		matrix[2][7]=15;
		matrix[2][1]=20;
		matrix[2][3]=7;
		matrix[2][10]=6;
		
		matrix[3][2]=7;
		matrix[3][4]=5;
		matrix[3][0]=11;
		
		matrix[4][3]=5;
		
		matrix[5][9]=10;
		
		matrix[6][7]=12;
		matrix[6][1]=8;
		matrix[6][10]=5;
		
		matrix[7][2]=15;
		matrix[7][6]=12;
		matrix[7][8]=13;
		
		matrix[8][7]=13;
		matrix[8][9]=13;
		
		matrix[9][5]=10;
		matrix[9][8]=13;
		
		matrix[10][6]=15;
		matrix[10][2]=6;
		matrix[10][11]=8;
		
		matrix[11][10]=8;
		return matrix;
	}
	public static void testThread()
	{
		double[][] matrix = createClustersMatrix();
		
		ExecutorService threadPool = Executors.newFixedThreadPool(4);
		SortedSolvertmp ss = new SortedSolvertmp();
		SortedSolvertmp solver = new SortedSolvertmp();
		Object[] sortedClusters = solver.sortingDistances(matrix);
		int[] path = new int[sortedClusters.length];
    	for (int i = 0; i < sortedClusters.length; i++) {
    		path[i] = (int)sortedClusters[i];
    	}
		for (int i : path) {
			System.out.print(i+"\t");
		}
		System.out.println();
		// submit jobs to be executing by the pool
		 for (id = 0; id < path.length; id++) {
		    threadPool.submit(new Runnable() {
		         public void run() {
		        	 System.out.println(path[id]);
		             ParallelCallableTest pr = new ParallelCallableTest(path[id]);
		             pr.setProcessorId(count-1);
		             pr.displayId();
		         }
		     });
		 }
		 // once you've submitted your last job to the service it should be shut down
		 threadPool.shutdown();
		 // wait for the threads to finish if necessary
		 try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void testExecuter()
	{
		double[][] matrix = createClustersMatrix();
		SortedSolvertmp ss = new SortedSolvertmp();
		SortedSolvertmp solver = new SortedSolvertmp();
		Object[] sortedClusters = solver.sortingDistances(matrix);
		int[] path = new int[sortedClusters.length];
    	for (int i = 0; i < sortedClusters.length; i++) {
    		path[i] = (int)sortedClusters[i];
    	}
		for (int i : path) {
			System.out.print(i+"\t");
		}
		System.out.println();
		//IntStream.range(0, 11).forEach(i ->  executor.submit(new ParallelRunner(path[i]).task));
		ExecutorService executor = Executors.newFixedThreadPool(4);
		for(int i=0;i<12;i++)
		{
			Future<String> result = executor.submit(new ParallelCallableTest(path[i]).task);
/*			try {
				System.out.println("This is from Callable "+ result.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}

		ConcurrentUtils.stop(executor);
	}
	public static void main(String[] args)
	{
		testExecuter();
	}
}
