/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.measures;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.cache.MemoryCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.mapper.AtomicMapper;
import de.uni_leipzig.simba.mapper.atomic.EDJoin;
import de.uni_leipzig.simba.measures.string.JaccardMeasure;
import de.uni_leipzig.simba.measures.string.QGramSimilarity;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author ngonga
 */
public class MeasureProcessor {

    static Logger logger = Logger.getLogger("LIMES");

    /**
     * Computes a list that contains all measures used in a given expression
     * 
     * @param expression
     *            Expression
     * @return List of all measures used
     */
    public static List<String> getMeasures(String expression) {
        List<String> results = new ArrayList<String>();
        Parser p = new Parser(expression, 0);
        if (p.isAtomic()) {
            results.add(p.getOperation());
        } else {
            results.addAll(getMeasures(p.getTerm1()));
            results.addAll(getMeasures(p.getTerm2()));
        }
        return results;
    }

    public static double getSimilarity(Instance sourceInstance, Instance targetInstance, String expression,
            String sourceVar, String targetVar) {
        Parser p = new Parser(expression, 0);

        if (p.isAtomic()) {
            // System.out.println("ATOMIC");
            Measure measure = MeasureFactory.getMeasure(p.getOperation());
            AtomicMapper mapper = MeasureFactory.getMapper(p.getOperation());
            Cache source = new HybridCache();
            Cache target = new HybridCache();
            source.addInstance(sourceInstance);
            target.addInstance(targetInstance);
            // System.out.println("Measure = " + measure.getName());
            // get property name
            // 0. get properties
            // get property labels
            // get first property label
            String property1 = null, property2 = null;
            // get property labels

            // get first property label
            String term1 = "?" + p.getTerm1();
            String term2 = "?" + p.getTerm2();
            String split[];
            String var;

            String property = "";
            if (term1.contains(".")) {
                split = term1.split("\\.");
                var = split[0];
                property = split[1];
                if (split.length >= 2) {
                    for (int i = 2; i < split.length; i++) {
                        property = property + "." + split[i];
                    }
                }
                if (var.equals(sourceVar)) {
                    // property1 = split[1];
                    property1 = property;
                } else {
                    // property2 = split[1];
                    property2 = property;
                }
            } else {
                property1 = term1;
            }

            // get second property label
            if (term2.contains(".")) {
                split = term2.split("\\.");
                var = split[0];
                property = split[1];
                if (split.length >= 2) {
                    for (int i = 2; i < split.length; i++) {
                        property = property + "." + split[i];
                    }
                }
                if (var.equals(sourceVar)) {
                    // property1 = split[1];
                    property1 = property;
                } else {
                    // property2 = split[1];
                    property2 = property;
                }
            } else {
                property2 = term2;
            }

            // if no properties then terminate
            if (property1 == null || property2 == null) {
                logger.fatal("Property values could not be read. Exiting");
                // System.exit(1);
            } else {
                if (mapper instanceof EDJoin || measure instanceof QGramSimilarity)
                    return measure.getSimilarity(sourceInstance, targetInstance, property1, property2);

                Mapping m = mapper.getMapping(source, target, sourceVar, targetVar, expression, 0);
                for (String s : m.map.keySet()) {
                    for (String t : m.map.get(s).keySet()) {
                        return m.map.get(s).get(t);
                    }
                }
                // return measure.getSimilarity(sourceInstance, targetInstance,
                // property1, property2);

            }

        } else {
            if (p.op.equalsIgnoreCase("MAX") | p.op.equalsIgnoreCase("OR") | p.op.equalsIgnoreCase("XOR")) {
                return Math.max(getSimilarity(sourceInstance, targetInstance, p.getTerm1(), sourceVar, targetVar),
                        getSimilarity(sourceInstance, targetInstance, p.getTerm2(), sourceVar, targetVar));
            }
            if (p.op.equalsIgnoreCase("MIN") | p.op.equalsIgnoreCase("AND")) {
                return Math.min(getSimilarity(sourceInstance, targetInstance, p.getTerm1(), sourceVar, targetVar),
                        getSimilarity(sourceInstance, targetInstance, p.getTerm2(), sourceVar, targetVar));
            }
            if (p.op.equalsIgnoreCase("ADD")) {
                return p.coef1 * getSimilarity(sourceInstance, targetInstance, p.getTerm1(), sourceVar, targetVar)
                        + p.coef2 * getSimilarity(sourceInstance, targetInstance, p.getTerm2(), sourceVar, targetVar);
            } else {
                logger.warn("Not sure what to do with operator " + p.op + ". Using MAX.");
                return Math.max(getSimilarity(sourceInstance, targetInstance, p.getTerm1(), sourceVar, targetVar),
                        getSimilarity(sourceInstance, targetInstance, p.getTerm2(), sourceVar, targetVar));
            }
        }
        return 0;

    }

    /*
     * When computing similarities using a metric that has PPJoinPlusPlus as
     * mapper, the results returned by the measure (measure.getSimilarity) and
     * by the mapper (mapper.getMapping) are different. PPJoinPlusPlus has a
     * bug. However, MeasureProcessor.getSimilarity is used ONLY by the Helios
     * and the Dynamic Planner, because they include filters with metric
     * expressions. Canonical planner computes filters without a metric
     * expressions, hence this function is never called. In order to make sure
     * that all results returned by all planners are comparable with equal size,
     * we create a Caches for source and target with one instance each and
     * instead of using measure.getSimilarity as before, we use the
     * corresponding mapper. Be aware that EDJoin and QGramsSimilarity do not
     * work with Caches of one instance.
     */
    public static double getSimilarity(Instance sourceInstance, Instance targetInstance, String expression,
            double threshold, String sourceVar, String targetVar) {

        Parser p = new Parser(expression, threshold);
        // logger.info(expression);
        // logger.info(threshold);
        if (p.isAtomic()) {
            // System.out.println("ATOMIC");
            Measure measure = MeasureFactory.getMeasure(p.getOperation());
            AtomicMapper mapper = MeasureFactory.getMapper(p.getOperation());
            Cache source = new HybridCache();
            Cache target = new HybridCache();
            source.addInstance(sourceInstance);
            target.addInstance(targetInstance);
            // System.out.println("Measure = " + measure.getName());
            // get property name
            // 0. get properties
            // get property labels
            // get first property label
            String property1 = null, property2 = null;
            // get property labels

            // get first property label
            String term1 = "?" + p.getTerm1();
            String term2 = "?" + p.getTerm2();
            String split[];
            String var;

            String property = "";
            if (term1.contains(".")) {
                split = term1.split("\\.");
                var = split[0];
                property = split[1];
                if (split.length >= 2) {
                    for (int i = 2; i < split.length; i++) {
                        property = property + "." + split[i];
                    }
                }
                if (var.equals(sourceVar)) {
                    // property1 = split[1];
                    property1 = property;
                } else {
                    // property2 = split[1];
                    property2 = property;
                }
            } else {
                property1 = term1;
            }

            // get second property label
            if (term2.contains(".")) {
                split = term2.split("\\.");
                var = split[0];
                property = split[1];
                if (split.length >= 2) {
                    for (int i = 2; i < split.length; i++) {
                        property = property + "." + split[i];
                    }
                }
                if (var.equals(sourceVar)) {
                    // property1 = split[1];
                    property1 = property;
                } else {
                    // property2 = split[1];
                    property2 = property;
                }
            } else {
                property2 = term2;
            }

            // if no properties then terminate
            if (property1 == null || property2 == null) {
                logger.fatal("Property values could not be read. Exiting");
                // System.exit(1);
            } else {
                if (mapper instanceof EDJoin || measure instanceof QGramSimilarity)
                    return measure.getSimilarity(sourceInstance, targetInstance, property1, property2);

                Mapping m = mapper.getMapping(source, target, sourceVar, targetVar, expression, threshold);
                for (String s : m.map.keySet()) {
                    for (String t : m.map.get(s).keySet()) {
                        return m.map.get(s).get(t);
                    }
                }
                // return measure.getSimilarity(sourceInstance, targetInstance,
                // property1, property2);
            }
        } else {
            if (p.op.equalsIgnoreCase("MAX") | p.op.equalsIgnoreCase("OR") | p.op.equalsIgnoreCase("XOR")) {
                double parentThreshold = p.threshold;
                double firstChild = getSimilarity(sourceInstance, targetInstance, p.getTerm1(), p.threshold1, sourceVar,
                        targetVar);
                double secondChild = getSimilarity(sourceInstance, targetInstance, p.getTerm2(), p.threshold2,
                        sourceVar, targetVar);

                double maxSimilarity = Math.max(firstChild, secondChild);
                // find max value between or terms
                if (maxSimilarity >= parentThreshold)
                    return maxSimilarity;
                else
                    return 0;
            }
            if (p.op.equalsIgnoreCase("MIN") | p.op.equalsIgnoreCase("AND")) {
                double parentThreshold = p.threshold;
                double firstChild = getSimilarity(sourceInstance, targetInstance, p.getTerm1(), p.threshold1, sourceVar,
                        targetVar);
                double secondChild = getSimilarity(sourceInstance, targetInstance, p.getTerm2(), p.threshold2,
                        sourceVar, targetVar);

                double minSimilarity = Math.min(firstChild, secondChild);
                if (firstChild == 0 && secondChild == 0)
                    return 0;
                if (minSimilarity >= parentThreshold)
                    return minSimilarity;
                else
                    return 0;
            }
            if (p.op.equalsIgnoreCase("ADD")) {
                double parentThreshold = p.threshold;
                double firstChild = p.coef1 * getSimilarity(sourceInstance, targetInstance, p.getTerm1(), p.threshold1,
                        sourceVar, targetVar);
                double secondChild = p.coef2 * getSimilarity(sourceInstance, targetInstance, p.getTerm2(), p.threshold2,
                        sourceVar, targetVar);
                if (firstChild + secondChild >= parentThreshold)
                    return firstChild + secondChild;
                else
                    return 0;

            } else {// perform MINUS as usual
                // logger.warn("Not sure what to do with operator " + p.op + ".
                // Using MAX.");
                double parentThreshold = p.threshold;
                double firstChild = getSimilarity(sourceInstance, targetInstance, p.getTerm1(), p.threshold1, sourceVar,
                        targetVar);
                double secondChild = getSimilarity(sourceInstance, targetInstance, p.getTerm2(), p.threshold2,
                        sourceVar, targetVar);
                // the second similarity must be below threshold2 in order for
                // the instance to have a change to be included at the final
                // result
                if (secondChild < p.threshold2) {
                    if (firstChild >= parentThreshold)
                        return firstChild;
                    else
                        return 0;
                } else
                    return 0;
            }
        }
        return 0;

    }

    public static void main(String args[]) {
        Cache source = new MemoryCache();
        Cache target = new MemoryCache();
        source.addTriple("S1", "pub", "test");
        source.addTriple("S1", "conf", "conf one");
        source.addTriple("S2", "pub", "test2");
        source.addTriple("S2", "conf", "conf2");

        target.addTriple("S1", "pub", "test");
        target.addTriple("S1", "conf", "conf one");
        target.addTriple("S3", "pub", "test1");
        target.addTriple("S3", "conf", "conf three");

        System.out.println(MeasureProcessor.getSimilarity(source.getInstance("S1"), target.getInstance("S3"),
                "ADD(0.5*trigram(x.conf, y.conf),0.5*cosine(y.conf, x.conf))", "?x", "?y"));

        System.out.println(MeasureProcessor
                .getMeasures("AND(jaccard(x.authors,y.authors)|0.4278,overlap(x.authors,y.authors)|0.4278)"));
        System.out.println(MeasureProcessor.getMeasures("trigrams(x.conf, y.conf)"));

    }

    /**
     * Returns the approximation of the runtime for a certain expression
     * 
     * @param measureExpression
     *            Expression
     * @param mappingSize
     *            Size of the mapping to process
     * @return Runtime approximation
     */
    public static double getCosts(String measureExpression, double mappingSize) {
        List<String> measures = getMeasures(measureExpression);
        double runtime = 0;
        for (int i = 0; i < measures.size(); i++)
            runtime = runtime + MeasureFactory.getMeasure(measures.get(i)).getRuntimeApproximation(mappingSize);
        return runtime;
    }
}
