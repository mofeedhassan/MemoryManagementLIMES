package de.uni_leipzig.simba.learning.refinement.operator;

import java.util.Set;

import de.uni_leipzig.simba.specification.LinkSpec;

/**
 * Interface for all refinement operators based on link specifications.
 * A refinement operator maps a link spec to a set of link specs. 
 * For downward refinement operators those specs are more special. 
 * For upward refinement operators, those specs are more general.
 * 
 * @author Jens Lehmann
 *
 */
public interface RefinementOperator {

	/**
	 * Standard refinement operation.
	 * @param spec Link spec, which will be refined.
	 * @return Set of refined specs.
	 */
	public Set<LinkSpec> refine(LinkSpec spec);
	
}
