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
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Cell editor for the value of attributes or the default value of attribute configs.
 */
public class AttrValueRenderer extends StringEditor {
	public ViperViewMediator mediator;
	
	public void setAttrValue(AttrValueWrapper params, Object val) {
		if (val == null) {
			setText("NULL");
		} else {
			setText(val.toString());
		}
	}
	public void setAttrValue(Node n) {
		if (n instanceof AttrConfig) {
			AttrConfig ac = (AttrConfig) n;
			setAttrValue(ac.getParams(), ac.getDefaultVal());
		} else if (n instanceof Attribute) {
			Attribute a = (Attribute) n;
			AttrConfig ac = a.getAttrConfig();
			Object val = a.getAttrValue();
			if (mediator != null && ac.isDynamic()) {
				Sourcefile sf = ((Sourcefile) a.getParent().getParent());
				String myFile = sf.getReferenceMedia().getSourcefileName();
				if (mediator.getFocalFile().equals(myFile)) {
					val = a.getAttrValueAtInstant(mediator.getMajorMoment());
				}
			}
			setAttrValue(ac.getParams(), val);
		}
	}
}
