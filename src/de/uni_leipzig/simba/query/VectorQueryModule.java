/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.query;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.io.KBInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * This class reads vectors contained in a text file so as to enable LIMES to
 * compute the similarity of the entities described by these vectors efficiently.
 * The file format is assumed to be URI\tVector. The vector itself is read by the
 * metrics of the metric factory for maximum flexibility.
 * @author ngonga
 */
public class VectorQueryModule implements QueryModule {

    KBInfo kb;
    String SEP = "\t";

    /**
     * Constructor
     * @param kbinfo Contains the knowledge base infos necessary to read the
     * input stream
     */
    public VectorQueryModule(KBInfo kbinfo) {
        kb = kbinfo;
    }

    /**
     * Fills the cache c with the data contained in the data source described by
     * kb
     * @param c The cache to be filled
     */
    public void fillCache(Cache c) {
        Logger logger = Logger.getLogger("LIMES");
        try {
            
            // in case a CSV is used, endpoint is the file to read
            BufferedReader reader = new BufferedReader(new FileReader(kb.endpoint));
            logger.info("Reading vectors from "+kb.endpoint);
            String s = reader.readLine();
            String uri;
            
            //read properties. Vectors are assumed to have only one property,
            //which is that used by the user to in the description of the similarity
            //to be used. In general, we assume that vectors can only be compared
            //with other vectors.

            ArrayList<String> properties = new ArrayList<String>();
            properties.add(kb.properties.get(0));
            while (s != null) {
                if (s.contains(SEP)) {
                    uri = s.substring(1, s.indexOf(SEP)-1);
                    c.addTriple(uri, properties.get(0), s.substring(s.indexOf(SEP)));                    
                }
                s = reader.readLine();
            }
            reader.close();
            logger.info("Retrieved " + c.size() + " statements");
        } catch (Exception e) {
            logger.fatal("Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
