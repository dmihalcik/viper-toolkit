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
import viper.filters.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This attribute represents a rotated recatangle or Oriented Box.
 */
public class Attribute_obox extends Attribute_polygon implements Cloneable {
	public static final String LOCAL_TYPE_NAME = "obox";
	public static final String TYPE = Attributes.DEFAULT_NAMESPACE_URI + LOCAL_TYPE_NAME;

	static {
		Distances.useSameDistances(Attribute_obox.TYPE, Attribute_polygon.TYPE);

		Distances.HelperAttrDistance d;
		d = new Distances.HelperAttrDistance(new PositionalCoefficient(),
				"positional", Distance.BALANCED, "Position Accuracy", false);
		Distances.putDistanceFunctorFor(Attribute_obox.TYPE, d);

		d = new Distances.HelperAttrDistance(new SizeCoefficient(), "size",
				Distance.BALANCED, "Size Coefficient", false);
		Distances.putDistanceFunctorFor(Attribute_obox.TYPE, d);

		d = new Distances.HelperAttrDistance(new OrientationCoefficient(),
				"orientation", Distance.BALANCED, "Orientation Coefficient",
				false);
		Distances.putDistanceFunctorFor(Attribute_obox.TYPE, d);

		d = new Distances.HelperAttrDistance(new PositionalAccuracyRecall(),
				"positionalaccuracyrecall", Distance.TARG_V_CANDS,
				"Positional Accuracy Recall", false);
		Distances.putDistanceFunctorFor(Attribute_obox.TYPE, d);

		d = new Distances.HelperAttrDistance(new PositionalAccuracyPrecision(),
				"positionalaccuracyprecision", Distance.CAND_V_TARGS,
				"Positional Accuracy Precision", false);
		Distances.putDistanceFunctorFor(Attribute_obox.TYPE, d);

		try {
			DefaultMeasures.setDefaultMetricFor(Attribute_obox.TYPE, "dice");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor(Attribute_obox.TYPE, 0.0);
	}

	private static final class PositionalCoefficient
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			BoxInformation truthBox = (BoxInformation) D.getAlpha();
			BoxInformation candBox = (BoxInformation) D.getBeta();

			if (truthBox == null || candBox == null) {
				if (truthBox == null && candBox == null) {
					return new Double(1.0);
				} else {
					return new Double(0.0);
				}
			}
			double maximumDistance = (truthBox.getWidth()
					+ truthBox.getHeight() + candBox.getWidth() + candBox
					.getHeight()) / 2.0;
			Pnt tCent = truthBox.getCentroid();
			Pnt cCent = candBox.getCentroid();

			double distanceC = Util.manhattanDistance(tCent, cCent)
					.doubleValue();
			if (distanceC < maximumDistance) {
				return new Double(1.0 - (distanceC / maximumDistance));
			} else {
				return new Double(0.0);
			}
		}
	}

	private static final class SizeCoefficient
			implements
				Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			BoxInformation truthBox = (BoxInformation) D.getAlpha();
			BoxInformation candBox = (BoxInformation) D.getBeta();

			if (truthBox == null || candBox == null) {
				if (truthBox == null && candBox == null) {
					return new Double(1.0);
				} else {
					return new Double(0.0);
				}
			}

			// Calculate Size-Based Metric
			double sizeT = truthBox.getWidth() * truthBox.getHeight();
			double sizeC = candBox.getWidth() * candBox.getHeight();
			return new Double((sizeT < sizeC)
					? (sizeT / sizeC)
					: (sizeC / sizeT));
		}
	}

	private static final class OrientationCoefficient
			implements
				Distances.QuickValueDistance {
		private double normDegrees(double deg) {
			double diff = (Math.abs(deg) / 360.0) - 0.5;
			if (diff > 0) { // then out of [-180, 180] range
				int cycles = (int) Math.ceil(diff);
				deg += (deg < 0) ? (360 * cycles) : (-360 * cycles);
			}
			return deg;
		}
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			BoxInformation truthBox = (BoxInformation) D.getAlpha();
			BoxInformation candBox = (BoxInformation) D.getBeta();

			if (truthBox == null || candBox == null) {
				if (truthBox == null && candBox == null) {
					return new Double(1.0);
				} else {
					return new Double(0.0);
				}
			}

			// Calculate Orientation-Based Metric
			int theta1 = truthBox.getRotation();
			int theta2 = candBox.getRotation();
			if (theta1 != theta2) {
				double diff = Math.abs(normDegrees(theta1)
						- normDegrees(theta2));
				return new Double(
						1.0 - ((diff > 180 ? 360.0 - diff : diff) / 180.0));
			} else {
				return new Integer(1);
			}
		}
	}

	private static final class PositionalAccuracyRecall
			implements
				Distances.QuickValueDistance {
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			BoxInformation truthBox = (BoxInformation) D.getAlpha();
			BoxInformation candBox = (BoxInformation) D.getBeta();

			if (truthBox == null || candBox == null) {
				if (truthBox == null && candBox == null) {
					return new Double(1.0);
				} else {
					return new Double(0.0);
				}
			}

			Pnt p1 = truthBox.getCentroid();
			PolyList candBoxes = ((AbstractPolygonValue) candBox).value;

			double best = 0;
			for (Iterator cands = candBoxes.getOriginals(); cands.hasNext()
					&& best < 1;) {
				ConvexPolygon curr = (ConvexPolygon) cands.next();
				Pnt q1 = curr.getCentroid();
				double currDist = 0;
				if (truthBox.contains(q1)) {
					if (q1.equals(truthBox.getCentroid())) {
						currDist = 1;
					} else {
						Pnt r1 = truthBox.getNearIntersection(q1);
						currDist = Util.euclideanDistance(q1, r1)
								/ Util.euclideanDistance(p1, r1);
					}
					best = Math.max(best, currDist);
				}
			}
			return new Double(best);
		}
	}

	private static final class PositionalAccuracyPrecision
			implements
				Distances.QuickValueDistance {
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			BoxInformation truthBox = (BoxInformation) D.getAlpha();
			BoxInformation candBox = (BoxInformation) D.getBeta();

			if (truthBox == null || candBox == null) {
				if (truthBox == null && candBox == null) {
					return new Double(1.0);
				} else {
					return new Double(0.0);
				}
			}

			Pnt p1 = candBox.getCentroid();
			PolyList truthBoxes = ((AbstractPolygonValue) truthBox).value;

			double best = 0;
			for (Iterator truths = truthBoxes.getOriginals(); truths.hasNext()
					&& best < 1;) {
				ConvexPolygon curr = (ConvexPolygon) truths.next();
				Pnt q1 = curr.getCentroid();
				if (candBox.contains(q1)) {
					double currDist = 0;
					if (q1.equals(candBox.getCentroid())) {
						currDist = 1;
					} else {
						Pnt r1 = candBox.getNearIntersection(q1);
						currDist = Util.euclideanDistance(q1, r1)
								/ Util.euclideanDistance(p1, r1);
					}
					best = Math.max(best, currDist);
				}
			}
			return new Double(best);
		}
	}

	/** 
	 * @inheritDoc
	 * @return Attribute_obox.TYPE 
	 */
	public String getType() {
		return Attribute_obox.TYPE;
	}

	private static final Obox arch = new Obox();
	
	/**
	 * Constructs an empty static obox-valued attribute.
	 */
	public Attribute_obox() {
		super();
		setArchetype(arch);
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with dynamic set
	 * to true.
	 * 
	 * @param dynamic
	 *            Descriptor that holds this Attribute is of type OBJECT
	 */
	public Attribute_obox(boolean dynamic) {
		super(dynamic);
		setArchetype(arch);
	}

	/**
	 * Copies the old attribute.
	 * @param old the attribute to copy
	 */
	public Attribute_obox(Attribute old) {
		super(old);
	}

	/**
	 * Returns a clone of this Attribute.
	 * 
	 * @return a reference to a new Attribute with all the values of the
	 *         original
	 */
	public Object clone() {
		return new Attribute_obox(this);
	}

	/** @inheritDoc */
	public boolean possibleValueOf(String S) {
		boolean prevNum = false;
		int numCount = 0;
		for (int i = 0, len = S.length(); i < len; i++) {
			char c = S.charAt(i);
			switch (Character.getType(c)) {

				case Character.DECIMAL_DIGIT_NUMBER :
				case Character.OTHER_NUMBER :
					if (!prevNum) {
						if (++numCount > 5) {
							return false;
						}
						prevNum = true;
					}
					break;

				case Character.SPACE_SEPARATOR :
				case Character.PARAGRAPH_SEPARATOR :
				case Character.LINE_SEPARATOR :
					prevNum = false;
					break;

				default :
					return false;
			}
		}
		return numCount == 5;
	}

	/** @inheritDoc */
	public boolean isValidRule(String ruleName) {
		return super.isValidRule(ruleName) || ruleName.equals("resize");
	}

	/** @inheritDoc */
	public Filterable.Rule convertRule(String unparsedRule,
			List unparsedValues, ErrorWriter err) throws BadDataException {
		if (!"resize".equals(unparsedRule)) {
			return super.convertRule(unparsedRule, unparsedValues, err);
		}
		if (unparsedValues == null || unparsedValues.size() != 4) {
			throw new BadDataException(
					"The resize fulter takes four arguments: x, y, width, and height deltas");
		}
		int x, y, width, height;
		int i = 0;
		try {
			x = Integer.parseInt((String) unparsedValues.get(i++));
			y = Integer.parseInt((String) unparsedValues.get(i++));
			width = Integer.parseInt((String) unparsedValues.get(i++));
			height = Integer.parseInt((String) unparsedValues.get(i++));
		} catch (NumberFormatException nfx) {
			throw new BadDataException("Not a valid integer: "
					+ unparsedValues.get(i));
		}
		return new Resizer(x, y, width, height);
	}

	/**
	 * Adds to the boxes size.
	 */
	protected static class Resizer implements Filterable.Rule {
		private int[] delta = new int[4];
		/**
		 * takes the deltas in each of the four values.
		 * 
		 * @param x
		 *            the change in x
		 * @param y
		 *            the change in y
		 * @param width
		 *            the change to the width
		 * @param height
		 *            the change to the height
		 */
		public Resizer(int x, int y, int width, int height) {
			delta[0] = x;
			delta[1] = y;
			delta[2] = width;
			delta[3] = height;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			Obox measurable = (Obox) o;
			OrientedBox box = (OrientedBox) measurable.value;
			box.set(box.getX() + delta[0], box.getY() + delta[1], box
					.getWidth()
					+ delta[2], box.getHeight() + delta[3], box.getRotation());
			return true;
		}

		/** @inheritDoc */
		public String toString() {
			return "resize (\"" + delta[0] + "\", \"" + delta[1] + "\", \""
					+ delta[2] + "\", \"" + delta[3] + "\")";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return
		 */
		public boolean isShortCircuit() {
			return false;
		}

	}

	protected static class Obox extends AbstractPolygonValue
			implements
				BoxInformation {
		
		public Object clone() {
			Obox copy = new Obox();
			OrientedBox oldBox = (OrientedBox) value;
			copy.value = new OrientedBox(oldBox.getX(), oldBox.getY(), oldBox.getWidth(), oldBox.getHeight(), oldBox.getRotation());
			return copy;
		}
		/** @inheritDoc */
		public String getType() {
			return Attribute_obox.TYPE;
		}
		
		/** @inheritDoc */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_obox.Obox;
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
			if (!Attribute_obox.LOCAL_TYPE_NAME.equals(el.getLocalName())) {
				throw new IllegalArgumentException("Unexpected data type: "
						+ el.getTagName());
			} else {
				Obox copy = new Obox();
				try {
					copy.value = new OrientedBox(parseAnInt(el, "x"),
							parseAnInt(el, "y"), parseAnInt(el, "width"),
							parseAnInt(el, "height"),
							parseAnInt(el, "rotation"));
				} catch (BadDataException bdx) {
					throw new IllegalArgumentException(bdx.getMessage());
				}
				return copy;
			}
		}

		/**
		 * Sets the state of the object to the data the String represents.
		 * Useful for old GTF format Should be able to run setValue(toString())
		 * and have it come out the same.
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
				Obox copy = new Obox();
				copy.value = new OrientedBox(Integer.parseInt(st.nextToken()),
						Integer.parseInt(st.nextToken()), Integer.parseInt(st
								.nextToken()),
						Integer.parseInt(st.nextToken()), Integer.parseInt(st
								.nextToken()));
				return copy;
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException(
						"Bad integer value in obox string: " + S);
			} catch (NoSuchElementException nsex) {
				throw new IllegalArgumentException(
						"Not enough numbers in obox string: " + S);
			}
		}

		/**
		 * Returns an xml element for this object, sans angle brackets, eg:
		 * <code>svalue value="something"</code>, so that the Attribute
		 * manager can add a span value, and namespace prefix, if necessary.
		 * 
		 * @param doc
		 *            the DOM Document to use while creating the element
		 * @return a DOM describing the attribute
		 */
		public Element toXML(Document doc) {
			Element el = doc.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			OrientedBox ob = (OrientedBox) value;
			el.setAttribute("x", String.valueOf(ob.getX()));
			el.setAttribute("y", String.valueOf(ob.getY()));
			el.setAttribute("width", String.valueOf(ob.getWidth()));
			el.setAttribute("height", String.valueOf(ob.getHeight()));
			el.setAttribute("rotation", String.valueOf(ob.getRotation()));
			return el;
		}

		/** @inheritDoc */
		public boolean contains(Pnt point) {
			return value.contains(point);
		}
		
		/** @inheritDoc */
		public Pnt getCentroid() {
			return ((OrientedBox) value).getCentroid();
		}
		
		/** @inheritDoc */
		public int getHeight() {
			return ((OrientedBox) value).getHeight();
		}
		
		/** @inheritDoc */
		public Pnt getNearIntersection(Pnt q1) {
			return ((OrientedBox) value).getNearIntersection(q1);
		}
		
		/** @inheritDoc */
		public int getRotation() {
			return ((OrientedBox) value).getRotation();
		}
		
		/** @inheritDoc */
		public int getWidth() {
			return ((OrientedBox) value).getWidth();
		}
		
		/** @inheritDoc */
		public int getX() {
			return ((OrientedBox) value).getX();
		}
		
		/** @inheritDoc */
		public int getY() {
			return ((OrientedBox) value).getY();
		}
		
		/** @inheritDoc */
		public AbstractPolygonValue setValue(PolyList newVal) {
			Obox copy = new Obox();
			copy.value = newVal;
			return copy;
		}
	}
}