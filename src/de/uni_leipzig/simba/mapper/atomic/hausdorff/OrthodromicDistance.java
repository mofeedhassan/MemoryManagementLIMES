/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic.hausdorff;

import java.util.ArrayList;
import java.util.List;

import de.uni_leipzig.simba.data.Point;

/**
 *
 * @author ngonga
 */
public class OrthodromicDistance {

    public static double R = 6371f;

    public static double getDistanceInDegrees(Point x, Point y) {
	return getDistanceInDegrees(x.coordinates.get(0), x.coordinates.get(1), y.coordinates.get(0),
		y.coordinates.get(1));
    }

    public static double getDistanceInDegrees(double lat1, double long1, double lat2, double long2) {
	double la1 = (double) Math.toRadians(lat1);
	double lo1 = (double) Math.toRadians(long1);
	double la2 = (double) Math.toRadians(lat2);
	double lo2 = (double) Math.toRadians(long2);
	return getDistance(la1, lo1, la2, lo2);
    }

    /**
     * Computes the distance between two points on earth Input
     * latitudes/longitudes are in Radians
     * 
     * @param lat1,
     *            Latitude of first point
     * @param long1,
     *            Longitude of first point
     * @param lat2,
     *            Latitude of second point
     * @param long2,
     *            Longitude of second point
     * @return Distance between both points
     */
    public static double getDistance(double lat1, double long1, double lat2, double long2) {
	double dLat = lat2 - lat1;
	double dLon = long2 - long1;
	double sinLat = (double) Math.sin(dLat / 2);
	double sinLon = (double) Math.sin(dLon / 2);

	double a = (double) (sinLat * sinLat + sinLon * sinLon * Math.cos(lat1) * Math.cos(lat2));
	double c = (double) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
	return R * c;
    }

    // public static Point getMidPoint(Point p, Point q){
    // return getMidPoint("mid_" + p.label + "_" + q.label, p, q);
    // }
    //
    // public static Point getMidPoint(String label, Point p, Point q){
    // List<Float> midCoordinates = new ArrayList<Float>();
    // for(int i=0 ; i<p.coordinates.size() ; i++){
    // midCoordinates.set(i, (p.coordinates.get(i) + q.coordinates.get(i)) / 2);
    // }
    // return new Point(label + q.label, midCoordinates);
    // }

    public static void main(String args[]) {
	System.out.println(getDistanceInDegrees(16.9275f, 81.6736f, 16.92f, 81.67f));
    }
}
