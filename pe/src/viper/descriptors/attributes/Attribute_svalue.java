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
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This Attribute describes a String of characters. This class implements
 * various distance functions on strings, specifically edit (Levenshtein) and
 * Hamming distances, as well as equality. For composition, it just sticks the
 * two strings together, so it is not commutative.
 */
public class Attribute_svalue extends Attribute {
	static {
		Distances.HelperAttrDistance d;

		d = new Distances.HelperAttrDistance(new HammingDistance(), "h",
				Distance.BALANCED, "Normalized Hamming distance", true);
		Distances.putDistanceFunctorFor("svalue", d);

		d = new Distances.HelperAttrDistance(new EditDistance(), "l",
				Distance.BALANCED, "Normalized edit distance", true);
		Distances.putDistanceFunctorFor("svalue", d);
		Distances.putDistanceFunctorFor("svalue", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("svalue", "L");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("svalue", 0.0);
	}
	
	private static class HammingDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			String sT = (String) D.getAlpha();
			String sC = (String) D.getBeta();

			if (sT == null || sC == null) {
				if (sC == sT) {
					return new Integer(0);
				} else {
					return new Integer(1);
				}
			}

			int dist = 0;
			if (sT.length() != sC.length())
				return new Integer(1);
			for (int i = 0; i < sT.length(); i++)
				if (sT.charAt(i) != sC.charAt(i))
					dist++;
			return new Double(((double) dist) / sT.length());
		}
	}
	
	private static class EditDistance implements Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			int ans = 0;
			String sT = (String) D.getAlpha();
			String sC = (String) D.getBeta();

			if (sT == null || sC == null) {
				if (sT == null && sC == null) {
					return new Integer(0);
				} else if (sT == null) {
					ans = sC.length();
				} else {
					ans = sT.length();
				}
			} else {
				char[] t = sT.toCharArray();
				char[] c = sC.toCharArray();
				int[][] L = new int[c.length + 1][t.length + 1];
				for (int i = 0; i < c.length + 1; i++)
					L[i][0] = i;
				for (int j = 0; j < t.length + 1; j++)
					L[0][j] = j;
				for (int i = 1; i < c.length + 1; i++)
					for (int j = 1; j < t.length + 1; j++)
						L[i][j] = Math.min(L[i - 1][j], Math.min(L[i][j - 1],
								L[i - 1][j - 1]))
								+ ((c[i - 1] == t[j - 1]) ? 0 : 1);
				ans = L[c.length][t.length];
			}
			return new Double(Distances.infiniteDistanceRangeToClosed(ans, 1.0));
		}
	}

	/**
	 * @inheritDoc
	 * @return "svalue"
	 */
	public String getType() {
		return "svalue";
	}

	private static final StringValue archSV = new StringValue();
	
	/**
	 * Constructs a new static string-valued attribute.
	 */
	public Attribute_svalue() {
		super();
		setArchetype(archSV);
	}

	/**
	 * Constructs a new string-values attribute.
	 * @param dynamic if the attribute can take on different
	 * values over time
	 */
	public Attribute_svalue(boolean dynamic) {
		super(dynamic);
		setArchetype(archSV);
	}

	/**
	 * Copies the old attribute.
	 * @param old the old attribute
	 */
	public Attribute_svalue(Attribute old) {
		super(old);
	}

	/** @inheritDoc */
	public Object clone() {
		return new Attribute_svalue(this);
	}

	/** @inheritDoc */
	public boolean possibleValueOf(String S) {
		return true;
	}

	/** @inheritDoc */
	public boolean isValidRule(String ruleName) {
		return super.isValidRule(ruleName)
				|| StringHelp.isRelationalOperator(ruleName);
	}

	/** @inheritDoc */
	public Filterable.Rule convertRule(String unparsedRule,
			List unparsedValues, ErrorWriter err) throws BadDataException {
		if (unparsedValues.size() != 1) {
			throw new BadDataException(
					"This attribute's rules take one argument: " + unparsedRule);
		}
		String val = (String) unparsedValues.get(0);
		if (StringHelp.isRelationalOperator(unparsedRule)) {
			return Rules.getComparisonRelation(val, unparsedRule);
		} else if (super.isValidRule(unparsedRule)) {
			return super.convertRule(unparsedRule, unparsedValues, err);
		} else {
			err
					.printError("svalues only support filtering by lexocagraphic ordering");
			return Rules.getTrue();
		}
	}

	private static final class StringValue
			implements
				AttributeValue,
				Comparable {
		private String value = "NULL";
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof StringValue) {
				return value.equals(((StringValue) o).value);
			} else {
				return false;
			}
		}
		/** @inheritDoc */
		public int hashCode() {
			return value == null ? 0 : value.hashCode();
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
		private final String helpGetValue(Measurable m) {
			return (m == null) ? null : ((StringValue) m).value;
		}

		/** @inheritDoc */
		public String getType() {
			return "svalue";
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
		public AttributeValue setValue(Element el)
				throws IllegalArgumentException {
			StringValue copy = new StringValue();
			if (!el.getTagName().endsWith("svalue")) {
				throw new IllegalArgumentException(
						"Unexpected data type (not svalue): " + el.getTagName());
			} else {
				copy.value = el.getAttribute("value");
			}
			return copy;
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
		public AttributeValue setValue(String S)
				throws IllegalArgumentException {
			StringValue copy = new StringValue();
			copy.value = S;
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
			el.setAttribute("value", value);
			return el;
		}
		
		/** @inheritDoc */
		public String toString() {
			return value;
		}

		/** @inheritDoc */
		public int compareTo(Object obj) {
			return value.compareTo(((StringValue) obj).value);
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check against the attribute's config
		 * @return true iff the value is valid for this attribute
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_svalue.StringValue;
		}
	}
}