package de.uni_leipzig.simba.keydiscovery;

import java.sql.SQLException;
import java.util.Set;

import de.uni_leipzig.simba.keydiscovery.model.CandidateNode;
import de.uni_leipzig.simba.keydiscovery.rockerone.Rocker;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RockerKeyDiscovery implements IKeyDiscovery {
	
	private Rocker rocker;

	@Override
	public void init(String datasetName, String inputFile, String classname,
			boolean oneKey, double coverage, boolean fastSearch) {
		try {
			rocker = new Rocker(datasetName, inputFile, classname, oneKey, fastSearch, coverage);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		rocker.run();
	}

	@Override
	public Set<CandidateNode> getResults() {
		return rocker.getKeys();
	}
	
	/**
	 * @return The algorithm object which provides insights from the key discovery task.
	 */
	public Rocker getRockerObject() {
		return rocker;
	}

}
