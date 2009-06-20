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
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This attribute represents a polygon
 */
public class Attribute_polygon extends Attribute {
	private static final Integer ONE = new Integer(1);
	private static final Integer ZERO = new Integer(0);
	private static final Double UNDEFINED = new Double(Double.NaN);

	static {
		Distances.HelperAttrDistance d;

		d = new Distances.HelperAttrDistance(new DiceDistance(), "dice",
				Distance.BALANCED, "Dice coefficient", true);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new MaximumDeviationDistance(),
				"maxdev", Distance.BALANCED, "Maximum deviation", true);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new OverlapDistance(), "overlap",
				Distance.TARG_V_CANDS, "Target overlap", true);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new AreaRecallDistance(),
				"arearecall", Distance.TARG_V_CANDS, "Object Area Recall",
				false);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new AreaPrecisionDistance(),
				"areaprecision", Distance.CAND_V_TARGS, "Box Area Precision",
				false);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new IntersectsDistance(),
				"intersects", Distance.BALANCED, "Some shared area", true);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new MatchedPixelCount(),
				"matchedpixels", Distance.OVERALL_SUM, "Pixels matched", false);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new MissedPixelCount(),
				"missedpixels", Distance.OVERALL_SUM, "Pixels missed", true);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new FalsePixelCount(),
				"falsepixels", Distance.OVERALL_SUM, "Pixels falsely detected",
				true);
		Distances.putDistanceFunctorFor("polygon", d);

		d = new Distances.HelperAttrDistance(new FragmentationDistance(),
				"fragmentation", Distance.TARG_V_CANDS, "Fragmentation metric",
				false);
		Distances.putDistanceFunctorFor("polygon", d);

		Distances.putDistanceFunctorFor("polygon", Distances
				.getEqualityDistance());

		try {
			DefaultMeasures.setDefaultMetricFor("polygon", "dice");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("polygon", 0.0);
	}

	private static PolygonDiff convertMD(Measurable.Difference D) {
		if (D instanceof PolygonDiff) {
			return (PolygonDiff) D;
		} else {
			return new PolygonDiff(D);
		}
	}

	private static class DiceDistance implements Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				return (pd.getAlphaArea().equals(pd.getBetaArea()))
						? Attribute_polygon.ZERO
						: ONE;
			}
			Rational dice = new Rational(2);
			Rational.multiply(dice, pd.getShared(), dice);
			Rational temp = new Rational();
			Rational.plus(pd.getAlphaArea(), pd.getBetaArea(), temp);
			Rational.divide(dice, temp, dice);
			temp.setTo(1);
			Rational.minus(temp, dice, dice);
			return new Double(dice.doubleValue());
		}
	}
	private static class IntersectsDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				return ONE;
			}

			return (pd.getShared().equals(0)) ? ONE : ZERO;
		}
	}
	private static class OverlapDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				return (pd.getAlphaArea().equals(pd.getBetaArea())) ? ZERO : ONE;
			}
			Rational r = new Rational();
			Rational.divide(pd.getShared(), pd.getAlphaArea(), r);
			Rational.minus(new Rational(1), r, r);
			return new Double(r.doubleValue());
		}
	}
	private static class AreaRecallDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				if (pd.getAlphaArea().equals(0) && pd.getBetaArea().equals(0)) {
					return UNDEFINED;
				} else if (pd.getAlphaArea().equals(0)) {
					return ONE;
				} else {
					return ZERO;
				}
			}
			Rational r = new Rational();
			Rational.divide(pd.getShared(), pd.getAlphaArea(), r);
			return new Double(r.doubleValue());
		}
	}
	private static class AreaPrecisionDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				if (pd.getAlphaArea().equals(0) && pd.getBetaArea().equals(0)) {
					return UNDEFINED;
				} else if (pd.getBetaArea().equals(0)) {
					return ONE;
				} else {
					return ZERO;
				}
			}
			Rational r = new Rational();
			Rational.divide(pd.getShared(), pd.getBetaArea(), r);
			return new Double(r.doubleValue());
		}
	}

	private static class MaximumDeviationDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				return (pd.getAlphaArea().equals(pd.getBetaArea())) ? ZERO : ONE;
			}
			
			return new Double(Math.max(
					((pd.getAlphaArea().doubleValue() - pd.getShared().doubleValue()) / pd.getAlphaArea().doubleValue()),
					((pd.getBetaArea().doubleValue() - pd.getShared().doubleValue()) / pd.getBetaArea().doubleValue())));
		}
	}

	private static class MatchedPixelCount
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);
			return new Double(pd.getShared().doubleValue());
		}
	}
	private static class MissedPixelCount
			implements
				Distances.QuickValueDistance {

		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);
			return new Double(pd.getMissed().doubleValue());
		}
	}
	private static class FalsePixelCount
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);
			return new Double(pd.getFalse().doubleValue());
		}
	}

	private static class FragmentationDistance
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			PolygonDiff pd = convertMD(D);

			if (pd.getAlphaArea().equals(0) || pd.getBetaArea().equals(0)) {
				return new Double(Double.NaN);
			}

			int count = pd.getFragCount();
			if (count == 0) {
				return new Double(Double.NaN);
			}
			return new Double(1.0 / (1.0 + (Math.log(count) / Math.log(10))));
		}
	}

	/*
	 * The metric for calculating the distance between two bounding boxes. <BR>
	 * <em> So far: </em><UL><LI><code> dice </code> : 1 - 2*overlap / sum of
	 * areas </LI><LI><code> overlap </code> : 1 - overlap / target area </LI>
	 * <LI><code> E </code> : 0 if equivalent, 1 otherwise </LI><LI><code>
	 * maxdev </code> : max ((A - overlap) / A, (B - overlap) / B) </LI></UL>
	 */

	/**
	 * @inheritDoc 
	 * @return "polygon"
	 */
	public String getType() {
		return "polygon";
	}

	/**
	 * Constructs a new static polygon-valued attribute.
	 */
	public Attribute_polygon() {
		super();
		setArchetype(archGon);
	}

	/**
	 * Constructs a new polygon valued attribute.
	 * @param dynamic if the attribute can take on different 
	 * values at different frames
	 */
	public Attribute_polygon(boolean dynamic) {
		super(dynamic);
		setArchetype(archGon);
	}

	/**
	 * Copies the old attribute.
	 * @param old the old attribute
	 */
	public Attribute_polygon(Attribute old) {
		super(old);
		setArchetype(old.getArchetype());
	}
	
	/** @inheritDoc */
	public Object clone() {
		return new Attribute_polygon(this);
	}
	
	private static final PolyLineValue archLine = new Attribute_polygon.PolyLineValue();
	private static final PolygonValue archGon = new Attribute_polygon.PolygonValue();
	
	/** @inheritDoc */
	public void setArchetype(String value) throws BadDataException {
		if (value.equals("open")) {
			setArchetype(archLine);
		} else if (value.equals("closed")) {
			setArchetype(archGon);
		} else {
			throw new BadDataException("Error: 'open' expected, found: "
					+ value);
		}
	}

	/** @inheritDoc */
	public void setArchetype(Element E) throws BadDataException {
		NodeList elements = E.getElementsByTagNameNS(
				Attributes.DEFAULT_NAMESPACE_URI, "polygon-type");
		if (elements.getLength() == 0) {
			setArchetype(new Attribute_polygon.PolygonValue());
			return;
		} else if (elements.getLength() > 1) {
			throw new BadDataException("polygon " + E.getAttribute("name")
					+ " may only have one <polygon-type> tag.");
		}

		Element type = (Element) elements.item(0);
		if (type.getAttribute("open").equals("true")) {
			setArchetype(new Attribute_polygon.PolyLineValue());
			return;
		} else if (type.getAttribute("open").equals("false")) {
			setArchetype(new Attribute_polygon.PolygonValue());
			return;
		} else {
			throw new BadDataException(
					"polygon-type@open must be either 'true' or 'false', not: '"
							+ type.getAttribute("open") + "'");
		}
	}

	/**
	 * Gets the area of the polygon. For dynamic polygons,
	 * gets the sum of the area on all frames.
	 * @return the area
	 */
	public Rational getArea() {
		if (!isDynamic()) {
			return new Rational(((PolyList) value).area());
		} else {
			Object[] values = (Object[]) value;
			Rational totalArea = new Rational(0);
			for (int i = 0; i < values.length; i++) {
				Rational.plus(totalArea, ((PolyList) values[i]).area(), totalArea);
			}
			return totalArea;
		}
	}

	/**
	 * Gets the area of the polygon shared with the other polygon.
	 * @param other the other polygon
	 * @return the approximate area of the intersection
	 */
	public Rational getAreaOfIntersectionWith(Attribute_polygon other) {
		if (other == null || (value == null) || (other.value == null))
			return new Rational(0);

		PolyList myBoxes;
		PolyList theirBoxes;
		if (isDynamic()) {
			myBoxes = (PolyList) ((Object[]) value)[0];
		} else {
			myBoxes = (PolyList) value;
		}
		if (other.isDynamic()) {
			theirBoxes = (PolyList) ((Object[]) other.value)[0];
		} else {
			theirBoxes = (PolyList) other.value;
		}

		PolyList inters = myBoxes.getIntersection(theirBoxes);
		if (inters == null)
			return new Rational(0);
		else
			return inters.area();
	}

	/** @inheritDoc */
	public boolean isValidRule(String ruleName) {
		return super.isValidRule(ruleName) || ruleName.equals("smallerthan")
				|| ruleName.equals("biggerthan") || ruleName.equals("size");
	}
	
	/**
	 * This converts a string into a rule, writing out errors in the format to
	 * <code>err</code>.
	 * 
	 * @param unparsedRule
	 *            A string containing a single rule.
	 * @param unparsedValues
	 *            A string containing values.
	 * @param err
	 *            A place to log the errors.
	 * @return A List containing a rule (String) and value (Object) pair.
	 * @throws BadDataException
	 */
	public Filterable.Rule convertRule(String unparsedRule,
			List unparsedValues, ErrorWriter err) throws BadDataException {
		if (unparsedValues == null) {
			throw new BadDataException(
					"This attribute's rules take at least one argument: "
							+ unparsedRule);
		}
		String valStr = (String) unparsedValues.get(0);
		if ("smallerthan".equals(unparsedRule)) {
			if (unparsedValues.size() != 1) {
				throw new BadDataException("Polygon filter rule "
						+ unparsedRule + " must take one value, not "
						+ unparsedValues.size() + ".");
			}
			if (getArchetype() instanceof PolyLineValue) {
				throw new BadDataException("Polygon filter rule "
						+ unparsedRule + " does not work on polylines.");
			} else if (valStr == null) {
				return new Sizer("<", new Rational(0));
			} else {
				AbstractPolygonValue av = (AbstractPolygonValue) getArchetype()
						.setValue(valStr);
				return new Sizer("<", av.value.area());
			}
		} else if ("biggerthan".equals(unparsedRule)) {
			if (unparsedValues.size() != 1) {
				throw new BadDataException("Polygon filter rule "
						+ unparsedRule + " must take one value, not "
						+ unparsedValues.size() + ".");
			}

			if (getArchetype() instanceof PolyLineValue) {
				throw new BadDataException("Polygon filter rule "
						+ unparsedRule + " does not work on polylines.");
			} else if (valStr == null) {
				return new Sizer(">", new Rational(0));
			} else {
				AbstractPolygonValue av = (AbstractPolygonValue) getArchetype()
						.setValue(valStr);
				return new Sizer(">", av.value.area());
			}
		} else if ("size".equals(unparsedRule)) {
			if (unparsedValues.size() != 2) {
				throw new BadDataException(
						"Polygon filter rule "
								+ unparsedRule
								+ " must take two values, a relational operator and a size, like \"4\", or a polygon, \n\t"
								+ "eg 'size (\"<=\", \"10 10 100 100\")' for bboxes, or\n\t"
								+ "'size (\"<=\", \"[(10 10) (10 13) (14 10)]\")' for polygons.");
			}
			String relOp = valStr;
			valStr = (String) unparsedValues.get(1);
			if (getArchetype() instanceof PolyLineValue) {
				throw new BadDataException("Polygon filter rule "
						+ unparsedRule + " does not work on polylines.");
			} else {
				Rational sz;
				if (valStr == null) {
					sz = new Rational(0);
				} else if (possibleValueOf(valStr)) {
					AbstractPolygonValue av = (AbstractPolygonValue) getArchetype()
							.setValue(valStr);
					sz = av.value.area();
				} else {
					try {
						sz = Rational.parseRational(valStr);
					} catch (NumberFormatException nfx) {
						throw new BadDataException(
								"Expected a number - representing an area - or a shape the same type as the attribute");
					}
				}
				return new Sizer(relOp, sz);
			}
		} else if ("==".equals(unparsedRule)) {
			AttributeValue av = valStr == null ? null : getArchetype()
					.setValue(valStr);
			return Rules.getEquality(av);
		} else if ("!=".equals(unparsedRule)) {
			AttributeValue av = valStr == null ? null : getArchetype()
					.setValue(valStr);
			return Rules.getInequality(av);
		} else {
			err
					.printError("svalues only support filtering by lexocagraphic ordering");
			return Rules.getTrue();
		}
	}

	/**
	 * Checks that the current value's size.
	 */
	protected static class Sizer implements Filterable.Rule {
		private Rational area;
		private int op;
		Sizer(String relationalOp, Rational size) throws BadDataException {
			area = new Rational(size);
			op = StringHelp.getRelationalOperatorEnum(relationalOp);
			if (op < 0) {
				throw new BadDataException("Not a relational operator: "
						+ relationalOp);
			}
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			Rational other = ((AbstractPolygonValue) o).value.area();
			switch (op) {
				case StringHelp.REL_EQ :
					return area.equals(other);
				case StringHelp.REL_NEQ :
					return !area.equals(other);

				case StringHelp.REL_LT :
					return other.lessThan(area);
				case StringHelp.REL_GT :
					return other.greaterThan(area);

				case StringHelp.REL_LTEQ :
					return other.lessThanEqualTo(area);
				case StringHelp.REL_GTEQ :
					return other.greaterThanEqualTo(area);

				default :
					throw new IllegalStateException(
							"Invalid relational operator type: " + op);
			}
		}

		/** @inheritDoc */
		public String toString() {
			return "size (\"" + StringHelp.getRelationalOperatorString(op)
					+ "\", \"" + area + "\")";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return true
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	private static class PolygonDiff implements Measurable.Difference {
		private AbstractPolygonValue alpha, beta, blackout, ignore;
		private PolyList frame;
		private PolyList alphaF, betaF, blackoutF, ignoreF;
		private CanonicalFileDescriptor cfd;
		private boolean ignoredValue = false;
		PolygonDiff(Measurable.Difference old) {
			set((AbstractPolygonValue) old.getAlpha(),
					(AbstractPolygonValue) old.getBeta(),
					(AbstractPolygonValue) old.getBlackout(),
					(AbstractPolygonValue) old.getIgnore(), old
							.getFileInformation());
		}
		
		PolygonDiff(AbstractPolygonValue alpha,
				AbstractPolygonValue beta, AbstractPolygonValue blackout,
				AbstractPolygonValue ignore, CanonicalFileDescriptor cfd)
				throws IgnoredValueException {
			this.set(alpha, beta, blackout, ignore, cfd);
			if (this.ignoredValue) {
				throw new IgnoredValueException();
			}
		}
		
		/** @inheritDoc */
		public Object getAlpha() {
			return alpha;
		}

		/** @inheritDoc */
		public Object getBeta() {
			return beta;
		}
		
		/** @inheritDoc */
		public Object getBlackout() {
			return blackout;
		}
		
		/** @inheritDoc */
		public Object getIgnore() {
			return ignore;
		}
		
		/** @inheritDoc */
		public CanonicalFileDescriptor getFileInformation() {
			return cfd;
		}

		/**
		 * Resets this difference object.
		 * @param alpha the new target value
		 * @param beta the new candidate value
		 * @param blackout the new blackout value
		 * @param ignore the new value to ignore
		 * @param cfd the new file descriptor
		 */
		public void set(AbstractPolygonValue alpha, AbstractPolygonValue beta,
				AbstractPolygonValue blackout, AbstractPolygonValue ignore,
				CanonicalFileDescriptor cfd) {
			this.cfd = cfd;

			shared = null;
			missed = null;
			falsed = null;
			betaArea = null;
			alphaArea = null;
			ignoreblack = new Rational(0);
			match = null;

			this.alpha = alpha;
			this.beta = beta;
			this.blackout = blackout;
			this.ignore = ignore;
			this.ignoreBlackoutShared = null;

			alphaF = alpha == null ? null : this.alpha.value;
			betaF = beta == null ? null : this.beta.value;
			ignoreF = ignore == null ? null : this.ignore.value;
			blackoutF = blackout == null ? null : this.blackout.value;
			frame = null;

			if (cfd != null) {
				int[] dims = cfd.getDimensions();
				if (dims != null && dims.length == 2 && dims[0] > 0
						&& dims[1] > 0) {
					frame = new BoundingBox(0, 0, dims[0], dims[1]);

					alphaF = (alpha == null) ? null : frame
							.getIntersection(alpha.value);
					betaF = (beta == null) ? null : frame
							.getIntersection(beta.value);
					ignoreF = (this.ignore == null) ? null : frame
							.getIntersection(ignore.value);
					blackoutF = (this.blackout == null) ? null : frame
							.getIntersection(blackout.value);
				}
			}

			if (ignoreF != null && betaF != null) {
				if (getBetaArea().equals(betaF.getIntersection(ignoreF).area())) {
					ignoredValue = true;
				}
			}
			if (blackoutF != null) {
				this.match = getMatch();
				if (this.match != null) {
					this.blackout = this.blackout.setValue(this.blackout.value
							.getIntersection(match));
					if (this.blackout.value.area().equals(0)) {
						this.blackout = null;
						this.blackoutF = null;
					} else if (this.ignore != null) {
						ignoreBlackoutShared = this.blackout.value
								.getIntersection(ignoreF);
						ignoreblack = ignoreBlackoutShared.area();
						if (frame != null) {
							blackoutF = blackout.value.getIntersection(frame);
						}
					} else if (frame != null) {
						blackoutF = blackout.value.getIntersection(frame);
					}
				} else {
					blackout = null;
					blackoutF = null;
				}
			}
		}

		/**
		 * Gets the false area.
		 * @return the area of the candidate polygon not overlapping
		 * the target or don't care region.
		 */
		public Rational getFalse() {
			if (falsed == null) {
				falsed = new Rational(0);
				if (betaF != null) {
					Rational.minus(betaF.area(), getShared(), falsed);
					if (ignoreF != null) {
						Rational.minus(falsed, betaF.getIntersection(ignoreF).area(), falsed);
						if (match != null) {
							Rational.plus(falsed, match.getIntersection(ignoreF).area(), falsed);
						}
					}
				}
			}
			return falsed;
		}

		/**
		 * Gets the area of the missed region.
		 * @return the area of the target not covered by the 
		 * candidate or the ignored region
		 */
		public Rational getMissed() {
			if (missed == null) {
				missed = new Rational(0);
				if (alphaF != null) {
					Rational.minus(alphaF.area(), getShared(), missed);
					if (ignoreF != null) {
						Rational.minus(missed, alpha.value.getIntersection(ignoreF).area(), missed);
						if (match != null) {
							Rational.plus(missed, match.getIntersection(ignoreF).area(), missed);
						}
					}
				}
			}
			return missed;
		}

		/**
		 * Gets the area of the intersection, less the 
		 * ignored area and the blackout area.
		 * @return the shared area
		 */
		public Rational getShared() {
			if (shared == null) {
				shared = new Rational(0);
				if (getMatch() != null) {
					shared = new Rational(getMatch().area());
				}
				if (blackoutF != null) {
					Rational.minus(shared, blackoutF.area(), shared);
					Rational.plus(shared, ignoreblack, shared);
				}
			}
			return shared;
		}

		/**
		 * Gets the intersection region.
		 * @return the intersection
		 */
		public PolyList getMatch() {
			if (alphaF == null || betaF == null) {
				return null;
			} else if (match == null) {
				match = alphaF.getIntersection(betaF);
			}
			return match;
		}

		/**
		 * Gets the target's area.
		 * @return the area of the target polygon
		 */
		public Rational getAlphaArea() {
			if (alphaArea == null) {
				alphaArea = (alphaF == null) ? new Rational(0) : alphaF.area();
			}
			return alphaArea;
		}
		
		/**
		 * Gets the candidate's area.
		 * @return the area of the candidate
		 */
		public Rational getBetaArea() {
			if (betaArea == null) {
				betaArea = (betaF == null) ? new Rational(0) : betaF.area();
			}
			return betaArea;
		}

		/**
		 * Gets the area of the blackout shape(s).
		 * @return the size of the blackout area
		 */
		public Rational getBlackoutArea() {
			Rational r = new Rational(0);
			if (blackoutF != null) {
				Rational.minus(blackoutF.area(), ignoreblack, r);
			}
			return r;
		}

		/**
		 * Gets the fragmentation count of the candidate.
		 * @return the candidate's fragmentation count
		 */
		public int getFragCount() {
			if (beta == null || alpha == null) {
				return 0;
			}
			return beta.value.getFragmentationCount(alpha.value);
		}

		private PolyList match;
		private PolyList ignoreBlackoutShared;

		private Rational shared;
		private Rational missed;
		private Rational falsed;

		private Rational ignoreblack;

		private Rational alphaArea;
		private Rational betaArea;
	}

	abstract static class AbstractPolygonValue
			implements
				AttributeValue,
				Composable {
		protected PolyList value = null;

		abstract AbstractPolygonValue setValue(PolyList newVal);

		/** @inheritDoc */
		public String toString() {
			return value == null ? null : value.toString();
		}
		
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof AbstractPolygonValue) {
				return ((AbstractPolygonValue) o).value.equals(value);
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
				CanonicalFileDescriptor cfd) throws IgnoredValueException {
			return new PolygonDiff(this, (AbstractPolygonValue) beta,
					(AbstractPolygonValue) blackout,
					(AbstractPolygonValue) ignore, cfd);
		}

		/** @inheritDoc */
		public Measurable.Difference getDifference(Measurable beta,
				Measurable blackout, Measurable ignore,
				CanonicalFileDescriptor cfd, Measurable.Difference old)
				throws IgnoredValueException {
			return new PolygonDiff(this, (AbstractPolygonValue) beta,
					(AbstractPolygonValue) blackout,
					(AbstractPolygonValue) ignore, cfd);
		}

		/** @inheritDoc */
		abstract public String getType();

		/**
		 * Tell if a name is a valid rule or not
		 * 
		 * @param ruleName
		 *            the name of the rule to check
		 * @return <code>true</code> if the rule name is known
		 */
		public boolean isValidRule(String ruleName) {
			return "==".equals(ruleName) || "!=".equals(ruleName)
					|| ruleName.equals("smallerthan")
					|| ruleName.equals("biggerthan") || ruleName.equals("size");
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
		public abstract AttributeValue setValue(Element el)
				throws IllegalArgumentException;

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
		public abstract AttributeValue setValue(String S)
				throws IllegalArgumentException;

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
		abstract public Element toXML(Document doc);

		/** @inheritDoc */
		public Composable compose(Composable partner) {
			AbstractPolygonValue other = (AbstractPolygonValue) partner;
			return setValue(PolyList.union(value, other.value));
		}
		
		/** @inheritDoc */
		public int getCompositionType() {
			return Composable.UNORDERED;
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check
		 * @return true iff the value is valid for this type
		 */
		abstract public boolean validate(AttributeValue v);
	}

	static class PolygonValue extends AbstractPolygonValue
			implements
				ExtendedAttributeValue {
		/** @inheritDoc */
		public AttributeValue setValue(String S)
				throws IllegalArgumentException {
			PolygonValue copy = new PolygonValue();
			try {
				copy.value = new Polygon(S);
			} catch (BadDataException bdx) {
				throw new IllegalArgumentException(bdx.getMessage());
			}
			return copy;
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
			PolygonValue copy = new PolygonValue();
			try {
				Polygon v = new Polygon();
				NodeList points = el.getElementsByTagNameNS(
						Attributes.DEFAULT_NAMESPACE_URI, "point");
				for (int i = 0; i < points.getLength(); i++) {
					Element pntEl = (Element) points.item(i);
					int x = parseAnInt(pntEl, "x");
					int y = parseAnInt(pntEl, "y");
					v.addVertex(new Pnt(x, y));
				}
				copy.value = v;
			} catch (BadDataException bdx) {
				throw new IllegalArgumentException(bdx.getMessage());
			}
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
			Polygon v = (Polygon) value;
			for (Iterator iter = v.getPoints(); iter.hasNext();) {
				Pnt curr = (Pnt) iter.next();
				Element child = doc.createElementNS(
						Attributes.DEFAULT_NAMESPACE_URI,
						Attributes.DEFAULT_NAMESPACE_QUALIFIER + "point");
				child.setAttribute("x", String.valueOf(curr.getX()));
				child.setAttribute("y", String.valueOf(curr.getY()));
				el.appendChild(child);
			}
			return el;
		}

		/** @inheritDoc */
		public String getType() {
			return "polygon";
		}

		/** @inheritDoc */
		public Element getExtraConfig(Document root) {
			Element el = root.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + "polygon-type");
			el.setAttribute("open", "false");
			return el;
		}
		
		/** @inheritDoc */
		public String getExtraConfigString() {
			return null;
		}

		/** @inheritDoc */
		public AbstractPolygonValue setValue(PolyList newVal) {
			PolygonValue copy = new PolygonValue();
			copy.value = newVal;
			return copy;
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check against this attribute's type
		 *            information
		 * @return true iff the value is valid
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_polygon.PolygonValue;
		}
	}

	static class PolyLineValue extends AbstractPolygonValue
			implements
				ExtendedAttributeValue {
		/** @inheritDoc */
		public AttributeValue setValue(String S)
				throws IllegalArgumentException {
			PolyLineValue copy = new PolyLineValue();
			try {
				copy.value = new PolyLine(S);
			} catch (BadDataException bdx) {
				throw new IllegalArgumentException(bdx.getMessage());
			}
			return copy;
		}

		/** @inheritDoc */
		public Element toXML(Document doc) {
			Element el = doc.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			PolyLine v = (PolyLine) value;
			for (Iterator iter = v.getPoints(); iter.hasNext();) {
				Pnt curr = (Pnt) iter.next();
				Element child = doc.createElementNS(
						Attributes.DEFAULT_NAMESPACE_URI,
						Attributes.DEFAULT_NAMESPACE_QUALIFIER + "point");
				child.setAttribute("x", String.valueOf(curr.getX()));
				child.setAttribute("y", String.valueOf(curr.getY()));
				el.appendChild(child);
			}
			return el;
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
			PolyLineValue copy = new PolyLineValue();
			try {
				PolyLine v = new PolyLine();
				NodeList points = el.getElementsByTagNameNS(
						Attributes.DEFAULT_NAMESPACE_URI, "point");
				for (int i = 0; i < points.getLength(); i++) {
					Element pntEl = (Element) points.item(i);
					int x = parseAnInt(pntEl, "x");
					int y = parseAnInt(pntEl, "y");
					v.addVertex(new Pnt(x, y));
				}
				copy.value = v;
			} catch (BadDataException bdx) {
				throw new IllegalArgumentException(bdx.getMessage());
			}
			return copy;
		}

		/** @inheritDoc */
		public String getType() {
			return "polyline";
		}

		/** @inheritDoc */
		public Element getExtraConfig(Document root) {
			Element el = root.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + "polygon-type");
			el.setAttribute("open", "true");
			return el;
		}
		
		/** @inheritDoc */
		public String getExtraConfigString() {
			return "open";
		}

		/** @inheritDoc */
		public AbstractPolygonValue setValue(PolyList newVal) {
			PolyLineValue copy = new PolyLineValue();
			copy.value = newVal;
			return copy;
		}

		/**
		 * Checks to make sure that the value can be set.
		 * 
		 * @param v
		 *            the value to check against the attribute's config
		 * @return true iff the value is valid for this attribute
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_polygon.PolyLineValue;
		}
	}
}