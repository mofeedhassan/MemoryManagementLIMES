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

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class BeginBeforeEndTogether extends AllenAlgebraMapper {
    public BeginBeforeEndTogether() {
	// BB1 & EE0
	this.requiredAtomicRelationsSource.add(0);

	this.requiredAtomicRelationsTarget.add(0);
	this.requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(1);
	this.getRequiredAtomicRelations().add(6);
    }

    @Override
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks,
	    ArrayList<TreeMap<String, Set<Instance>>> targetBlocks) {
	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<Instance>> sourceBB = sourceBlocks.get(0);
	
	TreeMap<String, Set<Instance>> targetBB = targetBlocks.get(0);
	TreeMap<String, Set<Instance>> targetEE = targetBlocks.get(1);
	try {
	    if (f.exists()) {
		writer = new FileWriter(f, true);
	    } else {
		writer = new FileWriter(f);

	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	for (Entry<String, Set<Instance>> entry : sourceBB.entrySet()) {
	    long start1 = System.currentTimeMillis();
	    String sourceIntervalBB = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    // targets of BB are the same for each instance in sourceInstances
	    Set<Instance> targetBBInstances = AllenAlgebraMapper.getIntermediateMapping(targetBB,
		    sourceIntervalBB, 1.0);
	    Set<String> tempBB = new TreeSet<String>();
	    for (Instance i : targetBBInstances)
		tempBB.add(i.getUri());
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;

	    if (!targetBBInstances.isEmpty()) {
		// remember that each instance of the current time slot of BB is
		// probably belongs to another time slot in EE
		for (Instance sourceInstance : sourceInstances) {
		    
		    long start2 = System.currentTimeMillis();
		    // find the corresponding interval of sourceInstance in EE
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
		    // given the interval, get the targets from EE
		    Set<Instance> targetEEInstances = AllenAlgebraMapper.getIntermediateMapping(targetEE, intervalEE,
			    0.0);
		    Set<String> tempEE = new TreeSet<String>();
		    for (Instance i : targetEEInstances)
			tempEE.add(i.getUri());
		    
		    Set<String> intersection = AllenAlgebraMapper.intersection(tempBB, tempEE);
		    long end2 = System.currentTimeMillis();
		    duration += end2 - start2;
		    size += intersection.size();
		    /*if (iteration != 0)
			continue;
		    if (!intersection.isEmpty()) {
			for (String targetInstanceUri : intersection) {
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
	return "BeginTogetherEndBefore: BB1 & EE0";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {

	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapBB1 = maps.get(0);
	TreeMap<String, Set<String>> mapEE0 = maps.get(1);
	try {
	    if (f.exists()) {
		writer = new FileWriter(f, true);
	    } else {
		writer = new FileWriter(f);

	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	for (Map.Entry<String, Set<String>> entryBB1 : mapBB1.entrySet()) {

	    long start1 = System.currentTimeMillis();
	    String instanceBB1 = entryBB1.getKey();
	    Set<String> setBB1 = entryBB1.getValue();
	   
	    
	    Set<String> setEE0 = mapEE0.get(instanceBB1);
	    if(setEE0 == null)
		setEE0 = new TreeSet<String>();
	
	    
	    Set<String> intersection = AllenAlgebraMapper.intersection(setBB1, setEE0);
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += intersection.size();
	    /*if (iteration != 0)
		continue;
	    if (!intersection.isEmpty()) {
		for (String targetInstanceUri : intersection) {
		    try {
			this.addLink(instanceBB1, targetInstanceUri);
			writer.write(instanceBB1 + "," + targetInstanceUri + "\n");
		    } catch (IOException e) {
			// TODO Auto-generated catch block
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
