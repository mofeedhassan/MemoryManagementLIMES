/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic.topology;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import de.uni_leipzig.simba.data.Mapping;
import static de.uni_leipzig.simba.mapper.atomic.OrchidMapper.getPoints;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Useful utils for the topological relations.
 * @author psmeros
 */
public class TopologicalRelationUtils {

    //This variable is useful only when creating the squares.
    //It must be given as parameter or (better) be computed dynamically.
    public static float theta=10;

    public static boolean verbose = false;
    public static long indexingTime;
    
    public static final String EQUALS = "equals";
    public static final String DISJOINT = "disjoint";
    public static final String INTERSECTS = "intersects";
    public static final String TOUCHES = "touches";
    public static final String CROSSES = "crosses";
    public static final String WITHIN = "within";
    public static final String CONTAINS = "contains";
    public static final String OVERLAPS = "overlaps";

    
     /**
     * Computes the geo squares for each polygon based on their MBBs (Minimum Bounding Boxes).
     *
     * @param input Set of polygon to be indexed
     * @return Index for all polygons
     */
    static GeoIndex assignSquaresByMBBs(Set<Polygon> input) {
        float delta = (float) ((theta * 180) / (Math.PI * OrthodromicDistance.R));

        GeoIndex index = new GeoIndex();
        for (Polygon p : input) {
            Geometry g = null;
            try {
                g = p.getGeometry();
            } catch (ParseException ex) {
                Logger.getLogger(TopologicalRelationUtils.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            Envelope envelope = g.getEnvelopeInternal();
            
            int minLatIndex = (int) Math.floor(envelope.getMinY() / delta);
            int maxLatIndex = (int) Math.ceil(envelope.getMaxY() / delta);
            int minLongIndex = (int) Math.floor(envelope.getMinX() / delta);
            int maxLongIndex = (int) Math.ceil(envelope.getMaxX() / delta);
            
            for (int latIndex = minLatIndex; latIndex<=maxLatIndex; latIndex++) {
                for (int longIndex = minLongIndex; longIndex<=maxLongIndex; longIndex++) {
                    if (verbose) {
                        System.out.println(p.uri + ": (" + latIndex + "," + longIndex + ")");
                    }
                    index.addPolygon(p, latIndex, longIndex);
                }
            }
        }
        return index;
    }
    
    /**
      * This function returns true if the given relation holds between two polygons.
      *
      * @param polygon1
      * @param polygon2
      * @param relation
      * @return Boolean
      */
    static Boolean relate(Polygon polygon1, Polygon polygon2, String relation) {
        try {
            Geometry geometry1 = polygon1.getGeometry();
            Geometry geometry2 = polygon2.getGeometry();
            
            switch (relation) {
                case EQUALS: return geometry1.equals(geometry2);
                case DISJOINT: return geometry1.disjoint(geometry2);
                case INTERSECTS: return geometry1.intersects(geometry2);
                case TOUCHES: return geometry1.touches(geometry2);
                case CROSSES: return geometry1.crosses(geometry2);
                case WITHIN: return geometry1.within(geometry2);
                case CONTAINS: return geometry1.contains(geometry2);
                case OVERLAPS: return geometry1.overlaps(geometry2);
                default: return geometry1.relate(geometry2, relation);
            }
        } catch (ParseException ex) {
            Logger.getLogger(TopologicalRelationUtils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }   
    
    /**
     * This function computes the Mapping between two sets of Polygons based on a given relation.
     *
     * @param sourceData Set of Polygons
     * @param targetData Set of Polygons
     * @param relation
     * @return Mapping
     */
    public static Mapping getMapping(Set<Polygon> sourceData, Set<Polygon> targetData, String relation) {

        long begin = System.currentTimeMillis();
        GeoIndex source = assignSquaresByMBBs(sourceData);
        GeoIndex target = assignSquaresByMBBs(targetData);            
        long end = System.currentTimeMillis();
    	indexingTime = end - begin;
        Map<String, Set<String>> computed = new HashMap<>();
        if(verbose){
        	System.out.println("Geo-Indexing took: " + indexingTime + " ms");
        	System.out.println("|Source squares|= " + source.squares.keySet().size());
        	System.out.println("|Target squares|= " + target.squares.keySet().size());
        	System.out.println("Index = " + source);
        }
        Mapping m = new Mapping();

        for (Integer sourceLatIndex : source.squares.keySet()) {
            for (Integer sourceLongIndex : source.squares.get(sourceLatIndex).keySet()) {
                GeoSquare g1 = source.getSquare(sourceLatIndex, sourceLongIndex);

                //case that two geometries are in the same square
                GeoSquare g2 = target.getSquare(sourceLatIndex, sourceLongIndex);
                for (Polygon a : g1.elements) {
                    for (Polygon b : g2.elements) {
                        if (!computed.containsKey(a.uri)) {
                            computed.put(a.uri, new HashSet<String>());
                        }
                        if (!computed.get(a.uri).contains(b.uri)) {
                            if (relate(a, b, relation))
                            {
                                m.add(a.uri, b.uri, 1.0);
                            }
                            computed.get(a.uri).add(b.uri);
                        }
                    }
                }

                //case that two geometries are in different squares (in this case the DISJOINT relation holds)                    
                if(relation.equals(DISJOINT))
                {
                    for (Integer targetLatIndex : target.squares.keySet()) {
                        for (Integer targetLongIndex : target.squares.get(targetLatIndex).keySet()) {
                            if(!sourceLatIndex.equals(targetLatIndex) || !sourceLongIndex.equals(targetLongIndex))
                            {
                                g2 = target.getSquare(targetLatIndex, targetLongIndex);
                                for (Polygon a : g1.elements) {
                                    for (Polygon b : g2.elements) {
                                        if (!computed.containsKey(a.uri)) {
                                            computed.put(a.uri, new HashSet<String>());
                                        }
                                        if (!computed.get(a.uri).contains(b.uri)){
                                            if(verbose)
                                            {
                                                System.out.println("geometries in different squares -> disjoint");
                                            }
                                            m.add(a.uri, b.uri, 1.0);
                                            computed.get(a.uri).add(b.uri);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }            
        return m;
    } 
    
    public static void main(String args[]) {

        String polygonA, polygonB, relation;
        
        polygonA = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        polygonB = "POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2))";
        relation = DISJOINT;
        theta = 10;
        System.out.println("Test 1: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));

        polygonA = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        polygonB = "POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2))";
        relation = DISJOINT;
        theta = 1000;
        System.out.println("Test 2: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));
        
        polygonA = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        polygonB = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        relation = DISJOINT;
        System.out.println("Test 3: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));

        polygonA = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        polygonB = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        relation = EQUALS;
        System.out.println("Test 4: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));

        polygonA = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        polygonB = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        relation = INTERSECTS;
        System.out.println("Test 5: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));

        polygonA = "POLYGON ((0 0, 3 0, 3 3, 0 3, 0 0))";
        polygonB = "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))";
        relation = CONTAINS;
        System.out.println("Test 6: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));

        polygonA = "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))";
        polygonB = "POLYGON ((0 0, 3 0, 3 3, 0 3, 0 0))";
        relation = WITHIN;
        System.out.println("Test 7: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));        

        polygonA = "POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1))";
        polygonB = "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))";                
        relation = OVERLAPS;
        System.out.println("Test 8: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));

        polygonA = "POLYGON ((0 2, 2 3, 3 3, 3 2, 0 2))";
        polygonB = "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))";                
        relation = TOUCHES;
        System.out.println("Test 9: " + (((getMapping((new HashSet<>(Arrays.asList(new Polygon("A", getPoints(polygonA))))), (new HashSet<>(Arrays.asList(new Polygon("Β", getPoints(polygonB))))), relation).size != 0)) ? (polygonA + " " + relation + " " + polygonB) : "No Mapping."));        
    }
}
