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

package viper.api.extensions;

import org.w3c.dom.*;

import viper.api.Node;


/**
 * If a class implements this, it means it may be converted to 
 * XML.
 */
public interface XmlVisibleNode extends Node {
	/**
	 * Get a new XML Element node representing this
	 * Node.
	 * @param root the DOM Document root to use to generate
	 *      the necessary elements.
	 * @return a new Element representing this Node
	 */
	public Element getXMLFormat(Document root);
}
