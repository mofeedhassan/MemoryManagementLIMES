package de.uni_leipzig.simba.allenalgebra.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import de.uni_leipzig.simba.allenalgebra.mappers.atomic.AtomicAllenAlgebraMapper;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class ArtificialData {
    protected static final Logger logger = Logger.getLogger(ArtificialData.class.getName());
    public static TreeMap<String, Set<Instance>> sources = new TreeMap<String, Set<Instance>>();
    public static TreeMap<String, Set<Instance>> newInstances = new TreeMap<String, Set<Instance>>();
    public static Set<String> newUris = new TreeSet<String>();

    static HybridCache source;
    public static double population;

    public static void orderCache(Cache cache, String expression) {

	Parser p = new Parser(expression, 0.0d);
	String propertyBegin = AtomicAllenAlgebraMapper.getBeginProperty(p.getTerm1());
	String propertyEnd = AtomicAllenAlgebraMapper.getEndProperty(p.getTerm1());

	for (Instance instance : cache.getAllInstances()) {
	    TreeSet<String> beginTimes = instance.getProperty(propertyBegin);
	    TreeSet<String> endTimes = instance.getProperty(propertyEnd);

	    try {
		String endTime = endTimes.first();
		// logger.info(beginTimes.first() + "#" + endTime);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		Date date;
		date = df.parse(endTime);
		// get epoch time from end time
		long epoch = date.getTime();
		// if begin and end time are the same, then increase end time by
		// 1 second
		if (beginTimes.first().equals(endTime)) {
		    epoch += 1000L;
		}
		// convert new end time to the appropriate format
		Date newDate = new Date(epoch);
		SimpleDateFormat newDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		newDf.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
		String newEndTime = newDf.format(newDate);

		String key = beginTimes.first() + "#" + newEndTime;
		// logger.info(key);
		// logger.info("------------------------------");
		if (!sources.containsKey(key))
		    sources.put(key, new TreeSet<Instance>());
		sources.get(key).add(instance);

	    } catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

    }

    public static void increasePopulation(String datasetFolder) {

	File dirName = new File(datasetFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}
	int t = (int) (source.getAllInstances().size() * population);
	logger.info(t);
	String outputFile = datasetFolder + "/" + String.valueOf(t) + "events.ttl";
	FileWriter writer = null;
	try {
	    writer = new FileWriter(outputFile, false);
	    Model m = ModelFactory.createDefaultModel();
	    Property propertyBegin = ResourceFactory.createProperty("http://www.myOntology.com#beginDate");
	    Property propertyEnd = ResourceFactory.createProperty("http://www.myOntology.com#endDate");
	    Property propertySilk = ResourceFactory.createProperty("http://www.myOntology.com#silkProperty");
	    // Property propertyMachineID =
	    // ResourceFactory.createProperty("http://www.myOntology.com#machineID");
	    Resource class1 = ResourceFactory.createResource("http://www.myOntology.com#" + "Event");

	    for (Map.Entry<String, Set<Instance>> entry : sources.entrySet()) {
		String key = entry.getKey();
		Set<Instance> instances = entry.getValue();
		String begin = key.split("#")[0];
		String end = key.split("#")[1];
		for (Instance instance : instances) {
		    RDFNode beginDate = ResourceFactory.createTypedLiteral(begin, XSDDatatype.XSDdateTime);
		    RDFNode endDate = ResourceFactory.createTypedLiteral(end, XSDDatatype.XSDdateTime);
		    String temp = "[" + begin.split("\\+")[0] + "," + end.split("\\+")[0] + ")";
		    RDFNode silkValue = ResourceFactory.createTypedLiteral(temp);
		    // RDFNode machineID =
		    // ResourceFactory.createTypedLiteral(instance.getProperty("machineID").first(),
		    // XSDDatatype.XSDstring);

		    for (int i = 0; i < population; i++) {
			String newUri = null;
			do {
			    newUri = instance.getUri() + RandomStringUtils.random((int) population, true, true);
			} while (newUris.contains(newUri));
			newUris.add(newUri);

			Instance newInstance = new Instance(newUri);
			newInstance.addProperty("beginDate", begin);
			newInstance.addProperty("endDate", end);
			newInstance.addProperty("silkProperty", temp);
			// newInstance.addProperty("machineID",
			// instance.getProperty("machineID").first());
			String newKey = begin + "#" + end;
			if (!newInstances.containsKey(newKey)) {
			    newInstances.put(newKey, new TreeSet<Instance>());
			}
			newInstances.get(newKey).add(newInstance);

			Resource newResource = ResourceFactory.createResource(newUri);
			m.add(newResource, RDF.type, class1);
			m.add(newResource, propertyBegin, beginDate);
			m.add(newResource, propertyEnd, endDate);
			m.add(newResource, propertySilk, silkValue);
			// m.add(newResource, propertyMachineID, machineID);
		    }
		}

	    }
	    m.write(writer, "TTL");
	    logger.info("Old Model size: " + source.getAllInstances().size());
	    StmtIterator triples = m.listStatements();
	    Set<String> temp = new TreeSet<String>();
	    while (triples.hasNext()) {
		Statement triple = triples.next();
		temp.add(triple.getSubject().toString());
	    }
	    logger.info("New Model size: " + temp.size());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	/*
	 * for(Entry<String, Set<Instance>> entry: newInstances.entrySet()){
	 * logger.info(entry.getKey()); logger.info(entry.getValue().size());
	 * logger.info(sources.get(entry.getKey()));
	 * 
	 * }
	 */
    }

    public static void decreasePopulation(String datasetFolder) {

	File dirName = new File(datasetFolder);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}
	int t = (int) (source.getAllInstances().size() * population);
	logger.info(t);
	String outputFile = datasetFolder + "/" + String.valueOf(t) + "events.ttl";
	FileWriter writer = null;
	try {
	    writer = new FileWriter(outputFile, false);
	    Model m = ModelFactory.createDefaultModel();
	    Property propertyBegin = ResourceFactory.createProperty("http://www.myOntology.com#beginDate");
	    Property propertyEnd = ResourceFactory.createProperty("http://www.myOntology.com#endDate");
	    Property propertySilk = ResourceFactory.createProperty("http://www.myOntology.com#silkProperty");

	    // Property propertyMachineID =
	    // ResourceFactory.createProperty("http://www.myOntology.com#machineID");
	    Resource class1 = ResourceFactory.createResource("http://www.myOntology.com#" + "Event");
	    int counter = 0;
	    for (Map.Entry<String, Set<Instance>> entry : sources.entrySet()) {
		String key = entry.getKey();
		Set<Instance> instances = entry.getValue();
		String begin = key.split("#")[0];
		String end = key.split("#")[1];

		double tempPopulation = Math.round(population * instances.size());

		if (tempPopulation == 0.0d)
		    tempPopulation = 1.0d;
		logger.info(tempPopulation);
                if(counter == 21)
                      break;
		if (key.split("#")[0].equals(key.split("#")[1]))
		    logger.info("something's wrong");
		RDFNode beginDate = ResourceFactory.createTypedLiteral(begin, XSDDatatype.XSDdateTime);
		RDFNode endDate = ResourceFactory.createTypedLiteral(end, XSDDatatype.XSDdateTime);
		String temp = "[" + begin.split("\\+")[0] + "," + end.split("\\+")[0] + ")";
		RDFNode silkValue = ResourceFactory.createTypedLiteral(temp);

		for (int i = 0; i < (int) tempPopulation; i++) {
		    String newUri = null;
		    do {
			newUri = instances.iterator().next().getUri() + String.valueOf(i);
		    } while (newUris.contains(newUri));
		    newUris.add(newUri);
                    counter++;
                    //if(counter == 21)
                    //   break;
		    Instance newInstance = new Instance(newUri);
		    newInstance.addProperty("beginDate", begin);
		    newInstance.addProperty("endDate", end);
		    newInstance.addProperty("silkProperty", temp);

		    String newKey = key.split("#")[0] + "#" + key.split("#")[1];
		    if (!newInstances.containsKey(newKey)) {
			newInstances.put(newKey, new TreeSet<Instance>());
		    }
		    newInstances.get(newKey).add(newInstance);

		    Resource newResource = ResourceFactory.createResource(newUri);
		    m.add(newResource, RDF.type, class1);
		    m.add(newResource, propertyBegin, beginDate);
		    m.add(newResource, propertyEnd, endDate);
		    m.add(newResource, propertySilk, silkValue);
		}
	    }

	    m.write(writer, "TTL");
	    logger.info("Old Model size: " + source.getAllInstances().size());
	    StmtIterator triples = m.listStatements();
	    Set<String> temp = new TreeSet<String>();
	    while (triples.hasNext()) {
		Statement triple = triples.next();
		temp.add(triple.getSubject().toString());
	    }
	    logger.info("New Model size: " + temp.size());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	/*
	 * for(Entry<String, Set<Instance>> entry: newInstances.entrySet()){
	 * logger.info(entry.getKey()); logger.info(entry.getValue().size());
	 * logger.info(sources.get(entry.getKey()));
	 * 
	 * }
	 */
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
	    logger.warn(
		    "Argument 1: Name of folder for the new data.\n Argument 2: configuration file.\n Argument 3: 10 or 100");
	    System.exit(1);
	}
	// base folder, configuration file
	ConfigReader cR = new RDFConfigReader();
	String baseFolder = "resources/";
	// where to place the new dataset resources/new_datasets
	String datasetFolderSource = "resources/" + args[0];
	// configuration file: where the original data is resources/sake.ttl
	String configFile = args[1];
	population = Double.valueOf(args[2]);
	logger.info(population);
	cR.validateAndRead(baseFolder + configFile);

	source = HybridCache.getData(cR.sourceInfo);

	orderCache(source, cR.metricExpression);
	if (population >= 1.0d)
	    increasePopulation(datasetFolderSource);
	else
	    decreasePopulation(datasetFolderSource);
    }
}
