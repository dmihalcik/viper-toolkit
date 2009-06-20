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
 * @author davidm@cfar.umd.edu
 * @since Jun 20, 2003
 */
public class SetDescType implements PropertyInterfacer {
	public void setValue(Object bean, Object value) {
		Config c = (Config) bean;
		int newVal = c.getDescType();
		if (value instanceof String) {
			newVal = Util.getDescType((String) value);
		} else if (value instanceof Number) {
			newVal = ((Number) value).intValue();
		}
		if (newVal != c.getDescType()) {
			Config.Edit editor = c.getEditor();
			if (editor != null) {
				editor.setDescType(newVal);
			} else {
				throw new PropertyAccessException();
			}
		}
	}

	public String getName() {
		return "DescType";
	}
	public Class getPropertyClass() {
		return int.class;
	}
	public boolean isReadable() {
		return true;
	}
	public boolean isWritable() {
		return true;
	}
	public Object getValue(Object bean) {
		return new Integer (((Config) bean).getDescType());
	}
	public boolean isWritableOn(Object bean) {
		if (bean instanceof Config) {
			Config b = (Config) bean;
			if (b.getEditor() == null) {
				return false;
			}
			if (b.getDescType() == Config.OBJECT) {
				for (int i = 0; i < b.getNumberOfChildren(); i++) {
					AttrConfig c = (AttrConfig) b.getChild(i);
					if (c.isDynamic()) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isReadableOn(Object bean) {
		return bean instanceof Config;
	}

}
