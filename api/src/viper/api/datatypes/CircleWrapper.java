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
 * Attribute data type specifier for circles.
 * @author davidm
 */
public class CircleWrapper extends InstanceOfConstraint implements AttrValueParser,DefaultedAttrValueWrapper {
	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String AT_R = "radius";

	/**
	 * Creates a new instance of the circle attribute data wrapper.
	 */
	public CircleWrapper() {
		super(Circle.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "circle");
		Circle c = (Circle) o;
		el.setAttribute(AT_X, String.valueOf(c.getCenter().getX()));
		el.setAttribute(AT_Y, String.valueOf(c.getCenter().getY()));
		el.setAttribute(AT_R, String.valueOf(c.getRadius()));
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_X)
			&& el.hasAttribute(AT_Y)
			&& el.hasAttribute(AT_R)) {

			int x = Integer.parseInt(el.getAttribute(AT_X));
			int y = Integer.parseInt(el.getAttribute(AT_Y));
			int r = Integer.parseInt(el.getAttribute(AT_R));

			return new Circle(x, y, r);
		} else {
			throw new BadAttributeDataException("Missing an attribute for dimensions of a circle");
		}
	}
	/**
	 * Returns the point at 0,0.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new Circle(0,0,0);
	}
}
