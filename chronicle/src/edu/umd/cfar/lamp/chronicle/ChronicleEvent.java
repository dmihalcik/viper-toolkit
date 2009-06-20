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
 * A ChronicleEvent is thrown by a model to indicate
 * changes. Since a Chronicle is a graph, it reports which
 * nodes are changed directly. So you have three sets of nodes:
 * removed nodes, altered nodes, and added nodes. Note that 
 * altered nodes are still .equals their old value, otherwise
 * they would count as an add/delete (for hashing purposes).
 */
public class ChronicleEvent extends EventObject {
	private TimeLine[] adds;
	private TimeLine[] alts;
	private TimeLine[] dels;

	/**
	 * Helper method for creating a new ChronicleEvent for 
	 * timeline removals
	 * @param src
	 * @param dels
	 * @return
	 */
	public static ChronicleEvent removal(Object src, Collection dels) {
		ChronicleEvent r = new ChronicleEvent(src);
		r.setRemovedLines(dels);
		return r;
	}

	/**
	 * Helper method for creating a new ChronicleEvent for 
	 * timeline additions.
	 * @param src the source of the event
	 * @param adds the added lines
	 * @return a new event describing the addition of adds to src
	 */
	public static ChronicleEvent addition(Object src, Collection adds) {
		ChronicleEvent r = new ChronicleEvent(src);
		r.setAddedLines(adds);
		return r;
	}

	/**
	 * Creates a new ChronicleEvent from the given
	 * source {@link ChronicleDataModel} and no changes.
	 * @param source
	 */
	public ChronicleEvent(Object source) {
		super(source);
		adds = new TimeLine[0];
		alts = new TimeLine[0];
		dels = new TimeLine[0];
	}
	
	/**
	 * Gets the lines that were added during this event. 
	 * @return the new lines
	 */
	public TimeLine[] getAddedLines() {
		return adds;
	}
	
	/**
	 * Gets the lines that were changed during this event. 
	 * @return the modified lines
	 */
	public TimeLine[] getChangedLines() {
		return alts;
	}
	
	/**
	 * Gets the lines that were removed during this event. 
	 * @return the old lines
	 */
	public TimeLine[] getRemovedLines() {
		return dels;
	}
	
	void setAddedLines(TimeLine[] adds) {
		this.adds = adds;
	}
	void setChangedLines(TimeLine[] alts) {
		this.alts = alts;
	}
	void setRemovedLines(TimeLine[] dels) {
		this.dels = dels;
	}
	void setAddedLines(Collection adds) {
		this.adds = (TimeLine[]) adds.toArray(new TimeLine[adds.size()]);
	}
	void setChangedLines(Collection alts) {
		this.alts = (TimeLine[]) alts.toArray(new TimeLine[alts.size()]);
	}
	void setRemovedLines(Collection dels) {
		this.dels = (TimeLine[]) dels.toArray(new TimeLine[dels.size()]);
	}
}
