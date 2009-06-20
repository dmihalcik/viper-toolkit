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

package edu.umd.cfar.lamp.viper.gui.data.circle;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * @author davidm
 */
public class CircleInterpolator extends HelpInterpolate {
	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		Circle a = (Circle) alpha;
		Circle b = (Circle) beta;

		int[] A = new int[] {a.getCenter().getX().intValue(), a.getCenter().getY().intValue(), a.getRadius()};
		int[] B = new int[] {b.getCenter().getX().intValue(), b.getCenter().getY().intValue(), b.getRadius()};
		ArbitraryIndexList l = new LengthwiseEncodedList();
		long i = 0;
		Long end = new Long(between);
		int[] c = new int[3];
		while (i < between) {
			boolean changed = false;
			for (int j = 0; j < 3; j++) {
				int temp = (int) HelpInterpolate.oneNth(A[j], B[j], i, between+1);
				if(temp != c[j]){
					c[j] = temp;
					changed = true;
				}
			}
			if(changed)
				l.set(new Long(i), end, new Circle(c[0], c[1], c[2]));
			++i;
		}
		return l;
	}
}
