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
 * This class takes in an array of Objects and runs
 * through all possible permutations.
 *
 * @see Combinator
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 */
public class Permuter
{
  /**
   * Gets the factorial of an integer.
   * @param x The integer.
   * @return x!
   */
  public static int factorial (int x)
  {
    int fact = 1;
    if (x <= 0)
      return 1;
    for (int i = 2; i < x; i++)
      fact *= i;
    return fact;
  }

  /** The string that is being permuted. */
  private Object[] code;
  /** Does the real permutaion. */
  private int[] lexicographer;
  /** Keeps track of the state a bit, indicating if the first permutation has been reported. */
  private boolean started = false;

  /**
   * Constructs a new Permuter to iterate over all
   * permutations of the given string.
   *
   * @param stringToPermute The Objects to rearrange.
   */
  public Permuter (Object[] stringToPermute)
  {
    code = stringToPermute;
    lexicographer = new int[code.length];
    for (int i = 0; i < lexicographer.length; i++)
      lexicographer[i] = i;
  }

  /**
   * Swap the data and references at the given indeces.
   * @param i Index of first item.
   * @param j Index of other item.
   */
  private void swap (int i, int j)
  {
    Object tempObj = code[i];
    int tempDex = lexicographer[i];
    code[i] = code[j];
    lexicographer[i] = lexicographer[j];
    code[j] = tempObj;
    lexicographer[j] = tempDex;
  }

  /**
   * Based on Dijkstra's method for doing this sort of thing. 
   * <pre>
   *    private void getNext()
   *    {
   *        int i = N - 1;
   *        while (Value[i-1] &gt;= Value[i])
   *            i = i-1;
   *
   *        int j = N;
   *        while (Value[j-1] &lt;= Value[i-1])
   *            j = j-1;
   *
   *        swap(i-1, j-1);
   *        i++; j = N;
   *        while (i &lt; j) {
   *            swap(i-1, j-1);
   *            i++;
   *            j--;
   *        }
   *    }
   * </pre>
   *
   * @return The next arrangement.
   */
  public Object[] getNextPermutation ()
  {
    int i = code.length - 1;
    int j = code.length;
    if (started) {
      while ((lexicographer[i - 1] >= lexicographer[i]) && (i > 0))
        i = i - 1;
      if (i == 0) {
        j = code.length - 1;
        while (i < j)
          swap(i++, j--);
        return code;
      }
      while (lexicographer[j - 1] <= lexicographer[i - 1])
        j = j - 1;

      /* swap values at positions (i-1) and (j-1) */
      swap(i - 1, j - 1);

      i++;
      j = code.length;

      while (i < j) {
        swap(i - 1, j - 1);
        i++;
        j--;
      }
    } else {
      started = true;
    }
    return code;
  }

  /**
   * Gets a string representation of the current permutation.
   * @return The current permutation bracketed and with spaces.
   */
  public String toString ()
  {
    StringBuffer sb = new StringBuffer ("[ ");
    for (int i = 0; i < code.length; i++)
      sb.append (code[i].toString()).append(" ");
    sb.append("]");
    return sb.toString();
  }
}

