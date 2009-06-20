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

package edu.umd.cfar.lamp.viper.gui.data.dvalue;

import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;


/**
 * @author davidm
 */
public class DvalueInterpolator extends HelpInterpolate {
	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		int a = ((Integer) alpha).intValue();
		int b = ((Integer) beta).intValue();

		ArbitraryIndexList l = new LengthwiseEncodedList();
		long i = 0;
		Long end = new Long(between);
		
		Integer prevNval = null; 
		while (i < between) {
			int nval = (int) HelpInterpolate.oneNth(a, b, i, between+1);
			if(prevNval == null || nval != prevNval.intValue()){
				l.set(new Long(i), end, new Integer(nval));
				prevNval = new Integer(nval);
			}
			++i;
		}
		return l;
	}
}
