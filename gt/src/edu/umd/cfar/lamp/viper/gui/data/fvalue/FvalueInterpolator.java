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

package edu.umd.cfar.lamp.viper.gui.data.fvalue;

import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;


/**
 * @author davidm
 */
public class FvalueInterpolator extends HelpInterpolate {
	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		double a = ((Double) alpha).doubleValue();
		double b = ((Double) beta).doubleValue();

		ArbitraryIndexList l = new LengthwiseEncodedList();
		long i = 0;
		Long end = new Long(between);
		
		Double lastNval = null;
		while (i < between) {
			double nval = HelpInterpolate.oneNth(a, b, i, between+1);
			if(lastNval == null || nval != lastNval.doubleValue())
				l.set(new Long(i), end, new Double(nval));
			++i;
		}
		return l;
	}
}
