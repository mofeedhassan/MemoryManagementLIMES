package de.uni_leipzig.simba.memorymanagement.parallel.tests;

import java.util.concurrent.Callable;

import org.math.array.util.Random;

public class ParallelCallableTest {
	int clusterId=0;
	int processorId=0;
	public ParallelCallableTest(){}
	public ParallelCallableTest(int cid){clusterId = cid;}
	Callable<String> task = () -> {		int r = Random.randInt(1, 4);
											System.out.println(clusterId+" at processor "+ ExecuterTest.count /*java.lang.Thread.activeCount()*/); 
											ExecuterTest.decCount();
											try {
												Thread.sleep(1000);
											} catch (InterruptedException e) {
												e.printStackTrace();}
											ExecuterTest.IncCount();
											return clusterId+" at processor "+ processorId;
									};

	public void displayId(){
		int r = Random.randInt(1, 4);
		System.out.println(clusterId+" at processor "+ processorId); 
		processorId++;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
	public  void setProcessorId(int pId){processorId = pId;}

}
