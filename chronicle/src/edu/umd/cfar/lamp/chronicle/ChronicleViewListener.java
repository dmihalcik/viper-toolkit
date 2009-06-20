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
 * Listens for changes to the collection of visible
 * lines, their order, or other display properties. 
 */
public interface ChronicleViewListener extends EventListener {
	/**
	 * Indicates that the user focus changed.
	 * @param e the focus change event
	 */
	public abstract void focusChanged(EventObject e);
	
	/**
	 * Indicates that the data model has changed.
	 * @param e a data change event
	 */
	public abstract void dataChanged(EventObject e);
}