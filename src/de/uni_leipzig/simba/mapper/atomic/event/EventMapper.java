package de.uni_leipzig.simba.mapper.atomic.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.controller.Parser;
import de.uni_leipzig.simba.data.Instance;

public abstract class EventMapper {
    protected String getFirstProperty(String properties) {
        properties = properties.substring(properties.indexOf(".") + 1, properties.length());
        int plusIndex = properties.indexOf("|");
        if (properties.indexOf("|") != -1) {
            String p1 = properties.substring(0, plusIndex);
            return p1;
        } else
            return properties;
    }

    protected String getSecondProperty(String properties) {
        properties = properties.substring(properties.indexOf(".") + 1, properties.length());
        int plusIndex = properties.indexOf("|");
        if (properties.indexOf("|") != -1) {
            String p1 = properties.substring(plusIndex + 1, properties.length());
            return p1;
        } else
            throw new IllegalArgumentException();
    }

    protected TreeMap<String, Set<Instance>> orderByBeginDate(Cache cache, String expression) {

        TreeMap<String, Set<Instance>> blocks = new TreeMap<String, Set<Instance>>();
        Parser p = new Parser(expression, 0.0d);
        String property = getFirstProperty(p.getTerm1());
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
}
