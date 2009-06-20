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


package edu.umd.cfar.lamp.viper.gui.remote;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary for remote control panel preferences.
 */
public class REMOTE {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/gt/remote#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/gt/remote#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	public static final Property playIcon =
		ResourceFactory.createProperty(uri + "playIcon");
	public static final Property pauseIcon =
		ResourceFactory.createProperty(uri + "pauseIcon");
}
