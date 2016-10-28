package de.uni_leipzig.simba.grecall.optimizer.recalloptimizer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.execution.Instruction;
import de.uni_leipzig.simba.execution.NestedPlan;
import de.uni_leipzig.simba.grecall.oracle.SimpleOracle;
import de.uni_leipzig.simba.grecall.util.DiffPair;
import de.uni_leipzig.simba.measures.MeasureProcessor;
import de.uni_leipzig.simba.specification.LinkSpec;
import de.uni_leipzig.simba.specification.Operator;

public class DownwardRefinementMonAs extends RecallOptimizer {
    private ArrayList<LinkSpec> waiting = new ArrayList<LinkSpec>(); // LS that
    // are
    // waiting
    // to be
    // expanded
    private HashSet<String> overall = new HashSet<String>(); // all the LS that
    // have been
    // expanded,are
    // waiting or have
    // been rejected
    //private HashSet<String> rejected = new HashSet<String>(); // all the LS that
    private LinkedHashMap<LinkSpec, NestedPlan> newPlans = new LinkedHashMap<LinkSpec, NestedPlan>();

    public DownwardRefinementMonAs(long sourceSize, long targetSize, SimpleOracle oracle, double recall) {
	super(sourceSize, targetSize, oracle, recall);

    }

    public void optimize() {
	int counter = 0;
	this.waiting.add(this.bestEntry.getX());
	this.overall.add(this.bestEntry.getX().toString());
	Iterator<LinkSpec> waitingIT = this.waiting.iterator();

	long start = System.currentTimeMillis();
	long end = start + this.timeCounter;

	while (this.waiting.size() != 0 && System.currentTimeMillis() < end) {
	    //counter++;

	    //logger.info("--------------Attempt: " + counter +"-------------------");

	    //logger.info("Best selectivity now: " +bestEntry.getY().selectivity + " || " + this.DesiredSelectivity);
	    //logger.info("Best runtime: " + bestEntry.getY().runtimeCost);
	    //logger.info("Best Spec: " + this.bestEntry.getX());
	    //logger.info("Just started: Stilling waiting for: " + waiting.size());

	    // set iterator at top of list
	    waitingIT = this.waiting.iterator();
	    // get the top of the list == next element
	    LinkSpec currentSpec = new LinkSpec();
	    currentSpec = waitingIT.next();

	    // compare best plan so far with the new node you are about to
	    // expand
	    NestedPlan currentPlan = this.getPlanFromSpecification(currentSpec);
	    if (this.scoreStrategy.compare(currentSpec, this.bestEntry.getX(), currentPlan,
		    this.bestEntry.getY()) == true) {
		this.bestEntry.setX(currentSpec);
		this.bestEntry.setY(currentPlan);
		//logger.info("Best new spec: " + this.bestEntry.getX());
		//logger.info("Best new runtime: " +this.bestEntry.getY().runtimeCost);
		//logger.info("Best new selectivity: " + this.bestEntry.getY().selectivity + "::" + this.DesiredSelectivity);
		if (checkSelectivity(this.bestEntry.getY().selectivity).equals("equal"))
		    break;
	    }

	    //logger.info("Refine current LS" + currentSpec);
	    //logger.info(this.getPlanFromSpecification(currentSpec).runtimeCost);
	    newPlans = new LinkedHashMap<LinkSpec, NestedPlan>();
	    List<LinkSpec> newNodes = refine(currentSpec);
	    addSpecifications(newNodes);
	    //logger.info(currentSpec);
	    //logger.info("Added some stuff: Stilling waiting for: " +waiting.size());

	    // add node to history
	    this.overall.add(currentSpec.toString());
	    // remove it from the waiting list
	    waitingIT.remove();

	    if (newPlans.size() != 0) {
		//logger.info("You can expand by " + newPlans.size() + "nodes.");
		addToWaiting();

	    } 
	    //logger.info("Removed previous: Stilling waiting for: " +waiting.size());
	    //logger.info("Overall: " + overall.size());
	    //logger.info("Rejected: "+rejected.size());
	}

    }

    private void addToWaiting() {
	LinkedHashMap<LinkSpec, Double> newOverallPlans = new LinkedHashMap<LinkSpec, Double>();
	newOverallPlans = this.scoreStrategy.LinkToScore(newPlans);
	LinkedHashMap<LinkSpec, Double> newOverallPlans2 = this.scoreStrategy.sortByValues(newOverallPlans, true);
	ArrayList<LinkSpec> list = new ArrayList<LinkSpec>(newOverallPlans2.keySet());
	waiting.addAll(0, list);
	// for (LinkSpec sp : waiting) {
	// logger.info(sp + " " +
	// this.getPlanFromSpecification(sp).runtimeCost);
	// }

    }

    protected void addSpecifications(List<LinkSpec> specs) {
	for (LinkSpec sp : specs) {
	    if (!this.overall.contains(sp.toString())) {
		//logger.info("---->New node");
		//logger.info(sp);
		NestedPlan plan = getPlanFromSpecification(sp);
		//logger.info("Selectivity now: " + plan.selectivity + " || " + this.DesiredSelectivity + " || " + this.root.getY().selectivity);
		if (!checkSelectivity(plan.selectivity).equals("lower")) {
		    newPlans.put(sp, plan);
		    //logger.info("Alles gut.Runtime result: " +plan.runtimeCost);
		} /*else {
		    logger.info("too low selectivity");
		    rejected.add(sp.toString());

	        } */
		overall.add(sp.toString());

	    }
	   /*else {
		logger.info("Seen before");
		logger.info(sp);
	   }*/
	}

    }

    protected LinkSpec refineChild(LinkSpec currentSpec) {
	LinkSpec newSpec = new LinkSpec();

	if (currentSpec.threshold == 1.0) {// don't add it as a plan
	    //logger.info("Cannot optimize anymore");
	    newSpec = null;
	} else {
	    newSpec = currentSpec.clone();
	    newSpec.threshold = this.oracle.returnNextThreshold(currentSpec.threshold);
	    //logger.info("Child refined: " + newSpec);
	}
	//logger.info(currentSpec);
	return newSpec;
    }

    protected List<LinkSpec> merge(LinkSpec parent, List<LinkSpec> leftChildren, List<LinkSpec> rightChildren,
	    boolean isLeft) {
	List<LinkSpec> specList = new ArrayList<>();
	
	if (isLeft) {// left child changed
	    for (LinkSpec leftChild : leftChildren) {
		for (LinkSpec rightChild : rightChildren) {
		    LinkSpec ls = new LinkSpec();
		    ls = parent.clone();
		    ls.children.set(0, leftChild.clone());
		    ls.children.set(1, rightChild.clone());
		    specList.add(ls);
		}
	    }

	} else {// left child changed
	    for (LinkSpec rightChild : rightChildren) {
		for (LinkSpec leftChild : leftChildren) {
		    LinkSpec ls = new LinkSpec();
		    ls = parent.clone();
		    ls.children.set(0, leftChild.clone());
		    ls.children.set(1, rightChild.clone());
		    specList.add(ls);

		}
	    }
	}
	return specList;

    }

    protected List<LinkSpec> refine(LinkSpec currentSpec) {
	
	List<LinkSpec> temp = new ArrayList<LinkSpec>();
	if (currentSpec.isAtomic()) {
	    currentSpec = refineChild(currentSpec);
	    if (currentSpec != null)
		temp.add(currentSpec);
	} else {

	    List<LinkSpec> l = merge(currentSpec, refine(currentSpec.children.get(0)),
		    new ArrayList<>(Arrays.asList(currentSpec.children.get(1))), true);
	    temp.addAll((ArrayList<LinkSpec>) l);

	    List<LinkSpec> r = null;
	    if (!currentSpec.operator.equals(Operator.MINUS)) {
		r = merge(currentSpec, new ArrayList<>(Arrays.asList(currentSpec.children.get(0))),
			refine(currentSpec.children.get(1)), false);
		temp.addAll((ArrayList<LinkSpec>) r);

	    }
	    if (currentSpec.operator.equals(Operator.OR)) {
		LinkSpec leftChild = currentSpec.children.get(0).clone();
		if (currentSpec.threshold > leftChild.threshold) {
		    leftChild.threshold = currentSpec.threshold;
		} ///////////////////////////////////////////////////////////
		LinkSpec rightChild = currentSpec.children.get(1).clone();
		if (currentSpec.threshold > rightChild.threshold) {
		    rightChild.threshold = currentSpec.threshold;
		}
		temp.add(leftChild);
		temp.add(rightChild);
	    }
	}
	
	return temp;

    }

    

}