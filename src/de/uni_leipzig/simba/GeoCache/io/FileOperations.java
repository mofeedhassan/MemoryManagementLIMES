package de.uni_leipzig.simba.GeoCache.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileOperations {
private String file="";
public FileOperations(String lFile)
{
	file = lFile;
}

 public List<String> readFile()
 {
	 List<String> lines = new ArrayList<String>();
	 BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(getFile()));

			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return lines;
 }

public String getFile() {
	return file;
}
// set the file
public void setFile(String file) {
	this.file = file;
}
}
