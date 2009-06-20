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

package edu.umd.cfar.lamp.viper.gui.data.bbox;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;


/**
 */
public class BboxInterpolator extends HelpInterpolate {
	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		BoundingBox a = (BoundingBox) alpha;
		BoundingBox b = (BoundingBox) beta;

		int[] A = new int[] {a.getX(), a.getY(), a.getWidth(), a.getHeight()};
		int[] B = new int[] {b.getX(), b.getY(), b.getWidth(), b.getHeight()};
		ArbitraryIndexList l = new LengthwiseEncodedList();
		long i = 0;
		Long end = new Long(between);
		int[] c = new int[4];
		while (i < between) {
			boolean changed = false;
			for (int j = 0; j < 4; j++) {
				int temp = (int) HelpInterpolate.oneNth(A[j], B[j], i, between+1);
				if(c[j] != temp){
					c[j] = temp;
					changed = true;
				}
			}
			if(changed)
				l.set(new Long(i), end, new BoundingBox(c));
			++i;
		}
		return l;
	}
}
