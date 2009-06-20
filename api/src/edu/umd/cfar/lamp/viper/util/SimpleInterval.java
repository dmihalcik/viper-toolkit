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
 * A simple immutable Interval of Comparable objects.
 * @author davidm
 */
public class SimpleInterval extends AbstractInterval implements Interval {
	private Comparable start;
	private Comparable end;
	/**
	 * Constructs a new immutable interval
	 * @param start the first element of the interval
	 * @param end the first element after the end of the interval
	 */
	public SimpleInterval(Comparable start, Comparable end) {
		this.start = start;
		this.end = end;
		assert getEnd() != null;
		assert getStart() != null;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.Interval#getStart()
	 */
	public Comparable getStart() {
		return start;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.Interval#getEnd()
	 */
	public Comparable getEnd() {
		return end;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.Interval#change(java.lang.Comparable, java.lang.Comparable)
	 */
	public Interval change(Comparable start, Comparable end) {
		return new SimpleInterval (start, end);
	}

}
