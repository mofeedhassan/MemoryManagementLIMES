/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.Index.planner.execution;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataOperator;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.indexing.Indexer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//import org.apache.log4j.Logger;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author ngonga
 */
public class CacheAccessExecution {
	static java.util.logging.Logger logger = Logger.getLogger("LIMES"); 

    DataCache cache;
    List<DataManipulationCommand> commands;
    Measure measure;
    double threshold;
    String property = "p1|p2";//
    Indexer indexer;

    public CacheAccessExecution(DataCache c, List<DataManipulationCommand> commands, Measure m, double threshold, Indexer indexer) {
        cache = c;
        this.commands = commands;
        measure = m;
        this.threshold = threshold;
        this.indexer = indexer;
    }

    /**
     * Run a computation plan including loading and flushing of data from memory
     * TODO: Implement comparisons
     */
    public int run() {
        Mapping m = new Mapping();
        AtomicInteger count = new AtomicInteger(0);
        DataManipulationCommand currentCommand;
        for (int i = 0; i < commands.size(); i++) {
            currentCommand = commands.get(i);
            if (currentCommand.op.equals(DataOperator.LOAD)) {
                for (int j = 0; j < currentCommand.operands.size(); j++) {
//                    System.out.println("LOADING " + currentCommand.operands.get(j).getId());
            		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":run():check data is in cache:"+ System.currentTimeMillis());
            		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":call()::"+ System.currentTimeMillis());

                    {cache.getData(currentCommand.operands.get(j), indexer,"Load");}
                    
                	//cache.getData(currentCommand.operands.get(j), indexer,"Load");
      //              System.out.println(currentCommand.operands.get(j));
                }
            } else if (currentCommand.op.equals(DataOperator.FLUSH)) {
                for (int j = 0; j < currentCommand.operands.size(); j++) {
//                    System.out.println("FLUSHING " + currentCommand.operands.get(j).getId());
            		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":run():flushes data from cache:"+ System.currentTimeMillis());
            		{cache.deleteData(currentCommand.operands.get(j));}
                	//cache.deleteData(currentCommand.operands.get(j));
   //                 System.out.println(currentCommand.operands.get(j));

                }
            } else {//compare
            	Cache source=null;
            	Cache target=null;
        		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":run():loads source from cache:"+ System.currentTimeMillis());
            	/*synchronized (cache)*/{source = cache.getData(currentCommand.operands.get(0), indexer);}
            	//source = cache.getData(currentCommand.operands.get(0), indexer);
 //               System.out.println(cache.getHits()+":"+cache.getMisses());
//                System.out.println("LOADING "+currentCommand.operands.get(0).getId());
        		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":run():loads target from cache:"+ System.currentTimeMillis());
            	/*synchronized (cache)*/{target = cache.getData(currentCommand.operands.get(1), indexer);}
            	//target = cache.getData(currentCommand.operands.get(1), indexer);
//                System.out.println(cache.getHits()+":"+cache.getMisses());
                double dd=0;
//                System.out.println("LOADING "+currentCommand.operands.get(1).getId());
//                System.out.println(currentCommand.operands.get(0));
//                System.out.println(currentCommand.operands.get(1));

//                System.out.println(source);
//                System.out.println(target);
        		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":run():get mappings:"+ System.currentTimeMillis());
                double d=0;
                for (Instance s : source.getAllInstances()) {
                    for (Instance t : source.getAllInstances()) {
                        d = measure.getSimilarity(s, t, property, property);
                        if (d >= threshold) {
                            count.incrementAndGet();
 //                          m.add(s.getUri(), t.getUri(), d);
                        }
                    }
                }
        		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":run():has a distance="+d+":"+ System.currentTimeMillis());
            }
        }
        return count.get(); // m.getNumberofMappings();
 //       System.out.println("Mapping contains "+m.getNumberofMappings()+" mappings");
 //              System.out.println("Mapping contains "+count+" mappings");
    }
}
