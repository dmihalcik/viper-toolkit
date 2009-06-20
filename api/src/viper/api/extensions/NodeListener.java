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

package viper.api.extensions;

import java.util.*;

/**
 * A NodeListener can be passed to the EventfulNode when it wants to
 * listen for events on that Node.
 * 
 * @author davidm
 */
public interface NodeListener extends EventListener {
	/**
	 * Invoked when a node to which the listener is attached gets an event.
	 * 
	 * @param nce a NodeChangeEvent representing what just happened.
	 */
	public void nodeChanged(NodeChangeEvent nce);

	/**
	 * Invoked when a node to which the listener is attached gets an event.
	 * 
	 * @param mnce a MinorNodeChangeEvent representing what just happened.
	 */
	public void minorNodeChanged(MinorNodeChangeEvent mnce);

	/**
	 * Invoked when a node to which the listener is attached gets an event.
	 * 
	 * @param mnce a MajorNodeChangeEvent representing what just happened.
	 */
	public void majorNodeChanged(MajorNodeChangeEvent mnce);
}
