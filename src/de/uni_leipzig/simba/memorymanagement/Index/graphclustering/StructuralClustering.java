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
import java.util.TreeSet;

/**
 *
 * @author ngonga
 */
public class StructuralClustering implements Clustering {

    Set<Cluster> clusters;

    public StructuralClustering() {
        clusters = new HashSet<>();
    }

    public Map<Integer, Cluster> cluster(Graph g, int capacity) {
        Map<Integer, Cluster> map = new TreeMap<>();
        int counter = 0;
        Set<Edge> allEdges = new HashSet<Edge>(g.getAllEdges());
        Set<Node> sources = new HashSet<>();
        for(Edge e: allEdges)
        {
            sources.add(e.source);
        }
        for (Node n : sources) {
            Cluster c = new Cluster(counter);
            c.nodes.add(n);
            Set<Edge> edges = g.getEdges(n);
            for (Edge e : edges) {
//                if(e.source == n)
//                {
                c.edges.add(e);
                c.nodes.add(e.target);
                c.nodes.add(e.source);
                allEdges.remove(e);
//                }
            }
            map.put(counter, c);
            counter++;
        }
        Cluster blob = new Cluster(counter);
        for (Edge e : allEdges) {
//                if(e.source == n)
//                {
            blob.edges.add(e);
            blob.nodes.add(e.target);
            blob.nodes.add(e.source);
//                    allEdges.remove(e);
//                }
        }
        map.put(counter, blob);
//        System.out.println("Remaining edges = " + allEdges.size());
        clusters.addAll(map.values());
        return map;
    }

    @Override
    public Set<Cluster> getClusters() {
        return clusters;
    }

}
