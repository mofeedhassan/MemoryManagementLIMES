package de.uni_leipzig.simba.execution.evaluation.dynamicevaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.grecall.oracle.SimpleOracle;
import de.uni_leipzig.simba.grecall.util.DatasetConfiguration;
import de.uni_leipzig.simba.grecall.util.StatisticsBase;
import de.uni_leipzig.simba.specification.LinkSpec;

public class Histogram {
    // optimization
    // time
    private int iterations = 3;
    private Object baseDirectory;
    protected static final String TAB_DELIMITER = "\t";
    protected static final String NEW_LINE_SEPARATOR = "\n";
    private String[] FileNames = { "Overall.csv", "PlanningTime.csv", "ExecutionTime.csv"};

    static Logger logger = Logger.getLogger("LIMES");

    public Histogram(String folderName) {
	this.baseDirectory = folderName;
    }

    public void getMin() {

	for (String file : FileNames) {
	    // specification number, <set of float numbers>
	    LinkedHashMap<Integer, LinkedList<Float>> statistics = new LinkedHashMap<Integer, LinkedList<Float>>();
	    String header = null;

	    for (int iteration = 0; iteration < iterations; iteration++) {
		BufferedReader br = null;
		try {
		    String csvFile = this.baseDirectory + "/" + iteration + "/" + file;
		    br = new BufferedReader(new FileReader(csvFile));
		    int specCounter = 0;
		    String line = "";
		    header = br.readLine();

		    while ((line = br.readLine()) != null) {

			String[] fullEntry = line.split(TAB_DELIMITER);

			LinkedList<Float> temp = new LinkedList<Float>();
			for (int pos = 0; pos < fullEntry.length; pos++) {
			    temp.add(Float.parseFloat(fullEntry[pos]));
			}

			if (statistics.get(specCounter) == null)
			    statistics.put(specCounter, temp);
			else {// get old statistics
			    LinkedList<Float> oldStats = statistics.get(specCounter);
			    for (int pos = 0; pos < oldStats.size(); pos++) {
				if (oldStats.get(pos) > temp.get(pos)) {
				    oldStats.set(pos, temp.get(pos));
				}
			    }
			    statistics.put(specCounter, oldStats);
			}
			specCounter++;
		    }

		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    if (br != null) {
			try {
			    br.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }
		}

	    }
	    String OptimizerTypeFolder = this.baseDirectory + "/min";
	    File dirName = new File(OptimizerTypeFolder);
	    if (!dirName.isDirectory()) {
		try {
		    dirName.mkdir();
		} catch (SecurityException se) {
		}
	    }

	    // file = file.replace(".csv", ".tsv");
	    String specFilename = OptimizerTypeFolder + "/" + file;
	    File file1 = new File(specFilename);
	    FileWriter writer = null;
	    DecimalFormat df = new DecimalFormat("#");
	    df = new DecimalFormat("#");
	    df.setMaximumFractionDigits(50);
	    if (!file1.exists()) {
		try {
		    writer = new FileWriter(specFilename);
		    header = "CP\tRW+CP\tHP\tRW+HP\tDP\tRW+DP";
		    writer.append(header);
		    writer.append(NEW_LINE_SEPARATOR);
		    for (Entry<Integer, LinkedList<Float>> entry : statistics.entrySet()) {
			Integer key = entry.getKey();
			LinkedList<Float> value = entry.getValue();
			int k = 0;
			for (float no : value) {
			    writer.append(df.format(no));
			    if (k != value.size() - 1)
				writer.append(TAB_DELIMITER);
			    k++;
			}
			writer.append(NEW_LINE_SEPARATOR);
		    }

		    writer.close();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	}

    }

    public void getResults() {

	for (String file : FileNames) {
	    // specification number, <set of float numbers>
	    LinkedHashMap<Integer, LinkedList<Float>> statistics = new LinkedHashMap<Integer, LinkedList<Float>>();
	    String header = null;

	    BufferedReader br = null;
	    try {
		String csvFile = this.baseDirectory + "min/" + file;
		br = new BufferedReader(new FileReader(csvFile));
		int specCounter = 0;
		String line = "";
		header = br.readLine();

		while ((line = br.readLine()) != null) {

		    String[] fullEntry = line.split(TAB_DELIMITER);

		    LinkedList<Float> temp = new LinkedList<Float>();
		    for (int pos = 0; pos < fullEntry.length; pos++) {
			if (pos == 0 || pos == 2 || pos == 4)
			    temp.add(Float.parseFloat(fullEntry[pos]));
		    }

		    statistics.put(specCounter, temp);
		    specCounter++;
		}

	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    } finally {
		if (br != null) {
		    try {
			br.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    }

	    String OptimizerTypeFolder = this.baseDirectory + "/min_final";
	    File dirName = new File(OptimizerTypeFolder);
	    if (!dirName.isDirectory()) {
		try {
		    dirName.mkdir();
		} catch (SecurityException se) {
		}
	    }
	    // file = file.replace(".csv", ".tsv");
	    String specFilename = OptimizerTypeFolder + "/" + file;
	    File file1 = new File(specFilename);
	    FileWriter writer = null;
	    DecimalFormat df = new DecimalFormat("#");
	    df = new DecimalFormat("#");
	    df.setMaximumFractionDigits(50);
	    if (!file1.exists()) {
		try {
		    writer = new FileWriter(specFilename);
		    header = "CP\tHP\tDP";
		    writer.append(header);
		    writer.append(NEW_LINE_SEPARATOR);
		    for (Entry<Integer, LinkedList<Float>> entry : statistics.entrySet()) {
			Integer key = entry.getKey();
			LinkedList<Float> value = entry.getValue();
			int k = 0;
			for (float no : value) {
			    writer.append(df.format(no));
			    if (k != value.size() - 1)
				writer.append(TAB_DELIMITER);
			    k++;
			}
			writer.append(NEW_LINE_SEPARATOR);
		    }

		    writer.close();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	}

    }

    public static void main(String args[]) {

	// for each dataset
	String DatasetName = args[0];

	// String BaseDirectory = "datasets/" + DatasetName +
	// "/results_smaller/";
	String BaseDirectory = "/home/kleanthi/Desktop/dynamic_planner_results/" + DatasetName + "/planner_results/";

	File dirName = new File(BaseDirectory);
	if (!dirName.isDirectory()) {
	    try {
		dirName.mkdir();
	    } catch (SecurityException se) {
	    }
	}

	Histogram exp = new Histogram(BaseDirectory);
	exp.getMin();
	exp.getResults();
    }
}
