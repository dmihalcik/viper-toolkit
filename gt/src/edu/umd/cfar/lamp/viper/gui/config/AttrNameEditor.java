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
 * Cell editor for the 'AttrName' property of attribute configuration objects.
 */
public class AttrNameEditor extends StringEditor {
	public void setText(AttrConfig ac) {
		super.setText(ac.getAttrName());
	}
}
