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

import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * 
 */
public interface InstantInterval extends Interval {
	/**
	 * Gets the number of Instants in the Span. Basically,
	 * it returns <code>(int) getEnd().minus(getStart()) + 1</code>.
	 * @return the number of Instants in the Span.
	 */
	public abstract long width();

	/**
	 * Tests to see if the interval is expressed in Frames.
	 * @return if the interval is in frames
	 */
	public abstract boolean isFrameBased();

	/**
	 * Tests to see if the interval is expressed in time.
	 * @return if the interval is in time
	 */
	public abstract boolean isTimeBased();
	
	/**
	 * Gets the start value as an Instant, to avoid annoying casts.
	 * @see Interval#getStart()
	 * @return the start instant, inclusive
	 */
	public abstract Instant getStartInstant();
	
	/**
	 * Gets the end value as an Instant, to avoid annoying casts.
	 * @see Interval#getEnd()
	 * @return the end instant, exclusive
	 */
	public abstract Instant getEndInstant();
	
	/**
	 * Usually a bad idea, this method returns an iterator
	 * over all elements of the span. Avoid this 
	 * if you can, but it is useful if you have no choice 
	 * but to seek over all frames or, worse, microseconds
	 * of something.
	 * @return an iterator over all instants in the span
	 */
	public abstract Iterator iterator();
}