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

package edu.umd.cfar.lamp.viper.gui.data.polygon;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 */
public class PolygonCanvasEditor extends CanvasEditor {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	private boolean resizeMode;

	PPath poly; // local reference to base class

	// origPolyPts are the points prior to pressing the mouse
	// It's a copy of polyPts. Updates to polyPoints are made
	// relative to origPolyPts
	Point2D[] polyPts, origPolyPts;

	PolygonNode polyNode;

	public PolygonCanvasEditor(Attributable attrIn) {
		// *********************************
		super(attrIn);
		polyNode = (PolygonNode) attrIn;
		poly = (PPath) attrIn;
		logger.fine("Created POLYGON EDITOR");

		// Local copy of information stored in oboxNode
		polyPts = polyNode.getPolyPts();
		// Make a second copy
		origPolyPts = new Point2D[polyPts.length];
		for (int i = 0; i < origPolyPts.length; i++) {
			origPolyPts[i] = new Point2D.Double(polyPts[i].getX(), polyPts[i]
					.getY());
		}
	}

	public String getName() {
		return "PolygonEditor";
	}

	int selectionMode = NONE;

	public static final int VERTEX = 1;

	public static final int EDGE = 2;

	public static final int INTERIOR = 3;

	public static final int INSERT_VERTEX = 4;

	public static final int DELETE_VERTEX = 5;

	public static final int RESIZE = 6;

	public static final int NONE = 7;
	
	public static final String[] SELECTION_MODE_NAMES = new String[] {"<select a mode>",
		"vertex", "edge", "interior", "insertVertex", "deleteVertex", "resize", "none"
	};
	

	Point2D pressPoint, dragPoint;

	final int CORNER_THRESHOLD = 8;

	final int LINE_THRESHOLD = 5;

	final int EDGE_THRESHOLD = 8;

	final int INTERIOR_THRESHOLD = 3;

	// List of points to be moved
	ArrayList movePts = new ArrayList();

	int findNearest(Point2D select) {
		double minDist = Double.MAX_VALUE;
		movePts.clear();

		// Find nearest point
		for (int i = 0; i < polyPts.length - 1; i++) {
			double currDist = select.distance(polyPts[i]);
			if (contains(select) && currDist < INTERIOR_THRESHOLD) {
				movePts.add(new Integer(i));
				return VERTEX;
			}
			if (currDist < CORNER_THRESHOLD) {
				movePts.add(new Integer(i));
				return VERTEX;
			}
		}

		// If no nearest point, find nearest edge
		minDist = Double.MAX_VALUE;
		for (int i = 0; i < polyPts.length - 1; i++) {
			Line2D line = new Line2D.Double(polyPts[i], polyPts[i + 1]);
			double currDist = line.ptSegDist(select);
			if (contains(select) && currDist < INTERIOR_THRESHOLD) {
				movePts.add(new Integer(i));
				movePts.add(new Integer((i + 1) % polyPts.length));
				return EDGE;
			}
			if (currDist < EDGE_THRESHOLD) {
				movePts.add(new Integer(i));
				movePts.add(new Integer((i + 1) % polyPts.length));
				return EDGE;
			}
		}
		// Check if it's inside the polygon
		if (contains(select))
			return INTERIOR;

		// Otherwise, it's "far away"
		return NONE;
	}

	public boolean contains(Point2D select) {
		Polygon poly = makePolygon();

		return poly.contains(select);
	}

	private Polygon makePolygon() {
		Polygon poly = new Polygon();
		for (int i = 0; i < polyPts.length; i++)
			poly.addPoint((int) polyPts[i].getX(), (int) polyPts[i].getY());
		return poly;
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
	//	boolean isRightOf( Point2D first, Point2D second, Point2D select )
	//	{
	//		Line2D edge = new Line2D.Double( first, second ) ;
	//		return edge.relativeCCW( select ) == -1 ;
	//	}
	public void keyPressed(PInputEvent e) {

	}

	boolean isNearHandle = false;

	CanvasDir currDir = CanvasDir.NONE;

	private void setSelectionMode(int selectionMode) {
		this.selectionMode = selectionMode;
	}
	
	public void mouseMoved(PInputEvent e) {
		boolean shiftDown = 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK);
		boolean altDown = 0 != (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK);

		boolean addRemoveMode = shiftDown;
		resizeMode = altDown;

		if (resizeMode) {
			handleResize(e);
			return;
		}

		int hoverMode = findNearest(e.getPosition());
		if (addRemoveMode && hoverMode == EDGE) {
			setSelectionMode(INSERT_VERTEX);
		} else if (addRemoveMode && hoverMode == VERTEX) {
			setSelectionMode(DELETE_VERTEX);
		} else {
			setSelectionMode(hoverMode);
		}
		if (selectionMode != INTERIOR) {
			refresh();
		}

		if (selectionMode == INTERIOR) {
			polyNode.unbold();
			polyNode.setDisplayProperties(HighlightSingleton.STYLE_HOVER);
		} else if (selectionMode == VERTEX) {
			int cornerIndex = ((Integer) movePts.get(0)).intValue();
			polyNode.boldVertex(cornerIndex);
		} else if (selectionMode == EDGE) {
			int cornerIndex = ((Integer) movePts.get(0)).intValue();
			polyNode.boldEdge(cornerIndex);
		} else if (selectionMode == INSERT_VERTEX) {
			polyNode.boldCircle(e.getPosition());
		} else if (canDeleteVertex()) {
			int cornerIndex = ((Integer) movePts.get(0)).intValue();
			polyNode.boldCrossEdge(cornerIndex);
		} else {
			setSelectionMode(NONE);
			polyNode.unbold();
		}
	}

	Rectangle2D scaleRect = null;

	Point2D origLowerRight = null;

	void handleResize(PInputEvent e) {
		Polygon poly = makePolygon();
		scaleRect = poly.getBounds2D();
		origLowerRight = getLowerRightCorner(scaleRect);
		Point2D direction = nearCorners(e.getPosition(), scaleRect);
		if (direction == null)
			polyNode.boldRect(scaleRect);
		else
			polyNode.boldRectAndCircle(scaleRect, direction);

	}

	private Point2D getLowerRightCorner(Rectangle2D rect) {
		double lowerRightX = rect.getX() + rect.getWidth();
		double lowerRightY = rect.getY() + rect.getHeight();
		return new Point2D.Double(lowerRightX, lowerRightY);
	}

	/**
	 * 
	 * @param point2D
	 * @param rect
	 * @return
	 */

	private Point2D nearCorners(Point2D point2D, Rectangle2D rect) {
		int x = (int) rect.getX(), y = (int) rect.getY();
		int w = (int) rect.getWidth(), h = (int) rect.getHeight();
		Point2D topLeft = new Point2D.Double(x, y);
		Point2D topRight = new Point2D.Double(x + w, y);
		Point2D bottomLeft = new Point2D.Double(x, y + h);
		Point2D bottomRight = new Point2D.Double(x + w, y + h);

		if (topLeft.distance(point2D) < CORNER_THRESHOLD)
			return topLeft;
		if (topRight.distance(point2D) < CORNER_THRESHOLD)
			return topRight;
		if (bottomLeft.distance(point2D) < CORNER_THRESHOLD)
			return bottomLeft;
		if (bottomRight.distance(point2D) < CORNER_THRESHOLD)
			return bottomRight;
		return null;
	}

	private CanvasDir getDirection(Point2D point2D, Rectangle2D rect) {
		int x = (int) rect.getX(), y = (int) rect.getY();
		int w = (int) rect.getWidth(), h = (int) rect.getHeight();
		Point2D topLeft = new Point2D.Double(x, y);
		Point2D topRight = new Point2D.Double(x + w, y);
		Point2D bottomLeft = new Point2D.Double(x, y + h);
		Point2D bottomRight = new Point2D.Double(x + w, y + h);

		if (topLeft.distance(point2D) < CORNER_THRESHOLD)
			return CanvasDir.TOP_LEFT;
		if (topRight.distance(point2D) < CORNER_THRESHOLD)
			return CanvasDir.TOP_RIGHT;
		if (bottomLeft.distance(point2D) < CORNER_THRESHOLD)
			return CanvasDir.BOTTOM_LEFT;
		if (bottomRight.distance(point2D) < CORNER_THRESHOLD)
			return CanvasDir.BOTTOM_RIGHT;

		// Check if near side
		Line2D top = new Line2D.Double(topLeft, topRight);
		Line2D right = new Line2D.Double(topRight, bottomRight);
		Line2D left = new Line2D.Double(topLeft, bottomLeft);
		Line2D bottom = new Line2D.Double(bottomLeft, bottomRight);

		if (top.ptLineDist(point2D) < LINE_THRESHOLD)
			return CanvasDir.TOP;
		if (bottom.ptLineDist(point2D) < LINE_THRESHOLD)
			return CanvasDir.BOTTOM;
		if (left.ptLineDist(point2D) < LINE_THRESHOLD)
			return CanvasDir.LEFT;
		if (right.ptLineDist(point2D) < LINE_THRESHOLD)
			return CanvasDir.RIGHT;
		return CanvasDir.NONE;
	}

	public void doWhenUnselected() {

	}

	int zapIndex = -1; // vertex to delete

	boolean pushedCursor = false;

	Point2D scaleRectCenter = null;

	CanvasDir dirResize = CanvasDir.NONE;

	Rectangle2D scaleRectAtPressed = new Rectangle2D.Double();

	public void mousePressed(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		boolean shiftDown = 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK);
		boolean altDown = 0 != (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK);

		if (!leftClick) {
			return;
		}

		boolean addRemoveMode = shiftDown;
		resizeMode = altDown;
		dirResize = CanvasDir.NONE;

		pressPoint = e.getPosition();
		if (resizeMode) {
			dirResize = getDirection(pressPoint, scaleRect);
			if (dirResize == CanvasDir.NONE) {
				setSelectionMode(NONE);
			} else {
				setSelectionMode(RESIZE);
			}
			
			// Center of scaleRect
			scaleRectCenter = new Point2D.Double(scaleRect.getCenterX(),
					scaleRect.getCenterY());
			scaleRectAtPressed.setRect(scaleRect);
			return;
		}

		findNearest(pressPoint);
		if (addRemoveMode) {
			if (selectionMode == EDGE) {
				setSelectionMode(INSERT_VERTEX);
			} else if (selectionMode == VERTEX) {
				setSelectionMode(DELETE_VERTEX);
			}

			if (selectionMode == INSERT_VERTEX) {
				// Insert a new point
				int index = ((Integer) movePts.get(0)).intValue();
				addPointAfterIndex(index);
				// Make the newly added vertex the one that's selected
				movePts.clear();
				movePts.add(new Integer(index + 1));
				// Points have changed
				polyNode.setPolyPts(polyPts);
			} else if (canDeleteVertex()) {
				int index = ((Integer) movePts.get(0)).intValue();
				polyNode.boldCrossEdge(index);
				zapIndex = index;
				e.pushCursor(Cursor
						.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				pushedCursor = true;
			}
		}
	}

	private boolean canDeleteVertex() {
		return selectionMode == DELETE_VERTEX && polyNode.getNumVertices() > 3;
	}

	private void addPointAfterIndex(int index) {
		Point2D[] newPolyPts = new Point2D[polyPts.length + 1];
		for (int i = 0; i <= index; i++)
			newPolyPts[i] = polyPts[i];
		newPolyPts[index + 1] = (Point2D) pressPoint.clone();
		for (int i = index + 2; i < newPolyPts.length; i++)
			newPolyPts[i] = polyPts[i - 1];
		polyPts = newPolyPts;

		// Make new copy of origPolyPts
		origPolyPts = new Point2D[polyPts.length];
		for (int i = 0; i < polyPts.length; i++) {
			origPolyPts[i] = (Point2D) polyPts[i].clone();
		}
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		boolean shiftDown = 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK);
		boolean altDown = 0 != (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK);

		if (!leftClick) {
			return;
		}
		dragPoint = e.getPosition();
		resizeMode = altDown;
		if (selectionMode == DELETE_VERTEX) {
			highlightForDelete(e);
		} else if (resizeMode) {
			resizeScaleRect(dragPoint);
		} else
			updatePolygon();
	}

	private void highlightForDelete(PInputEvent e) {
		if (vertexWithinRange(dragPoint, zapIndex)) {
			polyNode.boldCrossEdge(zapIndex);
			if (!pushedCursor) {
				e.pushCursor(Cursor
						.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				pushedCursor = true;
			}
		} else // out of range
		{
			if (pushedCursor)
				e.popCursor();
			pushedCursor = false;
			polyNode.boldVertex(zapIndex);
		}
	}

	boolean vertexWithinRange(Point2D select, int index) {
		double currDist = select.distance(polyPts[index]);
		if (contains(select) && currDist < INTERIOR_THRESHOLD)
			return true;

		if (currDist < CORNER_THRESHOLD)
			return true;
		return false;
	}

	private static AffineTransform getTransformForDrag(Point2D centroid,
			Point2D grabbedPoint, Point2D endPoint) {
		AffineTransform transFrom = AffineTransform.getTranslateInstance(
				-centroid.getX(), -centroid.getY());
		grabbedPoint = transFrom.transform(grabbedPoint, null);
		endPoint = transFrom.transform(endPoint, null);
		AffineTransform scale = AffineTransform.getScaleInstance(endPoint
				.getX()
				/ grabbedPoint.getX(), endPoint.getY() / grabbedPoint.getY());
		AffineTransform transBack = AffineTransform.getTranslateInstance(
				centroid.getX(), centroid.getY());
		scale.concatenate(transFrom);
		transBack.concatenate(scale);
		//	  System.out.println( "grabbed = " +
		//	  					  transBack.transform( grabbedPoint, null )
		//	  					  + " realgrab = " + grabbedPoint +
		//	  					  " realend = " + endPoint ) ;
		return transBack;
	}

	private void resetSelect(Point2D select) {
		int centerX = (int) scaleRectCenter.getX();
		int centerY = (int) scaleRectCenter.getY();
		int diffX = (int) (select.getX() - centerX);
		int diffY = (int) (select.getY() - centerY);
		int absDiffX = Math.abs(diffX);
		int absDiffY = Math.abs(diffY);
		int origDiffX = Math.abs((int) (origLowerRight.getX() - centerX));
		int origDiffY = Math.abs((int) (origLowerRight.getY() - centerY));
		if (dirResize == CanvasDir.TOP_LEFT || dirResize == CanvasDir.TOP_RIGHT
				|| dirResize == CanvasDir.BOTTOM_LEFT
				|| dirResize == CanvasDir.BOTTOM_RIGHT) {
			select.setLocation(centerX + absDiffX, centerY + absDiffY);
		} else if (dirResize == CanvasDir.TOP || dirResize == CanvasDir.BOTTOM) {
			select.setLocation(centerX + origDiffX, centerY + absDiffY);
		} else if (dirResize == CanvasDir.LEFT || dirResize == CanvasDir.RIGHT) {
			select.setLocation(centerX + absDiffX, centerY + origDiffY);
		}
	}

	/**
	 *  
	 */
	int oldDiffX, oldDiffY;

	private void resizeScaleRect(Point2D select) {
		resetSelect(select);
		// TODO Auto-generated method stub
		int centerX = (int) scaleRectCenter.getX();
		int centerY = (int) scaleRectCenter.getY();
		int diffX = (int) (select.getX() - centerX);
		int diffY = (int) (select.getY() - centerY);
		//		
		//		int absDiffX = redoDiffX( diffX ) ;
		//		int absDiffY = redoDiffY( diffY ) ;

		scaleRect.setRect(centerX - diffX, centerY - diffY, 2 * diffX,
				2 * diffY);

		int topLeftX = (int) scaleRect.getX();
		int topLeftY = (int) scaleRect.getY();
		int width = (int) scaleRect.getWidth();
		int height = (int) scaleRect.getHeight();

		Point2D circ = null;
		boolean atCorners = true;
		if (dirResize == CanvasDir.BOTTOM_RIGHT)
			circ = new Point2D.Double(topLeftX + width, topLeftY + height);
		else if (dirResize == CanvasDir.BOTTOM_LEFT)
			circ = new Point2D.Double(topLeftX, topLeftY + height);
		else if (dirResize == CanvasDir.TOP_RIGHT)
			circ = new Point2D.Double(topLeftX + width, topLeftY);
		else if (dirResize == CanvasDir.TOP_LEFT)
			circ = new Point2D.Double(topLeftX, topLeftY);
		else // TODO Allow adjustment of edges
		{
			atCorners = false;
		}

		if (atCorners)
			polyNode.boldRectAndCircle(scaleRect, circ);
		else
			polyNode.boldRect(scaleRect);

		Point2D currLowerRight = getLowerRightCorner(scaleRect);
		AffineTransform T = getTransformForDrag(scaleRectCenter,
				origLowerRight, currLowerRight);
		copyOrigToPoly();
		// Start at 1 since index 0 and index polyPts.length - 1 refer
		// to the same point, and we want to apply the transform once to
		// each point on the polygon.
		for (int i = 1; i < polyPts.length; i++) {
			Point2D p = new Point2D.Double(polyPts[i].getX(), polyPts[i].getY());
			T.transform(p, p);
			polyPts[i].setLocation(p);
		}
		//  		fixPolygon( scaleRectCenter, origLowerRight, currLowerRight ) ;
		poly.setPathToPolyline(polyPts);
	}

	/**
	 *  
	 */
	private void copyOrigToPoly() {
		for (int i = 0; i < origPolyPts.length; i++) {
			polyPts[i].setLocation(origPolyPts[i]);
		}
	}

	void fixPolygon(Point2D center, Point2D orig, Point2D curr) {
		Point2D[] copy = makeCopy();
		shiftPolygon(copy, -center.getX(), -center.getY());
		scalePolygon(copy, center, orig, curr);
		shiftPolygon(copy, center.getX(), center.getY());
		for (int i = 0; i < polyPts.length; i++) {
			polyPts[i].setLocation(copy[i]);
		}
	}

	Point2D[] makeCopy() {
		Point2D[] copy = new Point2D.Double[origPolyPts.length];
		for (int i = 0; i < origPolyPts.length; i++) {
			copy[i] = new Point2D.Double(origPolyPts[i].getX(), origPolyPts[i]
					.getY());
		}
		return copy;
	}

	/**
	 * @param copy
	 * @param d
	 * @param e
	 */
	private void shiftPolygon(Point2D[] copy, double d, double e) {
		// TODO Auto-generated method stub
		for (int i = 0; i < copy.length; i++) {
			double x = copy[i].getX();
			double y = copy[i].getY();
			copy[i].setLocation(x + d, y + e);
		}
	}

	/**
	 * @param copy
	 * @param orig
	 * @param curr
	 */
	private void scalePolygon(Point2D[] copy, Point2D center, Point2D orig,
			Point2D curr) {
		// TODO Auto-generated method stub
		double centerX = center.getX();
		double centerY = center.getY();
		double origX = orig.getX() - centerX;
		double origY = orig.getY() - centerY;
		double currX = curr.getX() - centerX;
		double currY = curr.getY() - centerY;
		double xFactor = currX / origX;
		double yFactor = currY / origY;
		// Scale here
		for (int i = 0; i < copy.length; i++) {
			Point2D point = copy[i];
			double newX = point.getX() * xFactor;
			double newY = point.getY() * yFactor;
			point.setLocation(newX, newY);
		}
	}

	/**
	 * @param diffX
	 * @return
	 */
	private int redoDiffX(int diffX) {
		// TODO Auto-generated method stub
		int absDiffX = Math.abs(diffX);
		if (absDiffX < getMinResizeWidth())
			return getMinResizeWidth();
		else
			return absDiffX;
		//		if ( dirResize == CanvasDir.BOTTOM_RIGHT && diffX > 0 )
		//			 return absDiffX ;
		//		if ( dirResize == CanvasDir.BOTTOM_LEFT && diffX < 0 )
		//			 return absDiffX ;
		//		if ( dirResize == CanvasDir.TOP_RIGHT && diffX > 0 )
		//			 return absDiffX ;
		//		if ( dirResize == CanvasDir.TOP_LEFT && diffX < 0 )
		//			 return absDiffX ;
		//		
		//		return getMinResizeWidth() ;
	}

	/**
	 * @param diffY
	 * @return
	 */
	private int redoDiffY(int diffY) {
		// TODO Auto-generated method stub
		int absDiffY = Math.abs(diffY);
		if (absDiffY < getMinResizeHeight())
			return getMinResizeHeight();
		else
			return absDiffY;

		//		if ( dirResize == CanvasDir.BOTTOM_RIGHT && diffY > 0 )
		//			 return absDiffY ;
		//		if ( dirResize == CanvasDir.BOTTOM_LEFT && diffY > 0 )
		//			 return absDiffY ;
		//		if ( dirResize == CanvasDir.TOP_RIGHT && diffY < 0 )
		//			 return absDiffY ;
		//		if ( dirResize == CanvasDir.TOP_LEFT && diffY < 0 )
		//			 return absDiffY ;
		//		
		//		return getMinResizeHeight() ;
	}

	/**
	 * @return
	 */
	private int getMinResizeHeight() {
		// TODO Auto-generated method stub
		return 10;
	}

	/**
	 * @return
	 */
	private int getMinResizeWidth() {
		// TODO Auto-generated method stub
		return 10;
	}

	/**
	 *  
	 */
	private void updatePolygon() {
		// TODO Auto-generated method stub
		double diffX = dragPoint.getX() - pressPoint.getX();
		double diffY = dragPoint.getY() - pressPoint.getY();
		// Code to move corner point
		if (selectionMode == VERTEX || selectionMode == EDGE
				|| selectionMode == INSERT_VERTEX) {
			// Code to move edge or move a vertex
			shiftPoints(diffX, diffY);
			int cornerIndex = ((Integer) movePts.get(0)).intValue();
			if (selectionMode == VERTEX)
				polyNode.boldVertex(cornerIndex);
			else if (selectionMode == INSERT_VERTEX)
				polyNode.boldCrossEdgeAndVertex(cornerIndex);
			else
				// EDGE
				polyNode.boldEdge(cornerIndex);
		}
		// Code to shift entire polygon
		else {
			shiftAllPoints(diffX, diffY);
		}
	}

	// TODO Should keep the original polygon around in a copy
	// and update as needed
	void shiftPoints(double diffX, double diffY) {
		for (int i = 0; i < movePts.size(); i++) {
			int index = ((Integer) movePts.get(i)).intValue();
			double origX = origPolyPts[index].getX();
			double origY = origPolyPts[index].getY();
			polyPts[index].setLocation(origX + diffX, origY + diffY);
		}
		// set PPath appropriately
		poly.setPathToPolyline(polyPts);
	}

	void shiftAllPoints(double diffX, double diffY) {
		for (int i = 0; i < origPolyPts.length; i++) {
			double origX = origPolyPts[i].getX();
			double origY = origPolyPts[i].getY();
			polyPts[i].setLocation(origX + diffX, origY + diffY);
		}
		// set PPath appropriately
		poly.setPathToPolyline(polyPts);
	}

	private void removeVertex(int index) {
		Point2D[] newPolyPts = new Point2D[polyPts.length - 1];
		for (int i = 0; i < index; i++)
			newPolyPts[i] = polyPts[i];
		for (int i = index; i < newPolyPts.length; i++)
			newPolyPts[i] = polyPts[i + 1];
		// Make sure last point and first point are same
		newPolyPts[newPolyPts.length - 1] = newPolyPts[0];
		polyPts = newPolyPts;

		// Make new copy of origPolyPts
		origPolyPts = new Point2D[polyPts.length];
		for (int i = 0; i < polyPts.length; i++) {
			origPolyPts[i] = (Point2D) polyPts[i].clone();
		}
	}

	public void mouseReleased(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);

		if (leftClick || selectionMode == NONE) {
			// left mouse button still down or no action being performed 
			return;
		}
		
		dragPoint = e.getPosition();
		if (canDeleteVertex()) {
			if (vertexWithinRange(dragPoint, zapIndex)) {
				removeVertex(zapIndex);
				polyNode.setPolyPts(polyPts);
			}
			if (pushedCursor) {
				e.popCursor();
			}
			pushedCursor = false;
		} else if (selectionMode == RESIZE) {
			resizeScaleRect(dragPoint);
		} else {
			updatePolygon();
			takeSnapshot();
		}
		setSelectionMode(NONE);
	}

	/**
	 * Copies the polyPts to origPolyPts Code updates points relative to
	 * origPolyPts
	 */
	void takeSnapshot() {
		for (int i = 0; i < origPolyPts.length; i++) {
			origPolyPts[i].setLocation(polyPts[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Selectable#minDist(java.awt.geom.Point2D)
	 */
	public double minDist(Point2D selectPt) {
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < polyPts.length - 1; i++) {
			Line2D line = new Line2D.Double(polyPts[i], polyPts[i + 1]);
			double currDist = line.ptSegDist(selectPt);
			if (currDist < minDist)
				minDist = currDist;
		}
		return minDist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.CanvasEditor#inRangeOfInterest(java.awt.geom.Point2D)
	 */
	public boolean inRangeOfInterest(Point2D point) {
		return findNearest(point) != NONE
				|| (resizeMode && (scaleRect.contains(point) || nearCorners(
						point, scaleRect) != null));
	}

}