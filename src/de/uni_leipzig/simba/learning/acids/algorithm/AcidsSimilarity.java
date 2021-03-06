package de.uni_leipzig.simba.learning.acids.algorithm;

import de.uni_leipzig.simba.learning.acids.utility.Statistics;

public abstract class AcidsSimilarity {

	protected AcidsProperty property;
	
	protected int index;
	
	private Statistics stats;

	private boolean computed = false;
	
	private double estimatedThreshold;
	
	public AcidsSimilarity(AcidsProperty property, int index) {
		this.property = property;
		this.index = index;
	}

	public AcidsProperty getProperty() {
		return property;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isComputed() {
		return computed ;
	}

	public void setComputed(boolean computed) {
		this.computed = computed;
	}

	public abstract AcidsFilter getFilter();

	public abstract String getName();
	
	public abstract int getDatatype();
	
	public double getEstimatedThreshold() {
		return estimatedThreshold;
	}

	public void setEstimatedThreshold(double estimatedThreshold) {
		this.estimatedThreshold = estimatedThreshold;
	}

	public abstract double getSimilarity(String a, String b);

	public Statistics getStats() {
		return stats;
	}

	public void setStats(Statistics stats) {
		this.stats = stats;
	}
	

}
