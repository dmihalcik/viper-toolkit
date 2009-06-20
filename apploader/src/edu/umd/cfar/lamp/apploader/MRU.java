/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Namespace schema for file operation histories (most recently used
 * lists).
 *  
 * Need to look at other history schemata before I 
 * write this, like Mozilla's, or something else...
 * 
 * I will probably use xsd:datatime stamps for the time.
 * 
 * For information, see the
 * <a href="http://viper-toolkit.sourceforge.net/owl/apploader/mru">namespace
 * document</a>.
 * 
 * @author davidm
 */
public class MRU {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader/mru#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader/mru#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * A class that all visited files should fall into.
	 */
	public static final Resource VisitedFile =
		ResourceFactory.createResource(uri + "VisitedFile");

	/**
	 * When this file was last viewed.
	 */
	public static final Property viewedOn =
		ResourceFactory.createProperty(uri + "viewedOn");
	
	/**
	 * The pretty name for the file.
	 */
	public static final Property name =
		ResourceFactory.createProperty(uri + "name");
}
