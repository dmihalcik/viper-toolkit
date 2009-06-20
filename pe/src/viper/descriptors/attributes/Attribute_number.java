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

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This attribute represents a number type, so that integers and doubles can be
 * compared.
 */
public class Attribute_number extends Attribute implements Cloneable {
	static {
		Distances.HelperAttrDistance d;

		d = new Distances.HelperAttrDistance(new DifferenceDistance(),
				"difference", Distance.BALANCED, "Normalized Difference", true);
		Distances.putDistanceFunctorFor("number", d);

		d = new Distances.HelperAttrDistance(new RelativeDistance(), "rho",
				Distance.CAND_V_TARGS, "Relative Difference", true);
		Distances.putDistanceFunctorFor("number", d);
		Distances.putDistanceFunctorFor("number", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("number", "difference");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("number", 0.0);
	}
	
	private static class DifferenceDistance
			implements
				Distances.QuickValueDistance {
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			int target = ((Number) D.getAlpha()).intValue();
			int candidate = ((Number) D.getBeta()).intValue();
			return new Double(Distances.infiniteDistanceRangeToClosed(Math
					.abs(target - candidate), 1.0));
		}
	}
	
	private static class RelativeDistance
			implements
				Distances.QuickValueDistance {
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			double target = ((Number) D.getAlpha()).doubleValue();
			double candidate = ((Number) D.getBeta()).doubleValue();
			return new Double(Math.max(Math.abs((target - candidate) / target),
					1.0));
		}
	}

	/**
	 * @inheritDoc
	 * @return "number" 
	 */
	public String getType() {
		return "number";
	}

	/**
	 * Empty constructor for static values.
	 */
	public Attribute_number() {
		super();
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with mobile set
	 * to true. Other Descriptor types can use this with mobile set to false.
	 * 
	 * @param mobile
	 *            Descriptor that holds this Attribute is of type OBJECT
	 */
	public Attribute_number(boolean mobile) {
		super(mobile);
	}

	/**
	 * Copies the old attribute.
	 * @param old the old attribute
	 */
	public Attribute_number(Attribute old) {
		super(old);
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
			NumberValue nv = (NumberValue) getArchetype().setValue(val);
			return Rules.getComparisonRelation(nv, unparsedRule);
		} else if (super.isValidRule(unparsedRule)) {
			return super.convertRule(unparsedRule, unparsedValues, err);
		} else {
			err
					.printError("svalues only support filtering by lexocagraphic ordering");
			return Rules.getTrue();
		}
	}

	protected abstract static class NumberValue extends Number
			implements
				AttributeValue,
				Comparable {
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

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to validate
		 * @return true iff the value is a valid attribute value for this
		 *         attribute's type
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_number.NumberValue;
		}

	}
}