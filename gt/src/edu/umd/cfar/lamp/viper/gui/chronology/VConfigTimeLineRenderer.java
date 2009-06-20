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


package edu.umd.cfar.lamp.viper.gui.chronology;

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;

/**
 * 
 */
public class VConfigTimeLineRenderer extends AbstractAttributeSegmentRenderer {
	/**
	 * 
	 */
	public VConfigTimeLineRenderer() {
		super();
		setColorings(GREENS);
		setAlignment(ALIGN_BOTTOM);
		setStyle(STYLE_NOMINAL);
	}
	private double factor = .1;
	public static final Color[] EVAL_COLORS =
		{ Color.green, Color.blue, Color.red, Color.magenta };
	public static final Color[] DUOTONE_GREEN =
		{ ColorUtilities.getColor("darkgreen"), ColorUtilities.getColor("greenyellow")};
	public static final Color[] DUOTONE_GRAY =
		{ ColorUtilities.getColor("darkgray"), ColorUtilities.getColor("silver")};
	public static final Color[] GREENS =
		{
			ColorUtilities.getColor("darkgreen"),
			ColorUtilities.getColor("darkseagreen"),
			ColorUtilities.getColor("darkolivegreen"),
			ColorUtilities.getColor("forestgreen"),
			ColorUtilities.getColor("green"),
			ColorUtilities.getColor("greenyellow")};
	public static final Color[] GRAYS =
		{
			ColorUtilities.getColor("darkgray"),
			ColorUtilities.getColor("lightgray"),
			ColorUtilities.getColor("gray"),
			ColorUtilities.getColor("lightslategray"),
			ColorUtilities.getColor("silver"),
			ColorUtilities.getColor("slategray")};
	public static final Color[] ALL_COLORS =
		{
			ColorUtilities.getColor("green"),
			ColorUtilities.getColor("blue"),
			ColorUtilities.getColor("gray"),
			ColorUtilities.getColor("red"),
			ColorUtilities.getColor("black"),
			ColorUtilities.getColor("orange"),
			ColorUtilities.getColor("darksalmon"),
			ColorUtilities.getColor("darkseagreen"),
			ColorUtilities.getColor("peru"),
			ColorUtilities.getColor("turquoise"),
			ColorUtilities.getColor("violet"),
			ColorUtilities.getColor("deepskyblue"),
			ColorUtilities.getColor("darkmagenta"),
			ColorUtilities.getColor("aqua"),
			ColorUtilities.getColor("blueviolet"),
			ColorUtilities.getColor("brown"),
			ColorUtilities.getColor("cadetblue"),
			ColorUtilities.getColor("chocolate"),
			ColorUtilities.getColor("crimson"),
			ColorUtilities.getColor("cornflowerblue"),
			ColorUtilities.getColor("coral"),
			ColorUtilities.getColor("darkblue"),
			ColorUtilities.getColor("darkcyan"),
			ColorUtilities.getColor("darkgoldenrod"),
			ColorUtilities.getColor("darkgray"),
			ColorUtilities.getColor("darkgreen"),
			ColorUtilities.getColor("olivedrab"),
			ColorUtilities.getColor("dodgerblue"),
			ColorUtilities.getColor("purple"),
			ColorUtilities.getColor("gold"),
			ColorUtilities.getColor("yellowgreen"),
			ColorUtilities.getColor("slateblue")};

	private Color[] colorings = EVAL_COLORS;

	private static int extractCount(Object o) {
		if (o instanceof Number) {
			return ((Number) o).intValue();
		} else if (o instanceof Numeric) {
			return ((Numeric) o).intValue();
		} else if (o instanceof Object[]) {
			Object[] multi = (Object[]) o;
			int count = 0;
			for (int k = 0; k < multi.length; k++) {
				count += extractCount(multi[k]);
			}
			return count;
		} else if (o == null) {
			return 0;
		} else {
			return 1;
		}
	}

	public PNode makeSegment(Interval span, double width, double height) {
		Object val = ((DynamicValue) span).getValue();
		Object[] v = (Object[]) val;

		return new SegsNode(v, this);
	}

	public static class SegsNode extends PNode {
		private Object[] vals;
		private int total = 0;
		private double maxHeight;
		private double factor;
		private int style;
		private int alignment;
		private Color[] colorings;
		public SegsNode(Object[] vals, VConfigTimeLineRenderer f) {
			this.vals = vals;
			for (int i = 0; i < vals.length; i++) {
				total += extractCount(vals[i]);
			}
			maxHeight = f.factor * total;
			style = f.getStyle();
			alignment = f.getAlignment();
			colorings = f.colorings;
			factor = f.factor;
		}
		public boolean setBounds(
			double x,
			double y,
			double width,
			double height) {
			return super.setBounds(x, y, width, height);
		}
		public void paint(PPaintContext aPaintContext) {
			Graphics2D g2 = aPaintContext.getGraphics();
			Paint oldP = g2.getPaint();
			Color oldC = g2.getColor();
			g2.setPaint(getPaint());

			double soFar = 0;
			double x, y, width, height;
			for (int i = 0; i < vals.length; i++) {
				Rectangle2D.Double box = new Rectangle2D.Double();
				x = super.getBoundsReference().x;
				y = super.getBoundsReference().y;
				width = super.getBoundsReference().width;
				height = super.getBoundsReference().height;
				Color neoColor = oldC;
				switch (style) {
					case STYLE_WIDE :
						neoColor = colorings[i % colorings.length];
						double coef = maxHeight > 1 ? 1 / maxHeight : factor;
						height = height * coef * extractCount(vals[i]);
						switch (alignment) {
							case ALIGN_TOP :
								y += soFar;
								break;
							case ALIGN_CENTER :
								y += soFar * .5;
								break;
							case ALIGN_BOTTOM :
								y += super.getBoundsReference().height - soFar;
								break;
							default :
								throw new IllegalArgumentException(
									"Bad alignment in BagSegment: "
										+ alignment);
						}
						soFar += height;
						break;

					case STYLE_NOMINAL :
						height = Math.min(height / vals.length, height * factor);
						soFar += height;
						if (vals[i] == null) {
							continue;
						}
						neoColor = colorings[i % colorings.length];
						y += height * i;
						break;
					default :
						System.err.println("Not a valid style: " + style);
				}

				g2.setColor(neoColor);
				box.setFrame(x, y, width, height);

				g2.fill(box);
			}

			g2.setColor(oldC);
			g2.setPaint(oldP);
		}
		public boolean intersects(Rectangle2D aBounds) {
			return this.getBoundsReference().intersects(aBounds);
		}
	}

	public Color[] getColorings() {
		return colorings;
	}
	public void setColorings(Color[] colors) {
		colorings = colors;
	}

	public static final int ALIGN_TOP = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_BOTTOM = 2;
	private int alignment = ALIGN_CENTER;
	public int getAlignment() {
		return alignment;
	}
	public void setAlignment(int i) {
		alignment = i;
	}

	public static final int STYLE_WIDE = 0;
	public static final int STYLE_BRIGHT = 1;
	public static final int STYLE_NOMINAL = 2;
	private int style = STYLE_WIDE;
	public int getStyle() {
		return style;
	}
	public void setStyle(int i) {
		style = i;
	}

	public double getPreferredHeight() {
		/// TODO: bag segment factory should return height proportional to total size of bag
		return 32;
	}
}
