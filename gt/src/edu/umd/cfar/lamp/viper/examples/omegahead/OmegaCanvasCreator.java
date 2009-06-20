package edu.umd.cfar.lamp.viper.examples.omegahead;

import java.awt.event.*;
import java.awt.geom.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cs.piccolo.event.*;

/**
 *  
 */
public class OmegaCanvasCreator extends CanvasCreator {
	private static final int START = 0;

	private static final int RADIUS = 1;

	private static final int BASELINE = 2;

	private static final int DONE = 3;
	
	private static final int TOPLINE = 4;

	private static final int HEIGHT = 5;

	private OmegaNode soFar;

	private int state;

	private Point2D pressPoint;

	private Point2D dragPoint;
	
	private Line2D topLine;
	
	/**
	 * @param asst
	 * @param attr
	 */
	public OmegaCanvasCreator(CreatorAssistant asst, Attribute attr) {
		super(asst, attr);
		soFar = new OmegaNode();
		displaySelected();
		state = START;
	}

	/** @inheritDoc */
	public String getName() {
		return "OMEGA CREATOR";
	}

	public void displaySelected() {
		soFar.setDisplayProperties(HighlightSingleton.STYLE_SELECTED);
	}

	/**
	 * Used to switch between edit modes.
	 * @param e {@inheritDoc}
	 */
	public void keyPressed(PInputEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			if (state == TOPLINE) {
				state = RADIUS;
				updateRadius();
			}
		}
	}
	
	/**
	 * Used to switch between edit modes.
	 * @param e {@inheritDoc}
	 */
	public void keyReleased(PInputEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			if (state == RADIUS) {
				// Switch to TOPLINE on the first mouse drag
				state = TOPLINE;
				updateTopline();
			}
		}
	}
	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);
		pressPoint = e.getPosition();
		commonEventHandling(e);
		// This adds the obox to the canvas
		if (state == START) {
			int mods = e.getModifiersEx();
			boolean ctrlDown = 0 != (mods & InputEvent.CTRL_DOWN_MASK);
			if (ctrlDown) {
				soFar.setShapeX(pressPoint.getX());
				soFar.setShapeY(pressPoint.getY());
				getAssistant().addShape(soFar);
				state = RADIUS;
			} else {
				soFar.setShapeX(pressPoint.getX());
				soFar.setShapeY(pressPoint.getY());
				getAssistant().addShape(soFar);
				state = TOPLINE;
			}
		}
	}

	/**
	 * @param e
	 */
	private void commonEventHandling(PInputEvent e) {
		dragPoint = e.getPosition();
		int mods = e.getModifiersEx();
		boolean ctrlDown = 0 != (mods & InputEvent.CTRL_DOWN_MASK);
		if (ctrlDown && state == RADIUS) {
			state = TOPLINE;
		} else if (ctrlDown && state == TOPLINE) {
			state = RADIUS;
		}
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		dragPoint = e.getPosition();
		if (state == RADIUS) {
			updateRadius();
		} else if (state == TOPLINE) {
			updateTopline();
		} else if (state == BASELINE) {
			updateBaseline();
		} else if (state == HEIGHT) {
			updateHeight();
		}
	}

	public void mouseMoved(PInputEvent e) {
		super.mouseMoved(e);
		commonEventHandling(e);
		if (state == RADIUS) {
			updateRadius();
		} else if (state == TOPLINE) {
			updateTopline();
		} else if (state == HEIGHT) {
			updateHeight();
		} else if (state == BASELINE) {
			dragPoint = e.getPosition();
			updateBaseline();
		}
	}

	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		commonEventHandling(e);
		switch (state) {
		case START:
			state = RADIUS;
			break;
		case RADIUS:
			updateRadius();
			state = BASELINE;
			break;
		case BASELINE:
			updateBaseline();
			state = DONE;
			setAttrValueInMediator(soFar.getUpdatedAttribute());
			break;
		case TOPLINE:
			updateTopline();
			state = HEIGHT;
			break;
		case HEIGHT:
			updateHeight();
			state = DONE;
			setAttrValueInMediator(soFar.getUpdatedAttribute());
			break;
		default:
		}
	}

	private void updateTopline() {
		double d = pressPoint.distance(dragPoint);
		topLine = new Line2D.Double(pressPoint,dragPoint);
		soFar.setShapeMinorDiameter(d/2);
		soFar.setShapeMajorDiameter(1);
		soFar.setShapeLineLength(d);
		double r = Math.atan2(topLine.getX2() - topLine.getX1(), topLine.getY2() - topLine.getY1());
		soFar.setShapeAngleInRadians(r - Math.PI/2);
		soFar.setShapeX((topLine.getX2() + topLine.getX1())/2);
		soFar.setShapeY((topLine.getY2() + topLine.getY1())/2);
	}

	private void updateRadius() {
		double d = pressPoint.distance(dragPoint) * 2;
		soFar.setShapeMajorDiameter(d);
		soFar.setShapeMinorDiameter(d);
		soFar.setShapeAngleInRadians(Math.atan2(dragPoint.getX() - pressPoint.getX(), dragPoint
						.getY()
						- pressPoint.getY()));
	}

	private void updateBaseline() {
		Line2D centerLine = new Line2D.Double(soFar.getShapeX(), soFar
				.getShapeY(), soFar.getNorthHandlePoint().getX(), soFar
				.getNorthHandlePoint().getY());
		double d = centerLine.ptLineDist(dragPoint.getX(), dragPoint.getY());
		soFar.setShapeLineLength(2 * d);
	}
	
	private Point2D lastDragPoint = null;
	private void updateHeight() {
		if (dragPoint.equals(lastDragPoint)) {
			return;
		}
		lastDragPoint = dragPoint;
		double h = topLine.ptLineDist(dragPoint.getX(), dragPoint.getY());
		boolean above = topLine.relativeCCW(dragPoint) > 0;
		// then set minor diameter to h
		soFar.setShapeMajorDiameter(h);
		// need to move xy 1/2 h from topline
		double r = Math.atan2(topLine.getX2() - topLine.getX1(), topLine.getY2() - topLine.getY1());
		double x = topLine.getX2() + topLine.getX1();
		double y = topLine.getY2() + topLine.getY1();
		x /= 2; y /= 2; h /= 2;
		if (above) {
			soFar.setShapeX(x + h * Math.cos(r));
			soFar.setShapeY(y - h * Math.sin(r));
		} else {
			soFar.setShapeX(x - h * Math.cos(r));
			soFar.setShapeY(y + h * Math.sin(r));
		}
	}
}