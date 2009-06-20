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
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * A value wrapper for the bounding box data type.
 * @author davidm
 */
public class AttributeBbox extends InstanceOfConstraint implements AttrValueParser, DefaultedAttrValueWrapper {

	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String AT_W = "width";
	private static final String AT_H = "height";

	/**
	 * Creates a new bounding box value wrapper.
	 */
	public AttributeBbox() {
		super(BoundingBox.class);
	}
	
	protected Object convert(Object o, Node container, Instant instant) {
		if (o instanceof BoundingBox || o == null) {
			return o;
		} else if (o instanceof PolyList) {
			return ((PolyList) o).getBoundingBox();
		}
		throw new AttributeDataConversionException("Cannot convert to BBOX from " + o);
	}

	/**
	 * Gets the value as an xml format. It is something like
	 * <code>&lt;viper-data:bbox x="" y="" width="" height="" /&gt;</code>.
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "bbox");
		BoundingBox b = (BoundingBox) o;
		el.setAttribute(AT_X, String.valueOf(b.getX()));
		el.setAttribute(AT_Y, String.valueOf(b.getY()));
		el.setAttribute(AT_W, String.valueOf(b.getWidth()));
		el.setAttribute(AT_H, String.valueOf(b.getHeight()));
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_X)
			&& el.hasAttribute(AT_Y)
			&& el.hasAttribute(AT_W)
			&& el.hasAttribute(AT_H)) {

			int x = Integer.parseInt(el.getAttribute(AT_X));
			int y = Integer.parseInt(el.getAttribute(AT_Y));
			int w = Integer.parseInt(el.getAttribute(AT_W));
			int h = Integer.parseInt(el.getAttribute(AT_H));

			return new BoundingBox(x, y, w, h);
		} else {
			throw new BadAttributeDataException("Missing an attribute for dimensions of a bounding box");
		}
	}

	/**
	 * Returns the point at 0,0.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new BoundingBox(0,0,0,0);
	}
}
