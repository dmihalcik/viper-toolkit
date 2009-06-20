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

package edu.umd.cfar.lamp.viper.gui.data.obox;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cs.piccolo.event.*;

/**
 * 
 * @author clin
 */
public class OboxCanvasEditor extends GenericBoxEditor {
	public static final int HANDLE_RADIUS = 5;
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	protected boolean isNearNorthHandle = false;
	protected boolean isNearRightHandle = false;
	Point2D[] rightHandlePts;
	Point2D[] northHandlePts;
	double angle;
	double oboxWidth, oboxHeight;
	Point2D origCenter;

	public OboxCanvasEditor(Attributable attrIn) {
		// *********************************
		super(attrIn);
		box = (OboxNode) attrIn;
		logger.fine("Created OBOX EDITOR");

		// Local copy of information stored in oboxNode
		currPts = box.getBoxPts();
		rightHandlePts = ((OboxNode) box).getRightHandlePts();
		northHandlePts = ((OboxNode) box).getNorthHandlePts();
		oboxWidth = box.getBoxWidth();
		oboxHeight = box.getBoxHeight();
		angle = ((OboxNode) box).getAngleInRadians();
		// for debugging purposes

		for (int i = 0; i < 4; i++)
			origPts[i] = new Point2D.Double();
	}

	public String getName() {
		return "OboxEditor";
	}

	/**
	 * Gets the distance from the selection point to the edge of the box.
	 * 
	 * @param select
	 *            the point to test
	 * @return the shortest distance from select to the edge of the box
	 */
	public double minDist(Point2D select) {
		int startPoint = currPts.length - 1;
		double min = Double.POSITIVE_INFINITY;
		for (int endPoint = 0; endPoint < currPts.length; endPoint++) {
			Line2D test = new Line2D.Double(currPts[startPoint],
					currPts[endPoint]);
			min = Math.min(min, test.ptSegDist(select));
			startPoint = endPoint;
		}
		return min;
	}

	// Should be fixed
	public boolean contains(Point2D select) {
		boolean result = true;
		for (int i = 0; i < 4; i++)
			result = result
					&& isRightOf(currPts[i], currPts[(i + 1) % 4], select);
		return result;
	}

	/**
	 * 
	 * @param first
	 *            Tail of a vector
	 * @param second
	 *            Head of a vector
	 * @param select
	 *            Point is right of it?
	 * @return true if it's right, false if not
	 */
	boolean isRightOf(Point2D first, Point2D second, Point2D select) {
		Line2D edge = new Line2D.Double(first, second);
		return edge.relativeCCW(select) == -1;
	}

	public void mouseMoved(PInputEvent e) {
		isNearRightHandle = nearRightHandle(e.getPosition());
		isNearNorthHandle = nearNorthHandle(e.getPosition());
		handleMouseCommon(e);
	}

	public void doWhenUnselected() {
		// XXX Hack fix for npe. not sure why this code is being called in the
		// first place
		if (box != null) {
			box.unbold();
		}
	}

	public void mousePressed(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		
		logger.fine("OboxCanvasEditor: mousePressed");
		pressPoint = e.getPosition();
		dragPoint = pressPoint; // need this for computeAndSetAngle()

		isNearNorthHandle = nearNorthHandle(pressPoint);
		isNearRightHandle = nearRightHandle(pressPoint);
		origWidth = oboxWidth;
		origHeight = oboxHeight;
		origCenter = box.getCenterPt();
		origPressPoint.setLocation(pressPoint);
		// Record original top left
		for (int i = 0; i < 4; i++) {
			origPts[i].setLocation(currPts[i]);
		}
		if (isNearNorthHandle || isNearRightHandle) {
			logger.fine("Near handle");
			currDir = CanvasDir.NONE;
			computeAndSetAngle();
		} else {
			currDir = findDirection(pressPoint);
			logger.fine("Direction is " + currDir);

			box.bold(currDir);
		}
	}

	public boolean nearRightHandle(Point2D pt) {
		double dist = pt.distance(rightHandlePts[1]);
		return dist < HANDLE_RADIUS;
	}
	public boolean nearNorthHandle(Point2D pt) {
		double dist = pt.distance(northHandlePts[1]);
		return dist < HANDLE_RADIUS;
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		
		//		oboxNode.unbold() ;
		dragPoint = e.getPosition();
		handleMouseCommon(e);
	}

	/**
	 * Checks to see if the event has the ctrl mask. Puts the controller in the
	 * appropriate draw state, depending on event.
	 * 
	 * @param e
	 *            the (mouse?) event to check
	 */
	protected void verifyCtrlIsPressed(PInputEvent e) {
		boolean ctrlPressed = 0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK);
		setDrawnFromCenter((ctrlPressed && !DEFAULT_DRAW_FROM_CENTER)
				|| (!ctrlPressed && DEFAULT_DRAW_FROM_CENTER));
	}

	private void handleMouseCommon(PInputEvent e) {
		verifyCtrlIsPressed(e);
		OboxNode obox = (OboxNode) box;
		obox.unbold();
		CanvasDir hoverDir = CanvasDir.NONE;
		if (isNearRightHandle) {
			obox.boldRightHandle();
		} else if (isNearNorthHandle) {
			obox.boldNorthHandle();
		} else {
			hoverDir = findDirection(e.getPosition());
		}
		boolean nearAHandle = isNearRightHandle || isNearNorthHandle;
		if (isLeftClicked(e)){
			if (nearAHandle) {
				computeAndSetAngle();
			} else {
				if (!(currDir == CanvasDir.NONE || currDir == CanvasDir.INTERIOR))
					resizeBox(currDir);
				else
					shift();
			}
		}
		if (!nearAHandle) {
			box.bold(hoverDir);
		}
	}

	public void mouseReleased(PInputEvent e) {
		if (!e.isLeftMouseButton()) {
			return;
		}
		
		dragPoint = e.getPosition();
		handleMouseCommon(e);
	}

	public static double MIN_HEIGHT = 8.0;
	public static double MIN_WIDTH = 8.0;
	public void shift() {
		MathVector shift = new MathVector(origPressPoint, dragPoint);
		currPts[0].setLocation(origPts[0].getX() + shift.getX(), origPts[0]
				.getY()
				+ shift.getY());
		((OboxNode) box).updateObox();
	}

	Point2D origPressPoint = new Point2D.Double();
	double origWidth, origHeight;

	// Right edge must have first point "above" second point
	// Thus, if you draw from the first point to the second, it should point
	// "down"
	private boolean rightOf(Line2D edge) {
		return edge.relativeCCW(dragPoint) == 1;
	}

	// Right edge must have first point "right of" second point
	// Thus, if you draw from the first point to the second, it should go "left"
	private boolean below(Line2D edge) {
		return edge.relativeCCW(dragPoint) == 1;
	}
	/**
	 * Computes the angle from the upper left hand corner of the obox and the
	 * drag point, and sets the orientation to that angle
	 *  
	 */
	private void computeAndSetAngle() {
		if (isNearRightHandle) {
			angle = MathVector.computeAngle(origPts[0], dragPoint);
			if (isConstrainingAspectRatio()) {
				angle = Util.snapAngleToMajorDirection(angle);
			}
			((OboxNode) box).setAngleInRadians(angle);
		} else if (isNearNorthHandle) {
			angle = MathVector.computeAngle(origCenter, dragPoint) - (Math.PI/2);
			if (isConstrainingAspectRatio()) {
				angle = Util.snapAngleToMajorDirection(angle);
			}
			OboxRectangle referenceBox = new OboxRectangle(origCenter.getX(), origCenter.getY(), -oboxWidth/2, -oboxHeight/2, angle);
			currPts[0] = referenceBox.p[2];
			((OboxNode) box).setAngleInRadians(angle);
		}
	}

	public void resizeBox(CanvasDir dir) {
		MathVector diff = new MathVector(origPressPoint, dragPoint);

		Point2D saveTopLeft = currPts[0];

		// Compute change to height and width
		double diffWidth = MathVector.computeWidth(diff, angle);
		double diffHeight = -MathVector.computeHeight(diff, angle);

		CanvasDir fixedPoint;
		// Zero out height and width, if they don't change
		if (dir == CanvasDir.BOTTOM_RIGHT || dir == CanvasDir.BOTTOM) {
			fixedPoint = CanvasDir.TOP_LEFT;
			if (dir == CanvasDir.BOTTOM)
				diffWidth = 0;
		} else if (dir == CanvasDir.TOP_RIGHT || dir == CanvasDir.RIGHT) {
			fixedPoint = CanvasDir.BOTTOM_LEFT;
			if (dir == CanvasDir.RIGHT)
				diffHeight = 0;
			else
				// compensates for change
				diffHeight = -diffHeight;
		} else if (dir == CanvasDir.TOP_LEFT || dir == CanvasDir.TOP) {
			fixedPoint = CanvasDir.BOTTOM_RIGHT;
			if (dir == CanvasDir.TOP)
				diffWidth = 0;
			else
				diffWidth = -diffWidth;
			diffHeight = -diffHeight;
		} else {
			fixedPoint = CanvasDir.TOP_RIGHT;
			if (dir == CanvasDir.LEFT)
				diffHeight = 0;
			diffWidth = -diffWidth;
		}
		if (this.isDrawnFromCenter()) {
			diffWidth *= 2;
			diffHeight *= 2;
			fixedPoint = CanvasDir.INTERIOR;
		}
		
		// Update height
		oboxHeight = origHeight + diffHeight;
		if (oboxHeight < MIN_HEIGHT)
			oboxHeight = MIN_HEIGHT;
		// Update width
		oboxWidth = origWidth + diffWidth;
		if (oboxWidth < MIN_WIDTH)
			oboxWidth = MIN_WIDTH;

		if(isConstrainingAspectRatio()) {
			double widthRatio = oboxWidth / origWidth;
			double heightRatio = oboxHeight / origHeight;
			if (widthRatio < heightRatio) {
				oboxWidth = origWidth * heightRatio;
			} else {
				oboxHeight = origHeight * widthRatio;
			}
		}
		
		fixTopLeftCorner(fixedPoint);

		// Now update info in the oboxNode
		((OboxNode) box).setWidthAndHeight(oboxWidth, oboxHeight);
	}

	private void fixTopLeftCorner(CanvasDir fixedPoint) {
		OboxRectangle referenceBox;
		if (fixedPoint == CanvasDir.BOTTOM_RIGHT) {
			referenceBox = new OboxRectangle(origPts[2].getX(), origPts[2]
					.getY(), -oboxWidth, -oboxHeight, angle);
			currPts[0] = referenceBox.p[2];
		} else if (fixedPoint == CanvasDir.BOTTOM_LEFT) {
			referenceBox = new OboxRectangle(origPts[3].getX(), origPts[3]
					.getY(), oboxWidth, -oboxHeight, angle);
			currPts[0] = referenceBox.p[3];
		} else if (fixedPoint == CanvasDir.TOP_RIGHT) {
			referenceBox = new OboxRectangle(origPts[1].getX(), origPts[1]
					.getY(), -oboxWidth, oboxHeight, angle);
			currPts[0] = referenceBox.p[1];
		} else if (fixedPoint == CanvasDir.INTERIOR) {
			referenceBox = new OboxRectangle(origCenter.getX(), origCenter.getY(), -oboxWidth/2, -oboxHeight/2, angle);
			currPts[0] = referenceBox.p[2];
		}
	}

	public boolean inRangeOfInterest(Point2D point) {
		return nearRightHandle(point) || nearNorthHandle(point) || findDirection(point) != CanvasDir.NONE;
	}
}