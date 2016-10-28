package de.uni_leipzig.simba.allenalgebra.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
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
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class ConstructData {
    protected static final Logger logger = Logger.getLogger(ConstructData.class.getName());
    static HybridCache source;

    private static void constructDATA(String output, String sparqlQuery) throws IOException {

	String endPoint = "http://lsq.aksw.org/sparql";
	// String graph = "http://dbpedia.org";
	// use this later
	// String output = "resources/SAKE_DATA/queries30.ttl";
	// String constructQryStr = null;
	String constructQryStr = null;
	FileReader reader = null;

	int len;
	char[] chr = new char[4096];
	final StringBuffer bufferSource = new StringBuffer();
	reader = new FileReader(sparqlQuery);
	try {
	    while ((len = reader.read(chr)) > 0) {
		bufferSource.append(chr, 0, len);
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (reader != null) {
		reader.close();
	    }
	}

	constructQryStr = bufferSource.toString();
	logger.info("Using " + endPoint + " to run construct query:\n" + constructQryStr);

	org.aksw.jena_sparql_api.core.QueryExecutionFactory qef = FluentQueryExecutionFactory.http(endPoint, "")
		.config().withPagination(1000).end().create();

	long timeToLive = 24l * 60l * 60l * 1000l;
	// Add delay in order to be nice to the remote server (delay in
	// milli seconds)

	QueryExecutionFactoryHttp foo = qef.unwrap(QueryExecutionFactoryHttp.class);
	logger.info(foo);
	// Create a QueryExecution object from a query string ...
	logger.info("createQueryExecution");
	QueryExecution qe = qef.createQueryExecution(constructQryStr);
	// and run it.
	logger.info("execSelect");
	Model m = qe.execConstruct();
	logger.info(m.size());
	FileWriter fileWriter = new FileWriter(output);
	m.write(fileWriter, "TTL");
    }

    public static void refactorData(String datasetFolder) {

	String outputFile = datasetFolder + "/" + "queries.ttl";
	FileWriter writer = null;

	try {
	    writer = new FileWriter(outputFile, false);
	    Model m = ModelFactory.createDefaultModel();
	    Property propertyBegin = ResourceFactory.createProperty("http://www.myOntology.com#beginDate");
	    Property propertyEnd = ResourceFactory.createProperty("http://www.myOntology.com#endDate");
	    Property propertySilk = ResourceFactory.createProperty("http://www.myOntology.com#silkProperty");
	    Resource class1 = ResourceFactory.createResource("http://www.myOntology.com#" + "Query");
	    logger.info(source.size());
	    for (Instance instance : source.getAllInstances()) {
		TreeSet<String> beginTimes = instance.getProperty("beginDate");
		TreeSet<String> durationTimes = instance.getProperty("duration");

		String durationTime = durationTimes.first();
		String beginTime = beginTimes.first();
		// logger.info(beginTimes.first() + "#" + endTime);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		Date date = df.parse(beginTime);

		// get epoch time from begin time
		long epoch = date.getTime();
		// convert end time to the appropriate format
		Date newBeginDate = new Date(epoch);
		Date newEndDate;

		if (Long.parseLong(durationTime) < 1000L)
		    newEndDate = new Date(epoch + Long.parseLong(durationTime) + 1000L);
		else
		    newEndDate = new Date(epoch + Long.parseLong(durationTime));

		SimpleDateFormat newDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		newDf.setTimeZone(TimeZone.getTimeZone("GMT"));
		beginTime = newDf.format(newBeginDate);
		String endTime = newDf.format(newEndDate);

		RDFNode beginDate = ResourceFactory.createTypedLiteral(beginTime, XSDDatatype.XSDdateTime);
		RDFNode endDate = ResourceFactory.createTypedLiteral(endTime, XSDDatatype.XSDdateTime);
		String temp = "[" + beginTime.split("\\+")[0] + "," + endTime.split("\\+")[0] + ")";
		RDFNode silkValue = ResourceFactory.createTypedLiteral(temp);

		Resource newResource = ResourceFactory.createResource("http://www.myOntology.com/"
			+ instance.getUri().substring(instance.getUri().lastIndexOf('/') + 1));
		m.add(newResource, RDF.type, class1);
		m.add(newResource, propertyBegin, beginDate);
		m.add(newResource, propertyEnd, endDate);
		m.add(newResource, propertySilk, silkValue);

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
	} catch (ParseException | IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) throws Exception {
	try {
	    PatternLayout layout = new PatternLayout("%d{dd.MM.yyyy HH:mm:ss} %-5p [%t] %l: %m%n");
	    FileAppender fileAppender = new FileAppender(layout, ("/test").replaceAll(".xml", "") + ".log", false);
	    fileAppender.setLayout(layout);
	    logger.removeAllAppenders();
	    logger.addAppender(fileAppender);
	} catch (Exception e) {
	    logger.warn("Exception creating file appender.");
	}
	if (args.length < 3) {
	    logger.warn(
		    "Argument 1: Name of folder for the new data.\n Argument 2: CONSTRUCT configuration file.\n Argument 3: sparql file.\n");
	    System.exit(1);
	}
	// base folder, configuration file
	ConfigReader cR = new RDFConfigReader();
	String baseFolder = "resources/";
	// where to place the new dataset resources/data/new_name
	String datasetFolderSource = "resources/data/" + args[0];
	File dirName = new File(datasetFolderSource);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}
	// configuration file TO CONSTRUCT THE QUERIES: queries_construct.ttl
	String configFile = baseFolder + args[1];
	//the query for construct
	String query = baseFolder + args[2];
	logger.info("Config of construct file "+configFile);
	logger.info("Construct Query file "+query);
	logger.info("File to save the output of construct query "+datasetFolderSource + "/" + args[0] + "Original.ttl");
	
	constructDATA(datasetFolderSource + "/" + args[0] + "Original.ttl", query);

	cR.validateAndRead(configFile);
	source = HybridCache.getData(cR.sourceInfo);
	refactorData(datasetFolderSource);
    }
}
