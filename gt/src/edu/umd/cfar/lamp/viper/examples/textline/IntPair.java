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

package edu.umd.cfar.lamp.viper.examples.textline;

/**
 * Used to store a pair of integers in the occlusion list of TextlineModel
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 12, 2005
 *
 */
public class IntPair {
	private int x;
	private int y;
	
	/* 
	 * Default constructor
	 */
	public IntPair() {
		x = 0;
		y = 0;
	}
	
	public IntPair(int a, int b) {
		x = a;
		y = b;
	}

	public void setOne(int x) {
		this.x = x;
	}

	public int getOne() {
		return x;
	}

	public void setTwo(int y) {
		this.y = y;
	}

	public int getTwo() {
		return y;
	}
	
	/**
	 * Determines whether the given integer falls into the range represented by this IntPair
	 * Returns 0 if yes, -1 if it's lower than the range, and 1 if it's higher
	 * 
	 * @param i the integer to check
	 * 
	 */
	public int rangeContains(int i) {
		if(y < x) return 2; // TODO: throw exception/log something
		if(x <= i && i <= y) {
			return 0;
		} else {
			if(i < x) return -1;
			if(i > y) return 1;
		}
		return 2;
	}
	
	public boolean equals(IntPair b) {
		return (x == b.x && y == b.y);
	}
	
	public String toString() {
		return (x + " " + y);
	}
}
