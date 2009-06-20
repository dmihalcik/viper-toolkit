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
 * @author davidm@cfar.umd.edu
 * @since Jun 19, 2003
 */
public class SetAttrValue implements PropertyInterfacer {
	public void setValue(Object bean, Object value) {
		try {
			if (bean instanceof AttrConfig) {
				((AttrConfig) bean).getEditor().setDefaultVal(value);
			} else {
				ConfigEditor.logger.fine("Can't set " + bean + " to " + value + " yet.");
			}
		} catch (BadAttributeDataException badx) {
			ConfigEditor.logger.warning(badx.getLocalizedMessage());
		}
	}

	public String getName() {
		return "AttrValue";
	}

	public Class getPropertyClass() {
		return Object.class;
	}

	public boolean isReadable() {
		return true;
	}
	public boolean isWritable() {
		return true;
	}

	public Object getValue(Object bean) {
		if (bean instanceof AttrConfig) {
			return ((AttrConfig) bean).getDefaultVal();
		} else {
			return null;
		}
	}

	public boolean isWritableOn(Object bean) {
		if (bean instanceof AttrConfig) {
			AttrConfig b = (AttrConfig) bean;
			return b.getEditor() != null;
		} else {
			return false;
		}
	}
	public boolean isReadableOn(Object bean) {
		return bean instanceof AttrConfig;
	}
}
