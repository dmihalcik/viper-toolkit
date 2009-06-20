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
 * This attribute represents a circle. It has the form (center) (radius), where
 * center is two integers, the x-coord and the y-coord. For example, a circle
 * located 10 units above the origin and 20 units to the right with a radius of
 * 3 would be represented as <code>10 20 3</code>.
 */
public class Attribute_circle extends Attribute implements Cloneable {
	static {
		Distances.HelperAttrDistance d;

		d = new Distances.HelperAttrDistance(new DiceDistance(), "dice",
				Distance.BALANCED, "Dice coefficient", true);
		Distances.putDistanceFunctorFor("circle", d);

		d = new Distances.HelperAttrDistance(new OverlapDistance(), "overlap",
				Distance.TARG_V_CANDS, "Target overlap", true);
		Distances.putDistanceFunctorFor("circle", d);
		Distances.putDistanceFunctorFor("circle", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("circle", "dice");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("circle", 0.0);
	}
	private static class DiceDistance implements Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			Circle target = (Circle) D.getAlpha();
			Circle candidate = (Circle) D.getBeta();

			return new Double(
					1 - ((2.0 * target.intersectArea(candidate)) / (target
							.area() + candidate.area())));

		}
	}
	private static class OverlapDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			Circle target = (Circle) D.getAlpha();
			Circle candidate = (Circle) D.getBeta();

			return new Double(1 - (target.intersectArea(candidate) / target
					.area()));

		}
	}

	/** 
	 * @inheritDoc
	 * @return "circle" 
	 */
	public String getType() {
		return "circle";
	}

	private static final CircleValue arch = new CircleValue();
	
	/**
	 * Constructs a new static circle-valued attribute.
	 */
	public Attribute_circle() {
		super();
		setArchetype(arch);
	}

	/**
	 * Constructs a new circle-valued attribute.
	 * @param dynamic if the circle is dynamic
	 */
	public Attribute_circle(boolean dynamic) {
		super(dynamic);
		setArchetype(arch);
	}

	/**
	 * Copies the old attribute
	 * @param old the old attribute
	 */
	public Attribute_circle(Attribute old) {
		super(old);
	}

	/**
	 * @inheritDoc
	 */
	public Object clone() {
		return new Attribute_circle(this);
	}

	/** @inheritDoc */
	public boolean possibleValueOf(String S) {
		StringTokenizer st = new StringTokenizer(getType());
		for (int i = 0; i < 3; i++) {
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

	protected static class CircleValue implements AttributeValue {
		private Circle value;
		/** @inheritDoc */
		public String toString() {
			return value.toString();
		}
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof CircleValue) {
				return value.equals(((CircleValue) o).value);
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
		private final Circle helpGetValue(Measurable m) {
			return ((CircleValue) m).value;
		}
		/** @inheritDoc */
		public String getType() {
			return "circle";
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
			if (!el.getTagName().endsWith("circle")) {
				throw new IllegalArgumentException("Unexpected data type: "
						+ el.getTagName());
			} else {
				try {
					CircleValue copy = new CircleValue();
					copy.value = new Circle(parseAnInt(el, "x"), parseAnInt(el,
							"y"), parseAnInt(el, "radius"));
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
				CircleValue copy = new CircleValue();
				copy.value = new Circle(Integer.parseInt(st.nextToken()),
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
			el.setAttribute("x", String.valueOf(value.getCenter().getX()));
			el.setAttribute("y", String.valueOf(value.getCenter().getY()));
			el.setAttribute("radius", String.valueOf(value.getRadius()));
			return el;
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check against this attribute's configuration
		 * @return true iff the value is of the appropriate type
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_circle.CircleValue;
		}

	}
}