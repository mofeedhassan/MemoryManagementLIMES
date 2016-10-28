package de.uni_leipzig.simba.allenalgebra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraFactory;
import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.BeginBegin;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.BeginEnd;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.EndBegin;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.EndEnd;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class AllenAlgebraLinkerTimeEfficiency {
    protected static final Logger logger = Logger.getLogger(AllenAlgebraLinkerTimeEfficiency.class.getName());
    ArrayList<TreeMap<String, Set<String>>> all = new ArrayList<TreeMap<String, Set<String>>>();
    public ArrayList<Long> statisticsAtomic = new ArrayList<Long>();
    public ArrayList<Long> statisticsComplex = new ArrayList<Long>();
    public ArrayList<Long> statisticsComplexExtra = new ArrayList<Long>();
    protected String[] mapperNames = { "StrictlyBefore", "StrictlyAfter", "DirectlyBefore", "DirectlyAfter",
	    "BeginAfterEndTogether", "BeginBeforeEndTogether", "BeginTogetherEndBefore", "BeginTogetherEndAfter",
	    "During", "DuringReverse", "Equal", "OverlapAfter", "OverlapBefore" };
    public ArrayList<Integer> linkSize = new ArrayList<Integer>();
    
    public void init(Cache source, Cache target, String expression, double threshold) {
	EndEnd ee = new EndEnd();
	BeginBegin bb = new BeginBegin();
	BeginEnd be = new BeginEnd();
	EndBegin eb = new EndBegin();

	long conBBstart = System.currentTimeMillis();
	all.add(bb.getConcurrentEvents(source, target, expression));
	long conBBend = System.currentTimeMillis();
	statisticsAtomic.add(0, (conBBend - conBBstart));
	logger.info("BB0");
	long preBBstart = System.currentTimeMillis();
	all.add(bb.getPredecessorEvents(source, target, expression));
	long preBBend = System.currentTimeMillis();
	statisticsAtomic.add(1, (preBBend - preBBstart));
	logger.info("BB1");

	long conBEstart = System.currentTimeMillis();
	all.add(be.getConcurrentEvents(source, target, expression));
	long conBEend = System.currentTimeMillis();
	statisticsAtomic.add(2, (conBEend - conBEstart));
	logger.info("BE0");

	long preBEstart = System.currentTimeMillis();
	all.add(be.getPredecessorEvents(source, target, expression));
	long preBEend = System.currentTimeMillis();
	statisticsAtomic.add(3, (preBEend - preBEstart));
	logger.info("BE1");

	long conEBstart = System.currentTimeMillis();
	all.add(eb.getConcurrentEvents(source, target, expression));
	long conEBend = System.currentTimeMillis();
	statisticsAtomic.add(4, (conEBend - conEBstart));
	logger.info("EB0");

	long preEBstart = System.currentTimeMillis();
	all.add(eb.getPredecessorEvents(source, target, expression));
	long preEBend = System.currentTimeMillis();
	statisticsAtomic.add(5, (preEBend - preEBstart));
	logger.info("EB1");

	long conEEstart = System.currentTimeMillis();
	all.add(ee.getConcurrentEvents(source, target, expression));
	long conEEend = System.currentTimeMillis();
	statisticsAtomic.add(6, (conEEend - conEEstart));
	logger.info("EE0");

	long preEEstart = System.currentTimeMillis();
	all.add(ee.getPredecessorEvents(source, target, expression));
	long preEEend = System.currentTimeMillis();
	statisticsAtomic.add(7, (preEEend - preEEstart));
	logger.info("EE1");
    }
    private void getMapping(int iteration, HybridCache source, HybridCache target, String expression,
	    double threshold, File dirName) {

	init(source, target, expression, threshold);
	int mapperCounter = 0;

	for (String mapperName : mapperNames) {
	    logger.info(mapperName);
	    AllenAlgebraMapper mapper = AllenAlgebraFactory.getMapper(mapperName);
	    
	    if(mapperName.equals("StrictlyAfter")){
		mapper.setSourceCache(source);
		mapper.setTargetCache(target);
	    }
	    // collect mapper's required lists
	    ArrayList<TreeMap<String, Set<String>>> maps = new ArrayList<TreeMap<String, Set<String>>>();
	    long durationExtra = 0L;
	    String outputFile = dirName + "/" + mapperName + ".txt";

	    // long start = System.currentTimeMillis();
	    for (int req : mapper.getRequiredAtomicRelations()) {
		maps.add(all.get(req));
	    }
	    
	    // long end = System.currentTimeMillis();
	    // durationExtra += end - start;

	    mapper.setIteration(iteration);
	    mapper.setFilePath(outputFile);
	    mapper.getMappingTimeEfficiency(maps);

	    
	    for (int req : mapper.getRequiredAtomicRelations()) {
		durationExtra += statisticsAtomic.get(req);
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
	    PatternLayout layout = new PatternLayout("%d{dd}.MM.yyyy HH:mm:ss} %-5p [%t] %l: %m%n");
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
	baseFolder += "/" + "ALLEN_ALGEBRA_TIME";
	dirName = new File(baseFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}

	for (int iteration = 0; iteration < 3; iteration++) {
	    long start = System.currentTimeMillis();
	    AllenAlgebraLinkerTimeEfficiency linker = new AllenAlgebraLinkerTimeEfficiency();
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
		    header = "BB0\t" + "BB1\t" + "BE0\t" + "BE1\t" + "EB0\t" + "EB1\t" + "EE0\t" + "EE1\t";

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
		for (int i = 0; i < 8; i++) {
		    writer.write(df.format(linker.statisticsAtomic.get(i)));
		    totalAll += linker.statisticsAtomic.get(i);
		    writer.write("\t");
		}

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
