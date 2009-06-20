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

package viper.api.time;

import java.io.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * An Instant represents a discrete quantum of the 
 * current time measurement. In the current current
 * paradigm, it is either a Frame or a microsecond.
 */
public interface Instant extends Incrementable, Numeric, Serializable {
	/**
	 * Gets the difference between this and another instance.
	 * @param i the amount to subtract
	 * @return <code>this.value - i.value</code>
	 * @throws ClassCastException if i is not the right type of Instant
	 */
	public long minus(Instant i);


	/**
	 * Returns an Instant diff away from this. For example,
	 * <code>i.go(0)</code> can return itself, and 
	 * <code>i.go(-1)</code> is equivalent to 
	 * <code>i.previous()</code>. 
	 *
	 * @param diff the number of instants to move
	 * @return Instant a new instant <code>diff</code> from this one 
	 */
	public Instant go(long diff);
}
