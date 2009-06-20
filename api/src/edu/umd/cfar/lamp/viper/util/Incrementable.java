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
 * Extension to Comparable objects that allow increment and 
 * decrement operations. This is useful for sorted maps, and other
 * systems that require an abstract increment procedure.
 * 
 * @author davidm
 */
public interface Incrementable extends Comparable {
	/**
	 * Gets the item following this one.
	 * Should have <code>true == a.next().prev().equals(a)</code>.
	 * @return Incrementable immediately following this one
	 */
	public Incrementable next();
	/**
	 * Gets the item before this one.
	 * Should have <code>true == a.prev().next().equals(a)</code>.
	 * @return Incrementable immediately prior to this one
	 */
	public Incrementable previous();
}
