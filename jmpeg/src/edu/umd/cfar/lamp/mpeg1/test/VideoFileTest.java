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
import edu.umd.cfar.lamp.mpeg1.*;

/**
 * Tests the ability to load and seek through a file.
 * Subclass this, replacing the {@link #runMyTest(Mpeg1VideoStream)}
 * method with another one.
 */
public class VideoFileTest extends TestCase {
	private String dir;
	private String[] fnames;

	public VideoFileTest(String testName) {
		super(testName);
	}

	public static int getNumFrames(Mpeg1VideoStream stream) {
		try {
			return stream.getNumFrames();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (MpegException e) {
			throw new RuntimeException(e);
		}
	}

	public static long getVideoLengthInMillis(Mpeg1VideoStream stream) {
		try {
			long nframes = getNumFrames(stream);
			double fps = stream.getFrameRate();
			return (long) ((1000*(long)nframes) / fps);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (MpegException e) {
			throw new RuntimeException(e);
		}
	}

	private Mpeg1VideoStream getStream(String fname) throws MpegException, IOException {
		File f = new File (dir + fname);
		Mpeg1File mpegFile = new Mpeg1File(f);
		return mpegFile.getVideoStream();
	}

	public static void seekThroughStream(Mpeg1VideoStream stream)
		throws IOException, MpegException {
		int fcount = getNumFrames(stream);
		for (int i = 0; i < fcount; i++) {
			stream.seek(i);
		}
	}
	
	protected void runMyTest(Mpeg1VideoStream stream) throws MpegException, IOException {
		seekThroughStream(stream);
	}
	
	private void helpMeTest (String fname) throws MpegException, IOException {
		Mpeg1VideoStream stream = getStream(fname);
		runMyTest(stream);
	}
	
	public void testLampVideo() throws MpegException, IOException {
		helpMeTest("LAMP-Moving.mpeg");
	}
	
	public void testDuckAndCover() throws MpegException, IOException {
		helpMeTest("DuckAndCover.mpeg");
	}


	protected void setUp() throws Exception {
		dir = "samples/media/";
	}
}
