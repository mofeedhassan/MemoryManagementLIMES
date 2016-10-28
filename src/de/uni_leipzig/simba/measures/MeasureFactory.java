/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.measures;

import de.uni_leipzig.simba.mapper.AtomicMapper;
import de.uni_leipzig.simba.mapper.atomic.*;
import de.uni_leipzig.simba.mapper.atomic.event.ConcurrentMapper;
import de.uni_leipzig.simba.mapper.atomic.event.PredecessorMapper;
import de.uni_leipzig.simba.mapper.atomic.event.SuccessorMapper;
import de.uni_leipzig.simba.mapper.atomic.fastngram.FastNGram;
import de.uni_leipzig.simba.measures.date.DayMeasure;
import de.uni_leipzig.simba.measures.date.SimpleDateMeasure;
import de.uni_leipzig.simba.measures.date.YearMeasure;
import de.uni_leipzig.simba.measures.pointsets.average.NaiveAverage;
import de.uni_leipzig.simba.measures.pointsets.frechet.NaiveFrechet;
import de.uni_leipzig.simba.measures.pointsets.hausdorff.NaiveHausdorff;
import de.uni_leipzig.simba.measures.pointsets.hausdorff.SymmetricHausdorff;
import de.uni_leipzig.simba.measures.pointsets.link.NaiveLink;
import de.uni_leipzig.simba.measures.pointsets.max.NaiveMax;
import de.uni_leipzig.simba.measures.pointsets.mean.NaiveMean;
import de.uni_leipzig.simba.measures.pointsets.min.NaiveMin;
import de.uni_leipzig.simba.measures.pointsets.sumofmin.NaiveSumOfMin;
import de.uni_leipzig.simba.measures.pointsets.surjection.FairSurjection;
import de.uni_leipzig.simba.measures.pointsets.surjection.NaiveSurjection;
import de.uni_leipzig.simba.measures.string.QGramSimilarity;
import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.measures.space.GeoDistance;
import de.uni_leipzig.simba.measures.string.*;
import org.apache.log4j.Logger;

/**
 *
 * @author ngonga
 */
public class MeasureFactory {

    static Logger logger = Logger.getLogger("LIMES");

    public static Measure getMeasure(String name) {
        Measure m;
        if (name.toLowerCase().startsWith("cosine")) {
            m = new CosineMeasure();
        } else if (name.toLowerCase().startsWith("jaccard")) {
            m = new JaccardMeasure();
        } else if (name.toLowerCase().startsWith("jarowinkler")) {
            m = new JaroWinkler();
        } else if (name.toLowerCase().startsWith("jaro")) {
            m = new Jaro();
        } else if (name.toLowerCase().startsWith("ratcliff")) {
            m = new RatcliffObershelpMeasure();
        } else if (name.toLowerCase().startsWith("euclidean")) {
            m = new EuclideanMetric();
        } else if (name.toLowerCase().startsWith("levens")) {
            m = new Levenshtein();
        } else if (name.toLowerCase().startsWith("qgrams")) {
            m = new QGramSimilarity();
        } else if (name.toLowerCase().startsWith("exactmatch")) {
            m = new ExactMatch();
        } else if (name.toLowerCase().startsWith("hausdorff")) {
            m = new NaiveHausdorff();
        } else if (name.toLowerCase().startsWith("orthodromic")) {
            //change this by implementing measure interface in orthodromicdistance class
            m = new GeoDistance();
        } else if (name.toLowerCase().startsWith("symmetrichausdorff")) {
            m = new SymmetricHausdorff();
        } else if (name.toLowerCase().startsWith("datesim")) {
            m = new SimpleDateMeasure();
        } else if (name.toLowerCase().startsWith("daysim")) {
            m = new DayMeasure();
        } else if (name.toLowerCase().startsWith("yearsim")) {
            m = new YearMeasure();
        } else if (name.toLowerCase().startsWith("geomn")) {
            m = new NaiveMin();
        } else if (name.toLowerCase().startsWith("geomx")) {
            m = new NaiveMax();
        } else if (name.toLowerCase().startsWith("geoavg")) {
            m = new NaiveAverage();
        } else if (name.toLowerCase().startsWith("geomean")) {
            m = new NaiveMean();
        } else if (name.toLowerCase().startsWith("frechet")) {
            m = new NaiveFrechet();
        } else if (name.toLowerCase().startsWith("geolink")) {
            m = new NaiveLink();
        } else if (name.toLowerCase().startsWith("geosummn")) {
            m = new NaiveSumOfMin();
        } else if (name.toLowerCase().startsWith("surjection")) {
            m = new NaiveSurjection();
        } else if (name.toLowerCase().startsWith("fairsurjection")) {
            m = new FairSurjection();
        } else {
            m = new TrigramMeasure();
        }

//        System.out.println("Got measure "+m.getName()+" for name <"+name+">");
        return m;
    }

    /**
     * Returns measures of a particular type. If measure with name "name" and
     * type "type" is not found, the default measure for the given type is
     * returned, e.g., trigram similarity for strings. To get the defaukt
     * measure of a given type, simply use getMeasure("", type).
     *
     * @param name Name of the measure
     * @param type Type of the measure
     * @return Similarity measure of the given type
     */
    public static Measure getMeasure(String name, String type) {
        if (type.equals("string")) {
            if (name.toLowerCase().startsWith("cosine")) {
                return new CosineMeasure();
            } else if (name.toLowerCase().startsWith("jaccard")) {
                return new JaccardMeasure();
            }
            //default
            return new TrigramMeasure();

        } else if (type.equals("spatial")) {
            if (name.toLowerCase().startsWith("geo")) {
                return new GeoDistance();
            } else if (name.toLowerCase().startsWith("euclidean")) {
                return new EuclideanMetric();
            }
            //default
            return new EuclideanMetric();
        } else if (type.equals("date")) {
            if (name.toLowerCase().startsWith("datesim")) {
                return new SimpleDateMeasure();
            } else if (name.toLowerCase().startsWith("daysim")) {
                return new DayMeasure();
            } else if (name.toLowerCase().startsWith("yearsim")) {
                return new YearMeasure();
            }
            //default
            return new SimpleDateMeasure();
        }
        //default of all
        return new TrigramMeasure();
    }

    /**
     * Get mapper to measure
     *
     * @param measure
     * @return
     */
    public static AtomicMapper getMapper(String measure) {
        AtomicMapper am;
        if (measure.toLowerCase().startsWith("leven")) {
            am = new EDJoin();
        } else if (measure.toLowerCase().startsWith("qgrams")) {
            am = new FastNGram();
        } else if (measure.toLowerCase().startsWith("jarowinkler")) {
            am =  new JaroWinklerMapper();
        } else if (measure.toLowerCase().startsWith("jaro")) {
            am = new JaroMapper();
        } else if (measure.toLowerCase().startsWith("trigrams")) {
            am = new PPJoinPlusPlus();
        } else if (measure.toLowerCase().startsWith("soundex")) {
            am = new SoundexMapper();
        } else if (measure.toLowerCase().startsWith("ratcliff")) {
            am = new RatcliffObershelpMapper();
        } else if (measure.toLowerCase().startsWith("monge")) {
            am = new MongeElkanMapper();
        } else if (measure.toLowerCase().startsWith("exactmatch")) {
            am = new ExactMatchMapper();
        } else if (measure.toLowerCase().startsWith("euclid")) {
            am = new TotalOrderBlockingMapper();
        } else if (measure.toLowerCase().startsWith("jaccard")) {
            am = new PPJoinPlusPlus();
        } else if (measure.toLowerCase().startsWith("hausdorff")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("orthodromic")) {
            //the hausdorff distance is the same as the orthodromic distance for single points 
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("symmetrichausdorff")) {
            am = new SymmetricHausdorffMapper();
        } else if (measure.toLowerCase().startsWith("datesim")) {
            am = new PPJoinPlusPlus();
        } else if (measure.toLowerCase().startsWith("daysim")) {
            am = new PPJoinPlusPlus();
        } else if (measure.toLowerCase().startsWith("yearsim")) {
            am = new PPJoinPlusPlus();
        } else if (measure.toLowerCase().startsWith("geomin")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("geomax")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("geosumofmin")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("frechet")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("link")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("surjection")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("fairsurjection")) {
            am = new OrchidMapper();
        } else if (measure.toLowerCase().startsWith("successor")) {
            am = new SuccessorMapper();
        } else if (measure.toLowerCase().startsWith("predecessor")) {
            am = new PredecessorMapper();
        } else if (measure.toLowerCase().startsWith("concurrent")) {
            am = new ConcurrentMapper();
        } 
        //        logger.warn("Could not find mapper for " + measure + ". Using default mapper.");
        else {
            am = new PPJoinPlusPlus();
        }
//        System.out.println("Got mapper with name <"+am.getName()+"> for expression <"+measure+">");
        return am;
    }
}
