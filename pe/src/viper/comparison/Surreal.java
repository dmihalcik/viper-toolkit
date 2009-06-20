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

package viper.comparison;

/**
 * Not actually a Surreal number class (not defined recursively, 
 * doesn't look at infinitesimals) but a useful hack for an optimization
 * problem.
 *
 * Since the distances can go to infinity, and I need to find the minimum sum,
 * this is used to minimize the sum without too much hassle. I remember
 * something Gasarch said about monotonically decreasing functions, and also
 * saying "I hate you infinity plus one!" running through my head when I
 * wrote this,
 * 
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 */
class Surreal implements Cloneable
{
  /** The number of infinities. */
  private int infinities;
  /** The sum of all the real values (except the infinities).*/
  private double reality;

  /**
   * Constructs a new <code>Surreal</code> set to zero.
   */
  public Surreal ()
  {
    infinities = 0;
    reality = 0.0;
  }

  /**
   * Constructs a new <code>Surreal</code> set to the given value.
   * If it is infinity, it is the same as <code>new Surreal (1, 0.0)</code>.
   *
   * @param d The value to give it.
   */
  public Surreal (double d)
  {
    infinities = 0;
    reality = 0.0;

    if (d == Double.POSITIVE_INFINITY)
      infinities++;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities--;
    else
      reality += d;
  }

  /**
   * Constructs a new <code>Surreal</code> set to the given value.
   *
   * @param i The number of times infinity.
   * @param d The value of the double field.
   */
  public Surreal (int i, double d)
  {
    infinities = i;
    reality = 0.0;

    if (d == Double.POSITIVE_INFINITY)
      infinities++;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities--;
    else
      reality += d;
  }

  /**
   * Constructs a new <code>Surreal</code> set to the given value.
   *
   * @param S The <code>Surreal</code> to copy.
   */
  public Surreal (Surreal S)
  {
    infinities = S.infinities;
    reality = S.reality;    
  }

  /**
   * Sets <code>this</code> to the given value.
   *
   * @param i The number of times infinity.
   * @param d The value of the double field.
   * @return A reference to <code>this</code>.
   */
  public Surreal set (int i, double d)
  {
    infinities = i;
    reality = 0.0;

    if (d == Double.POSITIVE_INFINITY)
      infinities++;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities--;
    else
      reality += d;
    return this;
  }

  /**
   * Sets <code>this</code> to the given double.
   *
   * @param d The value of the double field.
   * @return A reference to <code>this</code>.
   */
  public Surreal set (double d)
  {
    infinities = 0;
    reality = 0.0;

    if (d == Double.POSITIVE_INFINITY)
      infinities++;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities--;
    else
      reality += d;
    return this;
  }

  /**
   * Sets <code>this</code> to the given value.
   *
   * @param S The <code>Surreal</code> to copy into <code>this</code>.
   * @return A reference to <code>this</code>.
   */
  public Surreal set (Surreal S)
  {
    infinities = S.infinities;
    reality = S.reality;    
    return this;
  }

  /**
   * Adds the given values to <code>this</code>.
   * 
   * @param i The number of infinities to add.
   * @param d The double value to add to this Surreal.
   * @return A reference to <code>this</code>.
   */
  public Surreal add (int i, double d)
  {
    if (d == Double.POSITIVE_INFINITY)
      infinities++;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities--;
    else
      reality += d;
    infinities += i;
    return this;
  }

  /**
   * Adds the given double to <code>this</code>.
   * 
   * @param d The double value to add to this Surreal.
   * @return A reference to <code>this</code>.
   */
  public Surreal add (double d)
  {
    if (d == Double.POSITIVE_INFINITY)
      infinities++;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities--;
    else
      reality += d;
    return this;
  }

  /**
   * Adds the given Surreal to <code>this</code>.
   * 
   * @param S The <code>Surreal</code> to add.
   * @return A reference to <code>this</code>.
   */
  public Surreal add (Surreal S)
  {
    infinities += S.infinities;
    reality += S.reality;
    return this;
  }

  /**
   * Subtracts the given values from <code>this</code>.
   *
   * @param d The double value to subtract.
   * @param i The number of infinities to remove.
   * @return A reference to <code>this</code>.
   */
  public Surreal subtract (int i, double d)
  {
    if (d == Double.POSITIVE_INFINITY)
      infinities--;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities++;
    else
      reality -= d;
    infinities -= i;
    return this;
  }

  /**
   * Subtracts the given double from <code>this</code>.
   *
   * @param d The double to subtract.
   * @return A reference to <code>this</code>.
   */
  public Surreal subtract (double d)
  {
    if (d == Double.POSITIVE_INFINITY)
      infinities--;
    else if (d == Double.NEGATIVE_INFINITY)
      infinities++;
    else
      reality -= d;
    return this;
  }

  /**
   * Subtracts the given Surreal from <code>this</code>.
   *
   * @param S The Surreal to find the difference with.
   * @return A reference to <code>this</code>.
   */
  public Surreal subtract (Surreal S)
  {
    infinities -= S.infinities;
    reality -= S.reality;
    return this;
  }

  /**
   * Returns a String representing this data, in parantheses.
   * @return <code>(<i># of infinities</i>oo, <i>double value</i>)</code>
   */
  public String toString ()
  {
    return ("(" + infinities + "oo, " + reality + " )");    
  }

  /**
   * Tests equality of two Surreals.
   * @param o The object to test against.
   * @return <code>true</code> if they are equal.
   */
  public boolean equals (Object o)
  {
    if (this == o)
      return true;
    else if (o instanceof Surreal) {
      Surreal S = (Surreal) o;
      return ((infinities == S.infinities)
	      && (reality == S.reality));
    } else {
      return false;
    }
  }

  /**
   * Returns a hashcode for this <code>Surreal</code> object.
   * The value is that of the number of infinities xored with
   * a hash value for the double value, which is taken from 
   * the java api, the exclusive OR of the two halves
   * of the long integer bit representation
   * @return <code>(int)(v^(v>>>32)) ^ infinities</code>
   */
  public int hashCode ()
  {
    long v = Double.doubleToLongBits(reality);
    return (int)(v^(v>>>32)) ^ infinities;
  }

  /**
   * Compares two Surreals.
   *
   * @param o The Surreal to compare this with.
   * @return A value of 0 if they are equal, less if the argument is greater 
   * than this Surreal, and more than 0 if this is the greater value.
   * @throws ClassCastException if the argument is not a <code>Surreal</code>.
   */
  public int compareTo (Object o) throws ClassCastException
  {
    if( !o.getClass().getName().equals( this.getClass().getName() ))
      throw( new ClassCastException() );
    return ((infinities == ((Surreal)o).infinities) 
	    ? Float.floatToIntBits((float) (reality - ((Surreal)o).reality))
	    : (infinities - ((Surreal)o).infinities));
  }

  /**
   * Generates a copy of this Surreal.
   * @return A new <code>Surreal</code> that is equal to <code>this</code>.
   */
  public Object clone ()
  {
    Surreal S = new Surreal (this);
    return S;
  }
}
