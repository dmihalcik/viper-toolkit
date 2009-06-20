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


package edu.umd.cfar.lamp.viper.gui.data.polygon;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Interpolates two polygons. There are a lot of parameters possible
 * for how this should be done, and I don't pretend to have the 
 * perfect 2d polygon morph here, so this should work better with
 * simpler polygons and between similar polygons.
 * 
 * XXX: for now, this just shifts the polygons
 */
public class PolygonInterpolator extends HelpInterpolate {

	/** @inheritDoc */
	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between)
			throws InterpolationException {
		Polygon a = (Polygon) alpha;
		Polygon b = (Polygon) beta;
		
		Pnt centerA = a.getCentroid();
		double Ax = centerA.getX().doubleValue();
		double Ay = centerA.getY().doubleValue();
		Pnt centerB = b.getCentroid();
		double Bx = centerB.getX().doubleValue();
		double By = centerB.getY().doubleValue();
		
		ArbitraryIndexList l = new LengthwiseEncodedList();
		Long i = new Long(0);
		long halfway = between/2;
		double moveX = Bx - Ax;
		double moveY = By - Ay;
		while (i.longValue() < halfway) {
			int x = (int) HelpInterpolate.oneNth(0, moveX, i.doubleValue(), between+1);
			int y = (int) HelpInterpolate.oneNth(0, moveY, i.doubleValue(), between+1);
			l.set(i, i = new Long(i.longValue() + 1), a.shift(x, y));
		}
		moveX = Ax - Bx;
		moveY = Ay - By;
		while (i.longValue() < between) {
			int x = (int) HelpInterpolate.oneNth(moveX, 0, i.doubleValue(), between+1);
			int y = (int) HelpInterpolate.oneNth(moveY, 0, i.doubleValue(), between+1);
			l.set(i, i = new Long(i.longValue() + 1), b.shift(x, y));
		}
		return l;
	}
}
