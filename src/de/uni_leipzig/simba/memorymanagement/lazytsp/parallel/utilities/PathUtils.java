package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

/**
 * 
 * @author mofeed
 * It provides set of operations regarding a given path
 */
public class PathUtils {
	/*This method standardize the path by ensuring the addition of / at the end of it*/
	public static String standardizePath(String originalPath)
	{
		if(!originalPath.endsWith("/"))
			originalPath+="/";
		return originalPath;
	}
}
