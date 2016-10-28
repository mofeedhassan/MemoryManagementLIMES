package de.uni_leipzig.simba.allenalgebra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class Baseline {
    protected static final Logger logger = Logger.getLogger(Baseline.class.getName());
    public long maxTime = 86400000L;
    public int counter = 0;
    public long duration = 0l;
    public long totalTime = 0l;
    // public Mapping m = new Mapping();

    public void runEqual(HybridCache source, HybridCache target) {
	long start = System.currentTimeMillis();
	long end = start + maxTime;
	for (Instance sourceInstance : source.getAllInstances()) {
	    for (Instance targetInstance : target.getAllInstances()) {
		String beginDateSource = sourceInstance.getProperty("beginDate").first();
		String endDateSource = sourceInstance.getProperty("endDate").first();

		String beginDateTarget = targetInstance.getProperty("beginDate").first();
		String endDateTarget = targetInstance.getProperty("endDate").first();

		if (beginDateSource.equals(beginDateTarget) && endDateSource.equals(endDateTarget)) {
		    counter++;
		    // m.add(sourceInstance.getUri(), targetInstance.getUri(),
		    // 1);
		}
		duration = System.currentTimeMillis();
		if (duration >= end) {
		    break;
		}
	    }
	    duration = System.currentTimeMillis();
	    if (duration >= end) {
		break;
	    }
	}
	totalTime = duration - start;
	//logger.info(duration);
	//logger.info(start);
	//logger.info(totalTime);
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

	HybridCache source = HybridCache.getData(cR.sourceInfo);
	HybridCache target = HybridCache.getData(cR.targetInfo);

	Baseline b = new Baseline();
	b.runEqual(source, target);

	baseFolder += "SAKE_DATA/" + resultsFolder;
	File dirName = new File(baseFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}
	baseFolder += "/" + "BASELINE";
	dirName = new File(baseFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}
	logger.info(b.counter);
	logger.info(b.totalTime);
	String statistics = dirName + "/" + "statistics.csv";
	String header = "Links" + "\t" + "Duration" + "\n";
	File f = new File(statistics);
	FileWriter writer = null;
	try {
	    writer = new FileWriter(f);
	    DecimalFormat df = new DecimalFormat("#");
	    df = new DecimalFormat("#");
	    df.setMaximumFractionDigits(50);
	    writer.write(header);
	    writer.write(df.format(b.counter));
	    writer.write("\t");
	    writer.write(df.format(b.totalTime));
	    writer.write("\n");
	    writer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
}
