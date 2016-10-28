package de.uni_leipzig.simba.allenalgebra.mappers.complex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.data.Instance;

public class StrictlyAfter extends AllenAlgebraMapper {

    public StrictlyAfter() {
	// SxT \ (BE0 U BE1)

	requiredAtomicRelationsSource.add(0);
	requiredAtomicRelationsTarget.add(1);

	this.getRequiredAtomicRelations().add(2);
	this.getRequiredAtomicRelations().add(3);
    }

    @Override
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks,
	    ArrayList<TreeMap<String, Set<Instance>>> targetBlocks) {

	TreeMap<String, Set<Instance>> sourcesBE = sourceBlocks.get(0);
	TreeMap<String, Set<Instance>> targetsBE = targetBlocks.get(0);

	Set<String> targets = new HashSet<String>();
	targets.addAll(target.getAllUris());
	
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
	for (Entry<String, Set<Instance>> entry : sourcesBE.entrySet()) {
	    long start = System.currentTimeMillis();
	    String intervalBE = entry.getKey();
	    Set<Instance> sourceInstances = entry.getValue();

	    // same targets for all source instances in this time blocks in BE0
	    Set<Instance> targetInstancesBE0 = AllenAlgebraMapper.getIntermediateMapping(targetsBE,
		    intervalBE, 0.0d);
	    Set<String> tempBE0 = new TreeSet<String>();
	    for (Instance i : targetInstancesBE0)
		tempBE0.add(i.getUri());
	    
	    // time interval of a source in BE0 and BE1 is the same
	    // same targets for all source instances in this time blocks in BE1
	    Set<Instance> targetInstancesBE1 = AllenAlgebraMapper.getIntermediateMapping(targetsBE,
		    intervalBE, 1.0d);
	    Set<String> tempBE1 = new TreeSet<String>();
	    for (Instance i : targetInstancesBE1)
		tempBE1.add(i.getUri());
	    
	    // get (BE0 U BE1) of targets
	    Set<String> union = AllenAlgebraMapper.union(tempBE0, tempBE1);

	    // SxT \ (BE0 U BE1)
	    Set<String> difference = AllenAlgebraMapper.difference(targets, union);

	    long end = System.currentTimeMillis();
	    duration += end - start;
	    size += (difference.size()*sourceInstances.size());

	    /*if (iteration != 0)
		continue;
	    for (Instance sourceInstance : sourceInstances) {
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
	return "StrictlyAfter: SxT \\ (BE0 U BE1)";
    }

    @Override
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps) {
	
	TreeMap<String, Set<String>> mapBE0 = maps.get(0);
	TreeMap<String, Set<String>> mapBE1 = maps.get(1);

	Set<String> sources = new HashSet<String>();
	sources.addAll(source.getAllUris());
	
	Set<String> targets = new HashSet<String>();
	targets.addAll(target.getAllUris());
	
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
	
	for (String sourceInstance : sources) {

	    long start1 = System.currentTimeMillis();
	    Set<String> setBE0 = mapBE0.get(sourceInstance);
	    Set<String> setBE1 = mapBE1.get(sourceInstance);
	    if(setBE0 == null)
		setBE0 = new TreeSet<String>();
	    if(setBE1 == null)
		setBE1 = new TreeSet<String>();
	    

	    Set<String> union = AllenAlgebraMapper.union(setBE0, setBE1);
	    Set<String> difference = AllenAlgebraMapper.difference(targets, union);
	    
	    long end1 = System.currentTimeMillis();
	    duration += end1 - start1;
	    size += difference.size();

	    /*if (iteration != 0)
		continue;
	    if (!difference.isEmpty()) {
		for (String targetInstanceUri : difference) {
		    try {
			this.addLink(sourceInstance, targetInstanceUri);
			writer.write(sourceInstance + "," + targetInstanceUri + "\n");
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
