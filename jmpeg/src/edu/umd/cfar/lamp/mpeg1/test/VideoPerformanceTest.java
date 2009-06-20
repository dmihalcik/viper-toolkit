/***************************************
 *            ViPER-MPEG               *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *             MPEG-1 Decoder          *
 * Distributed under the LGPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.mpeg1.test;

import java.io.*;

import junit.framework.*;
import junit.textui.*;
import edu.umd.cfar.lamp.mpeg1.*;

/**
 * Extended tests for performance evaluation
 */
public class VideoPerformanceTest extends VideoFileTest {
	public VideoPerformanceTest(String testName) {
		super(testName);
	}

	protected void runMyTest(Mpeg1VideoStream stream) throws MpegException, IOException {
		long timeout = getVideoLengthInMillis(stream);
		long startTime = System.currentTimeMillis();
		seekThroughStream(stream);
		long totalTime = System.currentTimeMillis() - startTime;
		double fps = (getNumFrames(stream) * 1000.0) / totalTime;
		System.out.println("Averaged " + fps + "fpms");
		assertTrue(
			"Video didn't decode close enough to real time. Took "
				+ totalTime
				+ " ms, but should be less than "
				+ timeout
				+ " ms.",
			totalTime < timeout);
	}
	public static void main(String args[]) {
		TestSuite suite = new TestSuite();
		suite.addTest(new VideoPerformanceTest("testLampVideo"));
		suite.addTest(new VideoPerformanceTest("testDuckAndCover"));
		TestRunner.run(suite);
	}
}
