/**
 * 
 */
package de.uni_leipzig.simba.memorymanagement.parallel;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_leipzig.simba.memorymanagement.io.LazyTspConfigReader;

/**
 * @author mofeed
 *
 */
public class LazyTspConfigReaderTest {

	@Test
	public void testLazyTspConfigReader() {
		assertTrue(LazyTspConfigReader.getInstance().getAllPropertyNames().size() > 0);
		assertTrue(LazyTspConfigReader.getInstance().getAllPropertyNames().size() == 17);
	}

}
