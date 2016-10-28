package de.uni_leipzig.simba.grecall.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.execution.ExecutionEngine;
import de.uni_leipzig.simba.execution.NestedPlan;
import de.uni_leipzig.simba.execution.evaluation.AutomaticExperiments;
import de.uni_leipzig.simba.execution.planner.CanonicalPlanner;
import de.uni_leipzig.simba.execution.planner.ExecutionPlanner;
import de.uni_leipzig.simba.execution.planner.HeliosPlanner;
import de.uni_leipzig.simba.execution.rewriter.AlgebraicRewriter;
import de.uni_leipzig.simba.execution.rewriter.Rewriter;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.grecall.optimizer.RecallOptimizerFactory;
import de.uni_leipzig.simba.grecall.optimizer.recalloptimizer.Baseline;
import de.uni_leipzig.simba.grecall.optimizer.recalloptimizer.Optimizer;
import de.uni_leipzig.simba.grecall.optimizer.recalloptimizer.RecallOptimizer;
import de.uni_leipzig.simba.grecall.oracle.SimpleOracle;
import de.uni_leipzig.simba.grecall.util.DatasetConfiguration;
import de.uni_leipzig.simba.grecall.util.DiffPair;
import de.uni_leipzig.simba.grecall.util.StatisticsBase;
import de.uni_leipzig.simba.specification.LinkSpec;

public class RecallOptimizerEvaluation {

    private static final String FILE_HEADER = "Original LS\tC-RO\tRO-MA";
    private static final String SELECTIVITY_HEADER = "Original LS\tDesired\tC-RO\tRO-MA";
    private static final String OPP_FILE_HEADER = "C-RO\tRO-MA";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String TAB_DELIMITER = "\t";

    private String baseDirectory;
    private List<String> specifications;
    private SimpleOracle oracle;
    private ExecutionEngine ee;
    private int maxRefinement = 0;
    private double recall = 0;
    private int iterations = 1; // number of iterations
    private ArrayList<String> OptimizerType = new ArrayList<String>() {
        {
            add(0, "downward_refinement");
            // add(1, "downward_refinement_monAS");
        }
    };

    private Cache source;
    private Cache target;
    private String[] FileNames = { "ExecutionTime.csv", "OverallTime.csv", "OptimizationTime.csv",
            "EstimatedExecutionTime.csv", "MappingSize.csv", "EstimatedSelectivity.csv", "Selectivity.csv",
            "OptimizedSpecifications.csv" };

    static Logger logger = Logger.getLogger("LIMES");

    public RecallOptimizerEvaluation(String folderName, List<String> specifications, SimpleOracle oracle, Cache source,
            Cache target) {
        this.specifications = specifications;
        // "datasets/"+DatasetName+"/results/";
        this.baseDirectory = folderName;
        ee = new ExecutionEngine(source, target, "?x", "?y");
        try {
            PatternLayout layout = new PatternLayout("%d{dd.MM.yyyy HH:mm:ss} %-5p [%t] %l: %m%n");
            FileAppender fileAppender = new FileAppender(layout,
                    (this.baseDirectory + "/test.txt").replaceAll(".xml", "") + ".log", false);
            fileAppender.setLayout(layout);
            logger.removeAllAppenders();
            logger.addAppender(fileAppender);
        } catch (Exception e) {
            logger.warn("Exception creating file appender.");
        }
        this.source = source;
        this.target = target;
        this.oracle = oracle;
    }

    public RecallOptimizerEvaluation(String folderName, List<String> specifications, SimpleOracle oracle, Cache source,
            Cache target, double k, int maxRefinement) {
        this.specifications = specifications;

        this.baseDirectory = folderName;
        ee = new ExecutionEngine(source, target, "?x", "?y");
        try {
            PatternLayout layout = new PatternLayout("%d{dd.MM.yyyy HH:mm:ss} %-5p [%t] %l: %m%n");
            FileAppender fileAppender = new FileAppender(layout,
                    (this.baseDirectory + "/test.txt").replaceAll(".xml", "") + ".log", false);
            fileAppender.setLayout(layout);
            logger.removeAllAppenders();
            logger.addAppender(fileAppender);
        } catch (Exception e) {
            logger.warn("Exception creating file appender.");
        }
        this.source = source;
        this.target = target;
        this.oracle = oracle;
        this.recall = k;
        this.maxRefinement = maxRefinement;

    }

    public void saveMapping(int specificationCounter, String iterationDirectory, String refinementType,
            Experiment experiment) {
        // datasets/AMAZONGOOGLE/results/0/downward_refinement/22.txt
        // or
        // datasets/AMAZONGOOGLE/results/baseline/0/baseline/22.txt
        String refinementTypeFolder = iterationDirectory + "/" + refinementType;

        // create optimization folder
        File dirName = new File(refinementTypeFolder);
        if (!dirName.isDirectory()) {
            try {
                dirName.mkdir();
            } catch (SecurityException se) {
            }
        }
        // save mapping
        String specFilename = refinementTypeFolder + "/" + specificationCounter + ".txt";
        File file = new File(specFilename);
        FileWriter writer = null;
        if (!file.exists()) {
            try {
                writer = new FileWriter(specFilename);
                Mapping m = experiment.getMapping();
                String mappingPairs = m.stringOutput();
                writer.append(mappingPairs);
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void writeResults(File directory, LinkedHashMap<String, Experiment> experiments) {
        int filenameCounter = 0;
        // report results for this iteration, k, max and
        // specification
        // optimization time
        for (String filename : FileNames) {
            try {
                String FileName = directory + "/" + filename;
                File f = new File(FileName);
                FileWriter writer = null;
                // create file if it doesn't exist
                if (f.exists()) {
                    writer = new FileWriter(f, true);
                } else {
                    writer = new FileWriter(f);
                    if (filenameCounter != 2) { // optimization time
                        if (filenameCounter == 5) { // est selectivity
                            writer.append(SELECTIVITY_HEADER.toString());
                            writer.append(NEW_LINE_SEPARATOR);
                        } else {
                            writer.append(FILE_HEADER.toString());
                            writer.append(NEW_LINE_SEPARATOR);
                        }
                    } else {// optimization time stats
                        writer.append(OPP_FILE_HEADER.toString());
                        writer.append(NEW_LINE_SEPARATOR);

                    }
                }
                DecimalFormat df = new DecimalFormat("#");
                df = new DecimalFormat("#");
                df.setMaximumFractionDigits(50);
                // report statistics for each experiment

                for (Map.Entry<String, Experiment> entry : experiments.entrySet()) {
                    String OptimizerType = entry.getKey();
                    Experiment experiment = entry.getValue();

                    float[] statistics = experiment.getStatistics();
                    LinkSpec spec = experiment.getSpec();

                    if (filenameCounter == 7) { // spec file
                        String LS = spec.toStringOneLine();
                        writer.append((String) LS);
                        writer.append(TAB_DELIMITER);
                    } else { // third file is the optimization
                        // file
                        if (filenameCounter == 2 && OptimizerType.equals("baseline"))
                            continue;
                        // estimate true selectivity
                        if (filenameCounter == 6) {
                            float t = statistics[4] / (float) (source.size() * target.size());
                            writer.append(df.format(t));
                            writer.append(TAB_DELIMITER);
                        } else {

                            if (filenameCounter == 5 && OptimizerType.equals("downward_refinement")) {
                                // add desired selectivity
                                float t2 = statistics[6];
                                writer.append(df.format(t2));
                                writer.append(TAB_DELIMITER);
                            }
                            float t = statistics[filenameCounter];
                            writer.append(df.format(t));
                            writer.append(TAB_DELIMITER);
                        }

                    }

                }
                writer.append(NEW_LINE_SEPARATOR);
                writer.flush();
                writer.close();
                filenameCounter++;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void runBaseline() {

        for (int iteration = 0; iteration < iterations; iteration++) {
            int specificationCounter = 0;
            for (String specification : specifications) {
                LinkedHashMap<String, Experiment> baselines = new LinkedHashMap<String, Experiment>();
                specificationCounter++;
                logger.info("+++++++++++++++++++++++++++++++" + "Baseline for iteration: " + iteration
                        + "+++++++++++++++++++++++++++++++");
                Experiment experiment = new Experiment(specification);

                RecallOptimizer rr = new Baseline(this.oracle);
                experiment.optimize(rr, (long) 0.0);
                experiment.run(ee);
                baselines.put(specification, experiment);
                String iterationDirectory = this.baseDirectory + "/" + iteration;
                File dirName = new File(iterationDirectory);
                if (!dirName.isDirectory()) {
                    try {
                        dirName.mkdir();
                    } catch (SecurityException se) {
                    }
                }
                if (iteration == 0) {
                    // if (iteration == 0 && specCounter % 20 == 0)
                    // {
                    saveMapping(specificationCounter, iterationDirectory, "baseline", experiment);
                }
                writeResults(dirName, baselines);

            }
        }

    }

    public List<Experiment> loadBaselines(int iteration) {
        List<Experiment> baselines = new ArrayList<Experiment>(specifications.size());
        for (int i = 0; i < specifications.size(); i++) {
            baselines.add(i, null);
        }
        int fileNameCounter = 0;
        for (String file : FileNames) {
            if (file.equals("OptimizedSpecifications.csv"))
                continue;
            BufferedReader br = null;
            try {
                String csvFile = this.baseDirectory + "baseline/" + iteration + "/" + file;
                br = new BufferedReader(new FileReader(csvFile));
                int specCounter = 0;
                String line = "";
                String header = br.readLine();
                while ((line = br.readLine()) != null) {
                    String specification = specifications.get(specCounter);
                    Experiment experiment = null;
                    if (baselines.get(specCounter) == null)
                        experiment = new Experiment(specification);
                    else
                        experiment = baselines.get(specCounter);

                    float value = Float.parseFloat(line);
                    experiment.getStatistics()[fileNameCounter] = value;
                    baselines.set(specCounter, experiment);
                    specCounter++;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            fileNameCounter++;
        }

        return baselines;

    }

    public void runAllConfigs() {
        for (int iteration = 0; iteration < iterations; iteration++) {
            logger.info("\n\n");
            logger.info("Loading baseline for iteration: " + iteration);
            List<Experiment> baselines = loadBaselines(iteration);
            // System.exit(1);
            int specCounter = 0;

            for (String specification : specifications) {
                logger.info("Iteration: " + iteration);
                logger.info("Running for k: " + recall + " and maxTime: " + maxRefinement);

                LinkedHashMap<String, Experiment> experiments = new LinkedHashMap<String, Experiment>();
                // experiments for each specification
                experiments = new LinkedHashMap<String, Experiment>();
                // just add baseline experiment first
                Experiment baselineExperiment = baselines.get(specCounter);
                experiments.put("baseline", baselineExperiment);
                // for each different optimizer typer,
                // do the following
                for (int i = 0; i < OptimizerType.size(); i++) {
                    logger.info("+++++++++++++++++++++++++++++++" + OptimizerType.get(i)
                            + "+++++++++++++++++++++++++++++++");
                    Experiment experiment = new Experiment(specification);
                    // create optimizer
                    RecallOptimizer rr = RecallOptimizerFactory.getOptimizer(OptimizerType.get(i), source.size(),
                            target.size(), oracle, recall);
                    // optimize
                    experiment.optimize(rr, maxRefinement);
                    // run the experiment
                    experiment.run(ee);

                    experiments.put(OptimizerType.get(i), experiment);
                }

                // iteration folder
                String str1 = this.baseDirectory + "/" + iteration;
                File dirName = new File(str1);
                if (!dirName.isDirectory()) {
                    try {
                        dirName.mkdir();
                    } catch (SecurityException se) {
                    }
                }
                // max optimization time folder
                String str2 = str1 + "/" + maxRefinement;
                dirName = new File(str2);
                if (!dirName.isDirectory()) {
                    try {
                        dirName.mkdir();
                    } catch (SecurityException se) {
                    }
                }
                // k folder
                String DirectoryName = str2 + "/" + String.valueOf(recall * 100) + "%";
                dirName = new File(DirectoryName);
                if (!dirName.isDirectory()) {
                    try {
                        dirName.mkdir();
                    } catch (SecurityException se) {
                    }
                }
                // report links for each experiment
                for (Map.Entry<String, Experiment> entry : experiments.entrySet()) {
                    String OptimizerType = entry.getKey();
                    Experiment experiment = entry.getValue();
                    if (iteration == 0) {
                        // if (iteration == 0 && specCounter % 20 == 0)
                        // {
                        if (OptimizerType.equals("baseline"))
                            continue;
                        saveMapping(specCounter + 1, DirectoryName, OptimizerType, experiment);
                    }

                }
                writeResults(dirName, experiments);
                specCounter++;

            }
        }

    }

    public static void main(String args[]) {

        DatasetConfiguration dataCr = null;
        String baselineRun = null;
        double k = 0;
        int maxRefinement = 0;
        if (args.length > 0) {
            dataCr = new DatasetConfiguration(args[0]);

            if (args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("no"))
                baselineRun = args[1];
            else
                System.exit(1);

            try {
                k = Double.parseDouble(args[2]);
                if (k < 0.0d || k > 1.0d)
                    System.exit(1);

            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[2] + " must be an integer.");
                System.exit(1);
            }

            try {
                maxRefinement = Integer.parseInt(args[3]);

            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[3] + " must be a double.");
                System.exit(1);
            }

        } else
            System.exit(1);

        String DatasetName = dataCr.getDatasets().get(0);

        logger.info("Current dataset: " + DatasetName);
        dataCr.setCurrentData(DatasetName);

        String SpecificationsFileName = "datasets/" + DatasetName + "/specifications/specifications_smaller.txt";
        List<String> specifications = dataCr.getSpecifications(SpecificationsFileName);

        SimpleOracle or = new SimpleOracle(DatasetName);
        or.loadOracle("max");

        String BaseDirectory = null;
        BaseDirectory = "datasets/" + DatasetName + "/results_smaller/";
        File dirName = new File(BaseDirectory);
        if (!dirName.isDirectory()) {
            try {
                dirName.mkdir();
            } catch (SecurityException se) {
            }
        }
        if (baselineRun.equalsIgnoreCase("no")) {
            // load baselines
            RecallOptimizerEvaluation exp = new RecallOptimizerEvaluation(BaseDirectory, specifications, or,
                    dataCr.getSource(), dataCr.getTarget(), k, maxRefinement);
            exp.runAllConfigs();
            exp = null;
        } else {
            // run baseline
            BaseDirectory = BaseDirectory + "baseline/";
            dirName = new File(BaseDirectory);
            if (!dirName.isDirectory()) {
                try {
                    dirName.mkdir();
                } catch (SecurityException se) {
                }
            }
            RecallOptimizerEvaluation exp = new RecallOptimizerEvaluation(BaseDirectory, specifications, or,
                    dataCr.getSource(), dataCr.getTarget());
            exp.runBaseline();
            exp = null;
        }

    }

}