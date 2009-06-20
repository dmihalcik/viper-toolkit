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

package edu.umd.cfar.lamp.viper.gui.core;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary for ViPER-GT preferences.
 */
public class GT {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/gt#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/gt#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * Resource that describes a file selection filter.
	 */
	public static final Resource ChoosableFile =
		ResourceFactory.createResource(uri + "ChoosableFile");

	/**
	 * A property that describes a possible file extension for a 
	 * {@link #ChoosableFile}.
	 */
	public static final Property extension =
		ResourceFactory.createProperty(uri + "extension");
	
	/**
	 * A property that describes a possible mime type for a 
	 * {@link #ChoosableFile}.
	 */
	public static final Property mimeType =
		ResourceFactory.createProperty(uri + "mimeType");

	/**
	 * A property for locating an alternate location for a 
	 * file with the subject resource uri.
	 */
	public static final Property fileLocation =
		ResourceFactory.createProperty(uri + "fileLocation");

	/**
	 * The icon for 'playback selected'. XXX: This is used in the 
	 * timeline, right?
	 */
	public static final Property playbackSelectedIcon =
		ResourceFactory.createProperty(uri + "playbackSelectedIcon");
	
	/**
	 * The icon for 'playback unselected'.
	 */
	public static final Property playbackUnselectedIcon =
		ResourceFactory.createProperty(uri + "playbackUnselectedIcon");
	
	/**
	 * The class used to interpolate a given data type.
	 * used in the form:
	 *   data:type gt:interpolatorClass [ {interpolator bean} ] .
	 */
	public static final Property interpolator =
		ResourceFactory.createProperty(uri + "interpolator");

	public static final Property visualNode = ResourceFactory.createProperty(uri + "visualNode");
	public static final Property visualEditor = ResourceFactory.createProperty(uri + "visualEditor");
	public static final Property visualCreator = ResourceFactory.createProperty(uri + "visualCreator");
}
