package de.uni_leipzig.simba.memorymanagement.pathfinder;

import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.measures.string.TrigramMeasure;
import de.uni_leipzig.simba.memorymanagement.structure.DataType;

public class MeasureFactory {

	public static Measure createMeasure(DataType type)
	{
			
		if(type.equals(DataType.TRIGRAMS))
			return new TrigramMeasure();
		else 
			return new EuclideanMetric();
	}
}
