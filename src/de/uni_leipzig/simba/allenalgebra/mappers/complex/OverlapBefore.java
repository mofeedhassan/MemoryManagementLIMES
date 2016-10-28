package de.uni_leipzig.simba.allenalgebra.mappers.complex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Set;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class OverlapBefore extends AllenAlgebraMapper {
    // BB0 = 0, BB1 = 1, BE0 = 2, BE1 = 3, EB0 = 4, EB1 = 5, EE0 = 6, EE1 = 7

    public OverlapBefore() {
	// (BB1 & EE1) \ (EB0 U EB1)
	requiredAtomicRelationsSource.add(0);

	requiredAtomicRelationsTarget.add(0);
	requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(1);
	this.getRequiredAtomicRelations().add(7);
	this.getRequiredAtomicRelations().add(4);
	this.getRequiredAtomicRelations().add(5);

    }

    @Override
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks,
	    ArrayList<TreeMap<String, Set<Instance>>> targetBlocks) {
	File f = new File(filePath);
	FileWriter writer = null;
	try {
	    if (f.exists())
		writer = new FileWriter(f, true);
	    else
		writer = new FileWriter(f);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	TreeMap<String, Set<Instance>> sourceBB = sourceBlocks.get(0);

	TreeMap<String, Set<Instance>> targetBB = targetBlocks.get(0);
	TreeMap<String, Set<Instance>> targetEE = targetBlocks.get(1);

	for (Entry<String, Set<Instance>> entry : sourceBB.entrySet()) {

	    long start1 = System.currentTimeMillis();
	    String sourceIntervalBB = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    // targets of BB1 are the same for each instance in sourceInstances
	    Set<Instance> targetBB1Instances = AllenAlgebraMapper.getIntermediateMapping(targetBB, sourceIntervalBB,
		    1.0);
	    Set<String> tempBB1 = new TreeSet<String>();
	    for (Instance i : targetBB1Instances)
		tempBB1.add(i.getUri());
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;

	    for (Instance sourceInstance : sourceInstances) {

		if (!targetBB1Instances.isEmpty()) {
		    long start3 = System.currentTimeMillis();
		    // find the corresponding interval of sourceInstance in EE1
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    String s = sourceInstance.getProperty("endDate").first();
		    Date date = null;
		    try {
			date = df.parse(s);
		    } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		    long epoch = date.getTime();
		    String intervalEE = String.valueOf(epoch);
		    // given the interval, get the targets from EE1
		    Set<Instance> targetEEInstances = AllenAlgebraMapper.getIntermediateMapping(targetEE, intervalEE,
			    1.0);
		    Set<String> tempEE1 = new TreeSet<String>();
		    for (Instance i : targetEEInstances)
			tempEE1.add(i.getUri());

		    // intersection(BB1 & EE1)
		    Set<String> intersection = AllenAlgebraMapper.intersection(tempBB1, tempEE1);
		    long end3 = System.currentTimeMillis();
		    duration += end3 - start3;

		    // no need to re-remove the sourceInstance
		    if (!intersection.isEmpty()) {
			long start4 = System.currentTimeMillis();
			// for EB: sources need to be grouped based on their
			// endDate (use sourceIntervalEE)
			// and targets need to be grouped based on their
			// beginDate (use targetBB)

			// get targets from EB0
			Set<Instance> targetBE0Instances = AllenAlgebraMapper.getIntermediateMapping(targetBB,
				intervalEE, 0.0);
			Set<String> tempBE0 = new TreeSet<String>();
			for (Instance i : targetBE0Instances)
			    tempBE0.add(i.getUri());
			// get targets from EB1
			Set<Instance> targetBE1Instances = AllenAlgebraMapper.getIntermediateMapping(targetBB,
				intervalEE, 1.0);
			Set<String> tempBE1 = new TreeSet<String>();
			for (Instance i : targetBE1Instances)
			    tempBE1.add(i.getUri());
			// union (EB0 U EB1)
			Set<String> union = AllenAlgebraMapper.union(tempBE0, tempBE1);

			// difference
			Set<String> difference = AllenAlgebraMapper.difference(intersection, union);
			long end4 = System.currentTimeMillis();
			duration += end4 - start4;
			size += difference.size();

			/*if (iteration != 0)
			    continue;
			if (!difference.isEmpty()) {
			    for (String targetInstanceUri : difference) {
				try {
				    this.addLink(sourceInstance.getUri(), targetInstanceUri);
				    writer.write(sourceInstance.getUri() + "," + targetInstanceUri + "\n");
				} catch (IOException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
			    }

			}*/

		    }
		}
	    }
	}
	try {
	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public String getName() {
	return "OverlapBefore: (BB1 & EE1) \\ (EB0 U EB1)";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {

	TreeMap<String, Set<String>> mapBB1 = maps.get(0);
	TreeMap<String, Set<String>> mapEE1 = maps.get(1);

	TreeMap<String, Set<String>> mapEB0 = maps.get(2);
	TreeMap<String, Set<String>> mapEB1 = maps.get(3);

	File f = new File(filePath);
	FileWriter writer = null;
	try {
	    if (f.exists())
		writer = new FileWriter(f, true);
	    else
		writer = new FileWriter(f);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	for (Map.Entry<String, Set<String>> entryBB1 : mapBB1.entrySet()) {
	    long start = System.currentTimeMillis();
	    // get targets from EB1
	    String instanceBB1 = entryBB1.getKey();
	    Set<String> setBB1 = entryBB1.getValue();

	    Set<String> setEE1 = mapEE1.get(instanceBB1);
	    if (setEE1 == null)
		setEE1 = new TreeSet<String>();

	    Set<String> intersection = AllenAlgebraMapper.intersection(setBB1, setEE1);

	    Set<String> setEB0 = mapEB0.get(instanceBB1);
	    Set<String> setEB1 = mapEB1.get(instanceBB1);
	    if (setEB0 == null)
		setEB0 = new TreeSet<String>();
	    if (setEB1 == null)
		setEB1 = new TreeSet<String>();
	    Set<String> union = AllenAlgebraMapper.union(setEB0, setEB1);

	    Set<String> difference = AllenAlgebraMapper.difference(intersection, union);

	    long end = System.currentTimeMillis();
	    duration += end - start;
	    size += difference.size();

	    /*if (iteration != 0)
		continue;
	    if (!difference.isEmpty()) {
		for (String targetInstanceUri : difference) {
		    try {
			this.addLink(instanceBB1, targetInstanceUri);
			writer.write(instanceBB1 + "," + targetInstanceUri + "\n");
		    } catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

	    }*/

	}
	try {
	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
