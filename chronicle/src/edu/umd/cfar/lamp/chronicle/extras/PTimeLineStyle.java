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

package edu.umd.cfar.lamp.chronicle.extras;

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;

/**
 * The timeline style is the style of the stuff drawn behind the 
 * timeline segments. This could include things like tinting and 
 * borders, or wierder things like time marks and so forth.
 * @author davidm
 */
public class PTimeLineStyle extends PNode {
	private Color fill;
	private Color pen;
	private Stroke strokeWidth;
	
	/**
	 * Constructs a new default timeline style node,
	 * with a white background and light gray pen
	 * with a hairline nib.
	 */
	public PTimeLineStyle() {
		fill = Color.white;
		pen = Color.lightGray;
		strokeWidth = new BasicStroke(1);
	}
	protected void paint(PPaintContext aPaintContext) {
		Graphics2D g2 = aPaintContext.getGraphics(); 
		g2.setPaint(fill);
		PBounds toDraw = this.getBounds();
		//toDraw.inset(1,1);
		g2.fill(toDraw);
		g2.setStroke(strokeWidth);
		g2.setPaint(pen);
		g2.draw(toDraw);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(Rectangle2D aBounds) {
		return getBoundsReference().intersects(aBounds);
	}
	
	/**
	 * Gets the fill color for the timeline.
	 * This is drawn behind the timeline segments.
	 * @return the background color.
	 */
	public Color getFillColor() {
		return fill;
	}

	/**
	 * Gets the color for use with the lines.
	 * @return the line color
	 */
	public Color setStrokeColor() {
		return pen;
	}

	/**
	 * Gets the stroke with for use with the segments.
	 * @return the preferred stroke width 
	 */
	public Stroke getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * Sets the background color.
	 * @param color the background color
	 */
	public void setFillColor(Color color) {
		fill = color;
	}

	/**
	 * Sets the color of the pen.
	 * @param color the pen color
	 */
	public void setStrokeColor(Color color) {
		pen = color;
	}

	/**
	 * Sets the preferred stroke width.
	 * @param stroke the preferred stroke width
	 */
	public void setStrokeWidth(Stroke stroke) {
		strokeWidth = stroke;
	}

}
