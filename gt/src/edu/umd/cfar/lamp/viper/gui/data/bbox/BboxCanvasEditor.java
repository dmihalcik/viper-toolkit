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

package edu.umd.cfar.lamp.viper.gui.data.bbox;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cs.piccolo.event.*;

/**
 * @author clin
 */
public class BboxCanvasEditor extends GenericBoxEditor {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");

	private boolean mouseDown = false;
	private CanvasDir mouseMovedDir = CanvasDir.NONE;
	private double bboxWidth, bboxHeight;

	public BboxCanvasEditor(Attributable attrIn) {
		// *********************************
		super(attrIn);
		box = (BboxNode) attrIn;
		logger.fine("Created BBOX EDITOR");

		// Local copy of information stored in bboxNode
		currPts = box.getBoxPts();
		bboxWidth = box.getBoxWidth();
		bboxHeight = box.getBoxHeight();

		for (int i = 0; i < 4; i++)
			origPts[i] = new Point2D.Double();
	}

	public String getName() {
		return "BboxEditor";
	}

	public double minDist(Point2D select) {
		Point2D upperLeft = currPts[0];
		Point2D upperRight = currPts[1];
		Point2D lowerRight = currPts[2];
		Point2D lowerLeft = currPts[3];

		// Create lines
		Line2D top = new Line2D.Double(upperLeft, upperRight);
		Line2D left = new Line2D.Double(upperLeft, lowerLeft);
		Line2D right = new Line2D.Double(upperRight, lowerRight);
		Line2D bottom = new Line2D.Double(lowerLeft, lowerRight);

		double min = top.ptSegDist(select);
		if (left.ptSegDist(select) < min)
			min = left.ptSegDist(select);
		if (right.ptSegDist(select) < min)
			min = right.ptSegDist(select);
		if (bottom.ptSegDist(select) < min)
			min = bottom.ptSegDist(select);

		return min;
	}

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
		mouseDown = false;
		//if (!isLeftClicked(e))
		//	return;
		mouseMovedDir = findDirection(e.getPosition());
		box.bold(mouseMovedDir);
	}

	public void doWhenUnselected() {
		if (box!= null) {
			box.unbold();
		}
	}

	Point2D centerPt = null;
	//
	double pressedWidth, pressedHeight;

	// Records the orignal bbox at time of mouse press
	// Used for computing the adjusted box
	Point2D origTopLeft = new Point2D.Double();
	boolean pressedAtCorner = false;
	boolean pressedAtEdge = false;

	public void mousePressed(PInputEvent e) {
		interpretModifiers(e);
		if (!isLeftClicked(e)) {
			return;
		}
		mouseDown = true;
		// Find the center point
		centerPt = box.getCenterPt();
		pressedWidth = box.getBoxWidth() / 2;
		pressedHeight = box.getBoxHeight() / 2;

		logger.fine("BboxCanvasEditor: mousePressed");
		pressPoint = e.getPosition();
		dragPoint = pressPoint; // need this for computeAndSetAngle()

		currDir = findDirection(pressPoint);
		box.bold(currDir);
		logger.fine("Direction is " + currDir);

		// Record whether corner is pressed
		pressedAtCorner = isCorner(currDir);
		// Record whether edge was selected
		pressedAtEdge = isEdge(currDir);

		// Record original top left
		origTopLeft = new Point2D.Double();
		origTopLeft.setLocation(currPts[0]);

		// Record original top left
		for (int i = 0; i < 4; i++) {
			origPts[i].setLocation(currPts[i]);
		}
		origWidth = bboxWidth;
		origHeight = bboxHeight;
		origPressPoint.setLocation(pressPoint);
	}

	public void mouseDragged(PInputEvent e) {
		interpretModifiers(e);
		if (!isLeftClicked(e)) {
			return;
		}
		mouseDown = true;
		//		bboxNode.unbold() ;
		dragPoint = e.getPosition();
		handleMouseCommon();
	}

	private void handleMouseCommon() {
		if (!(currDir == CanvasDir.NONE || currDir == CanvasDir.INTERIOR))
			resizeBox(currDir);
		else
			shift();
		box.bold(currDir);
	}

	/**
	 * Checks to see if the event has the ctrl mask. Puts the controller in the
	 * appropriate draw state, depending on event.
	 * 
	 * @param e
	 *            the (mouse?) event to check
	 */
	protected void interpretModifiers(PInputEvent e) {
		boolean ctrlPressed = 0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK);
		boolean shiftPressed = 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK);
		
		setDrawnFromCenter(DEFAULT_DRAW_FROM_CENTER ? !ctrlPressed : ctrlPressed);
		setConstrainingAspectRatio(DEFAULT_CONSTRAIN_ASPECT_RATIO ? !shiftPressed : shiftPressed);
	}

	public void mouseReleased(PInputEvent e) {
		interpretModifiers(e);
		if (isLeftClicked(e)) {
			return;
		}
		if (mouseDown) {
			dragPoint = e.getPosition();
			handleMouseCommon();
		}
		mouseDown = false;
	}


	public static double MIN_HEIGHT = 8.0;
	public static double MIN_WIDTH = 8.0;

	public void shift() {
		MathVector shift = new MathVector(origPressPoint, dragPoint);
		((BboxNode) box).setUpperLeft(origPts[0].getX() + shift.getX(),
				origPts[0].getY() + shift.getY());
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
	 * @param dir
	 */
	public void resizeBox(CanvasDir dir) {
		Rectangle2D rect = null;
		boolean isCorner = isCorner(dir);
		CanvasDir newDir = null;
		if (isDrawnFromCenter()) {
			if (isCorner) {
				rect = computeRectFromCenter(dragPoint);
				newDir = computeBoldDir(dragPoint, rect);
			} else if (isEdge(dir)) {
				rect = computeRectFromCenter(dragPoint, dir);
				newDir = computeBoldDir(dragPoint, rect, dir);
			}
		} else {
			if (isCorner) {
				Point2D fixedPoint = computeFixedPoint(dir);
				rect = computeRect(fixedPoint, dragPoint);
				newDir = computeBoldDir(dragPoint, rect);
				modifyRect(rect, fixedPoint, dragPoint);
			} else if (isEdge(dir)) {
				rect = computeRect(dragPoint, dir);
				newDir = computeBoldDir(dragPoint, rect, dir);
				modifyRect(rect, dragPoint, dir);
			}
		}
		box.bold(newDir);
		if (rect != null) {
			assert rect.getWidth() >= 0 && rect.getHeight() >= 0;

			((BboxNode) box).setBbox(rect.getX(), rect.getY(), rect.getWidth(), rect
					.getHeight());
		}
	}

	/**
	 * @param dragPoint
	 * @return
	 */
	private Rectangle2D computeRectFromCenter(Point2D dragPoint) {
		double[] newSize = diffPoint(centerPt, dragPoint);
		newSize[0] *= 2; newSize[1] *= 2;

		constrainNewSize(newSize);
		Rectangle2D rect = new Rectangle2D.Double(centerPt.getX() - newSize[0]/2,
				centerPt.getY() - newSize[1]/2, newSize[0], newSize[1]);
		return rect;
	}

	/**
	 * Applies minimum width and aspect ratio constraints
	 * to the given new size.
	 * @param newSize modified in place.
	 */
	private void constrainNewSize(double[] newSize) {
		// Guarantees minimum height and width
		if (newSize[0] < MIN_WIDTH) {
			newSize[0] = MIN_WIDTH;
		}
		if (newSize[1] < MIN_HEIGHT) {
			newSize[1] = MIN_HEIGHT;
		}

		if (isConstrainingAspectRatio()) {
			double widthRatio = newSize[0] / origWidth;
			double heightRatio = newSize[1] / origHeight;
			if (widthRatio < heightRatio) {
				newSize[0] = origWidth * heightRatio;
			} else {
				newSize[1] = origHeight * widthRatio;
			}
		}
	}

	/**
	 * @param dragPoint
	 * @return
	 */
	private Rectangle2D computeRectFromCenter(Point2D dragPoint, CanvasDir dir) {
		Point2D refPoint = null;

		if (dir == CanvasDir.BOTTOM || dir == CanvasDir.TOP)
			refPoint = new Point2D.Double(origTopLeft.getX(), dragPoint.getY());
		else
			// LEFT or RIGHT
			refPoint = new Point2D.Double(dragPoint.getX(), origTopLeft.getY());

		return computeRectFromCenter(refPoint);
	}

	final static double THRESHOLD = 0.001;
	final static int UPPER_LEFT = 0;
	final static int UPPER_RIGHT = 1;
	final static int LOWER_RIGHT = 2;
	final static int LOWER_LEFT = 3;
	private CanvasDir computeBoldDir(Point2D drag, Rectangle2D rect) {
		Point2D upperLeft = getRectCorner(rect, UPPER_LEFT);
		Point2D upperRight = getRectCorner(rect, UPPER_RIGHT);
		Point2D lowerLeft = getRectCorner(rect, LOWER_LEFT);
		Point2D lowerRight = getRectCorner(rect, LOWER_RIGHT);

		if (drag.distance(upperLeft) < THRESHOLD)
			return CanvasDir.TOP_LEFT;
		else if (drag.distance(upperRight) < THRESHOLD)
			return CanvasDir.TOP_RIGHT;
		else if (drag.distance(lowerLeft) < THRESHOLD)
			return CanvasDir.BOTTOM_LEFT;
		else if (drag.distance(lowerRight) < THRESHOLD)
			return CanvasDir.BOTTOM_RIGHT;
		return null;
	}

	private static Point2D getRectCorner(Rectangle2D rect, int direction) {
		double x = rect.getX();
		double y = rect.getY();
		double width = rect.getWidth();
		double height = rect.getHeight();
		if (direction == UPPER_LEFT) {
			return new Point2D.Double(x, y);
		} else if (direction == UPPER_RIGHT) {
			return new Point2D.Double(x + width, y);
		} else if (direction == LOWER_RIGHT) {
			return new Point2D.Double(x + width, y + height);
		} else // LOWER_LEFT
		{
			return new Point2D.Double(x, y + height);
		}
	}

	private CanvasDir computeBoldDir(Point2D drag, Rectangle2D rect,
			CanvasDir dir) {
		CanvasDir newDir = null;
		if (dir == CanvasDir.TOP || dir == CanvasDir.BOTTOM) {
			if (Math.abs(drag.getY() - rect.getY()) < THRESHOLD)
				newDir = CanvasDir.TOP;
			else
				newDir = CanvasDir.BOTTOM;
		} else {
			if (Math.abs(drag.getX() - rect.getX()) < THRESHOLD)
				newDir = CanvasDir.LEFT;
			else
				newDir = CanvasDir.RIGHT;
		}
		return newDir;
	}


	private Point2D computeFixedPoint(CanvasDir dir) {
		if (dir == CanvasDir.TOP_LEFT || dir == CanvasDir.LEFT
				|| dir == CanvasDir.TOP) {
			// Bottom right
			return new Point2D.Double(origTopLeft.getX() + origWidth,
					origTopLeft.getY() + origHeight);
		} else if (dir == CanvasDir.BOTTOM_LEFT) {
			// top right
			return new Point2D.Double(origTopLeft.getX() + origWidth,
					origTopLeft.getY());
		} else if (dir == CanvasDir.TOP_RIGHT) {
			// bottom left
			return new Point2D.Double(origTopLeft.getX(), origTopLeft.getY()
					+ origHeight);
		} else if (dir == CanvasDir.BOTTOM_RIGHT || dir == CanvasDir.RIGHT
				|| dir == CanvasDir.BOTTOM) {
			return origTopLeft;
		}
		return null;
	}

	Rectangle2D computedRect = new Rectangle2D.Double();
	public Rectangle2D computeRect(Point2D topCorner, Point2D bottomCorner) {
		// Swap corners so topCorner has smaller y value than bottom
		if (topCorner.getY() > bottomCorner.getY()) {
			Point2D temp = topCorner;
			topCorner = bottomCorner;
			bottomCorner = temp;
		}

		double[] newSize = diffPoint(topCorner, bottomCorner);
		constrainNewSize(newSize);

		// (x,y) is coordinate of top left. Initially, assume that
		// topCorner is the top left corner
		double x = topCorner.getX();
		double y = topCorner.getY();

		// If (x,y) is top right corner, then recompute x and y
		if (topCorner.getX() > bottomCorner.getX()) {
			// Case 2 top corner is right of bottom corner
			x = bottomCorner.getX();
			y = topCorner.getY();
		}
		computedRect.setRect(x, y, newSize[0], newSize[1]);
		return computedRect;
	}

	public Rectangle2D computeRect(Point2D corner, CanvasDir dir) {
		assert isEdge(dir);
		if (dir == CanvasDir.TOP) {
			Point2D lowerRight = new Point2D.Double(origTopLeft.getX()
					+ origWidth, origTopLeft.getY() + origHeight);
			// Depending on y, this might not be upperLeft, but that's OK
			Point2D upperLeft = new Point2D.Double(origTopLeft.getX(), corner
					.getY());
			return computeRect(lowerRight, upperLeft);
		} else if (dir == CanvasDir.BOTTOM) {
			Point2D upperLeft = new Point2D.Double(origTopLeft.getX(),
					origTopLeft.getY());
			// Depending on y, this might not be lowerRight, but that's OK
			Point2D lowerRight = new Point2D.Double(origTopLeft.getX()
					+ origWidth, corner.getY());
			return computeRect(lowerRight, upperLeft);
		} else if (dir == CanvasDir.RIGHT) {
			Point2D upperLeft = new Point2D.Double(origTopLeft.getX(),
					origTopLeft.getY());
			// Depending on y, this might not be lowerRight, but that's OK
			Point2D lowerRight = new Point2D.Double(corner.getX(), origTopLeft
					.getY()
					+ origHeight);
			return computeRect(lowerRight, upperLeft);
		} else if (dir == CanvasDir.LEFT) {
			Point2D lowerRight = new Point2D.Double(origTopLeft.getX()
					+ origWidth, origTopLeft.getY() + origHeight);
			// Depending on y, this might not be upperLeft, but that's OK
			Point2D upperLeft = new Point2D.Double(corner.getX(), origTopLeft
					.getY());
			return computeRect(lowerRight, upperLeft);
		}
		// Shouldn't reach here
		return null;
	}

	private void modifyRect(Rectangle2D rect, Point2D fixedPoint, Point2D drag) {
		// Correct for height
		double diffY = diffY(drag, fixedPoint);

		if (diffY < MIN_HEIGHT) {
			if (isBelow(drag, fixedPoint)) {
				rect.setRect(rect.getX(), rect.getY(), rect.getWidth(),
						MIN_HEIGHT);
			} else // drag is above fixed point
			{
				double minDiff = MIN_HEIGHT - diffY;
				rect.setRect(rect.getX(), rect.getY() - minDiff, rect
						.getWidth(), MIN_HEIGHT);
			}
		}
		// Correct for width
		double diffX = diffX(drag, fixedPoint);

		if (diffX < MIN_WIDTH) {
			if (isRight(drag, fixedPoint)) {
				rect.setRect(rect.getX(), rect.getY(), MIN_WIDTH, rect
						.getHeight());
			} else { // drag is left of fixed point
				double minDiff = MIN_WIDTH - diffX;
				rect.setRect(rect.getX() - minDiff, rect.getY(), MIN_WIDTH,
						rect.getHeight());
			}
		}
	}

	public static int locatePointInRect(Point2D point, Rectangle2D rect) {
		if (point.distance(getRectCorner(rect, UPPER_LEFT)) < 0.001)
			return UPPER_LEFT;
		else if (point.distance(getRectCorner(rect, UPPER_LEFT)) < 0.001)
			return UPPER_RIGHT;
		else if (point.distance(getRectCorner(rect, UPPER_LEFT)) < 0.001)
			return LOWER_LEFT;
		else if (point.distance(getRectCorner(rect, UPPER_LEFT)) < 0.001)
			return LOWER_RIGHT;
		return -1; // SHOULD NOT REACH HERE
	}

	public void modifyRect(Rectangle2D rect, Point2D corner, CanvasDir dir) {
		assert isEdge(dir);
		if (dir == CanvasDir.TOP) {
			Point2D lowerRight = new Point2D.Double(origTopLeft.getX()
					+ origWidth, origTopLeft.getY() + origHeight);
			// Depending on y, this might not be upperLeft, but that's OK
			Point2D upperLeft = new Point2D.Double(origTopLeft.getX(), corner
					.getY());
			modifyRect(rect, lowerRight, upperLeft);
		} else if (dir == CanvasDir.BOTTOM) {
			Point2D upperLeft = new Point2D.Double(origTopLeft.getX(),
					origTopLeft.getY());
			// Depending on y, this might not be lowerRight, but that's OK
			Point2D lowerRight = new Point2D.Double(origTopLeft.getX()
					+ origWidth, corner.getY());
			modifyRect(rect, upperLeft, lowerRight);
		} else if (dir == CanvasDir.RIGHT) {
			Point2D upperLeft = new Point2D.Double(origTopLeft.getX(),
					origTopLeft.getY());
			// Depending on y, this might not be lowerRight, but that's OK
			Point2D lowerRight = new Point2D.Double(corner.getX(), origTopLeft
					.getY()
					+ origHeight);
			modifyRect(rect, upperLeft, lowerRight);
		} else if (dir == CanvasDir.LEFT) {
			Point2D lowerRight = new Point2D.Double(origTopLeft.getX()
					+ origWidth, origTopLeft.getY() + origHeight);
			// Depending on y, this might not be upperLeft, but that's OK
			Point2D upperLeft = new Point2D.Double(corner.getX(), origTopLeft
					.getY());
			modifyRect(rect, lowerRight, upperLeft);
		}
		// Shouldn't reach here
	}

	private static double diffX(Point2D first, Point2D second) {
		return Math.abs(first.getX() - second.getX());
	}

	private static double diffY(Point2D first, Point2D second) {
		return Math.abs(first.getY() - second.getY());
	}
	private static double[] diffPoint(Point2D first, Point2D second) {
		return new double[] {diffX(first,second), diffY(first,second)};
	}
	/**
	 * 
	 * @param first
	 * @param second
	 * @return true if first point lies below second (i.e., has greater y value)
	 */
	private static boolean isBelow(Point2D first, Point2D second) {
		return first.getY() >= second.getY();
	}
	/**
	 * 
	 * @param first
	 * @param second
	 * @return true if first point lies below second (i.e., has smaller y value)
	 */
	private static boolean isAbove(Point2D first, Point2D second) {
		return first.getY() <= second.getY();
	}
	/**
	 * 
	 * @param first
	 * @param second
	 * @return true if first point lies below second (i.e., has greater y value
	 */
	private static boolean isLeft(Point2D first, Point2D second) {
		return first.getX() <= second.getX();
	}

	/**
	 * 
	 * @param first
	 * @param second
	 * @return true if first point lies below second (i.e., has greater y value
	 */
	private static boolean isRight(Point2D first, Point2D second) {
		return first.getX() >= second.getX();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.CanvasEditor#inRangeOfInterest(java.awt.geom.Point2D)
	 */
	public boolean inRangeOfInterest(Point2D point) {
		return findDirection(point) != CanvasDir.NONE;
	}

}
