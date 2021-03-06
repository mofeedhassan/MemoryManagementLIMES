/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearning.transfer.classes;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.transferlearning.transfer.config.Configuration;
import de.uni_leipzig.simba.transferlearning.util.Execution;

/**
 *
 * @author ngonga
 */
public class LabelBasedClassSimilarity implements ClassSimilarity{
    
    public int SAMPLING_RATE = 100;
    public double THRESHOLD = 0.25;
    
    /**
     * get the object values of label property for the given resource c from endpoint
     * @param c
     * @param endpoint
     * @return
     */
    private static Cache getLabels(String c, String endpoint) {
        Cache cache = new HybridCache();
        String query = "SELECT ?l "
                + "WHERE { <"+c+"> <http://www.w3.org/2000/01/rdf-schema#label> ?l. }";
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            if (soln.get("l").isLiteral()) {
                cache.addTriple(c, "p", ((Literal) soln.get("l")).getLexicalForm());
            }
        }
        return cache; //triples as ?c "p" ?l
    }

    /**
     * get max achieved similarity value over all similar labels between source and target instances
     */
    @Override
    public double getSimilarity(String class1, String class2, Configuration config) {
        Cache source = getLabels(class1, config.source.endpoint);
        Cache target = getLabels(class2, config.target.endpoint);
        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
        System.out.println(source+"\n"+target+"\n"+m);
        double max = 0.0, sim = 0.0;
        //we could use max, min instead
        //could also
        for (String s : m.map.keySet()) {
            for (String t : m.map.get(s).keySet()) {
                sim = m.getSimilarity(s, t);
                if(sim > max)
                    max = sim;
            }
        }
        return max;
    }
	@Override
	
	public double getSimilarity(String class1, String class2, Configuration config1, Configuration config2, boolean isSource) {
		Cache source,target;
		if(isSource)
		{
			source = getLabels(class1, config1.source.endpoint);
			target = getLabels(class2, config2.source.endpoint);
		}
		else
		{
			source = getLabels(class1, config1.target.endpoint);
			target = getLabels(class2, config2.target.endpoint);
		}
		
        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
        System.out.println(source+"\n"+target+"\n"+m);
        double max = 0.0, sim = 0.0;
        //we could use max, min instead
        //could also
        for (String s : m.map.keySet()) {
            for (String t : m.map.get(s).keySet()) {
                sim = m.getSimilarity(s, t);
                if(sim > max)
                    max = sim;
            }
        }
        return max;
	}
    
    public static void main(String[] args)
    {
       // System.out.println(getLabels("http://dbpedia.org/resource/Leipzig", "http://live.dbpedia.org/sparql"));
    }


}
