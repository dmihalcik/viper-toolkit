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

import java.math.*;
import java.util.*;

/**
 * Represents numbers as a fraction with two BigIntegers. Useful for
 * intersecting polygons and the like.
 */
public class Rational extends Number implements Comparable {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * This array holds the numerator and denominator
	 */
	private BigInteger[] bignum = new BigInteger[2];

	/**
	 * Tries to reduce the fraction, maybe moving from BigIntegers to ints.
	 * @return <code>this</code> after reduction.
	 */
	private Rational reduce() {
		// handle cases with zeroes
		if ((bignum[0].equals(BigInteger.ZERO))
			|| (bignum[1].equals(BigInteger.ZERO))) {
			int compVal = bignum[0].compareTo(BigInteger.ZERO);
			if (!bignum[1].equals(BigInteger.ZERO)) {
				bignum[1] = BigInteger.ONE;
			} else if (compVal < 0) {
				bignum[0] = BigInteger.ONE.negate();
				bignum[1] = BigInteger.ZERO;
			} else if (compVal > 0) {
				bignum[0] = BigInteger.ONE;
				bignum[1] = BigInteger.ZERO;
			} else {
				bignum[1] = bignum[1] = BigInteger.ZERO;
			}
			return this;
		}

		// keep the negative signs on the top
		if (bignum[1].compareTo(BigInteger.ZERO) < 0) {
			bignum[0] = bignum[0].negate();
			bignum[1] = bignum[1].negate();
		}

		// Reduce stuff
		if (!bignum[1].equals(BigInteger.ONE)) {
			BigInteger gcd = bignum[0].gcd(bignum[1]);
			if (!gcd.equals(BigInteger.ONE)) {
				bignum[0] = bignum[0].divide(gcd);
				bignum[1] = bignum[1].divide(gcd);
			}
		}
		return this;
	}

	/**
	 * Constructs a new Rational. Defaults to NaN.
	 */
	public Rational() {
		bignum[0] = bignum[1] = BigInteger.ZERO;
	}

	/**
	 * Constructs a new Rational with the given integer value.
	 * @param numerator Integer value to give the Rational.
	 */
	public Rational(long numerator) {
		bignum[0] = BigInteger.valueOf(numerator);
		bignum[1] = BigInteger.ONE;
	}

	/**
	 * Constructs a new Rational with the given value.
	 * @param numerator The number on top of the fraction.
	 * @param denominator The number on bottom.
	 */
	public Rational(long numerator, long denominator) {
		bignum[0] = BigInteger.valueOf(numerator);
		bignum[1] = BigInteger.valueOf(denominator);
		reduce();
	}

	/**
	 * Constructs a new Rational with the given value. (Copy Constructor.)
	 * @param old Rational value to give the new one.
	 */
	public Rational(Rational old) {
		bignum[0] = old.bignum[0];
		bignum[1] = old.bignum[1];
	}

	/**
	 * Set this to the given value.
	 * @param old Rational to set this to.
	 * @return <code>this</code> after getting set.
	 */
	public Rational setTo(Rational old) {
		bignum[0] = old.bignum[0];
		bignum[1] = old.bignum[1];
		return this;
	}

	/**
	 * Set this to the given value.
	 * 
	 * @param numerator
	 *            An integer value to set this to.
	 * @return <code>this</code> after getting set.
	 */
	public Rational setTo(long numerator) {
		bignum[0] = BigInteger.valueOf(numerator);
		bignum[1] = BigInteger.ONE;
		return this;
	}

	/**
	 * Set this to the given value.
	 * 
	 * @param numerator
	 *            The numerator of the fraction.
	 * @param denominator
	 *            The denominator of the fraction.
	 * @return <code>this</code> after getting set.
	 */
	public Rational setTo(long numerator, long denominator) {
		bignum[0] = BigInteger.valueOf(numerator);
		bignum[1] = BigInteger.valueOf(denominator);
		reduce();
		return this;
	}

	/**
	 * Gets a string representation. Either -inf, +inf, NAN, or the numerator /
	 * denominator.
	 * 
	 * @return The fraction as a String.
	 */
	public String toString() {
		if (bignum[1].equals(BigInteger.ONE)) {
			return bignum[0].toString();
		} else if (bignum[1].equals(BigInteger.ZERO)) {
			switch (bignum[0].compareTo(BigInteger.ZERO)) {
				case -1 :
					return "-inf";
				case 0 :
					return "NaN";
				case 1 :
					return "+inf";
				default :
					return "badValue";
			}
		} else
			return ("(" + bignum[0] + " / " + bignum[1] + ")");
	}

	/**
	 * Get a hashcode for this number.
	 * 
	 * @return The numerator xored with the denominator.
	 */
	public int hashCode() {
		return bignum[0].xor(bignum[1]).intValue();
	}

	/**
	 * Tests the equality of two Rationals or Numbers.
	 * 
	 * @param other
	 *            The object to compare against this rational number.
	 * @return <code>true</code> if they are equal.
	 */
	public boolean equals(Object other) {
		if (other instanceof Rational) {
			Rational rother = (Rational) other;
			return (
				bignum[0].equals(rother.bignum[0])
					&& bignum[1].equals(rother.bignum[1]));
		} else if (other instanceof Number) {
			return ((Number) other).doubleValue() == doubleValue();
		} else {
			return false;
		}
	}

	/**
	 * Tests the equality of this Rational with an int.
	 * 
	 * @param other
	 *            The int to compare against this rational number.
	 * @return <code>true</code> if they are equal.
	 */
	public boolean equals(int other) {
		if (!bignum[1].equals(BigInteger.ONE))
			return false;
		else
			return bignum[0].equals(BigInteger.valueOf(other));
	}

	/**
	 * Tests to see if the number is zero.
	 * @return <code>true</code> if the number is zero
	 */
	public boolean isZero() {
		return bignum[0].equals(BigInteger.ZERO)
			&& !bignum[1].equals(BigInteger.ZERO);
	}

	/**
	 * Tests to see if the number is negative.
	 * @return <code>true</code> if the number is less than zero
	 */
	public boolean isNegative() {
		return bignum[0].compareTo(BigInteger.ZERO) < 0;
	}

	/**
	 * Tests to see if the number is positive.
	 * @return <code>true</code> if the number is greater than zero
	 */
	public boolean isPositive() {
		return bignum[0].compareTo(BigInteger.ZERO) > 0;
	}

	/**
	 * Checks to see if this is less than another Rational.
	 * @param other the Rational to test against.
	 * @return <code>true</code> iff this is less than the other.
	 */
	public boolean lessThan(Rational other) {
		BigInteger[] otherNum = other.bignum;
		BigInteger[] thisNum = bignum;
		if ((thisNum[0].equals(BigInteger.ZERO)
			&& thisNum[1].equals(BigInteger.ZERO))
			|| (otherNum[0].equals(BigInteger.ZERO)
				&& otherNum[1].equals(BigInteger.ZERO))) {
			throw new ArithmeticException("0/0 error: " + this +" < " + other);

		} else if (
			thisNum[1].equals(BigInteger.ZERO)
				&& otherNum[1].equals(BigInteger.ZERO)) {
			return (thisNum[0].compareTo(otherNum[0]) < 0);

		} else if (otherNum[1].equals(BigInteger.ZERO)) {
			return (otherNum[0].compareTo(BigInteger.ZERO) > 0);

		} else if (thisNum[1].equals(BigInteger.ZERO)) {
			return (thisNum[0].compareTo(BigInteger.ZERO) < 0);

		} else if (thisNum[1].equals(otherNum[1])) {
			return thisNum[0].compareTo(otherNum[0]) < 0;
		} else {
			if (thisNum[1].signum() < 0) {
				this.reduce();
			}
			if (otherNum[1].signum() < 0) {
				other.reduce();
			}
			return (
				thisNum[0].multiply(otherNum[1]).compareTo(
					otherNum[0].multiply(thisNum[1]))
					< 0);
		}
	}

	/**
	 * Checks to see if this is less than an int.
	 * @param other the int to test against.
	 * @return <code>true</code> iff this is less than the other.
	 */
	public boolean lessThan(int other) {
		return lessThan(new Rational(other));
	}

	/**
	 * Checks to see if this is greater than an int.
	 * @param other the int to test against.
	 * @return <code>true</code> iff this is greater than the other.
	 */
	public boolean greaterThan(int other) {
		return this.greaterThan(new Rational(other));
	}

	/**
	 * Checks to see if this is greater than another Rational.
	 * @param other the number to test against.
	 * @return <code>true</code> iff this is greater than the other.
	 */
	public boolean greaterThan(Rational other) {
		return other.lessThan(this);
	}

	/**
	 * Checks to see if this is less than or equal to another Rational.
	 * @param other the number to test against.
	 * @return <code>true</code> iff this is less than or equal to the other.
	 */
	public boolean lessThanEqualTo(Rational other) {
		return !this.greaterThan(other);
	}

	/**
	 * Checks to see if this is greater than or equal to another Rational.
	 * @param other the number to test against.
	 * @return <code>true</code> iff this is greater than or equal to the other.
	 */
	public boolean greaterThanEqualTo(Rational other) {
		return !this.lessThan(other);
	}

	///////// Comparable //////////

	/**
	 * Compares to the other number. 
	 * @param o the number to compare against
	 * @return zero if they are equal, negative if o is greater, and positive if this is greater
	 */
	public int compareTo(Object o) {
		if (o == this) {
			return 0;
		} else if (o instanceof Rational) {
			Rational result = new Rational();
			Rational.minus(this, (Rational) o, result);
			return result.isZero() ? 0 : result.isPositive() ? 1 : -1;
		} else {
			double diff = doubleValue() - ((Number) o).doubleValue();
			return (diff == 0) ? 0 : (diff > 0) ? 1 : -1;
		}
	}

	/////////// Number ////////////

	/**
	 * Gets the <code>double</code> approximation of this rational.
	 * @return a <code>double</code> close to the value of <code>this</code>
	 */
	public double doubleValue() {
		BigInteger[] quotAndRem = bignum[0].divideAndRemainder(bignum[1]);
		double retval = quotAndRem[0].doubleValue();
		quotAndRem[1] =
			quotAndRem[1].multiply(
				BigInteger.valueOf(Integer.MAX_VALUE)).divide(
				bignum[1]);
		retval += quotAndRem[1].doubleValue() / Integer.MAX_VALUE;
		return retval;
	}

	/**
	 * Gets the <code>float</code> approximation of this rational.
	 * @return a <code>float</code> close to the value of <code>this</code>
	 */
	public float floatValue() {
		return (float) doubleValue();
	}

	/**
	 * Gets the <code>byte</code> approximation of this rational.
	 * @return the <code>byte</code> closest to the value of <code>this</code>
	 */
	public byte byteValue() {
		return (byte) intValue();
	}

	/**
	 * Gets the <code>short</code> approximation of this rational.
	 * @return the <code>short</code> closest to the value of <code>this</code>
	 */
	public short shortValue() {
		return (short) intValue();
	}

	/**
	 * Gets the <code>int</code> approximation of this rational.
	 * @return the <code>int</code> closest to the value of <code>this</code>
	 */
	public int intValue() {
		return (int) Math.floor(doubleValue());
	}

	/**
	 * Compute the floor as an integer.
	 * @return the floor. This is equivalent to 
	 * num / div, where they are both integers. 
	 */
	public BigInteger floor() {
		return bignum[0].divide(bignum[1]);
	}
	
	/**
	 * Compute the ceiling as an integer.
	 * @return the ceiling
	 */
	public BigInteger ceiling() {
		BigInteger[] divAndRem = bignum[0].divideAndRemainder(bignum[1]);
		if (divAndRem[1].intValue() == 0) {
			return divAndRem[0];
		}
		return divAndRem[0].add(BigInteger.ONE);
	}

	/**
	 * Gets the <code>long</code> approximation of this rational.
	 * @return the <code>long</code> closest to the value of <code>this</code>
	 */
	public long longValue() {
		return bignum[0].divide(bignum[1]).longValue();
	}

	///// Mathematical Functions ///////

	/**
	 * Subtracts one Rational from another and stores the result into a third.
	 * Does not clobber result, so you can pass it all the same reference and
	 * end up with twice the original just fine.
	 * 
	 * @param first
	 *            A Rational to add to.
	 * @param second
	 *            A Rational to add.
	 * @param result
	 *            Where to store the result.
	 */
	public static void plus(Rational first, Rational second, Rational result) {
		BigInteger[] A;
		BigInteger[] B;
		A = first.bignum;
		B = second.bignum;
		BigInteger tempDenom = A[1].multiply(B[1]);
		result.bignum[0] = (A[0].multiply(B[1]).add(B[0].multiply(A[1])));
		result.bignum[1] = tempDenom;
		result.reduce();
	}

	/**
	 * Subtracts one Rational from another and stores the result into a third.
	 * Keeps the order, so you can pass it all the same reference and end up
	 * with zero just fine.
	 * 
	 * @param first a Rational to subtract something from.
	 * @param second a Rational to subtract from the first.
	 * @param difference where to store the result.
	 */
	public static void minus(
		Rational first,
		Rational second,
		Rational difference) {
		Rational.plus(first, new Rational(second).negate(), difference);
	}

	/**
	 * Multiplies two Rationals and stores the result into a third. Keeps the
	 * order, so you can pass it all the same reference and end up with the
	 * square just fine.
	 * 
	 * @param first
	 *            A Rational to multiply.
	 * @param second
	 *            Another Rational to multiply.
	 * @param result
	 *            Where to store the result.
	 */
	public static void multiply(
		Rational first,
		Rational second,
		Rational result) {
		BigInteger[] A;
		BigInteger[] B;
		A = first.bignum;
		B = second.bignum;

		result.bignum[0] = A[0].multiply(B[0]);
		result.bignum[1] = A[1].multiply(B[1]);
		result.reduce();
	}

	/**
	 * Multiplies a Rational with a long and stores the result into another
	 * Rational. Keeps the order, so you can pass it all the same reference and
	 * end up fine.
	 * 
	 * @param first
	 *            A Rational to multiply.
	 * @param second
	 *            A long to multiply.
	 * @param result
	 *            Where to store the result.
	 */
	public static void multiply(Rational first, long second, Rational result) {
		result.bignum[0] = first.bignum[0].multiply(BigInteger.valueOf(second));
		result.bignum[1] = first.bignum[1];
		result.reduce();
	}

	/**
	 * Divides two Rationals and stores the result in a third. Keeps the order,
	 * so you can pass it all the same reference and end up with 1 just fine.
	 * 
	 * @param numerator
	 *            The thing on top, or divisor.
	 * @param denominator
	 *            The thing on the bottom, or dividend.
	 * @param quotient
	 *            Where to store the answer.
	 */
	public static void divide(
		Rational numerator,
		Rational denominator,
		Rational quotient) {
		BigInteger[] A;
		BigInteger[] B;
		A = numerator.bignum;
		B = denominator.bignum;

		BigInteger temp = A[0].multiply(B[1]);
		quotient.bignum[1] = A[1].multiply(B[0]);
		quotient.bignum[0] = temp;
		quotient.reduce();
	}

	/**
	 * Turns negates fraction upside-down, by negating its numerator.
	 * 
	 * @return Itself, now the opposite of what it was.
	 */
	public Rational negate() {
		bignum[0] = bignum[0].negate();
		return this;
	}

	/**
	 * Turns this fraction upside-down, by turning it to its reciprocal.
	 * 
	 * @return Itself, now upside-down.
	 */
	public Rational reciprocate() {
		BigInteger temp = bignum[1];
		bignum[1] = bignum[0];
		bignum[0] = temp;
		if (bignum[1].signum() < 0) {
			bignum[0] = bignum[0].negate();
			bignum[1] = bignum[1].negate();
		}
		return this;
	}

	/**
	 * Changes <code>this</code> to refer to its abolute value 
	 * @return <code>this</code>
	 */
	public Rational abs() {
		bignum[0] = bignum[0].abs();
		bignum[1] = bignum[1].abs();
		return this;
	}

	/**
	 * Square a Rational. Sets <code>this</code> to its square.
	 * 
	 * @return <code>this</code>, now squared.
	 */
	public Rational square() {
		Rational.multiply(this, this, this);
		reduce(); // Needed to check for overflow
		return this;
	}

	/**
	 * @param valStr
	 * @return
	 */
	public static Rational parseRational(String valStr) {
		StringTokenizer st = new StringTokenizer(valStr, " ./", true);
		if (!st.hasMoreTokens()) {
			return new Rational(0);
		}
		String first = st.nextToken();
		String wholePart = "";
		String fracPart = "";
		boolean emptyDec = ".".equals(first);
		boolean dec = emptyDec;
		if (!dec) {
			wholePart = first;
			String next = st.nextToken();
			dec = ".".equals(next);
			if ("/".equals(next)) {
				wholePart = "";
				fracPart = wholePart;
			} else {
				fracPart = st.nextToken();
				st.nextToken();
			}
		}
		Rational r = new Rational();
		if (dec) {
			fracPart = st.nextToken();
			BigInteger denom = new BigInteger("10").pow(fracPart.length());
			BigInteger num = new BigInteger(fracPart);
			r.bignum[0] = num;
			r.bignum[1] = denom;
			return r;
		} else {
			r.bignum[0] = new BigInteger(fracPart);
			r.bignum[1] = new BigInteger(st.nextToken());
		}
		if (wholePart.length() > 0) {
			BigInteger top = new BigInteger(wholePart);
			top = top.multiply(r.bignum[1]);
			r.bignum[0] = r.bignum[0].add(top);
		}
		return r;
	}
}
