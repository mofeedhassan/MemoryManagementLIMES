/**
 * 
 */
package de.uni_leipzig.simba.measures.pointsets.frechet;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.data.Point;
import de.uni_leipzig.simba.mapper.atomic.OrchidMapper;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.GreatEllipticDistance;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.OrthodromicDistance;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import de.uni_leipzig.simba.measures.PolygonMeasure;
import de.uni_leipzig.simba.measures.pointsets.SetMeasure;
import fr.ign.cogit.geoxygene.distance.Frechet;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineSegment;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineSegment;

/**
 * @author sherif
 *
 */
public class GeOxygeneFrechet extends PolygonMeasure implements SetMeasure {


	public double computeDistance(Polygon X, Polygon Y, double threshold){
//		PrintStream originalStream = System.out;
		PrintStream stdout = System.out;
		System.setOut(null);
		double f = Frechet.discreteFrechet(toLineSegment(X), toLineSegment(Y));
//		System.setOut(originalStream);
		System.setOut(stdout);
		return f;
	}
	
	
//	public static ILineSegment toLineSegment(Set<Polygon> polys){
//		DirectPositionList directPosList = new DirectPositionList();
//		for(Polygon poly: polys){
//			for(Point p : poly.points){
//				DirectPosition directPos = new DirectPosition(p.coordinates.get(0), p.coordinates.get(1));
//				directPosList.add(directPos);
//			}
//		}
//		return new GM_LineSegment(directPosList);
//	}

	public static ILineSegment toLineSegment(Polygon poly){
		DirectPositionList directPosList = new DirectPositionList();
		// assume a polygon as a curve starting at point p and end at point q 
		double pLat = poly.points.get(0).coordinates.get(0);
		double pLong = poly.points.get(0).coordinates.get(1);
		double qLat = poly.points.get(poly.points.size()-1).coordinates.get(0);
		double qLong = poly.points.get(poly.points.size()-1).coordinates.get(1);
		GM_LineSegment lineSegment = new GM_LineSegment(new DirectPosition(pLat, pLong),new DirectPosition(qLat, qLong));
		
		for(int i = 1 ; i < (poly.points.size()-1) ; i++){
			double lat = poly.points.get(i).coordinates.get(0);
			double lon = poly.points.get(i).coordinates.get(1);
			lineSegment.addControlPoint(new DirectPosition(lat, lon));
		}
		
		return lineSegment;
	}

	/**
	 * @param X First polygon
	 * @param Y Second polygon
	 * @return Distance between the two polygons
	 */
	public GeOxygeneFrechet() {
		computations = 0;
	}

	public int getComputations() {
		return computations;
	}


	public static double distance(Polygon X, Polygon Y, double threshold) {
		return new GeOxygeneFrechet().computeDistance(X, Y, threshold);
	}

	public String getName() {
		return "GeOxygeneFrechet";
	}

	/**
	 * Computes the SetMeasure distance for a source and target set
	 *
	 * @param source Source polygons
	 * @param target Target polygons
	 * @param threshold Distance threshold
	 * @return Mapping of uris
	 */
	public Mapping run(Set<Polygon> source, Set<Polygon> target, double threshold) {
		Mapping m = new Mapping();
		for (Polygon s : source) {
			for (Polygon t : target) {
				double d = computeDistance(s, t, threshold);
				if (d <= threshold) {
					m.add(s.uri, t.uri, d);
				}
			}
		}
		return m;
	}
	

	public double getSimilarity(Object a, Object b) {
		Polygon p1 = OrchidMapper.getPolygon((String) a);
		Polygon p2 = OrchidMapper.getPolygon((String) b);
		double d = computeDistance(p1, p2, 0);
		return 1d / (1d + (double) d);
	}

	public String getType() {
		return "geodistance";
	}

	public double getSimilarity(Instance a, Instance b, String property1, String property2) {
		TreeSet<String> source = a.getProperty(property1);
		TreeSet<String> target = b.getProperty(property2);
		Set<Polygon> sourcePolygons = new HashSet<Polygon>();
		Set<Polygon> targetPolygons = new HashSet<Polygon>();
		for (String s : source) {
			sourcePolygons.add(OrchidMapper.getPolygon(s));
		}
		for (String t : target) {
			targetPolygons.add(OrchidMapper.getPolygon(t));
		}
		double min = Double.MAX_VALUE;
		double d = 0;
		for (Polygon p1 : sourcePolygons) {
			for (Polygon p2 : targetPolygons) {
				d = computeDistance(p1, p2, 0);
				if (d < min) {
					min = d;
				}
			}
		}
		return 1d / (1d + (double) d);
	}

	public double getRuntimeApproximation(double mappingSize) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static void test() {
		//Malta in DBpedia
		Point maltaDbpediaP1 = new Point("MaltaDbpediaP1", Arrays.asList(new Double[]{14.4625, 35.8967}));
		Point maltaDbpediaP2 = new Point("MaltaDbpediaP2", Arrays.asList(new Double[]{14.4625, 35.8833}));
		Point maltaDbpediaP3 = new Point("MaltaDbpediaP3", Arrays.asList(new Double[]{14.5, 35.8833}));
		Point maltaDbpediaP4 = new Point("MaltaDbpediaP4", Arrays.asList(new Double[]{14.5, 35.8967}));
		Polygon maltaDbpediaPoly1 = new Polygon("maltaDbpediaPoly1", Arrays.asList(new Point[]{maltaDbpediaP1, maltaDbpediaP2, maltaDbpediaP3, maltaDbpediaP4}));
		Set<Polygon> maltaDbpedia = new HashSet<Polygon>();
		maltaDbpedia.add(maltaDbpediaPoly1);

		//Malta in Nuts
		Point maltaNutsP1 = new Point("MaltaNutsP1", Arrays.asList(new Double[]{14.342771550000066, 35.931038250000043}));
		Point maltaNutsP2 = new Point("MaltaNutsP2", Arrays.asList(new Double[]{14.328761050000054, 35.990215250000048}));
		Point maltaNutsP3 = new Point("MaltaNutsP3", Arrays.asList(new Double[]{14.389599050000101, 35.957935750000019}));
		Point maltaNutsP4 = new Point("MaltaNutsP4", Arrays.asList(new Double[]{14.56211105, 35.819926750000036}));
		Point maltaNutsP5 = new Point("MaltaNutsP5", Arrays.asList(new Double[]{14.416516550000068, 35.828308250000049}));
		Point maltaNutsP6 = new Point("maltaNutsP6", Arrays.asList(new Double[]{14.212639050000092, 36.07996375}));
		Point maltaNutsP7 = new Point("maltaNutsP7", Arrays.asList(new Double[]{14.336017550000065, 36.032375750000057}));
		Point maltaNutsP8 = new Point("maltaNutsP8", Arrays.asList(new Double[]{14.218683050000095, 36.021091250000026}));
		Point maltaNutsP9 = new Point("maltaNutsP9", Arrays.asList(new Double[]{14.18619805000003, 36.036388750000029}));
		
//		Polygon maltaNutsPoly1 = new Polygon("maltaNutsPoly1", Arrays.asList(new Point[]{maltaNutsP1, maltaNutsP2, maltaNutsP3, maltaNutsP4, maltaNutsP5}));
//		Polygon maltaNutsPoly2 = new Polygon("maltaNutsPoly2", Arrays.asList(new Point[]{maltaNutsP6, maltaNutsP7, maltaNutsP8, maltaNutsP9}));
//		Set<Polygon> maltaNuts = new HashSet<Polygon>();
//		maltaNuts.add(maltaNutsPoly1);
//		maltaNuts.add(maltaNutsPoly2);
		
		Polygon maltaNutsPoly1 = new Polygon("maltaNutsPoly1", Arrays.asList(new Point[]{maltaNutsP1, maltaNutsP2, maltaNutsP3, maltaNutsP4, maltaNutsP5
				, maltaNutsP6, maltaNutsP7, maltaNutsP8, maltaNutsP9}));
		Set<Polygon> maltaNuts = new HashSet<Polygon>();
		maltaNuts.add(maltaNutsPoly1);

		//Malta in LGD
		Point maltaLgdP1 = new Point("maltaLgdP1", Arrays.asList(new Double[]{14.504285, 35.8953019}));
		Polygon maltaLgdPoly1 = new Polygon("maltaLgdPoly1", Arrays.asList(new Point[]{maltaLgdP1}));
		Set<Polygon> maltaLgd = new HashSet<Polygon>();
		maltaLgd.add(maltaLgdPoly1);

		NaiveFrechet naiveFrechet = new NaiveFrechet();
		System.out.println("Normal: " + naiveFrechet.run(maltaNuts, maltaDbpedia, Double.MAX_VALUE));
		
		GeOxygeneFrechet geoxygenFrechet = new GeOxygeneFrechet();
		System.out.println("GeOxyGen:" + geoxygenFrechet.run(maltaNuts, maltaDbpedia, Double.MAX_VALUE));
	}



	public static void main(String args[]) {
		test();
	}

}
