/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearning.util;

import com.hp.hpl.jena.query.*;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.simba.transferlearning.transfer.Eval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ngonga
 */
public class SparqlUtils {

    Logger logger = LoggerFactory.getLogger(SparqlUtils.class);

    /**
     * For specific class the method retrieves the properties that achieve min. coverage percentage in number of instances 
     * belong to such class
     * It retrieves specific number of instances that belong to a given class then retrieves all used properties by the instances
     * and for each property it is counted the number of times it is used by the set of instances. The properties records number
     * of existence above the min coverage value is chosen for the final result
     * 
     * @param endpoint
     * @param className
     * @param instanceSampleSize number of instances to retrieve
     * @param minCoverage the min number of a property usage
     * @return
     */
    public static Set<String> getRelevantProperties(String endpoint, String className, int instanceSampleSize, double minCoverage) {
        try {


            Set<String> result = new HashSet<String>();
            Set<String> instances = new HashSet<String>();

            //get sample of instances, all instances of this type class
            String query = "SELECT DISTINCT ?a "
                    + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + className + ">. "
                    + "} LIMIT " + instanceSampleSize;
            Query sparqlQuery = QueryFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
            ResultSet results = qexec.execSelect();
            QuerySolution soln;
            while (results.hasNext()) {
                soln = results.nextSolution();
                instances.add(soln.get("a").toString());
            }
            
            String property;
            Map<String, Integer> count = new HashMap<String, Integer>();//property <-> number of instances possess the property
            for (String instance : instances) {
                //for each instance, get property values it possesses
                query = "SELECT DISTINCT ?p WHERE { <" + instance + "> ?p ?x. }";
                sparqlQuery = QueryFactory.create(query);
                qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
                results = qexec.execSelect();
                while (results.hasNext()) {
                    soln = results.nextSolution();
                    property = soln.get("p").toString();
                    if (!count.containsKey(property)) {
                        count.put(property, 1);
                    } else {
                        count.put(property, count.get(property) + 1);
                    }
                }
            }

            // now only take those above the coverage threshold
            // coverage is a fraction
            //use size just in case we do not have enough instances
            if (minCoverage < 1) {
                minCoverage = minCoverage * instances.size();
            }

            for (String p : count.keySet()) {
                if (count.get(p) >= minCoverage) {
                    result.add(p);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Class = " + className);
        }
        return new HashSet<String>();
    }
    /**
     * retrieves all properties used by instances from specific class
     * @param endpoint
     * @param className
     * @return string set represents properties
     */
    public static Set<String> getAllProperties(String endpoint, String className) {
        Set<String> result = new HashSet<String>();
        String query = "SELECT DISTINCT ?p "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + className + ">. "
                + "?a ?p ?x .}";
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            result.add(soln.get("p").toString());
        }
        return result;
    }

    /**
     * The endpoint is alive if we can get a single triple from it.
     * This method checkk if an endpoint is alive by sending sparql query to rterieve one triple from the endpoint
     * @param endpoint The endpopint to test
     * @param graph The targeted graph inside the endpoint
     * @return
     */
    public static boolean isAlive(String endpoint, String graph) {
        try {
            String query = "SELECT * {?s ?p ?o } LIMIT 1";
            Query sparqlQuery = QueryFactory.create(query);

            QueryExecution qexec;
            if (graph != null) {
                List<String> defaultGraphs = new LinkedList<String>();

                defaultGraphs.add(graph);
                qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery, defaultGraphs, null);
            } else {
                qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
            }
            ResultSet results = qexec.execSelect();
            results.hasNext();
        } catch (Exception e) {
            LoggerFactory.getLogger(SparqlUtils.class).debug(e.getMessage());
            return false;
        }
        return true;
    }
}
