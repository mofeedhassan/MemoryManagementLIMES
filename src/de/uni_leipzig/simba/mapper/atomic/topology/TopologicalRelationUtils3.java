package de.uni_leipzig.simba.mapper.atomic.topology;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.io.SerializerFactory;
import de.uni_leipzig.simba.query.FileQueryModule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdressler
 */
@SuppressWarnings("Duplicates")
public class TopologicalRelationUtils3 {

    // workaround for large number of threads:
    // -- just paste reference to map into global list.
    // -- in main matching loop wait for results to pop up in the list
    // merge results into main mapping while waiting for other threads to finish

    public static class Stats {

        public final static String AVG = "avg";
        public final static String MIN = "min";
        public final static String MAX = "max";
        public final static String MED = "median";
        public static boolean swap = false;

        public static double[] decideForTheta (Stats s, Stats t, String measure) {
            double[] stats = {s.minX, s.minY, t.minX, t.minY};
            double[] result = new double[2];
            if (measure.equals(MIN)) {
            } else if (measure.equals(MAX)) {
                stats[0] = s.maxX;
                stats[1] = s.maxY;
                stats[2] = t.maxX;
                stats[3] = t.maxY;

            } else if (measure.equals(AVG)) {
                stats[0] = s.avgX;
                stats[1] = s.avgY;
                stats[2] = t.avgX;
                stats[3] = t.avgY;

            } else if (measure.equals(MED)) {
                stats[0] = s.medX;
                stats[1] = s.medY;
                stats[2] = t.medX;
                stats[3] = t.medY;

            }
            double estAreaS = stats[0] * stats[1] * s.size;
            double estAreaT = stats[2] * stats[3] * t.size;
            double diffX = stats[0]/stats[2];
            double diffY = stats[1]/stats[3];
            //significantly both axes or one axe
            boolean oneDimDiff = diffX*diffY > 4 && (diffX > 8 && diffY <= 5 || diffY > 8 && diffX <= 5);
            boolean twoDimDiff = diffX > 5 && diffY > 5;
            if ( oneDimDiff || twoDimDiff ) {

            } else {
                result[0] = (estAreaS + estAreaT) / (estAreaS * stats[0] + estAreaT * stats[2]);
                result[1] = (estAreaS + estAreaT) / (estAreaS * stats[1] + estAreaT * stats[3]);
            }
            // Decision rules:
            // 1> Swap if the estimated area covered by T is smaller than the estimated area covered by S.
            //    (So the indexing method can optimize square generation)
            swap = estAreaS > estAreaT;
            // 2> If {stat_S,stat_T} do not differ significantly,
            //    use a weighted mean of both. (weighted by estimated area coverage)

            swap = estAreaS > estAreaT;
            // 3> If one of {stat_S,stat_T} is significantly greater than the other and the areas covered are not
            //     biased towards the smaller one, swap to the smaller dataset and use the smaller grid (big theta)
            /**
             *  Use two continuous function f and g. For computing the grid sizes we have
             *  f_grid(estAreaS, estAreaT, avgWidthS, avgWidthT, avgHeightS, avgHeightT) and for the swap property
             *  f_swap(estAreaS, estAreaT, avgWidthS, avgWidthT, avgHeightS, avgHeightT)
             *
             */

            swap = estAreaS > estAreaT;

            return result;
        }

        private double size;
        private double minX;
        private double maxX;
        private double avgX;
        private double medX;
        private double minY;
        private double maxY;
        private double avgY;
        private double medY;

        public Stats(Collection<Geometry> input) {
            double[] x = new double[input.size()];
            double[] y = new double[input.size()];
            int i = 0;
            for (Geometry geometry : input) {
                Envelope e = geometry.getEnvelopeInternal();
                y[i] = e.getHeight();
                x[i] = e.getWidth();
                i++;
            }
            this.size = input.size();
            Arrays.sort(x);
            this.minX = x[0];
            this.maxX = x[x.length-1];
            this.avgX = 0;
            for (double v : x) {
                this.avgX+=v;
            }
            this.avgX/=x.length;
//            this.avgX = Arrays.stream(x).average().getAsDouble();
            this.medX = x.length % 2 == 0 ? (x[x.length/2 - 1] + x[x.length/2]) / 2.0d : x[x.length/2];
            Arrays.sort(y);
            this.minY = y[0];
            this.maxY = y[y.length-1];
            this.avgY = 0;
            for (double v : y) {
                this.avgY+=v;
            }
            this.avgY/=y.length;
//            this.avgY = Arrays.stream(y).average().getAsDouble();
            this.medY = y.length % 2 == 0 ? (y[y.length/2 - 1] + y[y.length/2]) / 2.0d : y[y.length/2];
        }

        public double getSize() {
            return size;
        }

        public double getMinX() {
            return minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getAvgX() {
            return avgX;
        }

        public double getMedX() {
            return medX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getAvgY() {
            return avgY;
        }

        public double getMedY() {
            return medY;
        }

        public String toString() {
            return "[MIN(" + df.format(minX) + ";" + df.format(minY) +
                    ");MAX(" + df.format(maxX) + ";" + df.format(maxY) +
                    ";AVG(" + df.format(avgX) + ";" + df.format(avgY) +
                    ");MED(" + df.format(medX) + ";" + df.format(medY) +
                    ")]";
        }

    }

    public static class MBBIndex {

        public int lat1, lat2, lon1, lon2;
        public Geometry polygon;
        private String uri;


        public MBBIndex(int lat1, int lon1, int lat2, int lon2, Geometry polygon, String uri) {
            this.lat1 = lat1;
            this.lat2 = lat2;
            this.lon1 = lon1;
            this.lon2 = lon2;
            this.polygon = polygon;
            this.uri = uri;
        }

        public boolean contains(MBBIndex i) {
            return this.lat1 <= i.lat1 && this.lon1 <= i.lon1 && this.lon2 >= i.lon2 && this.lat2 >= i.lat2;
        }

        public boolean intersects(MBBIndex i) {
            return !this.disjoint(i);
        }

        public boolean disjoint(MBBIndex i) {
            return this.lat2 < i.lat1 || this.lat1 > i.lat2 || this.lon2 < i.lon1 || this.lon1 > i.lon2;
        }

        public boolean equals(Object o) {
            if (!(o instanceof MBBIndex)) {
                return false;
            }
            MBBIndex i = ((MBBIndex) o);
            return lat1 == i.lat1 && lat2 == i.lat2 && lon1 == i.lon1 && lon2 == i.lon2;
        }


    }

    public static class SquareIndex {

        public HashMap<Integer, HashMap<Integer, List<MBBIndex>>> map = new HashMap<Integer, HashMap<Integer, List<MBBIndex>>>();

        public SquareIndex () {

        }

        public SquareIndex (int capacity) {
            this.map = new HashMap<>(capacity);
        }

        public synchronized void add(int i, int j, MBBIndex m) {
            if (!map.containsKey(i)) {
                map.put(i, new HashMap<Integer, List<MBBIndex>>());
            }
            if (!map.get(i).containsKey(j)) {
                map.get(i).put(j, new ArrayList<MBBIndex>());
            }
            map.get(i).get(j).add(m);
        }

        public List<MBBIndex> getSquare(int i, int j) {
            if (!map.containsKey(i) || !map.get(i).containsKey(j))
                return Collections.emptyList();
            else
                return map.get(i).get(j);
        }
    }

    public static class Matcher implements Runnable {

        private String relation;
        private final List<Map<String, Set<String>>> result;
        private List<MBBIndex> scheduled;

        public Matcher(String relation, List<Map<String, Set<String>>> result) {
            this.relation = relation;
            this.result = result;
            this.scheduled = new ArrayList<>();
        }

        @Override
        public void run() {
            Map<String, Set<String>> temp = new HashMap<>();
            for (int i = 0; i < scheduled.size(); i+=2) {
                MBBIndex s = scheduled.get(i);
                MBBIndex t = scheduled.get(i+1);
                if (relate(s.polygon, t.polygon, relation)) {
                    if (!temp.containsKey(s.uri)) {
                        temp.put(s.uri, new HashSet<String>());
                    }
                    temp.get(s.uri).add(t.uri);
                }
            }
            synchronized (result) {
                result.add(temp);
            }
        }

        public void schedule (MBBIndex s, MBBIndex t) {
            scheduled.add(s);
            scheduled.add(t);
        }

        public int size() {
            return scheduled.size();
        }

        private static Boolean relate(Geometry geometry1, Geometry geometry2, String relation) {
            switch (relation) {
                case EQUALS:
                    return geometry1.equals(geometry2);
                case DISJOINT:
                    return geometry1.disjoint(geometry2);
                case INTERSECTS:
                    return geometry1.intersects(geometry2);
                case TOUCHES:
                    return geometry1.touches(geometry2);
                case CROSSES:
                    return geometry1.crosses(geometry2);
                case WITHIN:
                    return geometry1.within(geometry2);
                case CONTAINS:
                    return geometry1.contains(geometry2);
                case OVERLAPS:
                    return geometry1.overlaps(geometry2);
                default:
                    return geometry1.relate(geometry2, relation);
            }
        }
    }

    public static class Merger implements Runnable {

        private Mapping m;
        private List<Map<String, Set<String>>> localResults = new ArrayList<>();

        public Merger (List<Map<String, Set<String>>> results, Mapping m) {
            this.m = m;
            // copy over entries to local list
            synchronized (results) {
                for (Iterator<Map<String, Set<String>>> iterator = results.listIterator(); iterator.hasNext(); ) {
                    localResults.add(iterator.next());
                    iterator.remove();
                }
            }
        }

        @Override
        public void run() {
            // merge back to m
            for (Map<String, Set<String>> result : localResults) {
                for (String s : result.keySet()) {
                    for (String t : result.get(s)) {
                        m.add(s, t, 1.0d);
                    }
                }
            }
        }
    }

    public static class Stopwatch {

        private static long start;

        public static void start() {
            start = System.currentTimeMillis();
        }

        public static long stop() {
            long now = System.currentTimeMillis();
            long ms = now - start;
            start = now;
            return ms;
        }

        public static long lap() {
            return System.currentTimeMillis() - start;
        }

    }

    public static int i = 0;
    public static int numThreads = 1;
    public static long indexTime = 0;
    public static long scheduleTime = 0;
    public static long matchTime = 0;
    public static int matcherSize = 1000;
    public static double theta = 10;
    public static double thetaX = 10;
    public static double thetaY = 10;
    public static boolean computeTheta = false;
    public static String statMeasure = "manual";
    public static final String EQUALS = "equals";
    public static final String DISJOINT = "disjoint";
    public static final String INTERSECTS = "intersects";
    public static final String TOUCHES = "touches";
    public static final String CROSSES = "crosses";
    public static final String WITHIN = "within";
    public static final String CONTAINS = "contains";
    public static final String OVERLAPS = "overlaps";
    private static PrintStream parkedStream = new PrintStream(new ByteArrayOutputStream());
    public static DecimalFormat df = new DecimalFormat("0.0000");

    public static void main(String args[]) {
        // deny logging
        toggleLog();
        String[] ds = args[0].split("-");
        String sourceFile, targetFile;
        if (args.length >= 6) {
            sourceFile = args[4];
            targetFile = args[5];
        } else {
            sourceFile = ds[0] + ".nt";
            targetFile = ds[1] + ".nt";
        }
        if (args.length >= 7) {
            numThreads = Integer.parseInt(args[6]);
        }
        if (args.length >= 8) {
            matcherSize = Integer.parseInt(args[7]);
        }
        HashMap<String, Geometry> source = getDataSet(ds[0], sourceFile);
        HashMap<String, Geometry> target = getDataSet(ds[1], targetFile);
        long begin, end;
        try {
            theta = Double.valueOf(args[3]);
            thetaX = theta;
            thetaY = theta;
        } catch (NumberFormatException e) {
            computeTheta = true;
            statMeasure = args[3];
        }
        i = 0;
        begin = System.currentTimeMillis();
        Mapping mm = getMapping(source, target, args[1]);
        end = System.currentTimeMillis();
        SerializerFactory.getSerializer("NT").writeToFile(mm, args[1], args[2]);
        toggleLog();

        System.out.println(sourceFile + "\t" + targetFile + "\t" + args[1] + "\t" + df.format(thetaX) + "\t" + df.format(thetaY) + "\t" + statMeasure + "\t" + (end - begin)   + "\t" + i + "\t" + mm.size() + "\t" + numThreads + "\t" + indexTime + "\t" + matchTime + "\t" + scheduleTime);
    }

    public static Mapping getMapping(Map<String, Geometry> sourceData, Map<String, Geometry> targetData, String relation) {

        // Relation thats actually used for computation.
        // Might differ from input relation when swapping occurs or the input relation is 'disjoint'.
        String rel = relation;

        // When relation for Mapping M is 'disjoint' we compute mapping M' relation 'intersects'
        // and return M = (S x T) \ M'
        boolean disjointStrategy = rel.equals(DISJOINT);
        if (disjointStrategy)
            rel = INTERSECTS;

        Stopwatch.start();
        if (computeTheta) {
            Stats s = new Stats(sourceData.values());
            Stats t = new Stats(targetData.values());
            double[] ttemp = Stats.decideForTheta(s, t, statMeasure);
            thetaX = ttemp[0];
            thetaY = ttemp[1];
        }

        // swap smaller dataset to source
        // if swap is necessary is decided in Stats.decideForTheta([...])!
        Map<String, Geometry> swap;
        boolean swapped = Stats.swap;
        if (swapped) {
            swap = sourceData;
            sourceData = targetData;
            targetData = swap;
            swap = null;
            if (rel.equals(WITHIN))
                rel = CONTAINS;
            else if (rel.equals(CONTAINS))
                rel = WITHIN;
        }

        // set up indexes
        SquareIndex sourceIndex = index(sourceData, null);
        SquareIndex targetIndex = index(targetData, sourceIndex);
        indexTime = Stopwatch.stop();

        // execute matching
        int xmatcherSize = matcherSize;
        ExecutorService matchExec = Executors.newFixedThreadPool(numThreads);
        ExecutorService mergerExec = Executors.newFixedThreadPool(1);
        Mapping m = new Mapping();
        List<Map<String, Set<String>>> results = Collections.synchronizedList(new ArrayList<Map<String, Set<String>>>());
        Map<String, Set<String>> computed = new HashMap<>();
        Matcher matcher = new Matcher(relation, results);

        for (Integer lat : sourceIndex.map.keySet()) {
            for (Integer lon : sourceIndex.map.get(lat).keySet()) {
                List<MBBIndex> source = sourceIndex.getSquare(lat, lon);
                List<MBBIndex> target = targetIndex.getSquare(lat, lon);
                if (target != null && target.size() > 0) {
//                    i += Matcher.compute(rel, source, target, computed, computed, null, m);
                    for (MBBIndex a : source) {
                        if (!computed.containsKey(a.uri))
                            computed.put(a.uri, new HashSet<String>());
                        for (MBBIndex b : target) {
                            if (!computed.get(a.uri).contains(b.uri)) {
                                computed.get(a.uri).add(b.uri);
                                boolean compute =
                                        (rel.equals(CONTAINS) && a.contains(b))
                                                || (rel.equals(WITHIN) && b.contains(a))
                                                || (rel.equals(EQUALS) && a.equals(b))
                                                || rel.equals(INTERSECTS)
                                                || rel.equals(CROSSES)
                                                || rel.equals(TOUCHES)
                                                || rel.equals(OVERLAPS);
                                if (compute) {
                                    i++;
                                    if (numThreads == 1) {
                                        if (Matcher.relate(a.polygon, b.polygon, relation)) {
                                            m.add(a.uri, b.uri, 1.0);
                                        }
                                    } else {
                                        matcher.schedule(a, b);
                                        if (matcher.size() == xmatcherSize) {
                                            matchExec.execute(matcher);
                                            matcher = new Matcher(relation, results);
                                            if (results.size() > 0) {
                                                mergerExec.execute(new Merger(results, m));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (numThreads > 1) {
            scheduleTime = Stopwatch.lap();
            if (matcher.size() > 0) {
                matchExec.execute(matcher);
            }
            matchExec.shutdown();
            while (!matchExec.isTerminated()) {
                try {
                    if (results.size() > 0) {
                        mergerExec.execute(new Merger(results, m));
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (results.size() > 0) {
                mergerExec.execute(new Merger(results, m));
            }
            mergerExec.shutdown();
            while(!mergerExec.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        // Compute M = (S x T) \ M' for disjoint relation
        if (disjointStrategy) {
            Mapping disjoint = new Mapping();
            for (String s : sourceData.keySet()) {
                for (String t : targetData.keySet()) {
                    if (!m.contains(s, t)) {
                        disjoint.add(s, t, 1.0d);
                    }
                }
            }
            m = disjoint;
        }

        matchTime = Stopwatch.stop();
        return m;
    }

    public static SquareIndex index (Map<String, Geometry> input, SquareIndex extIndex) {

        SquareIndex result = new SquareIndex();

        for (String p : input.keySet()) {
            Geometry g = input.get(p);
            Envelope envelope = g.getEnvelopeInternal();

            int minLatIndex = (int) Math.floor(envelope.getMinY() * thetaY);
            int maxLatIndex = (int) Math.ceil(envelope.getMaxY() * thetaY);
            int minLongIndex = (int) Math.floor(envelope.getMinX() * thetaX);
            int maxLongIndex = (int) Math.ceil(envelope.getMaxX() * thetaX);

            if (extIndex == null) {
                for (int latIndex = minLatIndex; latIndex <= maxLatIndex; latIndex++) {
                    for (int longIndex = minLongIndex; longIndex <= maxLongIndex; longIndex++) {
                        result.add(latIndex, longIndex, new MBBIndex(minLatIndex, minLongIndex, maxLatIndex, maxLongIndex, g, p));
                    }
                }
            } else {
                for (int latIndex = minLatIndex; latIndex <= maxLatIndex; latIndex++) {
                    for (int longIndex = minLongIndex; longIndex <= maxLongIndex; longIndex++) {
                        if (extIndex.getSquare(latIndex, longIndex) != null)
                            result.add(latIndex, longIndex, new MBBIndex(minLatIndex, minLongIndex, maxLatIndex, maxLongIndex, g, p));
                    }
                }
            }
        }
        input = null;
        return result;
    }

    private static HashMap<String, Geometry> getDataSet(String name, String filePath) {
        KBInfo kb = new KBInfo();
        HashMap<String, Geometry> result = new HashMap<>();
        if (name.equals("nuts")) {
            kb.type = "N3";
            kb.var = "?x";
            kb.prefixes = new HashMap<>();
            kb.prefixes.put("geo", "http://www.opengis.net/ont/geosparql#");
            kb.restrictions = new ArrayList<>(Arrays.asList(""));
            kb.properties = new ArrayList<>(Arrays.asList("geo:asWKT"));
        } else if (name.equals("clc")) {
            kb.type = "N3";
            kb.var = "?x";
            kb.prefixes = new HashMap<>();
            kb.prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            kb.prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            kb.prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
            kb.prefixes.put("geo", "http://www.opengis.net/ont/geosparql#");
            kb.prefixes.put("geosf", "http://www.opengis.net/ont/sf#");
            kb.restrictions = new ArrayList<>(Arrays.asList("?x rdf:type geosf:Polygon"));
            kb.properties = new ArrayList<>(Arrays.asList("geo:asWKT"));
        } else if (name.equals("lgd")) {
            kb.type = "N3";
            kb.var = "?x";
            kb.prefixes = new HashMap<>();
            kb.prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            kb.prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            kb.prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
            kb.prefixes.put("geo", "http://www.opengis.net/ont/geosparql#");
            kb.prefixes.put("geosf", "http://www.opengis.net/ont/sf#");
            kb.restrictions = new ArrayList<>(Arrays.asList(""));
            kb.properties = new ArrayList<>(Arrays.asList("geo:asWKT"));
        }
        kb.endpoint = System.getProperty("user.dir") + "/" + filePath;
        kb.afterPropertiesSet();
        FileQueryModule fqm = new FileQueryModule(kb);
        HybridCache hc = new HybridCache();
        fqm.fillCache(hc);
        for (String uri : hc.getAllUris()) {
            for (String value : hc.getInstance(uri).getProperty("geo:asWKT")) {
                Geometry g;
                try {
                    WKTReader reader = new WKTReader();
                    String wkt = value.replaceAll("<.*> ", "");
                    g = reader.read(wkt);
                } catch (ParseException ex) {
                    Logger.getLogger(TopologicalRelationUtils3.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                } catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
                    continue;
                }
                result.put(uri, g);
            }
        }
        return result;
    }

    public static void toggleLog() {
        PrintStream swap = parkedStream;
        parkedStream = System.out;
        System.setOut(swap);
    }
}