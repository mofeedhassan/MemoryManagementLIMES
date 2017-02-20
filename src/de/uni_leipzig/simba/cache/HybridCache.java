/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.cache;

import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.query.NoPrefixSparqlQueryModule;
import de.uni_leipzig.simba.query.QueryModule;
import de.uni_leipzig.simba.query.QueryModuleFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * This cache implements a hybrid between memory and file cache. It generates a
 * hash for each data source associated with it and serializes the content of
 * the corresponding data source into a file. If another mapping task is
 * associated with the same data source, it retrieves the corresponding data
 * from the file, which is obviously more efficient for online data sources (no
 * HTTP latency, offline processing, etc.). Else, it retrieves the data,
 * generates a hash and caches it on the harddrive. Enhancing it with folders:
 * specify the folder, where the application has permissions to read and write
 * files.
 * 
 * @author ngonga
 * @author Lyko
 */
public class HybridCache extends MemoryCache implements Serializable {
    // maps uris to instance. A bit redundant as instance contain their URI

    /**
     * 
     */
    private static final long serialVersionUID = -2268344215686055231L;
    HashMap<String, Instance> instanceMap;
    // Iterator for getting next instance
    Iterator<Instance> instanceIterator;
    static Logger logger = Logger.getLogger("LIMES");
    // pointing to the parent folder of the "cache" folder
    private File folder = new File("");

    public HybridCache() {
	instanceMap = new HashMap<String, Instance>();
    }

    /**
     * Create cache specifying the parent folder. Make shure the Application has
     * write permissions there.
     * 
     * @param folder
     *            File pointing to the the parent folder of the (to-be-created)
     *            "cache" folder.
     */
    public HybridCache(File folder) {
	this();
	setFolder(folder);
    }

    /**
     * Returns the next instance in the list of instances
     * 
     * @return null if no next instance, else the next instance
     */
    public Instance getNextInstance() {
	if (instanceIterator == null) {
	    instanceIterator = instanceMap.values().iterator();
	}

	if (instanceIterator.hasNext()) {
	    return instanceIterator.next();
	} else {
	    return null;
	}
    }

    /**
     * Returns all the instance contained in the cache
     * 
     * @return ArrayList containing all instances
     */
    public ArrayList<Instance> getAllInstances() {
	return new ArrayList<Instance>(instanceMap.values());
    }

    public void addInstance(Instance i) {
	// System.out.println("Adding Instance " + i);
	if (instanceMap.containsKey(i.getUri())) {
	    // Instance m = instanceMap.get(i.getUri());

	} else {
	    instanceMap.put(i.getUri(), i);
	}
    }

    /**
     *
     * @param uri
     *            URI to look for
     * @return The instance with the URI uri if it is in the cache, else null
     */
    public Instance getInstance(String uri) {
	if (instanceMap.containsKey(uri)) {
	    return instanceMap.get(uri);
	} else {
	    return null;
	}
    }

    /**
     *
     * @return The size of the cache
     */
    public int size() {
	// logger.info("Size of key set = "+instanceMap.keySet().size());
	return instanceMap.size();
    }

    /**
     * Adds a new spo statement to the cache
     * 
     * @param s
     *            The URI of the instance linked to o via p
     * @param p
     *            The property which links s and o
     * @param o
     *            The value of the property of p for the entity s
     */
    public void addTriple(String s, String p, String o) {
	// logger.info(instanceMap.containsKey(s));
	if (instanceMap.containsKey(s)) {
	    Instance m = instanceMap.get(s);
	    m.addProperty(p, o);
	} else {
	    Instance m = new Instance(s);
	    m.addProperty(p, o);
	    instanceMap.put(s, m);
	}
    }

    /**
     *
     * @param uri
     *            The URI to looks for
     * @return True if an instance with the URI uri is found in the cache, else
     *         false
     */
    public boolean containsUri(String uri) {
	return instanceMap.containsKey(uri);
    }

    public void resetIterator() {
	instanceIterator = instanceMap.values().iterator();
    }

    @Override
    public String toString() {
	return instanceMap.toString();
    }

    public ArrayList<String> getAllUris() {
	return new ArrayList(instanceMap.keySet());
    }

    /**
     *
     * @param i
     *            The instance to look for
     * @return true if the URI of the instance is found in the cache
     */
    public boolean containsInstance(Instance i) {
	return instanceMap.containsKey(i.getUri());
    }

    /**
     * Tries to serialize the content of the cache to a file. If it fails, no
     * file is written to avoid the corruption of future data sources.
     * 
     * @param file
     *            File wherein the content of the cache is to be serialized
     * @throws IOException
     */
    public void saveToFile(File file) {
	FileOutputStream out;
	Logger logger = Logger.getLogger("LIMES");
	logger.info("Serializing " + size() + " objects to " + file.getAbsolutePath());

	try {
	    out = new FileOutputStream(file);
	    ObjectOutputStream serializer = new ObjectOutputStream(out);
	    serializer.writeObject(this);
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    file.delete();
	}
    }

    /**
     * Tries to load the content of the cache from a file
     *
     * @param file
     *            File from which the content is to be loaded
     * @return A Hybrid cache
     * @throws IOException
     */
    public static HybridCache loadFromFile(File file) throws IOException {
	String path = file.getAbsolutePath();
	String parentPath = path.substring(0, path.lastIndexOf("cache"));
	//this line is added to support the existence of other types of caching folders, 
	//such as labelcach and samplecache used by transfer learning to cache class labels or data samples.
	// it does not ruin the original functionality
	parentPath = parentPath.substring(0, parentPath.lastIndexOf("/")+1); 

	File parent = new File(parentPath);

	FileInputStream in = new FileInputStream(file);
	ObjectInputStream deSerializer = new ObjectInputStream(in);
	HybridCache cache;

	try {
	    cache = (HybridCache) deSerializer.readObject();
	    cache.setFolder(parent);
	    return cache;
	} catch (ClassNotFoundException e) {
	    throw new IOException(e);
	} finally {
	    in.close();
	}
    }

    public static HybridCache getData(KBInfo kb) {
	return getData(new File(""), kb);
    }
    public static HybridCache getDataFromGraph(KBInfo kb) {
	return getDataFromGraph(new File(""), kb);
    }
    /**
     * Method to get Data of the specified endpoint, and cache it to the "cache"
     * folder in the folder specified.
     * 
     * @param folder
     *            Path to the parent folder of the "cache" folder.
     * @param kb
     *            Endpoint specification.
     * @return
     */
    public static HybridCache getData(File folder, KBInfo kb) {

	HybridCache cache = new HybridCache(folder);
	// 1. Try to get content from a serialization
	String hash = kb.hashCode() + "";
	File cacheFile = new File(folder + "cache/" + hash + ".ser");
	logger.info("Checking for file " + cacheFile.getAbsolutePath());
	try {
	    if (cacheFile.exists()) {
		logger.info("Found cached data. Loading data from file " + cacheFile.getAbsolutePath());
		cache = HybridCache.loadFromFile(cacheFile);
	    }
	    if (cache.size() == 0) {
		throw new Exception();
	    } else {
		logger.info("Cached data loaded successfully from file " + cacheFile.getAbsolutePath());
		logger.info("Size = " + cache.size());
	    }
	} // 2. If it does not work, then get it from data sourceInfo as
	  // specified
	catch (Exception e) {
	    // e.printStackTrace();
	    // need to add a QueryModuleFactory
	    logger.info("No cached data found for " + kb.id);
	    QueryModule module = QueryModuleFactory.getQueryModule(kb.type, kb);
	    module.fillCache(cache);

	    if (!new File(folder.getAbsolutePath() + File.separatorChar + "cache").exists()
		    || !new File(folder.getAbsolutePath() + File.separatorChar + "cache").isDirectory()) {
		new File(folder.getAbsolutePath() + File.separatorChar + "cache").mkdir();
	    }
	    cache.saveToFile(new File(folder.getAbsolutePath() + File.separatorChar + "cache/" + hash + ".ser"));
	}

	return cache;
    }

    /**
     * Method to get Data of the specified endpoint, and cache it to the "cache"
     * folder in the folder specified.
     * 
     * @param folder
     *            Path to the parent folder of the "cache" folder.
     * @param kb
     *            Endpoint specification.
     * @return
     */
    public static HybridCache getDataFromGraph(File folder, KBInfo kb) {

	HybridCache cache = new HybridCache(folder);
	QueryModule module = QueryModuleFactory.getQueryModule(kb.type, kb);
	module.fillCache(cache);

	return cache;
    }

    /**
     * This method is used by learners which do not have prefix information.
     *
     * @param kb
     *            Info to the knowledge base to query
     * @return A cache filled with the entities to link
     */
    public static HybridCache getNoPrefixData(KBInfo kb) {
	return getNoPrefixData(new File(""), kb);
    }

    /**
     * This method is used by learners which do not have prefix information and
     * with a specified folder containing the cache folder.
     *
     * @param folder
     *            Path to parent folder of the supposed cache folder.
     * @param kb
     *            Info to the knowledge base to query
     * @return A cache filled with the entities to link
     */
    public static HybridCache getNoPrefixData(File folder, KBInfo kb) {
	HybridCache cache = new HybridCache();
	// 1. Try to get content from a serialization
	File cacheFile = new File(folder.getAbsolutePath() + File.separatorChar + "cache/" + kb.hashCode() + ".ser");
	try {
	    if (cacheFile.exists()) {
		logger.info("Found cached data. Loading data from file " + cacheFile.getAbsolutePath());
		cache = HybridCache.loadFromFile(cacheFile);
	    }
	    if (cache.size() == 0) {
		throw new Exception();
	    } else {
		logger.info("Cached data loaded successfully from file " + cacheFile.getAbsolutePath());
		logger.info("Size = " + cache.size());
	    }
	} // 2. If it does not work, then get it from data sourceInfo as
	  // specified
	catch (Exception e) {
	    // need to add a QueryModuleFactory
	    logger.info("No cached data found for " + kb.id);
	    NoPrefixSparqlQueryModule module = new NoPrefixSparqlQueryModule(kb);
	    module.fillCache(cache);

	    if (!new File(folder.getAbsolutePath() + File.separatorChar + "cache").exists()
		    || !new File(folder.getAbsolutePath() + File.separatorChar + "cache").isDirectory()) {
		new File(folder.getAbsolutePath() + File.separatorChar + "cache").mkdir();
	    }
	    cache.saveToFile(
		    new File(folder.getAbsolutePath() + File.separatorChar + "cache/" + kb.hashCode() + ".ser"));
	}

	return cache;
    }
    //
    // public Cache getSample(int size) {
    // Cache c = new MemoryCache();
    // ArrayList<String> uris= getAllUris();
    // while(c.size() < 1000)
    // {
    // int index = (int)Math.floor(Math.random()*size());
    // Instance i = getInstance(uris.get(index));
    // c.addInstance(i);
    // }
    // return c;
    // }

    /**
     * Returns a set of properties (most likely) all instances have.
     * 
     * @return
     */
    // public Set<String> getAllProperties() {
    // if(instanceMap.size() > 0) {
    // HashSet<String> props = new HashSet<String>();
    // Cache c = this;
    // if(size()>30)
    // c = getSample(30);
    // for(Instance i : c.getAllInstances())
    // props.addAll(i.getAllProperties());
    // return props;
    // }
    // else
    // return new HashSet<String>();
    // }

    // public Cache addProperty(String sourcePropertyName, String
    // targetPropertyName, String processingChain) {
    // System.out.println("Adding Property '"+targetPropertyName+"' based upon
    // property '"+sourcePropertyName+"' to cache...");
    // Cache c = new MemoryCache();
    // for(Instance instance: getAllInstances())
    // {
    // String uri = instance.getUri();
    // for(String p: instance.getAllProperties())
    // {
    // for(String value: instance.getProperty(p))
    // {
    // if(p.equals(sourcePropertyName)) {
    // c.addTriple(uri, targetPropertyName, Preprocessor.process(value,
    // processingChain));
    // c.addTriple(uri, p, value);
    // }
    // else
    // c.addTriple(uri, p, value);
    // }
    // }
    // }
    // return c;
    // }

    /**
     * Returns the file pointing to the parent folder of cache.
     * 
     * @return
     */
    public File getFolder() {
	return folder;
    }

    /**
     * Set the parent folder of the cache sub folder.
     * 
     * @param folder
     *            Pointing to the parent folder holding the cache.
     */
    public void setFolder(File folder) {
	this.folder = folder;
    }
    /**
     * @param other
     * @return union of the current cache and the input cache
     */
    @Override
    public Cache union(Cache other) {
    	Cache result = new HybridCache(); 
        for(Instance i : this.getAllInstances()){
        	result.addInstance(i);
        }
        for(Instance i : other.getAllInstances()){
        	result.addInstance(i);
        }
        return result;
    }
}
