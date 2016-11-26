package de.uni_leipzig.simba.memorymanagement.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Write {
	private static void wrtieToFile2(List<String> results,String fileName)
	{
		try{
			//	String data = " This content will append to the end of the file";

			File file =new File(fileName);

			//if file doesnt exists, then create it
			if(!file.exists()){
				file.createNewFile();
			}

			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(),true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (String data : results) {
				bufferWritter.write(data);
			}
			bufferWritter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public static void wrtieToFile(List<String> results,String fileName)
	{
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(fileName));

			for (String line : results) {
				bufferedWriter.write(line);
			}
		}
		catch(IOException ex) {
			System.out.println(
					"Error writing to file '"
							+ fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		finally{try {
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
	}
}
