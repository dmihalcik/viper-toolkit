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


package edu.umd.cfar.lamp.viper.gui.sourcefiles;

import com.hp.hpl.jena.rdf.model.*;

/**
 * RDF constants for setting preferences and performing
 * localization of the sourcefile editors.
 */
public class MEDIAMANAGEMENT {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/gt/mediamanagement#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/gt#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * The icon for 'remove current file'.
	 */
	public static final Property removeFileIcon =
		ResourceFactory.createProperty(uri + "removeFileIcon");
	
	/**
	 * The icon for 'add new file'.
	 */
	public static final Property addFileIcon =
		ResourceFactory.createProperty(uri + "addFileIcon");

	/**
	 * The icon for 'configure file link'.
	 */
	public static final Property linkFileIcon =
		ResourceFactory.createProperty(uri + "linkFileIcon");
}
