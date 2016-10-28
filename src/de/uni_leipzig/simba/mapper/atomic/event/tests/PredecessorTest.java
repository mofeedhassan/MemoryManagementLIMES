package de.uni_leipzig.simba.mapper.atomic.event.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.filter.LinearFilter;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.mapper.SetConstraintsMapper;
import de.uni_leipzig.simba.mapper.SetConstraintsMapperFactory;

public class PredecessorTest {




    public Cache source = new HybridCache();
    public Cache target = new HybridCache();

    @Before
    public void setUp() {
        source = new HybridCache();
        target = new HybridCache();
        // create source cache
        source.addTriple("S1", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:21:04+02:00");
        source.addTriple("S1", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:22:04+02:00");
        source.addTriple("S1", "http://myOntology#MachineID", "26");
        source.addTriple("S1", "name", "kleanthi");

        source.addTriple("S2", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:21:04+02:00");
        source.addTriple("S2", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:22:04+02:00");
        source.addTriple("S2", "http://myOntology#MachineID", "26");
        source.addTriple("S2", "name", "abce");

        source.addTriple("S3", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:24:04+02:00");
        source.addTriple("S3", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:25:04+02:00");
        source.addTriple("S3", "http://myOntology#MachineID", "26");
        source.addTriple("S3", "name", "pony");

        source.addTriple("S4", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:31:04+02:00");
        source.addTriple("S4", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:32:04+02:00");
        source.addTriple("S4", "http://myOntology#MachineID", "27");
        source.addTriple("S4", "name", "ping");

        source.addTriple("S5", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T09:21:04+02:00");
        source.addTriple("S5", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T09:24:04+02:00");
        source.addTriple("S5", "http://myOntology#MachineID", "27");
        source.addTriple("S5", "name", "kleanthi");

        source.addTriple("S6", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:51:04+02:00");
        source.addTriple("S6", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T09:24:04+02:00");
        source.addTriple("S6", "http://myOntology#MachineID", "27");
        source.addTriple("S6", "name", "blabla");

        source.addTriple("S7", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:41:04+02:00");
        source.addTriple("S7", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:51:04+02:00");
        source.addTriple("S7", "http://myOntology#MachineID", "28");
        source.addTriple("S7", "name", "blabla");

        source.addTriple("S8", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:41:04+02:00");
        source.addTriple("S8", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:43:04+02:00");
        source.addTriple("S8", "http://myOntology#MachineID", "29");
        source.addTriple("S8", "name", "lolelele");

        source.addTriple("S9", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:21:04+02:00");
        source.addTriple("S9", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:34:04+02:00");
        source.addTriple("S9", "http://myOntology#MachineID", "29");
        source.addTriple("S9", "name", "mattttt");

        source.addTriple("S10", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T09:21:04+02:00");
        source.addTriple("S10", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T09:22:04+02:00");
        source.addTriple("S10", "http://myOntology#MachineID", "30");
        source.addTriple("S10", "name", "mattttt");

        source.addTriple("S11", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T09:21:04+02:00");
        source.addTriple("S11", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T09:22:04+02:00");
        source.addTriple("S11", "http://myOntology#MachineID", "30");
        source.addTriple("S11", "name", "mattttt");

        source.addTriple("S12", "http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime", "2015-05-20T08:31:04+02:00");
        source.addTriple("S12", "http://purl.org/NET/c4dm/timeline.owl#endsAtDateTime", "2015-05-20T08:45:04+02:00");
        source.addTriple("S12", "http://myOntology#MachineID", "30");
        source.addTriple("S12", "name", "mattttt");

        target = source;

    }

    @After
    public void tearDown() {
        source = null;
        target = null;
    }

    @Test
    public void simpleLS() {
        System.out.println("simpleLS");
        
      //Mapping mapping = (new PPJoinPlusPlus()).getMapping(sourceInfo, targetInfo, cr.sourceInfo.var, cr.targetInfo.var, cr.metricExpression, cr.verificationThreshold);
        SetConstraintsMapper mapper = SetConstraintsMapperFactory.getMapper("simple",
                new KBInfo(), new KBInfo(),source, target, new LinearFilter(), 0);
        //cr.sourceInfo, cr.targetInfo, sourceInfo, targetInfo, new PPJoinMapper(), new LinearFilter());
        System.out.println("Getting links ...");
        long time = System.currentTimeMillis();
        Mapping mapping = mapper.getLinks("predecessor(x.http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime|http://myOntology#MachineID,y.http://purl.org/NET/c4dm/timeline.owl#beginsAtDateTime|http://myOntology#MachineID)", 1.0);
        System.out.println("Got links in " + (System.currentTimeMillis() - time) + "ms.");
        System.out.println(mapping);
        //get Writer ready
        

    }
   


}
