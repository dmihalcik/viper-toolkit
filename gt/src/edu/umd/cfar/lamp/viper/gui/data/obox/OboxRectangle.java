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

package edu.umd.cfar.lamp.viper.gui.data.obox;

import java.awt.geom.*;

/**
 * @author clin
 *
 * Primarily used to compute the edges of an oriented box
 * given x, y, width, height, and orientation in radians
 */
public class OboxRectangle {
	public Point2D p[] = new Point2D.Double[4];
	public double h, w; //  height, width
	public double o; //  orientation in radians

	public OboxRectangle(
		double x,
		double y,
		double width,
		double height,
		double radians) {
		h = height;
		w = width;
		o = radians;

		p[0] = new Point2D.Double(x, y);

		// Dummy values, since Mathematica expects p[1], p[2], p[3] to
		// be non-null.
		p[1] = new Point2D.Double(0, 0);
		p[2] = new Point2D.Double(0, 0);
		p[3] = new Point2D.Double(0, 0);

		// Calculate the 4 points from orientation
		rectanglePointsFromParams(this);

	}

	public static void rectanglePointsFromParams(OboxRectangle rec) {
		double sine, cosine;
		// Orientation is already in radians.  No need to convert.
		sine = Math.sin(rec.o);
		cosine = Math.cos(rec.o);

		rec.p[1].setLocation(
			rec.p[0].getX() + rec.w * cosine,
			rec.p[0].getY() - rec.w * sine);
		rec.p[2].setLocation(
			rec.p[0].getX() + rec.w * cosine + rec.h * sine,
			rec.p[0].getY() - rec.w * sine + rec.h * cosine);
		rec.p[3].setLocation(
			rec.p[0].getX() + rec.h * sine,
			rec.p[0].getY() + rec.h * cosine);

	}
}