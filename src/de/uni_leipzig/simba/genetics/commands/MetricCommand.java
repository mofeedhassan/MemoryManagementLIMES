package de.uni_leipzig.simba.genetics.commands;

import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.IMutateable;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.ProgramChromosome;
import org.jgap.util.CloneException;
import org.jgap.util.ICloneable;

import de.uni_leipzig.simba.genetics.core.ExpressionProblem.ResourceTerminalType;
import de.uni_leipzig.simba.genetics.core.Metric;
/**
 * This is the top of the tree, e.g. root of every gene for link specification learning.
 * @author Klaus Lyko
 *
 */
/**
 * GP Command for a Link Specification. Either atomic or a complex
 * similarity measure. Either case, it is basically a chromosome expecting two children:
 * a metric (String) and a threshold (double).
 * @author Klaus Lyko
 *
 */
public class MetricCommand extends CommandGene 
implements IMutateable, ICloneable{
	
	/** */
	private static final long serialVersionUID = -5555554301086427498L;

	public MetricCommand(final GPConfiguration config) throws InvalidConfigurationException {
		this(config, Metric.class);
	}
	
	public MetricCommand(final GPConfiguration config, Class returnType) throws InvalidConfigurationException {
		super(config, 2, returnType, 
				88
				,new int[] {1, ResourceTerminalType.GOBALTHRESHOLD.intValue()}
		);
	}
	
	public Class getChildType(IGPProgram a_ind, int a_chromNum) {
		if(a_chromNum == 0)
			return String.class;
		else
			return CommandGene.IntegerClass;
	}
	@Override
	public String toString() {
		return "Metric(&1, &2)";
	}

	public CommandGene applyMutation(int a_index, double a_percentage)
			throws InvalidConfigurationException {
		return this;
	}

	@Override
	public Object clone() {
	    try {
	      MetricCommand result =  new MetricCommand(getGPConfiguration(), this.getReturnType());
	      return result;
	    } catch (Throwable t) {
	      throw new CloneException(t);
	    }
	}
	
	public Metric execute_object(ProgramChromosome a_chrom, int a_n, Object[] args) {
		String expr =  AddMetric.removeThresholdFromMeasure((String)a_chrom.execute_object(a_n, 0, args));
		double threshold = (double)a_chrom.execute_int(a_n, 1, args);
		if(threshold > 1 && threshold <=100)
			threshold = threshold/100;
    
		//@FIXME really necessary?
		if(expr.startsWith("euclidean") && threshold < 0.4d) {
			threshold = 0.4d;
		}
		return new Metric(expr, threshold);
	}
		
}
