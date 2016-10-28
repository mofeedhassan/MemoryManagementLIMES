package de.uni_leipzig.simba.mapper.atomic.event;


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.AtomicMapper;

public class ConcurrentMapper extends EventMapper implements AtomicMapper {
    static Logger logger = Logger.getLogger("LIMES");

    @Override
    public Mapping getMapping(Cache source, Cache target, String sourceVar, String targetVar, String expression,
            double threshold) {

        Mapping m = new Mapping();
        Parser p = new Parser(expression, threshold);

        TreeMap<String, Set<Instance>> sources = this.orderByBeginDate(source, expression);
        TreeMap<String, Set<Instance>> targets = this.orderByBeginDate(target, expression);
        String machineID = null;

        try {
            machineID = this.getSecondProperty(p.getTerm1());
        } catch (IllegalArgumentException e) {
            logger.error("Missing machine id property in " + p.getTerm1() + ".Exiting..");
            System.exit(1);
        }

        for (Map.Entry<String, Set<Instance>> sourceEntry : sources.entrySet()) {
            String epochSource = sourceEntry.getKey();

            Set<Instance> targetInstances = targets.get(epochSource);
            if (targetInstances != null) {
                Set<Instance> sourceInstances = sourceEntry.getValue();
                for (Instance i : sourceInstances) {
                    for (Instance j : targetInstances) {
                        if (i.getProperty(machineID).equals(j.getProperty(machineID)))
                            m.add(i.getUri(), j.getUri(), 1);
                    }
                }
            }
        }

        return m;
    }

    @Override
    public String getName() {
        return "concurrent";
    }

    @Override
    public double getRuntimeApproximation(int sourceSize, int targetSize, double theta, Language language) {
        return 1000d;
    }

    @Override
    public double getMappingSizeApproximation(int sourceSize, int targetSize, double theta, Language language) {
        return 1000d;
    }

}
