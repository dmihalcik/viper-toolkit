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
 * A replacement for the <code>java.lang.Number</code>
 * class, which really should be an interface.
 * @author davidm
 */
public interface Numeric extends Comparable {
	/**
	 * Gets the closest or most appropriate long
	 * value for this number.
	 * @return the long approximation
	 */
	public long longValue();

	/**
	 * Gets the closest or most appropriate int
	 * value for this number.
	 * @return the int approximation
	 */
	public int intValue();

	/**
	 * Gets the closest or most appropriate double
	 * value for this number.
	 * @return the double approximation
	 */
	public double doubleValue();

	/**
	 * Gets the closest or most appropriate float
	 * value for this number.
	 * @return the float approximation
	 */
	public float floatValue();
}
