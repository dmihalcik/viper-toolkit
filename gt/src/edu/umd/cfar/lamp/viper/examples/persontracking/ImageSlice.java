/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/


package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.net.*;
import java.util.*;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * An image slice is a rectangular piece of a video frame,
 * defined by the video URI, the frame number and the bounds
 * of the clipping rectangle. It is immutable and canonicalized
 * and suitable for using as keys to an cache of such images, backed
 * by a DataPlayer. They implement a comparison operation that sorts
 * first by URI, then by Frame, then by location. This means
 * that carving a sorted list of image slices should give the DataPlayer
 * the least amount of work. However, this assumes that the dataplayer
 * is queued to the beginning and not much will change.
 * @author davidm
 */
public class ImageSlice extends Triple implements Comparable {
	private static Map imageKeyCache = new HashMap();
	public static ImageSlice createImageSlice(URI source, int frame, BoundingBox what) {
		final ImageSlice temp = new ImageSlice(source, frame, what);
		ImageSlice canon = (ImageSlice) imageKeyCache.get(temp);
		if (null == canon) {
			imageKeyCache.put(temp, temp);
			canon = temp;
		}
		return canon;
	}
	public static void cleanCache() {
		imageKeyCache.clear();
	}
	private ImageSlice(URI source, int frame, BoundingBox what) {
		super(source, new Integer(frame), what);
	}
	public URI getSource() {
		return (URI) getFirst();
	}
	
	public BoundingBox getBox() {
		return (BoundingBox) getThird();
	}
	
	public int getFrame() {
		return ((Integer) getSecond()).intValue();
	}

	private final int cmp (int a, int b) {
		if (a < b) {
			return -1;
		}
		if (a > b) {
			return 1;
		}
		return 0;
	}

	public int compareTo(Object b) {
		ImageSlice beta = (ImageSlice) b;
		int d;
		
		d = cmp(this.getFrame(), beta.getFrame());
		if (d != 0) {
			return d;
		}
		
		d = cmp(this.getBox().getX(), beta.getBox().getX());
		if (d != 0) {
			return d;
		}
		d = cmp(this.getBox().getY(), beta.getBox().getY());
		if (d != 0) {
			return d;
		}
		d = cmp(this.getBox().getWidth(), beta.getBox().getWidth());
		if (d != 0) {
			return d;
		}
		d = cmp(this.getBox().getHeight(), beta.getBox().getHeight());
		if (d != 0) {
			return d;
		}
		return 0;
	}
	public static void retain(Set allIds) {
		imageKeyCache.keySet().retainAll(allIds);
	}
}