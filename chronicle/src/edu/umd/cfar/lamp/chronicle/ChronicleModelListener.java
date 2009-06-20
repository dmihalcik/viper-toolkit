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

/**
 * Listens for changes to the timelines.
 */
public interface ChronicleModelListener extends EventListener {
	/**
	 * Indicates that some lines have been changed.
	 * @param e the change event
	 */
	public abstract void timeLinesChanged(ChronicleEvent e);

	/**
	 * Indicates that some lines have been added.
	 * @param e the change event
	 */
	public abstract void timeLinesAdded(ChronicleEvent e);

	/**
	 * Indicates that some lines have been removed.
	 * @param e the change event
	 */
	public abstract void timeLinesRemoved(ChronicleEvent e);

	/**
	 * Indicates that some more major change, including
	 * any number of removals, additions, or changes.
	 * @param e the change event
	 */
	public abstract void structureChanged(ChronicleEvent e);
}