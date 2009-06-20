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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.util.*;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Apr 6, 2005
 *
 * This class is almost exclusive based on edu.umd.cfar.lamp.viper.gui.data.obox.OboxInterpolator
 * NOTICE: It will always use the text, occlusions, and offsets from the "A" object (i.e. the first
 * one specified in the constructor) since there is no way to cleanly and logically interpolate
 * these values.
 * 
 * This class was written while sitting outside on McKeldin Mall during one of the first beautiful 
 * days of spring in 2005 :-)
 *
 */

public class TextlineInterpolator extends HelpInterpolate {
	private int nr(int theta) {
		theta = theta % 360;
		if (theta < 0) {
			theta += 360;
		}
		return theta;
	}

	public ArbitraryIndexList helpInterpolate(Object alpha, Object beta, long between) throws InterpolationException {
		OrientedBox a = ((TextlineModel) alpha).getObox();
		OrientedBox b = ((TextlineModel) beta).getObox();

		int[] A = new int[] {a.getX(), a.getY(), a.getWidth(), a.getHeight(), nr(a.getRotation())};
		int[] B = new int[] {b.getX(), b.getY(), b.getWidth(), b.getHeight(), nr(b.getRotation())};
		if (A[4] - B[4] > 180) {
			B[4] += 360;
		} else if (A[4] - B[4] < -180) {
			B[4] -= 360;
		}
		ArbitraryIndexList l = new LengthwiseEncodedList();
		Long i = new Long(0);
		while (i.longValue() < between) {
			int[] c = new int[5];
			for (int j = 0; j < 5; j++) {
				c[j] = (int) HelpInterpolate.oneNth(A[j], B[j], i.doubleValue(), between+1);
			}
			TextlineModel aTemp = (TextlineModel) alpha;
			ArrayList aOffsets = aTemp.getWordOffsets();
			ArrayList bOffsets = ((TextlineModel) beta).getWordOffsets();
			ArrayList interpOffsets = new ArrayList(); // interpolate the word offsets separately
			
			// can only interpolate properly when both arrays are of the same size (they will be in the vast majority of cases)
			if(aOffsets.size() == bOffsets.size()) {
				for(int h = 0; h < aOffsets.size(); h++) {
					int ia = ((Integer) aOffsets.get(h)).intValue();
					int ib = ((Integer) bOffsets.get(h)).intValue();
					int ic = (int) HelpInterpolate.oneNth(ia, ib, i.doubleValue(), between+1);
					interpOffsets.add(new Integer(ic));
				}
			// if the arrays cannot be interpolated because their sizes differ, use the one from alpha by default
			} else {
				interpOffsets = aOffsets;
			}
			
			l.set(i, i = new Long(i.longValue() + 1), new TextlineModel(c, aTemp.getText(null), aTemp.getOcclusions(), interpOffsets));
		}
		return l;
	}
}
