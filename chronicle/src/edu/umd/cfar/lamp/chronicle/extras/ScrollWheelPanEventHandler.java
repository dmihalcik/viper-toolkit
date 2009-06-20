package edu.umd.cfar.lamp.chronicle.extras;


import java.awt.event.*;

import edu.umd.cs.piccolo.event.*;

/**
 * @author clin
 */
public class ScrollWheelPanEventHandler extends PPanEventHandler {
	public ScrollWheelPanEventHandler() {
		super();
		setEventFilter(new PInputEventFilter());
		setAutopan(false);
	}
	
	public boolean acceptsEvent(PInputEvent event, int type) {
		int mods = event.getModifiersEx();
		// middle mouse button is the only acceptable one
		boolean buttonDown = 0 != (mods & InputEvent.BUTTON2_DOWN_MASK);
		return buttonDown;
	}
}

