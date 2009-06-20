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

package viper.descriptors.attributes;

import java.util.*;

import org.w3c.dom.*;

import viper.descriptors.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class is used to hold an attribute for a {@link 
 * viper.descriptors.Descriptor Descriptor}.
 * 
 * An Attribute is an aspect of an object that is being monitored in a given
 * video file. For example, Attributes of a text object would be a
 * {@link Attribute_bbox bounding box}, the text, the color, etc.
 */
public class Attribute extends AbstractAttribute
		implements
			Cloneable,
			Filterable {
	/*
	 * The data is stored as an object. Each inheritor of this does not need to
	 * know about that; it just needs to override the distance method, the clone
	 * method, etc.
	 * 
	 * If it is set composeable, it will have to set composeable to true, and
	 * implement the methods compose and compositionDistance.
	 */

	/**
	 * This is the number of characters allowed when the toString method is
	 * called before printing an elipsis. If it is -1, it will print out all of
	 * the information.
	 */
	static int outputWidth = -1;

	/**
	 * @inheritDoc
	 */
	public void setName(String S) {
		super.setName(S);
	}

	protected Object value = null;

	protected boolean errors;

	/**
	 * This is true iff this Attribute is composed of several.
	 */
	protected boolean composition = false;

	protected String compositionType = "first";
	protected double compositionTolerance = 0;

	/**
	 * Sets the width to crop to when printing out
	 * attribute overviews.
	 * @param W the number of characters to allow 
	 * for attribute summaries
	 */
	static public void setOutputWidth(int W) {
		outputWidth = W;
	}

	/**
	 * Sets the composition type of the attribute. In order to implement new
	 * composition types, a subclass must override isCompositionType and the
	 * composition functions.
	 * 
	 * @param s
	 *            the composition type to use - e.g. "none"
	 * @throws ImproperMetricException
	 */
	public final void setCompositionType(String s)
			throws ImproperMetricException {
		if (!s.equals("none") && isCompositionType(s))
			compositionType = s;
		else
			throw (new ImproperMetricException(s
					+ " is not a valid Composition type " + "for " + getType()
					+ " Attributes."));
	}

	/**
	 * Gets the composition type associated with this descriptor.
	 * @return the composition type
	 */
	public final String getCompositionType() {
		return compositionType;
	}

	/**
	 * This allows attributes to define their own composition types in addition
	 * to the default ones. The compose (Object, Object) methods of the
	 * subclasses should call this one first to see if the superclass can handle
	 * the composition.
	 * 
	 * @param s
	 *            the name of composition type to test
	 * @return true if s is "none" or "first".
	 */
	public boolean isCompositionType(String s) {
		return ((s.equalsIgnoreCase("none")) || (s.equalsIgnoreCase("first")));
	}

	/**
	 * Create an empty Attribute. toString will return an empty String until
	 * more is known.
	 */
	public Attribute() {
		errors = false;
		setDynamic(false);
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with mobile set
	 * to true. Other Descriptor types can use this with mobile set to false.
	 * 
	 * @param dynamic
	 *            Descriptor that holds this Attribute is of type OBJECT
	 */
	public Attribute(boolean dynamic) {
		errors = false;
		setDynamic(dynamic);
	}

	/**
	 * @inheritDoc
	 * @return "none"
	 */
	public String getType() {
		return "none";
	}

	/**
	 * Default copy constructor.
	 * 
	 * @param old
	 *            the attribute to copy
	 */
	public Attribute(Attribute old) {
		errors = false;
		setDynamic(old.isDynamic());
		setName(old.getName());
		setArchetype(old.getArchetype());
		resetDefault(old.getDefault());

		if (old.value != null) {
			if (isDynamic()) {
				AttributeValue[] oldVals = (AttributeValue[]) old.value;
				value = new AttributeValue[oldVals.length];
				System.arraycopy(oldVals, 0, value, 0, oldVals.length);
			} else {
				value = old.value;
			}
		}
	}

	/**
	 * Determines if the specified string is a possible Attribute data type.
	 * 
	 * @param str
	 *            The string to be tested.
	 * @return true if the string is a proper data type
	 */
	public static final boolean isType(String str) {
		String attributeClassName = "viper.descriptors.attributes.Attribute_"
				+ str;
		try {
			Attribute.class.getClassLoader().loadClass(attributeClassName);
		} catch (ClassNotFoundException cnfx) {
			return false;
		}
		return true;
	}

	/**
	 * Useful if arranging the set of Attributes using a linked list, I suppose.
	 * Does not perform to spec. That is, only the Attributes name is used in
	 * the comparison, not the other data fields.
	 * 
	 * @param o
	 *            the object to compare with this Attribute
	 * @return the value 0 if the argument Attribute's name is equal to
	 *         this.name; a value less than 0 if this.name is lexicographically
	 *         less than the argument; and a value greater than 0 if this.name
	 *         is lexicographically greater than the argument.name
	 * @throws ClassCastException
	 */
	public final int compareTo(Object o) {
		Attribute other = (Attribute) o;
		return getName().compareTo(other.getName());
	}

	/**
	 * Returns a clone of this Attribute.
	 * 
	 * @return a reference to a new Attribute with all the values of the
	 *         original
	 */
	public Object clone() {
		Attribute at = new Attribute(isDynamic());
		at.setName(getName());
		at.setArchetype(getArchetype());
		at.resetDefault(getDefault());

		at.composition = composition;
		if (value != null) {
			if (isDynamic()) {
				AttributeValue[] oldVals = (AttributeValue[]) value;
				at.value = new AttributeValue[oldVals.length];
				System.arraycopy(oldVals, 0, at.value, 0, oldVals.length);
			} else {
				at.value = value;
			}
		}
		return at;
	}

	/**
	 * This composes two Attributes across a set of frames using the
	 * <code>composeValues</code> method. Depending on how it is implemented,
	 * this function might not be comutative.
	 * 
	 * @param thisSpan
	 *            the span of attribute A
	 * @param thisAttribute
	 *            the value of attribute A
	 * @param otherSpan
	 *            the span of attribute B
	 * @param otherAttribute
	 *            the value of attribute B
	 * @return the composed attribute, if the two attributes may be composed
	 * @throws UncomposableException
	 */
	public static Attribute compose(FrameSpan thisSpan,
			Attribute thisAttribute, FrameSpan otherSpan,
			Attribute otherAttribute) throws UncomposableException {
		try {
			if (thisAttribute.getType().equals("none")
					|| otherAttribute.getType().equals("none")) {
				throw new UncomposableException(
						"Attempting to compose two typeless attributes");
			} else {
				Attribute temp = (Attribute) otherAttribute.clone();
				temp.value = null;
				temp.composition = true;

				if (thisAttribute.value == null) {
					return (Attribute) otherAttribute.clone();
				} else if (otherAttribute.value == null) {
					return (Attribute) thisAttribute.clone();
				} else if (thisAttribute.isDynamic()) {
					FrameSpan matchSpan = thisSpan.union(otherSpan);
					AttributeValue[] newValues = new AttributeValue[matchSpan
							.size()];
					temp.value = newValues;

					FrameSpan[] oldSpans = new FrameSpan[2];
					AttributeValue[][] oldValues = new AttributeValue[2][];

					oldValues[0] = (AttributeValue[]) thisAttribute.value;
					oldSpans[0] = thisSpan;

					oldValues[1] = (AttributeValue[]) otherAttribute.value;
					oldSpans[1] = otherSpan;

					for (int k = 0; k < oldSpans.length; k++) {
						for (int i = oldSpans[k].beginning(); i <= oldSpans[k]
								.ending(); i++) {
							int matchOffset = i - matchSpan.beginning();
							int oldOffset = i - oldSpans[k].beginning();
							if (newValues[matchOffset] == null) {
								newValues[matchOffset] = oldValues[k][oldOffset];
							} else if (oldValues[k][oldOffset] != null) {
								newValues[matchOffset] = (AttributeValue) ((Composable) newValues[matchOffset])
										.compose((Composable) oldValues[k][oldOffset]);
							}
						}
					}
				} else { // If not dynamic
					if (thisAttribute.value == null) {
						temp.value = otherAttribute.value;
					} else if (otherAttribute.value == null) {
						temp.value = thisAttribute.value;
					} else {
						temp.value = ((Composable) thisAttribute.value)
								.compose((Composable) otherAttribute.value);
					}
				}
				return temp;
			}
		} catch (ClassCastException ccx) {
			ccx.printStackTrace();
			throw new UncomposableException(
					"Attempting to compose two uncomposable attributes, type: "
							+ thisAttribute.getType() + " & "
							+ otherAttribute.getType());
		}
	}

	/**
	 * Determines if this Attribute can take the value specified. Relations are
	 * not yet supported.
	 * 
	 * @param S
	 *            the string to be tested
	 * @return whether or not the string is valid
	 */
	public boolean possibleValueOf(String S) {
		try {
			getArchetype().setValue(S);
		} catch (IllegalArgumentException iax) {
			return false;
		}
		return true;
	}

	/**
	 * Tests to see if the given DOM element is 
	 * data that is valid for this attribute.
	 * @param E the data DOM element to test 
	 * @return if the element represents a unit
	 * of data for this type of attribute 
	 */
	public boolean possibleValueOf(Element E) {
		if (E.getTagName() == "null")
			return true;
		try {
			getArchetype().setValue(E);
		} catch (IllegalArgumentException iax) {
			return false;
		}
		return true;
	}

	protected final Element nullXML(Document root) {
		return root.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
				Attributes.DEFAULT_NAMESPACE_QUALIFIER + "null");
	}

	protected final Element nullXML(Document root, FrameSpan span) {
		Element el = nullXML(root);
		el.setAttribute("framespan", span.toString());
		return el;
	}

	/**
	 * @inheritDoc
	 */
	public Element getXMLFormat(Document root) {
		if (!isDynamic()) {
			Element el = root.createElement("attribute");
			el.setAttribute("name", getName());
			if (value == null) {
				el.appendChild(nullXML(root));
			} else {
				el.appendChild(((AttributeValue) value).toXML(root));
			}
			return el;
		} else {
			throw new UnsupportedOperationException(
					"Dynamic attributes must get passed a FrameSpan");
		}
	}

	/**
	 * Gets the xml representation of this attribute. Dynamic
	 * attributes require the descriptor's span information.
	 * @param root the xml root
	 * @param descriptorSpan the descriptor's span
	 * @return an element representing this attribute
	 */
	public Element getXMLFormat(Document root, FrameSpan descriptorSpan) {
		Element el = root.createElement("attribute");
		el.setAttribute("name", getName());
		if (!isDynamic()) {
			if (value == null) {
				el.appendChild(nullXML(root));
			} else {
				el.appendChild(((AttributeValue) value).toXML(root));
			}
		} else {
			for (Iterator iter = getValues(descriptorSpan); iter.hasNext();) {
				ValueSpan curr = (ValueSpan) iter.next();
				AttributeValue currVal = (AttributeValue) curr.getValue();
				if (currVal != null) {
					Element child = currVal.toXML(root);
					child.setAttribute("framespan", String.valueOf(curr
							.getStart())
							+ ":" + String.valueOf(curr.getEnd()));
					el.appendChild(child);
				}
			}
		}
		return el;
	}

	/**
	 * Gets the string representation of static Attribute data or an individual
	 * frame of static data.
	 * 
	 * @return a string in proper format of the data value
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(StringHelp.padLeft(16, getName())).append(" : ");
		int width = sb.length() + Attribute.outputWidth;
		if (value == null) {
			sb.append("NULL");
		} else if (!isDynamic()) {
			sb.append('"').append(StringHelp.backslashify(value.toString()))
					.append('"');
		} else {
			AttributeValue[] values = (AttributeValue[]) value;
			if (values.length == 0) {
				sb.append("NULL");
			} else {
				int i = 0;
				while ((i < values.length)
						&& ((Attribute.outputWidth == -1) || (sb.length() <= width))) {
					int multiplier = 0;
					AttributeValue sub = values[i];
					do {
						multiplier++;
						i++;
					} while ((i < values.length)
							&& (values[i] == sub || (sub != null && sub
									.equals(values[i]))));
					String S = (sub == null) ? "NULL" : "\""
							+ StringHelp.backslashify(sub.toString()) + "\"";
					if (multiplier > 1) {
						sb.append(multiplier).append("*(").append(S)
								.append(")");
					} else {
						sb.append(S);
					}
					sb.append(", ");
				}
				if ((Attribute.outputWidth != -1) && (sb.length() > width)) {
					sb.setLength(width - 3);
					sb.append("...");
				} else {
					sb.setLength(sb.length() - 2);
				}
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * Sets the attribute to the given string value. Use this for static
	 * attributes. For dynamic, call
	 * {@link #setValue(String,ErrorWriter,FrameSpan) another setValue}.
	 * 
	 * @param s
	 *            the attribute value
	 * @throws BadDataException
	 */
	public void setValue(String s) throws BadDataException {
		if (!isDynamic()) {
			try {
				if (s != null) {
					value = getArchetype().setValue(s);
				} else {
					value = null;
				}
			} catch (StringIndexOutOfBoundsException sioobx) {
				throw new BadDataException("Misplaced quote mark: " + s);
			} catch (IllegalArgumentException iax) {
				throw new BadDataException(iax.getMessage());
			}
		} else {
			throw new BadDataException(
					"Dynamic attribute requires frame number");
		}
	}

	/**
	 * Gets the value at a specific frame. This is a reference to the internal
	 * value, so treat it carefully. Also, not that is may become severed if
	 * someone calls the setValue method while you aren't paying attention.
	 * 
	 * @param span
	 *            The attributes span. Ignored if this is a static attr.
	 * @param frame
	 *            The frame number's data to return. Ignored for static data.
	 *            Will throw an AIOOBX if the frame isn't inside the span.
	 * @return the value at the specified frame number
	 */
	public AttributeValue getValue(FrameSpan span, int frame) {
		if (isDynamic()) {
			AttributeValue[] values = (AttributeValue[]) value;
			if (null == values) {
				return null;
			} else {
				return values[frame - span.beginning()];
			}
		} else {
			return (AttributeValue) value;
		}
	}

	/**
	 * Iterates over all non-null values of an attribute
	 */
	private static final class DynamicValueIterator implements Iterator {
		private Iterator t;
		private AttributeValue[] vals;
		private int i;
		private ValueSpan n = null;
		private FrameSpan span = null;
		private FrameSpan currSpan = null;
		private int offset;
		private ValueSpan cache() {
			try {
				int firstFrame;
				ValueSpan old = n;
				if (currSpan == null) {
					if (t.hasNext()) {
						currSpan = (FrameSpan) t.next();
						i = currSpan.beginning() - offset;
						firstFrame = currSpan.beginning();
					} else {
						n = null;
						return old;
					}
				} else {
					firstFrame = (int) n.getEnd() + 1;
					i = firstFrame - offset;
				}
				AttributeValue v = vals[i];
				int count = 1;
				while ((i + count < vals.length)
						&& (i + offset <= currSpan.ending())
						&& (v == null ? v == vals[i + count] : v.equals(vals[i
								+ count]))) {
					count++;
				}
				n = new ValueSpan(v, firstFrame, firstFrame + count - 1);
				i = count;
				if (n.getEnd() >= currSpan.ending()) {
					currSpan = null;
				}
				return old;
			} catch (ArrayIndexOutOfBoundsException aioobx) {
				System.err.println("offset = " + offset + ", i = " + i
						+ ", currspan = " + currSpan + ", n = " + n);
				aioobx.printStackTrace();
				throw new IndexOutOfBoundsException(
						"Invalid FrameSpan for attribute that occupies "
								+ vals.length + " frames: " + span);
			}
		}
		DynamicValueIterator(Object v, FrameSpan span) {
			this.span = span;
			t = span.split().iterator();
			vals = (AttributeValue[]) v;
			i = 0;
			offset = span.beginning();
			cache();
		}
		/**
		 * @inheritDoc
		 */
		public Object next() {
			if (n != null) {
				return cache();
			}
			throw new NoSuchElementException();
		}
		/**
		 * @inheritDoc
		 */
		public boolean hasNext() {
			return n != null;
		}
		/**
		 * @inheritDoc
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class StaticValueIterator implements Iterator {
		private Iterator t;
		private AttributeValue val;
		StaticValueIterator(AttributeValue v, FrameSpan span) {
			t = span.split().iterator();
			val = v;
		}
		/**
		 * @inheritDoc
		 */
		public Object next() {
			FrameSpan nv = (FrameSpan) t.next();
			return new ValueSpan(val, nv.beginning(), nv.ending());
		}
		/**
		 * @inheritDoc
		 */
		public boolean hasNext() {
			return t.hasNext();
		}
		/**
		 * @inheritDoc
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Gets an iterator over all values at all frames/times, returning
	 * {@link ValueSpan}objects.
	 * 
	 * @param span
	 *            the attributes span
	 * @return <code>Iterator&lt;ValueSpan&gt;</code>
	 */
	public Iterator getValues(FrameSpan span) {
		if (isDynamic()) {
			return new DynamicValueIterator(value, span);
		} else {
			return new StaticValueIterator(getStaticValue(), span);
		}
	}

	/**
	 * Sets the value of this static attribute.
	 * @param v the new value
	 * @throws BadDataException if this is a dynamic attribute, or 
	 * if the data isn't the right type
	 */
	public void setStaticValue(AttributeValue v) throws BadDataException {
		if (!getArchetype().validate(v)) {
			throw new BadDataException("Not a valid value for " + getName()
					+ ": " + v);
		}
		if (isDynamic()) {
			throw new UnsupportedOperationException(
					"Dynamic attributes: Must specify a frame for the value to set");
		}
		value = v;
	}
	
	/**
	 * Gets the value of this static attribute
	 * @return the value
	 */
	public AttributeValue getStaticValue() {
		if (isDynamic()) {
			throw new UnsupportedOperationException(
					"Dynamic attributes: Must specify a frame for the value to get");
		} else {
			return (AttributeValue) value;
		}
	}

	/**
	 * Changes the framespan of the attribute. Since attributes currently don't
	 * have a link to their framespan, requires both the old and new framespans.
	 * This only has any affect if the attribute is dynamic, in which case it
	 * sets things outside the new framespan to null.
	 * 
	 * @param newSpan
	 *            the new span
	 * @param oldSpan
	 *            the old span
	 * @throws IndexOutOfBoundsException
	 *             when oldSpan is of inappropriate length
	 */
	public void setFrameSpan(FrameSpan newSpan, FrameSpan oldSpan) {
		if (isDynamic() && value != null) {
			AttributeValue[] values = (AttributeValue[]) value;
			if (values.length != oldSpan.size()) {
				throw new IndexOutOfBoundsException(
						"FrameSpan is not "
								+ oldSpan
								+ ", as that is not the proper number of frames, in this case, "
								+ values.length + ".");
			}
			AttributeValue[] newValues = new AttributeValue[newSpan.size()];

			FrameSpan matchSpan = newSpan.intersect(oldSpan);
			int newOffset = matchSpan.beginning() - newSpan.beginning();
			int oldOffset = matchSpan.beginning() - oldSpan.beginning();

			System.arraycopy(values, oldOffset, newValues, newOffset, matchSpan
					.size());

			value = newValues;
		}
	}

	/**
	 * Get a copy with only the specified framespan filled in.
	 * 
	 * @param newSpan
	 *            the new span to crop to
	 * @param oldSpan
	 *            the old span
	 * @return the cropped attribute
	 * @throws IndexOutOfBoundsException
	 */
	public Attribute crop(FrameSpan newSpan, FrameSpan oldSpan) {
		if (isDynamic() && value != null) {
			AttributeValue[] values = (AttributeValue[]) value;
			if (values.length != oldSpan.size()) {
				throw new IndexOutOfBoundsException(
						"FrameSpan is not "
								+ oldSpan
								+ ", as that is not the proper number of frames, in this case, "
								+ values.length + ".");
			}

			AttributeValue[] newValues = new AttributeValue[newSpan.size()];

			FrameSpan matchSpan = newSpan.intersect(oldSpan);
			int newOffset = matchSpan.beginning() - newSpan.beginning();
			int oldOffset = matchSpan.beginning() - oldSpan.beginning();

			// Make a copy, but avoid copying the attribute values
			value = null;
			Attribute copy = (Attribute) clone();
			value = values;

			// Now copy only those values we want
			System.arraycopy(values, oldOffset, newValues, newOffset, matchSpan
					.size());
			copy.value = newValues;
			return copy;
		} else {
			return (Attribute) clone();
		}
	}

	/**
	 * Sets the Attribute to the value indicated by the String over a given
	 * FrameSpan.
	 * 
	 * @param s
	 *            the string containing the new value for the Attribute
	 * @param err
	 *            where to write errors and warnings
	 * @param span
	 *            the numbers of the beginning and end frame; needed for dynamic
	 *            attributes
	 */
	public void setValue(String s, ErrorWriter err, FrameSpan span) {
		if (!isDynamic()) {
			try {
				if (s == null || "NULL".equals(s)) {
					value = null;
				} else {
					if ((s.indexOf('"')) > -1) {
						s = StringHelp.debackslashify(s.substring(s
								.indexOf('"') + 1, s.lastIndexOf('"')));
					} else {
						err.printWarning("This attribute is missing quotes.");
					}
					value = getArchetype().setValue(s);
				}
			} catch (IllegalArgumentException iax) {
				err.printError(iax.getMessage());
				return;
			} catch (StringIndexOutOfBoundsException sioobx) {
				err.printError("Misplaced quote mark: " + s);
			}
		} else {
			int quotingErrors = 0;
			StringTokenizer st = new StringTokenizer(s, ",", true);
			int count = 0;
			AttributeValue[] values = new AttributeValue[span.size()];
			while (st.hasMoreTokens()) {
				int multiplier = -1;
				AttributeValue o = null;
				boolean quoting = false;
				boolean foundStar = false;
				boolean foundQuote = false;
				String t = st.nextToken();
				StringBuffer buff = null;
				StringBuffer filler = new StringBuffer();
				try {
					if (t.charAt(0) == ',') {
						t = st.nextToken();
					}
				} catch (NoSuchElementException nsex) {
					err.printWarning("Extra comma found at end of line.");
					break;
				}
				try {
					buff = new StringBuffer(t.length());
					filler.delete(0, filler.length());
					do {
						int i = 0;
						while (i < t.length()) {
							char c = t.charAt(i++);
							switch (c) {
								case '\\' :
									c = t.charAt(i++);
									switch (c) {
										case 'n' :
											buff.append('\n');
											break;
										default :
											buff.append(c);
									}
									break;
								case '"' :
									quoting = !quoting;
									foundQuote = true;
									break;
								case '*' :
									if (quoting) {
										buff.append(c);
									} else {
										multiplier = Integer.parseInt(buff
												.toString().trim());
										buff.delete(0, buff.length());
										foundStar = true;
									}
								default :
									if (quoting) {
										buff.append(c);
									} else if (multiplier == -1 && c != ' '
											&& c != '\t') {
										buff.append(c);
									} else {
										filler.append(c);
									}
							}
						}
						if (quoting) {
							t = st.nextToken();
						}
					} while (quoting);
					if (!foundStar) {
						multiplier = 1;
					}
					try {
						if (foundStar && !foundQuote) {
							// This is a case where the user is probably using
							// the
							// old data format. So, strip the parens off filler!
							t = filler.toString();
							String temp = t.substring(t.indexOf('(') + 1, t
									.lastIndexOf(')'));
							// NULL should not be enclosed in quotes, in case
							// the text of an svalue is "NULL"
							if (temp.equalsIgnoreCase("NULL")) {
								o = null;
								foundQuote = true;
							} else {
								o = getArchetype().setValue(temp);
							}
						} else if (!foundQuote
								&& buff.toString().equalsIgnoreCase("NULL")) {
							o = null;
							foundQuote = true;
						} else {
							String temp = StringHelp.debackslashify(buff
									.toString());
							if (temp.equalsIgnoreCase("NULL")) {
								o = null;
							} else {
								o = getArchetype().setValue(temp);
							}
						}
					} catch (IllegalArgumentException iax) {
						err.printError(iax.getMessage());
						o = null;
					}
					if (!foundQuote) {
						quotingErrors++;
					}
				} catch (NoSuchElementException nsex) {
					nsex.printStackTrace();
					try {
						if ((buff == null) || (buff.length() < 1)) {
							err.printError("Unexpected end of line.");
							return;
						} else if (!foundStar) {
							multiplier = 1;
							o = getArchetype().setValue(buff.toString());
						} else {
							t = filler.toString();
							o = getArchetype().setValue(
									t.substring(t.indexOf('('), t
											.lastIndexOf(')')));
						}
						err
								.printWarning("Badly formed quotes, just found "
										+ o);
					} catch (IllegalArgumentException iax) {
						err
								.printError("Badly formed quotes; possible cause of error:\n "
										+ iax.getMessage());
						o = null;
					}
				}

				if (multiplier > 1) {
					if (multiplier + count > values.length) {
						err.printError("Data exceeds number of frames");
						multiplier = values.length - count;
					}
					Arrays.fill(values, count, count + multiplier, o);
				} else {
					values[count] = o;
				}
				count += multiplier;
			}
			if (count < values.length) {
				err
						.printError("Data not sufficient for number of frames; found "
								+ count + ", " + values.length + " required");
			}
			if (quotingErrors == 1) {
				err.printWarning("This attribute is missing quotes.");
			} else if (quotingErrors > 1) {
				err.printWarning(quotingErrors + " cases of missing quotes.");
			}
			value = values;
		}
	}

	/**
	 * Sets the value of this attribute, using the given string.
	 * @see #setValue(String, ErrorWriter, FrameSpan)
	 * @param s the value
	 * @param err where to write any errors in the value's format
	 * @param span the span
	 * @param neighbors the enclosing desc vector
	 */
	public void setValue(String s, ErrorWriter err, FrameSpan span,
			DescVector neighbors) {
		setValue(s, err, span);
	}

	/**
	 * Sets the value of the attribute from the given DOM node.
	 * @param el the attribute dom node to parse
	 * @param span the descriptor's frame span
	 * @param neighbors the descriptor list
	 * @throws BadDataException
	 */
	public void setValue(Element el, FrameSpan span, DescVector neighbors)
			throws BadDataException {
		setValue(el, span);
	}

	/**
	 * Sets the value of the attribute from the given DOM node.
	 * @param el the attribute dom node to parse
	 * @param span the descriptor's frame span
	 * @throws BadDataException
	 */
	public void setValue(Element el, FrameSpan span) throws BadDataException {
		Iterator vals = XmlHelper.nodeList2Iterator(el.getChildNodes());
		vals = new ExceptIterator(XmlHelper.ELEMENTS_ONLY, vals);
		
		if (!isDynamic()) {
			if (!vals.hasNext()) {
				value = null;
			} else {
				Element vEl = (Element) vals.next();
				if (vals.hasNext()) {
					throw new BadDataException(
							"Should be one value for a static attribute '"
									+ this.getName() + "'.");
				}
				if ("null".equals(vEl.getLocalName())) {
					value = null;
				} else {
					try {
						AttributeValue arch = getArchetype();
						value = arch.setValue(vEl);
					} catch (IllegalArgumentException iax) {
						throw new BadDataException(iax.getMessage());
					}
				}
			}
		}

		// Count the data items up.
		else {
			int count = 0;
			int offset = span.beginning();
			boolean specificSpans = true;
			AttributeValue[] values = new AttributeValue[span.size()];
			String badDataErrors = "";
			while(vals.hasNext()) {
				Element curr = (Element) vals.next();
				if (curr.hasAttribute("framespan")) {
					FrameSpan cspan = FrameSpan.parseFrameSpan(curr
							.getAttribute("framespan"));
					cspan.intersectWith(span);
					if (cspan.isEmpty()) {
						continue;
					}
					AttributeValue o;
					try {
						o = getArchetype().setValue(curr);
					} catch (IllegalArgumentException iax) {
						badDataErrors += iax.getMessage() + "\n";
						o = null;
					}
					count = cspan.ending() + 1 - offset;
					Arrays.fill(values, cspan.beginning() - offset, count, o);
				} else {
					String spanString = curr.getAttribute("span");
					specificSpans = false;
					int multiplier = 1;
					if (spanString.length() > 0) {
						try {
							multiplier = Integer.parseInt(spanString);
						} catch (NumberFormatException nfx) {
							throw new BadDataException("Not a valid span: "
									+ span);
						}
					}
					if (count + multiplier > values.length) {
						throw new BadDataException(
								"Data exceeds number of frames");
					}
					if ("null".equals(curr.getLocalName())) {
						Arrays.fill(values, count, count + multiplier, null);
					} else {
						AttributeValue o;
						try {
							o = getArchetype().setValue(curr);
						} catch (IllegalArgumentException iax) {
							badDataErrors += iax.getMessage() + "\n";
							o = null;
						}
						Arrays.fill(values, count, count + multiplier, o);
					}
					count += multiplier;
				}
			}
			if (!specificSpans && count < values.length) {
				throw new BadDataException(
						"Data insufficient for number of frames; found "
								+ count + ", " + span + " required");
			}
			value = values;
			if (badDataErrors.length() > 0) {
				throw new BadDataException(badDataErrors);
			}
		}
	}

	String notInDescriptorsError(String undefDescriptorName, int bf, int ef) {
		return ("\nWARNING: Descriptor '" + undefDescriptorName
				+ "' not defined\n" + "       : " + undefDescriptorName + " "
				+ bf + ":" + ef + "\n");
	}

	String notAnAttributeError(String undefAttributeName, String name, int bf,
			int ef) {
		return ("\nWARNING: Attribute '" + undefAttributeName
				+ "' not defined\n" + "       : " + name + " " + bf + ":" + ef + "\n");
	}

	String notInAttributesError(String missingAttr, String descName, int bf,
			int ef, String attribName) {
		return ("\nWARNING: Attribute " + attribName + " has no member '"
				+ missingAttr + "'\n       : " + descName + " " + bf + ":" + ef + "\n");
	}

	String wrongAttribInResult(String S) {
		if (!errors) {
			System.out.print("\n\n\n***  ERRORS DETECTED. See LOG ***\n\n");
			errors = true;
		}
		return ("\nERROR: Mapping for '" + S + "' was ignored because\n" + "\tit was not in the Ground Truth Configuration File\n");
	}

	/**
	 * @inheritDoc 
	 * @return true when it is "==" or "!="
	 */
	public boolean isValidRule(String rule) {
		return "==".equals(rule) || "!=".equals(rule);
	}

	/**
	 * This converts a rule into the proper format for the data type. Each
	 * subclass of this class must implement this function if they want to be
	 * able to use the rule-based filtering system.
	 * 
	 * @param unparsedRule
	 *            the rule to convert
	 * @param unparsedValues
	 *            the values
	 * @param err
	 *            the error stream
	 * @return the rule for this attribute
	 * @throws BadDataException
	 *             if there is an error in the rule or values
	 */
	public Filterable.Rule convertRule(String unparsedRule,
			List unparsedValues, ErrorWriter err) throws BadDataException {
		if (unparsedValues.size() != 1) {
			throw new BadDataException(
					"This attribute's rules take one argument: " + unparsedRule);
		}
		try {
			String val = (String) unparsedValues.get(0);
			if ("==".equals(unparsedRule)) {
				AttributeValue av = getArchetype().setValue(val);
				return Rules.getEquality(av);
			} else if ("!=".equals(unparsedRule)) {
				AttributeValue av = getArchetype().setValue(val);
				return Rules.getInequality(av);
			} else {
				err
						.printError("This attribute type only supports equality/inequality filtering.");
				return Rules.getTrue();
			}
		} catch (IllegalArgumentException iax) {
			throw new BadDataException(iax.getMessage());
		}
	}

	/**
	 * Since dyanmic attributes need to evaluate rules multiple times, they have
	 * their own evaluation that evaluates the rule on the value returned by
	 * <code>getValue</code> for each of the frames.
	 * 
	 * @param rule
	 *            the rule to test against
	 * @return <code>true</code> if at least one frame's attribute passes the
	 *         rule.
	 */
	public final boolean passes(Filterable.Rule rule) {
		if (isDynamic()) {
			AttributeValue[] values = (AttributeValue[]) value;
			boolean passing = false;
			Object previous = null;
			if (values.length > 0 && values[0] == null) {
				previous = this; // just something that is guaranteed to not be
								 // in the list
			}
			if (rule.isShortCircuit()) {
				for (int i = 0; i < values.length; i++) {
					if (previous != values[i]) {
						previous = values[i];
						if (rule.passes(values[i])) {
							return true;
						}
					}
				}
			} else {
				for (int i = 0; i < values.length; i++) {
					if (previous != values[i]) {
						previous = values[i];
						passing = rule.passes(values[i]) || passing;
					}
				}
			}
			return passing;
		} else {
			return rule.passes((AttributeValue) value);
		}
	}

	/**
	 * Gets the string representation of the value.
	 * @return the value of the attribute, encoded as a string
	 */
	public String getValueToString() {
		if (null == value)
			return "NULL";
		Object o = value;
		if (isDynamic())
			o = ((Object[]) value)[0];
		return (null == o) ? "NULL" : o.toString();
	}

	/**
	 * Sets the extra config information. It is a map
	 * from the first string to the remaining strings.
	 * @param list the map
	 * @throws MethodNotSupportedException if the attribute
	 * does not entail extended information
	 * @throws BadDataException
	 */
	public void setExtraConfig(Map list) throws MethodNotSupportedException,
			BadDataException {
		throw new MethodNotSupportedException(
				"This type has no extra config info.");
	}
	
	/**
	 * Gets the extra config information.
	 * @return an empty map, or a map from extended information
	 * names to strings
	 */
	public Map getExtraConfig() {
		return new TreeMap();
	}

	/**
	 * Reads in the extended information string.
	 * @param value the extedned information.
	 * @return the extended information map
	 * @throws BadDataException
	 */
	public Map parseExtraConfig(String value) throws BadDataException {
		throw new BadDataException("Elements of type " + getType()
				+ " do not take any extra viper-config information.");
	}

	protected void setDefaultValue(String defaultValue) throws BadDataException {
		AttributeValue def = getArchetype().setValue(defaultValue);
		resetDefault(def);
	}

	protected void setDefaultValue(Element defaultValue)
			throws BadDataException, MethodNotSupportedException {
		AttributeValue def = getArchetype().setValue(defaultValue);
		resetDefault(def);
	}
}