/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.memorymanagement.Index.graphclustering;

import java.io.Serializable;

import de.uni_leipzig.simba.memorymanagement.indexing.AbstractIndexItem;
import de.uni_leipzig.simba.memorymanagement.indexing.IndexItem;

/**
 *
 * @author ngonga
 */
public class Node implements Comparable,Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1651457970878326148L;
	IndexItem item;

    public Node(IndexItem ii) {
        item = ii;
    }

    public int getWeight() {
        return item.getSize();
    }

    public String toString() {
        return item.toString();
    }
    
    public IndexItem getItem()
    {
        return item;
    }

    public int compareTo(Object o) {
        if (o instanceof Node) {
            if(item.getId().equals(((Node) o).item.getId()))
                     {
                return 0;
            }            
        }
        return -1;
    }
}
