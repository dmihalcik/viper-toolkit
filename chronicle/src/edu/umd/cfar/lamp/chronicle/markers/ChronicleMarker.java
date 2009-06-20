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

import viper.api.time.*;

/**
 * A single marker; it has a time (an {@link Instant}) and a variety of other
 * properties. 
 */
public interface ChronicleMarker {
	/**
	 * Gets the marked instant.
	 * @return the marked instant
	 */
	public Instant getWhen();
	
	/**
	 * Gets the marker model this is defined within.
	 * @return the parent model
	 */
	public ChronicleMarkerModel getParentModel();

	/**
	 * Sets the time of the marker
	 * @param i the new time
	 * @throws UnsupportedOperationException when not editable
	 */
	public void setWhen(Instant i);
	
	/**
	 * Tests to see if setWhen will work.
	 * @return <code>true</code> when the marker
	 * is mutable
	 */
	public boolean isEditable();
	
	/**
	 * Gets the tooltip for the marker. 
	 * @return the tooltip text
	 */
	public String getLabel();
	
	/**
	 * Sets the tooltip for the marker. 
	 * @param label the tooltip text
	 */
	public void setLabel(String label);
}
