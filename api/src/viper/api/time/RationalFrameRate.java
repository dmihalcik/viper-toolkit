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

import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Stores a framerate and allows for conversion between Time
 * objects representing microseconds and Frame objects. This
 * assumes that Time zero is the start of the first frame, and that
 * the conversion can be accomplished by a rational multiplier.
 */
public class RationalFrameRate extends AbstractFrameRate implements Serializable {
	private boolean rational = false;
	
	/** The number of frames in a microt */
	private double rate;
	
	/** The number of frames in a microt as rational */
	private Rational rrate;

	/**
	 * Create a new FrameRate with the specified number of 
	 * frames per unit of time.
	 * @param rate the number of frames in a second
	 */
	public RationalFrameRate(double rate) {
		this.rate = rate;
		this.rational = false;
	}
	
	/**
	 * Creates a new frame rate from the given ratio of frames to seconds.
	 * @param frames number of frames
	 * @param seconds number of seconds
	 */
	public RationalFrameRate(long frames, long seconds) {
		this(new Rational(frames, seconds));
	}
	
	/***
	 * Takes the frame rate in directly as a rational number.
	 * @param fpn frames over time
	 */
	public RationalFrameRate(Rational fpn) {
		this.rrate = new Rational(fpn);
		this.rate = this.rrate.doubleValue();
		this.rational = true;
	}
	
	/**
	 * Gets the Frame corresponding to the instant.
	 * @param i the instant to cast to frame
	 * @return either the instant, or a copy of it as a frame
	 */
	public Frame asFrame(Time o) {
		long t = o.getTime();
		if (rational) {
			Rational r = new Rational();
			Rational.multiply(rrate, t, r);
			return new Frame(r.intValue()+1);
		} else {
			double f = t * rate;
			return new Frame(1+(int) Math.ceil(f));
		}
	}
		
	/**
	 * Converts the given instant into a Time object.
	 * @param o the instant to convert. May be Frame or Time based.
	 * @return the instant expressed as times
	 */
	public Time asTime(Frame o) {
		int f = o.getFrame()-1;
		if (rational) {
			Rational r = new Rational();
			Rational.multiply(rrate.reciprocate(), f, r);
			rrate.reciprocate();
			return new Time(r.longValue());
		} else {
			double t = f * rate;
			return new Time((long) Math.ceil(t));
		}
	}

	/**
	 * Returns the rate.
	 * @return double frames per unit of time
	 */
	public double getRate() {
		return rate;
	}
	
	/**
	 * Tests to see that the rates are equal.
	 * @param o the rate to compare to
	 * @return true if o is a framerate object describing the same 
	 * rate as <code>this</code>
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof RationalFrameRate) {
			return ((RationalFrameRate) o).getRate() == this.getRate();
		} else {
			return false;
		}
	}
	
	/**
	 * Returns a hash on this value.
	 * @return xor of the 32-bit segments of the double value of the rate
	 */
	public int hashCode() {
		long v = Double.doubleToLongBits(getRate());
		return (int) (v ^ (v >>> 32));
	}
	
	/**
	 * String representation of the frame rate.
	 * @return the ratio, followed by " fpt"
	 */
	public String toString() {
		if (rational) {
			return rrate + " fpt";
		} else {
			return rate + " fpt";
		}
	}
}
