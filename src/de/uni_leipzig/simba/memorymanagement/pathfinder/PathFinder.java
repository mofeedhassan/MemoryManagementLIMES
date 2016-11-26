/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.pathfinder;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.lazytsp.serial.Item;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for TSP solvers and path finders
 * @author ngonga
 */
public interface PathFinder {
    /*List<Integer> getPath(Map<Integer, Cluster> clusters);*/
	int[] getPath(Map<Integer, Cluster> clusters);
}
