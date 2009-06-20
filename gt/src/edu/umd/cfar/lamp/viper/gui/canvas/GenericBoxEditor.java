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


package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.event.*;
import java.awt.geom.*;

import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cs.piccolo.event.*;

/**
 * @author davidm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class GenericBoxEditor extends CanvasEditor {
	protected static final boolean DEFAULT_DRAW_FROM_CENTER = false;
	protected static final boolean DEFAULT_CONSTRAIN_ASPECT_RATIO = false;
	protected static final int LEFT_MOUSE_MASK = InputEvent.BUTTON1_MASK;

	protected CanvasDir currDir = CanvasDir.NONE;

	protected Point2D[] origPts = new Point2D[4];
	protected Point2D[] currPts;
	protected Point2D pressPoint, dragPoint;

	private boolean drawnFromCenter;
	private boolean constrainingAspectRatio;
	protected PBoxNode box;

	public GenericBoxEditor(Attributable attrIn) {
		super(attrIn);
	}

	protected int CORNER_THRESHOLD = 7;
	protected int SIDE_THRESHOLD = 7;
	protected CanvasDir findDirection(Point2D refPoint) {
		if (refPoint.distance(currPts[0]) < CORNER_THRESHOLD)
			return CanvasDir.TOP_LEFT;
		else if (refPoint.distance(currPts[1]) < CORNER_THRESHOLD)
			return CanvasDir.TOP_RIGHT;
		else if (refPoint.distance(currPts[2]) < CORNER_THRESHOLD)
			return CanvasDir.BOTTOM_RIGHT;
		else if (refPoint.distance(currPts[3]) < CORNER_THRESHOLD)
			return CanvasDir.BOTTOM_LEFT;
		else {
			Line2D top = new Line2D.Double(currPts[0], currPts[1]);
			Line2D right = new Line2D.Double(currPts[1], currPts[2]);
			Line2D bottom = new Line2D.Double(currPts[2], currPts[3]);
			Line2D left = new Line2D.Double(currPts[3], currPts[0]);
			if (top.ptSegDist(refPoint) < SIDE_THRESHOLD)
				return CanvasDir.TOP;
			else if (right.ptSegDist(refPoint) < SIDE_THRESHOLD)
				return CanvasDir.RIGHT;
			else if (left.ptSegDist(refPoint) < SIDE_THRESHOLD)
				return CanvasDir.LEFT;
			else if (bottom.ptSegDist(refPoint) < SIDE_THRESHOLD)
				return CanvasDir.BOTTOM;
			if (contains(refPoint))
				return CanvasDir.INTERIOR;
		}

		return CanvasDir.NONE;
	}
	
	/**
	 * @return
	 */
	public boolean isDrawnFromCenter() {
		return drawnFromCenter;
	}
	public boolean isConstrainingAspectRatio() {
		return constrainingAspectRatio;
	}
	public void setConstrainingAspectRatio(boolean maintainingAspectRatio) {
		if (this.constrainingAspectRatio != maintainingAspectRatio) {
			this.constrainingAspectRatio = maintainingAspectRatio;
			if (cornerPointsValid()) {
				resizeBox(currDir);
			}
		}
	}

	/**
	 * @param b
	 */
	public void setDrawnFromCenter(boolean b) {
		if (drawnFromCenter != b) {
			drawnFromCenter = b;
			if (cornerPointsValid()) {
				resizeBox(currDir);
			}
			box.setCenterBolded(drawnFromCenter);
		}
	}
	
	public abstract void resizeBox(CanvasDir dir);

	
	public static boolean isCorner(CanvasDir dir) {
		return (dir == CanvasDir.TOP_LEFT || dir == CanvasDir.TOP_RIGHT
				|| dir == CanvasDir.BOTTOM_LEFT || dir == CanvasDir.BOTTOM_RIGHT);
	}

	public static boolean isEdge(CanvasDir dir) {
		return !(isCorner(dir) || dir == CanvasDir.NONE || dir == CanvasDir.INTERIOR);
	}
	/**
	 * @return
	 */
	protected boolean cornerPointsValid() {
		return pressPoint != null && dragPoint != null && (isCorner(currDir) || isEdge(currDir));
	}
	
	public void keyPressed(PInputEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			setDrawnFromCenter(!DEFAULT_DRAW_FROM_CENTER);
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			setConstrainingAspectRatio(!DEFAULT_CONSTRAIN_ASPECT_RATIO);
		} 
	}

	public void keyReleased(PInputEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			setDrawnFromCenter(DEFAULT_DRAW_FROM_CENTER);
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			setConstrainingAspectRatio(DEFAULT_CONSTRAIN_ASPECT_RATIO);
		}
	}
}
