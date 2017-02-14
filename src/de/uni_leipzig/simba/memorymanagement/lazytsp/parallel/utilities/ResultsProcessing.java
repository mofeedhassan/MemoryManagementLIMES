package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.io.Read;

/**
 * 
 * @author mofeed
 * It is responsible for extracting processing and organizing the results
 */
public class ResultsProcessing {

	static Map<Integer,String> resultsFiles= new HashMap<Integer,String> ();
	
	/*This method extracts results using a results' folder in addition to the targeted results to be extracted*/
	public static List<String> extractResults(String[] info)
	{
		String resultsFolder = PathUtils.standardizePath(info[1]);
		int targetCol =Integer.parseInt(info[2]);
		String resultsFinalFolder =PathUtils.standardizePath(info[3]);
		List<String> res = extractSpecificResults(resultsFolder, targetCol);
		for (String lines : res) {
			System.out.println(lines);
		}
		ResultsProcessing.initializeFilesNames();
		return res;
	}
	public static List<String> extractSpecificResults(String folder, int col)//2 run times, 3 hits, 4 misses
	{
		List<String> files =FolderUtils.getAllFiles(folder);// get list of files in the folder
		List<String> results = new ArrayList<String>();//
		String cacheName="";
		boolean firstTime =true;
		if(files!=null)
		{
			List<String> data = new ArrayList<String>();
			for (String file : files) {
				cacheName+=file.substring(file.lastIndexOf("/")+1)+"\t";// extracts the file name is a header
				data = Read.readFromFile(file);// read the data recorded from this file as lines
				if(firstTime)//first time
				{
					results.add(0, "");
					firstTime=false;
				}
				for(int i=1;i< data.size();i++)// for each line
				{
					if(results.size()==i)
						results.add(i, "");
					String l = data.get(i); // get the line
					String value = l.split("\\s+")[col]; // split it and get the required column's data
					String oldValue= results.get(i);
					results.set(i, oldValue+value+"\t");// add it to the results in the same position concatenated with other results from previous files
				}
			}
			for(int i=0;i< data.size();i++)
			{
				results.set(i, results.get(i)+"\n");
			}
		}
		results.set(0, cacheName+"\n");

		return results;
	}
	//creates map of results' file type and a prefix name of it
	public static void initializeFilesNames()
	{
		resultsFiles.put(2, "baselineRunTimes");
		resultsFiles.put(3, "baselineHits");
		resultsFiles.put(4, "baselineMisses");
		resultsFiles.put(6, "approachRunTimes");
		resultsFiles.put(7, "approachHits");
		resultsFiles.put(8, "approachMisses");
	}
}
