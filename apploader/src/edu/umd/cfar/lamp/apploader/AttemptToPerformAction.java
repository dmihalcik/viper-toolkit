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

package edu.umd.cfar.lamp.apploader;

/**
 * A wrapper for a runnable. This is useful when you
 * have to ask the user if it is a good idea to do x, 
 * for example. Also, you may want to do this in process 
 * or in a seperate thread.
 */
public interface AttemptToPerformAction {
	/**
	 * Try, or maybe try, to run the given command.
	 * @param r the thing to try to run, if you choose
	 */
	public void attempt(Runnable r);
}
