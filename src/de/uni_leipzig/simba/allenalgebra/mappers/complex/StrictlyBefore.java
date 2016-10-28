package de.uni_leipzig.simba.allenalgebra.mappers.complex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.AtomicAllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class StrictlyBefore extends AllenAlgebraMapper {

    public StrictlyBefore() {
	// EB1
	requiredAtomicRelationsSource.add(1);
	requiredAtomicRelationsTarget.add(0);

	this.getRequiredAtomicRelations().add(5);
    }

    @Override
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks,
	    ArrayList<TreeMap<String, Set<Instance>>> targetBlocks) {

	TreeMap<String, Set<Instance>> sourceEB1 = sourceBlocks.get(0);
	TreeMap<String, Set<Instance>> targetEB1 = targetBlocks.get(0);

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

	for (Entry<String, Set<Instance>> entry : sourceEB1.entrySet()) {

	    long start = System.currentTimeMillis();
	    // get targets from EB1
	    String sourceInterval = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    Set<Instance> m = AllenAlgebraMapper.getIntermediateMapping(targetEB1, sourceInterval, 1.0);
	    long end = System.currentTimeMillis();
	    duration += end - start;
	    size += (m.size() * sourceInstances.size());

	    /*if (iteration != 0)
		continue;
	    if (!m.isEmpty()) {
		for (Instance sourceInstance : sourceInstances) {
		    for (Instance targetInstanceUri : m) {
			try {
			    this.addLink(sourceInstance.getUri(), targetInstanceUri.getUri());
			    writer.write(sourceInstance.getUri() + "," + targetInstanceUri.getUri() + "\n");
			} catch (IOException e) { // TODO Auto-generated catch
						  // block
			    e.printStackTrace();
			}
		    }
		}
	    }*/

	}
	try

	{
	    writer.flush();
	    writer.close();
	} catch (

	IOException e)

	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public String getName() {
	return "StrictlyBefore: EB1";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {

	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapEB1 = maps.get(0);
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
	for (Map.Entry<String, Set<String>> entryEB1 : mapEB1.entrySet()) {
	    long start1 = System.currentTimeMillis();
	    String instancEB1 = entryEB1.getKey();
	    Set<String> setEB1 = entryEB1.getValue();
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += setEB1.size();

	    /*if (iteration != 0)
		continue;
	    for (String targetInstanceUri : setEB1) {
		try {
		    this.addLink(instancEB1, targetInstanceUri);
		    writer.write(instancEB1 + "," + targetInstanceUri + "\n");
		} catch (IOException e) {
		    e.printStackTrace();
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
