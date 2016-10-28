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
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class BeginAfterEndTogether extends AllenAlgebraMapper {
    public BeginAfterEndTogether() {
	// EE0 \\ (BB0 U BB1)

	this.requiredAtomicRelationsSource.add(1);
	
	this.requiredAtomicRelationsTarget.add(0);
	this.requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(6);
	this.getRequiredAtomicRelations().add(0);
	this.getRequiredAtomicRelations().add(1);
    }

    @Override
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks,
	    ArrayList<TreeMap<String, Set<Instance>>> targetBlocks) {
	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<Instance>> sourceEE = sourceBlocks.get(0);
	
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
	for (Map.Entry<String, Set<Instance>> entry : sourceEE.entrySet()) {

	    long start1 = System.currentTimeMillis();
	    String sourceIntervalEE = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    // targets of EE are the same for each instance in sourceInstances
	    Set<Instance> targetEEInstances = AllenAlgebraMapper.getIntermediateMapping(targetEE, sourceIntervalEE,
		    0.0);
	    Set<String> tempEE = new TreeSet<String>();
	    for (Instance i : targetEEInstances)
		tempEE.add(i.getUri());
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;

	    // remember that each instance of the current time slot of EE is
	    // probably belongs to another time slot in BB
	    for (Instance sourceInstance : sourceInstances) {

		if (!targetEEInstances.isEmpty()) {
		    
		    long start2 = System.currentTimeMillis();
		    // sources in both BB0 and BB1 have the same time stamp
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    String s = sourceInstance.getProperty("beginDate").first();
		    Date date = null;
		    try {
			date = df.parse(s);
		    } catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		    long epoch = date.getTime();
		    String intervalBB = String.valueOf(epoch);
		    
		    // compute BB0
		    Set<Instance> targetsBB0 = AllenAlgebraMapper.getIntermediateMapping(targetBB, intervalBB, 0.0d);
		    Set<String> tempBB0 = new TreeSet<String>();
		    for (Instance i : targetsBB0)
			tempBB0.add(i.getUri());
		    
		    // compute BB1
		    Set<Instance> targetsBB1 = AllenAlgebraMapper.getIntermediateMapping(targetBB, intervalBB, 1.0d);
		    Set<String> tempBB1 = new TreeSet<String>();
		    for (Instance i : targetsBB1)
			tempBB1.add(i.getUri());
		    
		    // union (BB0 U BB1)
		    Set<String> union = AllenAlgebraMapper.union(tempBB0, tempBB1);
		    // difference EE0 \ (BB0 U BB1)
		    Set<String> difference = AllenAlgebraMapper.difference(tempEE, union);

		    long end2 = System.currentTimeMillis();
		    duration += end2 - start2;

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
	return "BeginAfterEndTogether: EE0 \\ (BB0 U BB1)";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {
	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapEE0 = maps.get(0);
	TreeMap<String, Set<String>> mapBB0 = maps.get(1);
	TreeMap<String, Set<String>> mapBB1 = maps.get(2);
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
	for (Map.Entry<String, Set<String>> entryEE0 : mapEE0.entrySet()) {

	    long start1 = System.currentTimeMillis();
	    String instanceEE0 = entryEE0.getKey();
	    Set<String> setEE0 = entryEE0.getValue();

	    Set<String> setBB0 = mapBB0.get(instanceEE0);
	    Set<String> setBB1 = mapBB1.get(instanceEE0);
	    if (setBB0 == null)
		setBB0 = new TreeSet<String>();
	    if (setBB1 == null)
		setBB1 = new TreeSet<String>();

	    Set<String> union = AllenAlgebraMapper.union(setBB0, setBB1);
	    Set<String> difference = AllenAlgebraMapper.difference(setEE0, union);
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += difference.size();
	    /*if (iteration != 0)
		continue;
	    if (!difference.isEmpty()) {
		for (String targetInstanceUri : difference) {
		    try {
			this.addLink(instanceEE0, targetInstanceUri);
			writer.write(instanceEE0 + "," + targetInstanceUri + "\n");
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
