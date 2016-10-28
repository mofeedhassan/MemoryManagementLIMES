package de.uni_leipzig.simba.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.Serializer;
import de.uni_leipzig.simba.io.SerializerFactory;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;
import de.uni_leipzig.simba.mapper.AtomicMapper;
import de.uni_leipzig.simba.measures.MeasureFactory;

public class EventLinker {
    static Logger logger = Logger.getLogger("LIMES");

    public static void main(String args[]) {
	// base folder, configuration file
	if (args.length < 2) {
	    logger.warn("Argument 1: Basefolder.\n Argument 2: configuration file.\n Argument 3: output file");
	    System.exit(1);
	}
	ConfigReader cR = new RDFConfigReader();
	String baseFolder = args[0];
	String configFile = args[1];
	cR.validateAndRead(baseFolder + "/"+ configFile);
	//String outputFile = baseFolder + "SAKE_DATA/" + cR.metricExpression + ".ttl";
	String outputFile = baseFolder + "/"+ args[2];
	
	HybridCache source = HybridCache.getData(cR.sourceInfo);
	HybridCache target = HybridCache.getData(cR.targetInfo);

	logger.info(source);
	logger.info(target);
	logger.info(cR.metricExpression);
	logger.info(cR.acceptanceThreshold);

	AtomicMapper mapper = MeasureFactory.getMapper(cR.metricExpression);
	long start = System.currentTimeMillis();
	Mapping m = mapper.getMapping(source, target, "?x", "?y", cR.metricExpression, cR.acceptanceThreshold);
	long end = System.currentTimeMillis();
	logger.info("Total runtime: " + (end - start));
	Serializer ser = SerializerFactory.getSerializer("TTL");
        ser.writeToFile(m, cR.acceptanceRelation, outputFile);
	/*FileWriter writer = null;
	try {
	    writer = new FileWriter(outputFile);
	    writer.append("Total runtime: " + (end - start) + "\n");
	    for (String key : m.map.keySet()) {
		for (String value : m.map.get(key).keySet()) {
		    writer.write(key + "," + value + "\n");

		}
	    }
	    writer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}*/

    }
}
