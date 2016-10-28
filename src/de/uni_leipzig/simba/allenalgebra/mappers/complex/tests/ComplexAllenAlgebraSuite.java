package de.uni_leipzig.simba.allenalgebra.mappers.complex.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({ StrictlyBeforeTest.class, StrictlyAfterTest.class, DirectlyBeforeTest.class, DirectlyAfterTest.class, BeginAfterEndTogetherTest.class,
	BeginBeforeEndTogetherTest.class, BeginTogetherEndBeforeTest.class, BeginTogetherEndAfterTest.class, DuringTest.class, DuringReverseTest.class, EqualTest.class,
	OverlapAfterTest.class, OverlapBeforeTest.class })
public class ComplexAllenAlgebraSuite {

}
