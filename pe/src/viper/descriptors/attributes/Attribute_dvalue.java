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
 * This attribute represents an integral value.
 */
public class Attribute_dvalue extends Attribute_number implements Cloneable {
	static {
		Distances.useSameDistances("dvalue", "number");
		try {
			DefaultMeasures.setDefaultMetricFor("dvalue", "difference");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("dvalue", 0.0);
	}

	/**
	 * @inheritDoc
	 * @return "dvalue"
	 */
	public String getType() {
		return "dvalue";
	}

	private static final IntegerValue arch = new IntegerValue();

	/**
	 * Create an empty static integer-valued Attribute.
	 */
	public Attribute_dvalue() {
		super();
		setArchetype(arch);
	}

	/**
	 * Constructs an integer-values attribute.
	 * @param dynamic if the attribute may take on different values
	 * at different frames
	 */
	public Attribute_dvalue(boolean dynamic) {
		super(dynamic);
		setArchetype(arch);
	}

	/**
	 * Copies the old descriptor.
	 * @param old the descriptor to copy
	 */
	public Attribute_dvalue(Attribute old) {
		super(old);
	}

	/** @inheritDoc */
	public Object clone() {
		return new Attribute_dvalue(this);
	}

	/** @inheritDoc */
	public boolean possibleValueOf(String S) {
		try {
			Integer.parseInt(S);
			return true;
		} catch (NumberFormatException nfx) {
			return false;
		}
	}

	static class IntegerValue extends Attribute_number.NumberValue {
		private int value;
		/** @inheritDoc */
		public String toString() {
			return String.valueOf(value);
		}
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this)
				return true;
			else if (o instanceof Number)
				return value == ((Number) o).intValue();
			else
				return false;
		}
		
		/** @inheritDoc */
		public int compareTo(Object obj) {
			return value - ((Number) obj).intValue();
		}

		/** @inheritDoc */
		public double doubleValue() {
			return value;
		}
		/** @inheritDoc */
		public float floatValue() {
			return value;
		}
		/** @inheritDoc */
		public int intValue() {
			return value;
		}
		/** @inheritDoc */
		public long longValue() {
			return value;
		}
		/** @inheritDoc */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_dvalue.IntegerValue;
		}

		/** @inheritDoc */
		public String getType() {
			return "dvalue";
		}

		/** @inheritDoc */
		public AttributeValue setValue(Element el)
			throws IllegalArgumentException {
			if (!el.getTagName().endsWith("dvalue")) {
				throw new IllegalArgumentException(
					"Unexpected data type (not dvalue): " + el.getTagName());
			} else {
				IntegerValue copy = new IntegerValue();
				copy.value = Integer.parseInt(el.getAttribute("value"));
				return copy;
			}
		}

		/** @inheritDoc */
		public AttributeValue setValue(String S)
			throws IllegalArgumentException {
			IntegerValue copy = new IntegerValue();
			copy.value = Integer.parseInt(S);
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
	}
}
