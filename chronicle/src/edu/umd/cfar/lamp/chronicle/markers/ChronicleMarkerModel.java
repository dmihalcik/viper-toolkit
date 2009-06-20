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

import viper.api.time.*;

/**
 * A set of marks, each representing an instant in time, that are
 * currently set on a chronicle view. These are represented as lines
 * drawn across the current interface, on top of the values. If preferred,
 * they may have handles drawn across the top (or bottom) of the screen, to
 * allow dragging and editing.  
 */
public interface ChronicleMarkerModel {
	/**
	 * Gets the ith marker.
	 * @param i the index of the marker to retrieve
	 * @return the marker
	 * @throws IndexOutOfBoundsException when i is out
	 * of bounds
	 */
	public ChronicleMarker getMarker(int i);
	
	/**
	 * Gets the count of markers.
	 * @return the number of markers in this model
	 */
	public int getSize();
	
	/**
	 * Creates a new marker. This is optional.
	 * @return the new marker
	 * @throws UnsupportedOperationException
	 */
	public ChronicleMarker createMarker();
	
	/**
	 * Removes a marker. This is optional.
	 * @param m the marker to remove
	 * @throws UnsupportedOperationException
	 */
	public void removeMarker(ChronicleMarker m);
	
	/**
	 * Removes a marker. This is optional.
	 * @param i the index of marker to remove
	 * @throws UnsupportedOperationException
	 */
	public void removeMarker(int i);
	
	/**
	 * Gets the interval the marker model describes.
	 * @return instants within this interval may be marked
	 */
	public InstantInterval getInterval();
	
	/**
	 * Sets the interval that may contain markers
	 * @param i the new interval
	 */
	public void setInterval(InstantInterval i);
	
	/**
	 * Returns the one of the markers closest to the given instant. 
	 * @param i the instant to look for
	 * @return a marker that is as close to or closer than each other
	 * marker to the given instant
	 */
	public ChronicleMarker getMarkerClosestTo(Instant i);

	/**
	 * Gets all marker labels.
	 * @return the set of all marker labels
	 */
	public Set getLabels();
	
	/**
	 * Removes all markers with the given label, and removes the 
	 * label from the set of labels.
	 * @param l the label to remove
	 */
	public void removeMarkersWithLabel(String l);
	
	/**
	 * Iterates over all markers with the given label.
	 * @param l the label to search for
	 * @return the markers with that label
	 */
	public Iterator getMarkersWithLabel(String l);

	/**
	 * Iterates over all markers
	 * @return Iterator over all the markers
	 */
	public Iterator markerIterator();

	/**
	 * Adds a listener for changes to the marker model.
	 * @param l the new listener
	 */
	public void addChronicleMarkerListener(ChronicleMarkerListener l);
	
	/**
	 * Removes a listener for changes to the marker model.
	 * @param l the listener to remove
	 */
	public void removeChronicleMarkerListener(ChronicleMarkerListener l);
}
