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
import viper.api.impl.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * Cell renderer for the 'Type' property of descriptor configuration objects.
 */
public class DescTypeRenderer extends StringEditor {
	
	public void setDescType(int x) {
		setText(Util.getDescType(x));
	}
	public void setDescType(Config cfg) {
		setDescType(cfg.getDescType());
	}
}
