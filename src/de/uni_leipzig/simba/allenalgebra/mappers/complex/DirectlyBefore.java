package de.uni_leipzig.simba.allenalgebra.mappers.complex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.AtomicAllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class DirectlyBefore extends AllenAlgebraMapper {

    public DirectlyBefore() {
	// EB0
	requiredAtomicRelationsSource.add(1);
	requiredAtomicRelationsTarget.add(0);

	this.getRequiredAtomicRelations().add(4);

    }

    @Override
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks,
	    ArrayList<TreeMap<String, Set<Instance>>> targetBlocks) {

	TreeMap<String, Set<Instance>> sourceEB0 = sourceBlocks.get(0);
	TreeMap<String, Set<Instance>> targetEB0 = targetBlocks.get(0);

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

	for (Entry<String, Set<Instance>> entry : sourceEB0.entrySet()) {

	    long start = System.currentTimeMillis();
	    // get targets from EB0
	    String sourceInterval = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();
	    Set<Instance> m = AllenAlgebraMapper.getIntermediateMapping(targetEB0, sourceInterval, 0.0);
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
	return "DirectlyBefore: EB0";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {

	File f = new File(filePath);
	FileWriter writer = null;

	TreeMap<String, Set<String>> mapEB0 = maps.get(0);
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
	for (Map.Entry<String, Set<String>> entryEB0 : mapEB0.entrySet()) {
	    long start1 = System.currentTimeMillis();
	    String instancEB0 = entryEB0.getKey();
	    Set<String> setEB0 = entryEB0.getValue();
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
    	    size += setEB0.size();

	    /*if (iteration != 0)
		continue;
	    for (String targetInstanceUri : setEB0) {
		try {
		    this.addLink(instancEB0, targetInstanceUri);
		    writer.write(instancEB0 + "," + targetInstanceUri + "\n");
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
