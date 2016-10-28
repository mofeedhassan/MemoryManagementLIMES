/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.Index.graphclustering;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author ngonga
 */
public class SimpleEdgeClustering implements Clustering {

    Set<Cluster> clusters;

    public SimpleEdgeClustering() {
        clusters = new HashSet<>();
    }
    
    public Map<Integer, Cluster> cluster(Graph g, int capacity) {
        Map<Integer, Cluster> map = new TreeMap<>();
        int counter = 0;
        Set<Edge> allEdges = new HashSet<Edge>(g.getAllEdges());
        Map<String, Cluster> result = new TreeMap<>();
//        int counter = 0;
        for(Edge e: allEdges)
        {
            if(!result.containsKey(e.source.item.getId()))
            {
                Cluster c = new Cluster(counter);
                c.nodes.add(e.source);
                result.put(e.source.item.getId(), c);
                map.put(counter, c);
                counter++;
            }
            
            Cluster c = result.get(e.source.item.getId());
            c.edges.add(e);
            c.nodes.add(e.target);
        }
        return map;
    }

    @Override
    public Set<Cluster> getClusters() {
        return clusters;
    }
    
}
