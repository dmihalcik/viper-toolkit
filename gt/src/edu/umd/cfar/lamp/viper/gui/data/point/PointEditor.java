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

package edu.umd.cfar.lamp.viper.gui.data.point;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 8, 2003
 */
public class PointEditor extends ViperDataFsmTextEditor {
	public PointEditor() {
		super(new IntegerListFSM(2));
	}

	public Object parse(String s) {
		if (null == s || "".equals(s)) {
			return null;
		} else {
			try {
				return Pnt.valueOf(s);
			} catch (BadAttributeDataException badx) {
				badx.printStackTrace();
				return null;
			}
		}
	}
}
