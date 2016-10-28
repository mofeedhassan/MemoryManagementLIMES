package de.uni_leipzig.simba.allenalgebra.mappers.atomic.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.simba.allenalgebra.mappers.atomic.AtomicAllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.BeginBegin;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.BeginEnd;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.EndBegin;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class EndBeginTest {




    public Cache source;
    public Cache target;
    public ConfigReader cR = new RDFConfigReader();

    @Before
    public void setUp() {

	String baseFolder = "resources/";
	String configFile = "sake.ttl";
	cR.validateAndRead(baseFolder + configFile);

	source = HybridCache.getData(cR.sourceInfo);
	target = HybridCache.getData(cR.targetInfo);

    }

    @After
    public void tearDown() {
	source = null;
	target = null;
    }

    @Test
    public void getProperties() {

	System.out.println("EndBeginTest-getProperties");

	System.out.println(cR.metricExpression);
	String expression = cR.metricExpression;

	AtomicAllenAlgebraMapper mapper = new EndBegin();
	Parser p = new Parser(expression, 0.0d);
	System.out.println(p.getTerm1());

	assertTrue(p.getTerm1().contains("|") == true);

	String propertyBegin = mapper.getEndProperty(p.getTerm1());
	System.out.println(propertyBegin);
	assertTrue(propertyBegin != null);
	assertTrue(propertyBegin.equals("endDate"));


	String propertyEnd = mapper.getBeginProperty(p.getTerm1());
	System.out.println(propertyEnd);
	assertTrue(propertyEnd != null);
	assertTrue(propertyEnd.equals("beginDate"));

	System.out.println("====================================");

    }

    @Test
    public void sourceBlocks() {

	System.out.println("EndBeginTest-sourceBlocks");

	String expression = cR.metricExpression;

	AtomicAllenAlgebraMapper mapper = new BeginBegin();
	TreeMap<String, Set<Instance>> sourceBlocks = mapper.orderByEndDate(source, expression);
	assertTrue(sourceBlocks != null);
	int counter = 0;

	// check size
	for (Entry<String, Set<Instance>> entry : sourceBlocks.entrySet()) {
	    counter += entry.getValue().size();
	}
	assertTrue(counter == 24);

	// check if all instances are included
	ArrayList<String> temp = new ArrayList<String>();
	for (Entry<String, Set<Instance>> entry : sourceBlocks.entrySet()) {
	    for (Instance instance : entry.getValue()) {
		temp.add(instance.getUri());
	    }
	}
	ArrayList<String> temp2 = source.getAllUris();

	Collections.sort(temp);
	Collections.sort(temp2);
	assertTrue(temp.equals(temp2));

	// check if source blocks includes any duplicates
	Set<String> set = new HashSet<String>(temp);
	assertTrue(set.size() == temp.size());
	System.out.println("====================================");

    }

    @Test
    public void targetBlocks() {

	System.out.println("EndBeginTest-targetBlocks");

	String expression = cR.metricExpression;

	AtomicAllenAlgebraMapper mapper = new BeginBegin();
	TreeMap<String, Set<Instance>> targetBlocks = mapper.orderByBeginDate(target, expression);
	assertTrue(targetBlocks != null);
	int counter = 0;

	// check size
	for (Entry<String, Set<Instance>> entry : targetBlocks.entrySet()) {
	    counter += entry.getValue().size();
	}
	assertTrue(counter == 24);

	// check if all instances are included
	ArrayList<String> temp = new ArrayList<String>();
	ArrayList<String> keys = new ArrayList<String>();
	for (Entry<String, Set<Instance>> entry : targetBlocks.entrySet()) {
	    for (Instance instance : entry.getValue()) {
		temp.add(instance.getUri());
	    }
	    keys.add(entry.getKey());

	}
	// check if keys are ordered
	boolean sorted = true;
	for (int i = 1; i < keys.size(); i++) {
	    if (keys.get(i - 1).compareTo(keys.get(i)) > 0)
		sorted = false;
	}
	assertTrue(sorted == true);

	ArrayList<String> temp2 = source.getAllUris();

	Collections.sort(temp);
	Collections.sort(temp2);
	assertTrue(temp.equals(temp2));

	// check if source blocks includes any duplicates
	Set<String> set = new HashSet<String>(temp);
	assertTrue(set.size() == temp.size());

	System.out.println("====================================");

    }
    @Test
    public void compareWithBeginEnd() {

	System.out.println("EndBeginTest-compareWithBeginEnd");

	String expression = cR.metricExpression;

	AtomicAllenAlgebraMapper mapperEB = new EndBegin();
	TreeMap<String, Set<Instance>> sourceBlocksEB = mapperEB.orderByEndDate(source, expression);
	TreeMap<String, Set<Instance>> targetBlocksEB = mapperEB.orderByBeginDate(target, expression);
	
	AtomicAllenAlgebraMapper mapperBE = new BeginEnd();
	TreeMap<String, Set<Instance>> sourceBlocksBE = mapperBE.orderByBeginDate(source, expression);
	TreeMap<String, Set<Instance>> targetBlocksBE = mapperBE.orderByEndDate(target, expression);
	
	
	
	// check if source blocks are equal
	assertTrue(sourceBlocksEB.keySet().equals(targetBlocksBE.keySet()));

	assertTrue(targetBlocksEB.keySet().equals(sourceBlocksBE.keySet()));

	

	System.out.println("====================================");

    }



}
