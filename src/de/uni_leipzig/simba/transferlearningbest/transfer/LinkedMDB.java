package de.uni_leipzig.simba.transferlearningbest.transfer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;

public class LinkedMDB {
	
	
 public void getLinkedMDB() throws IOException, InterruptedException
 {
	 Cache cache = new HybridCache();
     String query;
     int offset = 0;
     int limit=1000;
     while(true)
     {
         query = "SELECT * where {?s ?p ?o} OFFSET "+offset+" LIMIT "+limit+"";
         System.out.println(query);

         Query sparqlQuery = QueryFactory.create(query);

         QueryExecution qexec;
         //System.out.println(endpoint);
         qexec = QueryExecutionFactory.sparqlService("http://data.linkedmdb.org/sparql", sparqlQuery);
        // if(query.contains("http://dbpedia.org/ontology/Country"))

         ResultSet results = qexec.execSelect();
         if(!results.hasNext())
        	 break;
         String x, a, b;
         QuerySolution soln;
         File fout = new File("linkedmdb"+offset+".nt");
     	FileOutputStream fos = new FileOutputStream(fout);
      
     	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

         while (results.hasNext()) {
             soln = results.nextSolution();
             bw.write("|"+soln.get("s").toString()+"|"+soln.get("p").toString()+"|"+soln.get("o").toString()+"|");
     		 bw.newLine();
             }
         bw.close();
         Thread.sleep(5000);
         offset=limit;
         limit+=1000;
     }


 }
	
 public static void main(String args[])
 {
	 LinkedMDB x = new LinkedMDB();
	 try {
		x.getLinkedMDB();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }

}
