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

package edu.umd.cfar.lamp.viper.examples.omegahead;

import java.util.*;

import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * An oriented ellipse with an attached line segment at the bottom.
 * The ellipse is centered at the 'centroid' point, is ellipseHeight
 * tall and ellipseWidth wide, and then oriented by the 'orientation'
 * parameter (in degrees). The line segmnet is drawn, by default, centered
 * with the ellipse and connected at the bottom. The line is 
 * lineLength long. lineOffset is how many pixels to move
 * the line segment center point to the right. yOffset moves the center of
 * the line down. lineOrientation rotates the line about the line
 * center point.
 * 
 * @author davidm
 */
public class OmegaHeadModel {
	private Pnt centroid;

	private int lineLength;

	private int lineOffset;

	private int ellipseHeight;

	private int ellipseWidth;

	private int orientation;
	
	private int lineOrientation;

	private int yLineOffset;

	/**
	 * @param centroid
	 * @param width
	 * @param height
	 * @param orientation
	 */
	public OmegaHeadModel(Pnt centroid, int lineLength, int lineOffset,
			int majorDiameter, int minorDiameter, int yLineOffset,
			int orientation, int lineOrientation) {
		super();
		assert centroid != null;
		this.centroid = centroid;
		this.lineLength = lineLength;
		this.lineOffset = lineOffset;
		this.ellipseHeight = majorDiameter;
		this.ellipseWidth = minorDiameter;
		this.yLineOffset = yLineOffset;
		this.orientation = orientation;
		this.lineOrientation = lineOrientation;
	}

	public OmegaHeadModel(int[] data) {
		this(new Pnt(data[0], data[1]), data[2], data[3], data[4], data[5],
				data[6], data[7], data[8]);
	}

	public int[] toArray(int[] A) {
		if (A == null) {
			A = new int[9];
		}
		A[0] = centroid.getX().intValue();
		A[1] = centroid.getY().intValue();
		A[2] = lineLength;
		A[3] = lineOffset;
		A[4] = ellipseHeight;
		A[5] = ellipseWidth;
		A[6] = yLineOffset;
		A[7] = orientation;
		A[8] = lineOrientation;
		return A;
	}

	public Pnt getCentroid() {
		return centroid;
	}

	public int getEllipseHeight() {
		return ellipseHeight;
	}

	public int getOrientation() {
		return orientation;
	}

	public int getLineOrientation() {
		return lineOrientation;
	}
	
	public int getLineLength() {
		return lineLength;
	}

	/**
	 * @param s
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Object valueOf(String s) {
		int[] data = new int[9];
		try {
			StringTokenizer st = new StringTokenizer(s);
			for (int i = 0; i < data.length; i++) {
				data[i] = Integer.parseInt(st.nextToken());
			}
			data[data.length - 1] = data[data.length - 1] % 360;
		} catch (NumberFormatException nfx) {
			throw new IllegalArgumentException("That wasn't a number - " + s);
		} catch (NoSuchElementException nsex) {
			throw new IllegalArgumentException("Not enough numbers in obox - "
					+ s);
		}
		return new OmegaHeadModel(data);
	}

	public String toString() {
		return centroid.getX() + " " + centroid.getY() + " " + lineLength + " "
				+ lineOffset + " " + ellipseHeight + " " + ellipseWidth + " "
				+ yLineOffset + " " + orientation + " " + lineOrientation;
	}

	/**
	 * @return Returns the lineOffset.
	 */
	public int getLineOffset() {
		return lineOffset;
	}

	/**
	 * @return Returns the minorDiameter.
	 */
	public int getEllipseWidth() {
		return ellipseWidth;
	}

	public int getYLineOffset() {
		return yLineOffset;
	}
}
