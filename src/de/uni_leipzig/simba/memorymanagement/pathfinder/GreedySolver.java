/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.pathfinder;

import java.util.Map;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import de.uni_leipzig.simba.memorymanagement.lazytsp.serial.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author ngonga
 */
public class GreedySolver implements PathFinder{

    /**
     * Computes a path across different clusters in a greedy fashion
     *
     * @param clusters Set of clusters
     * @return Sequence of clusters
     */
    public int[] getPath(Map<Integer, Cluster> clusters) {
        Map<Integer, Map<Integer, Integer>> graph = new HashMap<>();
        int maxEdgeNode = -1;
        int maxSim = 0;

        List<Integer> list = new ArrayList<>(clusters.keySet());
        Collections.sort(list);
            //build graph
        for (int i : list) {
            for (int j : list) {
                if (i < j) {
                    int sim = getSimilarity(clusters.get(i), clusters.get(j));
                    if (sim > 0) {
                        if (!graph.containsKey(i)) {
                            graph.put(i, new HashMap<Integer, Integer>());
                        }
                        if (!graph.containsKey(j)) {
                            graph.put(j, new HashMap<Integer, Integer>());
                        }
                        graph.get(i).put(j, sim);
                        graph.get(j).put(i, sim);
                        if (sim > maxSim) {
                            maxSim = sim;
                            maxEdgeNode = i;
                        }
                    }
                }
            }
        }

        //get path
        Set<Integer> seenNodes = new HashSet<>();
        List<Integer> path = new ArrayList<>();
        seenNodes.add(maxEdgeNode);
        path.add(maxEdgeNode);
        int length = clusters.keySet().size();
        while (path.size() < length) {
            int currentIndex = path.get(path.size() - 1);
            int sim = 0;
            int bestIndex = -1;
            if (graph.containsKey(currentIndex)) {
                for (Integer i : graph.get(currentIndex).keySet()) {
                    if (!seenNodes.contains(i)) {
                        if (graph.get(currentIndex).get(i) > sim) {
                            sim = graph.get(currentIndex).get(i);
                            bestIndex = i;
                        }
                    }
                }
            }

            if (bestIndex == -1) {
                for (Integer k : clusters.keySet()) {
                    if (!seenNodes.contains(k)) {
                        bestIndex = k;
                        break;
                    }
                }
            }
            seenNodes.add(bestIndex);
            path.add(bestIndex);

        }
        
        int[] ransformedath = new int[path.size()];
		for (int k = 0; k < path.size(); k++) {
			ransformedath[k] = path.get(k);
		}
		
        return ransformedath;
    }

    public int getSimilarity(Cluster c1, Cluster c2) {
        int sim = 0;
        TreeSet<Node> n1 = new TreeSet<>(c1.nodes);
        TreeSet<Node> n2 = new TreeSet<>(c2.nodes);
        n1.retainAll(n2);
        for (Node n : n1) {
            sim = sim + n.getWeight();
        }
        return sim;
    }
}
