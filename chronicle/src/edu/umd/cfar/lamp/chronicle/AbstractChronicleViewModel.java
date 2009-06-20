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

import java.util.*;

import javax.swing.event.*;

/**
 * Implements some standard methods for a ChronicleModel's view.
 */
public abstract class AbstractChronicleViewModel implements ChronicleViewModel {
	private EventListenerList viewListeners = new EventListenerList();
	
	/**
	 * Indicate the the user has changed focus.
	 * @param e may be <code>null</code>
	 */
	protected void fireFocusChange(EventObject e) {
		Object[] listeners = viewListeners.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChronicleViewListener.class) {
				if (e == null)
					e = new EventObject(this);
				((ChronicleViewListener) listeners[i+1]).focusChanged(e);
			}
		}
	}
	
	/**
	 * Indicate the the data has changed.
	 * @param e may be <code>null</code>
	 */
	public void fireDataChanged(EventObject e) {
		Object[] listeners = viewListeners.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChronicleViewListener.class) {
				if (e == null)
					e = new EventObject(this);
				((ChronicleViewListener) listeners[i+1]).dataChanged(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addChronicleViewListener(ChronicleViewListener cl) {
		viewListeners.add(ChronicleViewListener.class, cl);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeChronicleViewListener(ChronicleViewListener cl) {
		viewListeners.remove(ChronicleViewListener.class, cl);
	}
}
