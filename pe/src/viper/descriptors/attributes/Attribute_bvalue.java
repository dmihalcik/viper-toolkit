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
import viper.descriptors.*;

/**
 * This attribute represents a boolean value.
 */
public class Attribute_bvalue extends Attribute {
	static {
		Distances.putDistanceFunctorFor("bvalue", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("bvalue", "E");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("bvalue", 0.0);
	}

	/**
	 * @inheritDoc
	 */
	public String getType() {
		return "bvalue";
	}

	private static final BooleanValue arch = new BooleanValue();
	
	/**
	 * Constructs a new static boolean value attribute.
	 */
	public Attribute_bvalue() {
		super();
		setArchetype(arch);
	}
	
	/**
	 * Constructs a new boolean value attribute.
	 * @param dynamic if the attribute is dynamic
	 */
	public Attribute_bvalue(boolean dynamic) {
		super(dynamic);
		setArchetype(arch);
	}

	/**
	 * Constructs a new copy of the old boolean attribute.
	 * @param old the old attribute
	 */
	public Attribute_bvalue(Attribute old) {
		super(old);
		setArchetype(old.getArchetype());
	}

	/**
	 * @inheritDoc
	 */
	public Object clone() {
		return new Attribute_bvalue(this);
	}

	/**
	 * @inheritDoc
	 */
	public boolean possibleValueOf(String S) {
		return S.equalsIgnoreCase("false") || S.equalsIgnoreCase("true");
	}

	private static final class BooleanValue implements AttributeValue {
		private boolean value;
		/** @inheritDoc */
		public String toString() {
			return String.valueOf(value).toUpperCase();
		}
		
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this)
				return true;
			else if (o instanceof BooleanValue)
				return value == ((BooleanValue) o).value;
			else
				return false;
		}
		
		/** @inheritDoc */
		public int hashCode() {
			return value ? 1 : 0;
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
		public String getType() {
			return "bvalue";
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
			if (!el.getTagName().endsWith("bvalue")) {
				throw new IllegalArgumentException(
						"Unexpected data type (not bvalue): " + el.getTagName());
			} else {
				BooleanValue copy = new BooleanValue();
				copy.value = "true".equalsIgnoreCase(el.getAttribute("value"));
				return copy;
			}
		}

		/** @inheritDoc */
		public AttributeValue setValue(String S)
				throws IllegalArgumentException {
			BooleanValue copy = new BooleanValue();
			copy.value = "true".equalsIgnoreCase(S);
			return copy;
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
			el.setAttribute("value", value ? "true" : "false");
			return el;
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check
		 * @return true iff the value is an instance of this attribute's type
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_bvalue.BooleanValue;
		}

	}
}