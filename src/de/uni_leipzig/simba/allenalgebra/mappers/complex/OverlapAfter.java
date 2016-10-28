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
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class OverlapAfter extends AllenAlgebraMapper {
    public OverlapAfter() {
	// { BE1 \ (BB0 U BB1) } \ (EE0 U EE1)
	requiredAtomicRelationsSource.add(0);

	requiredAtomicRelationsTarget.add(0);
	requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(3);
	this.getRequiredAtomicRelations().add(0);
	this.getRequiredAtomicRelations().add(1);
	this.getRequiredAtomicRelations().add(6);
	this.getRequiredAtomicRelations().add(7);
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

	    String sourceIntervalB = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    // targets of BE1 are the same for each instance in sourceInstances
	    Set<Instance> targetBE1Instances = AllenAlgebraMapper.getIntermediateMapping(targetEE, sourceIntervalB,
		    1.0);
	    Set<String> tempBE = new TreeSet<String>();
	    for (Instance i : targetBE1Instances)
		tempBE.add(i.getUri());
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;

	    if (!targetBE1Instances.isEmpty()) {
		long start2 = System.currentTimeMillis();
		// remember that the targets of the BB0 and BB1 are the same
		// for each source in the instances of BE0
		// get targets for BB0
		Set<Instance> targetBB0Instaces = AllenAlgebraMapper.getIntermediateMapping(targetBB, sourceIntervalB,
			0.0d);
		Set<String> tempBB0 = new TreeSet<String>();
		for (Instance i : targetBB0Instaces)
		    tempBB0.add(i.getUri());

		// get targets for BB1
		Set<Instance> targetBB1Instaces = AllenAlgebraMapper.getIntermediateMapping(targetBB, sourceIntervalB,
			1.0d);
		Set<String> tempBB1 = new TreeSet<String>();
		for (Instance i : targetBB1Instaces)
		    tempBB1.add(i.getUri());
		// union BB0 U BB1
		Set<String> targetBBInstances = AllenAlgebraMapper.union(tempBB0, tempBB1);

		// difference BE1 \ (BB0 U BB1)
		Set<String> difference1 = AllenAlgebraMapper.difference(tempBE, targetBBInstances);

		long end2 = System.currentTimeMillis();
		duration += end2 - start2;

		for (Instance sourceInstance : sourceInstances) {

		    if (!difference1.isEmpty()) {
			long start3 = System.currentTimeMillis();
			// source interval of each instance in EE is different
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
			// get targets for EE0
			Set<Instance> targetEE0Instaces = AllenAlgebraMapper.getIntermediateMapping(targetEE,
				intervalEE, 0.0d);
			Set<String> tempEE0 = new TreeSet<String>();
			for (Instance i : targetEE0Instaces)
			    tempEE0.add(i.getUri());

			// get targets for EE1
			Set<Instance> targetEE1Instaces = AllenAlgebraMapper.getIntermediateMapping(targetEE,
				intervalEE, 1.0d);
			Set<String> tempEE1 = new TreeSet<String>();
			for (Instance i : targetEE1Instaces)
			    tempEE1.add(i.getUri());
			// union EE0 U EE1
			Set<String> targetEEInstances = AllenAlgebraMapper.union(tempEE0, tempEE1);

			// final difference { BE1 \ (BB0 U BB1) } \ (EE0 U
			// EE1)
			Set<String> difference2 = AllenAlgebraMapper.difference(difference1, targetEEInstances);
			long end3 = System.currentTimeMillis();
			duration += end3 - start3;
			size += difference2.size();

			/*if (iteration != 0)
			    continue;
			if (!difference2.isEmpty()) {
			    for (String targetInstanceUri : difference2) {
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
	return "OverlapAfter: {BE1 \\ (BB1 U BB0)} \\ (EE0 U EE1)";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {
	TreeMap<String, Set<String>> mapBE1 = maps.get(0);

	TreeMap<String, Set<String>> mapBB0 = maps.get(1);
	TreeMap<String, Set<String>> mapBB1 = maps.get(2);

	TreeMap<String, Set<String>> mapEE0 = maps.get(3);
	TreeMap<String, Set<String>> mapEE1 = maps.get(4);

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
	for (Map.Entry<String, Set<String>> entryBE1 : mapBE1.entrySet()) {
	    long start = System.currentTimeMillis();
	    // get targets from EB1
	    String instanceBE1 = entryBE1.getKey();
	    Set<String> setBE1 = entryBE1.getValue();

	    Set<String> setBB0 = mapBB0.get(instanceBE1);
	    Set<String> setBB1 = mapBB1.get(instanceBE1);
	    if (setBB0 == null)
		setBB0 = new TreeSet<String>();
	    if (setBB1 == null)
		setBB1 = new TreeSet<String>();
	    Set<String> unionBB = AllenAlgebraMapper.union(setBB0, setBB1);
	    Set<String> difference1 = AllenAlgebraMapper.difference(setBE1, unionBB);

	    Set<String> setEE0 = mapEE0.get(instanceBE1);
	    Set<String> setEE1 = mapEE1.get(instanceBE1);
	    if (setEE0 == null)
		setEE0 = new TreeSet<String>();
	    if (setEE1 == null)
		setEE1 = new TreeSet<String>();
	    Set<String> unionEE = AllenAlgebraMapper.union(setEE0, setEE1);

	    Set<String> difference2 = AllenAlgebraMapper.difference(difference1, unionEE);

	    long end = System.currentTimeMillis();
	    duration += end - start;
	    size += difference2.size();

	    /*if (iteration != 0)
		continue;
	    if (!difference2.isEmpty()) {
		for (String targetInstanceUri : difference2) {
		    try {
			this.addLink(instanceBE1, targetInstanceUri);
			writer.write(instanceBE1 + "," + targetInstanceUri + "\n");
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
