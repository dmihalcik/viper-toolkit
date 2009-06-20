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

import viper.comparison.distances.*;
import viper.descriptors.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This attribute represents a Ellipse. It has the same form as a bounding box.
 */
public class Attribute_ellipse extends Attribute implements Cloneable {
	static {
		Distances.putDistanceFunctorFor("ellipse", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("ellipse", "e");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("ellipse", 0.0);
	}

	/**
	 * @inheritDoc
	 * @return "ellipse"
	 */
	public String getType() {
		return "ellipse";
	}
	private static final EllipseValue arch = new EllipseValue();
	
	/**
	 * Constructs a new static ellipse-valued attribute.
	 */
	public Attribute_ellipse() {
		super();
		setArchetype(arch);
	}

	/**
	 * Constructs a new ellipse-valued attribute.
	 * @param dynamic if the attribute may change as a
	 * function of the frame number
	 */
	public Attribute_ellipse(boolean dynamic) {
		super(dynamic);
		setArchetype(arch);
	}

	/**
	 * Copies the old attribute.
	 * @param old the attribute to copy
	 */
	public Attribute_ellipse(Attribute old) {
		super(old);
	}

	/** @inheritDoc */
	public Object clone() {
		return new Attribute_ellipse(this);
	}

	/** @inheritDoc */
	public boolean possibleValueOf(String S) {
		StringTokenizer st = new StringTokenizer(getType());
		for (int i = 0; i < 4; i++) {
			if (!st.hasMoreTokens())
				return false;
			try {
				Integer.parseInt(st.nextToken());
			} catch (NumberFormatException nfx) {
				return false;
			}
		}
		return !st.hasMoreTokens();
	}

	protected static class EllipseValue implements AttributeValue {
		Ellipse value;
		/** @inheritDoc */
		public String toString() {
			return value.toString();
		}
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof EllipseValue) {
				return value.equals(((EllipseValue) o).value);
			} else {
				return false;
			}
		}
		/** @inheritDoc */
		public int hashCode() {
			return value.hashCode();
		}

		/** @inheritDoc */
		public Measurable.Difference getDifference(Measurable beta,
				Measurable blackout, Measurable ignore,
				CanonicalFileDescriptor cfd) {
			return getDifference(beta, blackout, ignore, cfd, null);
		}

		/** @inheritDoc */
		public Measurable.Difference getDifference(Measurable beta,
				Measurable blackout, Measurable ignore,
				CanonicalFileDescriptor cfd, Measurable.Difference old) {
			return new Distances.DefaultDifference(value, helpGetValue(beta),
					helpGetValue(blackout), helpGetValue(ignore), cfd);
		}
		
		private final Ellipse helpGetValue(Measurable m) {
			return ((EllipseValue) m).value;
		}
		
		/** @inheritDoc */
		public String getType() {
			return "ellipse";
		}

		/**
		 * Sets the state of the object to the data the xml-dom element
		 * represents.
		 * 
		 * @param el
		 *            DOM Node to parse
		 * @return the parsed value
		 * @throws IllegalArgumentException
		 *             If the data is ill-formed
		 */
		public AttributeValue setValue(Element el) {
			if (!el.getTagName().endsWith("ellipse")) {
				throw new IllegalArgumentException("Unexpected data type: "
						+ el.getTagName());
			} else {
				try {
					EllipseValue copy = new EllipseValue();
					copy.value = new Ellipse(parseAnInt(el, "x"), parseAnInt(
							el, "y"), parseAnInt(el, "width"), parseAnInt(el,
							"height"), parseAnInt(el, "rotation"));
					return copy;
				} catch (BadDataException bdx) {
					throw new IllegalArgumentException(bdx.getMessage());
				}
			}
		}

		/**
		 * Sets the state of the object to the data the String represents.
		 * Useful for old GTF format Should be able to run setValue(toString())
		 * and have it come out the same. Should try to use XML format whenever
		 * possible.
		 * 
		 * @param S
		 *            String representation of this type of value.
		 * @return the parsed value
		 * @throws IllegalArgumentException
		 *             If the data is ill-formed
		 */
		public AttributeValue setValue(String S) {
			try {
				StringTokenizer st = new StringTokenizer(S);
				EllipseValue copy = new EllipseValue();
				copy.value = new Ellipse(Integer.parseInt(st.nextToken()),
						Integer.parseInt(st.nextToken()), Integer.parseInt(st
								.nextToken()),
						Integer.parseInt(st.nextToken()), Integer.parseInt(st
								.nextToken()));
				return copy;
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("Bad " + getType() + " - "
						+ S);
			} catch (NoSuchElementException nsex) {
				throw new IllegalArgumentException(
						"Ellipse requires four integers : " + S);
			}
		}

		/**
		 * Returns an xml element for this object. It should be in the namespace
		 * Attribute.DEFAULT_NAMESPACE_QUALIFIER with a qualified name in the
		 * form <i><code>Attribute.DEFAULT_NAMESPACE_QUALIFIER</code> :type
		 * </i>.
		 * 
		 * @param doc
		 *            The root for the element.
		 * @return New DOM element for this data.
		 */
		public Element toXML(Document doc) {
			Element el = doc.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			el.setAttribute("x", String.valueOf(value.getX()));
			el.setAttribute("y", String.valueOf(value.getY()));
			el.setAttribute("width", String.valueOf(value.getWidth()));
			el.setAttribute("height", String.valueOf(value.getHeight()));
			el.setAttribute("rotation", String.valueOf(value.getRotation()));
			return el;
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check
		 * @return true iff the type meets with the attribute configuration
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_ellipse.EllipseValue;
		}

	}
}