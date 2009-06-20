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

package viper.api.extensions;

import viper.api.*;

/**
 * Nodes that support Events should implement this interface.
 * Note that events should propagate upward, meaning that
 * a parent's listeners will be called after a child's listeners,
 * with the same NodeChangeEvent.
 */
public interface EventfulNode extends Node {
	/**
	 * Adds a listener for changes to the node or its children.
	 * @param nl the node listener to add
	 */
	public void addNodeListener(NodeListener nl);

	/**
	 * Adds a listener for changes to the node or its children.
	 * @param nl the node listener to add
	 */
	public void removeNodeListener(NodeListener nl);
	
	/**
	 * Fires the given node change event, indicating that some
	 * of the children of the node have been added or removed.
	 * This probably shouldn't be part of the interface, but instead
	 * on an Abstract type.
	 * @param nce the change event
	 */
	public void fireNodeChanged(NodeChangeEvent nce);

	/**
	 * Fires the given minor node change event, indicating that
	 * some aspect of the node has changed.
	 * This probably shouldn't be part of the interface, but instead
	 * on an Abstract type.
	 * @param mnce the change event
	 */
	public void fireMinorNodeChanged(MinorNodeChangeEvent mnce);

	/**
	 * Fires the given major node change event, combining several
	 * other changes into one.
	 * This probably shouldn't be part of the interface, but instead
	 * on an Abstract type.
	 * @param mnce the change event
	 */
	public void fireMajorNodeChanged(MajorNodeChangeEvent mnce);

	/**
	 * @return
	 */
	public boolean isNotifyingListeners();
}
