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

package edu.umd.cfar.lamp.chronicle.markers;

/**
 * Listens for changes to the selection of markers.
 */
public interface ChronicleMarkerSelectionListener {
	/**
	 * Indicates a change in the markers that are selected.
	 * @param e the change event
	 */
	public void selectionChanged(ChronicleMarkerSelectionEvent e);
}
