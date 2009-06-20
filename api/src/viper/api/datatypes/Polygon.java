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
import org.w3c.dom.Node;

import viper.api.*;
import viper.api.extensions.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Attribute value wrapper for the polygon or polyline data
 * type.
 * @author davidm
 */
public class Polygon
	extends InstanceOfConstraint
	implements ExtendedAttrValueParser, DefaultedAttrValueWrapper {
	private boolean open = false;
	
	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String POINT = "point";


	/**
	 * Creates a new closed polygon wrapper.
	 */
	public Polygon() {
		super(edu.umd.cfar.lamp.viper.geometry.Polygon.class);
	}
	/**
	 * Creates a polygon or polyline wrapper.
	 * @param open <code>true</code> for a polyline
	 */
	public Polygon(boolean open) {
		super(open ? PolyLine.class : edu.umd.cfar.lamp.viper.geometry.Polygon.class);
		this.open = open;
	}
	private String localName() {
		return open ? "polyline" : "polygon";
	}
	private Element createPolygonEl(Document root) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		String localName = localName();
		return root.createElementNS(uri, qualifier + localName);
	}
	private Element createPointEl(Document root) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		String localName = POINT;
		return root.createElementNS(uri, qualifier + localName);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, viper.api.Node)
	 */
	public Element getXMLFormat(Document root, Object o, viper.api.Node container) {
		Element el = createPolygonEl(root);
		PolyList p = (PolyList) o;
		for (Iterator iter = p.getPoints(); iter.hasNext();) {
			Pnt curr = (Pnt) iter.next();
			Element childEl = createPointEl(root);
			childEl.setAttribute(AT_X, String.valueOf(curr.getX()));
			childEl.setAttribute(AT_Y, String.valueOf(curr.getY()));
			el.appendChild(childEl);
		}
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, viper.api.Node)
	 */
	public Object setValue(Element el, viper.api.Node container) {
		String uri = ViperData.ViPER_DATA_URI;
		NodeList nl = el.getChildNodes();
		PolyList p;
		if (open) {
			p = new PolyLine();
		} else {
			p = new edu.umd.cfar.lamp.viper.geometry.Polygon(); 
		}
		for (int k = 0; k < nl.getLength(); k++) {
			Node n = nl.item(k);
			if (n.getNodeType() == Node.ELEMENT_NODE
				&& n.getNamespaceURI().equals(uri)
				&& n.getLocalName().equals(POINT)) {
				Element child = (Element) nl.item(k);

				String str1 = child.getAttribute(AT_X);
				String str2 = child.getAttribute(AT_Y);

				int x = Integer.parseInt(str1);
				int y = Integer.parseInt(str2);

				Pnt pt = new Pnt(x, y);
				try {
					p.addVertex(pt);
				} catch (BadDataException bdx) {
					throw new BadAttributeDataException(bdx.getLocalizedMessage());
				}
			}
		}
		return p;
	}

	/**
	 * @see viper.api.extensions.ExtendedAttrValueParser#setConfig(org.w3c.dom.Element, viper.api.Node)
	 */
	public AttrValueWrapper setConfig(Element el, viper.api.Node container) {
		String uri = ViperData.ViPER_DATA_URI;

		NodeList elements = el.getElementsByTagNameNS(uri, "polygon-type");
		boolean newOpen = false;
		if (elements.getLength() == 0) {
			// defaults to false;
		} else if (elements.getLength() > 1) {
			throw new BadAttributeDataException(
				"polygon "
					+ el.getAttribute("name")
					+ " may only have one <polygon-type> tag.");
		} else {
			Element type = (Element) elements.item(0);
			String openAttr = type.getAttribute("open");
			if ("true".equals(openAttr)) {
				newOpen = true;
			} else if (!"false".equals(openAttr)) {
				throw new BadAttributeDataException(
					"polygon-type@open must be either 'true' or 'false', not: '"
						+ type.getAttribute("type")
						+ "'");
			}
		}
		if (this.open != newOpen) {
			return new Polygon(newOpen);
		} else {
			return this;
		}
	}
	/**
	 * @see viper.api.extensions.ExtendedAttrValueParser#getXMLFormatConfig(org.w3c.dom.Document, viper.api.Node)
	 */
	public Element getXMLFormatConfig(Document root, viper.api.Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element poss = root.createElementNS(uri, qualifier + "polygon-type");
		poss.setAttribute("open", open ? "true" : "false");
		return poss;
	}
	/**
	 * Returns the empty closed polygon.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(viper.api.Node)
	 */
	public Object getMetaDefault(viper.api.Node container) {
		return new Polygon();
	}
}
