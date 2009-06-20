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

package edu.umd.cfar.lamp.viper.gui.data.point;


import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PointCanvasEditor extends CanvasEditor {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	PPath base; // local reference to base class

	PointNode pntNode;

	public PointCanvasEditor(Attributable attrIn) {
		// *********************************
		super(attrIn);
		pntNode = (PointNode) attrIn;
		logger.fine("Created PNT EDITOR");
	}

	public String getName() {
		return "PointEditor";
	}

	Point2D pressPoint, dragPoint;

	public double minDist(Point2D select) {
		Point2D location = pntNode.getPoint();
		return location.distance(select);
	}

	public static final int MIN_DIST = 20;

	public boolean contains(Point2D select) {
		return minDist(select) < MIN_DIST;
	}

	public void keyPressed(PInputEvent e) {

	}

	private static int LEFT_MOUSE_MASK = InputEvent.BUTTON1_MASK;

	public void doWhenUnselected() {
		// nothing needs to happen
	}

	public void mousePressed(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		
		logger.fine("PointCanvasEditor: mousePressed");

		pntNode.setPoint(e.getPosition());
	}

	public void mouseMoved(PInputEvent e) {
		Point2D localPoint = e.getPosition();

		ShapeDisplayProperties style = HighlightSingleton.STYLE_UNSELECTED;
		boolean s = this.isSelected();
		boolean h = contains(localPoint);
		boolean l = isLocked(e);
		
		if (l) {
			// Don't bother indicating hover state if the point is locked.
			style = s ? HighlightSingleton.STYLE_LOCKED_SELECTED : HighlightSingleton.STYLE_LOCKED_UNSELECTED;
		} else {
			// point is unlocked, so hover information is useful
			if (h) {
				style = s ? HighlightSingleton.STYLE_HOVER : HighlightSingleton.STYLE_HANDLE;
			} else {
				// the normal view
				style = s ? HighlightSingleton.STYLE_SELECTED : HighlightSingleton.STYLE_UNSELECTED;
			}
		}
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;

		pntNode.setPoint(e.getPosition());
	}

	public void mouseReleased(PInputEvent e) {
		if (!e.isLeftMouseButton()) {
			return;
		}
		
		pntNode.setPoint(e.getPosition());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.CanvasEditor#inRangeOfInterest(java.awt.geom.Point2D)
	 */
	public boolean inRangeOfInterest(Point2D point) {
		return contains(point);
	}
}