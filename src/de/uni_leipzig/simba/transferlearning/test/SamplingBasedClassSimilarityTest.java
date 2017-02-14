package de.uni_leipzig.simba.transferlearning.test;
import static org.junit.Assert.*;

import java.lang.reflect.*;

import org.junit.Test;

import de.uni_leipzig.simba.transferlearning.transfer.classes.SamplingBasedClassSimilarity;
import de.uni_leipzig.simba.transferlearning.transfer.config.ConfigReader;
import de.uni_leipzig.simba.transferlearning.transfer.config.Configuration;

public class SamplingBasedClassSimilarityTest {

	@Test
	public void test() {
		//getPropertiesValuesTest("http://dbpedia.org/ontology/Airport", "http://dbpedia.org/sparql/", 100, true);
		ConfigReader cr =new ConfigReader();
		Configuration conf  = cr.readLimesConfig("/media/mofeed/A0621C46621C24164/TransferLearningCollections/00-AllSpecsCashing/FinalWorkingSpecs2017/dbpedia-linkedgeodata-airport/spec.xml");
		getPropertiesValuesTest("http://dbpedia.org/ontology/Airport",conf, 100, true);

	}

	public void getPropertiesValuesTest(String c, Configuration conf, int size, boolean isSource)
	{
			SamplingBasedClassSimilarity.getPropertyValues(c, size,conf, isSource);
	}

}
