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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.util.*;

import org.apache.commons.lang.*;
import org.apache.commons.lang.builder.*;
import org.w3c.dom.*;

import edu.umd.cfar.lamp.viper.geometry.*;

import viper.api.*;
import viper.api.Node;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.time.*;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Feb 12, 2005
 * 
 * Serializes and deserializes a TextlineModel object to and from XML.
 * 
 */
public class AttributeWrapperTextline extends InstanceOfConstraint implements
		ExtendedAttrValueParser, DefaultedAttrValueWrapper,
		LinkedAttrValueParser {

	private static final String AT_X = "x";

	private static final String AT_Y = "y";

	private static final String AT_W = "width";

	private static final String AT_H = "height";

	private static final String AT_R = "rotation";

	private static final String AT_OCC = "occlusions";

	private static final String AT_OFF = "offsets";

	private static final String AT_TXT = "text";

	private AttrConfig attrConfig;

	private AttrConfig textLink; // the attribute storing the text for all
									// instances of the textline; DO NOT
									// reference directly, may be null

	private String textLinkName = "TextlineString"; // name of textLink if we
													// don't have the object yet

	/**
	 * Creates a new attribute value wrapper for textbox data.
	 */
	public AttributeWrapperTextline() {
		super(TextlineModel.class);
	}

	/**
	 * Creates an XML element from the given object o
	 * 
	 * @return the XML serialization of o (a TextlineModel object)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "textline");
		TextlineModel tm = (TextlineModel) o;
		el.setAttribute(AT_X, String.valueOf(tm.getX()));
		el.setAttribute(AT_Y, String.valueOf(tm.getY()));
		el.setAttribute(AT_W, String.valueOf(tm.getWidth()));
		el.setAttribute(AT_H, String.valueOf(tm.getHeight()));
		el.setAttribute(AT_R, String.valueOf(tm.getRotation()));
		el.setAttribute(AT_OCC, String.valueOf(tm.getOcclusionsAsStr()));
		el.setAttribute(AT_OFF, String.valueOf(tm.getWordOffsetsAsStr()));
		el.setAttribute(AT_TXT, String.valueOf(tm.getText(null)));
		return el;
	}

	/**
	 * Creates a new instance of TextlineModel from an XML element.
	 * 
	 * @return a TextlineModel object corresponding to the given XML
	 *         serialization
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_X) && el.hasAttribute(AT_Y)
				&& el.hasAttribute(AT_W) && el.hasAttribute(AT_H)
				&& el.hasAttribute(AT_R) && el.hasAttribute(AT_OCC)
				&& el.hasAttribute(AT_OFF) && el.hasAttribute(AT_TXT)) {

			int x = Integer.parseInt(el.getAttribute(AT_X));
			int y = Integer.parseInt(el.getAttribute(AT_Y));
			int w = Integer.parseInt(el.getAttribute(AT_W));
			int h = Integer.parseInt(el.getAttribute(AT_H));
			int r = Integer.parseInt(el.getAttribute(AT_R));
			String text = el.getAttribute(AT_TXT).toString();
			String occ = el.getAttribute(AT_OCC).toString();
			String off = el.getAttribute(AT_OFF).toString();

			String[] tok; // stores tokens temporarily

			// convert space-separated list of occlusion *pairs* to ArrayList of
			// IntPairs
			ArrayList occAL = new ArrayList();
			tok = occ.split(" ");
			if (tok[0] != "") { // IMPORTANT: skip if it's empty
				if (tok.length % 2 != 0)
					throw new BadAttributeDataException(
							"Odd number of occlusions in a textline object");
				for (int i = 0; i < tok.length; i += 2) { // notice the
															// step-by-two
															// increase
					occAL.add(new IntPair(Integer.parseInt(tok[i]), Integer
							.parseInt(tok[i + 1])));
				}
			}

			// convert space-separated list of offsets to ArrayList object
			ArrayList offAL = new ArrayList();
			tok = off.split(" ");
			if (tok[0] != "") { // IMPORTANT: skip if it's empty
				for (int i = 0; i < tok.length; i++) {
					offAL.add(new Integer(tok[i]));
				}
			}

			return new TextlineModel(x, y, w, h, r, text, occAL, offAL); // create
																			// new
																			// TextlineModel
																			// object

		} else {
			throw new BadAttributeDataException(
					"Missing an attribute for dimensions of a textline object");
		}
	}

	/**
	 * @return the default TextlineModel object with all parameters at
	 *         zero/empty
	 */
	public Object getMetaDefault(Node container) {
		TextlineModel retVal = new TextlineModel();
		helpSetObjectTextPointer(retVal, container);
		return retVal;
	}

	/**
	 * Sets the attrConfig value
	 */
	public void setAttrConfig(AttrConfig ac) {
		attrConfig = ac;
	}

	/**
	 * @return Returns the attrConfig.
	 */
	public AttrConfig getAttrConfig() {
		return attrConfig;
	}

	/**
	 * @return Returns the textLink.
	 */
	public AttrConfig getTextLink() {
		if (textLink == null && attrConfig != null) {
			textLink = ((Config) attrConfig.getParent())
					.getAttrConfig(textLinkName);
		}
		return textLink;
	}

	/**
	 * @param textLink
	 *            The textLink to set.
	 */
	public void setTextLink(AttrConfig textLink) {
		this.textLink = textLink;
		if (textLink != null) {
			textLinkName = textLink.getAttrName(); // just for consistency
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see viper.api.extensions.ExtendedAttrValueParser#getXMLFormatConfig(org.w3c.dom.Document,
	 *      viper.api.Node)
	 */
	public Element getXMLFormatConfig(Document root, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element poss = root.createElementNS(uri, qualifier + "textlink");
		if (getTextLink() != null)
			poss.setAttribute("value", getTextLink().getAttrName());
		return poss;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see viper.api.extensions.ExtendedAttrValueParser#setConfig(org.w3c.dom.Element,
	 *      viper.api.Node)
	 */
	public AttrValueWrapper setConfig(Element el, Node container) {
		String uri = ViperData.ViPER_DATA_URI;

		NodeList elements = el.getElementsByTagNameNS(uri, "textlink");
		boolean newOpen = false;
		if (elements.getLength() == 0) {
			// defaults to looking for the default attribute named
			// "TextlineString" (see init)
		} else if (elements.getLength() > 1) {
			throw new BadAttributeDataException("textline "
					+ el.getAttribute("name")
					+ " may only have one <textlink> tag.");
		} else { // length == 1
			Element type = (Element) elements.item(0);
			AttributeWrapperTextline ret = new AttributeWrapperTextline();
			ret.setTextLinkName(type.getAttribute("value"));
			return ret;
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see viper.api.AttrValueWrapper#getObjectValue(java.lang.Object,
	 *      viper.api.Node)
	 */
	public Object getObjectValue(Object o, Node container, Instant instant) {
		helpSetObjectTextPointer(o, container);
		return super.getObjectValue(o, container, instant);
	}

	/**
	 * @param o
	 * @param container
	 */
	private void helpSetObjectTextPointer(Object o, Node container) {
		// every time this is an actual descriptor (not a default setting), call
		// setText on the TextlineModel
		if (container instanceof Attribute && o instanceof TextlineModel) {
			Attribute a = (Attribute) container;
			((TextlineModel) o).setTextPointer(a.getDescriptor().getAttribute(
					getTextLink()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see viper.api.AttrValueWrapper#setAttributeValue(java.lang.Object,
	 *      viper.api.Node)
	 */
	public Object setAttributeValue(Object o, Node container) {
		if (o == null) {
			return null;
		}
		if (o instanceof BoundingBox) {
			BoundingBox bbox = (BoundingBox) o;
			o = new TextlineModel(bbox.getX(), bbox.getY(), bbox.getWidth(),
					bbox.getHeight(), 0);
		} else if (o instanceof OrientedBox) {
			OrientedBox obox = (OrientedBox) o;
			o = new TextlineModel(obox.getX(), obox.getY(), obox.getWidth(),
					obox.getHeight(), obox.getRotation());
		}
		if (o instanceof TextlineModel) {
			helpSetObjectTextPointer(o, container);
			return o;
		} else {
			throw new BadAttributeDataException(
					"Value not textline (or something that can be converted to one: "
							+ o + " (" + o.getClass() + ")");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof AttributeWrapperTextline) {
			return ObjectUtils.equals(getTextLink(),
					((AttributeWrapperTextline) o).getTextLink());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder().append(super.hashCode()).append(
				getTextLink()).toHashCode();
	}

	/**
	 * @return Returns the textLinkName.
	 */
	public String getTextLinkName() {
		return textLinkName;
	}

	/**
	 * @param textLinkName
	 *            The textLinkName to set.
	 */
	public void setTextLinkName(String textLinkName) {
		this.textLinkName = textLinkName;
		this.textLink = null;
	}
}
