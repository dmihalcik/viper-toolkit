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
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Attribute value wrapper for the Pnt data type.
 * @author davidm
 */
public class Point
	extends InstanceOfConstraint
	implements AttrValueParser, DefaultedAttrValueWrapper {
	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String localName = "point";

	/**
	 * Creates a new wrapper for the point data type.
	 */
	public Point() {
		super(Pnt.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + localName);
		Pnt curr = (Pnt) o;
		el.setAttribute(AT_X, String.valueOf(curr.getX()));
		el.setAttribute(AT_Y, String.valueOf(curr.getY()));
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		String uri = ViperData.ViPER_DATA_URI;
		if (el.getNamespaceURI().equals(uri) 
			&& el.getLocalName().equals(localName)
			&& el.hasAttribute(AT_X)
			&& el.hasAttribute(AT_Y)) {
			String str1 = el.getAttribute(AT_X);
			String str2 = el.getAttribute(AT_Y);

			int x = Integer.parseInt(str1);
			int y = Integer.parseInt(str2);

			return new Pnt(x, y);
		} else {
			throw new BadAttributeDataException("Missing an attribute for dimensions of an oriented box");
		}
	}
	/**
	 * Returns the point at 0,0.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new Pnt(0,0);
	}
}
