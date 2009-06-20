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
 * This is the change event that should be passed to ChangeListeners
 * that are attached to ViperSubTrees.
 */
public class ViperSubTreeChangedEvent extends ChangeEvent {
	private ViperSubTree removed;
	private ViperSubTree added;

	/**
	 * Creates a new event, describing the change that 
	 * just occured on the source subtree. There should be a 
	 * record of what was added and what was removed. 
	 * @param source the source of the change event
	 * @param removed the subtree removed
	 * @param added the subtree added
	 */
	public ViperSubTreeChangedEvent(ViperSubTree source, ViperSubTree removed, ViperSubTree added) {
		super(source);
		this.removed = removed;
		this.added = added;
	}
	
	/**
	 * Gets the subtree that was added.
	 * Note that there may be some overlap between the added
	 * and removed nodes.
	 * @return the added nodes
	 */
	public ViperSubTree getAddedNodes() {
		return added;
	}

	/**
	 * Gets the subtree that was removed.
	 * Note that there may be some overlap between the added
	 * and removed nodes.
	 * @return the removed nodes
	 */
	public ViperSubTree getRemovedNodes() {
		return removed;
	}
}
