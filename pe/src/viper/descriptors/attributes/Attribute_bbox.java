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
 * This attribute represents a rectangle or Bounding Box. It has dice, overlap,
 * and equality metrics.
 */
public class Attribute_bbox extends Attribute_polygon {
	static {
		Distances.useSameDistances("bbox", "obox");
		try {
			DefaultMeasures.setDefaultMetricFor("bbox", "dice");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor("bbox", 0.0);
	}

	static String defaultCompositionType = "union";
	
	/**
	 * @inheritDoc
	 * @return if the type is union, perimeter, or one of 
	 * the parent types
	 */
	public boolean isCompositionType(String s) {
		return ((s.equalsIgnoreCase("union"))
				|| (s.equalsIgnoreCase("perimeter")) || (super
				.isCompositionType(s)));
	}

	/**
	 * @inheritDoc
	 * @return "bbox"
	 */
	public String getType() {
		return "bbox";
	}

	private static final Bbox arch = new Bbox();

	/**
	 * Create an empty Attribute. toString will return an empty String until
	 * more is known.
	 */
	public Attribute_bbox() {
		super();
		setArchetype(arch);
		compositionType = Attribute_bbox.defaultCompositionType;
	}

	/**
	 * Attributes for OBJECT type should use this constructor, with mobile set
	 * to true. Other Descriptor types can use this with mobile set to false.
	 * 
	 * @param mobile
	 *            Descriptor that holds this Attribute is of type OBJECT
	 */
	public Attribute_bbox(boolean mobile) {
		super(mobile);
		setArchetype(arch);
		compositionType = Attribute_bbox.defaultCompositionType;
	}

	/**
	 * Constructs a new copy of the old bbox value.
	 * @param old the old value.
	 */
	public Attribute_bbox(Attribute old) {
		super(old);

		if (old.getClass().equals(Attribute_bbox.class)) {
			compositionType = old.compositionType;
		} else {
			compositionType = Attribute_bbox.defaultCompositionType;
		}
	}

	/**
	 * @inheritDoc
	 */
	public Object clone() {
		return new Attribute_bbox(this);
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
		boolean prevNum = false;
		int numCount = 0;
		for (int i = 0, len = S.length(); i < len; i++) {
			char c = S.charAt(i);
			switch (Character.getType(c)) {

				case Character.DECIMAL_DIGIT_NUMBER :
				case Character.OTHER_NUMBER :
					if (!prevNum) {
						if (++numCount > 4) {
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
		return numCount == 4;
	}

	/**
	 * @inheritDoc
	 * @return if this rule is valid for the parent type,
	 * or if is resize, propresize, or cropTo
	 */
	public boolean isValidRule(String ruleName) {
		return super.isValidRule(ruleName) || ruleName.equals("resize")
				|| ruleName.equals("propresize") || ruleName.equals("cropTo");
	}

	/**
	 * @inheritDoc
	 */
	public Filterable.Rule convertRule(String unparsedRule,
			List unparsedValues, ErrorWriter err) throws BadDataException {
		if ("resize".equals(unparsedRule)) {
			if (unparsedValues == null || unparsedValues.size() != 4) {
				throw new BadDataException(
						"The resize filter takes four arguments: x, y, width, and height deltas");
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
		} else if ("cropTo".equals(unparsedRule)) {
			if (unparsedValues == null || unparsedValues.size() != 4) {
				throw new BadDataException(
						"The cropTo filter takes four arguments: x, y, width, and height for the window");
			}
			int x, y, width, height;
			int i = 0;
			try {
				x = Integer.parseInt((String) unparsedValues.get(i++));
				y = Integer.parseInt((String) unparsedValues.get(i++));
				width = Integer.parseInt((String) unparsedValues.get(i++));
				height = Integer.parseInt((String) unparsedValues.get(i++));
				return new Crops(x, y, width, height);
			} catch (NumberFormatException nfx) {
				throw new BadDataException("Not a valid integer: "
						+ unparsedValues.get(i));
			} catch (IllegalArgumentException iax) {
				throw new BadDataException(iax.getMessage());
			}
		} else if ("propresize".equals(unparsedRule)) {
			if (unparsedValues == null || unparsedValues.size() != 2) {
				throw new BadDataException(
						"The propresize fulter takes two arguments: width, and height fractions");
			}
			double width, height;
			int i = 0;
			try {
				width = Double.parseDouble((String) unparsedValues.get(i++));
				height = Double.parseDouble((String) unparsedValues.get(i++));
			} catch (NumberFormatException nfx) {
				throw new BadDataException("Not a valid number: "
						+ unparsedValues.get(i - 1));
			}
			return new ProportionalResizer(width, height);
		} else {
			return super.convertRule(unparsedRule, unparsedValues, err);
		}
	}

	/**
	 * Adds to the boxes size by some fixed number of pixels. FIXME: Fails when
	 * shrinking boxes by more width/height than they have.
	 */
	protected static class Resizer implements Filterable.Rule {
		private int[] delta = new int[4];
		/**
		 * takes the deltas in each of the four values.
		 * 
		 * @param x
		 *            the x delta
		 * @param y
		 *            the y delta
		 * @param width
		 *            the width delta
		 * @param height
		 *            the height delta
		 */
		public Resizer(int x, int y, int width, int height) {
			delta[0] = x;
			delta[1] = y;
			delta[2] = width;
			delta[3] = height;
		}

		/**
		 * @inheritDoc
		 */
		public boolean passes(Measurable o) {
			if (o == null)
				return true;
			Bbox measurable = (Bbox) o;
			BoundingBox box = (BoundingBox) measurable.value;
			box.set(box.getX() + delta[0], box.getY() + delta[1], box
					.getWidth()
					+ delta[2], box.getHeight() + delta[3]);
			return true;
		}

		/**
		 * @inheritDoc
		 */
		public String toString() {
			return "resize (\"" + delta[0] + "\", \"" + delta[1] + "\", \""
					+ delta[2] + "\", \"" + delta[3] + "\")";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return false
		 */
		public boolean isShortCircuit() {
			return false;
		}
	}

	/**
	 * Crops all rectangles to be within the given rectangle Returns false when
	 * the rectangle is without the window.
	 */
	protected static class Crops implements Filterable.Rule {
		private int minX;
		private int minY;
		private int maxX;
		private int maxY;
		/**
		 * The containing rectangle/window.
		 * 
		 * @param x
		 *            the x (top-left) coordinate of the cropping window
		 * @param y
		 *            the y (top-left) coordinate of the cropping window
		 * @param width
		 *            the width of the cropping window
		 * @param height
		 *            the height of the cropping window
		 */
		public Crops(int x, int y, int width, int height) {
			minX = x;
			minY = y;
			maxX = minX + width;
			maxY = minY + height;
			if (minX >= maxX || minY >= maxY) {
				throw new IllegalArgumentException("Bad rectangle for crop");
			}
		}

		/**
		 * @inheritDoc
		 */
		public boolean passes(Measurable o) {
			if (o == null)
				return true;
			Bbox measurable = (Bbox) o;
			BoundingBox box = (BoundingBox) measurable.value;

			if (box.getX() > maxX || (box.getX() + box.getWidth() < minX))
				return false;
			if (box.getY() > maxY || (box.getY() + box.getHeight() < minY))
				return false;

			int newX = Math.max(box.getX(), minX);
			int newWidth = Math.min(box.getX() + box.getWidth(), maxX) - newX;
			int newY = Math.max(box.getY(), minY);
			int newHeight = Math.min(box.getY() + box.getHeight(), maxY) - newY;

			box.set(newX, newY, newWidth, newHeight);

			return true;
		}

		/**
		 * @inheritDoc
		 */
		public String toString() {
			return "crop (\"" + minX + "\", \"" + minY + "\", \""
					+ (maxX - minX) + "\", \"" + (maxY - minY) + "\")";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return false
		 */
		public boolean isShortCircuit() {
			return false;
		}
	}
	/**
	 * Modifies the boxes widths and heights about the center.
	 */
	protected static class ProportionalResizer implements Filterable.Rule {
		private double deltaWidth;
		private double deltaHeight;

		/**
		 * takes the deltas in each of the width and height
		 * 
		 * @param width
		 *            the amount to multiply the width by
		 * @param height
		 *            the amount to multiply the height by
		 */
		public ProportionalResizer(double width, double height) {
			deltaWidth = width;
			deltaHeight = height;
		}

		/**
		 * @inheritDoc
		 */
		public boolean passes(Measurable o) {
			if (o == null)
				return true;
			Bbox measurable = (Bbox) o;
			BoundingBox box = (BoundingBox) measurable.value;
			int nw = (int) (deltaWidth * box.getWidth());
			int nx = box.getX() + (box.getWidth() - nw) / 2;
			int nh = (int) (deltaHeight * box.getHeight());
			int ny = box.getY() + (box.getHeight() - nh) / 2;
			box.set(nx, ny, nw, nh);
			return true;
		}

		/**
		 * @inheritDoc
		 */
		public String toString() {
			return "propresize (\"" + deltaWidth + "\", \"" + deltaHeight
					+ "\")";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>false</code>
		 */
		public boolean isShortCircuit() {
			return false;
		}
	}

	protected static class Bbox extends AbstractPolygonValue
			implements
				BoxInformation {
		
		public Object clone() {
			Bbox copy = new Bbox();
			BoundingBox oldBox = (BoundingBox) value;
			copy.value = new BoundingBox(oldBox.getRectangle());
			return copy;
		}
		
		/**
		 * @inheritDoc
		 * @return "bbox"
		 */
		public String getType() {
			return "bbox";
		}

		/**
		 * Sets the state of the object to the data the xml-dom element
		 * represents.
		 * 
		 * @param el
		 *            DOM Node to parse
		 * @return the new value of the attribute
		 * @throws IllegalArgumentException
		 *             If the data is ill-formed
		 */
		public AttributeValue setValue(Element el) {
			if (!"bbox".equals(el.getLocalName())) {
				throw new IllegalArgumentException("Unexpected data type: "
						+ el.getTagName());
			} else {
				try {
					Bbox copy = new Bbox();
					copy.value = new BoundingBox(parseAnInt(el, "x"),
							parseAnInt(el, "y"), parseAnInt(el, "width"),
							parseAnInt(el, "height"));
					return copy;
				} catch (BadDataException bdx) {
					throw new IllegalArgumentException(bdx.getMessage());
				}
			}
		}

		/**
		 * Sets the state of the object to the data the String represents.
		 * Useful for old GTF format Should be able to run setValue(toString())
		 * and have it come out the same.
		 * 
		 * @param S
		 *            String representation of this type of value.
		 * @return the new value
		 * @throws IllegalArgumentException
		 *             If the data is ill-formed
		 */
		public AttributeValue setValue(String S) {
			try {
				StringTokenizer st = new StringTokenizer(S);
				Bbox copy = new Bbox();
				copy.value = new BoundingBox(Integer.parseInt(st.nextToken()),
						Integer.parseInt(st.nextToken()), Integer.parseInt(st
								.nextToken()), Integer.parseInt(st.nextToken()));
				return copy;
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException(
						"Bad integer value in bbox string: " + S);
			} catch (NoSuchElementException nsex) {
				throw new IllegalArgumentException(
						"Not enough numbers in bbox string: " + S);
			}
		}

		/**
		 * Returns an xml element for this object, sans angle brackets, eg:
		 * <code>svalue value="something"</code>, so that the Attribute
		 * manager can add a span value, and namespace prefix, if necessary.
		 * 
		 * @param doc
		 *            the DOM Document to use when creating the element
		 * @return the DOM element
		 */
		public Element toXML(Document doc) {
			Element el = doc.createElementNS(Attributes.DEFAULT_NAMESPACE_URI,
					Attributes.DEFAULT_NAMESPACE_QUALIFIER + getType());
			BoundingBox bb = (BoundingBox) value;
			el.setAttribute("x", String.valueOf(bb.getX()));
			el.setAttribute("y", String.valueOf(bb.getY()));
			el.setAttribute("width", String.valueOf(bb.getWidth()));
			el.setAttribute("height", String.valueOf(bb.getHeight()));
			return el;
		}

		/**
		 * @inheritDoc
		 */
		public AbstractPolygonValue setValue(PolyList newVal) {
			Bbox copy = new Bbox();
			copy.value = newVal;
			return copy;
		}

		/**
		 * @inheritDoc
		 */
		public boolean contains(Pnt point) {
			return value.contains(point);
		}

		/**
		 * @inheritDoc
		 */
		public Pnt getCentroid() {
			return ((BoundingBox) value).getCentroid();
		}

		/**
		 * @inheritDoc
		 */
		public int getHeight() {
			return ((BoundingBox) value).getHeight();
		}

		/**
		 * @inheritDoc
		 */
		public Pnt getNearIntersection(Pnt q1) {
			return ((BoundingBox) value).getNearIntersection(q1);
		}

		/**
		 * @inheritDoc
		 */
		public int getRotation() {
			return ((BoundingBox) value).getRotation();
		}

		/**
		 * @inheritDoc
		 */
		public int getWidth() {
			return ((BoundingBox) value).getWidth();
		}

		/**
		 * @inheritDoc
		 */
		public int getX() {
			return ((BoundingBox) value).getX();
		}

		/**
		 * @inheritDoc
		 */
		public int getY() {
			return ((BoundingBox) value).getY();
		}

		/**
		 * @inheritDoc
		 */
		public boolean validate(AttributeValue v) {
			return v instanceof Attribute_bbox.Bbox;
		}
	}
}