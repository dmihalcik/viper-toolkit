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

import java.util.*;

/**
 * Reports information about what markers the user currently has selected
 */
public interface ChronicleMarkerSelectionModel {
	/**
	 * Adds the given marker to the selection of markers.
	 * @param m the marker to select.
	 */
	public void addMarkerToSelection(ChronicleMarker m);
	
	/**
	 * Removes the marker from selection.
	 * @param m the marker to remove
	 */
	public void removeMarkerFromSelection(ChronicleMarker m);
	
	/**
	 * Lists all currently selected markers.
	 * @return the selected markers
	 */
	public ChronicleMarker[] getSelectedMarkers();
	
	/**
	 * Removes all of the given markers from the selection.
	 * @param markers the markers to remove
	 */
	public void removeMarkersFromSelection(Collection markers);
	
	/**
	 * Adds all of the given markers to the selection.
	 * @param markers the markers to add
	 */
	public void addMarkersToSelection(Collection markers);
	
	/**
	 * Adds a listener for changes to the selected markers.
	 * @param l the listener
	 */
	public void addChronicleMarkerSelectionListener(ChronicleMarkerSelectionListener l);

	/**
	 * Removes a listener for changes to the selected markers.
	 * @param l the listener
	 */
	public void removeChronicleMarkerSelectionListener(ChronicleMarkerSelectionListener l);
}
