/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.pathfinder;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Edge;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author ngonga
 */
public class SimpleSolver extends GreedySolver {

    public static double optimizationTime = 100;
    
    public int[] getPath(Map<Integer, Cluster> clusters) {        
        List<Integer> list = new ArrayList<>(clusters.keySet());
        int delta;
        //Collections.sort(list);
        long time = System.currentTimeMillis();
        double currentCondition = getCondition(list, clusters);
        while(System.currentTimeMillis() - time < optimizationTime)
        {
            //pick two clusters
            int index1 = (int)(Math.random()*(list.size()-2))+1;            
            int index2 = (int)(Math.random()*(list.size()-2))+1;

            int i = list.get(index1);
            int j = list.get(index2);
            //compute condition if permutation is carried out
             delta = getSimilarity(clusters.get(list.get(index1-1)), clusters.get(j)) +
                     getSimilarity(clusters.get(list.get(index1+1)), clusters.get(j)) +
                     getSimilarity(clusters.get(list.get(index2-1)), clusters.get(i)) +
                     getSimilarity(clusters.get(list.get(index2+1)), clusters.get(i))
                     -
                     (
                     getSimilarity(clusters.get(list.get(index1-1)), clusters.get(i)) +
                     getSimilarity(clusters.get(list.get(index1+1)), clusters.get(i)) +
                     getSimilarity(clusters.get(list.get(index2-1)), clusters.get(j)) +
                     getSimilarity(clusters.get(list.get(index2+1)), clusters.get(j)));
            //if better, perform permutation
             if(delta > 0)
             {
                 int temp = list.get(index1);
                 list.set(index1, list.get(index2));
                 list.set(index2, temp);
                 currentCondition = currentCondition - delta;
                 //System.out.println("Condition: "+currentCondition);
             }
             
             
        }
        
        int[] ransformedath = new int[list.size()];
		for (int k = 0; k < list.size(); k++) {
			ransformedath[k] = list.get(k);
		}
		
        return ransformedath;
    }

    private double getCondition(List<Integer> list, Map<Integer, Cluster> clusters) {
        double condition = 0d;
        for(int i=1; i<list.size(); i++)
        {
            condition = condition + getSimilarity(clusters.get(i), clusters.get(i-1));
        }
        return condition;
    }
    
    
    
}
