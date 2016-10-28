package de.uni_leipzig.simba.execution.planner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.execution.ExecutionEngine;
import de.uni_leipzig.simba.execution.Instruction;
import de.uni_leipzig.simba.execution.Instruction.Command;
import de.uni_leipzig.simba.execution.NestedPlan;
import de.uni_leipzig.simba.execution.evaluation.dynamicevaluation.DataConfiguration;
import de.uni_leipzig.simba.mapper.AtomicMapper.Language;
import de.uni_leipzig.simba.mapper.atomic.EDJoin;
import de.uni_leipzig.simba.mapper.atomic.PPJoinPlusPlus;
import de.uni_leipzig.simba.mapper.atomic.TotalOrderBlockingMapper;
import de.uni_leipzig.simba.mapper.atomic.fastngram.FastNGram;
import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.measures.MeasureFactory;
import de.uni_leipzig.simba.measures.MeasureProcessor;
import de.uni_leipzig.simba.specification.LinkSpec;
import de.uni_leipzig.simba.specification.Operator;

public class DynamicPlanner implements ExecutionPlanner {

    static Logger logger = Logger.getLogger("LIMES");
    public Cache source;
    public Cache target;
    public Language lang;
    // <String representation of LinkSpec, corresponding plan>
    public LinkedHashMap<String, NestedPlan> allPlans = new LinkedHashMap<String, NestedPlan>();
    // <String representation of LinkSpec, LinkSpec>
    public LinkedHashMap<String, LinkSpec> specifications = new LinkedHashMap<String, LinkSpec>();
    // <String represantion of LinkSpec A, LinkSpec B>
    // where LinkSpec B and C are subsumption of LinkSpec A
    public LinkedHashMap<String, LinkSpec> dependencies = new LinkedHashMap<String, LinkSpec>();
    public String finalPlan = "";

    /**
     * Constructor. Caches are needed for stats.
     *
     * @param s
     *            Source cache
     * @param t
     *            Target get
     */
    public DynamicPlanner(Cache s, Cache t, LinkSpec spec) {
        source = s;
        target = t;
        lang = Language.EN;
        initSteps(spec);
    }

    @Override
    /**
     * Returns string representation of the final plan as it was executed from
     * the execution engine.
     * 
     * @return string representation of final plan
     * 
     */
    public String getFinalPlan() {
        return finalPlan;
    }

    /**
     * Initialize allPlans map
     *
     * @param spec,
     *            the original link specification
     */
    public void initSteps(LinkSpec spec) {
        NestedPlan plan = new NestedPlan();
        if (!allPlans.containsKey(spec.toString())) {
            if (spec.isAtomic()) {
                allPlans.put(spec.toString(), plan);
                specifications.put(spec.toString(), spec);
            } else {
                for (LinkSpec child : spec.children) {
                    initSteps(child);
                }
                allPlans.put(spec.toString(), plan);
                specifications.put(spec.toString(), spec);
            }
        }
    }

    /**
     * Create/Update dependency between recently executed specification and
     * other specification(s). A specification L2 is dependent on an executed
     * specification L1 if: L1 and L2 have the same metric expression and L1
     * threshold < L2 threshold. Using this definition, L2 is a subsumption of
     * L1. Therefore, the execution of the initial specification L is
     * speeded-up. Instead of fully executing L2, dynamic planner informs the
     * execution engine about the dependency between L2 and L1, and the
     * execution engine retrieves the mapping of L1 from the results buffer and
     * creates a temporary filtering instruction in order to get L2's mapping
     * from L1's mapping. If L2 is dependent on L1 but it is already dependent
     * on another specification L3, then if L1's threshold must be higher than
     * L3' threshold in order to replace the previous L2-L3 dependency.
     * 
     * @param spec,
     *            the recently executed specification
     */
    public void createDependencies(LinkSpec spec) {
        for (Entry<String, LinkSpec> entry : specifications.entrySet()) {
            String dependentString = entry.getKey();
            LinkSpec dependent = entry.getValue();
            if (spec.fullExpression.equals(dependent.fullExpression) && spec.threshold < dependent.threshold) {
                if (dependencies.containsKey(dependentString)) {
                    LinkSpec oldDependent = dependencies.get(dependentString);
                    if (oldDependent.threshold < spec.threshold)
                        dependencies.put(dependentString, spec);
                } else
                    dependencies.put(dependentString, spec);
            }
        }
    }

    /**
     * Finds and returns specification that the specification parameter is
     * dependent upon, if any.
     * 
     * @param spec,
     *            the dependent specification
     * 
     * @return string representation of specification that spec depends upon
     */
    public String getDependency(LinkSpec spec) {
        String specString = spec.toString();
        if (dependencies.containsKey(specString)) {
            return dependencies.get(spec.toString()).toString();
        }
        return null;
    }

    /**
     * Computes atomic costs for a measure
     *
     * @param measure
     * @param threshold
     * @return
     */
    public double getAtomicRuntimeCosts(String measure, double threshold) {
        Measure m = MeasureFactory.getMeasure(measure);
        double runtime;
        if (m.getName().equalsIgnoreCase("levenshtein")) {
            runtime = (new EDJoin()).getRuntimeApproximation(source.size(), target.size(), threshold, lang);
        } else if (m.getName().equalsIgnoreCase("euclidean")) {
            runtime = (new TotalOrderBlockingMapper()).getRuntimeApproximation(source.size(), target.size(), threshold,
                    lang);
        } else if (m.getName().equalsIgnoreCase("qgrams")) {
            runtime = (new FastNGram()).getRuntimeApproximation(source.size(), target.size(), threshold, lang);
        } else {
            runtime = (new PPJoinPlusPlus()).getRuntimeApproximation(source.size(), target.size(), threshold, lang);
        }
        return runtime;
    }

    /**
     * checks if the plan of the specified link specification is executed
     * 
     * @return true if the plan is executed
     */
    public boolean isExecuted(LinkSpec spec) {
        return (allPlans.get(spec.toString()).isExecuted);
    }

    /**
     * Computes atomic mapping sizes for a measure
     *
     * @param measure
     * @param threshold
     * @return
     */
    public double getAtomicMappingSizes(String measure, double threshold) {
        Measure m = MeasureFactory.getMeasure(measure);
        double size;
        if (m.getName().equalsIgnoreCase("levenshtein")) {
            size = (new EDJoin()).getMappingSizeApproximation(source.size(), target.size(), threshold, lang);
        }
        if (m.getName().equalsIgnoreCase("euclidean")) {
            size = (new TotalOrderBlockingMapper()).getMappingSizeApproximation(source.size(), target.size(), threshold,
                    lang);
        }
        if (m.getName().equalsIgnoreCase("qgrams")) {
            size = (new FastNGram()).getMappingSizeApproximation(source.size(), target.size(), threshold, lang);
        } else {
            size = (new PPJoinPlusPlus()).getMappingSizeApproximation(source.size(), target.size(), threshold, lang);
        }
        return size;
    }

    /**
     * Computes costs for a filtering
     *
     * @param filterExpression
     *            Expression used to filter
     * @param mappingSize
     *            Size of mapping
     * @return Costs for filtering
     */
    public double getFilterCosts(List<String> measures, int mappingSize) {
        // need costs for single operations on measures
        // = MeasureProcessor.getMeasures(filterExpression);
        double cost = 0;
        for (String measure : measures) {
            cost = cost + MeasureFactory.getMeasure(measure).getRuntimeApproximation(mappingSize);
        }
        return cost;
    }

    public NestedPlan getPlan(LinkSpec spec) {
        if (allPlans.containsKey(spec.toString()))
            return allPlans.get(spec.toString());
        return null;
    }

    public LinkSpec getLinkSpec(NestedPlan plan) {
        for (Map.Entry<String, NestedPlan> entry : allPlans.entrySet()) {
            String spec = entry.getKey();
            NestedPlan value = entry.getValue();
            if (value.equals(plan))
                return specifications.get(spec);
        }
        return null;
    }

    /**
     * Updates the characteristics of a plan
     *
     * @param spec,
     *            the corresponding link specification
     * @param rt,
     *            the real runtime of the plan
     * @param selectivity,
     *            the real selectivity of the plan
     * @param msize,
     *            the real mapping size returned when the plan is executed
     */
    public void updatePlan(LinkSpec spec, double rt, double selectivity, double msize) {
        if (!allPlans.containsKey(spec.toString())) {
            logger.error("Specification: " + spec.fullExpression + " was not initialised. Exiting..");
            System.exit(1);
        }
        NestedPlan plan = allPlans.get(spec.toString());
        plan.runtimeCost = rt;
        plan.selectivity = selectivity;
        plan.mappingSize = msize;
        plan.isExecuted = true;
        // logger.info("Runtime is: " + plan.runtimeCost + " mappingsize is: " +
        // plan.mappingSize + " selectivity is: "
        // + plan.selectivity);
        allPlans.put(spec.toString(), plan);
        createDependencies(spec);
    }

    public NestedPlan plan(LinkSpec spec) {
        return plan(spec, source, target, new Mapping(), new Mapping());

    }

    /**
     * Generates a instructionList based on the optimality assumption used in
     * databases
     *
     * @param spec
     *            Specification for which a instructionList is needed
     * @param source
     *            Source cache
     * @param target
     *            Target cache
     * @param sourceMapping
     *            Size of source mapping
     * @param targetMapping
     *            Size of target mapping
     * @return Nested instructionList for the given spec
     */
    public NestedPlan plan(LinkSpec spec, Cache source, Cache target, Mapping sourceMapping, Mapping targetMapping) {
        NestedPlan plan = new NestedPlan();
        // if plan is executed, just return the plan
        // remember that the plan is automatically updated once it is executed
        plan = allPlans.get(spec.toString());
        if (plan.isExecuted) {
            // logger.info("Already executed");
            return plan;
        }
        plan = new NestedPlan();
        // atomic specs are simply ran
        if (spec.isAtomic()) {
            Parser p = new Parser(spec.getFilterExpression(), spec.threshold);
            plan.instructionList = new ArrayList<Instruction>();
            plan.addInstruction(new Instruction(Instruction.Command.RUN, spec.getFilterExpression(),
                    spec.threshold + "", -1, -1, 0));
            plan.runtimeCost = getAtomicRuntimeCosts(p.getOperation(), spec.threshold);
            plan.mappingSize = getAtomicMappingSizes(p.getOperation(), spec.threshold);
            plan.selectivity = plan.mappingSize / (double) (source.size() * target.size());

        } else {
            if (spec.operator.equals(Operator.OR)) {
                List<NestedPlan> children = new ArrayList<NestedPlan>();
                double runtimeCost = 0;
                for (LinkSpec child : spec.children) {
                    NestedPlan childPlan = plan(child, source, target, sourceMapping, targetMapping);
                    logger.info("Child is: " + child + " with plan: " + childPlan);
                    children.add(childPlan);
                    runtimeCost = runtimeCost + childPlan.runtimeCost;
                }
                // RUNTIME
                plan.runtimeCost = runtimeCost + (spec.children.size() - 1);
                // SUBPLANS
                plan.subPlans = children;
                // FILTERING INSTRUCTION
                plan.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                        spec.threshold + "", -1, -1, 0);
                // OPERATOR
                plan.operator = Instruction.Command.UNION;
                // SELECTIVITY
                double selectivity = 1 - children.get(0).selectivity;
                for (int i = 1; i < children.size(); i++) {
                    selectivity = selectivity * (1 - children.get(i).selectivity);
                }
                plan.selectivity = 1 - selectivity;
                // MAPPING SIZE
                plan.mappingSize = source.size() * target.size() * plan.selectivity;
            } else if (spec.operator.equals(Operator.XOR)) {
                List<NestedPlan> children = new ArrayList<NestedPlan>();
                double runtimeCost = 0;
                for (LinkSpec child : spec.children) {
                    NestedPlan childPlan = plan(child, source, target, sourceMapping, targetMapping);
                    children.add(childPlan);
                    runtimeCost = runtimeCost + childPlan.runtimeCost;
                }
                // RUNTIME
                plan.runtimeCost = runtimeCost + (spec.children.size() - 1);
                // SUBPLANS
                plan.subPlans = children;
                // FILTERING INSTRUCTION
                plan.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                        spec.threshold + "", -1, -1, 0);
                // OPERATOR
                plan.operator = Instruction.Command.XOR;
                // SELECTIVITY
                // A XOR B = (A U B) \ (A & B)
                double selectivity = children.get(0).selectivity;
                for (int i = 1; i < children.size(); i++) {
                    selectivity = (1 - (1 - selectivity) * (1 - children.get(i).selectivity))
                            * (1 - selectivity * children.get(i).selectivity);
                }
                plan.selectivity = selectivity;
                // MAPPING SIZE
                plan.mappingSize = source.size() * target.size() * plan.selectivity;

            } else if (spec.operator.equals(Operator.MINUS)) {
                List<NestedPlan> children = new ArrayList<NestedPlan>();
                plan.runtimeCost = 0;
                for (LinkSpec child : spec.children) {
                    NestedPlan childPlan = plan(child, source, target, sourceMapping, targetMapping);
                    children.add(childPlan);
                }
                // SELECTIVITY
                double selectivity = children.get(0).selectivity;
                for (int i = 1; i < children.size(); i++) {
                    // selectivity is not influenced by bestConjuctivePlan
                    selectivity = selectivity * (1 - children.get(i).selectivity);
                }
                plan = getBestDifferencePlan(spec, children.get(0), children.get(1), selectivity);

            } else if (spec.operator.equals(Operator.AND)) {
                List<NestedPlan> children = new ArrayList<NestedPlan>();
                plan.runtimeCost = 0;
                for (LinkSpec child : spec.children) {
                    NestedPlan childPlan = plan(child, source, target, sourceMapping, targetMapping);
                    children.add(childPlan);
                }
                // SELECTIVITY
                double selectivity = 1d;
                for (int i = 1; i < children.size(); i++) {
                    // selectivity is not influenced by bestConjuctivePlan
                    selectivity = selectivity * children.get(i).selectivity;
                }
                // this puts all options to this.steps and returns the best plan
                plan = getBestConjunctivePlan(spec, children.get(0), children.get(1), selectivity);
            }
        }
        this.allPlans.put(spec.toString(), plan);
        // logger.info("--------------------------------------------------------------------");
        return plan;

    }

    /**
     * Puts all the options to this.steps Computes the best difference
     * instructionList for one pair of nested plans
     *
     * @param left
     *            Left instructionList
     * @param right
     *            Right instructionList
     * @param selectivity
     * @return
     */
    public NestedPlan getBestDifferencePlan(LinkSpec spec, NestedPlan left, NestedPlan right, double selectivity) {
        double runtime1 = 0, runtime2 = 0;
        NestedPlan result = new NestedPlan();
        double mappingSize = source.size() * target.size() * selectivity;

        // both children are executed: do DIFF
        if (left.isExecuted && right.isExecuted) {
            // OPERATOR
            result.operator = Instruction.Command.DIFF;
            // SUBPLANS
            List<NestedPlan> plans = new ArrayList<NestedPlan>();
            plans.add(left);
            plans.add(right);
            result.subPlans = plans;
            // FILTERING INSTRUCTION
            result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                    spec.threshold + "", -1, -1, 0);
            // RUNTIME
            // result.runtimeCost = left.runtimeCost + right.runtimeCost;
            result.runtimeCost = 0.0d;
            // SELECTIVITY
            result.selectivity = selectivity;
            // MAPPING SIZE
            result.mappingSize = mappingSize;
            return result;
        } // if right child is executed, then there is one option: run left and
          // then do filter
        else if (!left.isExecuted && right.isExecuted) {
            // OPERATOR
            result.operator = Instruction.Command.DIFF;
            // FILTERING INSTRUCTION
            result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                    spec.threshold + "", -1, -1, 0);
            // SUBPLANS
            List<NestedPlan> plans = new ArrayList<NestedPlan>();
            plans.add(left);
            plans.add(right);
            result.subPlans = plans;
            // RUNTIME
            // seems like a good idea ..
            // result.runtimeCost = left.runtimeCost + right.runtimeCost;
            result.runtimeCost = left.runtimeCost;
            // SELECTIVITY
            result.selectivity = selectivity;
            // MAPPING SIZE
            result.mappingSize = mappingSize;
            return result;
        }
        // if left is/isn't executed and right is not executed: run right, DIFF
        // OR REVERSEFILTER with right
        // never add the runtime of left if it is already executed
        // first instructionList: run both children and then merge
        if (!left.isExecuted)
            runtime1 = left.runtimeCost;
        runtime1 = runtime1 + right.runtimeCost;
        ////////////////////////////////////////////////////////////////////////
        // second instructionList: run left child and use right child as filter
        if (!left.isExecuted)
            runtime2 = left.runtimeCost;
        runtime2 = runtime2 + getFilterCosts(right.getAllMeasures(),
                (int) Math.ceil(source.size() * target.size() * right.selectivity));

        double min = Math.min(runtime1, runtime2);
        if (min == runtime1) {
            result.operator = Instruction.Command.DIFF;
            List<NestedPlan> plans = new ArrayList<NestedPlan>();
            plans.add(left);
            plans.add(right);
            result.subPlans = plans;
            result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                    spec.threshold + "", -1, -1, 0);
        } else if (min == runtime2) {
            String rightChild = spec.children.get(1).fullExpression;
            result.filteringInstruction = new Instruction(Instruction.Command.REVERSEFILTER, rightChild,
                    spec.children.get(1).threshold + "", -1, -1, 0);
            result.filteringInstruction.setMainThreshold(spec.threshold + "");
            result.operator = null;
            List<NestedPlan> plans = new ArrayList<NestedPlan>();
            plans.add(left);
            result.subPlans = plans;
        }
        result.runtimeCost = min;
        result.selectivity = selectivity;
        result.mappingSize = mappingSize;
        return result;
    }

    /**
     * Puts all the options to this.steps Computes the best conjunctive
     * instructionList for one pair of nested plans
     *
     * @param left
     *            Left instructionList
     * @param right
     *            Right instructionList
     * @param selectivity
     * @return
     */
    public NestedPlan getBestConjunctivePlan(LinkSpec spec, NestedPlan left, NestedPlan right, double selectivity) {
        double runtime1 = 0, runtime2 = 0, runtime3 = 0;
        NestedPlan result = new NestedPlan();

        // both children are executed: do AND
        if (left.isExecuted && right.isExecuted) {
            // OPERATOR
            result.operator = Instruction.Command.INTERSECTION;
            // SUBPLANS
            List<NestedPlan> plans = new ArrayList<NestedPlan>();
            plans.add(left);
            plans.add(right);
            result.subPlans = plans;
            // FILTERING INSTRUCTION
            result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                    spec.threshold + "", -1, -1, 0);
            // RUNTIME
            // result.runtimeCost = left.runtimeCost + right.runtimeCost;
            result.runtimeCost = 0.0d;
            // SELECTIVITY
            result.selectivity = selectivity;
            // MAPPING SIZE
            result.mappingSize = source.size() * target.size() * selectivity;
            return result;
        } // left is executed, right is not: RUN B, FILTER OR FILTER WITH B
        else if (left.isExecuted && !right.isExecuted) {
            // first instructionList: run both children and then merge
            runtime1 = right.runtimeCost;
            // second instructionList: run left child and use right child as
            // filter
            // RUNTIME
            runtime2 = getFilterCosts(right.getAllMeasures(),
                    (int) Math.ceil(source.size() * target.size() * right.selectivity));

            double min = Math.min(runtime1, runtime2);
            if (min == runtime1) {
                result.operator = Instruction.Command.INTERSECTION;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(left);
                plans.add(right);
                result.subPlans = plans;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                        spec.threshold + "", -1, -1, 0);
            } else {
                String rightChild = spec.children.get(1).fullExpression;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, rightChild,
                        spec.children.get(1).threshold + "", -1, -1, 0);
                result.filteringInstruction.setMainThreshold(spec.threshold + "");
                result.operator = null;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(left);
                result.subPlans = plans;
            }
            result.runtimeCost = min;
            result.selectivity = selectivity;
            result.mappingSize = source.size() * target.size() * selectivity;
            return result;

        } // left is not executed: RUN A, FILTER OR FILTER WITH A
        else if (!left.isExecuted && right.isExecuted) {
            // first instructionList: run both children and then merge
            // runtime1 = left.runtimeCost + right.runtimeCost;
            runtime1 = left.runtimeCost;
            // third instructionList: run right child and use left child as
            // runtime3 = right.runtimeCost;
            runtime3 = getFilterCosts(left.getAllMeasures(),
                    (int) Math.ceil(source.size() * target.size() * left.selectivity));

            double min = Math.min(runtime1, runtime3);
            if (min == runtime1) {
                result.operator = Instruction.Command.INTERSECTION;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(left);
                plans.add(right);
                result.subPlans = plans;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                        spec.threshold + "", -1, -1, 0);

            } else // min == runtime3
            {
                String leftChild = spec.children.get(0).fullExpression;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, leftChild,
                        spec.children.get(0).threshold + "", -1, -1, 0);
                result.filteringInstruction.setMainThreshold(spec.threshold + "");
                result.operator = null;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(right);
                result.subPlans = plans;
            }
            result.runtimeCost = min;
            result.selectivity = selectivity;
            result.mappingSize = source.size() * target.size() * selectivity;
            return result;

        } // if either of the children is executed, then 3 options available
        else if (!left.isExecuted && !right.isExecuted) {
            // first instructionList: run both children and then merge
            runtime1 = left.runtimeCost + right.runtimeCost;
            // second instructionList: run left child and use right child as
            // filter
            runtime2 = left.runtimeCost;
            runtime2 = runtime2 + getFilterCosts(right.getAllMeasures(),
                    (int) Math.ceil(source.size() * target.size() * right.selectivity));

            // third instructionList: run right child and use left child as
            // filter
            runtime3 = right.runtimeCost;
            runtime3 = runtime3 + getFilterCosts(left.getAllMeasures(),
                    (int) Math.ceil(source.size() * target.size() * left.selectivity));

            double min = Math.min(Math.min(runtime3, runtime2), runtime1);
            if (min == runtime1) {
                result.operator = Instruction.Command.INTERSECTION;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(left);
                plans.add(right);
                result.subPlans = plans;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, spec.getFilterExpression(),
                        spec.threshold + "", -1, -1, 0);

            } else if (min == runtime2) {
                String rightChild = spec.children.get(1).fullExpression;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, rightChild,
                        spec.children.get(1).threshold + "", -1, -1, 0);
                result.filteringInstruction.setMainThreshold(spec.threshold + "");
                result.operator = null;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(left);
                result.subPlans = plans;

            } else // min == runtime3
            {
                String leftChild = spec.children.get(0).fullExpression;
                result.filteringInstruction = new Instruction(Instruction.Command.FILTER, leftChild,
                        spec.children.get(0).threshold + "", -1, -1, 0);
                result.filteringInstruction.setMainThreshold(spec.threshold + "");
                result.operator = null;
                List<NestedPlan> plans = new ArrayList<NestedPlan>();
                plans.add(right);
                result.subPlans = plans;
            }
            result.runtimeCost = min;
            result.selectivity = selectivity;
            result.mappingSize = source.size() * target.size() * selectivity;
            return result;
        }

        return result;
    }

}
