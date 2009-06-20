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
 * @since Jun 20, 2003
 */
public class SetAttrType implements PropertyInterfacer {
	public static class AttrTypePair {
		public String type;
		public AttrValueWrapper param;
	}
	public void setValue(Object bean, Object value) {
		AttrConfig cfg = (AttrConfig) bean;
		String oldType = cfg.getAttrType();
		AttrConfig.Edit editor = cfg.getEditor();
		if (!value.equals(oldType) && editor != null) {
			AttrTypePair v = (AttrTypePair) value;
			editor.setAttrType(v.type, v.param);
		}
	}

	public String getName() {
		return "AttrType";
	}
	public Class getPropertyClass() {
		return AttrTypePair.class;
	}
	public boolean isReadable() {
		return true;
	}
	public boolean isWritable() {
		return true;
	}
	public Object getValue(Object bean) {
		AttrConfig b = (AttrConfig) bean;
		AttrTypePair p = new AttrTypePair();
		p.type = b.getAttrType();
		p.param = b.getParams();
		return p;
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
