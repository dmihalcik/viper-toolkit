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
 * @since Jun 12, 2003
 */
public class SetDescName implements PropertyInterfacer {
	public void setValue(Object bean, Object value) {
		Config cfg = (Config) bean;
		Config.Edit editor = cfg.getEditor();
		if (editor != null) {
			editor.setDescName((String) value);
		} else {
			throw new PropertyAccessException();
		}
	}
	public String getName() {
		return "DescName";
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
		return ((Config) bean).getDescName();
	}
	public boolean isWritableOn(Object bean) {
		if (bean instanceof Config) {
			Config b = (Config) bean;
			return b.getEditor() != null;
		} else {
			return false;
		}
	}
	public boolean isReadableOn(Object bean) {
		return bean instanceof Config;
	}
}
