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
 * Accessor of the 'isDynamic' method for Attribute Configs.
 */
public class SetAttrIsDynamic implements PropertyInterfacer {
	public void setValue(Object bean, Object value) {
		AttrConfig ac = (AttrConfig) bean;
		AttrConfig.Edit editor = ac.getEditor();
		if (editor != null) {
			editor.setDynamic(((Boolean) value).booleanValue());
		} else {
			throw new PropertyAccessException();
		}
	}

	public String getName() {
		return "Dynamic";
	}

	public Class getPropertyClass() {
		return Boolean.class;
	}

	public boolean isReadable() {
		return true;
	}
	public boolean isWritable() {
		return true;
	}

	public Object getValue(Object bean) {
		return ((AttrConfig) bean).isDynamic() ? Boolean.TRUE : Boolean.FALSE;
	}

	public boolean isWritableOn(Object bean) {
		if (bean instanceof AttrConfig) {
			AttrConfig b = (AttrConfig) bean;
			Config parent = (Config) b.getParent();
			return parent == null || parent.getDescType() == Config.OBJECT && b.getEditor() != null;
		} else {
			return false;
		}
	}
	public boolean isReadableOn(Object bean) {
		return bean instanceof AttrConfig;
	}
}
