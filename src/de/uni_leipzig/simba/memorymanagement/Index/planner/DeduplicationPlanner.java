/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.Index.planner;

import java.io.File;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.structure.IndexerType;

/**
 * Interface for a planner that generates a list of cache access commands that 
 * decide on when data is to be loaded and flushed from memory 
 * @author ngonga
 */
public interface DeduplicationPlanner {
    List<DataManipulationCommand> run(File data, Map<String, String> parameters, IndexerType indexer, int capacity);
}
