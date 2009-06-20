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
 * An attribute wrapper for the boolean attribute data type.
 * @author davidm
 */
public class Bvalue extends InstanceOfConstraint implements AttrValueParser, DefaultedAttrValueWrapper {
	private static final String AT_V = "value";
    /**
     * The only instance of Bvalue that you'll ever need. Use this instead 
     * of creating a new one when possible.
     */
    public static final Bvalue BV = new Bvalue();

	/**
	 * Creates a new instance of the boolean data wrapper.
	 */
	public Bvalue() {
		super(Boolean.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "bvalue");
		el.setAttribute(AT_V, o.toString());
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_V)) {
			return new Boolean (el.getAttribute(AT_V));
		} else {
			throw new BadAttributeDataException("Missing value attribute for svalue");
		}
	}
	/**
	 * Returns false.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return Boolean.FALSE;
	}
}
