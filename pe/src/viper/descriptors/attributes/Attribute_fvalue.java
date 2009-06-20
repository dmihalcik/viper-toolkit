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

import org.w3c.dom.*;

import viper.comparison.distances.*;

/**
 * This attribute represents a doubleing point value.
 */
public class Attribute_fvalue extends Attribute_number implements Cloneable {
	static {
		Distances.useSameDistances("fvalue", "number");
		Distances.HelperAttrDistance d;

		d =
			new Distances.HelperAttrDistance(
				new DifferenceDistance(),
				"difference",
				Distance.BALANCED,
				"Normalized difference",
				true);
		Distances.putDistanceFunctorFor("fvalue", d);
		try {
			DefaultMeasures.setDefaultMetricFor("fvalue", "difference");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("fvalue", 0.0);
	}
	private static class DifferenceDistance
		implements Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			double target = ((Double) D.getAlpha()).doubleValue();
			double candidate = ((Double) D.getBeta()).doubleValue();
			return new Double(
				Distances.infiniteDistanceRangeToClosed(
					Math.abs(target - candidate),
					1.0));
		}
	}
	private static final FloatValue arch = new FloatValue();

	/**
	 * @inheritDoc 
	 * @return "fvalue"
	 */
	public String getType() {
		return "fvalue";
	}

	/**
	  Create an empty Attribute. toString will return an empty String until more is known.
	 */
	public Attribute_fvalue() {
		super();
		setArchetype(arch);
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with
	 * mobile set to true. Other Descriptor types can use this with
	 * mobile set to false.
	 *
	 * @param mobile true iff Descriptor that holds this Attribute is
	 *      of type OBJECT and not [static]
	 */
	public Attribute_fvalue(boolean mobile) {
		super(mobile);
		setArchetype(arch);
	}

	/**
	 * Copies the old attribute
	 * @param old the old attribute
	 */
	public Attribute_fvalue(Attribute old) {
		super(old);
	}

	/** @inheritDoc */
	public Object clone() {
		return new Attribute_fvalue(this);
	}

	/**
	 * Determines if this Attribute can take the value specified. 
	 * @param S - the string to be tested (must be a valid java double)
	 * @return whether or not the string is valid
	 */
	public boolean possibleValueOf(String S) {
		try {
			Double.valueOf(S);
			return (true);
		} catch (NumberFormatException nfx) {
			return (false);
		}
	}

	static class FloatValue extends Attribute_number.NumberValue {
		private double value;
		
		/** @inheritDoc */
		public String toString() {
			return String.valueOf(value);
		}
		
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this)
				return true;
			else if (o instanceof Number)
				return value == ((Number) o).doubleValue();
			else
				return false;
		}
		
		/** @inheritDoc */
		public int compareTo(Object obj) {
			return (int) (value - ((Number) obj).doubleValue());
		}

		/** @inheritDoc */
		public double doubleValue() {
			return value;
		}
		
		/** @inheritDoc */
		public float floatValue() {
			return (float) value;
		}
		
		/** @inheritDoc */
		public int intValue() {
			return (int) value;
		}
		
		/** @inheritDoc */
		public long longValue() {
			return (long) value;
		}

		/** @inheritDoc */
		public String getType() {
			return "fvalue";
		}

		/** @inheritDoc */
		public AttributeValue setValue(Element el)
			throws IllegalArgumentException {
			if (!el.getTagName().endsWith("fvalue")) {
				throw new IllegalArgumentException(
					"Unexpected data type (not dvalue): " + el.getTagName());
			} else {
				FloatValue copy = new FloatValue();
				copy.value = Double.parseDouble(el.getAttribute("value"));
				return copy;
			}
		}

		/** @inheritDoc */
		public AttributeValue setValue(String S)
			throws IllegalArgumentException {
			FloatValue copy = new FloatValue();
			copy.value = Double.parseDouble(S);
			return copy;
		}

		/** @inheritDoc */
		public Element toXML(Document doc) {
			Element el =
				doc.createElementNS(
					Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			el.setAttribute("value", String.valueOf(value));
			return el;
		}
		
		/** @inheritDoc */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_fvalue.FloatValue;
		}
	}
}