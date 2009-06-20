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
 * An ordered set of three objects. It is sort-of-immutable, in that 
 * its reference elements cannot be switched with other elements.
 */
public class Triple extends Pair {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;

	private Object third;
	
	/**
	 * Constructs a new triple with the given objects.
	 * @param first the first element
	 * @param second the second element
	 * @param third the third, final element
	 */
	public Triple (Object first, Object second, Object third) {
		super (first, second);
		this.third = third;
	}
	
	/**
	 * Uses the toString method to concatenate the elements together
	 * in a space-seperated list. Note, it currently
	 * doesn't encode the spaces, which would be necessary to make 
	 * the output parseable, and nulls would probably have 
	 * to be different, somehow.
	 * @return the elements toString versions, concatenated.
	 */
	public String toString () {
		return getFirst() + " " + getSecond() + " " + getThird();
	}
	/**
	 * Gets a reference to the third element.
	 * @return reference to the third element
	 */
	public Object getThird () {
		return third;
	}
	/**
	 * Compares the elements of the triple with the elements of the target
	 * triple.
	 * @param o the object to compare with
	 * @return <code>true</code> if all the elements of each are equal
	 */
	public boolean equals (Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Triple) {
			Triple that = (Triple) o;
			return super.equals(o) && (third == null ? null == that.third : third.equals (that.third));
		} else {
			return false;
		}
	}
	/**
	 * The xor of the three elements of this.
	 * @see edu.umd.cfar.lamp.viper.util.Pair#hashCode()
	 */
	public int hashCode () {
		return super.hashCode() ^ (third == null ? 0 : third.hashCode());
	}
}
