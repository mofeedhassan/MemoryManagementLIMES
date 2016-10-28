/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.execution.planner;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.execution.ExecutionPlan;
import de.uni_leipzig.simba.execution.Instruction;
import de.uni_leipzig.simba.execution.Instruction.Command;
import de.uni_leipzig.simba.execution.NestedPlan;
import de.uni_leipzig.simba.specification.LinkSpec;
import de.uni_leipzig.simba.specification.Operator;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author ngonga
 */
public class CanonicalPlanner implements ExecutionPlanner {

    static Logger logger = Logger.getLogger("LIMES");
    ArrayList<String> plans = new ArrayList<String>();

    public CanonicalPlanner() {
    }

    /**
     * Generates a nested instructionList for a link spec
     *
     * @param spec
     *            Input spec
     * @return Nested instructionList
     */
    public NestedPlan plan(LinkSpec spec) {
        NestedPlan plan = new NestedPlan();
        plan.instructionList = new ArrayList<Instruction>();
        // atomic specs are simply ran
        if (spec.isAtomic()) {
            // nested plan have a null instruction list as default
            plan.instructionList = new ArrayList<Instruction>();
            plan.addInstruction(new Instruction(Instruction.Command.RUN, spec.getFilterExpression(),
                    spec.threshold + "", -1, -1, 0));
        } else {
            List<NestedPlan> children = new ArrayList<NestedPlan>();
            // set children
            for (LinkSpec child : spec.children) {
                children.add(plan(child));
                plans.add("RUN:" + child.fullExpression + "-" + child.threshold);
            }
            // logger.info("Added "+children.size()+" subplans");
            plan.subPlans = children;
            // set operator
            if (spec.operator.equals(Operator.AND)) {
                plan.operator = Command.INTERSECTION;
                plans.add("INTERSECTION");
            } else if (spec.operator.equals(Operator.OR)) {
                plan.operator = Command.UNION;
                plans.add("UNION");
            } else if (spec.operator.equals(Operator.XOR)) {
                plan.operator = Command.XOR;
                plans.add("XOR");
            } else if (spec.operator.equals(Operator.MINUS)) {
                plan.operator = Command.DIFF;
                plans.add("DIFFERENCE");
            }
            plan.filteringInstruction = new Instruction(Command.FILTER, spec.getFilterExpression(), spec.threshold + "",
                    -1, -1, 0);
            plans.add("FILTER:" + spec.threshold);
        }
        // logger.info("Generated the following instructionList:\n"+plan);
        return plan;

    }

    @Override
    public String getFinalPlan() {
        String s = "";
        for (String p : plans) {
            s += p + "\n";
        }
        return s;
    }
}
