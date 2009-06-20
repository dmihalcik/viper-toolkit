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

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;

/**
 * A simple pnode indicating a selection of time.
 */
public class PRoundedTimeSegment extends PNode {
	private RoundRectangle2D box = new RoundRectangle2D.Double();
	private Color fillColor = Color.blue;
	private Color lineColor = Color.black;
	private double arcRadius = 20;
	
	/**
	 * Constructs a new segment with the default color,
	 * black.
	 */
	public PRoundedTimeSegment () {
		super();
	}
	
	RoundRectangle2D getBox() {
		return box;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		if (super.setBounds(x, y, width, height))  {
			resetBox();
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Called when the bounds changes size; this redraws the box to keep the corners nice
	 */
	protected void resetBox() {
		PBounds bounds = super.getBoundsReference();
		double x = bounds.getX(); double y = bounds.getY(); double width = bounds.getWidth(); double height = bounds.getHeight();
		double arcWidth = Math.min(width/2, arcRadius);
		double arcHeight = Math.min(height/2, arcRadius);
		if (arcWidth > arcHeight) {
			arcWidth = arcHeight;
		}
		getBox().setRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(Rectangle2D aBounds) {
		return getBox().intersects(aBounds);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void paint(PPaintContext aPaintContext) {
		Graphics2D g2 = aPaintContext.getGraphics(); 
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();
		try {
			g2.setPaint(fillColor);
			g2.fill(getBox());
			g2.setPaint(lineColor);
			g2.draw(getBox());
		} finally {
			g2.setPaint(oldPaint);
			g2.setStroke(oldStroke);
		}
		
	}
	public double getArcRadius() {
		return arcRadius;
	}
	public void setCornerRadius(double cornerRadius) {
		this.arcRadius = cornerRadius;
		resetBox();
		invalidatePaint();
	}
	public Color getFillColor() {
		return fillColor;
	}
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
		invalidatePaint();
	}
	public Color getLineColor() {
		return lineColor;
	}
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
		invalidatePaint();
	}
}
