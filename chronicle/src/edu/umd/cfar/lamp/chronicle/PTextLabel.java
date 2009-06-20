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

package edu.umd.cfar.lamp.chronicle;

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

/**
 * PText node modified to look more like a tooltip.
 */
public class PTextLabel extends PText {
	private Paint fill = Color.white;
	private Stroke borderStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private Paint borderColor = Color.black;
	private double borderCurveRadius = 4;
	private double textInset = 2;
	private double xTextOffset;
	private double yTextOffset;

	// used to adjust the size of the box manually
	private double wOffset;
	private double hOffset;

	/**
	 * 
	 */
	public PTextLabel() {
		super();
	}

	/**
	 * @param text
	 */
	public PTextLabel(String text) {
		super(text);
		wOffset = 0;
		hOffset = 0;
	}

	/**
	 * Draws a box behind the thing.
	 * @param paintContext the paint context
	 */
	public void paint(PPaintContext paintContext) {
		Graphics2D g2 = paintContext.getGraphics();
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();
		
		try {
			double x1 = getBoundsReference().getMinX();
			double y1 = getBoundsReference().getMinY();
			double x2 = getBoundsReference().getMaxX();
			double y2 = getBoundsReference().getMaxY();
			
			double w = x2-x1 + wOffset;
			double h = y2-y1 + hOffset;
	
			Shape flag = new RoundRectangle2D.Double(x1, y1, w, h, borderCurveRadius*2, borderCurveRadius*2);
			g2.setPaint(fill);
			g2.fill(flag);
			g2.setPaint(borderColor);
			g2.setStroke(borderStroke);
			g2.draw(flag);
	
			PBounds b = super.getBoundsReference();
			b.x += xTextOffset; b.y += yTextOffset;
			try {
				super.paint(paintContext);
			} finally {
				b.x -= xTextOffset; b.y -= yTextOffset;
			}
		} finally {
			g2.setPaint(oldPaint);
			g2.setStroke(oldStroke);
		}
	}

	/**
	 * @see edu.umd.cs.piccolo.nodes.PText#recomputeLayout()
	 */
	public void recomputeLayout() {
		super.recomputeLayout();
		PBounds b = super.getBoundsReference();
		b.width += 2*textInset;
		b.height += 2*textInset;
		double minLen = 2*borderCurveRadius;
		xTextOffset = yTextOffset = textInset;
		if (b.width < minLen) {
			xTextOffset = textInset + (minLen - b.width)/2;
			b.width = minLen;
		}
		if (b.height < minLen) {
			yTextOffset = textInset + (minLen - b.height)/2;
			b.height = minLen;
		}
	}

	/**
	 * @return
	 */
	public Paint getFill() {
		return fill;
	}

	/**
	 * @return
	 */
	public double getTextInset() {
		return textInset;
	}

	/**
	 * @return
	 */
	public Paint getBorderColor() {
		return borderColor;
	}

	/**
	 * @return
	 */
	public double getBorderCurveRadius() {
		return borderCurveRadius;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	/**
	 * @param color
	 */
	public void setFill(Paint color) {
		if (!color.equals(fill)) {
			fill = color;
			this.invalidatePaint();
		}
	}

	/**
	 * @param i
	 */
	public void setTextInset(double i) {
		if (i != textInset) {
			textInset = i;
			this.invalidatePaint();
		}
	}

	/**
	 * @param color
	 */
	public void setBorderColor(Paint color) {
		if (!borderColor.equals(color)) {
			borderColor = color;
			this.invalidatePaint();
		}
	}

	/**
	 * @param i
	 */
	public void setBorderCurveRadius(double i) {
		if (borderCurveRadius != i) {
			borderCurveRadius = i;
			this.invalidatePaint();
		}
	}

	public void setBorderStroke(Stroke stroke) {
		if (!stroke.equals(this.borderStroke)) {
			this.borderStroke = stroke;
			this.invalidatePaint();
		}
	}
	
	/**
	 * @param offset The hOffset to set.
	 */
	public void setHOffset(double offset) {
		hOffset = offset;
	}
	
	/**
	 * @param offset The wOffset to set.
	 */
	public void setWOffset(double offset) {
		wOffset = offset;
	}
}
