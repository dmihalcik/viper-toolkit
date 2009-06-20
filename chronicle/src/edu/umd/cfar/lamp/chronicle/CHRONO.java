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

package edu.umd.cfar.lamp.chronicle;

import com.hp.hpl.jena.rdf.model.*;

/**
 */
public class CHRONO {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/gt/chronicle#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/gt/chronicle#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}
	
	/**
	 * Indicates the icon to be used for expand emblems, if any.
	 */
	public static final Property expandIcon =
		ResourceFactory.createProperty(uri + "expandIcon");
	/**
	 * Indicates the icon to be use for collapse emblems, if any.
	 */
	public static final Property contractIcon =
		ResourceFactory.createProperty(uri + "contractIcon");

	/**
	 * Indicates what segment factory to use for certain things.
	 */
	public static final Property segmentFactory = 
		ResourceFactory.createProperty(uri + "segmentFactory");
}
