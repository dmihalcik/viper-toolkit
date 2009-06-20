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

package edu.umd.cfar.lamp.viper.gui.table;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Schema for table component's preferences. See the 
 * <a href="http://viper-toolkit.sourceforge.net/owl/gt/table">actual
 * schema declaration</a> for more information.
 */
public class TABLE {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/gt/table#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/gt/table#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}


	public static final Property enableTableIcon =
		ResourceFactory.createProperty(uri + "enableTableIcon");
	public static final Property lockedTableIcon =
		ResourceFactory.createProperty(uri + "lockedTableIcon");
	public static final Property disableTableIcon =
		ResourceFactory.createProperty(uri + "disableTableIcon");
	public static final Property describesDataType =
		ResourceFactory.createProperty(uri + "describesDataType");
	public static final Property attrProperty =
		ResourceFactory.createProperty(uri + "attrProperty");
}
