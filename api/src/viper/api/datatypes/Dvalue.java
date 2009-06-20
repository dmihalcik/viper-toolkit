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

package viper.api.datatypes;

import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;

/**
 * Attribute data wrapper for the integer type, 'dvalue'.
 * @author davidm
 */
public class Dvalue extends InstanceOfConstraint implements AttrValueParser, DefaultedAttrValueWrapper {
	private static final String AT_V = "value";

	/**
     * The only instance of Dvalue you'll ever need.
     */
    public static final Dvalue DV = new Dvalue();

	/**
	 * Creates a new instance of the integer type wrapper.
	 */
	public Dvalue() {
		super(Integer.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "dvalue");
		el.setAttribute(AT_V, o.toString());
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_V)) {
			return new Integer (el.getAttribute(AT_V));
		} else {
			throw new BadAttributeDataException("Missing value attribute for svalue");
		}
	}
	/**
	 * Returns zero.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new Integer(0);
	}
}
