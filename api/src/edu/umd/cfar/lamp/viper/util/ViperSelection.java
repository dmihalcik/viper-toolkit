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

import viper.api.*;

/**
 * Simple selection interface for viper data.
 */
public interface ViperSelection {
	/**
	 * Tests to see if the given node is selected.
	 * @param n the viper data node to test
	 * @return <code>true</code> iff the node is selected
	 */
	public boolean isSelected(Node n);
	
	/**
	 * Adds a change listener to the selection. The listener
	 * is to be notified whenver the collection of selected objects
	 * changes.
	 * @param l an event listener
	 */
	public void addChangeListener(ChangeListener l);
	/**
	 * Removes the attached listener.
	 * @param l an event listener
	 */
	public void removeChangeListener(ChangeListener l);

	/**
	 * Gets a list of the attached listeners.
	 * @return all currently attached listeners
	 */
	public ChangeListener[] getChangeListeners();

	/**
	 * Tests to see if something is selected.
	 * @return if anything is selected
	 */
	public boolean isEmpty();
}
