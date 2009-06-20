package edu.umd.cfar.lamp.chronicle;

import java.util.*;

public interface TimeLineInputEventListener extends EventListener {
	void processEvent(TimeLineInputEvent aEvent, int type);
}