package de.uni_leipzig.simba.allenalgebra.mappers.complex.tests;


import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ComplexAllenAlgebraSuite.class})
public class ComplexAllenAlgebraRunner {

    public void main() {
	Result result = JUnitCore.runClasses(ComplexAllenAlgebraSuite.class);
	System.out.println(result.wasSuccessful());
	
    }

}
