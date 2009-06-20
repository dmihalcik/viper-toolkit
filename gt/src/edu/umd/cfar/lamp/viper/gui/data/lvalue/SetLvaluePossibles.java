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

import java.awt.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * @author davidm@cfar.umd.edu
 */
public class SetLvaluePossibles implements PropertyInterfacer {
	public static final String URI = ViperData.ViPER_DATA_URI + "lvalue";
	
	public String getName() {
		return "possibles";
	}
	public Class getPropertyClass() {
		return String[].class;
	}
	public boolean isReadable() {
		return true;
	}
	public boolean isWritable() {
		return true;
	}
	private AttrConfig toAC(Object bean) {
		if (bean instanceof AttrConfig) {
			return (AttrConfig) bean;
		} else {
			return ((Attribute) bean).getAttrConfig();
		}
	}
	public void setValue(Object bean, Object value) {
		final AttrConfig ac = toAC(bean);
		String[] val = (String[]) value;
		final Lvalue param = new Lvalue(val);
		Runnable r = new Runnable() {
			public void run() {
				ac.getEditor().setAttrType(URI, param);
			}
		};
		if (ac instanceof EventfulNode && ((EventfulNode) ac).isNotifyingListeners()) {
			EventQueue.invokeLater(r);
		} else {
			r.run();
		}
	}

	public Object getValue(Object bean) {
		return ((Lvalue) toAC(bean).getParams()).getPossibles();
	}

	public boolean isWritableOn(Object bean) {
		return toAC(bean).getAttrType().equals(URI);
	}
	public boolean isReadableOn(Object bean) {
		return toAC(bean).getAttrType().equals(URI);
	}

}
