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

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Refers to a specific frame in a video.
 */
public class Frame extends Number implements Instant {
	protected int currFrame;
	
	/**
	 * The first possible frame represented by the system.
	 */
	public static Frame ALPHA = new Frame(Integer.MIN_VALUE);
	
	/**
	 * The last possible frame representable by this data type.
	 */
	public static Frame OMEGA = new Frame(Integer.MAX_VALUE);

	/**
	 * Constructs a Frame object for the given frame number.
	 * @param i the frame number
	 */
	public Frame(int i) {
		currFrame = i;
	}

	/**
	 * Parses a string in the form of an integer as a Frame.
	 * @param val an integer that represents a Frame.
	 * @throws IllegalArgumentException if val isn't a valid int
	 * @return new Frame representation of the value stored in the String
	 */
	public static Frame parseFrame(String val) {
		return new Frame(Integer.parseInt(val));
	}

	/**
	 * Gets the index of the frame.
	 * @return the index of the frame
	 */
	public int getFrame() {
		return currFrame;
	}

	/**
	 * {@inheritDoc}
	 */
	public Incrementable next() {
		return new Frame(currFrame + 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public Incrementable previous() {
		return new Frame(currFrame - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public long minus(Instant i) {
		return currFrame - ((Frame) i).currFrame;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		Frame that = (Frame) o;
		return this.currFrame < that.currFrame ? -1 : this.currFrame == that.currFrame ? 0 : 1;
	}

	

	/**
	 * If i is a Frame, tests to see that this
	 * is greater (later) than i.
	 * @param i the Frame to test against.
	 * @throws ClassCastException if i is not a Frame
	 * @return <code>true</code> iff this Frame is later than i
	 */
	public boolean isGreater(Instant i) {
		if (currFrame > ((Frame) i).currFrame)
			return true;
		return false;
	}
	/**
	 * If i is a Frame, tests to see that this
	 * is less (earlier) than i.
	 * @param t the Frame to test against.
	 * @throws ClassCastException if i is not a Frame
	 * @return <code>true</code> iff this Frame is earlier than i
	 */
	public boolean isLess(Instant t) {
		if (currFrame < ((Frame) t).currFrame)
			return true;
		return false;
	}



	/**
	 * True when the two frames have the same number
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (this == o
			|| (o instanceof Frame && currFrame == ((Frame) o).currFrame)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @return the frame number
	 */
	public int hashCode() {
		return currFrame;
	}

	/**
	 * {@inheritDoc}
	 * @return the frame number
	 */
	public String toString() {
		return String.valueOf(currFrame);
	}

	/**
	 * {@inheritDoc}
	 */
	public Instant go (long i) {
		return new Frame((int) (currFrame + i));
	}

	/**
	 * {@inheritDoc}
	 */
	public long longValue() {
		return getFrame();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int intValue() {
		return getFrame();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public double doubleValue() {
		return getFrame();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public float floatValue() {
		return getFrame();
	}
}
