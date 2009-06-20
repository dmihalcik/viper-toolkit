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

import java.util.*;

import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * An attribute wrapper for the oriented box type.
 * @author davidm
 */
public class AttributeObox
	extends InstanceOfConstraint
	implements AttrValueParser, DefaultedAttrValueWrapper, FacetedAttributeWrapper {
	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String AT_W = "width";
	private static final String AT_H = "height";
	private static final String AT_R = "rotation";

	/**
	 * Creates a new attribute value wrapper for oriented box data.
	 */
	public AttributeObox() {
		super(OrientedBox.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "obox");
		OrientedBox ob = (OrientedBox) o;
		el.setAttribute(AT_X, String.valueOf(ob.getX()));
		el.setAttribute(AT_Y, String.valueOf(ob.getY()));
		el.setAttribute(AT_W, String.valueOf(ob.getWidth()));
		el.setAttribute(AT_H, String.valueOf(ob.getHeight()));
		el.setAttribute(AT_R, String.valueOf(ob.getRotation()));
		return el;
	}
	
	protected Object convert(Object o, Node container, Instant instant) {
		if (o instanceof OrientedBox || o == null) {
			return o;
		} else if (o instanceof PolyList) {
			BoundingBox b;
			if (o instanceof BoundingBox) {
				b = (BoundingBox) o;
			} else {
				b = ((PolyList) o).getBoundingBox();
			}
			return new OrientedBox(b.getX(), b.getY(), b.getWidth(), b.getHeight(), 0);
		}
		throw new AttributeDataConversionException("Cannot convert to BBOX from " + o);
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

			return new OrientedBox(x, y, w, h, r);
		} else {
			throw new BadAttributeDataException("Missing an attribute for dimensions of an oriented box");
		}
	}
	/**
	 * Returns the point at 0,0.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new OrientedBox(0,0,0,0,0);
	}

	public List getFacetDefinitions(AttrConfig link) {
		return null;
	}

	public FacetConfig getFacetByName(String name, AttrConfig link) {
		return null;
	}
	
	private static class OrientationFacet implements FacetValueWrapper {
		public Object getFacetValue(Object attrValue) {
			OrientedBox av = (OrientedBox) attrValue;
			return new Integer(av.getRotation());
		}
		public Object setFacetValue(Object facetValue, Object attrValue) {
			OrientedBox av = (OrientedBox) attrValue;
			int rotation = ((Integer) facetValue).intValue();
			return new OrientedBox(av.getX(), av.getY(), av.getWidth(), av.getHeight(), rotation);
		}
		public Object getObjectValue(Object encodedFormat, Node container, Instant instant) {
			return getFacetValue(encodedFormat);
		}
		public Object setAttributeValue(Object o, Node container) {
			throw new UnsupportedOperationException();
		}
	}
}
