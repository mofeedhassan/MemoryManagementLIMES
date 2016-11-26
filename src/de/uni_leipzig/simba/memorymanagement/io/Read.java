package de.uni_leipzig.simba.memorymanagement.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;




public class Read {
	public static Map<Object,Object> readConfiguration ()
	{
		Map<Object,Object> configurations = new HashMap<>();
		
		File propertiesFile = new File("lazytsp.properties");

		Parameters params = new Parameters();
		
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
				new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
		    .configure(params.fileBased()
		        .setFile(propertiesFile));
		
		try
		{
		    Configuration config = builder.getConfiguration();
		    

		}
		catch(ConfigurationException cex)
		{
		    // loading of the configuration file failed
		}
		return configurations;
	}
	
	public static List<String> readFromFile(String fileName)
	{
		List<String> lines = new ArrayList<String>();
		BufferedReader bufferedReader=null;
		try {
			bufferedReader = new BufferedReader(new FileReader(fileName));
			String line ="";
			while((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}   
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");                  
		}
		finally{try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}         
		}		return lines;
	}
	
	

}
