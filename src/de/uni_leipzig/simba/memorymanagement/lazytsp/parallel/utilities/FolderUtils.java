package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author mofeed
 * It provides set of operations regarding an input folder
 * This folder contains data, contains resuts,..
 */
public class FolderUtils {
	public static List<String> getAllFiles(String folderPath)
	{
		List<String> files= new ArrayList<String>();
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("Cache")) {
				//	        System.out.println("File " + listOfFiles[i]);
				try {
					files.add(listOfFiles[i].getCanonicalPath().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return files;
	}
}
