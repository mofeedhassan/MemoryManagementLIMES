package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Sorters {
	
	
	public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map, boolean ascending) 
	   {

		   List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		   Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() 
		   {
	           @Override
	           public int compare(Entry<K,V> e1, Entry<K,V> e2) 
	           {
	        	   if(ascending)
	        		   return e1.getValue().compareTo(e2.getValue());
	        	   else
	        		   return e2.getValue().compareTo(e1.getValue()); 
	           }
		   }
				   );

		   return sortedEntries;
	   }
	
	   public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) 
	   {

		   List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		   Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() 
		   {
	           @Override
	           public int compare(Entry<K,V> e1, Entry<K,V> e2) 
	           {
	               return e1.getValue().compareTo(e2.getValue());
	           }
		   }
				   );

		   return sortedEntries;
	   }
	   
	   
	   public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValuesDesc(Map<K,V> map) 
	   {

		   List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		   Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() 
		   {
	           @Override
	           public int compare(Entry<K,V> e1, Entry<K,V> e2) 
	           {
	               return e2.getValue().compareTo(e1.getValue());
	           }
		   }
				   );

		   return sortedEntries;
	   }
}
