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

package viper.descriptors;

import org.w3c.dom.*;

import viper.comparison.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class represents a Descriptor of a given visual element in a video file.
 * There are three types: FILE, CONTENT, and OBJECT.
 */
public class DescSingle extends Descriptor {
	int id = 0;

	/**
	 * Constructs a new object with the given 
	 * category.
	 * @param category the category, e.g. "CONTENT"
	 * @throws BadDataException
	 */
	public DescSingle(String category) throws BadDataException {
		super(category);
		span = null;
	}

	/**
	 * Constructs a new descriptor with the given 
	 * category and type name.
	 * @param category the category, e.g. "CONTENT"
	 * @param name the name of the descriptor type, e.g. "Person"
	 * @throws BadDataException
	 */
	public DescSingle(String category, String name) throws BadDataException {
		super(category);
		span = null;
		setName(name);
	}

	/**
	 * Generates a new Descriptor Object sharing none of the references of the
	 * original but containing identical data.
	 * 
	 * @return new Descriptor initialized with this Descriptor's data
	 */
	public Object clone() {
		DescSingle temp;
		try {
			temp = new DescSingle(new String(getCategory()));
		} catch (BadDataException bdx) {
			bdx.printStackTrace();
			return (null);
		}
		temp.setName(getName());
		temp.attributes = new Attribute[attributes.length];
		for (int i = 0; i < attributes.length; i++)
			temp.attributes[i] = (Attribute) attributes[i].clone();
		temp.id = id;
		temp.span = (span == null) ? null : (FrameSpan) span.clone();
		return (temp);
	}

	/**
	 * Returns the ID number of the descriptor.
	 * 
	 * @return the ID number of the descriptor
	 */
	public Object getID() {
		return new Integer(id);
	}

	/**
	 * Sets the id number of the descriptor. This is used
	 * to uniquely identify the instance among others of its
	 * type on the same source file.
	 * @param id {@inheritDoc}
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * @inheritDoc
	 * @return one
	 */
	public int numIDs() {
		return 1;
	}

	/**
	 * Breaks the combined frame span of the attributes. 
	 * @return the attributes frame span, if any attributes
	 * are dynamic. Otherwise, returns the descriptor's frame span.
	 */
	public FrameSpan getBrokenFrameSpan() {
		FrameSpan bs = new FrameSpan();
		boolean hasDynamic = false;
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].isDynamic()) {
				hasDynamic = true;
				for (int f = span.beginning(); f <= span.ending(); f++) {
					if (attributes[i].getValue(span, f) != null) {
						bs.set(f);
					}
				}
			}
		}
		if (hasDynamic) {
			return bs;
		} else {
			return (FrameSpan) span.clone();
		}
	}

	/**
	 * Gets the descriptor's frame span.
	 * @return {@inheritDoc}
	 */
	public FrameSpan getFrameSpan() {
		if (span == null) {
			span = new FrameSpan();
		}
		return span;
	}

	/**
	 * @inheritDoc
	 */
	public void moveFrame(int offset) {
		span.shift(offset);
	}

	/**
	 * changes the framespan. If an attribute is dynamic, then it will be set to
	 * null for all frames that it were not included in the previous span. All
	 * static attributes will remain the same.
	 * 
	 * @param span
	 *            the new span
	 */
	public void setFrameSpan(FrameSpan span) {
		if (this.span != null) {
			for (int i = 0; i < attributes.length; i++) {
				attributes[i].setFrameSpan(span, this.span);
			}
		}
		this.span = span;
	}

	/**
	 * Combines this descriptor with another section of the same descriptor with
	 * the same ID. This method is necessary since the old format output from
	 * viper-gt splits discontiguous descriptors into two descriptors with the
	 * same id.
	 * 
	 * @param shard
	 *            The descriptor to combine with.
	 * @throws BadDataException
	 *             if the Descriptors don't have the same id, overlap
	 *             temporally, or have different static attribute values
	 */
	void combineWith(Descriptor shard) throws BadDataException {
		if (!getType().equals(shard.getType())
				|| !getCategory().equals(shard.getCategory())) {
			throw new BadDataException(
					"Two descriptors of different type cannot be combined."
							+ ":: " + getFullName() + " + "
							+ shard.getFullName());
		}
		if (!getID().equals(shard.getID())) {
			throw new BadDataException(
					"Descriptors with different identification numbers cannot be combined.");
		}
		if (getFrameSpan().intersects(shard.getFrameSpan())) {
			throw new BadDataException("Split descriptors cannot overlap");
		}
		FrameSpan oldSpan = getFrameSpan();
		FrameSpan newSpan = oldSpan.union(shard.getFrameSpan());
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].isDynamic()) {
				// skip the first time through.
			} else if (attributes[i].getStaticValue() != shard.attributes[i]
					.getStaticValue()
					&& !attributes[i].getStaticValue().equals(
							shard.attributes[i].getStaticValue())) {
				throw new BadDataException(
						"Static values must be the same for all occurances of a "
								+ "descriptor with a given identification number");
			}
		}
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].isDynamic()) {
				try {
					attributes[i] = Attribute.compose(oldSpan, attributes[i],
							shard.getFrameSpan(), shard.attributes[i]);
				} catch (UncomposableException ux) {
					throw new BadDataException(ux.getMessage());
				}
			}
		}
		this.span = newSpan;
	}
	
	/**
	 * @inheritDoc
	 */
	public Descriptor compose(Descriptor D,
			EvaluationParameters.ScopeRules scope) throws BadDataException,
			UncomposableException {
		return new DescAggregate(this).compose(D, scope);
	}

	/**
	 * Gets an XML representation of the descriptor.
	 * 
	 * @param root
	 *            used to create the returned elements
	 * @return an xml element describing this descriptor
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement(getType().toLowerCase());
		el.setAttribute("name", getName());
		el.setAttribute("id", getID().toString());
		if (!getFrameSpan().isEmpty()) {
			el.setAttribute("framespan", getFrameSpan().toString());
		}
		for (int i = 0; i < attributes.length; i++) {
			Element child = attributes[i].getXMLFormat(root, getFrameSpan());
			el.appendChild(child);
		}
		return el;
	}

	/**
	 * Sets the named attribute to the given value
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param attribute
	 *            the value of the attribute
	 * @throws BadDataException
	 *             when trying to set the wrong type - ie dyanmic to static or
	 *             bbox to circle. It does not currently check to make sure that
	 *             there are the proper number of frames in a dynamic attribute,
	 *             however.
	 */
	public void setAttribute(String name, Attribute attribute)
			throws BadDataException {
		for (int i = 0; i < attributes.length; i++) {
			if (name == attributes[i].getName()) {
				if (attributes[i].isDynamic()) {
					if (!attribute.isDynamic()) {
						throw new BadDataException(
								"Trying to set a dynamic attribute to a static value.");
					}
				} else if (attribute.isDynamic()) {
					throw new BadDataException(
							"Trying to set a static attribute to a dynamic value.");
				}
				if (!attributes[i].getType().equals(attribute.getType())) {
					throw new BadDataException("Trying to set a "
							+ attributes[i].getType() + " to a "
							+ attribute.getType() + "value.");
				}
				attributes[i] = attribute;
				return;
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public Descriptor crop(FrameSpan span) {
		span = span.intersect(this.span);

		DescSingle copy;
		try {
			copy = new DescSingle(new String(getCategory()));
		} catch (BadDataException bdx) {
			throw new IllegalStateException(bdx.getMessage());
		}

		copy.setName(getName());
		copy.attributes = new Attribute[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			copy.attributes[i] = attributes[i].crop(span, this.span);
		}
		copy.id = id;
		copy.span = span;
		return copy;
	}
}