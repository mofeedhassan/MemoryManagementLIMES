package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

import java.util.HashMap;
import java.util.Map;

public class ResultsRuntimes {
	Map<String,Long>  runtimes = new HashMap<>();
	long phaseStartTime=0;
	
	public void setExperiementStart(long time)
	{runtimes.put("start", time);}
	public void setExperiementEnd(long time)
	{runtimes.put("end", time);}
	
	public void setPhaseStart(long time)
	{phaseStartTime = time;}
	
	public void setPhaseInterval(String phase,long time)
	{runtimes.put(phase, time-phaseStartTime);}
	
	public String getRunInfo()
	{ 
		String title = "indexing\tgraph\tclustering\tpath\tplan\tparallel\tTotal\n";
		String values = runtimes.get("indexing")+"\t"+runtimes.get("graph")+"\t"+runtimes.get("clustering")+"\t"+runtimes.get("path")+"\t"+
		                runtimes.get("plan")+"\t"+runtimes.get("parallel")+"\t"+(runtimes.get("end")-runtimes.get("start"));
		return title+values;
	}
}
