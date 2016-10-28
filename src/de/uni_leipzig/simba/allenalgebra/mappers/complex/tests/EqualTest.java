package de.uni_leipzig.simba.allenalgebra.mappers.complex.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraFactory;
import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.AtomicAllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.BeginBegin;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.BeginEnd;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.EndBegin;
import de.uni_leipzig.simba.allenalgebra.mappers.atomic.EndEnd;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.rdfconfig.RDFConfigReader;

public class EqualTest {

    public Cache source;
    public Cache target;
    public ConfigReader cR = new RDFConfigReader();
    // BB = 0, BE = 1, EB = 2, EE = 3
    private ArrayList<TreeMap<String, Set<Instance>>> targetBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
    private ArrayList<TreeMap<String, Set<Instance>>> sourceBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
    AllenAlgebraMapper mapper = AllenAlgebraFactory.getMapper("Equal");
    ArrayList<TreeMap<String, Set<String>>> all = new ArrayList<TreeMap<String, Set<String>>>();

    @Before
    public void setUp() {

	String baseFolder = "resources/";
	String configFile = "sake_test.ttl";
	cR.validateAndRead(baseFolder + configFile);

	source = HybridCache.getData(cR.sourceInfo);
	target = HybridCache.getData(cR.targetInfo);
	

	sourceBlocks.add(0, AtomicAllenAlgebraMapper.orderByBeginDate(source, cR.metricExpression));
	sourceBlocks.add(1, AtomicAllenAlgebraMapper.orderByEndDate(source, cR.metricExpression));
	targetBlocks.add(0, AtomicAllenAlgebraMapper.orderByBeginDate(target, cR.metricExpression));
	targetBlocks.add(1, AtomicAllenAlgebraMapper.orderByEndDate(target, cR.metricExpression));

	EndEnd ee = new EndEnd();
	BeginBegin bb = new BeginBegin();
	BeginEnd be = new BeginEnd();
	EndBegin eb = new EndBegin();

	all.add(bb.getConcurrentEvents(source, target, cR.metricExpression));
	all.add(bb.getPredecessorEvents(source, target, cR.metricExpression));
	all.add(be.getConcurrentEvents(source, target, cR.metricExpression));
	all.add(be.getPredecessorEvents(source, target, cR.metricExpression));
	all.add(eb.getConcurrentEvents(source, target, cR.metricExpression));
	all.add(eb.getPredecessorEvents(source, target, cR.metricExpression));
	all.add(ee.getConcurrentEvents(source, target, cR.metricExpression));
	all.add(ee.getPredecessorEvents(source, target, cR.metricExpression));
    }

    @After
    public void tearDown() {
	source = null;
	target = null;
    }

    @Test
    public void requiredAtomics() {
	System.out.println("Equal-requiredAtomics");
	// collect mapper's required lists
	ArrayList<TreeMap<String, Set<Instance>>> mapperSourceBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
	ArrayList<TreeMap<String, Set<Instance>>> mapperTargetBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();

	for (int req : mapper.getRequiredAtomicRelationsSource()) {
	    mapperSourceBlocks.add(sourceBlocks.get(req));
	}
	for (int req : mapper.getRequiredAtomicRelationsTarget()) {
	    mapperTargetBlocks.add(targetBlocks.get(req));
	}

	assertTrue(mapperSourceBlocks.get(0).equals(sourceBlocks.get(0)));
	
	assertTrue(mapperTargetBlocks.get(0).equals(targetBlocks.get(0)));
	assertTrue(mapperTargetBlocks.get(1).equals(targetBlocks.get(1)));

	ArrayList<TreeMap<String, Set<String>>> m = new ArrayList<TreeMap<String, Set<String>>>();
	for (int req : mapper.getRequiredAtomicRelations()) {
	    m.add(all.get(req));
	}
	assertTrue(m.get(0).equals(all.get(0)));
	assertTrue(m.get(1).equals(all.get(6)));
	System.out.println("==================================================");

    }

    @Test
    public void getMapping() {
	System.out.println("Equal-getMapping");
	// collect mapper's required lists
	ArrayList<TreeMap<String, Set<Instance>>> mapperSourceBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();
	ArrayList<TreeMap<String, Set<Instance>>> mapperTargetBlocks = new ArrayList<TreeMap<String, Set<Instance>>>();

	for (int req : mapper.getRequiredAtomicRelationsSource()) {
	    mapperSourceBlocks.add(sourceBlocks.get(req));
	}
	for (int req : mapper.getRequiredAtomicRelationsTarget()) {
	    mapperTargetBlocks.add(targetBlocks.get(req));
	}
	mapper.setFilePath("blabla.txt");

	mapper.getMappingMemoryEfficiency(mapperSourceBlocks, mapperTargetBlocks);

	TreeMap<String, Set<String>> links1 = new TreeMap<String, Set<String>>(mapper.links);
	/////////////////////////////////////////////////////////////////////////////////////////////
	// BB0 & EE0
	mapper.links = new TreeMap<String, Set<String>>();

	ArrayList<TreeMap<String, Set<String>>> m = new ArrayList<TreeMap<String, Set<String>>>();
	for (int req : mapper.getRequiredAtomicRelations()) {
	    m.add(all.get(req));
	}
	mapper.getMappingTimeEfficiency(m);

	TreeMap<String, Set<String>> links2 = new TreeMap<String, Set<String>>(mapper.links);

	for (Entry<String, Set<String>> entry1 : links1.entrySet()) {
	    System.out.println("--->" + entry1.getKey());
	    System.out.println(entry1.getValue());
	    System.out.println("===========");

	}
	System.out.println("-----------------------------");
	for (Entry<String, Set<String>> entry1 : links2.entrySet()) {
	    System.out.println("--->" + entry1.getKey());
	    System.out.println(entry1.getValue());
	    System.out.println("===========");

	}

	assertTrue(links1.equals(links2));
	System.out.println("==================================================");

    }

}
