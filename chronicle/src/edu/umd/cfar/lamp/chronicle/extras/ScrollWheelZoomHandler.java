package edu.umd.cfar.lamp.chronicle.extras;


import java.awt.geom.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;

/**
 * @author clin
 *
 * Code is a combination of PZoomEventHandler (which uses dragging to
 * control zooming) and ChronicleViewer which uses scrollwheel to control
 * zooming, and makes scrollwheel control zooming.  Event processing
 * is done by mouseWheelRotated.  This is added as an event handler to
 * ViperDataCanvas (as an InputEventListener), but can be used elsewhere.
 */
public class ScrollWheelZoomHandler extends PZoomEventHandler {
	private final double SCALE_FACTOR = 0.125 ;
	private double minScale = 0;
	private double maxScale = Double.MAX_VALUE;
	private Point2D viewZoomPoint;
	
	
	public boolean acceptsEvent(PInputEvent event, int type) {
		return event.isMouseWheelEvent();
	}

	/**
	 * Wheel rotation up zooms in.  Wheel rotation down zooms out
	 */
	public void mouseWheelRotated(PInputEvent event) {
		viewZoomPoint = event.getPosition();
		PCamera camera = event.getCamera();

		double scaleDelta = 1 - (event.getWheelRotation() * SCALE_FACTOR) ;

		double currentScale = camera.getViewScale();
		double newScale = currentScale * scaleDelta;

		if (newScale < minScale) {
			scaleDelta = minScale / currentScale;
		}
		if ((maxScale > 0) && (newScale > maxScale)) {
			scaleDelta = maxScale / currentScale;
		}

		camera.scaleViewAboutPoint(scaleDelta, viewZoomPoint.getX(), viewZoomPoint.getY());
	}

	/**
	 * Creates a new zoom handler.
	 */
//	public PZoomEventHandler() {
//		super();
//		setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
//	}

	//****************************************************************
	// Zooming
	//****************************************************************

	/**
	 * Returns the minimum view magnification factor that this event handler is bound by.
	 * The default is 0.
	 * @return the minimum camera view scale
	 */
	public double getMinScale() {
		return minScale;
	}

	/**
	 * Sets the minimum view magnification factor that this event handler is bound by.
	 * The camera is left at its current scale even if <code>minScale</code> is larger than
	 * the current scale.
	 * @param minScale the minimum scale, must not be negative.
	 */
	public void setMinScale(double minScale) {
		this.minScale = minScale;
	}

	/**
	 * Returns the maximum view magnification factor that this event handler is bound by.
	 * The default is Double.MAX_VALUE.
	 * @return the maximum camera view scale
	 */
	public double getMaxScale() {
		return maxScale;
	}

	/**
	 * Sets the maximum view magnification factor that this event handler is bound by.
	 * The camera is left at its current scale even if <code>maxScale</code> is smaller than
	 * the current scale. Use Double.MAX_VALUE to specify the largest possible scale. 
	 * @param maxScale the maximum scale, must not be negative.
	 */
	public void setMaxScale(double maxScale) {
		this.maxScale = maxScale;
	}
	
	//****************************************************************
	// Debugging - methods for debugging
	//****************************************************************

	/**
	 * Returns a string representing the state of this node. This method is
	 * intended to be used only for debugging purposes, and the content and
	 * format of the returned string may vary between implementations. The
	 * returned string may be empty but may not be <code>null</code>.
	 *
	 * @return  a string representation of this node's state
	 */
	protected String paramString() {
		StringBuffer result = new StringBuffer();

		result.append("minScale=" + minScale);
		result.append(",maxScale=" + maxScale);
		result.append(",viewZoomPoint=" + (viewZoomPoint == null ? "null" : viewZoomPoint.toString()));
		result.append(',');
		result.append(super.paramString());

		return result.toString();
	}	
}
