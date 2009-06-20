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

package edu.umd.cfar.lamp.viper.gui.data;

import java.util.*;

import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * @author davidm
 */
public abstract class HelpInterpolate implements Interpolator {
	public ArbitraryIndexList interpolate(Object[] Z, long[] length, String method)
		throws InterpolationException {
		assert Z.length == length.length + 1;

		int totalSize = Z.length;
		for (int i = 0; i < length.length; i++) {
			totalSize += length[i];
		}
		ArbitraryIndexList bba = new LengthwiseEncodedList();
		Long count = new Long(0);
		for (int i = 0; i < length.length; i++) {
			Object a = Z[i];
			Object b = Z[i + 1];
			boolean prop = false;
			if (a == null) {
				a = b;
				prop = true;
			} else if (b == null) {
				b = a;
				prop = true;
			}
			
			bba.set(count, count = new Long(count.longValue()+1), a);
			long finalCount = count.longValue() + length[i];
			if (prop) {
				if (count.intValue() < finalCount) {
					bba.set(count, new Long(finalCount), a);
				}
			} else {
				Iterator iter = helpInterpolate(a, b, length[i]).iterator();
				while (iter.hasNext()) {
					DynamicValue dv = (DynamicValue) iter.next();
					Long start = (Long) dv.getStart();
					Long end = (Long) dv.getEnd();
					if (count.intValue() != 0) {
						start = new Long(start.longValue() + count.longValue());
						end = new Long(end.longValue() + count.longValue());
					}
					bba.set(start, end, dv.getValue());
				}
			}
			count = new Long(finalCount);
		}
//		bba[bba.length - 1] = Z[Z.length - 1];
		return bba;
	}

	/**
	 * Return the <code>between</code> values between a and b.
	 * @param a
	 * @param b
	 * @param between
	 * @return
	 * @throws InterpolationException
	 */
	public abstract ArbitraryIndexList helpInterpolate(Object a, Object b, long between)
		throws InterpolationException;
	public static double oneNth(double a, double b, double where, double n) {
		return a - ((where + 1) * (a - b)) / n;
	}
}
