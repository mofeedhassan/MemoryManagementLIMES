package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.structures;

import java.io.Serializable;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Edge;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Node;

public class CoarsenedEdge implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7213501105738210585L;
	String id="";
    int sourceId;
    int targetId;
    int weight;

    public int getSource() {
        return sourceId;
    }

    public int getTarget() {
        return targetId;
    }

    public int getWeight() {
        return weight;
    }
    public String getEdgeId(){
    	return id;
    }

    public CoarsenedEdge(int source, int target, int weight) {
        this.sourceId = source;
        this.targetId = target;
        id=sourceId+"-"+targetId;
        this.weight = weight;
    }
    
    public int hashCode(){

        return 31 * sourceId + targetId;
    }
    
    @Override
    public boolean equals(Object other) {
    	//return id.equals(((CoarsenedEdge)edge).getEdgeId());
    	if(other instanceof CoarsenedEdge)
		{
    		CoarsenedEdge o = (CoarsenedEdge) other;
			if((this.sourceId == o.sourceId) && (this.targetId == o.targetId) || (this.sourceId == o.targetId) && (this.targetId == o.sourceId))
				return true;
			return false;

		}
		return false;
    }

    //we use the inverse natural order. Thus, a higher weight leads to a lower rank
    public int compareTo(Object o) {
    	if (o instanceof CoarsenedEdge) {
    		CoarsenedEdge e = (CoarsenedEdge) o;
            if(e.getWeight() > weight) return +1;
            else if(e.getWeight() < weight) return -1;
            else return 0;
        }
        return 0;
    }
    
    public String toString()
    {
        return "(S:"+sourceId+", T:"+targetId+", W:"+weight+")";
    }
}
