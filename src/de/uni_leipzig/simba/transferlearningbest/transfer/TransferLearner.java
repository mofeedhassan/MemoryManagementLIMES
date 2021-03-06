package de.uni_leipzig.simba.transferlearningbest.transfer;

import de.uni_leipzig.simba.data.Mapping;
//import de.uni_leipzig.simba.evaluation.PRFComputer;
import de.uni_leipzig.simba.evaluation.PRFCalculator;
import de.uni_leipzig.simba.learning.query.LabelBasedPropertyMapper;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.text.LabelView;

import de.uni_leipzig.simba.transferlearningbest.replacement.Replacer;

import de.uni_leipzig.simba.transferlearningbest.transfer.classes.ClassSimilarity;
import de.uni_leipzig.simba.transferlearningbest.transfer.classes.LabelBasedClassSimilarity;
import de.uni_leipzig.simba.transferlearningbest.transfer.classes.SamplingBasedClassSimilarity;
import de.uni_leipzig.simba.transferlearningbest.transfer.classes.UriBasedClassSimilarity;
import de.uni_leipzig.simba.transferlearningbest.transfer.config.ConfigAccuracy;
import de.uni_leipzig.simba.transferlearningbest.transfer.config.ConfigAccuracyWald95;
import de.uni_leipzig.simba.transferlearningbest.transfer.config.ConfigReader;
import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;
import de.uni_leipzig.simba.transferlearningbest.transfer.properties.UriBasedPropertySimilarity;
import de.uni_leipzig.simba.transferlearningbest.transfer.properties.PropertySimilarity;
import de.uni_leipzig.simba.transferlearningbest.transfer.properties.SamplingBasedPropertySimilarity;
import de.uni_leipzig.simba.transferlearningbest.util.Execution;
import de.uni_leipzig.simba.transferlearningbest.util.SparqlUtils;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The transfer learning algorithm core.
 *
 * @author Jens Lehmann
 * @author Axel Ngonga
 *
 */
public class TransferLearner {

    public static double COVERAGE = 0.6;
    public static int SAMPLESIZE = 300;
    private Set<Configuration> configurations;
    private Map<Configuration, String> posExamples;
    private Map<Configuration, String> negExamples;
    private Map<Configuration, Set<String>> sourceProperties;
    private Map<Configuration, Set<String>> targetProperties;
    private Map<String, Set<String>> sourcePropertyCache;
    private Map<String, Set<String>> targetPropertyCache;
    private String resultBuffer;
    Logger logger = LoggerFactory.getLogger(TransferLearner.class);

    public TransferLearner(Set<Configuration> configurations,
            Map<Configuration, String> posExamples,
            Map<Configuration, String> negExamples) {
        this.configurations = configurations;
        this.posExamples = posExamples;
        this.negExamples = negExamples;
    }

    public TransferLearner(String configFolder) {
        resultBuffer = "";
        System.out.println(new File(configFolder).getAbsolutePath());
        configurations = new HashSet<>();
        posExamples = new HashMap<>();
        negExamples = new HashMap<>();
        sourceProperties = new HashMap<>();
        targetProperties = new HashMap<>();
        negExamples = new HashMap<>();
        sourcePropertyCache = new HashMap<>();
        targetPropertyCache = new HashMap<>();

        File f = new File(configFolder), folderName;
        String[] files = f.list();
        ConfigReader cr = new ConfigReader();
        Configuration config;
        int count = 0;

        //check for writeCache
        File cache = new File(configFolder + "/propertyCache.tsv");
        if (cache.exists()) {
            System.out.println("Found cached data");
            readCache(configFolder);
        }
        for (String file : files) {
            folderName = new File(configFolder + "/" + file);
            if (folderName.isDirectory()) {
                if (!new File(folderName.getAbsoluteFile() + "/fixme.txt").exists()) {
                    config = cr.readLimesConfig(folderName.getAbsolutePath() + "/spec.xml");
                    count++;
                    System.out.println("Processing " + count + ".\t" + config.getName());
                    String sourceClass = config.source.getClassOfendpoint(true);
                    String targetClass = config.target.getClassOfendpoint(true);
                    for(String p: config.source.prefixes.keySet())
                    SamplingBasedPropertySimilarity.prefixes.put(p, config.source.prefixes.get(p));
                    for(String p: config.target.prefixes.keySet())
                    SamplingBasedPropertySimilarity.prefixes.put(p, config.target.prefixes.get(p));
                    SamplingBasedPropertySimilarity.prefixes.put("foaf", "http://xmlns.com/foaf/0.1/");
                    SamplingBasedPropertySimilarity.prefixes.put("BibTeX", "http://data.bibbase.org/ontology/#");
                    SamplingBasedPropertySimilarity.prefixes.put("epo", "http://epo.publicdata.eu/ebd/ontology/");

                    Set<String> relevantSourceProperties = new HashSet<>();
                    Set<String> relevantTargetProperties = new HashSet<>();

                    //check whether data already cached
                    if (sourcePropertyCache.containsKey(config.name)) {
                        relevantSourceProperties = sourcePropertyCache.get(config.name);
                    } else {
                        relevantSourceProperties = SparqlUtils.getRelevantProperties(config.getSource().endpoint, sourceClass, SAMPLESIZE, COVERAGE);
                        if (relevantSourceProperties.size() > 0) {
                            sourcePropertyCache.put(config.name, relevantSourceProperties);
                        }
                    }
                    //same here
                    if (targetPropertyCache.containsKey(config.name)) {
                        relevantTargetProperties = targetPropertyCache.get(config.name);
                    } else {
                        relevantTargetProperties = SparqlUtils.getRelevantProperties(config.getTarget().endpoint, targetClass, SAMPLESIZE, COVERAGE);
                        if (relevantTargetProperties.size() > 0) {
                            targetPropertyCache.put(config.name, relevantTargetProperties);
                        }
                    }

                    if (!relevantSourceProperties.isEmpty() && !relevantTargetProperties.isEmpty()) {
                        configurations.add(config);
                        sourceProperties.put(config, relevantSourceProperties);
                        targetProperties.put(config, relevantTargetProperties);
                        posExamples.put(config, folderName.getAbsolutePath() + "/positive.nt");
                        negExamples.put(config, folderName.getAbsolutePath() + "/negative.nt");/////
                    }
                }
            }
        }
        writeCache(configFolder);
    }

    public TransferLearner(String configFolder, boolean useSparql) {
        resultBuffer = "";
        configurations = new HashSet<>();
        posExamples = new HashMap<>();
        negExamples = new HashMap<>();
        sourceProperties = new HashMap<>();
        targetProperties = new HashMap<>();
        negExamples = new HashMap<>();
        sourcePropertyCache = new HashMap<>();
        targetPropertyCache = new HashMap<>();

        File f = new File(configFolder), folderName;
        String[] files = f.list();
        ConfigReader cr = new ConfigReader();
        Configuration config;
        int count = 0;

        //check for writeCache        
        for (String file : files) 
        {
            folderName = new File(configFolder + "/" + file);
            if (folderName.isDirectory()) 
            {
                if (!new File(folderName.getAbsoluteFile() + "/fixme.txt").exists()) {
                    config = cr.readLimesConfig(folderName.getAbsolutePath() + "/spec.xml");
                    count++;
                    System.out.println("Processing " + count + ".\t" + getConfigurationName(config.getName()));
                    String sourceClass = config.source.getClassOfendpoint(true);
                    String targetClass = config.target.getClassOfendpoint(true);
                    Set<String> relevantSourceProperties = new HashSet<>();;
                    Set<String> relevantTargetProperties = new HashSet<>();

                    //check whether data already cached
                    relevantSourceProperties = getRelevantProperties(config, true);
                    relevantTargetProperties = getRelevantProperties(config, false);

                    if (!relevantSourceProperties.isEmpty() && !relevantTargetProperties.isEmpty()) {
                        configurations.add(config);
                        sourceProperties.put(config, relevantSourceProperties);
                        targetProperties.put(config, relevantTargetProperties);
                        posExamples.put(config, folderName.getAbsolutePath() + "/positive.nt");
                        negExamples.put(config, folderName.getAbsolutePath() + "/negative.nt");//{
                    }
                }
            }
        }
    }

    public void writeCache(String folder) {
        try {
            new File(folder + "/propertyCache.tsv").delete();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(folder + "/propertyCache.tsv")));
            for (Configuration c : configurations) {
                Set<String> sourceProps = sourceProperties.get(c);
                for (String property : sourceProps) {
                    writer.println(c.name + "\tSourceProperty\t" + property);
                }
                Set<String> targetProps = targetProperties.get(c);

                for (String property : targetProps) {
                    writer.println(c.name + "\tTargetProperty\t" + property);
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readCache(String folder) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(folder + "/propertyCache.tsv"));
            String s = reader.readLine();
            sourcePropertyCache = new HashMap<>();
            targetPropertyCache = new HashMap<>();
            while (s != null) {
                String[] split = s.split("\t");
                if (split[1].equals("SourceProperty")) {
                    if (!sourcePropertyCache.containsKey(split[0])) {
                        sourcePropertyCache.put(split[0], new HashSet<String>());
                    }
                    sourcePropertyCache.get(split[0]).add(split[2]);
                } else {
                    if (!targetPropertyCache.containsKey(split[0])) {
                        targetPropertyCache.put(split[0], new HashSet<String>());
                    }
                    targetPropertyCache.get(split[0]).add(split[2]);
                }
                s = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * constructs for each configuration list of configurations sorted by their
     * similarity degrees to the main configuration
     *
     * @param instanceSampleSize Sampling size
     * @param coverage Minimal coverage for
     * @return
     */
    public Map<Configuration, List<Configuration>> runOrderedMatching() {
        Map<Configuration, List<Configuration>> results = new HashMap<>();//configuration -> list of similar sorted configurations
        List<Configuration> result;
        for (Configuration config : configurations) {
        	
            List<String> sourceClasses = getRestrictionList(config.source.restrictions);
            List<String> targetClasses = getRestrictionList(config.target.restrictions);
            
            Set<String> relevantSourceProperties = sourceProperties.get(config);
            Set<String> relevantTargetProperties = targetProperties.get(config);
            
            result = runLeaveOneOutOrdered(config, sourceClasses, targetClasses, relevantSourceProperties, relevantTargetProperties);
            results.put(config, result);
        }
        return results;
    }

    /**
     * Runs the whole transfer learning experiment for all specs using the URI matching approach
     * for each configuration select a configuration
     * 1- get its specifications in terms of classes and properties  
     * 2- get the best matching configuration of the others (returned cloned and replaced with input specifications except metric)
     * 3- get the reference
     * 4- run the best configuration
     * 5- calc results accuracy
     * @param instanceSampleSize Sampling size
     * @param coverage Minimal converage for
     * @return
     */
    public Map<Configuration, Double> runSimpleMatching(int instanceSampleSize, double coverage) {
        Map<Configuration, Double> results = new HashMap<Configuration, Double>();
        Configuration result;
        String output = "The results for running the specification with the best configuration's metrics\n";
        output += "Specification \t Similar Spec. \t Precision \t Recall \t F-Measure\n";
      //PRFComputer prf = new PRFComputer();
        PRFCalculator prf = new PRFCalculator();
        Execution exec = new Execution();
        //iterate over configurations
        for (Configuration config : configurations) {
        	//get the source and target classes
            System.out.println("Processing " + getConfigurationName(config.name) + "\t[" + config.source.getClassOfendpoint(true) + " -> " + config.target.getClassOfendpoint(true)+"]");
            String sourceClass = config.source.getClassOfendpoint(true);
            String targetClass = config.target.getClassOfendpoint(true);
            
            //get the properties of the source and the target of the configuration
            System.out.println("Getting source properties ... ");
            Set<String> relevantSourceProperties = sourceProperties.get(config);
            System.out.println("Got " + relevantSourceProperties.size() + " source properties.\nGetting target properties ... ");
            Set<String> relevantTargetProperties = targetProperties.get(config);
            System.out.println("Got " + relevantTargetProperties.size() + " target properties.\n");
            System.out.println("Running leave one out ... ");
            
            // gets the best configuration with its specs are replaced with the input configuration - metric
            result = runLeaveOneOut(config, sourceClass, targetClass, relevantSourceProperties, relevantTargetProperties);
            
            // now run best configuration and get precision, recall and f-measure
            System.out.println("Getting reference mapping ...");
            //Mapping reference = new Mapping().readNtFile(config.name.replaceAll(Pattern.quote("spec.xml"), "accept.nt"));
            Mapping reference = new Mapping().readNtFile(config.name.replaceAll(Pattern.quote("spec.xml"), "reference.nt"));// the name and file format of references are changed

            
            System.out.println("Running mapping for the most similar spec derived from [" + getConfigurationName(result.name) + "] with all information from the current configuration");
            Mapping transferResult = exec.executeComplex(result);

//            System.out.println("Transfermapping\n");
//            System.out.println(transferResult);
//            System.out.println("Reference\n");
//            System.out.println(reference);
//            

            //output = output + new File(config.name).getParent() + "\t" + prf.computePrecision(transferResult, reference) + "\t" + prf.computeRecall(transferResult, reference) + "\t" + prf.computeFScore(transferResult, reference) + "\n";
            output = output + getConfigurationName(config.name) + "\t" + getConfigurationName(result.name) + "\t" +prf.precision(transferResult, reference) + "\t" + prf.recall(transferResult, reference) + "\t" + prf.fScore(transferResult, reference) + "\n";
            ConfigAccuracy ca = new ConfigAccuracyWald95();
            double acc = ca.getAccuracy(result, posExamples.get(config), negExamples.get(config));
            //System.out.println(output);
            //add the transfered configuration (learned) and its accuracy
            results.put(result, acc);
            //System.exit(1);
        }
        System.out.println("\n\n=== FINAL RESULTS ===\n" + output + "\n\n");
        System.out.println("\n\n" + resultBuffer + "\n\n");
        return results;
    }
//    /**
//     * Gets an input source and target class and return the best possible config
//     *
//     * @param inputSourceClass Input source class
//     * @param inputTargetClass Input target class
//     * @param relevantSourceProperties Source properties
//     * @param relevantTargetProperties Target properties
//     */
//    public Configuration run(String inputSourceClass, String inputTargetClass, Set<String> relevantSourceProperties, Set<String> relevantTargetProperties) {
//
//        // setup
//        ConfigAccuracy confAcc = new ConfigAccuracyWald95();
//        ClassSimilarity classSim = new UriBasedClassSimilarity();
//        PropertySimilarity propSim = new UriBasedPropertySimilarity();
//        double bestF = 0;
//        Configuration bestConfig = configurations.iterator().next();
//        for (Configuration configuration : configurations) {
//            // accuracy of link specification; // TODO: where to get the positive and negative examples?
//            double alpha = confAcc.getAccuracy(configuration, posExamples.get(configuration), negExamples.get(configuration));
//
//            String sourceClass = configuration.getSource().getClassOfendpoint();
//            String targetClass = configuration.getTarget().getClassOfendpoint();
//
//            double cSSim = classSim.getSimilarity(sourceClass, inputSourceClass, configuration);
//            double cTSim = classSim.getSimilarity(targetClass, inputTargetClass, configuration);
//
//            double factor = alpha * cSSim * cTSim;
//            if (bestF < factor) {
//                bestF = factor;
//                bestConfig = configuration;
//            }
//
//        }
//        // figure out best mapping properties
//        //first for source
//
//        String sourceClass = bestConfig.getSource().getClassOfendpoint();
//        String targetClass = bestConfig.getTarget().getClassOfendpoint();
//        Map<String, String> sourcePropertyMapping = getPropertyMap(relevantSourceProperties,
//                sourceClass, inputSourceClass, propSim, bestConfig, true);
//        Map<String, String> targetPropertyMapping = getPropertyMap(relevantTargetProperties,
//                targetClass, inputTargetClass, propSim, bestConfig, true);
//
//        //important: clone the config
//        bestConfig = new ConfigReader().readLimesConfig(bestConfig.name);
//        for (String property : sourcePropertyMapping.keySet()) {
//            bestConfig = Replacer.replace(bestConfig, property, sourcePropertyMapping.get(property), true);
//        }
//        for (String property : targetPropertyMapping.keySet()) {
//            bestConfig = Replacer.replace(bestConfig, property, targetPropertyMapping.get(property), false);
//        }
//
//        System.out.println(bestConfig.source.getClassOfendpoint());
//        System.out.println(bestConfig.target.getClassOfendpoint());
//        System.out.println(bestConfig.measure);
//        //System.out.println(bestConfig.source.getClassOfendpoint());
//
//        return bestConfig;
//    }

    /**
     * Gets the best similar configuration to a given configuration input to use its metric
     * 1- It finds most similar config based on the used similarity criteria in terms of URI based similarity
     * 2- clone the bet configuration
     * 3- replace the cloned best configuration with the input configuration's specifications except the metric
     * 4- return the new configuration
     * Gets an input source and target class and return the best possible config
     *
     * @param inputSourceClass Input source class
     * @param inputTargetClass Input target class
     * @param relevantSourceProperties Source properties
     * @param relevantTargetProperties Target properties
     */
    public Configuration runLeaveOneOut(Configuration config, String inputSourceClass, String inputTargetClass, Set<String> relevantSourceProperties, Set<String> relevantTargetProperties) {

        // setup
        ConfigAccuracy confAcc = new ConfigAccuracyWald95();
        ClassSimilarity classSim = new UriBasedClassSimilarity();//new SamplingBasedClassSimilarity();//new LabelBasedClassSimilarity();//
        PropertySimilarity propSim =  new UriBasedPropertySimilarity();//new SamplingBasedPropertySimilarity();//
        double bestF = 0;
        Configuration bestConfig = configurations.iterator().next();
        double similarity = -1;
        
        if(config.name.contains("dbpedia-datagovUK-city"))
        	similarity=-1;
        //iterate for other configurations
        for (Configuration configuration : configurations) {
            String name1 = configuration.name.trim();
            String name2 = config.name.trim();
            if (!name1.equalsIgnoreCase(name2)) {
            	if(configuration.name.contains("dbpedia-linkedgeodata-country"))
            		System.out.println(config.name);
            	
                System.out.println("Check (" + getConfigurationName(configuration.name)+") similarity to ("+ getConfigurationName(config.name)+")");
                // accuracy of link specification; // TODO: where to get the positive and negative examples?
                double alpha = confAcc.getAccuracy(configuration, posExamples.get(configuration), negExamples.get(configuration));

                //get the source classes of the iterated (opponent) configuration
                String sourceClass = configuration.getSource().getClassOfendpoint();
                String targetClass = configuration.getTarget().getClassOfendpoint();
                
                if (sourceClass == null ) {
                    System.err.println(configuration.name + " leads to sourceClass == null");
                }
                if (sourceClass == null ) {
                    System.err.println(configuration.name + " leads to targetClass == null");
                }
                
                //get the similarity between the input configuration classes and the opponent configuration classes
                /*double cSSim = classSim.getSimilarity(sourceClass, inputSourceClass, configuration);
                double cTSim = classSim.getSimilarity(targetClass, inputTargetClass, configuration);*/
                boolean isSource=true;
                //get the similarity between the input configuration classes and the opponent configuration classes
                
            	            		
                double cSSim=classSim.getSimilarity(inputSourceClass, config,sourceClass,configuration,isSource);;
                double cTSim =classSim.getSimilarity(inputTargetClass,config, targetClass,configuration,!isSource);;
                

                double factor = alpha * cSSim * cTSim;
                //double factor = cSSim * cTSim;
                if (bestF < factor) {
                    bestF = factor;
                    bestConfig = configuration;
                }
            }
        }
        if(bestF==0)//no other similar link specification found
        {
        	bestConfig = config;
        	bestF=1;
        }
        
        System.out.println(getConfigurationName(bestConfig.name)+ " most similar configuration to " + getConfigurationName(config.name) +" with similarity =  " + bestF);
        System.out.println("Read the most similar configuration and replace all its information except IDs and measures (similar properties in measures are replaced) with the one in our hands");

        //clone the best config
        bestConfig = new ConfigReader().readLimesConfig(bestConfig.name);

        // figure out best mapping properties
        //first for source        
        String sourceClass = bestConfig.getSource().getClassOfendpoint();
        String targetClass = bestConfig.getTarget().getClassOfendpoint();

       /* Map<String, String> sourcePropertyMapping = getPropertyMap(relevantSourceProperties,inputSourceClass,sourceClass,  bestConfig, propSim,  true);
        Map<String, String> targetPropertyMapping = getPropertyMap(relevantTargetProperties,inputTargetClass,targetClass, bestConfig, propSim,  false);*/
        
        Map<String, String> sourcePropertyMapping = getPropertyMap(relevantSourceProperties,inputSourceClass,config,sourceClass,  bestConfig, propSim,  true);
        Map<String, String> targetPropertyMapping = getPropertyMap(relevantTargetProperties,inputTargetClass,config,targetClass, bestConfig, propSim,  false);

        //replace ids
        bestConfig.source.id =  config.source.id;
        bestConfig.target.id = config.target.id;
        
        //replace endpoints
        bestConfig.source.endpoint = config.source.endpoint;
        bestConfig.target.endpoint = config.target.endpoint;
        //replace graphs
        bestConfig.source.graph = config.source.graph;
        bestConfig.target.graph = config.target.graph;
        //fill in prefixes
        for (String entry : config.source.prefixes.keySet()) {
            bestConfig.source.prefixes.put(entry, config.source.prefixes.get(entry));
            bestConfig.target.prefixes.put(entry, config.source.prefixes.get(entry));
        }
        for (String entry : config.target.prefixes.keySet()) {
            bestConfig.source.prefixes.put(entry, config.target.prefixes.get(entry));
            bestConfig.target.prefixes.put(entry, config.target.prefixes.get(entry));
        }
        //replace properties

        for (String property : sourcePropertyMapping.keySet()) {

            bestConfig = Replacer.replace(bestConfig, property, config, sourcePropertyMapping.get(property), true);
        }
        for (String property : targetPropertyMapping.keySet()) {
            bestConfig = Replacer.replace(bestConfig, property, config, targetPropertyMapping.get(property), false);
        }


        //replace class
        String r = bestConfig.source.restrictions.get(0);
        r = r.replaceAll(Pattern.quote(bestConfig.source.getClassOfendpoint()), "<" + inputSourceClass + ">");
        bestConfig.source.restrictions.set(0, r);

        r = bestConfig.target.restrictions.get(0);
        r = r.replaceAll(Pattern.quote(bestConfig.target.getClassOfendpoint()), "<" + inputTargetClass + ">");
        bestConfig.target.restrictions.set(0, r);
        resultBuffer = resultBuffer + getConfigurationName(config.name) + "\t" + getConfigurationName(bestConfig.name) + "\t"
                + bestF + "\t" + bestConfig.source.getClassOfendpoint()
                + "\t" + bestConfig.target.getClassOfendpoint() + "\t" + bestConfig.measure + "\n";

        //remove replicated properties due to the difference in number of properties between the main configuration and best candidate
        // where properties mapping assign the same mapped property more than once. This will no affect the metric
        bestConfig.getSource().properties = removeDuplicatedProperties(bestConfig.getSource().properties);
        bestConfig.getTarget().properties = removeDuplicatedProperties(bestConfig.getTarget().properties);

        return bestConfig;
    }
    
    private List<String> removeDuplicatedProperties(List<String> properties)
    {
    	List<String> newProperties = null;
    	if(properties.size()>0)
    	{
    		newProperties = new ArrayList<>();
    		for (String property : properties) {
				if(!newProperties.contains(property))
					newProperties.add(property);
			}
    	}
    	return newProperties;
    }

    /**
     *
     * @param inputProperties Properties from class of main configuration (finding similarity to it)
     * @param inputClassName Known class from main configuration
     * @param inputConfiguration main configuration
     * 
     * @param className Class name of candidate configuration (finding out how similar it is to the main configuration)
     * @param configuration candidate configuration    
     * @param propSim Property similarity computation algorithm
     * @return Mapping from properties of known config to config to be learned
     */
    
    private Map<String, String> getPropertyMap(Set<String> inputProperties, String inputClassName, Configuration inputConfiguration, String className,   Configuration configuration, PropertySimilarity propSim,boolean source) {
        Map<String, String> propertyMapping = new HashMap<>();
        List<String> properties;
        if (source) {
            properties = configuration.source.properties;
        } else {
            properties = configuration.target.properties;
        }
        for (String candidateProperty : properties) {//candidate property
            double maxSim = -1, sim;
            String bestProperty = "";
            for (String inputProperty : inputProperties)// main config properties // ... call replacement function using propSim, relevantSourceProperties, configuration as input ...
            {//(String property1, String class1, Configuration config1,String property2,  String class2, Configuration config2)
                // get rid of rdf:type
                if (!inputProperty.endsWith("ype") && !inputProperty.endsWith("sameAs")) {
                    sim = propSim.getSimilarity(inputProperty,inputClassName,inputConfiguration, candidateProperty, className,  configuration,source);
                    if (sim > maxSim) {
                        bestProperty = inputProperty;
                        maxSim = sim;
                    }
                }
            }
            propertyMapping.put(candidateProperty, bestProperty);// assign for the candidate property the best mapped property from the main configuration
        }
        return propertyMapping;
    }
    /**
    *
    * @param inputProperties Properties from class whose mapping is to be
    * learned
    * @param className Class name whose mapping is to be learned
    * @param inputClassName Known class from config
    * @param propSim Property similarity computation algorithm
    * @param configuration Known configuration
    * @return Mapping from properties of known config to config to be learned
    */
    private Map<String, String> getPropertyMap(Set<String> inputProperties, String inputClassName, String className,   Configuration configuration, PropertySimilarity propSim,boolean source) {
        Map<String, String> propertyMapping = new HashMap<>();
        List<String> properties;
        if (source) {
            properties = configuration.source.properties;
        } else {
            properties = configuration.target.properties;
        }
        for (String knownProperty : properties) {
            double maxSim = -1, sim;
            String bestProperty = "";
            for (String inputProperty : inputProperties) // ... call replacement function using propSim, relevantSourceProperties, configuration as input ...
            {
                // get rid of rdf:type
                if (!inputProperty.endsWith("ype") && !inputProperty.endsWith("sameAs")) {
                    sim = propSim.getSimilarity(knownProperty, inputProperty, className, inputClassName, configuration);
                    if (sim > maxSim) {
                        bestProperty = inputProperty;
                        maxSim = sim;
                    }
                }
            }
            propertyMapping.put(knownProperty, bestProperty);
        }
        return propertyMapping;
    }

    /**
     * An example transfer learning task.
     *
     * @param args
     */
    public void test() {
        // input (example from https://github.com/LATC/24-7-platform/blob/master/link-specifications/dbpedia-diseasome-disease/spec.xml)
//		String inputSourceClass = "http://dbpedia.org/ontology/Disease";
//		String inputTargetClass = "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/diseases";

        // LIMES example: we learn inactive ingredients from other similar specs
        String inputSourceClass = "http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/drugs";
        String sourceEndpoint = "http://lgd.aksw.org:5678/sparql";
        String inputTargetClass = "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs";
        String targetEndpoint = "http://lgd.aksw.org:5678/sparql";
        Set<Configuration> configurations = new HashSet<Configuration>();

        Map<Configuration, String> posExamples = new HashMap<Configuration, String>();
        Map<Configuration, String> negExamples = new HashMap<Configuration, String>();

        boolean limesOnly = true;

        if (limesOnly) {
            ConfigReader cr = new ConfigReader();
            Configuration cf;
            cf = cr.readLimesConfig("specs/drugbank-dailymed-activeIngredients/spec.limes.xml");
            configurations.add(cf);
            posExamples.put(cf, "specs/drugbank-dailymed-activeIngredients/positive.ttl");
            negExamples.put(cf, "specs/drugbank-dailymed-activeIngredients/negative.ttl");
            cf = cr.readLimesConfig("specs/drugbank-dailymed-activeMoiety/spec.limes.xml");
            configurations.add(cf);
            posExamples.put(cf, "specs/drugbank-dailymed-activeMoiety/positive.ttl");
            negExamples.put(cf, "specs/drugbank-dailymed-activeMoiety/negative.ttl");
            cf = cr.readLimesConfig("specs/drugbank-dailymed-ingredients/spec.limes.xml");
            configurations.add(cf);
            posExamples.put(cf, "specs/drugbank-dailymed-ingredients/positive.ttl");
            negExamples.put(cf, "specs/drugbank-dailymed-ingredients/negative.ttl");
            // we learn drugbank-dailymed-inactiveIngredients from 
            // drugbank-dailymed-activeIngredients
            // drugbank-dailymed-activeMoiety
            // drugbank-dailymed-ingredients


        } else {
            // TODO: read configurations from LATC specs	
        }

        // determine relevant properties for current link Task
        Set<String> relevantSourceProperties = SparqlUtils.getRelevantProperties(sourceEndpoint, inputSourceClass, 100, 0.8);
        Set<String> relevantTargetProperties = SparqlUtils.getRelevantProperties(targetEndpoint, inputTargetClass, 100, 0.8);;

        TransferLearner tl = new TransferLearner(configurations, posExamples, negExamples);
        //tl.run(inputSourceClass, inputTargetClass, relevantSourceProperties, relevantTargetProperties);

    }

    public Set<String> getRelevantProperties(Configuration c, boolean source) {
        Set<String> properties = new HashSet<>();
        if (source) {
            for (String p : c.source.properties) {
                properties.add(p);
            }
        } else {
            for (String p : c.target.properties) {
                properties.add(p);
            }
        }
        return properties;
    }

    /**
     * returns list of the configurations sorted by their similarity degree to the input configuration
     * @param config
     * @param inputSourceClasses
     * @param inputTargetClasses
     * @param relevantSourceProperties
     * @param relevantTargetProperties
     * @return
     */
    private List<Configuration> runLeaveOneOutOrdered(Configuration config,
            List<String> inputSourceClasses, List<String> inputTargetClasses,
            Set<String> relevantSourceProperties, Set<String> relevantTargetProperties) {

        // setup
        ConfigAccuracy confAcc = new ConfigAccuracyWald95();
        ClassSimilarity classSim = new LabelBasedClassSimilarity();//new  UriBasedClassSimilarity();
        PropertySimilarity propSim = new UriBasedPropertySimilarity();
        List<Configuration> result = new ArrayList<Configuration>();

        for (Configuration configuration : configurations) {
            String name1 = configuration.name.trim();
            String name2 = config.name.trim();
            if (!name1.equalsIgnoreCase(name2)) {
                //System.out.println("Processing " + configuration.name);
                // accuracy of link specification; // TODO: where to get the positive and negative examples?
                double alpha = confAcc.getAccuracy(configuration, posExamples.get(configuration), negExamples.get(configuration));

                List<String> sourceClasses = getRestrictionList(configuration.getSource().restrictions);
                List<String> targetClasses = getRestrictionList(configuration.getTarget().restrictions);
                if (sourceClasses.isEmpty()) {
                    System.err.println(configuration.name + " leads to sourceClass == null");
                }
                if (targetClasses.isEmpty()) {
                    System.err.println(configuration.name + " leads to targetClass == null");
                }

                boolean isSource=true;
                double cSSim = 0, cTSim = 0;
                //get the max similarity pair-classes as source classes
                for (String sourceClass : sourceClasses) {
                    double max = -1, sim;
                    for (String inputSourceClass : inputSourceClasses) {
                    	System.out.println(configuration.name);
                        //sim = classSim.getSimilarity(inputSourceClass,sourceClass,  configuration);
                    	sim = classSim.getSimilarity(inputSourceClass, config,sourceClass, configuration,isSource);
                        if (sim > max) {
                            max = sim;
                        }
                    }
                    cSSim = cSSim + max;
                }
                cSSim = cSSim / (double) sourceClasses.size();

                //get the max similarity pair-classes as target classes
                for (String targetClass : targetClasses) {
                    double max = -1, sim;
                    for (String inputTargetClass : inputTargetClasses) {
                        //sim = classSim.getSimilarity(inputTargetClass,targetClass,  configuration);
                        sim = classSim.getSimilarity(inputTargetClass, config, targetClass, configuration,!isSource);
                        if (sim > max) {
                            max = sim;
                        }
                    }
                    cTSim = cTSim + max;
                }
                cTSim = cTSim / (double) targetClasses.size();
                //calculate overall similarity value between the compared configurations
                double factor = alpha * cSSim * cTSim;
                //double factor = cSSim * cTSim;
                //read the configuration into c
                Configuration c = new ConfigReader().readLimesConfig(configuration.name);
                //specify the similarity value
                c.similarity = factor;
                //add it to a list
                result.add(c);
            }
        }
        //sort the list of configurations based the calculated similarity for each descending
        Collections.sort(result);
        result = reverse(result);
        return result;
    }

    public List reverse(List x) {
        List result = new ArrayList();
        for (int i = x.size() - 1; i >= 0; i--) {
            result.add(x.get(i));
        }
        return result;
    }

    /**
     * Returns the class contained in the restriction
     *
     * @return Class label
     */
    public List<String> getRestrictionList(List<String> restrictions) {

        List<String> result = new ArrayList<String>();
        List<String> buffer = new ArrayList<String>();
        for (String rest : restrictions) {
            if (rest.contains("UNION")) {
                String split[] = rest.split("UNION");
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    s = s.replaceAll("<", "").replaceAll(">", "").replaceAll(Pattern.quote("."), "");
                    s = s.replaceAll(Pattern.quote("{"), "").replaceAll(Pattern.quote("}"), "");
                    s = s.split(" ")[2];
                    //s = s.substring(0, s.length() - 1);
                    result.add(s.trim());
                }
            } else {
                String s = rest;
                if(s.length()!=0)//mofeed
                {
	                s = s.replaceAll("<", "").replaceAll(">", "").replaceAll(Pattern.quote("."), "");
	                s = s.replaceAll(Pattern.quote("{"), "").replaceAll(Pattern.quote("}"), "");
	                s = s.split(" ")[2];
	                //s = s.substring(0, s.length() - 1);
                
                result.add(s.trim());
                }
            }
        }
        return result;
    }

    public static void main(String args[]) {
//        TransferLearner tl = new TransferLearner("finalSpecsTosvn/finalSpecsTosvn");
//        System.out.println(tl.runSimpleMatching(100, 0.6));
        TransferLearner tl = new TransferLearner("/media/mofeed/A0621C46621C24164/03_Work/TransferLearningBacks/TransferLearningTmp/20Specs/"/*"C:/workspace/TransferLearning/finalSpecs"*/, true);
        //runOrderedMatchingTest(tl);
        runSimpleMatchingTest(tl);
    }
    
    public static void runOrderedMatchingTest(TransferLearner tl)
    {
    	tl.displayLS(tl);
        Map<Configuration, List<Configuration>> result = tl.runOrderedMatching();
        for (Configuration c : result.keySet()) {
            System.out.print(c+":-\n");
            List<Configuration> list = result.get(c);
            for (Configuration l : list) {
                System.out.print("\n" + l);
            }
            System.out.println("\n----------------------------------------------------------------------------------------------------------------------------------");
            System.out.println();
        }
    }
    
    public static void runSimpleMatchingTest(TransferLearner tl)
    {
    	Map<Configuration, Double> simpleMatchingResults = tl.runSimpleMatching(100, 10);
        for (Configuration conf : simpleMatchingResults.keySet()) {
			System.out.println(conf.name+":"+simpleMatchingResults.get(conf));
		}
    }
    public static void displayLS(TransferLearner tl)
    {
    	for (Configuration configuration : tl.configurations) {
			System.out.println(configuration);
			System.out.println(configuration.getSource().endpoint);
			System.out.println(configuration.getTarget().endpoint);
			System.out.println(configuration.getSource().getClassRestriction());
			System.out.println(configuration.getTarget().getClassRestriction());
			System.out.println(tl.sourceProperties.get(configuration));
			System.out.println(tl.targetProperties.get(configuration));
			System.out.println(configuration.getMeasure());
			System.out.println("---------------------------------------------------------------");
		}
    	
    }
    public static String getConfigurationName(String fullPath)
    {
    	String tmp =fullPath.substring(0,fullPath.lastIndexOf("/"));
    	return tmp.substring(tmp.lastIndexOf("/")+1, tmp.length());
    }
}
