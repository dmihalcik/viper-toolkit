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

/**
 * An interval that refers to a value; useful for 
 * specifying that an item exists over the given
 * Interval.
 * @author davidm
 */
public interface DynamicValue extends Interval {
	/**
	 * Gets the value associated with the interval.
	 * @return the value
	 */
	public Object getValue();
}
