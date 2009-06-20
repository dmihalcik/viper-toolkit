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
import viper.api.time.*;

/**
 * In order for a datatype to support parsing and serialization, 
 * it must implement this interface. Note that, instead of 'set'
 * functions, it has 'change' methods, that create an altered 
 * copy instead of changing the value itself. The implementation
 * should treat these as immutable.
 */
public interface AttrValueParser extends AttrValueWrapper {
	/**
	 * Get a new XML Element node representing this
	 * Node.
	 * @param root the DOM Document root to use to generate
	 *      the necessary elements.
	 * @param o the attribute value (in encoded format)
	 * @param container TODO
	 * @return a new Element representing this Node
	 */
	public Element getXMLFormat(Document root, Object o, Node container);

	/**
	 * Gets a copy of this attribute with the given value
	 * @param el the element to parse in
	 * @param container TODO
	 * @return the new value for the attribute (as described in the passed xml element)
	 *  in encoded format. Call {@link AttrValueWrapper#getObjectValue(Object, Node, Instant)} to
	 * get the decoded format.
	 * @throws viper.api.BadAttributeDataException
	 */
	public Object setValue(Element el, Node container);
}
