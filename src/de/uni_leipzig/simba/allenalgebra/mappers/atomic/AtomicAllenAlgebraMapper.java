package de.uni_leipzig.simba.allenalgebra.mappers.atomic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.allenalgebra.mappers.AllenAlgebraMapper;
import de.uni_leipzig.simba.allenalgebra.mappers.IAllenAlgebraMapper;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;

public abstract class AtomicAllenAlgebraMapper {
    protected static final Logger logger = Logger.getLogger(AtomicAllenAlgebraMapper.class.getName());

    public abstract TreeMap<String, Set<String>> getConcurrentEvents(Cache source, Cache target, String expression);

    public abstract TreeMap<String, Set<String>> getPredecessorEvents(Cache source, Cache target, String expression);

    public abstract String getName();

    public AtomicAllenAlgebraMapper() {
    }

    public static String getBeginProperty(String properties) {
	properties = properties.substring(properties.indexOf(".") + 1, properties.length());
	int plusIndex = properties.indexOf("|");
	if (properties.indexOf("|") != -1) {
	    String p1 = properties.substring(0, plusIndex);
	    return p1;
	} else
	    return properties;
    }

    public static String getEndProperty(String properties) {
	properties = properties.substring(properties.indexOf(".") + 1, properties.length());
	int plusIndex = properties.indexOf("|");
	if (properties.indexOf("|") != -1) {
	    String p1 = properties.substring(plusIndex + 1, properties.length());
	    return p1;
	} else
	    return properties;
    }

    public static TreeMap<String, Set<Instance>> orderByBeginDate(Cache cache, String expression) {
	TreeMap<String, Set<Instance>> blocks = new TreeMap<String, Set<Instance>>();
	Parser p = new Parser(expression, 0.0d);
	String property = getBeginProperty(p.getTerm1());

	for (Instance instance : cache.getAllInstances()) {
	    TreeSet<String> time = instance.getProperty(property);

	    for (String value : time) {
		try {
		    // 2015-04-22T11:29:51+02:00
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    Date date = df.parse(value);
		    long epoch = date.getTime();
		    if (!blocks.containsKey(String.valueOf(epoch))) {
			Set<Instance> l = new HashSet<Instance>();
			l.add(instance);
			blocks.put(String.valueOf(epoch), l);
		    } else {
			blocks.get(String.valueOf(epoch)).add(instance);
		    }
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }

	}
	return blocks;
    }
    public static TreeMap<Long, Set<String>> orderByBeginDate1(Cache cache, String expression) {
	TreeMap<Long, Set<String>> blocks = new TreeMap<Long, Set<String>>();
	Parser p = new Parser(expression, 0.0d);
	String property = getBeginProperty(p.getTerm1());

	for (Instance instance : cache.getAllInstances()) {
	    TreeSet<String> time = instance.getProperty(property);

	    for (String value : time) {
		try {
		    // 2015-04-22T11:29:51+02:00
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    Date date = df.parse(value);
		    long epoch = date.getTime();
		    if (!blocks.containsKey(epoch)) {
			Set<String> l = new HashSet<String>();
			l.add(instance.getUri());
			blocks.put(epoch, l);
		    } else {
			blocks.get(epoch).add(instance.getUri());
		    }
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }

	}
	logger.info(blocks.size());
	return blocks;
    }
    public static TreeMap<Long, Set<String>> orderByEndDate1(Cache cache, String expression) {
	TreeMap<Long, Set<String>> blocks = new TreeMap<Long, Set<String>>();
	Parser p = new Parser(expression, 0.0d);
	String property = getEndProperty(p.getTerm1());

	for (Instance instance : cache.getAllInstances()) {
	    TreeSet<String> time = instance.getProperty(property);

	    for (String value : time) {
		try {
		    // 2015-04-22T11:29:51+02:00
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    Date date = df.parse(value);
		    long epoch = date.getTime();
		    if (!blocks.containsKey(epoch)) {
			Set<String> l = new HashSet<String>();
			l.add(instance.getUri());
			blocks.put(epoch, l);
		    } else {
			blocks.get(epoch).add(instance.getUri());
		    }
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }

	}
        logger.info(blocks.size());
	return blocks;
    }
    public static TreeMap<String, Set<Instance>> orderByEndDate(Cache cache, String expression) {
	TreeMap<String, Set<Instance>> blocks = new TreeMap<String, Set<Instance>>();
	Parser p = new Parser(expression, 0.0d);
	String property = getEndProperty(p.getTerm1());

	for (Instance instance : cache.getAllInstances()) {
	    TreeSet<String> time = instance.getProperty(property);

	    for (String value : time) {
		try {
		    // 2015-04-22T11:29:51+02:00
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		    Date date = df.parse(value);
		    long epoch = date.getTime();
		    if (!blocks.containsKey(String.valueOf(epoch))) {
			Set<Instance> l = new HashSet<Instance>();
			l.add(instance);
			blocks.put(String.valueOf(epoch), l);
		    } else {
			blocks.get(String.valueOf(epoch)).add(instance);
		    }
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }

	}

	return blocks;
    }

    public static TreeMap<String, Set<String>> mapConcurrent(TreeMap<Long, Set<String>> sources,
	    TreeMap<Long, Set<String>> targets) {
	TreeMap<String, Set<String>> concurrentEvents = new TreeMap<String, Set<String>>();

	for (Map.Entry<Long, Set<String>> sourceEntry : sources.entrySet()) {

	    Long sourceTimeStamp = sourceEntry.getKey();
	    Set<String> sourceInstances = sourceEntry.getValue();

	    Set<String> tempTargets = targets.get(sourceTimeStamp);
	    if (tempTargets != null) {
		for (String sourceInstance : sourceInstances) {
		    concurrentEvents.put(sourceInstance, tempTargets);
		}
	    }
	}

	return concurrentEvents;

    }

    // 1429695026000
    public static TreeMap<String, Set<String>> mapPredecessor(TreeMap<Long, Set<String>> sources,
	    TreeMap<Long, Set<String>> targets) {
	TreeMap<String, Set<String>> concurrentEvents = new TreeMap<String, Set<String>>();

	for (Map.Entry<Long, Set<String>> sourceEntry : sources.entrySet()) {

	    Long sourceTimeStamp = sourceEntry.getKey();
	    Set<String> sourceInstances = sourceEntry.getValue();

	    SortedMap<Long, Set<String>> tempTargets = targets.tailMap(sourceTimeStamp);

	    if (tempTargets != null) {

		Set<String> subTargets = new TreeSet<String>();
		for (Map.Entry<Long, Set<String>> targetEntry : tempTargets.entrySet()) {
		    Long targetTimeStamp = targetEntry.getKey();
		   
		    if (!targetTimeStamp.equals(sourceTimeStamp)) {
			subTargets.addAll(targetEntry.getValue());
		    }
		}
		if (!subTargets.isEmpty()) {
		    for (String sourceInstance : sourceInstances) {
			concurrentEvents.put(sourceInstance, subTargets);

		    }
		}

	    }
	}

	return concurrentEvents;

    }
}
