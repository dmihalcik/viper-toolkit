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
 * Cell renderer for the 'isDynamic' property of attribute configuration objects.
 */
public class AttrIsDynamicRenderer extends StringEditor {
	public void setAttrConfig(AttrConfig cfg) {
		setText(Boolean.toString(cfg.isDynamic()));
	}
}
