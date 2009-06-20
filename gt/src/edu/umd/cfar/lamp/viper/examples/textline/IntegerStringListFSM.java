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

import edu.umd.cfar.lamp.apploader.misc.*;

/**
 * Modeled on @{link 
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 21, 2005
 */

public class IntegerStringListFSM extends StringParserFSM {
	private StringBuffer sb;
	private IntegerListFSM delegate;
	private boolean finishedIntList = false;
	
	/**
	 * Makes a new FSM parser that will first look for <code>intCount</code>
	 * int parameters and then for one long free-form string (i.e. all chars allowed).
	 * 
	 * @param intCount - number of integers to parse
	 * @see edu.umd.cfar.lamp.viper.gui.data.IntegerListFSM
	 */
	public IntegerStringListFSM(int intCount) {
		sb = new StringBuffer();
		delegate = new IntegerListFSM(intCount);
	}
	
	public boolean pushDown(char c) {
		finishedIntList = finishedIntList || (delegate.isFinished() && !Character.isDigit(c));
		if (finishedIntList) {
			sb.append(c);
			return true;
		}
		return delegate.pushDown(c);
	}

	public void reset() {
		sb = new StringBuffer();
		delegate.reset();
		finishedIntList = false;
	}
	
	public String toString() {
		return delegate.toString() + sb.toString();
	}
	
	public String getValidString() {
		return delegate.getValidString() + sb;
	}
}
