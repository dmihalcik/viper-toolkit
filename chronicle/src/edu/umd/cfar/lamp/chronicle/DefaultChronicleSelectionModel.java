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

import java.io.*;
import java.util.*;

import viper.api.time.*;

/**
 * Implements a simple selection model, composed of a 
 * set of timelines and n InstantRange.
 */
public class DefaultChronicleSelectionModel
		extends
			AbstractChronicleSelectionModel implements Serializable {
	protected InstantRange time;
	protected Set lines;
	
	/**
	 * Creates a new empty selection model
	 */
	public DefaultChronicleSelectionModel () {
		lines = new HashSet();
		time = new InstantRange();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSelected(TimeLine tl) {
		return lines.contains(tl);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getSelectedLines() {
		return lines.iterator();
	}
	/**
	 * {@inheritDoc}
	 */
	public TemporalRange getSelectedTime() {
		// XXX : should return immutable version
		return (TemporalRange) time.clone();
	}
	
	/**
	 * Adds the timeline to the selection.
	 * @param tl the timeline to add
	 */
	public void addTimeLine(TimeLine tl) {
		if (lines.add(tl)) {
			fireChangeEvent(null);
		}
	}
	
	/**
	 * Removes the timeline to the selection.
	 * @param tl the timeline to remove
	 */
	public void removeTimeLine(TimeLine tl) {
		if (lines.remove(tl)) {
			fireChangeEvent(null);
		}
	}
	
	/**
	 * Replaces the set of selected lines.
	 * @param lines the new set of selected lines
	 */
	public void setSelectedLinesTo(Set lines) {
		if (!lines.equals(this.lines)) {
			this.lines.clear();
			this.lines.addAll(lines);
			fireChangeEvent(null);
		}
	}
	
	/**
	 * Sets the selected set of instants.
	 * @param range the range to mark as selected
	 */
	public void setSelectedTimeTo(TemporalRange range) {
		if (!time.equals(range)) {
			time.clear();
			time.addAll(range);
			fireChangeEvent(null);
		}
	}
}
