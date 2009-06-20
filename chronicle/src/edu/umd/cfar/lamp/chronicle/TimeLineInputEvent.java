package edu.umd.cfar.lamp.chronicle;

import java.awt.*;
import java.awt.geom.*;

import viper.api.time.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;

/**
 */
public class TimeLineInputEvent {
	private ChronicleViewer chronicleViewer;
	private PInputEvent piccoloEvent;
	private Instant instant;
	private TimeLine timeLine;
	
	/**
	 * @param chronicleViewer
	 * @param piccoloEvent
	 */
	public TimeLineInputEvent(ChronicleViewer chronicleViewer,
			PInputEvent piccoloEvent) {
		this.chronicleViewer = chronicleViewer;
		this.piccoloEvent = piccoloEvent;
		Point2D localPosition = chronicleViewer.getLocalPosition(piccoloEvent);
		this.instant = chronicleViewer.getInstantFor(localPosition);
		if (!chronicleViewer.getModel().getFocus().contains(this.instant)) {
			this.instant = null;
		}
		this.timeLine = chronicleViewer.getTimeLineFor(localPosition);
	}
	public Instant getInstant() {
		return instant;
	}
	
	/**
	 * @return Returns the piccoloEvent.
	 */
	public PInputEvent getPiccoloEvent() {
		return piccoloEvent;
	}
	
	/**
	 * @param piccoloEvent The piccoloEvent to set.
	 */
	public void setPiccoloEvent(PInputEvent piccoloEvent) {
		this.piccoloEvent = piccoloEvent;
	}
	/**
	 * @return
	 */
	public int getButton() {
		return piccoloEvent.getButton();
	}
	/**
	 * @return
	 */
	public int getClickCount() {
		return piccoloEvent.getClickCount();
	}
	/**
	 * @return
	 */
	public PComponent getComponent() {
		return piccoloEvent.getComponent();
	}
	/**
	 * @return
	 */
	public char getKeyChar() {
		return piccoloEvent.getKeyChar();
	}
	/**
	 * @return
	 */
	public int getKeyCode() {
		return piccoloEvent.getKeyCode();
	}
	/**
	 * @return
	 */
	public int getKeyLocation() {
		return piccoloEvent.getKeyLocation();
	}
	/**
	 * @return
	 * @deprecated use getModifiersEx
	 */
	public int getModifiers() {
		return piccoloEvent.getModifiers();
	}
	/**
	 * @return
	 */
	public int getModifiersEx() {
		return piccoloEvent.getModifiersEx();
	}
	/**
	 * @return
	 */
	public Point2D getPosition() {
		return piccoloEvent.getPosition();
	}
	/**
	 * @return
	 */
	public int getWheelRotation() {
		return piccoloEvent.getWheelRotation();
	}
	/**
	 * @return
	 */
	public long getWhen() {
		return piccoloEvent.getWhen();
	}
	/**
	 * @return
	 */
	public boolean isActionKey() {
		return piccoloEvent.isActionKey();
	}
	/**
	 * @return
	 * @deprecated use getModifiersEx
	 */
	public boolean isAltDown() {
		return piccoloEvent.isAltDown();
	}
	/**
	 * @return
	 * @deprecated use getModifiersEx
	 */
	public boolean isControlDown() {
		return piccoloEvent.isControlDown();
	}
	/**
	 * @return
	 */
	public boolean isFocusEvent() {
		return piccoloEvent.isFocusEvent();
	}
	/**
	 * @return
	 */
	public boolean isHandled() {
		return piccoloEvent.isHandled();
	}
	/**
	 * @return
	 */
	public boolean isKeyEvent() {
		return piccoloEvent.isKeyEvent();
	}
	/**
	 * @return
	 */
	public boolean isLeftMouseButton() {
		return piccoloEvent.isLeftMouseButton();
	}
	/**
	 * @return
	 */
	public boolean isMiddleMouseButton() {
		return piccoloEvent.isMiddleMouseButton();
	}
	/**
	 * @return
	 */
	public boolean isMouseEvent() {
		return piccoloEvent.isMouseEvent();
	}
	/**
	 * @return
	 */
	public boolean isMouseWheelEvent() {
		return piccoloEvent.isMouseWheelEvent();
	}
	/**
	 * @return
	 */
	public boolean isPopupTrigger() {
		return piccoloEvent.isPopupTrigger();
	}
	/**
	 * @return
	 */
	public boolean isRightMouseButton() {
		return piccoloEvent.isRightMouseButton();
	}
	/**
	 * @return
	 * @deprecated use getModifiersEx
	 */
	public boolean isShiftDown() {
		return piccoloEvent.isShiftDown();
	}
	/**
	 * 
	 */
	public void popCursor() {
		piccoloEvent.popCursor();
	}
	/**
	 * @param arg0
	 */
	public void pushCursor(Cursor arg0) {
		piccoloEvent.pushCursor(arg0);
	}
	public void setHandled(boolean arg0) {
		piccoloEvent.setHandled(arg0);
	}
	public TimeLine getTimeLine() {
		return timeLine;
	}
}
