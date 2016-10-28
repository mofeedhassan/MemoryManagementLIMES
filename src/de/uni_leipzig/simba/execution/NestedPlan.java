/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.execution;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import de.uni_leipzig.simba.execution.Instruction.Command;
import java.util.List;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.execution.planner.CanonicalPlanner;
import de.uni_leipzig.simba.execution.planner.DynamicPlanner;
import de.uni_leipzig.simba.execution.planner.HeliosPlanner;
import de.uni_leipzig.simba.grecall.util.DiffPair;
import de.uni_leipzig.simba.measures.MeasureProcessor;
import de.uni_leipzig.simba.selfconfig.Experiment;
import de.uni_leipzig.simba.specification.LinkSpec;
import de.uni_leipzig.simba.specification.Operator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import javax.swing.JFrame;

/**
 *
 * @author ngonga
 */
public class NestedPlan extends ExecutionPlan {

    public List<NestedPlan> subPlans;
    public Command operator;
    public Instruction filteringInstruction;
    public boolean isExecuted = false; // executed - true, not executed - false

    public NestedPlan() {
        super();
        subPlans = null;
        operator = null;
    }

    /**
     * Adds a subplan to the current list of subplans, if there is no list one
     * will be created
     * 
     * @param subplan
     *            to be added
     */
    public void addSubplan(NestedPlan subplan) {
        if (subPlans == null)
            subPlans = new ArrayList<NestedPlan>();
        subPlans.add(subplan);
    }

    @Override
    public boolean equals(Object other) {
        NestedPlan o = (NestedPlan) other;
        if (o == null)
            return false;

        // only RUN instructions in instructionList
        if (this.isAtomic() && o.isAtomic()) {
            return (this.instructionList.equals(o.instructionList));

        } else if (!this.isAtomic() && !o.isAtomic()) { // no instructionList
            if (this.operator == null && o.operator != null)
                return false;
            if (this.operator != null && o.operator == null)
                return false;
            // AND/MINUS operator (optimization): RUN one child, (reverse)FILTER
            // with other the child
            if (this.operator == null && o.operator == null) {
                if (this.filteringInstruction.equals(o.filteringInstruction)) {
                    return (this.subPlans.get(0).equals(o.subPlans.get(0)));
                } // different filteringInstructions
                return false;
            }
            if (this.operator.equals(o.operator)) {
                // all complex, non-optimized plans MUST have a filtering
                // instruction
                if (this.filteringInstruction == null && o.filteringInstruction == null)
                    return false;
                if (this.filteringInstruction != null && o.filteringInstruction == null)
                    return false;
                if (this.filteringInstruction == null && o.filteringInstruction != null)
                    return false;
                if (this.filteringInstruction.equals(o.filteringInstruction)) {
                    return (this.subPlans.get(0).equals(o.subPlans.get(0))
                            && this.subPlans.get(1).equals(o.subPlans.get(1)));
                } // different filtering instructions
                return false;
            } // different operators
            return false;
        }
        // one plan is atomic, the other is not
        return false;

    }

    public List<String> getAllMeasures() {
        List<String> result = new ArrayList<String>();

        if (isAtomic()) {
            if (filteringInstruction != null) {
                result.addAll(MeasureProcessor.getMeasures(filteringInstruction.getMeasureExpression()));
            }
        }
        if (!(subPlans == null)) {
            if (!subPlans.isEmpty()) {
                for (NestedPlan p : subPlans) {
                    result.addAll(p.getAllMeasures());
                }
            }
        }
        if (instructionList != null) {
            for (Instruction i : instructionList) {
                if (i.getMeasureExpression() != null) {
                    result.addAll(MeasureProcessor.getMeasures(i.getMeasureExpression()));
                }
            }
        }
        return result;
    }

    public boolean isAtomic() {
        if (subPlans == null) {
            return true;
        } else {
            if (subPlans.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return (instructionList == null && subPlans == null && filteringInstruction == null);
    }

    public String finalPlan() {
        if (isEmpty()) {
            return "Empty plan";
        }
        if (isAtomic()) {
            return this.instructionList.get(0).getMeasureExpression() + "-"
                    + this.instructionList.get(0).getThreshold();
        } else {
            if (operator == null) {
                String child = subPlans.get(0).finalPlan();
                String filter = "";
                if (filteringInstruction.getCommand().equals(Instruction.Command.FILTER))
                    filter = "FILTER:" + filteringInstruction.getMeasureExpression() + "-"
                            + filteringInstruction.getThreshold();
                else
                    filter = "REVERSEFILTER:" + filteringInstruction.getMeasureExpression() + "-"
                            + filteringInstruction.getThreshold();
                String mainFilter = "FILTER:" + filteringInstruction.getMainThreshold();

                if (subPlans.get(0).isAtomic()) {
                    return "RUN:" + child + "\n" + filter + "\n" + mainFilter + "\n";
                } else
                    return child + "\n" + filter + "\n" + mainFilter + "\n";

            } else {
                String childLeft = subPlans.get(0).finalPlan();
                String childRight = subPlans.get(1).finalPlan();
                String op = "";
                if (this.operator.equals(Command.DIFF)) {
                    op = "DIFFERENCE";
                } else if (this.operator.equals(Command.INTERSECTION)) {
                    op = "INTERSECTION";
                } else if (this.operator.equals(Command.UNION)) {
                    op = "UNION";
                } else if (this.operator.equals(Command.XOR)) {
                    op = "XOR";
                }
                String filter = "FILTER:" + filteringInstruction.getThreshold();
                if (subPlans.get(0).isAtomic() && subPlans.get(1).isAtomic()) {
                    return "RUN:" + childLeft + "\n" + "RUN:" + childRight + "\n" + op + "\n" + filter + "\n";
                } else if (subPlans.get(0).isAtomic() && !subPlans.get(1).isAtomic()) {
                    return "RUN:" + childLeft + "\n" + childRight + "\n" + op + "\n" + filter + "\n";
                } else if (!subPlans.get(0).isAtomic() && subPlans.get(1).isAtomic()) {
                    return childLeft + "\n" + "RUN:" + childRight + "\n" + op + "\n" + filter + "\n";
                } else if (!subPlans.get(0).isAtomic() && !subPlans.get(1).isAtomic()) {
                    return childLeft + "\n" + childRight + "\n" + op + "\n" + filter + "\n";
                }
            }
        }
        return null;
    }

    public String toString() {
        String pre = ("Selectivity = " + selectivity);
        if (isEmpty()) {
            return "Empty plan";
        }
        if (isAtomic()) {

            if (instructionList != null) {
                return "\n\nBEGIN\n" + pre + "\n-----\nNULL\n" + instructionList + "\nEND\n-----";
            } else {
                return "\nBEGIN\n" + pre + "-----\nNULL\n" + filteringInstruction + "\nEND\n-----";
            }
        } else {
            return "\nBEGIN\n" + pre + "-----\n" + filteringInstruction + "\nSubplans\n" + operator + "\n" + subPlans
                    + "\nEND\n-----";
        }
    }

    public String getFilterString(String filter) {
        String[] parts = filter.split("\t");
        String result = parts[0];
        if (!parts[1].equals("null")) {
            result = result + "\n" + parts[1];
        }
        result = result + "\n" + parts[2];
        return result;
    }

    /**
     * Returns the measure that is equivalent to this plan. Mostly for planning
     * purposes - a bit problematic - NOT USED IN DYNAMIC PLANNER, REPLACED IN
     * HELIOS PLANNER
     *
     * @return Measure as string
     */
    public String getEquivalentMeasure() {
        String result;
        // logger.info(this);
        if (isAtomic()) {
            result = instructionList.get(0).getMeasureExpression();
        } else {
            logger.info(filteringInstruction);
            logger.info(filteringInstruction.getThreshold());

            // filtering node with one - PROSOXI! NA MPEI KAI GIA MINUS ELEGXOS
            if (subPlans.size() == 1) {
                if (filteringInstruction != null) {
                    if (filteringInstruction.getMeasureExpression() == null) {
                        return subPlans.get(0).getEquivalentMeasure();
                    } else {// not atomic
                        String s = subPlans.get(0).getEquivalentMeasure();
                        if (!subPlans.get(0).isAtomic()) {// complex
                            return "AND(" + filteringInstruction.getMeasureExpression() + "|"
                                    + filteringInstruction.getThreshold() + "," + s + "|"
                                    + subPlans.get(0).getThreshold() + ")";

                        } else {
                            return "AND(" + filteringInstruction.getMeasureExpression() + "|"
                                    + filteringInstruction.getThreshold() + "," + s + "|"
                                    + subPlans.get(0).instructionList.get(0).getThreshold() + ")";
                        }

                    }
                }
            }

            if (operator == Command.INTERSECTION) {
                result = "AND(";
            } else if (operator == Command.UNION) {
                result = "OR(";
            } else if (operator == Command.XOR) {
                result = "XOR(";
            } else if (operator == Command.DIFF) {
                result = "MINUS(";
            } else {
                result = "";
            }

            for (NestedPlan p : subPlans) {
                result = result + p.getEquivalentMeasure() + "|";
                if (p.isAtomic()) {
                    result = result + p.instructionList.get(0).getThreshold() + ",";
                } else {
                    if (p.filteringInstruction != null) {
                        // if (p.filteringInstruction.getMeasureExpression() ==
                        // null) {
                        result = result + p.filteringInstruction.getThreshold() + ",";

                        // }
                        // else {
                        // result = result + p.getEquivalentMeasure() + "|" +
                        // p.filteringInstruction.getThreshold() + ",";
                        // }
                    }
                }
            }
            result = result.substring(0, result.length() - 1);
            result = result + ")";
            if (!(instructionList == null)) {
                if (!instructionList.isEmpty()) {
                    for (Instruction i : instructionList) {
                        result = "AND(" + i.getMeasureExpression() + "|" + i.getThreshold() + "," + result + ")";

                    }
                }
            }
        }
        if (filteringInstruction != null) {
            if (filteringInstruction.getMeasureExpression() != null) {
                result = "AND(" + filteringInstruction.getMeasureExpression() + "|"
                        + filteringInstruction.getThreshold() + "," + result + "|" + filteringInstruction.getThreshold()
                        + ")";

            }
        }

        // if (instructionList != null) {
        // if (instructionList.get(0).getMeasureExpression() != null) {
        // result = "AND(" + instructionList.get(0).getMeasureExpression() + "|"
        // + instructionList.get(0).getThreshold() + ","
        // + result + "|" + instructionList.get(0).getThreshold()+ ")";
        // }
        // }
        logger.info(result);
        return result;
    }

    /**
     * Generates a clone of the current spec
     * 
     * @return Clone of current spec
     */
    public NestedPlan clone() {
        NestedPlan clone = new NestedPlan();
        clone.filteringInstruction = this.filteringInstruction;
        clone.instructionList = this.instructionList;
        clone.mappingSize = this.mappingSize;
        clone.operator = this.operator;
        clone.runtimeCost = this.runtimeCost;
        clone.selectivity = this.selectivity;
        List<NestedPlan> l = new ArrayList<NestedPlan>();
        NestedPlan subPlanCopy;
        if (this.subPlans != null)
            for (NestedPlan c : this.subPlans) {
                subPlanCopy = c.clone();
                clone.addSubplan(subPlanCopy);
                l.add(subPlanCopy);
            }

        return clone;
    }

    /**
     * Returns the threshold to be used when reconstructing the metric that led
     * to this plans
     *
     * @return Threshold as string
     */
    public String getThreshold() {
        if (filteringInstruction != null) {
            return filteringInstruction.getThreshold();
        } else {
            return "0";
        }
    }

    public int getSize(String s) {
        int size = 0;
        if (s.contains("\n")) {
            String[] parts = s.split("\n");
            for (int i = 0; i < parts.length; i++) {
                size = Math.max(size, parts[i].length());
            }
            return size;
        }
        return s.length();
    }

    public String getInstructionString(List<Instruction> list) {
        Instruction i = list.get(0);
        String result = i.getCommand() + "\n";
        result = result + i.getMeasureExpression() + "\n";
        result = result + i.getThreshold();
        return result;
    }

    @Override
    public List<Instruction> getInstructionList() {
        List<Instruction> instructions = super.getInstructionList();
        if (!isAtomic()) {
            for (NestedPlan np : subPlans) {
                instructions.addAll(np.getInstructionList());
            }
        }
        return instructions;
    }

    public void draw(mxGraph graph, Object root) {
        int charsize = 8;
        Object parent = graph.getDefaultParent();
        if (isAtomic()) {
            Object v;
            if (instructionList != null && !instructionList.isEmpty()) {
                String inst = getInstructionString(instructionList);
                v = graph.insertVertex(parent, null, inst, 20, 40, getSize(inst) * charsize, 45, "ROUNDED");
            } else {
                String filter = getFilterString(filteringInstruction.toString());
                v = graph.insertVertex(parent, null, filter, 20, 40, getSize(filter) * charsize, 45, "ROUNDED");
            }
            if (root != null) {
                graph.insertEdge(parent, null, "", root, v);
            }
        } else {
            Object v1, v2;
            String filter;
            if (filteringInstruction != null) {
                filter = getFilterString(filteringInstruction.toString());
            } else {
                filter = "NULL";
            }
            // String inst = getInstructionString(instructionList);
            v1 = graph.insertVertex(parent, null, filter, 20, 40, getSize(filter) * charsize, 45, "ROUNDED");
            v2 = graph.insertVertex(parent, null, operator, 20, 40, (operator + "").length() * charsize, 45,
                    "RECTANGLE");
            graph.insertEdge(parent, null, "", root, v1);
            graph.insertEdge(parent, null, "", v1, v2);
            for (NestedPlan p : subPlans) {
                p.draw(graph, v2);
            }
        }
    }

    public mxGraph getGraph() {
        mxGraph graph = new mxGraph();

        mxStylesheet stylesheet = graph.getStylesheet();
        Hashtable<String, Object> rounded = new Hashtable<String, Object>();
        rounded.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        rounded.put(mxConstants.STYLE_OPACITY, 50);
        rounded.put(mxConstants.STYLE_FILLCOLOR, "#FF5240");
        rounded.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        stylesheet.putCellStyle("ROUNDED", rounded);

        Hashtable<String, Object> rectangle = new Hashtable<String, Object>();
        rectangle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        rectangle.put(mxConstants.STYLE_OPACITY, 50);
        rectangle.put(mxConstants.STYLE_FILLCOLOR, "#5FEB3B");
        rectangle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        stylesheet.putCellStyle("RECTANGLE", rectangle);

        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            // Object root = graph.insertVertex(parent, null, "ROOT", 20, 40,
            // 80, 30, "RECTANGLE");
            draw(graph, null);
        } finally {
            graph.getModel().endUpdate();
        }
        mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
        layout.setHorizontal(false);
        layout.execute(graph.getDefaultParent());
        return graph;
    }

    public void draw() {
        mxGraph graph = getGraph();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.getViewport().setOpaque(false);
        graphComponent.setBackground(Color.WHITE);
        // set all properties
        // layout.setMinDistanceLimit(10);
        // layout.setInitialTemp(10);
        // layout.setForceConstant(10);
        // layout.setDisableEdgeStyle(true);

        JFrame frame = new JFrame();
        // JFrame f = new JFrame();
        frame.setSize(500, 500);
        frame.setLocation(300, 200);
        frame.setBackground(Color.white);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphComponent);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void getPlan() {
        if (this.isAtomic())
            logger.info(this);
        else {
            for (NestedPlan p : this.subPlans) {
                logger.info(p);
                p.getPlan();
            }

        }
    }

    public static void main(String args[]) {
        CanonicalPlanner cp = new CanonicalPlanner();
        Cache source = Experiment.readFile("./Examples/GeneticEval/Datasets/DBLP-ACM/DBLP2.csv");
        Cache target = Experiment.readFile("./Examples/GeneticEval/Datasets/DBLP-ACM/ACM.csv");
        HeliosPlanner hp = new HeliosPlanner(source, source);
        LinkSpec spec = new LinkSpec();

        LinkSpec and = new LinkSpec();
        and.operator = Operator.OR;
        and.threshold = 0.4d;

        LinkSpec atom1 = new LinkSpec();
        atom1.threshold = 0.5;
        atom1.setAtomicFilterExpression("levenshtein", "x.title", "y.title");
        // atom1.filterExpression = "levenshtein(x.title,y.title)";
        atom1.parent = and;
        and.addChild(atom1);

        LinkSpec atom2 = new LinkSpec();
        atom2.threshold = 0.3d;
        atom1.setAtomicFilterExpression("trigrams", "x.authors", "y.authors");
        // atom2.filterExpression = "trigrams(x.authors,y.authors)";
        atom2.parent = and;
        and.addChild(atom2);

        spec.readSpec(
                "OR(OR(trigrams(x.title,y.title)|0.98,qgrams(x.title,y.title)|0.94)|0.6803,OR(leven(x.authors,y.authors)|0.98,jaccard(x.title,y.title)|0.91)|0.6803)",
                0.5861);
        // spec.readSpec("OR(cosine(x.title,y.title)|0.5263,AND(cosine(x.authors,y.authors)|0.5263,overlap(x.title,y.title)|0.5263)|0.2012)",
        // 0.3627);
        // spec.readSpec("AND(levenshtein(x.title, y.title)|0.7,
        // euclidean(x.year, y.year)|0.4)", 0.5);
        NestedPlan np = hp.plan(spec);
        logger.info(np);
        np.draw();

        // NestedPlan np = cp.plan(spec);
        // logger.info(np);
        // np.draw();

        ExecutionEngine ee = new ExecutionEngine(source, target, "?x", "?y");
        Mapping m = ee.runNestedPlan(np);

        // np.getPlan();
        System.out.println(np.getEquivalentMeasure());
    }
}
