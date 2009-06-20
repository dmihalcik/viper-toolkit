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
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This attribute represents a point in the video.
 */
public class Attribute_point extends Attribute implements Cloneable {
	private static final PointValue archSV = new PointValue();
	static {
		Distances.HelperAttrDistance d;

		d = new Distances.HelperAttrDistance(new EuclideanDistance(),
				"euclidean", Distance.BALANCED,
				"Normalized Euclidean distance", true);
		Distances.putDistanceFunctorFor("point", d);

		d = new Distances.HelperAttrDistance(new ManhattanDistance(),
				"manhattan", Distance.BALANCED,
				"Normalized Manhattan distance", true);
		Distances.putDistanceFunctorFor("point", d);
		Distances.putDistanceFunctorFor("point", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("point", "Euclidean");
		} catch (ImproperMetricException imx) {
			throw new IllegalArgumentException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("point", 0.0);
	}
	private static class EuclideanDistance
			implements
				Distances.QuickValueDistance {
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			int[] testPoint = new int[2];
			testPoint[0] = ((PointValue) D.getAlpha()).getX()
					- ((PointValue) D.getBeta()).getX();
			testPoint[1] = ((PointValue) D.getAlpha()).getY()
					- ((PointValue) D.getBeta()).getY();
			double dist = Math.sqrt(testPoint[0] * testPoint[0] + testPoint[1]
					* testPoint[1]);
			dist = Distances.infiniteDistanceRangeToClosed(dist, 1.0);
			return new Double(dist);
		}
	}
	private static class ManhattanDistance
			implements
				Distances.QuickValueDistance {
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			int dist = Math.abs(((PointValue) D.getAlpha()).getX()
					- ((PointValue) D.getBeta()).getX());
			dist += Math.abs(((PointValue) D.getAlpha()).getY()
					- ((PointValue) D.getBeta()).getY());
			return new Double(Distances
					.infiniteDistanceRangeToClosed(dist, 1.0));
		}
	}

	
	/** 
	 * @inheritDoc
	 * @return "point" 
	 */
	public String getType() {
		return "point";
	}

	/**
	 * Create an empty Attribute. toString will return an empty String until
	 * more is known.
	 */
	public Attribute_point() {
		super();
		setArchetype(archSV);
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with mobile set
	 * to true. Other Descriptor types can use this with mobile set to false.
	 * 
	 * @param mobile
	 *            Descriptor that holds this Attribute is of type OBJECT
	 */
	public Attribute_point(boolean mobile) {
		super(mobile);
		setArchetype(archSV);
	}

	/**
	 * Copies the old attribute.
	 * @param old the attribute to copy
	 */
	public Attribute_point(Attribute old) {
		super(old);
		setArchetype(archSV);
	}

	/**
	 * Returns a clone of this Attribute.
	 * 
	 * @return a reference to a new Attribute with all the values of the
	 *         original
	 */
	public Object clone() {
		return (new Attribute_point(this));
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
		StringTokenizer st = new StringTokenizer(getType());
		for (int i = 0; i < 2; i++) {
			if (!st.hasMoreTokens())
				return (false);
			try {
				Integer.parseInt(st.nextToken());
			} catch (NumberFormatException nfx) {
				return (false);
			}
		}
		return (!st.hasMoreTokens());
	}

	static class PointValue implements AttributeValue {
		private int[] value;
		
		/** @inheritDoc */
		public String toString() {
			return String.valueOf(value[0]) + " " + String.valueOf(value[1]);
		}
		
		/** @inheritDoc */
		public Element toXML(Document doc) {
			Element el = doc.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			el.setAttribute("x", String.valueOf(value[0]));
			el.setAttribute("y", String.valueOf(value[1]));
			return el;
		}
		
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof PointValue) {
				PointValue other = (PointValue) o;
				if (value == null) {
					return other.value == null;
				} else if (other.value == null) {
					return false;
				} else {
					return value[0] == other.value[0]
							&& value[1] == other.value[1];
				}
			} else {
				return false;
			}
		}
		
		/** @inheritDoc */
		public int hashCode() {
			return value == null ? 0 : value[0] ^ value[1];
		}
		
		/** @inheritDoc */
		public String getType() {
			return "point";
		}
		
		/** @inheritDoc */
		public Measurable.Difference getDifference(Measurable beta,
				Measurable blackout, Measurable ignore,
				CanonicalFileDescriptor cfd) {
			return new Distances.DefaultDifference(this, beta, blackout,
					ignore, cfd);
		}
		
		/** @inheritDoc */
		public Measurable.Difference getDifference(Measurable beta,
				Measurable blackout, Measurable ignore,
				CanonicalFileDescriptor cfd, Measurable.Difference old) {
			return new Distances.DefaultDifference(this, beta, blackout,
					ignore, cfd);
		}
		
		/** @inheritDoc */
		public AttributeValue setValue(Element el)
				throws IllegalArgumentException {
			if (!el.getTagName().endsWith("point")) {
				throw new IllegalArgumentException("Unexpected data type: "
						+ el.getTagName());
			} else {
				PointValue copy = new PointValue();
				copy.value = new int[2];
				try {
					copy.value[0] = parseAnInt(el, "x");
					copy.value[1] = parseAnInt(el, "y");
				} catch (BadDataException bdx) {
					throw new IllegalArgumentException("Error in point tag: "
							+ bdx.getMessage());
				}
				return copy;
			}
		}
		
		/** @inheritDoc */
		public AttributeValue setValue(String S)
				throws IllegalArgumentException {
			StringTokenizer st = new StringTokenizer(S);
			PointValue copy = new PointValue();
			copy.value = new int[2];
			copy.value[0] = Integer.parseInt(st.nextToken());
			copy.value[1] = Integer.parseInt(st.nextToken());
			if (st.hasMoreTokens()) {
				throw new IllegalArgumentException("Malformed point value: "
						+ S);
			}
			return copy;
		}
		
		/** 
		 * Gets the x value.
		 * @return the x coordinate of the point 
		 */
		public int getX() {
			return value[0];
		}
		
		/** 
		 * Gets the y value.
		 * @return the y coordinate of the point 
		 */
		public int getY() {
			return value[1];
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to validate
		 * @return true iff the value is valid according to this attribute's
		 *         configuration
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_point.PointValue;
		}
	}
}