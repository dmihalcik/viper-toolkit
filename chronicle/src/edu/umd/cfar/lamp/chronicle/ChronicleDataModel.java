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
 * The data model used by the chronicle object.
 * It is fairly simple, seeing the world as a bag of 
 * timelines. There are several other models the chronicle 
 * also relies on to be effective, such as the emblem model,
 * which displays the interactive buttons next to the timeline
 * name, and the focus model, which indicates where the 
 * view should be placed and which parts of which lines are selected.
 */
public interface ChronicleDataModel {
	/**
	 * Iterate over all timelines in the model.
	 * @return the timelines
	 */
	public abstract Collection getTimeLines();

	/**
	 * Adds a change listener.
	 * @param cl a change listener
	 */
	public abstract void addChronicleModelListener(ChronicleModelListener cl);
	
	/**
	 * Removes a change listener.
	 * @param cl a change listener
	 */
	public abstract void removeChronicleModelListener(ChronicleModelListener cl);
}
