package de.uni_leipzig.simba.measures;

import de.uni_leipzig.simba.data.Point;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GreatEllipticDistance;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.OrthodromicDistance;

public abstract class PolygonMeasure{
	
	public int computations;
	public static boolean USE_GREAT_ELLIPTIC_DISTANCE = false;
	
	/**
	 * @param x Point x
	 * @param y Point y
	 * @param useEllipticDistance
	 * @return Distance between x and y
	 * @return
	 */
	public double distance(Point x, Point y, boolean useEllipticDistance) {
		computations++;
		if(useEllipticDistance){
			return GreatEllipticDistance.getDistanceInDegrees(x,y);
		}
		return OrthodromicDistance.getDistanceInDegrees(x, y);
	}

}
