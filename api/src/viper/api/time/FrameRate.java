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

/**
 * Stores a framerate and allows for conversion between Time
 * objects representing microseconds and Frame objects.
 */
public interface FrameRate extends Comparator {
	/**
	 * Gets the Frame corresponding to the instant.
	 * @param i the instant to cast to frame
	 * @return either the instant, or a copy of it as a frame
	 */
	public abstract Frame asFrame(Instant i);

	/**
	 * Converts the given interval into a Frame interval.
	 * @param s the interval to convert. May be Frame or Time based.
	 * @return the interval expressed as frames
	 */
	public abstract InstantInterval asFrame(InstantInterval s);

	/**
	 * Converts the given instant into a Time object.
	 * @param i the instant to convert. May be Frame or Time based.
	 * @return the instant expressed as times
	 */
	public abstract Time asTime(Instant i);

	/**
	 * Converts the given interval into a Time interval.
	 * @param s the interval to convert. May be Frame or Time based.
	 * @return the interval expressed as times
	 */
	public abstract InstantInterval asTime(InstantInterval s);
}