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
package viper.api;

import viper.api.time.*;

/**
 * An interface for nodes that have associated 
 * ranges, such as dynamic attributes and 
 * OBJECT and CONTENT descriptors.
 */
public interface TemporalNode extends Node {
	/**
	 * Gets the range associated with the node.
	 * @return the range associated with the node.
	 */
	public TemporalRange getRange();
	
	/**
	 * Sets the range associated with the node.
	 * @param r the range over which the node
	 * is valid
	 */
	public void setRange(TemporalRange r);
}
