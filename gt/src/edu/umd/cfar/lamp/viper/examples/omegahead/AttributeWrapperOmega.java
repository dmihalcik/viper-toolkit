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

package edu.umd.cfar.lamp.viper.examples.omegahead;

import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import edu.umd.cfar.lamp.viper.geometry.*;


/**
 * An attribute wrapper for the omega shape type.
 * Used while parsing and serializing the data, as well
 * as for verification purposes internally.
 * @author davidm
 */
public class AttributeWrapperOmega
	extends InstanceOfConstraint
	implements AttrValueParser, DefaultedAttrValueWrapper {
	private static final String AT_X = "x";
	private static final String AT_Y = "y";
	private static final String AT_L = "length";
	private static final String AT_D = "diameter";
	private static final String AT_R = "rotation";
	
	private static final String AT_MD = "minorDiameter";
	private static final String AT_OFF = "offset";

	private static final String AT_Y_SHIFT = "yLineShift";
	private static final String AT_LR = "lineRotation";

	/**
	 * Creates a new attribute value wrapper for oriented box data.
	 */
	public AttributeWrapperOmega() {
		super(OmegaHeadModel.class);
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "omega");
		OmegaHeadModel ob = (OmegaHeadModel) o;
		el.setAttribute(AT_X, String.valueOf(ob.getCentroid().getX()));
		el.setAttribute(AT_Y, String.valueOf(ob.getCentroid().getY()));
		el.setAttribute(AT_L, String.valueOf(ob.getLineLength()));
		el.setAttribute(AT_D, String.valueOf(ob.getEllipseHeight()));
		el.setAttribute(AT_R, String.valueOf(ob.getOrientation()));
		el.setAttribute(AT_MD, String.valueOf(ob.getEllipseWidth()));
		el.setAttribute(AT_OFF, String.valueOf(ob.getLineOffset()));
		el.setAttribute(AT_Y_SHIFT, String.valueOf(ob.getYLineOffset()));
		el.setAttribute(AT_LR, String.valueOf(ob.getLineOrientation()));
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_X)
			&& el.hasAttribute(AT_Y)
			&& el.hasAttribute(AT_L)
			&& el.hasAttribute(AT_D)
			&& el.hasAttribute(AT_R)) {

			int x = Integer.parseInt(el.getAttribute(AT_X));
			int y = Integer.parseInt(el.getAttribute(AT_Y));
			int l = Integer.parseInt(el.getAttribute(AT_L));
			int d = Integer.parseInt(el.getAttribute(AT_D));
			int r = Integer.parseInt(el.getAttribute(AT_R));
			
			int minor = d;
			int offset = 0;
			int yShift = 0;
			int lr = 0;
			if(el.hasAttribute(AT_MD)) {
				minor = Integer.parseInt(el.getAttribute(AT_MD));
			}
			if(el.hasAttribute(AT_OFF)) {
				offset = Integer.parseInt(el.getAttribute(AT_OFF));
			}
			if (el.hasAttribute(AT_Y_SHIFT)) {
				yShift = Integer.parseInt(el.getAttribute(AT_Y_SHIFT));
			}
			if (el.hasAttribute(AT_LR)) {
				lr = Integer.parseInt(el.getAttribute(AT_LR));
			}

			return new OmegaHeadModel(new Pnt(x, y), l, offset, d, minor, yShift, r, lr);
		} else {
			throw new BadAttributeDataException("Missing an attribute for dimensions of an omega shape");
		}
	}
	
	/**
	 * Returns the point at 0,0.
	 * @see viper.api.extensions.DefaultedAttrValueWrapper#getMetaDefault(Node)
	 */
	public Object getMetaDefault(Node container) {
		return new OmegaHeadModel(new Pnt(),0,0,0,0,0,0,0);
	}
}
