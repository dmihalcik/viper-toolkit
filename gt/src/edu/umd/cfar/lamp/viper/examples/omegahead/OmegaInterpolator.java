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

import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Interpolates two omega heads, by interpolating the individual 
 * components (origin, size, and rotation).
 */
public class OmegaInterpolator extends HelpInterpolate {
	private int nr(int theta) {
		theta = theta % 360;
		if (theta < 0) {
			theta += 360;
		}
		return theta;
	}

	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		OmegaHeadModel a = (OmegaHeadModel) alpha;
		OmegaHeadModel b = (OmegaHeadModel) beta;

		int[] A = a.toArray(null);
		int[] B = b.toArray(null);
		B[7] = fixOrientationParameters(A[7], B[7]);
		B[8] = fixRotationParameters(A[8], B[8]);
		ArbitraryIndexList l = new LengthwiseEncodedList();
		Long i = new Long(0);
		while (i.longValue() < between) {
			int[] c = new int[A.length];
			for (int j = 0; j < A.length; j++) {
				c[j] = (int) HelpInterpolate.oneNth(A[j], B[j], i.doubleValue(), between+1);
			}
			l.set(i, i = new Long(i.longValue() + 1), new OmegaHeadModel(c));
		}
		return l;
	}

	/**
	 * Moves B so that it is within a rotation of A.
	 * @param A
	 * @param B
	 * @return B shifted to be in the same 360 degree arc as A
	 */
	private int fixOrientationParameters(int A, int B) {
		if (A - B > 180) {
			return B + 360;
		} else if (A - B < -180) {
			return B - 360;
		}
		return B;
	}
	/**
	 * Moves B so that it is within a rotation of A.
	 * @param A
	 * @param B
	 * @return B shifted to be in the same 360 degree arc as A
	 */
	private int fixRotationParameters(int A, int B) {
		if (A - B > 90) {
			return B + 180;
		} else if (A - B < -90) {
			return B - 180;
		}
		return B;
	}
}
