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

import javax.swing.event.*;

import viper.api.time.*;

/**
 * Represents a selection of the chronicle, including both 
 * a temporal selection and a line selection.
 */
public interface ChronicleSelectionModel {
	
	/**
	 * Tests to see if the given line is selected
	 * @param tl the line to check
	 * @return <code>true</code> indicates the line is within the 
	 * user selection
	 */
	public boolean isSelected (TimeLine tl);
	
	/**
	 * Iterate over all selected lines.
	 * @return an iterator for all selected lines 
	 */
	public Iterator getSelectedLines ();
	
	/**
	 * Gets the range that is currently selected.
	 * @return the currently selected intervals
	 */
	public TemporalRange getSelectedTime();
	
	/**
	 * Adds a listener for changes to the selection
	 * @param listener will be alerted when the selection changes
	 */
	public void addChangeListener(ChangeListener listener);
	
	/**
	 * Gets all listeners for changes to the selection
	 * @return all listeners currently attached
	 */
	public ChangeListener[] getChangeListeners();

	/**
	 * Removes a listener for changes to the selection
	 * @param listener to be removed
	 */
	public void removeChangeListener(ChangeListener listener);
}
