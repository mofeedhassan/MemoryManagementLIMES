package de.uni_leipzig.simba.genetics.commands;

import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.terminal.Terminal;
/**
 * @deprecated
 * @author Klaus Lyko
 *
 */
public class ThresholdTerminal extends Terminal {

	public ThresholdTerminal(GPConfiguration a_conf, Class a_returnType,
			double a_minValue, double a_maxValue, boolean a_wholeNumbers,
			int a_subReturnType, boolean a_randomize)
			throws InvalidConfigurationException {
		super(a_conf, a_returnType, a_minValue, a_maxValue, a_wholeNumbers,
				a_subReturnType, a_randomize);
	}

	@Override
	public CommandGene applyMutation(int index, double percentage) throws InvalidConfigurationException {
		CommandGene mutation = super.applyMutation(index, percentage);
//		if
		return mutation;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    ThresholdTerminal other = (ThresholdTerminal) obj;
	    if(other.getReturnType() != this.getReturnType())
	    	return false;
	   	
	    double other_value = other.execute_double(null, 1, null);
	    double this_value = execute_double(null, 1, null);
	    if(Math.abs((other_value - this_value)) < 0.01d)
	    	return true;
	    return false;
	}
}
