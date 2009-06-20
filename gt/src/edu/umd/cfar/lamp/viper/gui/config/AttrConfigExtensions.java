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

import java.util.*;

import viper.api.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * Extended properties for attribute configurations.
 * This includes the list of lvalue items, for example.
 */
public class AttrConfigExtensions extends DescriberBasedProperties {
	public AttrConfigExtensions() {
		super();
	}

	public AttrConfig getAttrConfig() {
		return (AttrConfig) getObject();
	}


	/**
	 * Set the attribute type.
	 * @param config The new type
	 */
	public void setAttrConfig(AttrConfig config) {
		setObject(config);
	}
	
	public Collection getAllDescribers() {
		if (null == getPrefs()) {
			return Collections.EMPTY_SET;
		} else {
			getPrefs().model.enterCriticalSection(ModelLock.READ);
			try {
				String dataTypeUri = getAttrConfig().getAttrType();
				Resource r = getPrefs().model.getResource(dataTypeUri);
				return Collections.singleton(r);
			} finally {
				getPrefs().model.leaveCriticalSection() ;
			}
		}
	}
}
