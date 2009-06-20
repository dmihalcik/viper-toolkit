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

package edu.umd.cfar.lamp.chronicle;

import viper.api.time.*;

/**
 * A listener that has startDrag, drag, and endDrag
 * methods to override.
 */
public class DragTimeLineListener extends BasicTimeLineInputEventAdapter {
	protected boolean isDragging = false;

	protected TimeLine startLine = null;
	protected Instant startInstant = null;

	protected TimeLine currentLine = null;
	protected Instant currentInstant = null;

	public void mousePressed(TimeLineInputEvent event) {
		startLine = event.getTimeLine();
		startInstant = event.getInstant();
		currentLine = event.getTimeLine();
		currentInstant = event.getInstant();
		if (shouldStartDragInteraction(event)) {
			isDragging = true;
			startDrag(event);
		}
	}

	/**
	 * Override this to only start dragging if, say, start instant 
	 * and start timeline are non-null.
	 * @param event
	 * @return
	 */
	protected boolean shouldStartDragInteraction(TimeLineInputEvent event) {
		return true;
	}

	public void mouseDragged(TimeLineInputEvent event) {
		if (isDragging) {
			currentLine = event.getTimeLine();
			currentInstant = event.getInstant();
			drag(event);
		}
	}

	public void mouseReleased(TimeLineInputEvent event) {
		if (isDragging) {
			currentLine = event.getTimeLine();
			currentInstant = event.getInstant();
			isDragging = false;
			endDrag(event);
			currentLine = null;
			startLine = null;
		}
	}

	protected void startDrag(TimeLineInputEvent event) {
	}
	protected void drag(TimeLineInputEvent event) {
	}
	/**
	 * Right now, this is never called by the default dragger,
	 * as I don't know when to call it.
	 */
	protected void cancelDrag() {
	}
	protected void endDrag(TimeLineInputEvent event) {
	}
}