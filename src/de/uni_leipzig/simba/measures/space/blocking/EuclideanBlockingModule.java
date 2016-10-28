/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_leipzig.simba.measures.space.blocking;

import de.uni_leipzig.simba.cache.MemoryCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.measures.space.SpaceMeasure;
import de.uni_leipzig.simba.measures.space.SpaceMeasureFactory;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author ngonga
 */
public class EuclideanBlockingModule implements BlockingModule {
    int dim = 2;
    ArrayList<Double> thresholds;
    ArrayList<String> properties;
    SpaceMeasure measure;
    Instance zero;

    /** Initializes the generator. The basic idea here is the following: First, pick
     * a random instance origin. That is the center upon which the block ids will be computed.
     * Each measure can return the threshold for blocking that is equivalent to the similarity
     * threshold given in by the user. For euclidean metrics, this value is the same. Yet,
     * for metrics that squeeze space, this might not be the case. It is important to notice that
     * the generation assumes that the size of props.split("|") is the same as dimensions.
     * @param origin The random instance used as reference for computing block ids
     * @param props List of properties that make up each dimension
     * @param measureName Name of the measure to be used to compute the similarity of instances
     * @param dimensions Number of dimensions to be considered
     * @param threshold General similarity threshold for the metric. This threshold is transformed
     * into a distance threshold, as sim = a -> d = (1 - a)/a. The space tiling is carried out
     * according to distances, not similarities. Still, we can ensure that all points within the
     * similarity range are found.
     */    

    public EuclideanBlockingModule(String props, String measureName, double threshold)
    {
        thresholds = new ArrayList<Double>();
        properties = new ArrayList<String>();
        String[] split = props.split("\\|");
        dim = split.length;
        measure = SpaceMeasureFactory.getMeasure(measureName, dim);
        for(int i=0; i<dim; i++)
        {
            thresholds.add(measure.getThreshold(i, threshold));
            properties.add(split[i]);
        }
    }
    /** Computes the block ID for a given instance a. The idea behind the blocking
     * is to tile the target space into blocks of dimension thresdhold^dimensions.
     * Each instance s from the source space is then compared with the blocks lying
     * directly around s's block and the block where s is.
     * @param a The instance whose blockId is to be computed
     * @return The ID for the block of a
     */
    public ArrayList<Integer> getBlockId(Instance a)
    {
        int blockId;
        ArrayList<Integer> blockIds = new ArrayList<Integer>();
        double value;
        for(int i=0; i<dim; i++)
        {
            value = Double.parseDouble(a.getProperty(properties.get(i)).first());
            blockId = (int)java.lang.Math.floor(value/thresholds.get(i));
            blockIds.add(blockId);
        }
        return blockIds;
    }

    /** Returns the ids of all the blocks surrounding a given block for comparison
     * Will be extremely useful for parallelizing as we can use blocking on T and S
     * as then put use locality
     * @param blockId
     * @return
     */
    public ArrayList<ArrayList<Integer>> getBlocksToCompare(ArrayList<Integer> blockId)
    {
        int dim = blockId.size();
        if(dim == 0) return new ArrayList<ArrayList<Integer>>();

        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> buffer = new ArrayList<ArrayList<Integer>>();
        result.add(blockId);

        ArrayList<Integer> minus = new ArrayList<Integer>();
        ArrayList<Integer> plus = new ArrayList<Integer>();
        ArrayList<Integer> copy = new ArrayList<Integer>();
//        for(int i=0; i<dim; i++)
//        {
//            minus.add(0);
//            copy.add(0);
//            plus.add(0);
//        }
        ArrayList<Integer> id;

        for(int i = 0; i < dim; i++)
        {
            for(int j = 0; j < Math.pow(3, i); j++)
            {
                //System.out.println("Result"+result);
                id = result.get(j);
                //System.out.println(j+" -> "+id);
                minus = new ArrayList<Integer>();
                plus = new ArrayList<Integer>();
                for(int k = 0; k < dim; k++)
                {                                        
                    if(k!=i)
                    {
                        minus.add(id.get(k));
                        plus.add(id.get(k));
                    }
                    else
                    {
                        minus.add(id.get(k)-1);
                        plus.add(id.get(k)+1);
                    }
                }
                //System.out.println("Adding to "+result);
                result.add(minus);
                //System.out.println(result);
                result.add(plus);
                //System.out.println(result);
            }                       
        }
        return result;
    }

public ArrayList<ArrayList<Integer>> getAllBlockIds(Instance a) {
        int blockId;
        TreeSet<String> propValues;
        ArrayList<ArrayList<Integer>> blockIds = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Double>> combinations = new ArrayList<ArrayList<Double>>();
        double value;
        //get all property combinations
        for (int i = 0; i < dim; i++) {
            combinations = addIdsToList(combinations, a.getProperty(properties.get(i)));
        }
        for(int i=0; i<combinations.size(); i++)
        {
            ArrayList<Double> combination = combinations.get(i);
            ArrayList<Integer> block = new ArrayList<Integer>();
            for(int j=0; j<combination.size();j++)
            {
                blockId = (int) java.lang.Math.floor((combination.get(j)) / thresholds.get(j));
                block.add(blockId);
            }
            blockIds.add(block);
        }
        return blockIds;
    }

public ArrayList<ArrayList<Integer>> getAllSourceIds(Instance a, String sourceProps) {
    String[] props = sourceProps.split("\\|");
        int blockId;
        TreeSet<String> propValues;
        ArrayList<ArrayList<Integer>> blockIds = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Double>> combinations = new ArrayList<ArrayList<Double>>();
        double value;
        //get all property combinations
        for (int i = 0; i < dim; i++) {
            combinations = addIdsToList(combinations, a.getProperty(props[i]));
        }
        for(int i=0; i<combinations.size(); i++)
        {
            ArrayList<Double> combination = combinations.get(i);
            ArrayList<Integer> block = new ArrayList<Integer>();
            for(int j=0; j<combination.size();j++)
            {
                blockId = (int) java.lang.Math.floor((combination.get(j)) / thresholds.get(j));
                block.add(blockId);
            }
            blockIds.add(block);
        }
        return blockIds;
    }

    public static ArrayList<Double> copyList(ArrayList<Double> list) {
        ArrayList<Double> copy = new ArrayList<Double>();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                copy.add(list.get(i));
            }
        }
        return copy;
    }

    public static ArrayList<ArrayList<Double>> addIdsToList(ArrayList<ArrayList<Double>> keys,
            TreeSet<String> propValues) {
        ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> values = new ArrayList<Double>();
        for (String s : propValues) {
//        	System.out.println("----------------------------------------------------------------------S: "+s);
            values.add(Double.parseDouble(s));
        }
        if (keys.size() == 0) {
            for (int j = 0; j < values.size(); j++) {
                ArrayList<Double> list = new ArrayList<Double>();
                list.add(values.get(j));
                result.add(list);
            }
        } else {
            ArrayList<Double> copy;
            for (int i = 0; i < keys.size(); i++) {
                for (int j = 0; j < values.size(); j++) {
                    copy = copyList(keys.get(i));
                    copy.add(values.get(j));
                    result.add(copy);
                }
            }
        }
        return result;
    }

    public static void main(String args[])
    {
        MemoryCache cache = new MemoryCache();
        cache.addTriple("A", "lon", "1");
        cache.addTriple("A", "lat", "1");
        cache.addTriple("B", "lon", "2");
        cache.addTriple("B", "lat", "1");

        EuclideanBlockingModule blocker = new EuclideanBlockingModule("lon|lat", "euclidean", 0.25);

        System.out.println(blocker.getBlockId(cache.getInstance("A")));
        System.out.println(blocker.getBlockId(cache.getInstance("B")));
        
        ArrayList<Integer> blockId = new ArrayList<Integer>();
        ArrayList<Integer> blockId2 = new ArrayList<Integer>();
        blockId.add(0);
        blockId.add(0);
        //blockId.add(0);
        System.out.println((new EuclideanBlockingModule("", "", 0.5)).getBlocksToCompare(blockId));
        System.out.println(blockId.equals(blockId2));
    }
}
