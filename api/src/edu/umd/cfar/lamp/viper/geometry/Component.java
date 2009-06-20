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


package edu.umd.cfar.lamp.viper.geometry;

/**
 * Represents a homogonous vector. Represented as Rationals.
 */
public class Component
{
  /** The first scalar in the vector, representing the x dimension. */
  public Rational x;

  /** The second scalar in the vector, representing the y dimension. */
  public Rational y;

  /** The homogonizing scalar. */
  public Rational t;


  /**
   * Constructs a new Component.
   */
  public Component ()
  {
    x = new Rational(0);
    y = new Rational(0);
    t = new Rational(0);
  }

  /**
   * Constructs a new Component with the given coordinates in Rationals.
   * @param X The x coordinate.
   * @param Y The y coordinate.
   * @param T The homogonous coordinate.
   */
  public Component (Rational X, Rational Y, Rational T)
  {
    x = new Rational (X);
    y = new Rational (Y);
    t = new Rational (T);
    if ((!T.equals (0)) || (!T.equals (1)))
      homogenize ();
  }

  /**
   * Constructs a new Component with the given coordinates.
   * @param X The x coordinate.
   * @param Y The y coordinate.
   * @param T The homogonous coordinate.
   */
  public Component (int X, int Y, int T)
  {
    x = new Rational (X);
    y = new Rational (Y);
    t = new Rational (T);
    if ((T != 0) || (T != 1))
      homogenize ();
  }

  /**
   * Constructs a new Component from the given Component.
   * Acts as a copy constructor.
   * @param C The Component to copy.
   */
  public Component (Component C)
  {
    x = new Rational (C.x);
    y = new Rational (C.y);
    t = new Rational (C.t);
  }

  /**
   * Sets this component to the given value.
   *
   * @param C The value to set this to.
   * @return A reference to this Rational.
   */
  public Component set (Component C)
  {
    x.setTo (C.x);
    y.setTo (C.y);
    t.setTo (C.t);
    return this;
  }

  /**
   * Tests the equality of this with another Component.
   * @param o The object to test against this Component.
   * @return <code>true</code> if these are equal.
   */
  public boolean equals (Object o)
  {
    if (o instanceof Component) {
      Component C = (Component) o;
      return (x.equals (C.x) && y.equals (C.y) && t.equals (C.t));
    } else
      return false;
  }

  /**
   * Generates a hash code for this object.
   * @return A hash code for use in HashMaps and so on.
   */
  public int hashCode()
  {
    return x.hashCode() + y.hashCode() + t.hashCode();
  }

  /**
   * Returns the dot product of the two input vectors.
   * A dot product is:
   * <pre>
   *    a.x * b.x + a.y * b.y
   * </pre>
   *
   * @param a A Component to do some math on.
   * @param b A component to do some math on.
   * @return The dot product of the components.
   */
  static public Rational dot (Component a, Component b)
  {
    Rational temp1 = new Rational();
    Rational temp2 = new Rational();

    //a.x * b.x + a.y * b.y
    Rational.multiply (a.x, b.x, temp1);
    Rational.multiply (a.y, b.y, temp2);
    Rational.plus (temp1, temp2, temp1);
    return temp1;
  }

  /**
   * Subtract another Component from this Component.
   * @param C A Component to subtract from this.
   * @return The result of the subtraction.
   */
  public Component minus (Component C)
  {
    Rational resultT = new Rational ();
    Rational.minus (t, C.t, resultT);
    if (resultT.lessThan (1)) {
      Component retVal = new Component ();
      retVal.x = new Rational(); Rational.minus (x, C.x, retVal.x);
      retVal.y = new Rational(); Rational.minus (y, C.y, retVal.y);
      retVal.t = resultT;
      return retVal;
    } else {
      throw new ArithmeticException
      ("Error in format of 2 Dimensional Component");
    }
  }

  /**
   * Add another Component to this.
   * @param C The Component to add to this.
   * @return The sum of the two.
   * @throws ArithmeticException if you try to add two points or something.
   */
  public Component plus (Component C)
  {
    Rational resultT = new Rational ();
    Rational.plus (t, C.t, resultT);
    if (resultT.lessThan (1) || resultT.equals (1)) {
      Component retVal = new Component ();
      retVal.x = new Rational(); Rational.plus (x, C.x, retVal.x);
      retVal.y = new Rational(); Rational.plus (y, C.y, retVal.y);
      retVal.t = resultT;
      return retVal;
    } else {
      throw new ArithmeticException
      ("Error in format of 2 Dimensional Component");
    }
  }

  /**
   * Determine the length of the Component as a <code>double</code>.
   * @return The length of the vector or the distance from origin.
   */
  public double length ()
  {
    double tempX = x.doubleValue ();
    double tempY = y.doubleValue ();
    return Math.sqrt ((tempX * tempX) + (tempY * tempY));
  }

  /**
   * Make sure that T is one.
   * @return A reference to this.
   */
  private Component homogenize ()
  {
    t.reciprocate ();
    Rational.multiply (x, t, x);
    Rational.multiply (y, t, y);
    t.setTo (1);
    return this;
  }

  /**
   * Get a String representing this component. It doean't display T.
   * @return (x, y)
   */
  public String toString ()
  {
    return ("(" + x + " " + y + ")");
  }
}
