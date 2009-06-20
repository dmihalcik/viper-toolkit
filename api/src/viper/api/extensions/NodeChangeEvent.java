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
 * Addition, removal, or alteration of a single node in the tree.
 * 
 * @author davidm
 */
public interface NodeChangeEvent extends ViperChangeEvent {
	/**
	 * Get the new node. This is necessary when undoing things.
	 * Note that the node will likely be unattached (i.e. getParent
	 * will return <code>null</code>).
	 * @return the new value
	 */
	public Node getNewValue();
	
	/**
	 * Gets the old value of the node.
	 * Note that the node will likely be unattached (i.e. getParent
	 * will return <code>null</code>).
	 * @return the old value
	 */
	public Node getOldValue();
	
	/**
	 * The index of the node from its parent.
	 * @return the index of the node
	 */
	public int getIndex();
}
