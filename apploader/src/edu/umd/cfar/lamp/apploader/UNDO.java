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
 * Namespace vocabulary for the undo history manager
 * preferences.
 */
public class UNDO {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader/undo#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader/undo#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * Describes an undo event.
	 */
	public static final Resource Describer =
		ResourceFactory.createResource(uri + "Describer");

	/**
	 * Gets the class of events this describes.
	 */
	public static final Property forEdit =
		ResourceFactory.createProperty(uri + "forEdit");
	
	/**
	 * Gets the text of the description. It is expected
	 * that the value resource be either text or a 
	 * localized list (a node with two properties, 
	 * lal:lang and undo:value).
	 */
	public static final Property text =
		ResourceFactory.createProperty(uri + "text");

	/**
	 * The text string or list.
	 */
	public static final Property value =
		ResourceFactory.createProperty(uri + "value");
}
