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

package edu.umd.cfar.lamp.viper.gui.data;

import viper.api.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 4, 2003
 */
public class AttrValueEditorFactory {
	private AttrConfig attributeConfig;
	private PrefsManager prefs;

	public Object getAttrValueEditor() {
		Resource r = null;
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			if (prefs != null) {
				r = prefs.model.getResource(attributeConfig.getAttrType());
			}
			if (r != null && r.hasProperty(PROPS.editor)) {
				RDFNode edNode = r.getProperty(PROPS.editor).getObject();
				try {
					return prefs.getCore().rdfNodeToValue(edNode,
							attributeConfig);
				} catch (PreferenceException e) {
					prefs.getLogger().warning(
							"Error while loading editor for type: "
									+ attributeConfig);
					e.printStackTrace();
					return null;
				}
			} else {
				Object val = attributeConfig.getDefaultVal();
				StringEditor ed = new StringEditor();
				ed.setText(val == null ? "NULL" : val.toString());
				return ed;
			}
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}
	/**
	 * @return
	 */
	public AttrConfig getAttributeConfig() {
		return attributeConfig;
	}

	/**
	 * @return
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * @param config
	 */
	public void setAttributeConfig(AttrConfig config) {
		attributeConfig = config;
	}

	/**
	 * @param manager
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
	}

}