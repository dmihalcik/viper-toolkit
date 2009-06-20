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


package edu.umd.cfar.lamp.chronicle;

/**
 * Piccolo nodes that depend on the view length 
 * and height should implement this interface.
 */
public interface NodeWithViewLength {
	public void viewLengthChanged();
	public void dataHeightChanged();
	public void viewAndDataChanged();
}
