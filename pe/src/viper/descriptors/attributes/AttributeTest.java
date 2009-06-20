/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.descriptors.attributes;

import junit.framework.*;

/**
 * Unit tests for the attribute package.
 */
public class AttributeTest extends TestCase {
	
	/**
	 * Constructs a new test.
	 * @param name the test name
	 */
	public AttributeTest(String name) {
		super(name);
	}

	protected void setUp() {
		// Init stuff
	}

	/**
	 * Initializes the test suite.
	 * @return the test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new AttributeTest("testFrameSpan"));

		return suite;
	}

	void helpTestFrameSpan(FrameSpan temp) {
		for (int i = temp.beginning(); i <= temp.ending(); i++) {
			assertTrue(temp.containsFrame(i));
		}
	}

	void testFrameSpan() {
		int[] aA = {1, 32};
		FrameSpan test = new FrameSpan(aA[0], aA[1]);
		assertTrue(test.beginning() == aA[0]);
		assertTrue(test.ending() == aA[1]);
		for (int i = 0; i < 10; i++) {
			helpTestFrameSpan(test);
			test.shift(1);
		}
		helpTestFrameSpan(test);
		aA[0] += 10;
		aA[1] += 10;

		int[] bA = {55, 70};
		FrameSpan b = new FrameSpan(bA[0], bA[1]);
		test = test.union(b);
		int i = 0;
		while (i < aA[0])
			assertTrue(test + " should not contain " + i, !test
					.containsFrame(i++));
		while (i <= aA[1])
			assertTrue(test + " should contain " + i, test.containsFrame(i++));
		while (i < bA[0])
			assertTrue(test + " should not contain " + i, !test
					.containsFrame(i++));
		while (i <= bA[1])
			assertTrue(test + " should contain " + i, test.containsFrame(i++));
		while (i < bA[1] + 32)
			assertTrue(test + " should not contain " + i, !test
					.containsFrame(i++));

		int s = 20;
		test.shift(s);
		aA[0] += s;
		aA[1] += s;
		bA[0] += s;
		bA[1] += s;
		while (i < aA[0])
			assertTrue(test + " should not contain " + i, !test
					.containsFrame(i++));
		while (i <= aA[1])
			assertTrue(test + " should contain " + i, test.containsFrame(i++));
		while (i < bA[0])
			assertTrue(test + " should not contain " + i, !test
					.containsFrame(i++));
		while (i <= bA[1])
			assertTrue(test + " should contain " + i, test.containsFrame(i++));
		while (i < bA[1] + 32)
			assertTrue(test + " should not contain " + i, !test
					.containsFrame(i++));

	}
}