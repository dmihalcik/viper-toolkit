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

import viper.api.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * JTable cell editor for the omega head model, which is basically an
 * oriented box with an ellipse inside. Currently, the parameters
 * are the center of the box, the width and height, and the orientation in 
 * degrees.
 * 
 * @author davidm
 */
public class OmegaCellEditor extends ViperDataFsmTextEditor {
	public OmegaCellEditor() {
		super(new IntegerListFSM(9));
	}

	public Object parse(String s) {
		if (null == s || "".equals(s)) {
			return null;
		} else {
			try {
				return OmegaHeadModel.valueOf(s);
			} catch (BadAttributeDataException badx) {
				badx.printStackTrace();
				return null;
			}
		}
	}
}
