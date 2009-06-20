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

package edu.umd.cfar.lamp.chronicle.markers;

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;


/**
 * A round sign containing a String message.
 */
public class CircleSignHeader extends PNode {
	private PTextLabel message;
	private Line2D line;
	private double signHeight;
	/**
	 * Creates a new sign with the given message
	 * @param message the message to display on the sign
	 * @param signHeight a value, in the range of 0 to 1, indicating how high off the 
	 * ruler's bottom the sign should be placed
	 */
	public CircleSignHeader(String message, double signHeight) {
		super();
		this.message = new PTextLabel(message);
		this.signHeight = signHeight;
		this.message.setTextInset(0);
	}

	private ChronicleMarkerNode getContainingNode() {
		return (ChronicleMarkerNode) getParent();
	}

	protected void paint(PPaintContext paintContext) {
		if (true) { // ORIENTATION = VERTICAL
			Graphics2D g2 = paintContext.getGraphics();
			Paint old = g2.getPaint();
			g2.setPaint(getContainingNode().getLineStyle());
			message.setTextPaint(getContainingNode().getLineStyle());
			message.setBorderColor(getContainingNode().getLineStyle());
			message.setFill(getContainingNode().getFillStyle());
			g2.draw(line);
			g2.setPaint(old);
			message.paint(paintContext);
		}
	}

	/**
	 * @see PNode#setBounds(double, double, double, double)
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		if (super.setBounds(x, y, width, height)) {
			double r = Math.min(this.message.getWidth(), this.message.getHeight());
			r = Math.max(r, width)/2;
			if (this.message.getBorderCurveRadius() != r) {
				this.message.setBorderCurveRadius(r);
				this.message.recomputeLayout();
			}

			double x1 = getBoundsReference().getMinX();
			double x2 = getBoundsReference().getMaxX();
			double y2 = getBoundsReference().getMaxY();
			
			double bottom = y2;
			double middle = (int) ((x2 + x1) / 2);

			double mw = message.getWidth();
			double mh = message.getHeight();
			double availableHeight = height - mh;
			availableHeight *= signHeight;
			double flagBottom = bottom - availableHeight;
			
			line = new Line2D.Double (middle, bottom, middle, flagBottom);
			
			double daX = middle-(mw/2);
			double daY = mh-availableHeight;
			message.setBounds(daX, daY, mw, mh);
			return true;
		}
		return false;
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D localBounds) {
		return message.intersects(localBounds) || line.intersects(localBounds);
	}

}
