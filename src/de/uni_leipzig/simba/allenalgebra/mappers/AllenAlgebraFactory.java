package de.uni_leipzig.simba.allenalgebra.mappers;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.allenalgebra.mappers.complex.BeginAfterEndTogether;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.BeginBeforeEndTogether;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.BeginTogetherEndAfter;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.BeginTogetherEndBefore;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.DirectlyAfter;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.DirectlyBefore;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.During;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.DuringReverse;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.Equal;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.OverlapAfter;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.OverlapBefore;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.StrictlyAfter;
import de.uni_leipzig.simba.allenalgebra.mappers.complex.StrictlyBefore;

public class AllenAlgebraFactory {
    private static final Logger logger = Logger.getLogger(AllenAlgebraFactory.class.getName());
    

    public static AllenAlgebraMapper getMapper(String measure) {
	if(measure.equalsIgnoreCase("StrictlyBefore"))
	    return new StrictlyBefore();
	if(measure.equalsIgnoreCase("StrictlyAfter"))
	    return new StrictlyAfter();
	if(measure.equalsIgnoreCase("DirectlyBefore"))
	    return new DirectlyBefore();
	if(measure.equalsIgnoreCase("DirectlyAfter"))
	    return new DirectlyAfter();
	if(measure.equalsIgnoreCase("BeginAfterEndTogether"))
	    return new BeginAfterEndTogether();
	if(measure.equalsIgnoreCase("BeginBeforeEndTogether"))
	    return new BeginBeforeEndTogether();
	if(measure.equalsIgnoreCase("BeginTogetherEndBefore"))
	    return new BeginTogetherEndBefore();
	if(measure.equalsIgnoreCase("BeginTogetherEndAfter"))
	    return new BeginTogetherEndAfter();
	if(measure.equalsIgnoreCase("During"))
	    return new During();
	if(measure.equalsIgnoreCase("DuringReverse"))
	    return new DuringReverse();
	if(measure.equalsIgnoreCase("Equal"))
	    return new Equal();
	if(measure.equalsIgnoreCase("OverlapAfter"))
	    return new OverlapAfter();
	if(measure.equalsIgnoreCase("OverlapBefore"))
	    return new OverlapBefore();
	
	logger.error("Sorry, " + measure + " is not yet implemented. Exit with error ...");
	System.exit(1);
	return null;
    }

    public static ArrayList<AllenAlgebraMapper> getAllMappers(){
	ArrayList<AllenAlgebraMapper> mappers = new ArrayList<AllenAlgebraMapper>();
	mappers.add(new StrictlyBefore());
	mappers.add(new DirectlyBefore());

	return mappers;
    }
}
