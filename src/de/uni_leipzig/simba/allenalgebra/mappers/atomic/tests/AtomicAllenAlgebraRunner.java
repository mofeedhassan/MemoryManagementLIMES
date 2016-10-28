package de.uni_leipzig.simba.allenalgebra.mappers.atomic.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(Suite.class)
@SuiteClasses({AtomicAllenAlgebraSuite.class})
public class AtomicAllenAlgebraRunner {

    public void main() {
	Result result = JUnitCore.runClasses(AtomicAllenAlgebraSuite.class);
	System.out.println(result.wasSuccessful());
	
    }

}
