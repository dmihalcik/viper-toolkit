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

package edu.umd.cfar.lamp.viper.gui.config;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * Cell renderer for the 'AttrType' property of attribute configuration objects.
 */
public class AttrTypeRenderer extends StringEditor {
	public void setAttrType(String t) {
		if (t.startsWith(ViperData.ViPER_DATA_URI)) {
			t = t.substring(ViperData.ViPER_DATA_URI.length());
		}
		setText(t);
	}
	public void setAttrType(AttrConfig cfg) {
		setAttrType(cfg.getAttrType());
	}
}
