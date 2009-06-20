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


package edu.umd.cfar.lamp.chronicle.extras.emblems;

import javax.swing.event.*;

/**
 * Implements standard emblem model methods, e.g. 
 * event generators..
 */
public abstract class AbstractEmblemModel implements EmblemModel {
	protected boolean notifyingListeners = false;
	private EventListenerList listeners = new EventListenerList();

	/**
	 * Fires a change event to all listeners.
	 * Note that this will modify the {@link #notifyingListeners}
	 * property while it is running.
	 * @throws IllegalStateException when already firing change 
	 * events (more specifically, when {@link #notifyingListeners} 
	 * is <code>true</code>.
	 */
	protected void fireChangeEvent() {
		if (notifyingListeners == true)
			throw new IllegalStateException();
		notifyingListeners = true;
		try {
			ChangeEvent ce = new ChangeEvent(this);
			Object[] listeners = this.listeners.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChangeListener.class) {
					((ChangeListener) listeners[i + 1]).stateChanged(ce);
				}
			}
		} finally {
			notifyingListeners = false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addChangeListener(ChangeListener cl) {
		listeners.add(ChangeListener.class, cl);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(ChangeListener.class, cl);
	}
}
