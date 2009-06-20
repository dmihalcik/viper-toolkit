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

import viper.api.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * Created on Apr 22, 2005
 * @author spikes51@umiacs.umd.edu
 */

public class TextLinkRenderer extends StringEditor {
	public void setAttrConfig(AttrConfig cfg) {
		AttributeWrapperTextline awt = (AttributeWrapperTextline) cfg.getParams();
		AttrConfig link = awt.getTextLink();
		if(link == null) {
			setText("");
		} else {
			setText(link.getAttrName());
		}
	}
}
