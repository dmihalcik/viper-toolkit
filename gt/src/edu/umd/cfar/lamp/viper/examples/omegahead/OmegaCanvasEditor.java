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

import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cs.piccolo.event.*;

/**
 * @author davidm
 */
public class OmegaCanvasEditor extends CanvasEditor {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	public static final int MIN_DIST = 20;

	private Point2D pressPoint, dragPoint;

	private OmegaNode refNode;

	private int dragType = OmegaNode.HIGHLIGHT.NONE;

	// for dragging the orientation handle
	private double offsetAngle;

	// for dragging points
	private double offsetX;

	private double offsetY;

	// / For dragging the line
	private double offsetDistance;

	public OmegaCanvasEditor(Attributable attrIn) {
		super(attrIn);
		refNode = (OmegaNode) attrIn;
		logger.fine("Created Omega Editor");
	}

	public String getName() {
		return "OmegaEditor";
	}

	public double minDist(Point2D selectPt) {
		return refNode.ptShapeDist(selectPt);
	}

	public boolean contains(Point2D select) {
		return refNode.getHoverRegion(select) != OmegaNode.HIGHLIGHT.NONE;
	}

	public void keyPressed(PInputEvent e) {

	}

	public void doWhenUnselected() {
		// nothing needs to happen
	}

	/**
	 * @param e
	 */
	private void handleMouseCommon(PInputEvent e) {
		Point2D localPoint = e.getPosition();
		if (!isLocked(e)) {
			refNode.setCurrentHightlight(refNode.getHoverRegion(localPoint));
		} else {
			refNode.setCurrentHightlight(OmegaNode.HIGHLIGHT.NONE);
		}
	}

	private Point2D oldLineRefPoint;
	
	public void mousePressed(PInputEvent e) {
		int mods = e.getModifiersEx();
		if ((mods & InputEvent.BUTTON1_DOWN_MASK) == 0) {
			return;
		}
		handleMouseCommon(e);
		dragType = refNode.getCurrentHightlight();
		oldLineRefPoint = null;
		if (dragType != OmegaNode.HIGHLIGHT.NONE) {
			pressPoint = e.getPosition();

			offsetAngle = 0;
			offsetX = 0;
			offsetY = 0;
			Point2D pointToOffsetTo = null;
			Line2D lineToOffsetTo = null;
			offsetX = 0;
			offsetY = 0;
			switch (dragType) {
			case OmegaNode.HIGHLIGHT.NORTH_SIZE_HANDLE:
				lineToOffsetTo = new Line2D.Double(refNode
						.getNorthwestCornerPt(), refNode.getNortheastCornerPt());
				break;
			case OmegaNode.HIGHLIGHT.NORTHEAST_SIZE_HANDLE:
				pointToOffsetTo = refNode.getNortheastCornerPt();
				break;
			case OmegaNode.HIGHLIGHT.EAST_SIZE_HANDLE:
				lineToOffsetTo = new Line2D.Double(refNode
						.getNortheastCornerPt(), refNode.getSoutheastCornerPt());
				break;
			case OmegaNode.HIGHLIGHT.SOUTHEAST_SIZE_HANDLE:
				pointToOffsetTo = refNode.getSoutheastCornerPt();
				break;
			case OmegaNode.HIGHLIGHT.SOUTH_SIZE_HANDLE:
				lineToOffsetTo = new Line2D.Double(refNode
						.getSoutheastCornerPt(), refNode.getSouthwestCornerPt());
				break;
			case OmegaNode.HIGHLIGHT.SOUTHWEST_SIZE_HANDLE:
				pointToOffsetTo = refNode.getSouthwestCornerPt();
				break;
			case OmegaNode.HIGHLIGHT.WEST_SIZE_HANDLE:
				lineToOffsetTo = new Line2D.Double(refNode
						.getSouthwestCornerPt(), refNode.getNorthwestCornerPt());
				break;
			case OmegaNode.HIGHLIGHT.NORTHWEST_SIZE_HANDLE:
				pointToOffsetTo = refNode.getNorthwestCornerPt();
				break;
			case OmegaNode.HIGHLIGHT.LEFT_POINT:
				pointToOffsetTo = refNode.getLeftPoint();
				break;
			case OmegaNode.HIGHLIGHT.RIGHT_POINT:
				pointToOffsetTo = refNode.getRightPoint();
				break;
			case OmegaNode.HIGHLIGHT.SOUTH_ORIENTATION_HANDLE:
			case OmegaNode.HIGHLIGHT.NORTH_ORIENTATION_HANDLE:
				offsetAngle = Math.atan2(pressPoint.getX()
						- refNode.getShapeX(), pressPoint.getY()
						- refNode.getShapeY());
				offsetAngle -= Math.PI;
				offsetAngle -= refNode.getShapeAngleInRadians();
				pointToOffsetTo = refNode.getNorthHandlePoint();
				break;
			case OmegaNode.HIGHLIGHT.LINE:
				lineToOffsetTo = refNode.getLine();
				pointToOffsetTo = refNode.getLineMidpoint();
				break;
			case OmegaNode.HIGHLIGHT.RING:
				pointToOffsetTo = new Point2D.Double(refNode.getShapeX(),
						refNode.getShapeY());
				break;
			}
			if (null != pointToOffsetTo) {
				offsetX = pointToOffsetTo.getX() - pressPoint.getX();
				offsetY = pointToOffsetTo.getY() - pressPoint.getY();
			}
			if (null != lineToOffsetTo) {
				offsetDistance = orientedPointLineDist(lineToOffsetTo,
						pressPoint);
			}
		}
	}

	/**
	 * @param lineToOffsetTo
	 */
	private static double orientedPointLineDist(Line2D lineToOffsetTo, Point2D p) {
		double d = lineToOffsetTo.ptLineDist(p);
		if (lineToOffsetTo.relativeCCW(p) > 0) {
			// If the press point is inside the shape
			d = -d;
		}
		return d;
	}

	public void mouseMoved(PInputEvent e) {
		dragType = OmegaNode.HIGHLIGHT.NONE;
		handleMouseCommon(e);
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		boolean shiftDown = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
		boolean altDown = (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
		boolean ctrlDown = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
		boolean metaDown = (e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0;

		if (!leftClick) {
			return;
		}
		Point2D localPoint = e.getPosition();
		boolean s = this.isSelected();
		boolean l = isLocked(e);
		if (dragType == OmegaNode.HIGHLIGHT.NONE) {
			if (s && !l) {
				refNode
						.setCurrentHightlight(refNode
								.getHoverRegion(localPoint));
			}
		} else {
			double d;
			Line2D meridian = refNode.getMeridian();
			Line2D equator = refNode.getEquator();
			double dx = 0;
			double dy = 0;
			double oldOffset = refNode.getShapeLineOffset();
			double oldLength = refNode.getShapeLineLength();
			double oldMajorDiameter = refNode.getShapeMajorDiameter();
			double oldMinorDiameter = refNode.getShapeMinorDiameter();
			double oldRight = oldOffset + oldLength / 2;
			double oldLeft = oldOffset - oldLength / 2;
			Point2D offsetPoint = new Point2D.Double(localPoint.getX()
					+ offsetX, localPoint.getY() + offsetY);
			double newHeight = -1;
			boolean left = false;
			Line2D oppositeMeridian = meridian;
			Line2D oppositeEquator = equator;
			if (OmegaNode.isNorthDirectionSizeHandle(dragType)) {
				oppositeEquator = new Line2D.Double(refNode
						.getSoutheastCornerPt(), refNode.getSouthwestCornerPt());
			} else if (OmegaNode.isSouthDirectionSizeHandle(dragType)) {
				oppositeEquator = new Line2D.Double(refNode
						.getNorthwestCornerPt(), refNode.getNortheastCornerPt());
			}
			if (OmegaNode.isEastDirectionSizeHandle(dragType)) {
				oppositeMeridian = new Line2D.Double(refNode
						.getSouthwestCornerPt(), refNode.getNorthwestCornerPt());
			} else if (OmegaNode.isWestDirectionSizeHandle(dragType)) {
				oppositeMeridian = new Line2D.Double(refNode
						.getNortheastCornerPt(), refNode.getSoutheastCornerPt());
			}
			switch (dragType) {
			case OmegaNode.HIGHLIGHT.LEFT_POINT:
				left = true;
			case OmegaNode.HIGHLIGHT.RIGHT_POINT:
				// Holding down shift maintains the shape
				// of the ellipse and the line offset.
				if (ctrlDown) {
					d = -orientedPointLineDist(meridian, offsetPoint);
					if ((left && d > oldRight) || (!left && d < oldLeft)) {
						dragType = OmegaNode.flipEastWest(dragType);
					}
					refNode.setShapeLineLength(Math.abs(2 * (d - oldOffset)));
				} else {
					if (left) {
						if (oldLineRefPoint == null) {
							oldLineRefPoint = refNode.getRightPoint();
						}
						refNode.setLinePoints(oldLineRefPoint,offsetPoint);
					} else {
						if (oldLineRefPoint == null) {
							oldLineRefPoint = refNode.getLeftPoint();
						}
						refNode.setLinePoints(offsetPoint, oldLineRefPoint);
					}
				}
				break;
			case OmegaNode.HIGHLIGHT.RING:
				refNode.setShapeX(offsetPoint.getX());
				refNode.setShapeY(offsetPoint.getY());
				break;
			case OmegaNode.HIGHLIGHT.NORTHWEST_SIZE_HANDLE:
			case OmegaNode.HIGHLIGHT.SOUTHWEST_SIZE_HANDLE:
			case OmegaNode.HIGHLIGHT.NORTHEAST_SIZE_HANDLE:
			case OmegaNode.HIGHLIGHT.SOUTHEAST_SIZE_HANDLE:
				if (ctrlDown) {
					dx = 2 * meridian.ptLineDist(offsetPoint);
					dy = 2 * equator.ptLineDist(offsetPoint);
				} else {
					dx = -orientedPointLineDist(oppositeMeridian, offsetPoint);
					dy = -orientedPointLineDist(oppositeEquator, offsetPoint);
				}
				if (shiftDown) {
					double oldRatio = oldMajorDiameter / oldMinorDiameter;
					double newRatio = dy / dx;
					// if newRatio > oldRatio, the click point is above the
					// diagonal. So, the dy value should be respected.
					if (newRatio > oldRatio) {
						dx = dy / oldRatio;
					} else {
						dy = dx * oldRatio;
					}
				}
				break;
			case OmegaNode.HIGHLIGHT.WEST_SIZE_HANDLE:
			case OmegaNode.HIGHLIGHT.EAST_SIZE_HANDLE:
				if (ctrlDown) {
					d = -orientedPointLineDist(meridian, localPoint);
				} else {
					d = -orientedPointLineDist(oppositeMeridian, localPoint);
				}
				d -= offsetDistance;
				if (ctrlDown) {
					d = 2 * d;
				}
				dx = 0;
				dy = 0;
				if (shiftDown) {
					double oldRatio = refNode.getShapeMajorDiameter()
							/ refNode.getShapeMinorDiameter();
					dy = d * oldRatio;
				} else {
					dy = oldMajorDiameter;
				}
				dx = d;
				break;
			case OmegaNode.HIGHLIGHT.NORTH_SIZE_HANDLE:
			case OmegaNode.HIGHLIGHT.SOUTH_SIZE_HANDLE:
				if (ctrlDown) {
					d = -orientedPointLineDist(equator, localPoint);
				} else {
					d = -orientedPointLineDist(oppositeEquator, localPoint);
				}
				d -= offsetDistance;
				if (ctrlDown) {
					d = 2 * d;
				}
				dx = 0;
				dy = 0;
				if (shiftDown) {
					double oldRatio = refNode.getShapeMinorDiameter()
							/ refNode.getShapeMajorDiameter();
					dx = d * oldRatio;
				} else {
					dx = oldMinorDiameter;
				}
				dy = d;
				break;
			case OmegaNode.HIGHLIGHT.SOUTH_ORIENTATION_HANDLE:
			case OmegaNode.HIGHLIGHT.NORTH_ORIENTATION_HANDLE:
				double na = Math.atan2(localPoint.getX() - refNode.getShapeX(),
						localPoint.getY() - refNode.getShapeY());
				na -= Math.PI;
				na -= offsetAngle;
				refNode.setShapeAngleInRadians(na);
				break;
			case OmegaNode.HIGHLIGHT.LINE:
				if (oldLineRefPoint == null) {
					oldLineRefPoint = refNode.getLineMidpoint();
				}
				refNode.setLineMidpoint(offsetPoint);
				break;
			}
			// System.out.println("dx,dy = " + dx + "," + dy + "; hi = " +
			// dragType);
			if (dx != 0) {
				boolean neg = dx < 0;
				refNode.setShapeMinorDiameter(neg ? -dx : dx);
				if (!ctrlDown) {
					if (OmegaNode.isEastDirectionSizeHandle(dragType)) {
						dx = (oldMinorDiameter - dx) / 2;
					} else if (OmegaNode.isWestDirectionSizeHandle(dragType)) {
						dx = -(oldMinorDiameter - dx) / 2;
					} else {
						dx = 0;
					}
				} else {
					dx = 0;
				}
				if (neg) {
					dragType = OmegaNode.flipEastWest(dragType);
					offsetDistance = -offsetDistance;
				}
			}
			if (dy != 0) {
				boolean neg = dy < 0;
				dy = neg ? -dy : dy;
				refNode.setShapeMajorDiameter(neg ? -dy : dy);
				if (!ctrlDown) {
					// shift in appropriate direction by dy-oldMajorDiameter
					if (OmegaNode.isNorthDirectionSizeHandle(dragType)) {
						dy = (oldMajorDiameter - dy) / 2;
					} else if (OmegaNode.isSouthDirectionSizeHandle(dragType)) {
						dy = -(oldMajorDiameter - dy) / 2;
					} else {
						dy = 0;
					}
				} else {
					dy = 0;
				}
				if (neg) {
					dragType = OmegaNode.flipNorthSouth(dragType);
					offsetDistance = -offsetDistance;
				}
			}
			if (dx != 0 || dy != 0) {
				double cosA = Math.cos(refNode.getShapeAngleInRadians());
				double sinA = Math.sin(refNode.getShapeAngleInRadians());
				refNode
						.setShapeX(refNode.getShapeX()
								+ (dx * cosA + dy * sinA));
				refNode.setShapeY(refNode.getShapeY()
						+ (-dx * sinA + dy * cosA));
			}
			refNode.setCurrentHightlight(dragType);
		}
	}

	public void mouseReleased(PInputEvent e) {
		boolean leftStillDown = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		if (leftStillDown) {
			return;
		}
		dragType = OmegaNode.HIGHLIGHT.NONE;
	}

	public boolean inRangeOfInterest(Point2D point) {
		return contains(point);
	}
}