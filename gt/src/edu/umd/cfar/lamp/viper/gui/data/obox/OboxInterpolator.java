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

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Interpolates two oboxes, by interpolating the individual 
 * components (origin, size, and rotation).
 */
public class OboxInterpolator extends HelpInterpolate {
	private int nr(int theta) {
		theta = theta % 360;
		if (theta < 0) {
			theta += 360;
		}
		return theta;
	}

	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		OrientedBox a = (OrientedBox) alpha;
		OrientedBox b = (OrientedBox) beta;

		int[] A = new int[] {a.getX(), a.getY(), a.getWidth(), a.getHeight(), nr(a.getRotation())};
		int[] B = new int[] {b.getX(), b.getY(), b.getWidth(), b.getHeight(), nr(b.getRotation())};
		if (A[4] - B[4] > 180) {
			B[4] += 360;
		} else if (A[4] - B[4] < -180) {
			B[4] -= 360;
		}
		ArbitraryIndexList l = new LengthwiseEncodedList();
		Long end = new Long(between);
		
		long i = 0;
		int[] c = new int[5];
		while (i < between) {
			boolean changed = false;

			for (int j = 0; j < 5; j++) {
				int temp = (int) HelpInterpolate.oneNth(A[j], B[j], i, between+1);
				if(temp != c[j]){
					c[j] = temp;
					changed = true;
				}
			}
			if(changed)
				l.set(new Long(i), end, new OrientedBox(c));
			++i;
		}
		return l;
	}
}
