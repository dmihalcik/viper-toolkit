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

import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary for ViPER-GT preferences.
 */
public class LOCAL_HISTORY {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/gt/localHistory#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/gt/localHistory#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}
	
	/**
	 * Indicates that there was an entry in the local history describing a
	 * given file.
	 */
	public static final Resource LocalHistoryEntry =
		ResourceFactory.createResource(uri + "LocalHistoryEntry");
	
	/**
	 * Resource for representing the file that has not yet been saved.
	 * Having only one is alright for now, as gtv4 doesn't support
	 * having multiple files open at a time.
	 */
	public static final Resource Untitled = 
		ResourceFactory.createResource(uri + "Untitled");
	
	/**
	 * The user file to which the entry refers. For the untitled
	 * document, refer to localHistory:Untitled
	 */
	public static final Property forFile =
		ResourceFactory.createProperty(uri + "forFile");
	/**
	 * The last time stamp in the local history. To determine
	 * if a file needs to be resurrected from the history, compare
	 * its timeStamp to the mru:viewedOn value for the file the
	 * mru list entry to which the local history entry refers. 
	 * If the entry is localHistory:Untitled, it suffices that
	 * <em>any</em> entry on the mru list comes after the timeStamp.
	 */
	public static final Property timeStamp =
		ResourceFactory.createProperty(uri + "timeStamp");

	/**
	 * Indicates the name of the local history entry on disk, 
	 * if it exists. 
	 */
	public static final Property savedAs =
		ResourceFactory.createProperty(uri + "savedAs");
	
	/**
	 * Get when entry saved, according to the localHistor:savedAs 
	 * property.
	 * @param localHistoryEntry the local history resource
	 * @return the date associated with the given resource
	 */
	public static XSDDateTime whenSaved(Resource localHistoryEntry) {
		Literal lit = localHistoryEntry.getProperty(LOCAL_HISTORY.savedAs).getLiteral();
		return (XSDDateTime) lit.getDatatype().parse(lit.getString());
	}
}
