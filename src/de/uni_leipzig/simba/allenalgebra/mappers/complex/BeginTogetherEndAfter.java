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

public class BeginTogetherEndAfter extends AllenAlgebraMapper {
    public BeginTogetherEndAfter() {
	// BB0 \\ (EE0 U EE1)

	this.requiredAtomicRelationsSource.add(0);

	this.requiredAtomicRelationsTarget.add(0);
	this.requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(0);
	this.getRequiredAtomicRelations().add(6);
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
	    // targets of BB0 are the same for each instance in sourceInstances
	    Set<Instance> targetBBInstances = AllenAlgebraMapper.getIntermediateMapping(targetBB,
		    sourceIntervalBB, 0.0);
	    Set<String> tempBB = new TreeSet<String>();
	    for (Instance i : targetBBInstances)
		tempBB.add(i.getUri());
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;

	    for (Instance sourceInstance : sourceInstances) {

		if (!tempBB.isEmpty()) {

		    long start2 = System.currentTimeMillis();
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
		    // compute EE0
		    Set<Instance> targetsEE0 = AllenAlgebraMapper.getIntermediateMapping(targetEE, intervalEE,
			    0.0d);
		    Set<String> tempEE0 = new TreeSet<String>();
		    for (Instance i : targetsEE0)
			tempEE0.add(i.getUri());
		    // compute EE1
		    Set<Instance> targetsEE1 = AllenAlgebraMapper.getIntermediateMapping(targetEE, intervalEE,
			    1.0d);
		    Set<String> tempEE1 = new TreeSet<String>();
		    for (Instance i : targetsEE1)
			tempEE1.add(i.getUri());
		    // union (EE0 U EE1)
		    Set<String> union = AllenAlgebraMapper.union(tempEE0, tempEE1);

		    // difference BB0 \ (EE0 U EE1)
		    Set<String> difference = AllenAlgebraMapper.difference(tempBB, union);

		    long end2 = System.currentTimeMillis();
		    duration += end2 - start2;
		    size += difference.size();
		    /*if (iteration != 0)
			continue;
		    if (!difference.isEmpty()) {
			for (String targetInstanceUri : difference) {
			    try {
				this.addLink(sourceInstance.getUri(), targetInstanceUri);
				writer.append(sourceInstance.getUri() + "," + targetInstanceUri + "\n");
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
	return "BeginTogetherEndAfter: BB0 \\ (EE0 U EE1)";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {

	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapBB0 = maps.get(0);
	TreeMap<String, Set<String>> mapEE0 = maps.get(1);
	TreeMap<String, Set<String>> mapEE1 = maps.get(2);
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
	    String instanceBB0 = entryBB0.getKey();
	    Set<String> setBB0 = entryBB0.getValue();

	    Set<String> setEE0 = mapEE0.get(instanceBB0);
	    Set<String> setEE1 = mapEE1.get(instanceBB0);
	    
	    if(setEE0 == null)
		setEE0 = new TreeSet<String>();
	    if(setEE1 == null)
		setEE1 = new TreeSet<String>();

	    Set<String> union = AllenAlgebraMapper.union(setEE0, setEE1);
	    Set<String> difference = AllenAlgebraMapper.difference(setBB0, union);
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += difference.size();
	    /*if (iteration != 0)
		continue;
	    if (!difference.isEmpty()) {
		for (String targetInstanceUri : difference) {
		    try {
			this.addLink(instanceBB0, targetInstanceUri);
			writer.write(instanceBB0 + "," + targetInstanceUri + "\n");
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
