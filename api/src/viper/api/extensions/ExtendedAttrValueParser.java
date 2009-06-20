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

import viper.api.*;
import viper.api.Node;

/**
 * In order for a datatype to support parsing and serialization, 
 * it must implement this interface. Note that, instead of 'set'
 * functions, it has 'change' methods, that create an altered 
 * copy instead of changing the value itself. The implementation
 * should treat these as immutable.
 */
public interface ExtendedAttrValueParser extends AttrValueParser {
	/**
	 * Get a new XML Element node describing additional
	 * rules constraining this node. For example, if the 
	 * polygon is open or closed, or the possible values 
	 * for an lvalue.
	 * @param root the DOM Document root to use to generate
	 *      the necessary elements.
	 * @param container TODO
	 * @return a new Element representing extended configuration
	 *          information
	 */
	public Element getXMLFormatConfig(Document root, Node container);
	
	/**
	 * Gets a copy of this attribute with the given config.
	 * @param el the config element
	 * @param container TODO
	 * @return the new value wrapper corresponding to the 
	 * config information, e.g. something that checks the 
	 * value of an enumeration (lvalue)
	 * @throws viper.api.BadAttributeDataException
	 */
	public AttrValueWrapper setConfig(Element el, Node container);
}
