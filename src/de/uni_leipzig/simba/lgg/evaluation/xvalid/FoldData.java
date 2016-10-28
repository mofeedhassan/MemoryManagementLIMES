package de.uni_leipzig.simba.lgg.evaluation.xvalid;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;

public class FoldData {
	public Mapping map = new Mapping();
	public Cache sourceCache = new HybridCache();
	public Cache targetCache = new HybridCache();
	public int size = -1;
	
	public FoldData(Mapping map, Cache sourceCache, Cache targetCache) {
		super();
		this.map = map;
		this.size = map.size;
		this.sourceCache = sourceCache;
		this.targetCache = targetCache;
	}

	public FoldData() {
	}

}
