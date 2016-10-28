package de.uni_leipzig.simba.allenalgebra.mappers.complex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class DirectlyAfter extends AllenAlgebraMapper {
    public DirectlyAfter() {
	// BE0
	requiredAtomicRelationsSource.add(0);
	requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(2);
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
	TreeMap<String, Set<Instance>> sourceBE0 = sourceBlocks.get(0);
	TreeMap<String, Set<Instance>> targetBE0 = targetBlocks.get(0);

	for (Entry<String, Set<Instance>> entry : sourceBE0.entrySet()) {
	    long start = System.currentTimeMillis();
	    // get targets from BE0
	    String sourceInterval = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    Set<Instance> m = AllenAlgebraMapper.getIntermediateMapping(targetBE0, sourceInterval,
		    0.0);
	    long end = System.currentTimeMillis();
	    duration += end - start;
	    size += (m.size()*sourceInstances.size());
	    /*if (iteration != 0)
		continue;
	    if (!m.isEmpty()) {
		for (Instance sourceInstance : sourceInstances) {
		    for (Instance targetInstanceUri : m) {
			try {
			    this.addLink(sourceInstance.getUri(), targetInstanceUri.getUri());
			    System.out.println(targetInstanceUri);
			    writer.write(sourceInstance.getUri() + "," + targetInstanceUri.getUri() + "\n");
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
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

    @Override
    public String getName() {
	return "DirectlyAfter: BE0";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {
	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapBE0 = maps.get(0);
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
	for (Map.Entry<String, Set<String>> entryBE0 : mapBE0.entrySet()) {
	    long start1 = System.currentTimeMillis();
	    String instancBE0 = entryBE0.getKey();
	    Set<String> setBE0 = entryBE0.getValue();
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += setBE0.size();
	    /*if (iteration != 0)
		continue;
	    for (String targetInstanceUri : setBE0) {
		try {
		    this.addLink(instancBE0, targetInstanceUri);
		    writer.write(instancBE0 + "," + targetInstanceUri + "\n");
		} catch (IOException e) {
		    // TODO Auto-generated catch block
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
