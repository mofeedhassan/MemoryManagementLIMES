package de.uni_leipzig.simba.allenalgebra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraFactory;
import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.AtomicAllenAlgebraMapper;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class AllenAlgebraLinkerMemoryEfficiency {
    protected static final Logger logger = Logger.getLogger(AllenAlgebraLinkerMemoryEfficiency.class.getName());
    // source ordered by beginDate = 0, source ordered by endDate = 1
    protected ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
    // target ordered by beginDate = 0, target ordered by endDate = 1
    protected ArrayList<TreeMap<String, Set<Instance>>> targetBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();

    protected String[] mapperNames = { "StrictlyBefore", "StrictlyAfter", "DirectlyBefore", "DirectlyAfter",
	    "BeginAfterEndTogether", "BeginBeforeEndTogether", "BeginTogetherEndBefore", "BeginTogetherEndAfter",
	    "During", "DuringReverse", "Equal", "OverlapAfter", "OverlapBefore" };
    public ArrayList<Long> statisticsSource = new ArrayList<Long>();
    public ArrayList<Long> statisticsTarget = new ArrayList<Long>();
    public ArrayList<Long> statisticsComplex = new ArrayList<Long>();
    public ArrayList<Long> statisticsComplexExtra = new ArrayList<Long>();
    public ArrayList<Integer> linkSize = new ArrayList<Integer>();

    public void init(Cache source, Cache target, String expression, double threshold) {

	long startSourceBB = System.currentTimeMillis();
	sourceBlocks.add(0, AtomicAllenAlgebraMapper.orderByBeginDate(source, expression));
	long endSourceBB = System.currentTimeMillis();
	statisticsSource.add(0, (endSourceBB - startSourceBB));
	
	long startSourceEE = System.currentTimeMillis();
	sourceBlocks.add(1, AtomicAllenAlgebraMapper.orderByEndDate(source, expression));
	long endSourceEE = System.currentTimeMillis();
	statisticsSource.add(1, (endSourceEE - startSourceEE));
	
	long startTargetBB = System.currentTimeMillis();
	targetBlocks.add(0, AtomicAllenAlgebraMapper.orderByBeginDate(target, expression));
	long endTargetBB = System.currentTimeMillis();
	statisticsTarget.add(0, (endTargetBB - startTargetBB));

	long startTargetEE = System.currentTimeMillis();
	targetBlocks.add(1, AtomicAllenAlgebraMapper.orderByEndDate(target, expression));
	long endTargetEE = System.currentTimeMillis();
	statisticsTarget.add(1, (endTargetEE - startTargetEE));

    }

    public void getMapping(int iteration, Cache source, Cache target, String expression, double threshold,
	    File dirName) {
	init(source, target, expression, threshold);
	int mapperCounter = 0;

	for (String mapperName : mapperNames) {
	    logger.info(mapperName);
	    AllenAlgebraMapper mapper = AllenAlgebraFactory.getMapper(mapperName);

	    if (mapperName.equals("StrictlyAfter")) {
		mapper.setSourceCache(source);
		mapper.setTargetCache(target);
	    }
	    // collect mapper's required lists
	    ArrayList<TreeMap<String, Set<Instance>>> mapperSourceBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
	    ArrayList<TreeMap<String, Set<Instance>>> mapperTargetBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
	    long durationExtra = 0L;
	    String outputFile = dirName + "/" + mapperName + ".txt";

	    // long start = System.currentTimeMillis();
	    for (int req : mapper.getRequiredAtomicRelationsSource()) {
		mapperSourceBlocks.add(sourceBlocks.get(req));
	    }
	    for (int req : mapper.getRequiredAtomicRelationsTarget()) {
		mapperTargetBlocks.add(targetBlocks.get(req));
	    }
	    // long end = System.currentTimeMillis();
	    // durationExtra += end - start;

	    mapper.setIteration(iteration);
	    mapper.setFilePath(outputFile);
	    mapper.getMappingMemoryEfficiency(mapperSourceBlocks, mapperTargetBlocks);

	    for (int req : mapper.getRequiredAtomicRelationsSource()) {
		durationExtra += statisticsSource.get(req);
	    }
	    for (int req : mapper.getRequiredAtomicRelationsTarget()) {
		durationExtra += statisticsTarget.get(req);
	    }
	    linkSize.add(mapper.getSize());
	    logger.info("Mapper Duration: " + mapper.getDuration());
	    statisticsComplex.add(mapperCounter, mapper.getDuration());
	    statisticsComplexExtra.add(mapperCounter, mapper.getDuration() + durationExtra);

	    mapperCounter++;
	}
    }

    public static void main(String args[]) {
	try {
	    PatternLayout layout = new PatternLayout("%d{dd.MM.yyyy HH:mm:ss} %-5p [%t] %l: %m%n");
	    FileAppender fileAppender = new FileAppender(layout, ("/test").replaceAll(".xml", "") + ".log", false);
	    fileAppender.setLayout(layout);
	    logger.removeAllAppenders();
	    logger.addAppender(fileAppender);
	} catch (Exception e) {
	    logger.warn("Exception creating file appender.");
	}
	if (args.length < 2) {
	    logger.warn("Argument 1: Results folder.\n Argument 2: configuration file");
	    System.exit(1);
	}
	// base folder, configuration file
	ConfigReader cR = new RDFConfigReader();
	String baseFolder = "resources/";
	// where to store results
	String resultsFolder = args[0];
	// configuration file
	String configFile = args[1];
	cR.validateAndRead(baseFolder + configFile);

	HybridCache source = HybridCache.getDataFromGraph(cR.sourceInfo);
	HybridCache target = HybridCache.getDataFromGraph(cR.targetInfo);

	baseFolder += "SAKE_DATA/" + resultsFolder;
	File dirName = new File(baseFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}
	baseFolder += "/" + "ALLEN_ALGEBRA_MEMORY";
	dirName = new File(baseFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}

	for (int iteration = 0; iteration < 3; iteration++) {
	    long start = System.currentTimeMillis();
	    AllenAlgebraLinkerMemoryEfficiency linker = new AllenAlgebraLinkerMemoryEfficiency();
	    linker.getMapping(iteration, source, target, cR.metricExpression, cR.acceptanceThreshold, dirName);
	    String statistics = dirName + "/" + "statistics.csv";
	    String links = dirName + "/" + "links.csv";

	    long end = System.currentTimeMillis();

	    String header = null;
	    File f = new File(statistics);
	    FileWriter writer = null;
	    try {
		if (f.exists())
		    writer = new FileWriter(f, true);
		else {
		    writer = new FileWriter(f);
		    header = "Source sorted by beginDate\t" + "Source sorted by beginDate\t"
			    + "Target sorted by beginDate\t" + "Target sorted by endDate\t";

		    for (int i = 0; i < linker.mapperNames.length; i++) {
			header += linker.mapperNames[i];
			header += "\t";
			header += linker.mapperNames[i] + " Extra";
			header += "\t";

		    }
		    header += "Total Runtime of Complex AllenAlgebra Relations without the atomic relations" + "\t"
			    + "Total Runtime of Complex AllenAlgebra Relations if run independently" + "\t"
			    + "Total Runtime of all relations" + "\t" + "Total Runtime of experiment" + "\n";
		    writer.write(header);
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    long totalOnlyComplex = 0l;
	    long totalIndComplex = 0l;
	    long totalAll = 0l;

	    try {

		DecimalFormat df = new DecimalFormat("#");
		df = new DecimalFormat("#");
		df.setMaximumFractionDigits(50);
		// Source sorted by beginDate
		writer.write(df.format(linker.statisticsSource.get(0)));
		totalAll += linker.statisticsSource.get(0);
		writer.write("\t");
		// Source sorted by beginDate
		writer.write(df.format(linker.statisticsSource.get(1)));
		totalAll += linker.statisticsSource.get(1);
		writer.write("\t");
		// Target sorted by beginDate
		writer.write(df.format(linker.statisticsTarget.get(0)));
		totalAll += linker.statisticsTarget.get(0);
		writer.write("\t");
		// Target sorted by endDate
		writer.write(df.format(linker.statisticsTarget.get(1)));
		totalAll += linker.statisticsTarget.get(1);

		writer.write("\t");

		for (int i = 0; i < linker.statisticsComplex.size(); i++) {
		    writer.write(df.format(linker.statisticsComplex.get(i)));
		    writer.write("\t");
		    writer.write(df.format(linker.statisticsComplexExtra.get(i)));
		    writer.write("\t");

		    totalAll += linker.statisticsComplex.get(i);
		    totalOnlyComplex += linker.statisticsComplex.get(i);
		    totalIndComplex += linker.statisticsComplexExtra.get(i);
		}
		writer.write(df.format(totalOnlyComplex));
		writer.write("\t");
		writer.write(df.format(totalIndComplex));
		writer.write("\t");
		writer.write(df.format(totalAll));
		writer.write("\t");
		writer.write(df.format(end - start));
		writer.write("\n");

		writer.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    String headerLinks = new String();
	    File fLinks = new File(links);
	    FileWriter writerLinks = null;
	    try {
		if (fLinks.exists())
		    writerLinks = new FileWriter(fLinks, true);
		else {
		    writerLinks = new FileWriter(fLinks);

		    for (int i = 0; i < linker.mapperNames.length; i++) {
			headerLinks += linker.mapperNames[i];
			headerLinks += "\t";
		    }
		    headerLinks += "\n";

		    writerLinks.write(headerLinks);
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    try {

		DecimalFormat df = new DecimalFormat("#");
		df = new DecimalFormat("#");
		df.setMaximumFractionDigits(50);

		for (int i = 0; i < linker.linkSize.size(); i++) {
		    writerLinks.write(df.format(linker.linkSize.get(i)));
		    writerLinks.write("\t");
		}
		writerLinks.write("\n");

		writerLinks.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

    }

}
