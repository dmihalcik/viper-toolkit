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
 * A listener for changes to the marker model.
 */
public interface ChronicleMarkerListener extends EventListener {
	/**
	 * Invoked when a marker has changed.
	 * @param e event describing the type of change
	 */
	public void markersChanged (ChronicleMarkerEvent e);
}
