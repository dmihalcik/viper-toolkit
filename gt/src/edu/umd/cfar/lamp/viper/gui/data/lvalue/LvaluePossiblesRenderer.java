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

package edu.umd.cfar.lamp.viper.gui.data.lvalue;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 6, 2003
 */
public class LvaluePossiblesRenderer extends StringEditor {
	public void setAttrConfig(AttrConfig cfg) {
		setText(LvalueUtils.getListAsString(cfg));
	}
}
