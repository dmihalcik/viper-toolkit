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
 */
public class LvaluePossiblesEditor extends StringEditor {
	public void setNode(Node n) {
		AttrConfig c;
		if (n instanceof AttrConfig) {
			c = (AttrConfig) n;
		} else {
			c = ((Attribute) n).getAttrConfig();
		}
		setText(LvalueUtils.getListAsString(c));
	}
	public Object getCellEditorValue() {
		return LvalueUtils.parseString((String) super.getCellEditorValue());
	}
}
