package de.uni_leipzig.simba.memorymanagement.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

public class LazyTspConfigReader {
	private static final LazyTspConfigReader instance = new LazyTspConfigReader();
	private final Properties configProp = new Properties();

	private LazyTspConfigReader(){loadParams();}
	public static LazyTspConfigReader getInstance(){return instance;} 
	
	public void loadParams() {
	    InputStream is = null;
	 
	    // Try loading from the current directory
	    try {
	        File f = new File("lazytsp.properties");
	        is = new FileInputStream( f );
	    }
	    catch ( Exception e ) { is = null; }
	 
	    try {
	        if ( is == null ) {
	            // Try loading from classpath
	            is = getClass().getResourceAsStream("lazytsp.properties");
	        }
	 
	        // Try loading properties from the file (if found)
	        configProp.load( is );
	    }
	    catch ( Exception e ) { }
	 

	}
	
/*	public int saveParamChanges() {
	    try {
	        Properties props = new Properties();
	        props.setProperty("ServerAddress", "ha");
	        props.setProperty("ServerPort", ""+"ha");
	        props.setProperty("ThreadCount", ""+"ha");
	        File f = new File("server.properties");
	        OutputStream out = new FileOutputStream( f );
	        props.store(out, "This is an optional header comment string");
	    }
	    catch (Exception e ) {
	        e.printStackTrace();
	    }
	    return 0;
	}*/
	
	public String getProperty(String key){
		return instance.configProp.getProperty(key);
	}

	public Set<String> getAllPropertyNames(){
		return instance.configProp.stringPropertyNames();
	}

	public boolean containsKey(String key){
		return instance.configProp.containsKey(key);
	}
	
}
