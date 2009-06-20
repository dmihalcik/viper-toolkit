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
 * A basic implementation of some of the more standard methods for a 
 * chronicle's data model, including event handling.
 */
public abstract class AbstractChronicleDataModel implements ChronicleDataModel {
	private EventListenerList dataListeners = new EventListenerList();
	protected boolean changing = false;
	
	/**
	 * Fires a structural change event from the given source.
	 * @param src the source chronicle
	 */
	protected void fireStructureChange() {
		if (changing)
			throw new IllegalStateException();
		changing = true;
		try {
			Object[] listeners = dataListeners.getListenerList();
			ChronicleEvent e = null;
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (e == null) {
					e = new ChronicleEvent(this);
				}
				((ChronicleModelListener) listeners[i+1]).structureChanged(e);
			}
		} finally {
			changing = false;
		}
	}
	
	/**
	 * Indicates that a timeline has changed in value or 
	 * range.
	 * @param src the source chronicle
	 */
	protected void fireTimeLinesChanged() {
		if (changing)
			throw new IllegalStateException();
		changing = true;
		try {
			Object[] listeners = dataListeners.getListenerList();
			ChronicleEvent e = null;
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (e == null) {
					e = new ChronicleEvent(this);
				}
				((ChronicleModelListener) listeners[i+1]).timeLinesChanged(e);
			}
		} finally {
			changing = false;
		}
	}
	/**
	 * Indicates that one or more lines has been added to the 
	 * data model
	 * @param src the source chronicle
	 */
	protected void fireTimeLinesAdded() {
		if (changing)
			throw new IllegalStateException();
		changing = true;
		try {
			Object[] listeners = dataListeners.getListenerList();
			ChronicleEvent e = null;
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (e == null) {
					e = new ChronicleEvent(this);
				}
				((ChronicleModelListener) listeners[i+1]).timeLinesAdded(e);
			}
		} finally {
			changing = false;
		}
	}
	
	/**
	 * Indicates that one or more lines has been removed from the 
	 * data model
	 * @param src the source chronicle
	 */
	protected void fireTimeLinesRemoved() {
		if (changing)
			throw new IllegalStateException();
		changing = true;
		try {
			Object[] listeners = dataListeners.getListenerList();
			ChronicleEvent e = null;
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (e == null) {
					e = new ChronicleEvent(this);
				}
				((ChronicleModelListener) listeners[i+1]).timeLinesRemoved(e);
			}
		} finally {
			changing = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addChronicleModelListener(ChronicleModelListener cl) {
		dataListeners.add(ChronicleModelListener.class, cl);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeChronicleModelListener(ChronicleModelListener cl) {
		dataListeners.remove(ChronicleModelListener.class, cl);
	}
}
