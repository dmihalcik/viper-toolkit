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

package edu.umd.cfar.lamp.viper.gui.core;

import java.util.*;

import viper.api.*;
import viper.api.time.*;

/**
 * Interface for the propagate/interpolate bean.
 */
public interface PropagateInterpolateModule {
	/**
	 * Listens for changes to the current propagation selection.
	 */
	public static interface PropagateListener {
		/**
		 * Indicates a change in the list of items (descriptors and 
		 * attributes) selected for propagation.
		 */
		public void listChanged();
	}
	
	/**
	 * Tests to see if this descriptor is on the propagation list.
	 * @param d the descriptor instance
	 * @return if the descriptor is propagating
	 */
	public abstract boolean isPropagatingThis(Descriptor d);
	
	/**
	 * Gets all propagating descriptors.
	 * @return the set of all propagating descriptors
	 */
	public abstract Set getPropagatingDescriptors();

	/**
	 * Adds a listener for changes to the list of items to 
	 * propagate.
	 * @param pl the new listener
	 */
	public void addPropagateListener(PropagateListener pl);
	
	/**
	 * Removes the propagation list listener.
	 * @param pl the listener to remove
	 */
	public void removePropagateListener(PropagateListener pl);


	/**
	 * Adds a descriptor to the propagate list.
	 * 
	 * @param desc
	 */
	public abstract void startPropagating(Descriptor desc);
	
	/**
	 * Stops all items from propagating.
	 */
	public abstract void stopPropagating();
	
	/**
	 * Removes the given descriptor from the propagation list.
	 * @param desc the descriptor to remove
	 */
	public abstract void stopPropagating(Descriptor desc);
	
	/**
	 * Adds the attribute to the propagation list.
	 * @param attr the attribute to modify
	 */
	public abstract void startPropagating(Attribute attr);
	
	/**
	 * Removes the attribute from the propagation list.
	 * @param attr the attribute to remove
	 */
	public abstract void stopPropagating(Attribute attr);

	/**
	 * Interpolates the value from start to the interval
	 * [start, end].
	 * @param start
	 * @param end
	 */
	public abstract void propagate(Instant start, Instant end);
	
	/**
	 * Interpolates all items on the propagation list.
	 * @param start the frame to use as the start of the interpolation
	 * @param end the frame or time to use as the end value of the 
	 * interpolation
	 */
	public abstract void interpolate(Instant start, Instant end);

	/**
	 * Propagates only the given descriptors, without reference to
	 * or modification of the propagation list.
	 * @param descs the descriptors to propagate
	 * @param start the value of the descriptor to use, and one end 
	 * of the propagation range
	 * @param end the last value to change
	 */
	public abstract void propagateDescriptors(
		Iterator descs,
		Instant start,
		Instant end);
	
	/**
	 * Propagates only the given attributes, without reference to
	 * or modification of the propagation list.
	 * @param ats the attributes to propagate
	 * @param start the value of the descriptor to use, and one end 
	 * of the propagation range
	 * @param end the last value to change
	 */
	public abstract void propagateAttributes(
		Iterator ats,
		Instant start,
		Instant end);

	/**
	 * Interpolates only the given descriptors, without reference to
	 * or modification of the propagation list.
	 * @param descs the descriptors to interpolate
	 * @param start the first value of to use, and one end 
	 * of the interpolation range
	 * @param stop the last value to use, and the end of the 
	 * interpolation range
	 */
	public abstract void interpolateDescriptors(
		Iterator descs,
		Instant start,
		Instant stop);
	
	/**
	 * Interpolates only the given attributes, without reference to
	 * or modification of the propagation list.
	 * @param ats the attributes to interpolate
	 * @param start the first value of to use, and one end 
	 * of the interpolation range
	 * @param stop the last value to use, and the end of the 
	 * interpolation range
	 */
	public abstract void interpolateAttributes(
		Iterator ats,
		Instant start,
		Instant stop);
}