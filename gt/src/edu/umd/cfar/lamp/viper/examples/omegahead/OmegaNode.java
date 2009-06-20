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

package edu.umd.cfar.lamp.viper.examples.omegahead;

import java.awt.*;
import java.awt.geom.*;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * Displays an 'omega' shape on the image. Right now, this is just a circle
 * above a line.
 *  
 */
public class OmegaNode extends AttributablePPathAdapter implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	private static final double HANDLE_LENGTH = 20;
	
	static boolean isSouthDirectionSizeHandle(int hover) {
		return hover == HIGHLIGHT.SOUTHEAST_SIZE_HANDLE || hover == HIGHLIGHT.SOUTH_SIZE_HANDLE || hover == HIGHLIGHT.SOUTHWEST_SIZE_HANDLE;
	}
	static boolean isEastDirectionSizeHandle(int hover) {
		return hover == HIGHLIGHT.SOUTHEAST_SIZE_HANDLE || hover == HIGHLIGHT.EAST_SIZE_HANDLE || hover == HIGHLIGHT.NORTHEAST_SIZE_HANDLE;
	}
	static boolean isNorthDirectionSizeHandle(int hover) {
		return hover == HIGHLIGHT.NORTHEAST_SIZE_HANDLE || hover == HIGHLIGHT.NORTH_SIZE_HANDLE || hover == HIGHLIGHT.NORTHWEST_SIZE_HANDLE;
	}
	static boolean isWestDirectionSizeHandle(int hover) {
		return hover == HIGHLIGHT.SOUTHWEST_SIZE_HANDLE || hover == HIGHLIGHT.WEST_SIZE_HANDLE || hover == HIGHLIGHT.NORTHWEST_SIZE_HANDLE;
	}
	static int flipNorthSouth(int hover) {
		switch (hover) {
		case HIGHLIGHT.NORTH_ORIENTATION_HANDLE:
			return HIGHLIGHT.SOUTH_ORIENTATION_HANDLE;
		case HIGHLIGHT.SOUTH_ORIENTATION_HANDLE:
			return HIGHLIGHT.NORTH_ORIENTATION_HANDLE;
		case HIGHLIGHT.NORTHEAST_SIZE_HANDLE:
			return HIGHLIGHT.SOUTHEAST_SIZE_HANDLE;
		case HIGHLIGHT.NORTH_SIZE_HANDLE:
			return HIGHLIGHT.SOUTH_SIZE_HANDLE;
		case HIGHLIGHT.NORTHWEST_SIZE_HANDLE:
			return HIGHLIGHT.SOUTHWEST_SIZE_HANDLE;
		case HIGHLIGHT.SOUTHEAST_SIZE_HANDLE:
			return HIGHLIGHT.NORTHEAST_SIZE_HANDLE;
		case HIGHLIGHT.SOUTH_SIZE_HANDLE:
			return HIGHLIGHT.NORTH_SIZE_HANDLE;
		case HIGHLIGHT.SOUTHWEST_SIZE_HANDLE:
			return HIGHLIGHT.NORTHWEST_SIZE_HANDLE;
		}
		return hover;
	}
	static int flipEastWest(int hover) {
		switch (hover) {
		case HIGHLIGHT.LEFT_POINT:
			return HIGHLIGHT.RIGHT_POINT;
		case HIGHLIGHT.RIGHT_POINT:
			return HIGHLIGHT.LEFT_POINT;
		case HIGHLIGHT.NORTHEAST_SIZE_HANDLE:
			return HIGHLIGHT.NORTHWEST_SIZE_HANDLE;
		case HIGHLIGHT.EAST_SIZE_HANDLE:
			return HIGHLIGHT.WEST_SIZE_HANDLE;
		case HIGHLIGHT.NORTHWEST_SIZE_HANDLE:
			return HIGHLIGHT.NORTHEAST_SIZE_HANDLE;
		case HIGHLIGHT.SOUTHEAST_SIZE_HANDLE:
			return HIGHLIGHT.SOUTHWEST_SIZE_HANDLE;
		case HIGHLIGHT.WEST_SIZE_HANDLE:
			return HIGHLIGHT.EAST_SIZE_HANDLE;
		case HIGHLIGHT.SOUTHWEST_SIZE_HANDLE:
			return HIGHLIGHT.SOUTHEAST_SIZE_HANDLE;
		}
		return hover;
	}

	private static Shape EMPTY_SHAPE = new GeneralPath();

	/** Enumeration for the type of highlight currently applied */
	public static interface HIGHLIGHT {
		/** no highlight applied */
		public static int NONE = 0;

		/** the circle is to be lit. This adjusts location. */
		public static int RING = 1;

		/** the shoulder line is to be lit. This adjusts height / circle radius. */
		public static int LINE = 2;

		/**
		 * the left endpoint of the shoulder line is to be lit. This adjusts
		 * width.
		 */
		public static int LEFT_POINT = 3;

		/**
		 * the right endpoint of the shoulder line is to be lit. This adjusts
		 * width.
		 */
		public static int RIGHT_POINT = 4;

		/** the north handle is to be lit. This adjusts orientation. */
		public static int NORTH_ORIENTATION_HANDLE = 5;

		/** the north handle is to be lit. This adjusts orientation. */
		public static int SOUTH_ORIENTATION_HANDLE = 6;
		
		/** the north handle is to be lit. This adjusts major axis length. */
		public static int NORTH_SIZE_HANDLE = 8;

		/** the north handle is to be lit. This adjusts major axis length. */
		public static int NORTHEAST_SIZE_HANDLE = 9;

		/** the east handle is to be lit. This adjusts minor axis length. */
		public static int EAST_SIZE_HANDLE = 10;

		/** the south handle is to be lit. This adjusts major axis length. */
		public static int SOUTHEAST_SIZE_HANDLE = 11;
		/** the south handle is to be lit. This adjusts major axis length. */
		public static int SOUTH_SIZE_HANDLE = 12;
		/** the south handle is to be lit. This adjusts major axis length. */
		public static int SOUTHWEST_SIZE_HANDLE = 13;
		
		/** the west handle is to be lit. This adjusts minor axis length. */
		public static int WEST_SIZE_HANDLE = 14;
		/** the north handle is to be lit. This adjusts major axis length. */
		public static int NORTHWEST_SIZE_HANDLE = 15;
	}

	private OmegaHeadModel localCopy;

	private double x;

	private double y;
	
	private double shapeYShift;

	private double lineLength;

	private double lineOffset;

	private double majorDiameter;

	private double minorDiameter;

	private double angle; // in radians
	
	private double lineAngle; // in radians, with respect to the local line

	// Displayed shape
	private PPath shoulderLine;

	private PPath headCircle;

	private PPath northPt;

	private PPath southPt;

	private Line2D.Double line;

	private Shape circle;

	private Shape biggerCircle;

	// UI
	private Point2D northHandlePt;

	private Point2D southHandlePt;

	private Point2D eastHandlePt;

	private Point2D westHandlePt;

	private Point2D northeastHandlePt;

	private Point2D northwestHandlePt;

	private Point2D southeastHandlePt;

	private Point2D southwestHandlePt;

	public Point2D getEastCornerPt() {
		return eastCornerPt;
	}
	public Point2D getNortheastCornerPt() {
		return northeastCornerPt;
	}
	public Point2D getNorthwestCornerPt() {
		return northwestCornerPt;
	}
	public Point2D getSoutheastCornerPt() {
		return southeastCornerPt;
	}
	public Point2D getSouthwestCornerPt() {
		return southwestCornerPt;
	}
	public Point2D getWestCornerPt() {
		return westCornerPt;
	}
	private Point2D eastCornerPt;

	private Point2D westCornerPt;

	private Point2D northeastCornerPt;

	private Point2D northwestCornerPt;

	private Point2D southeastCornerPt;

	private Point2D southwestCornerPt;

	private PPath eastHandle = new PPath();

	private PPath westHandle = new PPath();

	private PPath northHandle = new PPath();

	private PPath southHandle = new PPath();

	private int cornerRadius = 5;

	private int selectionThreshold = 5;

	// For highlighting
	private PPath highlight = new PPath();

	private int currentHightlight = HIGHLIGHT.NONE;

	public OmegaNode() {
		this(null);
	}

	/**
	 *  
	 */
	public OmegaNode(ViperViewMediator mediator) {
		super(mediator);
		
		shoulderLine = new PPath();
		headCircle = new PPath();
		northPt = new PPath();
		southPt = new PPath();
		eastHandle = new PPath();
		westHandle = new PPath();
		northHandle = new PPath();
		southHandle = new PPath();
		PNode allHandles = new PNode();
		allHandles.addChild(northHandle);
		allHandles.addChild(eastHandle);
		allHandles.addChild(southHandle);
		allHandles.addChild(westHandle);
		allHandles.addChild(northPt);
		allHandles.addChild(southPt);
		

		//Stroke s = new PFixedWidthStroke();
		
		// Set handle as obox child
		addChild(shoulderLine);
		addChild(headCircle);
		addChild(allHandles);
		addChild(highlight);

		resetStyle();
	}

	/**
	 *  
	 */
	protected void resetStyle() {
		shoulderLine.setStroke(getDisplayProperties().getStroke());
		headCircle.setStroke(getDisplayProperties().getStroke());
		shoulderLine.setStrokePaint(getDisplayProperties().getStrokePaint());
		headCircle.setStrokePaint(getDisplayProperties().getStrokePaint());

		highlight.setStroke(getHighlightDisplayProperties().getStroke());
		highlight.setStrokePaint(getHighlightDisplayProperties()
				.getStrokePaint());
		
		PNode allHandles = northPt.getParent();
		for (int i = 0; i < allHandles.getChildrenCount(); i++) {
			PPath h = (PPath) allHandles.getChild(i);
			h.setStroke(getHandleDisplayProperties().getStroke());
			h.setStrokePaint(getHandleDisplayProperties().getStrokePaint());
		}
	}

	public void setShapeAngleInRadians(double angleIn) {
		angle = angleIn;
		updateOmega();
	}

	public void setShapeLineAngleInRadians(double angleIn) {
		lineAngle = angleIn;
		updateOmega();
	}

	public double getShapeAngleInRadians() {
		return angle;
	}
	
	public double getShapeLineAngleInRadians() {
		return lineAngle;
	}

	public void updateOmega() {
		updateOmegaPart();
		updateHandles();
	}

	private AffineTransform getTransformForRotate() {
		AffineTransform transFrom = AffineTransform
				.getTranslateInstance(-x, -y);
		AffineTransform rotate = AffineTransform.getRotateInstance(-angle);
		AffineTransform transBack = AffineTransform.getTranslateInstance(x, y);
		rotate.concatenate(transFrom);
		transBack.concatenate(rotate);
		return transBack;
	}
	
	private void updateOmegaPart() {
		// XXX this should be a dirty mechanism; this method is called too often
		// Basically, you have to add a dirty bit, and run the method if the
		// bit is set when 'paint' is called. So, when the bit is set, you
		// also have to call 'invalidate' on this node.
		updateOmegaEllipsePart();
		updateOmegaLinePart();
	}

	private void updateOmegaLinePart() {
		// compute the midpoint of the line segment wrt the ellipse centroid
		Point2D P = new Point2D.Double(lineOffset, shapeYShift + (majorDiameter / 2));
		Point2D midpoint = AffineTransform.getRotateInstance(-angle).transform(P, null);
		
		// compute the offset from the line segment midpoint
		P = new Point2D.Double(lineLength/2, 0);
		Point2D shift = AffineTransform.getRotateInstance(lineAngle - angle).transform(P, null);

		// Finally, sum the ellipse centroid, the shift to the
 		// line segment midpoint, and the shifts to the
		// ends of the lines.
		double x1 = x + midpoint.getX() + shift.getX();
		double x2 = x + midpoint.getX() - shift.getX();
		double y1 = y + midpoint.getY() + shift.getY();
		double y2 = y + midpoint.getY() - shift.getY();
		line = new Line2D.Double(x1, y1, x2, y2);
		shoulderLine.setPathTo(line);
	}
	
	private void updateOmegaEllipsePart() {
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		Ellipse2D c = new Ellipse2D.Double(-minorDiameter / 2,
				-majorDiameter / 2, minorDiameter, majorDiameter);
		Ellipse2D bigC = new Ellipse2D.Double(-selectionThreshold
				- minorDiameter / 2, -selectionThreshold - majorDiameter / 2,
				minorDiameter + selectionThreshold * 2, majorDiameter
						+ selectionThreshold * 2);
		AffineTransform shift = AffineTransform.getTranslateInstance(x, y);
		AffineTransform rotate = AffineTransform.getRotateInstance(-angle);
		shift.concatenate(rotate);
		circle = new GeneralPath();
		((GeneralPath) circle).append(c.getPathIterator(shift), false);
		headCircle.setPathTo(circle);
		biggerCircle = new GeneralPath();
		((GeneralPath) biggerCircle).append(bigC.getPathIterator(shift), false);
		double distance = (majorDiameter / 2) + HANDLE_LENGTH;
		// XXX shift to take into account line end points
		// - make sure the boundary doesn't overlap the line end points
		northHandlePt = new Point2D.Double(x - distance * sinA, y - distance
				* cosA);
		southHandlePt = new Point2D.Double(x + distance * sinA, y + distance
				* cosA);
		northPt.setPathTo(new Ellipse2D.Double(northHandlePt.getX() - .5,
				northHandlePt.getY() - .5, 1, 1));
		southPt.setPathTo(new Ellipse2D.Double(southHandlePt.getX() - .5,
				southHandlePt.getY() - .5, 1, 1));

		double xDistance = minorDiameter / 2;
		double yDistance = majorDiameter / 2;
		eastCornerPt = new Point2D.Double(x - xDistance * cosA, y + xDistance
				* sinA);
		westCornerPt = new Point2D.Double(x + xDistance * cosA, y - xDistance
				* sinA);

		northeastCornerPt = new Point2D.Double(eastCornerPt.getX() - yDistance
				* sinA, eastCornerPt.getY() - yDistance * cosA);
		southeastCornerPt = new Point2D.Double(eastCornerPt.getX() + yDistance
				* sinA, eastCornerPt.getY() + yDistance * cosA);
		northwestCornerPt = new Point2D.Double(westCornerPt.getX() - yDistance
				* sinA, westCornerPt.getY() - yDistance * cosA);
		southwestCornerPt = new Point2D.Double(westCornerPt.getX() + yDistance
				* sinA, westCornerPt.getY() + yDistance * cosA);

		double borderDistance = HANDLE_LENGTH / 2;

		xDistance += borderDistance;
		yDistance += borderDistance;
		eastHandlePt = new Point2D.Double(x - xDistance * cosA, y + xDistance
				* sinA);
		westHandlePt = new Point2D.Double(x + xDistance * cosA, y - xDistance
				* sinA);

		northeastHandlePt = new Point2D.Double(eastHandlePt.getX() - yDistance
				* sinA, eastHandlePt.getY() - yDistance * cosA);
		southeastHandlePt = new Point2D.Double(eastHandlePt.getX() + yDistance
				* sinA, eastHandlePt.getY() + yDistance * cosA);
		northwestHandlePt = new Point2D.Double(westHandlePt.getX() - yDistance
				* sinA, westHandlePt.getY() - yDistance * cosA);
		southwestHandlePt = new Point2D.Double(westHandlePt.getX() + yDistance
				* sinA, westHandlePt.getY() + yDistance * cosA);
		eastHandle.setPathTo(new Line2D.Double(southeastHandlePt,
				northeastHandlePt));
		westHandle.setPathTo(new Line2D.Double(northwestHandlePt,
				southwestHandlePt));
		northHandle.setPathTo(new Line2D.Double(northwestHandlePt,
				northeastHandlePt));
		southHandle.setPathTo(new Line2D.Double(southeastHandlePt,
				southwestHandlePt));
	}

	private void updateHandles() {
		int r = 8;
		Point2D c = null;
		switch (currentHightlight) {
		case HIGHLIGHT.RING:
			highlight.setPathTo(headCircle.getPathReference());
			break;
		case HIGHLIGHT.LINE:
			highlight.setPathTo(shoulderLine.getPathReference());
			break;
		case HIGHLIGHT.LEFT_POINT:
			c = getLeftPoint();
			break;
		case HIGHLIGHT.RIGHT_POINT:
			c = getRightPoint();
			break;
		case HIGHLIGHT.NORTH_ORIENTATION_HANDLE:
			// The north handle is used to adjust orientation
			// It should be HANDLE_LENGTH away from the top of the omega
			c = northHandlePt;
			break;
		case HIGHLIGHT.SOUTH_ORIENTATION_HANDLE:
			c = southHandlePt;
			break;
		case HIGHLIGHT.NORTH_SIZE_HANDLE:
			highlight.setPathTo(northHandle.getPathReference());
			break;
		case HIGHLIGHT.NORTHEAST_SIZE_HANDLE:
			c = northeastHandlePt;
			break;
		case HIGHLIGHT.EAST_SIZE_HANDLE:
			highlight.setPathTo(eastHandle.getPathReference());
			break;
		case HIGHLIGHT.SOUTHEAST_SIZE_HANDLE:
			c = southeastHandlePt;
			break;
		case HIGHLIGHT.SOUTH_SIZE_HANDLE:
			highlight.setPathTo(southHandle.getPathReference());
			break;
		case HIGHLIGHT.SOUTHWEST_SIZE_HANDLE:
			c = southwestHandlePt;
			break;
		case HIGHLIGHT.WEST_SIZE_HANDLE:
			highlight.setPathTo(westHandle.getPathReference());
			break;
		case HIGHLIGHT.NORTHWEST_SIZE_HANDLE:
			c = northwestHandlePt;
			break;
		case HIGHLIGHT.NONE:
		default:
			highlight.setPathTo(EMPTY_SHAPE);
		}
		if (c != null) {
			double x = c.getX();
			double y = c.getY();
			highlight
					.setPathTo(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
		}
	}

	public Point2D getNortheastHandlePt() {
		return northeastHandlePt;
	}
	public Point2D getNorthwestHandlePt() {
		return northwestHandlePt;
	}
	public Point2D getSoutheastHandlePt() {
		return southeastHandlePt;
	}
	public Point2D getSouthwestHandlePt() {
		return southwestHandlePt;
	}
	public Object getUpdatedAttribute() {
		int degrees = radians2degrees(angle);
		int lineDegrees = radians2degrees(lineAngle) % 180;
		if (mediator != null) {
			Instant now = mediator.getMajorMoment();
			// Get the oriented box corresponding to current frame
			OmegaHeadModel origBox = (OmegaHeadModel) attr
					.getAttrValueAtInstant(now);

			// To make sure tiny changes don't affect result
			if (origBox != null) {
				int setDegrees = origBox.getOrientation();
				if (Math.abs(degrees - setDegrees) < 1.2)
					degrees = origBox.getOrientation();
			}
		}

		OmegaHeadModel newBox = new OmegaHeadModel(new Pnt((int) x, (int) y),
				(int) lineLength, (int) lineOffset, (int) majorDiameter,
				(int) minorDiameter, (int) shapeYShift, degrees, lineDegrees);
		return newBox;
	}
	
	/**
	 * Converts from unconstrained radians to degrees within [0, 360).
	 * @return the nearest degree to the given radian measurement
	 */
	private static int radians2degrees(double angle) {
		int degrees = (int) Math.toDegrees(angle);
		if (degrees < 0) {
			degrees += 360 * (1 + -degrees / 360);
		}
		degrees = degrees % 360;
		return degrees;
	}

	public void setCornerRadius(int val) {
		cornerRadius = val;
	}

	int getCornerRadius() {
		return cornerRadius;
	}

	public void setAttribute(Attribute attr) {
		this.attr = attr;
		Instant now = mediator.getMajorMoment();
		localCopy = (OmegaHeadModel) attr.getAttrValueAtInstant(now);
		// Extract information about circle for local use
		if (localCopy != null) {
			this.angle = Math.toRadians(localCopy.getOrientation());
			this.lineAngle = Math.toRadians(localCopy.getLineOrientation());
			this.majorDiameter = localCopy.getEllipseHeight();
			this.minorDiameter = localCopy.getEllipseWidth();
			this.lineLength = localCopy.getLineLength();
			this.lineOffset = localCopy.getLineOffset();
			this.x = localCopy.getCentroid().getX().intValue();
			this.y = localCopy.getCentroid().getY().intValue();
			this.shapeYShift = localCopy.getYLineOffset();
			updateOmega();
		}
	}

	/**
	 * 
	 * @return Returns the currentHightlight.
	 */
	public int getCurrentHightlight() {
		return currentHightlight;
	}

	/**
	 * @param currentHightlight
	 *            The currentHightlight to set.
	 */
	public void setCurrentHightlight(int currentHightlight) {
		this.currentHightlight = currentHightlight;
		updateHandles();
	}

	public Point2D getLeftPoint() {
		return line.getP1();
	}

	public Point2D getRightPoint() {
		return line.getP2();
	}
	
	
	/**
	 * Set the line using absolute points.
	 * @param left
	 * @param right
	 */
	public void setLinePoints(Point2D left, Point2D right) {
		// First, find the centroid, wrt the ellipse centroid
		Point2D midpoint = new Point2D.Double ((left.getX() + right.getX()) / 2,  (left.getY() + right.getY()) / 2);
		
		// shift the midpoint by -Centroid, then rotate by -Angle
		Point2D temp = new Point2D.Double(midpoint.getX() - x, midpoint.getY() - y);
		Point2D localMidpoint = AffineTransform.getRotateInstance(angle).transform(temp, null);

		lineOffset = localMidpoint.getX();
		shapeYShift = localMidpoint.getY() - (majorDiameter / 2);
		
		// Next, find lineLength and lineAngle.
		Point2D vec = new Point2D.Double (right.getX() - left.getX(),  right.getY() - left.getY());
		lineLength = vec.distance(0,0);
		lineAngle = Math.PI/2 - Math.atan2(vec.getX(), vec.getY()) + angle; 
		
		// Finally, update the shape
		updateOmegaLinePart();
	}
	
	public void setLineMidpoint(Point2D midpoint) {
		// shift the midpoint by -Centroid, then rotate by -Angle
		Point2D temp = new Point2D.Double(midpoint.getX() - x, midpoint.getY() - y);
		Point2D localMidpoint = AffineTransform.getRotateInstance(angle).transform(temp, null);

		lineOffset = localMidpoint.getX();
		shapeYShift = localMidpoint.getY() - (majorDiameter / 2);
		
		// Finally, update the shape
		updateOmegaLinePart();
	}

	/**
	 * Gets the region the point is hovering over. TODO: handle zoom invariance
	 * 
	 * @param selectPt
	 * @return
	 */
	public int getHoverRegion(Point2D selectPt) {
		double threshSq = selectionThreshold*selectionThreshold;
		if (selectPt.distanceSq(getLeftPoint()) < threshSq) {
			return HIGHLIGHT.LEFT_POINT;
		} else if (selectPt.distanceSq(getRightPoint()) < threshSq) {
			return HIGHLIGHT.RIGHT_POINT;
		} else if (circle.contains(selectPt)) {
			return HIGHLIGHT.RING;
		} else if (line.ptSegDistSq(selectPt) < threshSq) {
			return HIGHLIGHT.LINE;
		} else if (selectPt.distanceSq(northHandlePt) < threshSq) {
			return HIGHLIGHT.NORTH_ORIENTATION_HANDLE;
		} else if (selectPt.distanceSq(southHandlePt) < threshSq) {
			return HIGHLIGHT.SOUTH_ORIENTATION_HANDLE;
		} else if (new Line2D.Double(northeastHandlePt, southeastHandlePt).ptSegDistSq(selectPt) < threshSq) {
			if (northeastHandlePt.distanceSq(selectPt) < threshSq) {
				return HIGHLIGHT.NORTHEAST_SIZE_HANDLE;
			} else if (southeastHandlePt.distanceSq(selectPt) < threshSq) {
				return HIGHLIGHT.SOUTHEAST_SIZE_HANDLE;
			}
			return HIGHLIGHT.EAST_SIZE_HANDLE;
		} else if (new Line2D.Double(northwestHandlePt, southwestHandlePt).ptSegDistSq(selectPt) < threshSq) {
			if (northwestHandlePt.distanceSq(selectPt) < threshSq) {
				return HIGHLIGHT.NORTHWEST_SIZE_HANDLE;
			} else if (southwestHandlePt.distanceSq(selectPt) < threshSq) {
				return HIGHLIGHT.SOUTHWEST_SIZE_HANDLE;
			}
			return HIGHLIGHT.WEST_SIZE_HANDLE;
		} else if (new Line2D.Double(northeastHandlePt, northwestHandlePt).ptSegDistSq(selectPt) < threshSq) {
			return HIGHLIGHT.NORTH_SIZE_HANDLE;
		} else if (new Line2D.Double(southeastHandlePt, southwestHandlePt).ptSegDistSq(selectPt) < threshSq) {
			return HIGHLIGHT.SOUTH_SIZE_HANDLE;
		} else if (biggerCircle.contains(selectPt)) {
			return HIGHLIGHT.RING;
		}
		return HIGHLIGHT.NONE;
	}

	/**
	 * Calculates the distance from the point to the shape. If the point is
	 * within the shape, returns zero.
	 * 
	 * @param pt
	 * @return
	 */
	public double ptShapeDist(Point2D pt) {
		Rectangle2D r = new Rectangle2D.Double(northeastHandlePt.getX(), northeastHandlePt.getY(), southwestHandlePt.getX() - northeastHandlePt.getX(), southwestHandlePt.getY() - northeastHandlePt.getY());
		if (r.contains(pt)) {
			return 0;
		}
		double d = line.ptSegDistSq(pt);
		Point2D[] pts = new Point2D[] {northeastHandlePt, southeastHandlePt, southwestHandlePt, northwestHandlePt, northeastHandlePt};
		for (int i = 1; i <= 4; i++) {
			d = Math.min(d, Line2D.ptSegDistSq(pts[i-1].getX(), pts[i-1].getY(), pts[i].getX(), pts[i].getY(), pt.getX(), pt.getY()));
		}
		d = Math.min(d, pt.distanceSq(northHandlePt));
		d = Math.min(d, pt.distanceSq(southHandlePt));
		return Math.sqrt(d);
	}

	public int getSelectionThreshold() {
		return selectionThreshold;
	}

	public void setSelectionThreshold(int selectionThreshold) {
		this.selectionThreshold = selectionThreshold;
	}

	/**
	 * @return
	 */
	public Line2D getLine() {
		return line;
	}
	
	/**
	 * Gets the midpoint of the line
	 * in canvas coords.
	 * @return
	 */
	public Point2D getLineMidpoint() {
		Line2D L = getLine();
		return new Point2D.Double((L.getX1() + L.getX2()) / 2, (L.getY1() + L.getY2()) / 2);
	}

	/**
	 * @param d
	 */
	public void setShapeX(double x) {
		this.x = x;
		updateOmega();
	}

	/**
	 * @param d
	 */
	public void setShapeY(double y) {
		this.y = y;
		updateOmega();
	}

	/**
	 * @param d
	 */
	public double getShapeX() {
		return x;
	}

	/**
	 * @param d
	 */
	public double getShapeY() {
		return y;
	}

	/**
	 * @return
	 */
	public double getShapeMajorDiameter() {
		return this.majorDiameter;
	}

	/**
	 * @param d
	 */
	public void setShapeMajorDiameter(double d) {
		this.majorDiameter = d;
		updateOmega();
	}

	/**
	 * @return
	 */
	public double getShapeMinorDiameter() {
		return this.minorDiameter;
	}

	/**
	 * @param d
	 */
	public void setShapeMinorDiameter(double d) {
		this.minorDiameter = d;
		updateOmega();
	}

	/**
	 * @return
	 */
	public Point2D getNorthHandlePoint() {
		return this.northHandlePt;
	}

	/**
	 * @return
	 */
	public double getShapeLineLength() {
		return this.lineLength;
	}

	/**
	 * @param w
	 */
	public void setShapeLineLength(double w) {
		this.lineLength = w;
		updateOmega();
	}

	/**
	 * @return
	 */
	public double getShapeLineOffset() {
		return lineOffset;
	}

	/**
	 * @param d
	 */
	public void setShapeLineOffset(double d) {
		this.lineOffset = d;
		updateOmega();
	}

	public Point2D getEastHandlePt() {
		return eastHandlePt;
	}

	public Point2D getNorthHandlePt() {
		return northHandlePt;
	}

	public Point2D getSouthHandlePt() {
		return southHandlePt;
	}

	public Point2D getWestHandlePt() {
		return westHandlePt;
	}

	public Line2D getEquator() {
		return new Line2D.Double(eastHandlePt, westHandlePt);
	}

	public Line2D getMeridian() {
		return new Line2D.Double(northHandlePt, southHandlePt);
	}
	public double getShapeYShift() {
		return shapeYShift;
	}
	public void setShapeYShift(double shapeYShift) {
		this.shapeYShift = shapeYShift;
		updateOmega();
	}
}