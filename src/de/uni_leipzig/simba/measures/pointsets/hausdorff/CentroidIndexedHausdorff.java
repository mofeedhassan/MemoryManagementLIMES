/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.measures.pointsets.hausdorff;


import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class CentroidIndexedHausdorff extends IndexedHausdorff {

    public CentroidIndex sourceIndex;
    boolean verbose = false;
    public IndexedHausdorff ih = new IndexedHausdorff();
    
    public CentroidIndexedHausdorff()
    {
        ih = new IndexedHausdorff();
    }
    
    public void computeIndexes(Set<Polygon> source, Set<Polygon> target)
    {
        sourceIndex = new CentroidIndex();
        sourceIndex.index(source);
        targetIndex = new CentroidIndex();
        targetIndex.index(target);
        ih.targetIndex = targetIndex;      
    }
    
    @Override
    public Mapping run(Set<Polygon> source, Set<Polygon> target, double threshold) {
        // first run indexing
        Mapping m = new Mapping();
        targetIndex = new CentroidIndex();
        sourceIndex = new CentroidIndex();
//        long begin = System.currentTimeMillis();
        targetIndex.index(target);
        sourceIndex.index(source);
        ih.targetIndex = targetIndex;
//        long end = System.currentTimeMillis();
//        System.out.println("Indexing took " + (end - begin) + " ms and "+targetIndex.computations+" computations.");
        double d;
        for (Polygon s : source) {
            for (Polygon t : target) {
                d = distance(sourceIndex.centroids.get(s.uri).center, ((CentroidIndex) targetIndex).centroids.get(t.uri).center);
                if (d - (sourceIndex.centroids.get(s.uri).radius + ((CentroidIndex) targetIndex).centroids.get(t.uri).radius) <= threshold) {
                    d = computeDistance(s, t, threshold);
                    if (d <= threshold) {
                        m.add(s.uri, t.uri, d);
                    }
                }
            }
        }
        return m;
    }

    @Override
    public double computeDistance(Polygon X, Polygon Y, double threshold) {        
        //centroid distance check
        double d = distance(sourceIndex.centroids.get(X.uri).center, ((CentroidIndex) targetIndex).centroids.get(Y.uri).center);
        if (d - (sourceIndex.centroids.get(X.uri).radius + ((CentroidIndex) targetIndex).centroids.get(Y.uri).radius) > threshold) {
            return threshold + 1;
        }
        return ih.computeDistance(X, Y, threshold);
    }
}
