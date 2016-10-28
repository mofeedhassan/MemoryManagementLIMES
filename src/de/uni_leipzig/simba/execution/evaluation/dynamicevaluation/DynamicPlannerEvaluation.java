package de.uni_leipzig.simba.execution.evaluation.dynamicevaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.execution.ExecutionEngine;
import de.uni_leipzig.simba.execution.NestedPlan;
import de.uni_leipzig.simba.execution.planner.CanonicalPlanner;
import de.uni_leipzig.simba.execution.planner.DynamicPlanner;
import de.uni_leipzig.simba.execution.planner.ExecutionPlanner;
import de.uni_leipzig.simba.execution.planner.HeliosPlanner;
import de.uni_leipzig.simba.execution.rewriter.AlgebraicRewriter;
import de.uni_leipzig.simba.execution.rewriter.Rewriter;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser.DataSets;
import de.uni_leipzig.simba.grecall.util.DiffPair;
import de.uni_leipzig.simba.specification.LinkSpec;

public class DynamicPlannerEvaluation {

    public List<String> specifications;
    protected Cache source;
    protected Cache target;
    private String baseDirectory;
    protected ExecutionEngine ee;
    static Logger logger = Logger.getLogger("LIMES");
    public String[] files = { "Overall.csv", "PlanningTime.csv", "ExecutionTime.csv", "MappingSize.csv", "Plan.txt" };

    class Output {
        // 0: Overall Runtime
        // 1: Planning Time
        // 2: Execution Time
        // 3: Mapping Size
        Long[] statistics = new Long[4];
        String finalPlan = "";
    }

    public DynamicPlannerEvaluation(String baseDirectory, Cache source, Cache target, List<String> specs) {

        logger.info("Source size = " + source.getAllUris().size());
        logger.info("Target size = " + target.getAllUris().size());
        this.source = source;
        this.target = target;
        specifications = specs;
        this.baseDirectory = baseDirectory;
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
        logger.setLevel(Level.DEBUG);
        logger.info("Running on " + specifications.size() + " specs");
    }

    public void runAllConfigs(String outputFile, int iterations) {

        try {
            Output cp = new Output();
            Output cpr = new Output();
            Output hp = new Output();
            Output hpr = new Output();
            Output dp = new Output();
            Output dpr = new Output();

            for (String spec : specifications) {
                logger.info("===================Running spec " + spec + " ====================");
                try {

                    for (int i = 0; i < iterations; i++) {
                        File folder = new File(outputFile + "/" + i);
                        if (!folder.isDirectory()) {
                            try {
                                folder.mkdir();
                            } catch (SecurityException se) {
                            }
                        }

                        // runExperiment(cp, "c", false, spec);
                        // runExperiment(cpr, "c", true, spec);
                        // runExperiment(hp, "h", false, spec);
                        // runExperiment(hpr, "h", true, spec);
                        runExperiment(dp, "d", false, spec);
                        // runExperiment(dpr, "d", true, spec);
                        for (int k = 0; k < files.length; k++) {

                            String filename = folder + "/" + files[k];
                            FileWriter writer = null;
                            File f = new File(filename);
                            if (f.exists()) {
                                writer = new FileWriter(f, true);
                            } else {
                                writer = new FileWriter(f);
                                // writer.append("CP\tRW+CP\tHP\tRW+HP\tDP\tRW+DP\n");
                                if (k != 4)
                                    // writer.append("CP\tHP\tDP\n");
                                    writer.append("CP\tRW+CP\tHP\tRW+HP\tDP\tRW+DP\n");
                            }
                            DecimalFormat df = new DecimalFormat("#");
                            df = new DecimalFormat("#");
                            df.setMaximumFractionDigits(50);
                            if (k != 4) {
                                writer.append(df.format(cp.statistics[k]) + "\t" + df.format(cpr.statistics[k]) + "\t"
                                        + df.format(hp.statistics[k]) + "\t" + df.format(hpr.statistics[k]) + "\t"
                                        + df.format(dp.statistics[k]) + "\t" + df.format(dpr.statistics[k]) + "\n");
                                logger.info(files[k]);
                                logger.info(df.format(cp.statistics[k]) + "\t" + df.format(cpr.statistics[k]) + "\t"
                                        + df.format(hp.statistics[k]) + "\t" + df.format(hpr.statistics[k]) + "\t"
                                        + df.format(dp.statistics[k]) + "\t" + df.format(dpr.statistics[k]) + "\n");
                            } else {
                                writer.append("\n---Canonical Plan---\n" + cp.finalPlan
                                        + "\n---Canonical Plan with Rewriter---\n" + cpr.finalPlan
                                        + "\n---Helios Plan---\n" + hp.finalPlan + "\n---Helios Plan with Rewriter---\n"
                                        + hpr.finalPlan + "\n---Dynamic Plan---\n" + dp.finalPlan
                                        + "\n---Dynamic Plan with Rewriter---\n" + dpr.finalPlan + "\n");
                                writer.append("---------------------------------------------------------------\n");
                                logger.info("\n---Canonical Plan---\n" + cp.finalPlan
                                        + "\n---Canonical Plan with Rewriter---\n" + cpr.finalPlan
                                        + "\n---Helios Plan---\n" + hp.finalPlan + "\n---Helios Plan with Rewriter---\n"
                                        + hpr.finalPlan + "\n---Dynamic Plan---\n" + dp.finalPlan
                                        + "\n---Dynamic Plan with Rewriter---\n" + dpr.finalPlan + "\n");
                            }
                            writer.flush();
                            writer.close();
                        }
                    }
                } catch (Exception e) {
                    logger.info("Error running " + spec);
                    e.printStackTrace();
                }
                logger.info("==========================================================");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runExperiment(Output output, String planner, boolean rewrite, String specification) {
        ExecutionPlanner p;

        Mapping m = new Mapping();
        LinkSpec spec = new LinkSpec(specification.split(Pattern.quote(">="))[0],
                Double.parseDouble(specification.split(Pattern.quote(">="))[1]), true);
        long begin, end = Long.MAX_VALUE;
        logger.info("---------------------------------------");
        logger.info(spec);
        // create planner
        logger.info("Planner is: " + planner + " rewrite is: " + rewrite);

        Rewriter rewriter = new AlgebraicRewriter();

        begin = System.currentTimeMillis();
        // rewrite spec if required
        if (rewrite) {
            spec = rewriter.rewrite(spec);
        }
        if (planner.startsWith("c")) {
            p = new CanonicalPlanner();
        } else if (planner.startsWith("h")) {
            p = new HeliosPlanner(source, target);
        } else
            p = new DynamicPlanner(source, target, spec);

        NestedPlan np = new NestedPlan();
        // generate plan and run
        if (p instanceof CanonicalPlanner || p instanceof HeliosPlanner) {
            np = p.plan(spec);
            long b = System.currentTimeMillis();
            m = ee.runNestedPlan(np);
            long e = System.currentTimeMillis();
            // execution time
            output.statistics[2] = e - b;

        } else if (p instanceof DynamicPlanner) {
            m = ee.runNestedPlan(spec, (DynamicPlanner) p);
            // execution time
            output.statistics[2] = (long) ee.getDETime();
        }
        end = System.currentTimeMillis();

        output.statistics[0] = end - begin;
        output.statistics[1] = output.statistics[0] - output.statistics[2];
        output.statistics[3] = (long) m.getNumberofMappings();

        if (p instanceof CanonicalPlanner || p instanceof HeliosPlanner) {
            output.finalPlan = np.finalPlan();
        } else
            output.finalPlan = p.getFinalPlan();

        logger.info(output.finalPlan);

        ee.resetExecutionEngine();
        logger.info("---------------------------------------");

    }

    public static void main(String args[]) {
        DataConfiguration dataCr = null;
        if (args.length != 0) {
            dataCr = new DataConfiguration(args[0]);
        } else
            System.exit(1);
        String DatasetName = args[0];
        logger.info("Current dataset: " + DatasetName);
        EvaluationData data = dataCr.getDataset();

        // read specs
        String SpecificationsFileName = "datasets/" + DatasetName + "/specifications/specifications.txt";
        List<String> specifications = dataCr.getSpecifications(SpecificationsFileName);

        // create results folder
        String BaseDirectory = "datasets/" + DatasetName + "/planner_results_smaller/";
        File dirName = new File(BaseDirectory);
        if (!dirName.isDirectory()) {
            try {
                dirName.mkdir();
            } catch (SecurityException se) {
            }
        }

        DynamicPlannerEvaluation exp = new DynamicPlannerEvaluation(BaseDirectory, dataCr.getSource(),
                dataCr.getTarget(), specifications);
        exp.runAllConfigs(BaseDirectory, 1);

        /*
         * ArrayList<String> specs = (ArrayList<String>)
         * dataCr.getSpecifications("datasets/" + DatasetName +
         * "/specifications/specifications.txt");
         * 
         * double avg = 0.0; for (String sp : specs) { LinkSpec spec = new
         * LinkSpec(sp.split(Pattern.quote(">="))[0],
         * Double.parseDouble(sp.split(Pattern.quote(">="))[1]), true); avg +=
         * spec.size(); } logger.info(avg/100);
         */
    }

}
