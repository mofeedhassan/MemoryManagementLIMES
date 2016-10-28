/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.measures.pointsets.hausdorff;

import de.uni_leipzig.simba.data.Point;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GreatEllipticDistance;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.OrthodromicDistance;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.PolygonIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds the distance from the centroids, which are in the middle of the longest axis. 
 * Also stores the radius of the smallest circle which contains the polygon entirely
 * @author ngonga
 */
public class CentroidIndex extends PolygonIndex{
    
	public Map<String, Circle> centroids;
    public CentroidIndex()
    {
        super();
        centroids = new HashMap<String, Circle>();        
    }
	
    /**
     * @param x Point x
     * @param y Point y
     * @return Distance between x and y
     */
    public double distance(Point x, Point y) {
        if(USE_GREAT_ELLIPTIC_DISTANCE){
        	return GreatEllipticDistance.getDistanceInDegrees(x,y);
        }
        return OrthodromicDistance.getDistanceInDegrees(x, y);
    }
    
    @Override
    public void index(Polygon p)
    {
        Map<Point, Map<Point, Double>> index = new HashMap<Point, Map<Point, Double>>();
        Map<Point, Double> distances;
        double maxDistance = 0;
        double distance;
        int from = -1, to=-1;
        for (int i = 0; i < p.points.size(); i++) {
            distances = new HashMap<Point, Double>();            
            for (int j = i + 1; j < p.points.size(); j++) {
                distance = distance(p.points.get(i), p.points.get(j));
                distances.put(p.points.get(j), distance);
                if(distance > maxDistance)
                {
                    maxDistance = distance;
                    from = i;
                    to = j;
                }
                computations++;
            }
            if (!distances.isEmpty()) {
                index.put(p.points.get(i), distances);
            }
        }
        // if polygon size is above 1, then compute the middle of the longest axis
        if(from >= 0)
        {
            centroids.put(p.uri, new Circle(average(p.points.get(from), p.points.get(to)), maxDistance/2.0));
        }
        
        //else take the point itself
        else
        {
            centroids.put(p.uri, new Circle(p.points.get(0), 0.0));
        }
        distanceIndex.put(p.uri, index);
        polygonIndex.put(p.uri, p);        
    }
    
    public Point average(Point source, Point target)
    {
        List<Double> coordinates = new ArrayList<Double>();
        for(int i=0; i<source.coordinates.size(); i++)
        {
            coordinates.add((source.coordinates.get(i) + target.coordinates.get(i))/2.0);
        }
        Point center = new Point("", coordinates);
        return center;
    }
    
    public class Circle
    {
        public Point center;
        public double radius;
        
        public Circle(Point c, double r)
        {
            center = c;
            radius = r;
        }
    }
}
