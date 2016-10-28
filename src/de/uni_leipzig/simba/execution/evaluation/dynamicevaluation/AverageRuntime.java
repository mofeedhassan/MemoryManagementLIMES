package de.uni_leipzig.simba.execution.evaluation.dynamicevaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.specification.LinkSpec;

public class AverageRuntime {
    private String baseDirectory;
    protected static final String TAB_DELIMITER = "\t";
    protected static final String NEW_LINE_SEPARATOR = "\n";
    private String name;
    static Logger logger = Logger.getLogger("LIMES");

    public AverageRuntime(String baseDirectory, String name) {
	this.baseDirectory = baseDirectory;
	this.name = name;
    }

    public void getAverageRuntime() {
	DecimalFormat df = new DecimalFormat("#");
	df = new DecimalFormat("#");
	df.setMaximumFractionDigits(3);
	LinkedHashMap<Integer, Integer> specAndSize = new LinkedHashMap<Integer, Integer>();
	LinkedHashMap<Integer, Integer> lengthsAndFrequency = new LinkedHashMap<Integer, Integer>();
	String specificationFile = "datasets/" + name + "/specifications/specifications.txt";
	String header = "Specification Size\t" + "CP\t" + "HP\t" + "DP";

	BufferedReader br = null;
	try {

	    br = new BufferedReader(new FileReader(specificationFile));
	    String line = "";
	    int lineCounter = 1;
	    while ((line = br.readLine()) != null) {
		LinkSpec spec = new LinkSpec(line.split(Pattern.quote(">="))[0],
			Double.parseDouble(line.split(Pattern.quote(">="))[1]), true);
		int size = spec.size();
		if (size > 7)
		    size = 7;

		if (specAndSize.containsKey(lineCounter) == false) {
		    specAndSize.put(lineCounter, size);
		}
		if (lengthsAndFrequency.containsKey(size) == false)
		    lengthsAndFrequency.put(size, 0);
		lengthsAndFrequency.put(size, lengthsAndFrequency.get(size) + 1);
		lineCounter++;

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
	//////////////////////////////////////////////////////////////////////////////////////////////////
	String runtimeFile = this.baseDirectory + "planner_results/min_final/Overall.csv";
	TreeMap<Integer, LinkedList<Float>> averageOfSpecification = new TreeMap<Integer, LinkedList<Float>>();

	BufferedReader brRuntime = null;
	try {

	    brRuntime = new BufferedReader(new FileReader(runtimeFile));
	    String line = "";

	    int lineCounter = 1;
	    brRuntime.readLine();
	    while ((line = brRuntime.readLine()) != null) {
		String[] fullEntry = line.split(TAB_DELIMITER);

		int specSize = specAndSize.get(lineCounter);

		if (averageOfSpecification.containsKey(specSize) == false) {
		    averageOfSpecification.put(specSize, new LinkedList<Float>());
		    for (int pos = 0; pos < 3; pos++) {
			averageOfSpecification.get(specSize).add(pos, 0.0f);
		    }
		}

		for (int pos = 0; pos < 3; pos++) {
		    Float newValue = Float.parseFloat(fullEntry[pos]) / (float) lengthsAndFrequency.get(specSize);
		    Float currentValue = averageOfSpecification.get(specSize).get(pos);
		    averageOfSpecification.get(specSize).set(pos, newValue + currentValue);
		}

		lineCounter++;
	    }

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (brRuntime != null) {
		try {
		    brRuntime.close();

		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	String output = this.baseDirectory + "planner_results/min_final/" + "AverageRuntimes.csv";

	BufferedWriter writer = null;
	String all = "";
	for (Entry<Integer, LinkedList<Float>> entry : averageOfSpecification.entrySet()) {
	    Integer key = entry.getKey();
	    all += df.format(key) + TAB_DELIMITER;
	    LinkedList<Float> value = entry.getValue();
	    int k = 0;
	    for (Float no : value) {
		all += df.format(no/1000);
		if (k != value.size() - 1)
		    all += TAB_DELIMITER;
		k++;
	    }
	    all += NEW_LINE_SEPARATOR;

	}

	try {
	    writer = new BufferedWriter(new FileWriter(output));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	all = header + NEW_LINE_SEPARATOR + all;

	try {
	    writer.append(all);
	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public static void main(String args[]) {
	String[] datasets = { "ABTBUY", "AMAZONGOOGLE", "DBLPACM", "DBLPSCHOLAR", "MOVIES", "TOWNS", "VILLAGES" };
	for (String DatasetName : datasets) {
	    logger.info("Current dataset: " + DatasetName);

	    String BaseDirectory = "datasets/dynamic_planner_results/" + DatasetName + "/";

	    AverageRuntime exp = new AverageRuntime(BaseDirectory, DatasetName);
	    exp.getAverageRuntime();
	}

    }
}
