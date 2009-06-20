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

package edu.umd.cfar.lamp.chronicle.markers;

import javax.swing.event.*;

/**
 * An abstract chronicle model that implementats
 * the event methods.
 */
public abstract class AbstractChronicleMarkerModel
		implements
			ChronicleMarkerModel {
	private EventListenerList markerListeners = new EventListenerList();
	protected boolean notifyingListeners = false;
	protected void fireMarkerEvent(ChronicleMarkerEvent e) {
		if (notifyingListeners)
			throw new IllegalStateException();
		notifyingListeners = true;
		try {
			Object[] listeners = markerListeners.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChronicleMarkerListener.class) {
					if (e == null)
						e = new ChronicleMarkerEvent(this);
					((ChronicleMarkerListener) listeners[i + 1])
							.markersChanged(e);
				}
			}
		} finally {
			notifyingListeners = false;
		}
	}
	protected void fireMarkerAdded(ChronicleMarker m) {
		if (markerListeners.getListenerCount() > 0) {
			assert m != null;
			ChronicleMarkerEvent e = new ChronicleMarkerEvent(this, m,
					ChronicleMarkerEvent.ADDED);
			fireMarkerEvent(e);
		}
	}
	protected void fireMarkerChanged(ChronicleMarker m) {
		if (markerListeners.getListenerCount() > 0) {
			assert m != null;
			ChronicleMarkerEvent e = new ChronicleMarkerEvent(this, m,
					ChronicleMarkerEvent.CHANGED);
			fireMarkerEvent(e);
		}
	}
	protected void fireMarkerRemoved(ChronicleMarker m) {
		if (markerListeners.getListenerCount() > 0) {
			assert m != null;
			ChronicleMarkerEvent e = new ChronicleMarkerEvent(this, m,
					ChronicleMarkerEvent.DELETED);
			fireMarkerEvent(e);
		}
	}
	protected void fireMarkerMoved(ChronicleMarker m) {
		if (markerListeners.getListenerCount() > 0) {
			assert m != null;
			ChronicleMarkerEvent e = new ChronicleMarkerEvent(this, m,
					ChronicleMarkerEvent.MOVED);
			fireMarkerEvent(e);
		}
	}
	
	protected void fireMultipleChangesEvent() {
		fireMarkerEvent(null);
	}
	
	/**
	 * @inheritDoc
	 */
	public void addChronicleMarkerListener(ChronicleMarkerListener l) {
		markerListeners.add(ChronicleMarkerListener.class, l);
	}
	
	/**
	 * @inheritDoc
	 */
	public void removeChronicleMarkerListener(ChronicleMarkerListener l) {
		markerListeners.remove(ChronicleMarkerListener.class, l);
	}
}