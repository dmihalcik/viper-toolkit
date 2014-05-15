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
 * This attribute represents an Enumerated list element. This can be one value
 * of any specified in the config file.
 */
public class Attribute_lvalue extends Attribute implements Cloneable {	
	public static final String LOCAL_TYPE_NAME = "lvalue";
	public static final String TYPE = Attributes.DEFAULT_NAMESPACE_URI + LOCAL_TYPE_NAME;
	
	

	static {
		Distances.putDistanceFunctorFor(TYPE, Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor(TYPE, "E");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor(TYPE, 0.0);
	}

	/**
	 * @inheritDoc
	 * @return "lvalue"
	 */
	public String getType() {
		return TYPE;
	}

	/**
	 * Create an empty Attribute. toString will return an empty String until
	 * more is known.
	 */
	public Attribute_lvalue() {
		super();
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with mobile set
	 * to true. Other Descriptor types can use this with mobile set to false.
	 * 
	 * @param mobile ==
	 *            Descriptor that holds this Attribute is of type OBJECT
	 */
	public Attribute_lvalue(boolean mobile) {
		super(mobile);
	}

	/**
	 * Copies the old attribute.
	 * @param old the attribute to copy
	 */
	public Attribute_lvalue(Attribute old) {
		super(old);
	}

	/** @inheritDoc */
	public Object clone() {
		return new Attribute_lvalue(this);
	}

	/** @inheritDoc */
	public void setArchetype(String value) throws BadDataException {
		LinkedList result = new LinkedList();
		for (StringTokenizer st = new StringTokenizer(value); st
				.hasMoreTokens();) {
			result.add(st.nextToken());
		}
		setArchetype(new EnumeratedValue(result));
	}

	/** @inheritDoc */
	public void setArchetype(Element E) throws BadDataException {
		NodeList elements = E.getElementsByTagNameNS(
				Attributes.DEFAULT_NAMESPACE_URI, "lvalue-possibles");

		if (elements.getLength() == 0) {
			throw new BadDataException(
					"lvalues must enumerate all possible values in the <lvalue-possibles> tag.");
		} else if (elements.getLength() > 1) {
			throw new BadDataException(
					"lvalues may only have one <lvalue-possibles> tag.");
		}

		NodeList lvalueEnums = ((Element) elements.item(0))
				.getElementsByTagNameNS(Attributes.DEFAULT_NAMESPACE_URI,
						"lvalue-enum");
		if (lvalueEnums.getLength() == 0) {
			throw new BadDataException(
					"lvalues must have at least one <lvalue-enum> tag.");
		}

		String[] possible = new String[lvalueEnums.getLength()];
		for (int i = 0; i < lvalueEnums.getLength(); i++) {
			Element currEnum = (Element) lvalueEnums.item(i);
			possible[i] = currEnum.getAttribute("value");
		}
		setArchetype(new EnumeratedValue(possible));
	}

	static class EnumeratedValue implements ExtendedAttributeValue {
		private int which = 0;
		private String[] values;
		EnumeratedValue(String[] possibles) {
			values = possibles;
		}
		EnumeratedValue(Collection possibles) {
			values = (String[]) possibles.toArray(new String[possibles.size()]);
		}
		
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof EnumeratedValue) {
				EnumeratedValue other = (EnumeratedValue) o;
				return other.values == values && other.which == which;
			} else {
				return false;
			}
		}
		
		/** @inheritDoc */
		public int hashCode() {
			return which ^ values.hashCode();
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
			return TYPE;
		}

		/** @inheritDoc */
		public AttributeValue setValue(Element el)
				throws IllegalArgumentException {
			if (!el.getTagName().endsWith(TYPE)) {
				throw new IllegalArgumentException(
						"Unexpected data type (not lvalue): " + el.getTagName());
			} else {
				return setValue(el.getAttribute("value"));
			}
		}

		/** @inheritDoc */
		public AttributeValue setValue(String S)
				throws IllegalArgumentException {
			EnumeratedValue copy = new EnumeratedValue(values);
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(S)) {
					copy.which = i;
					return copy;
				}
			}
			throw new IllegalArgumentException("Not a value for this lvalue: "
					+ S);
		}
		
		/** @inheritDoc */
		public String toString() {
			return values[which];
		}
		
		/** @inheritDoc */
		public Element toXML(Document doc) {
			Element el = doc.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			el.setAttribute("value", values[which]);
			return el;
		}

		/** @inheritDoc */
		public Element getExtraConfig(Document root) {
			Element poss = root
					.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
							Attributes.DEFAULT_NAMESPACE_QUALIFIER
									+ "lvalue-possibles");
			for (int i = 0; i < values.length; i++) {
				Element child = root.createElementNS(
						Attributes.DEFAULT_NAMESPACE_URI,
						Attributes.DEFAULT_NAMESPACE_QUALIFIER + "lvalue-enum");
				child.setAttribute("value", values[i]);
				poss.appendChild(child);
			}
			return poss;
		}

		/** @inheritDoc */
		public String getExtraConfigString() {
			StringBuffer sb = new StringBuffer().append(" ");
			for (int i = 0; i < values.length; i++) {
				sb.append(values[i]).append(" ");
			}
			return sb.toString();
		}
		
		/** @inheritDoc */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_lvalue.EnumeratedValue;
		}
	}
}