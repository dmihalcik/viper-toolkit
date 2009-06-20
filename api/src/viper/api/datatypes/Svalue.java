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
 * An attribute value wrapper for the string data type.
 * @author davidm
 */
public class Svalue extends InstanceOfConstraint implements AttrValueParser, DefaultedAttrValueWrapper {
	private static final String AT_V = "value";
	/**
	 * The only string attribute value wrapper you'll need.
	 */
    public static final Svalue SV = new Svalue();

	/**
	 * Creates a new svalue item. Use the singleton {@link #SV} instead.
	 */
	public Svalue() {
		super(String.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "svalue");
		el.setAttribute(AT_V, o.toString());
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_V)) {
			return el.getAttribute(AT_V);
		} else {
			throw new BadAttributeDataException("Missing value attribute for svalue");
		}
	}
	/**
	 * Returns the empty string.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return "";
	}
}
