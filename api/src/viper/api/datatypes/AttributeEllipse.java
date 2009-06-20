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
 * The viper attribute parameter value wrapper for ellipses.
 * @author davidm
 */
public class AttributeEllipse
	extends InstanceOfConstraint
	implements AttrValueParser, DefaultedAttrValueWrapper {
	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String AT_W = "width";
	private static final String AT_H = "height";
	private static final String AT_R = "rotation";

	/**
	 * Creates a new value wrapper for ellipses.
	 */
	public AttributeEllipse() {
		super(Ellipse.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "obox");
		Ellipse ob = (Ellipse) o;
		el.setAttribute(AT_X, String.valueOf(ob.getX()));
		el.setAttribute(AT_Y, String.valueOf(ob.getY()));
		el.setAttribute(AT_W, String.valueOf(ob.getWidth()));
		el.setAttribute(AT_H, String.valueOf(ob.getHeight()));
		el.setAttribute(AT_R, String.valueOf(ob.getRotation()));
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_X)
			&& el.hasAttribute(AT_Y)
			&& el.hasAttribute(AT_W)
			&& el.hasAttribute(AT_H)
			&& el.hasAttribute(AT_R)) {

			int x = Integer.parseInt(el.getAttribute(AT_X));
			int y = Integer.parseInt(el.getAttribute(AT_Y));
			int w = Integer.parseInt(el.getAttribute(AT_W));
			int h = Integer.parseInt(el.getAttribute(AT_H));
			int r = Integer.parseInt(el.getAttribute(AT_R));

			return new Ellipse(x, y, w, h, r);
		} else {
			throw new BadAttributeDataException("Missing an attribute for dimensions of an oriented box");
		}
	}
	/**
	 * Returns the point at 0,0.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new Ellipse(0,0,0,0,0);
	}
}
