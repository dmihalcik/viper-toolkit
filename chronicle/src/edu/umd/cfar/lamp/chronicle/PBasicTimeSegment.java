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
public class PBasicTimeSegment extends PNode {
	private Rectangle2D box = new Rectangle2D.Double();
	private Color color = Color.black;
	
	/**
	 * Constructs a new segment with the default color,
	 * black.
	 */
	public PBasicTimeSegment () {
		super();
	}
	
	/**
	 * Constructs a new segment with the given color.
	 * @param c the color for the segment
	 */
	public PBasicTimeSegment (Color c) {
		super();
		color = c;
	}
	
	/**
	 * Will create a box with color paint and 
	 * with height (boxheight * size).
	 * 
	 * @param paint
	 * @param size
	 */
	public PBasicTimeSegment (Paint paint, double size) {
		super();
	}

	Rectangle2D getBox() {
		return box;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		boolean r = super.setBounds(x, y, width, height);
		getBox().setFrame(x, y, width, height);
		return r;
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
		g2.setPaint(getPaint());
		g2.setColor(color);
		g2.fill(getBox());
	}
	
}
