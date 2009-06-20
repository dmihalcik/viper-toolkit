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

import java.awt.*;

import viper.api.*;
import viper.api.extensions.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * Created on Apr 22, 2005
 * @author spikes51@umiacs.umd.edu
 */

public class SetTextLink implements PropertyInterfacer {
	public static final String URI = ViperData.ViPER_DATA_URI + "textline";
	
	public String getName() {
		return "textLink";
	}
	
	public Class getPropertyClass() {
		return AttrConfig.class;
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
		final AttrConfig val = (AttrConfig) value;
		final AttributeWrapperTextline param = new AttributeWrapperTextline();
		param.setTextLink(val);
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
		return ((AttributeWrapperTextline) toAC(bean).getParams()).getTextLink();
	}

	public boolean isWritableOn(Object bean) {
		return toAC(bean).getAttrType().equals(URI);
	}
	public boolean isReadableOn(Object bean) {
		return toAC(bean).getAttrType().equals(URI);
	}

}
