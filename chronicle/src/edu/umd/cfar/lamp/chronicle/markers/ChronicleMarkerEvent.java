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

import java.util.*;

/**
 * @author davidm
 */
public class ChronicleMarkerEvent extends EventObject {
	/**
	 * Indicates the event includes multiple changes, 
	 * such as additions or changes.
	 */
	public static final int MULTIPLE = -1;
	
	/**
	 * Indicates a marker has been removed.
	 */
	public static final int DELETED = 0;
	
	/**
	 * Indicates a marker has been moved.
	 */
	public static final int MOVED   = 1;

	/**
	 * Indicates a marker's style or name has changed, and that
	 * it may have been moved.
	 */
	public static final int CHANGED = 2;
	
	/**
	 * Indicates that a marker has been added.
	 */
	public static final int ADDED   = 3;
	
	private ChronicleMarker changedMarker;
	private int type;

	/**
	 * Construct a new {@link #MULTIPLE} event.
	 * @param source the source model
	 */
	public ChronicleMarkerEvent(ChronicleMarkerModel source) {
		super(source);
		changedMarker = null;
		type = MULTIPLE;
	}

	/**
	 * Construct a new single-marker event.
	 * @param source the source of the marker
	 * @param changed the marker that changed
	 * @param type the type of change undergone,
	 * e.g. {@link #DELETED} or {@link #MOVED}
	 */
	public ChronicleMarkerEvent(ChronicleMarkerModel source, ChronicleMarker changed, int type) {
		super(source);
		changedMarker = changed;
		this.type = type;
		if (type != MULTIPLE) {
			assert changed != null;
		}
	}
	
	/**
	 * Gets the changed marker. Node that {@link #MULTIPLE}
	 * events do not have one.
	 * @return the changed marker. <code>null</code> for 
	 * {@link #MULTIPLE} events.
	 */
	public ChronicleMarker getChangedMarker() {
		return changedMarker;
	}
	
	/**
	 * Gets the type of marker model change that occurred.
	 * @return the type of event, e.g. {@link #MULTIPLE}
	 * or {@link #DELETED}
	 */
	public int getType () {
		return this.type;
	}
}
