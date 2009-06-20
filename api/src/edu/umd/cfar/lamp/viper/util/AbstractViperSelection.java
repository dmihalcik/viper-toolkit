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
package edu.umd.cfar.lamp.viper.util;

import javax.swing.event.*;

/**
 * Implements event handling for viper selection. 
 */
public abstract class AbstractViperSelection implements ViperSelection, Cloneable {
	private EventListenerList cls = new EventListenerList();
	protected boolean notifyingListeners = false;

	/**
	 * {@inheritDoc}
	 */
	public void addChangeListener(ChangeListener l) {
		cls.add(ChangeListener.class, l);
	}
	/**
	 * {@inheritDoc}
	 */
	public void removeChangeListener(ChangeListener l) {
		cls.remove(ChangeListener.class, l);
	}
	/**
	 * {@inheritDoc}
	 */
	public ChangeListener[] getChangeListeners() {
		return (ChangeListener[]) cls.getListeners(ChangeListener.class);
	}
	/**
	 * Fire a change event to all registered listeners.
	 * @param e <code>null</code> if you wish to use a default change event, 
	 * with <code>this</code> as the source
	 */
	protected void fireChangeEvent(ChangeEvent e) {
		assert notifyingListeners == false;
		notifyingListeners = true;
		try {
			Object[] listeners = cls.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChangeListener.class) {
					if (e == null) {
						e = new ChangeEvent(this);
					}
					((ChangeListener) listeners[i + 1]).stateChanged(e);
				}
			}
		} finally {
			notifyingListeners = false;
		}
	}
	
	private boolean locked = false;
	protected synchronized boolean writeLock() {
		if (notifyingListeners) {
			throw new IllegalStateException("Cannot acquire write lock while notifying ");
		}

		try {
			while (locked) {
				wait();
			}
			locked = true;
			return true;
		} catch (InterruptedException iox) {
			return false;
		}
	}
	protected synchronized void writeUnlock() {
		if (locked) {
			locked = false;
			notifyAll();
		}
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException cnsx) {
			throw new RuntimeException(cnsx);
		}
	}
}