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

import java.math.*;

/**
 * This class takes in an array of Objects and runs
 * through all possible k-combinations. See 
 * <a href="http://www.caam.rice.edu/~dougm/twiddle/EnumComb.html">Doug Moore's Bit Twiddling</a>.
 * @see Permuter
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 */
public class Combinator
{
  /** The current combination. */
  private BigInteger current;

  /** The elements to keep in the set of possibilites. */
  private Object[] elements;

  /** The number of items in the combinations wanted. */
  private int k;

  /** Bookkeeping: says if got the first one yet. */
  private boolean started = false;

  /**
   * Constructs a new Combinator to iterate through all
   * combinations of the specified length on the given set.
   *
   * @param set The data to iterate over.
   * @param lengthOfPermutation The length of the sets to generate.
   */
  public Combinator (Object[] set, int lengthOfPermutation)
  {
    k = lengthOfPermutation;
    elements = set;
    current = (new BigInteger("1")).shiftLeft (k);
    current = current.subtract (new BigInteger("1"));
  }

  /**
   * Returns the least item in a combination.
   * eg if bits 2, 4, and 5 are set, returns an int with just bit 2 set (4).
   *
   * @param comb The combination to find the least set bit in.
   * @return A number with just one bit set, the least significant of those
   *    set in the parameter.
   */
  protected BigInteger leastItem (BigInteger comb)
  {
    return comb.and (comb.negate ()); // Ah, the beauty of 2's complement
  }

  /**
   * Returns the lexicographically next combination.
   * @return The next combination.
   */
  public Object[] getNextCombination ()
  {
    /* Generate next permutation bit string */
    if (!started) {
      started = true;
    } else {
      BigInteger hibit;
      BigInteger lobit = leastItem (current);
      current = current.add (lobit);
      hibit = leastItem (current);
      hibit = hibit.subtract (lobit);
      hibit = hibit.shiftRight (1 + hibit.getLowestSetBit ());
      current = current.or (hibit);
    }
    return getCurrentCombination ();
  }

  /**
   * Returns the most recently generated combination.
   * @return The last combination.
   */
  public Object[] getCurrentCombination ()
  {
    /* Convert the bit string into an array of objects */
    Object[] code = new Object[k];
    BigInteger temp = current;
    int offset = 0;
    for (int i = 0; i < k; i++) {
      int shifter = temp.getLowestSetBit ();
      offset += shifter;
      code[i] = elements[offset];
      temp = temp.shiftRight (shifter + 1);
      offset++;
    }
    return code;
  }

  /**
   * Prints out the last combination.
   * @return The last combination with french braces
   *          and comma seperated.
   */
  public String toString ()
  {
    StringBuffer sb = new StringBuffer ('{');
    Object[] code = getCurrentCombination();
    if (code.length == 0) {
      return "{}";
    } else if (code.length > 0) {
      sb.append ('{').append (code[0].toString ());
      for (int i = 1; i < code.length; i++)
        sb.append (", ").append (code[i].toString());
      sb.append ('}');
      return sb.toString();
    } else {
      return "Error in Combination";
    }
  }
}

