/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.measures.pointsets.benchmarking;

import de.uni_leipzig.simba.data.Point;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class MeasurementErrorModifier extends AbstractPolygonModifier {

	/**
	 * Modifies a polygon by adding a random error between -threshold and
	 * +threshold to its latitude and longitude
	 *
	 * @param p Polygon to modify
	 * @param threshold Error range
	 * @return Modified polygon with the same name
	 */
	public Polygon modify(Polygon p, double threshold) {
		Polygon q = new Polygon(p.uri);
		List<Point> points = new ArrayList<Point>();
		for (Point point : p.points) {
			List<Double> coordinates = new ArrayList<Double>();
			for (Double f : point.coordinates) {
				double v;
				double delta = (double) (Math.random() * threshold);
				if (Math.random() <= 0.5) {
					v = f + delta;
				} else {
					v = f - delta; //deal with lat and long issue				
				}
				if (Math.abs(f) <= 90 && Math.abs(v) > 90) {
					coordinates.add(Math.signum(v) * (180 - Math.abs(v)));
				}
				if (Math.abs(f) <= 180 && Math.abs(v) > 180) {
					coordinates.add(Math.signum(v) * (360 - Math.abs(v)));
				} else {
					coordinates.add(v);
				}
			}
			points.add(new Point(point.label, coordinates));
		}
		q.points = points;
		return q;
	}

	public String getName(){
		return "MeasurementErrorModifier";
	}

	public static void main(String args[]) {
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
		Polygon maltaNutsPoly1 = new Polygon("maltaNutsPoly1", Arrays.asList(new Point[]{maltaNutsP1, maltaNutsP2, maltaNutsP3, maltaNutsP4, maltaNutsP5}));
		
		Point maltaNutsP6 = new Point("maltaNutsP6", Arrays.asList(new Double[]{14.212639050000092, 36.07996375}));
		Point maltaNutsP7 = new Point("maltaNutsP7", Arrays.asList(new Double[]{14.336017550000065, 36.032375750000057}));
		Point maltaNutsP8 = new Point("maltaNutsP8", Arrays.asList(new Double[]{14.218683050000095, 36.021091250000026}));
		Point maltaNutsP9 = new Point("maltaNutsP9", Arrays.asList(new Double[]{14.18619805000003, 36.036388750000029}));
		Polygon maltaNutsPoly2 = new Polygon("maltaNutsPoly2", Arrays.asList(new Point[]{maltaNutsP6, maltaNutsP7, maltaNutsP8, maltaNutsP9}));

		Set<Polygon> maltaNuts = new HashSet<Polygon>();
		maltaNuts.add(maltaNutsPoly1);
		maltaNuts.add(maltaNutsPoly2);

		//Malta in LGD
		Point maltaLgdP1 = new Point("maltaLgdP1", Arrays.asList(new Double[]{14.504285, 35.8953019}));
		Polygon maltaLgdPoly1 = new Polygon("maltaLgdPoly1", Arrays.asList(new Point[]{maltaLgdP1}));
		Set<Polygon> maltaLgd = new HashSet<Polygon>();
		maltaLgd.add(maltaLgdPoly1);

		//Print Modified Malta
		Polygon maltaDbpediapoly1G = new MeasurementErrorModifier().modify(maltaDbpediaPoly1, 2);
		System.out.println("Malta in DBpedia after applying measurement error modifier: " + maltaDbpediapoly1G.points);

		Polygon maltaNutsPoly1G = new MeasurementErrorModifier().modify(maltaNutsPoly1, 2);
		System.out.println("Malta in Nuts after applying measurement error modifier: " + maltaNutsPoly1G.points);
		Polygon maltaNutsPoly2G = new MeasurementErrorModifier().modify(maltaNutsPoly2, 2);
		System.out.println("Malta in Nuts after applying measurement error modifier: " + maltaNutsPoly2G.points);

		Polygon maltaLgdPoly1G = new MeasurementErrorModifier().modify(maltaLgdPoly1, 2);
		System.out.println("Malta in LGD after applying measurement error modifier: " + maltaLgdPoly1G.points);
	}
}