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

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;


/**
 * A frame flag header is a flag-type shape with no
 * text inside.
 */
public class FrameFlagHeader extends PNode {
	private Shape flag;
	private Line2D line;

	private ChronicleMarkerNode getContainingNode() {
		return (ChronicleMarkerNode) getParent();
	}

	protected void paint(PPaintContext paintContext) {
		if (true) { // ORIENTATION = VERTICAL
			Graphics2D g2 = paintContext.getGraphics();
			Paint old = g2.getPaint();
			g2.setPaint(getContainingNode().getLineStyle());
			g2.draw(line);
			g2.fill(flag);

			g2.setPaint(old);
		}
	}


	/**
	 * @inheritDoc
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		if (super.setBounds(x, y, width, height)) {
			double x1 = getBoundsReference().getMinX();
			double y1 = getBoundsReference().getMinY();
			double x2 = getBoundsReference().getMaxX();
			double y2 = getBoundsReference().getMaxY();
			
			int bottom = (int) y2;
			int halfWidth = (int) (Math.min(y2-y1, x2-x1)/3);
			int h = halfWidth * 2 + 1;
			int flagBottom = (int) (y2 - (y2-y1)/4) - h;
			int middle = (int) ((x2 + x1) / 2);
			
			line = new Line2D.Double(middle, bottom, middle, flagBottom);
						
			GeneralPath flagPath = new GeneralPath();
			flagPath.moveTo((float) x1, (float) y1);
			flagPath.lineTo(middle, flagBottom);
			flagPath.lineTo((float) x2, (float) y1);
			flagPath.closePath();
			flag = flagPath;

			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D localBounds) {
		return flag.intersects(localBounds) || line.intersects(localBounds);
	}

}
