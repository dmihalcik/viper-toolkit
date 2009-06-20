package edu.umd.cfar.lamp.chronicle;

import java.awt.event.*;

public abstract class BasicTimeLineInputEventListener implements TimeLineInputEventListener {
	public void processEvent(TimeLineInputEvent event, int type) {
			if (!acceptsEvent(event, type)) return;

			switch (type) {
				case KeyEvent.KEY_PRESSED:
					keyPressed(event);
					break;

				case KeyEvent.KEY_RELEASED:
					keyReleased(event);
					break;

				case KeyEvent.KEY_TYPED:
					keyTyped(event);
					break;

				case MouseEvent.MOUSE_CLICKED:
					mouseClicked(event);
					break;

				case MouseEvent.MOUSE_DRAGGED:
					mouseDragged(event);
					break;

				case MouseEvent.MOUSE_ENTERED:
					mouseEntered(event);
					break;

				case MouseEvent.MOUSE_EXITED:
					mouseExited(event);
					break;

				case MouseEvent.MOUSE_MOVED:
					mouseMoved(event);
					break;

				case MouseEvent.MOUSE_PRESSED:
					mousePressed(event);
					break;

				case MouseEvent.MOUSE_RELEASED:
					mouseReleased(event);
					break;

				case MouseWheelEvent.WHEEL_UNIT_SCROLL:
					mouseWheelRotated(event);
					break;
				
				case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
					mouseWheelRotatedByBlock(event);
					break;
					
				case FocusEvent.FOCUS_GAINED:
					keyboardFocusGained(event);
					break;
					
				case FocusEvent.FOCUS_LOST:
					keyboardFocusLost(event);
					break;
								
				default:
					throw new RuntimeException("Bad Event Type");
			}
	}

	public abstract void keyboardFocusLost(TimeLineInputEvent event);

	public abstract void keyboardFocusGained(TimeLineInputEvent event);

	public abstract void mouseWheelRotatedByBlock(TimeLineInputEvent event);

	public abstract void mouseWheelRotated(TimeLineInputEvent event);

	public abstract void mouseReleased(TimeLineInputEvent event);

	public abstract void mousePressed(TimeLineInputEvent event);

	public abstract void mouseMoved(TimeLineInputEvent event);

	public abstract void mouseExited(TimeLineInputEvent event);

	public abstract void mouseEntered(TimeLineInputEvent event);

	public abstract void mouseDragged(TimeLineInputEvent event);

	public abstract void mouseClicked(TimeLineInputEvent event);

	public abstract void keyTyped(TimeLineInputEvent event);

	public abstract void keyReleased(TimeLineInputEvent event);

	public abstract void keyPressed(TimeLineInputEvent event);

	public abstract boolean acceptsEvent(TimeLineInputEvent event, int type);
}
