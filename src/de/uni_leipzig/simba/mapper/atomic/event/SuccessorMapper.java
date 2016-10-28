package de.uni_leipzig.simba.mapper.atomic.event;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.AtomicMapper;

public class SuccessorMapper extends EventMapper implements AtomicMapper {
    static Logger logger = Logger.getLogger("LIMES");

    @Override
    public Mapping getMapping(Cache source, Cache target, String sourceVar, String targetVar, String expression,
            double threshold) {

        Mapping m = new Mapping();

        TreeMap<String, Set<Instance>> sources = this.orderByBeginDate(source, expression);
        TreeMap<String, Set<Instance>> targets = this.orderByBeginDate(target, expression);

        for (Map.Entry<String, Set<Instance>> sourceEntry : sources.entrySet()) {
            String epochSource = sourceEntry.getKey();

            String lowerEpoch = targets.higherKey(epochSource);
            if (lowerEpoch != null) {
                Set<Instance> sourceInstances = sourceEntry.getValue();
                Set<Instance> targetInstances = targets.get(lowerEpoch);
                for (Instance i : sourceInstances) {
                    for (Instance j : targetInstances) {
                        m.add(i.getUri(), j.getUri(), 1);
                    }
                }
            }
        }

        return m;
    }

    @Override
    public String getName() {
        return "successor";
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
