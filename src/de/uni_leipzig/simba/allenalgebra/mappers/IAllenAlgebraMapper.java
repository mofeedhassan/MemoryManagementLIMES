package de.uni_leipzig.simba.allenalgebra.mappers;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.data.Instance;

public interface IAllenAlgebraMapper{
    public void getMappingMemoryEfficiency(ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks, ArrayList<TreeMap<String, Set<Instance>>> targetBlocks);
    public void getMappingTimeEfficiency(ArrayList<TreeMap<String, Set<String>>> maps);
    public String getName();
}
