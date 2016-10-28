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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class BeginTogetherEndBefore extends AllenAlgebraMapper {
    public BeginTogetherEndBefore() {
	// BB0 & EE1
	this.requiredAtomicRelationsSource.add(0);

	this.requiredAtomicRelationsTarget.add(0);
	this.requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(0);
	this.getRequiredAtomicRelations().add(7);
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
		    sourceIntervalBB, 0.0);
	    Set<String> tempBB = new TreeSet<String>();
	    for (Instance i : targetBBInstances)
		tempBB.add(i.getUri());
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;

	    if (!tempBB.isEmpty()) {
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
			    1.0);
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
	return "BeginTogetherEndBefore: BB0 & EE1";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {

	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapBB0 = maps.get(0);
	TreeMap<String, Set<String>> mapEE1 = maps.get(1);
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
	for (Map.Entry<String, Set<String>> entryBB0 : mapBB0.entrySet()) {

	    long start1 = System.currentTimeMillis();
	    String instancBB0 = entryBB0.getKey();
	    Set<String> setBB0 = entryBB0.getValue();
	    Set<String> setEE1 = mapEE1.get(instancBB0);
	    if(setEE1 == null)
		setEE1 = new TreeSet<String>();
	    
	    Set<String> intersection = AllenAlgebraMapper.intersection(setBB0, setEE1);
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += intersection.size();
	    /*if (iteration != 0)
		continue;
	    if (!intersection.isEmpty()) {
		for (String targetInstanceUri : intersection) {
		    try {
			this.addLink(instancBB0, targetInstanceUri);
			writer.write(instancBB0 + "," + targetInstanceUri + "\n");
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
