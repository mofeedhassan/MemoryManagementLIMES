/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic.hausdorff;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Same as a hypercube for polygons
 *
 * @author ngonga
 */
public class GeoSquare {

    public Set<Polygon> elements;

    public GeoSquare() {
        elements = new HashSet<Polygon>();
    }
    
    public String toString()
    {
        return elements.toString();
    }
    
    public long size(){
    	long size = 0;
    	for(Polygon p : elements){
    		size += p.size();
    	}
    	return size;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof GeoSquare))
            return false;

        GeoSquare o = (GeoSquare) obj;
        return elements.equals(o.elements);
	}
    
    
}
