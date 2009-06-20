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
public class SetAttrName implements PropertyInterfacer {
	public void setValue(Object bean, Object value) {
		AttrConfig ac = (AttrConfig) bean;
		AttrConfig.Edit editor = ac.getEditor();
		if (editor != null) {
			editor.setAttrName((String) value);
		} else {
			throw new PropertyAccessException();
		}
	}

	public String getName() {
		return "AttrName";
	}

	public Class getPropertyClass() {
		return String.class;
	}

	public boolean isReadable() {
		return true;
	}
	public boolean isWritable() {
		return true;
	}

	public Object getValue(Object bean) {
		return ((AttrConfig) bean).getAttrName();
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
