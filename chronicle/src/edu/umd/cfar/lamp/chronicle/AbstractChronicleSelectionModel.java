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

import javax.swing.event.*;

/**
 * Implements some of the more standard methods of a chronicle
 * selection, namely, handling event listeners.
 */
public abstract class AbstractChronicleSelectionModel
		implements
			ChronicleSelectionModel {
	protected EventListenerList listenerList = new EventListenerList();
	
	/**
	 * Fires a change event.
	 * @param e the change event. May be <code>null</code>.
	 */
	protected void fireChangeEvent(ChangeEvent e) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				if (e == null) {
					e = new ChangeEvent(this);
				}
				((ChangeListener) listeners[i + 1]).stateChanged(e);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ChangeListener[] getChangeListeners() {
		return (ChangeListener[]) listenerList.getListeners(ChangeListener.class);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}
}