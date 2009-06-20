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
 * Namespace schema for loading RDF from command line arguments.
 * 
 * For information, see the
 * <a href="http://viper-toolkit.sourceforge.net/owl/apploader/prefs#">namespace
 * document</a>.
 * 
 * @author davidm
 */
public class PREFS {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader/prefs#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * A preference trigger - a description of how a pair
	 * 'preference value' should be applied when passed as a 
	 * command line argument or as a property.
	 */
	public static final Resource PreferenceTrigger =
		ResourceFactory.createResource(uri + "PreferenceTrigger");

	/**
	 * A preference flag - a boolean value that is 
	 * set to on by its existence. An example would be
	 * the '-h' and '-v' flags at the command line
	 */
	public static final Resource PreferenceFlag =
		ResourceFactory.createResource(uri + "PreferenceFlag");

	/**
	 * The long name: the property name or the name
	 * passed using two hyphens, e.g. '--version'.
	 */
	public static final Property longName =
		ResourceFactory.createProperty(uri + "longName");

	/**
	 * The short name: the name
	 * passed using one hyphens, e.g. '-'.
	 */
	public static final Property abbr =
		ResourceFactory.createProperty(uri + "abbr");

	/**
	 * The method that the trigger or flag invokes.
	 */
	public static final Property invokes =
		ResourceFactory.createProperty(uri + "invokes");

	/**
	 * The value that the trigger or flag asserts.
	 */
	public static final Property inserts =
		ResourceFactory.createProperty(uri + "inserts");
	
	/**
	 * Indicates that the getProperties will use this file to find
	 * files.
	 */
	public static final Property loadsProps =
		ResourceFactory.createProperty(uri + "loadsProps");
	
	/**
	 * Indicates where to save changes to the properties.
	 * If there is only one loadsProps property, that is assumed
	 * to also be the savesProps location.
	 */
	public static final Property savesProps =
		ResourceFactory.createProperty(uri + "savesProps");
}
