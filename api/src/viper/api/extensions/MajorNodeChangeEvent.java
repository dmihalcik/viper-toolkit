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
 * An event that is actually an aggregation of subevents.
 * This event type is generated by a transaction.
 * @author davidm
 */
public interface MajorNodeChangeEvent extends ViperChangeEvent {
	/**
	 * Gets the subevents that make up this event.
	 * @return an iterator of ViperChangeEvents
	 */
	public Iterator getSubEvents();
}