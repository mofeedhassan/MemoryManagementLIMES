/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.MemoryCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.AtomicMapper;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.measures.space.SpaceMeasure;
import de.uni_leipzig.simba.measures.space.SpaceMeasureFactory;
import de.uni_leipzig.simba.measures.space.blocking.BlockingFactory;
import de.uni_leipzig.simba.measures.space.blocking.BlockingModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 * Uses metric spaces to create blocks.
 * 
 * @author ngonga
 */
public class TotalOrderBlockingMapper implements AtomicMapper {

    public int granularity = 4;
    static Logger logger = Logger.getLogger("LIMES");
    // this might only work for substraction. Need to create something that
    // transforms
    // the threshold on real numbers into a threshold in the function space.
    // Then it will work
    // perfectly

    public String getName() {
	return "TotalOrderBlockingMapper";
    }

    public Mapping getMapping(Cache source, Cache target, String sourceVar, String targetVar, String expression,
	    double threshold) {
	Mapping mapping = new Mapping();

	// maps each block id to a set of instances. Actually one should
	// integrate LIMES here
	HashMap<ArrayList<Integer>, TreeSet<String>> targetBlocks = new HashMap<ArrayList<Integer>, TreeSet<String>>();

	// 0. get properties
	String property1, property2;
	// get property labels
	Parser p = new Parser(expression, threshold);
	// get first property label
	String term1 = p.getTerm1();
	if (term1.contains(".")) {
	    String split[] = term1.split("\\.");
	    property1 = split[1];
	    if (split.length >= 2)
		for (int part = 2; part < split.length; part++)
		    property1 += "." + split[part];
	} else {
	    property1 = term1;
	}

	// get second property label
	String term2 = p.getTerm2();
	if (term2.contains(".")) {
	    String split[] = term2.split("\\.");
	    property2 = split[1];
	    if (split.length >= 2)
		for (int part = 2; part < split.length; part++)
		    property2 += "." + split[part];
	} else {
	    property2 = term2;
	}

	// get number of dimensions we are dealing with
	int dimensions = property2.split("\\|").length;
	// logger.info("Comparing " + property1 + " and " + property2 + ", ergo
	// " + dimensions + " dimensions");
	// important. The Blocking module takes care of the transformation from
	// similarity to
	// distance threshold. Central for finding the right blocks and might
	// differ from blocker
	// to blocker.
	// logger.info("Granularity is set to " + granularity);
	BlockingModule generator = BlockingFactory.getBlockingModule(property2, p.op, threshold, granularity);

	// initialize the measure for similarity computation
	SpaceMeasure measure = SpaceMeasureFactory.getMeasure(p.op, dimensions);

	// logger.info("Getting hypercubes for target.");
	// compute blockid for each of the elements of the target
	// implement our simple yet efficient blocking approach
	ArrayList<ArrayList<Integer>> blockIds;
	for (String key : target.getAllUris()) {
	    blockIds = generator.getAllBlockIds(target.getInstance(key));
	    for (int ids = 0; ids < blockIds.size(); ids++) {
		if (!targetBlocks.containsKey(blockIds.get(ids))) {
		    targetBlocks.put(blockIds.get(ids), new TreeSet<String>());
		}
		targetBlocks.get(blockIds.get(ids)).add(key);
	    }
	}
	// logger.info("Generated "+targetBlocks.size()+" hypercubes for
	// target.");
	// logger.info("Computing links ...");

	ArrayList<ArrayList<Integer>> blocksToCompare;
	// comparison
	TreeSet<String> uris;
	double sim;
	// necessary to compute RRR
	int comparisons = 0;
	int necessaryComparisons = 0;

	int counter = 0, size = source.getAllUris().size();
	for (String sourceInstanceUri : source.getAllUris()) {
	    counter++;
	    if (counter % 1000 == 0) {
		// logger.info("Processed " + (counter * 100 / size) + "% of the
		// links");
		// get key

	    }
	    // logger.info("Getting "+property1+" from "+sourceInstanceUri);
	    blockIds = generator.getAllSourceIds(source.getInstance(sourceInstanceUri), property1);
	    // logger.info("BlockId for "+sourceInstanceUri+" is "+blockId);
	    // for all blocks in [-1, +1] in each dimension compute similarities
	    // and store them
	    for (int ids = 0; ids < blockIds.size(); ids++) {
		blocksToCompare = generator.getBlocksToCompare(blockIds.get(ids));

		// logger.info(sourceInstanceUri+" is to compare with blocks
		// "+blocksToCompare);
		for (int index = 0; index < blocksToCompare.size(); index++) {
		    if (targetBlocks.containsKey(blocksToCompare.get(index))) {
			uris = targetBlocks.get(blocksToCompare.get(index));
			for (String targetInstanceUri : uris) {
			    sim = measure.getSimilarity(source.getInstance(sourceInstanceUri),
				    target.getInstance(targetInstanceUri), property1, property2);
			    comparisons++;
			    if (sim >= threshold) {
				mapping.add(sourceInstanceUri, targetInstanceUri, sim);
				necessaryComparisons++;
			    }
			}
		    }
		}
	    }
	}
	// logger.info("Cmin = "+necessaryComparisons+"; C = "+comparisons);
	return mapping;
    }

    // just for testing
    public static void main(String args[]) {
	MemoryCache source = new MemoryCache();
	MemoryCache target = new MemoryCache();

	target.addTriple("0", "lat", "0");
	target.addTriple("0", "lon", "0");

	target.addTriple("1", "lat", "4");
	target.addTriple("1", "lon", "4");

	target.addTriple("2", "lat", "4");
	target.addTriple("2", "lon", "3");

	target.addTriple("3", "lat", "3");
	target.addTriple("3", "lon", "4");

	source.addTriple("4", "lat", "2");
	source.addTriple("4", "lon", "2");

	source.addTriple("5", "lat", "5");
	source.addTriple("5", "lon", "2");

	TotalOrderBlockingMapper bm = new TotalOrderBlockingMapper();

	// System.out.println("Mapping = "+bm.getMapping(source, target, "?x",
	// "?y", "euclidean(x.lat|lon, y.lat|lon)", 0.5));
    }

    // need to change this
    public double getRuntimeApproximation(int sourceSize, int targetSize, double threshold, Language language) {
	if (language.equals(Language.DE)) {
	    // error = 667.22
	    return 16.27 + 5.1 * sourceSize + 4.9 * targetSize - 23.44 * threshold;
	} else {
	    // error = 5.45
	    return 200 + 0.005 * (sourceSize + targetSize) - 56.4 * threshold;
	}
    }

    public double getMappingSizeApproximation(int sourceSize, int targetSize, double threshold, Language language) {
	if (language.equals(Language.DE)) {
	    // error = 667.22
	    return 2333 + 0.14 * sourceSize + 0.14 * targetSize - 3905 * threshold;
	} else {
	    // error = 5.45
	    return 0.006 * (sourceSize + targetSize) - 134.2 * threshold;
	}
    }
}
