package de.uni_leipzig.simba.measures.pointsets.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.Polygon;
import de.uni_leipzig.simba.mapper.atomic.hausdorff.PolygonReader;
import de.uni_leipzig.simba.measures.PolygonMeasure;
import de.uni_leipzig.simba.measures.pointsets.SetMeasure;
import de.uni_leipzig.simba.measures.pointsets.average.NaiveAverage;
import de.uni_leipzig.simba.measures.pointsets.frechet.GeOxygeneFrechet;
import de.uni_leipzig.simba.measures.pointsets.frechet.NaiveFrechet;
import de.uni_leipzig.simba.measures.pointsets.hausdorff.NaiveHausdorff;
import de.uni_leipzig.simba.measures.pointsets.link.NaiveLink;
import de.uni_leipzig.simba.measures.pointsets.max.NaiveMax;
import de.uni_leipzig.simba.measures.pointsets.mean.NaiveMean;
import de.uni_leipzig.simba.measures.pointsets.min.NaiveMin;
import de.uni_leipzig.simba.measures.pointsets.sumofmin.NaiveSumOfMin;
import de.uni_leipzig.simba.measures.pointsets.surjection.FairSurjection;
import de.uni_leipzig.simba.measures.pointsets.surjection.NaiveSurjection;

public class CompareOrthodomic2Elliptic {
	private static final Logger logger = 	Logger.getLogger(CompareOrthodomic2Elliptic.class.getName());
	public static String resultStr = "Measure\tP_Ortho\tR_Ortho\tF_Ortho\tT_Ortho\tP_Elliptic\tR_Elliptic\tF_Elliptic\tT_Elliptic\n";

	static List<SetMeasure> geoDistances =  new ArrayList<SetMeasure>(Arrays.asList(
			new NaiveMin(),
			new NaiveMax(),
			new NaiveAverage(),
			new NaiveSumOfMin(),
			new NaiveLink(),
			new NaiveSurjection(),
			new FairSurjection(),
			new NaiveHausdorff(),
			new NaiveMean(),
			//				new NaiveFrechet()
			new GeOxygeneFrechet()
			//	new IndexedHausdorff(),
			//	new FastHausdorff(),
			//	new CentroidIndexedHausdorff()
			));


	public static void compareOrthodomic2Elliptic(String inputFile, int chunkSize){
		logger.info("Read " + chunkSize + " polygons from " + inputFile);
		Set<Polygon> sourcePolygonSet 	= PolygonReader.readPolygons(inputFile, chunkSize);

		for(SetMeasure geoDistance : geoDistances){
			logger.info("Compute " + geoDistance.getName()  +" distances for chunk of size: " + chunkSize + " using Orthodromic distance");
			PolygonMeasure.USE_GREAT_ELLIPTIC_DISTANCE = false;
			long t = System.currentTimeMillis();
			Mapping m = geoDistance.run(sourcePolygonSet, sourcePolygonSet, Float.MAX_VALUE);
			m = getBestMappings(m);
			long time = System.currentTimeMillis() - t;
			FMeasureRecorder fm = new FMeasureRecorder(m, chunkSize);
			resultStr += geoDistance.getName() 	+ "\t" +
					fm.precision 				+ "\t" +
					fm.recall 					+ "\t" + 
					fm.fMeasure 				+ "\t" + 
					time 						+ "\t" ;

			logger.info("Compute " + geoDistance.getName()  +" distances for chunk of size: " + chunkSize + " using Elliptic distance");
			PolygonMeasure.USE_GREAT_ELLIPTIC_DISTANCE = true;
			t = System.currentTimeMillis();
			m = geoDistance.run(sourcePolygonSet, sourcePolygonSet, Float.MAX_VALUE);
			m = getBestMappings(m);
			time = System.currentTimeMillis() - t;
			fm = new FMeasureRecorder(m, chunkSize);
			resultStr += 
					fm.precision 				+ "\t" +
							fm.recall 					+ "\t" + 
							fm.fMeasure 				+ "\t" + 
							time 						+ "\n" ;
			System.out.println("---------------- RESULTS ----------------");
			System.out.println(resultStr);
		}
	}

	/**
	 * @param map
	 * @return
	 * @author sherif
	 */
	private static Mapping getBestMappings(Mapping mapping) {
		Mapping result = new Mapping();
		for (String s : mapping.map.keySet()) {
			double minSim = Double.MAX_VALUE;
			Set<String> target = new HashSet<String>();;
			for (String t : mapping.map.get(s).keySet()) {
				double sim = mapping.getSimilarity(s, t);
				if (sim == minSim) {
					target.add(t);
				}
				if (sim < minSim) {
					minSim = sim;
					target = new HashSet<String>();
					target.add(t);
				}
			}
			for (String t : target) {
				result.add(s, t, minSim);
			}
		}
		return result;
	}


	public static void main(String[] args){
		compareOrthodomic2Elliptic(args[0], Integer.parseInt(args[1]));
	}


}
