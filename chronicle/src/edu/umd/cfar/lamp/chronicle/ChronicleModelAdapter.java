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
 * Implements no-op methods for the ChronicleListener interface.
 */
public class ChronicleModelAdapter implements ChronicleModelListener {
	/**
	 * {@inheritDoc}
	 */
	public void timeLinesChanged(ChronicleEvent e) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void timeLinesAdded(ChronicleEvent e) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void timeLinesRemoved(ChronicleEvent e) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void structureChanged(ChronicleEvent e) {
	}
}
