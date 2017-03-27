package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.GoModified;

import java.util.Stack;
//a single gain bucket with its gain value and a list that is used as a stack.
public class BList {
	public int gain;
	public Stack nodes = new Stack<>();
}
