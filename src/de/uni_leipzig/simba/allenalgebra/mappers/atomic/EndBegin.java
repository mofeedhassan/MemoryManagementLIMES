package de.uni_leipzig.simba.allenalgebra.mappers.atomic;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Instance;

public class EndBegin extends AtomicAllenAlgebraMapper {
    public EndBegin() {

    }

    @Override
    public String getName() {
	return "EndBegin";
    }

    @Override
    public TreeMap<String, Set<String>> getConcurrentEvents(Cache source, Cache target, String expression) {
	TreeMap<Long, Set<String>> sources = AtomicAllenAlgebraMapper.orderByEndDate1(source, expression);
	TreeMap<Long, Set<String>> targets = AtomicAllenAlgebraMapper.orderByBeginDate1(target, expression);
	TreeMap<String, Set<String>> events = AtomicAllenAlgebraMapper.mapConcurrent(sources, targets);
	return events;
    }

    @Override
    public TreeMap<String, Set<String>> getPredecessorEvents(Cache source, Cache target, String expression) {
	TreeMap<Long, Set<String>> sources = AtomicAllenAlgebraMapper.orderByEndDate1(source, expression);
	TreeMap<Long, Set<String>> targets = AtomicAllenAlgebraMapper.orderByBeginDate1(target, expression);
	TreeMap<String, Set<String>> events = AtomicAllenAlgebraMapper.mapPredecessor(sources, targets);
	return events;
    }

}
