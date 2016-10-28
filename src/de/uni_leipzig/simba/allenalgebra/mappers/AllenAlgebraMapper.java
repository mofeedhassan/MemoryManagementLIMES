package de.uni_leipzig.simba.allenalgebra.mappers;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Tree;

public abstract class AllenAlgebraMapper implements IAllenAlgebraMapper {

    protected ArrayList<Integer> requiredAtomicRelationsSource = new ArrayList<Integer>();
    protected ArrayList<Integer> requiredAtomicRelationsTarget = new ArrayList<Integer>();

    private ArrayList<Integer> requiredAtomicRelations = new ArrayList<Integer>();
    protected Cache source;
    protected Cache target;
    protected int size = 0;
    
    public int getSize() {
        return size;
    }
    public TreeMap<String, Set<String>> links = new TreeMap<String, Set<String>>();
    
    public void addLink(String source, String target){
	if(links.get(source) != null){
	    links.get(source).add(target);
	}else{
	    Set<String> temp = new TreeSet<String>();
	    temp.add(target);
	    links.put(source, temp);
	}
    }
    public void setSourceCache(Cache c){
	source = c;
    }
    public void setTargetCache(Cache c){
	target = c;
    }
    protected int iteration = 0;

    public void setIteration(int iteration) {
	this.iteration = iteration;
    }

    public void setFilePath(String filePath) {
	this.filePath = filePath;
    }

    protected String filePath = null;
    protected long duration = 0l;

    public ArrayList<Integer> getRequiredAtomicRelationsSource() {
	return requiredAtomicRelationsSource;
    }

    public ArrayList<Integer> getRequiredAtomicRelationsTarget() {
	return requiredAtomicRelationsTarget;
    }

    public long getDuration() {
	return duration;
    }

    protected static Set<String> union(Set<String> set1, Set<String> set2) {
	Set<String> temp = new HashSet<String>(set1);
	temp.addAll(new HashSet<String>(set2));
	return temp;

    }

    protected static Set<String> intersection(Set<String> set1, Set<String> set2) {
	Set<String> temp = new HashSet<String>(set1);
	temp.retainAll(new HashSet<String>(set2));
	return temp;

    }

    protected static Set<String> difference(Set<String> set1, Set<String> set2) {
	Set<String> temp = new HashSet<String>(set1);
	temp.removeAll(new HashSet<String>(set2));
	return temp;

    }

    /*protected static Long getInterval(TreeMap<Long, Set<String>> instances, String instance) {
	Long interval = 0l;
	for (Map.Entry<Long, Set<String>> entry : instances.entrySet()) {
	    for (String i : entry.getValue()) {
		if (i.equals(instance)) {
		    interval = entry.getKey();
		    break;
		}

	    }
	}
	return interval;
    }*/

    public static Set<Instance> getIntermediateMapping(TreeMap<String, Set<Instance>> target, String timeStamp,
	    double relationType) {
	Set<Instance> m = new TreeSet<Instance>();
	if (relationType == 0.0d) {
	    m = mapConcurrent(timeStamp, target);
	} else if (relationType == 1.0d) {
	    m = mapSuccessor(timeStamp, target);
	}
	return m;
    }

    private static Set<Instance> mapConcurrent(String sourceTimeStamp, TreeMap<String, Set<Instance>> target) {
	Set<Instance> tempTargets = target.get(sourceTimeStamp);
	Set<Instance> m = new TreeSet<Instance>();

	if (tempTargets != null) {
	    m.addAll(tempTargets);
	}
	return m;

    }

    private static Set<Instance> mapSuccessor(String sourceTimeStamp, TreeMap<String, Set<Instance>> target) {
	Set<Instance> m = new TreeSet<Instance>();
	SortedMap<String, Set<Instance>> tempTargets = target.tailMap(sourceTimeStamp);
	if (tempTargets != null) {
	    for (Map.Entry<String, Set<Instance>> entry : tempTargets.entrySet()) {
		if (!sourceTimeStamp.equals(entry.getKey())) {
		    Set<Instance> targets = entry.getValue();
		    m.addAll(targets);
		}
	    }
	}
	return m;

    }

    public ArrayList<Integer> getRequiredAtomicRelations() {
	return requiredAtomicRelations;
    }

    public void setRequiredAtomicRelations(ArrayList<Integer> requiredAtomicRelations) {
	this.requiredAtomicRelations = requiredAtomicRelations;
    }

}
