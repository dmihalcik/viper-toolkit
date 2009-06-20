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

package viper.api.impl;

import viper.api.*;
import viper.api.extensions.*;

/**
 * Non-undoable implementation of the NodeChangeEvent.
 */
public class InternalNodeChangeEvent
	extends AbstractViperChangeEvent
	implements NodeChangeEvent {

	private Node parent;
	private Node oldValue;
	private Node newValue;
	private String lname;
	private int[] indexes;

	/**
	 * Creates a new NodeChangeEvent.
	 * @param parent   the node above the changed node
	 * @param oldValue the old value. <code>null</code> if the node is new.
	 * @param newValue the new value. <code>null</code> if the node is removec.
	 * @param oldIndex the offset into the children of the parent that this node is/was
	 * @param localName The local part of the event uri.
	 */
	public InternalNodeChangeEvent(Node parent, Node oldValue, Node newValue, int oldIndex, String localName) {
		this.parent = parent;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.indexes = new int[] {oldIndex};
		lname = localName;
	}
	
	

	/**
	 * @see NodeChangeEvent#getIndex()
	 */
	public int getIndex() {
		return indexes[0];
	}

	/**
	 * @see viper.api.extensions.ViperChangeEvent#getParent()
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * @see NodeChangeEvent#getNewValue()
	 */
	public Node getNewValue() {
		return newValue;
	}

	/**
	 * @see NodeChangeEvent#getOldValue()
	 */
	public Node getOldValue() {
		return oldValue;
	}

	/**
	 * @see viper.api.extensions.ViperChangeEvent#getUri()
	 */
	public String getUri() {
		return ViperParser.IMPL + lname;
	}

	/**
	 * @see viper.api.extensions.ViperChangeEvent#getIndexes()
	 */
	public int[] getIndexes() {
		return indexes;
	}

	/**
	 * @see viper.api.extensions.ViperChangeEvent#getSource()
	 */
	public Object getSource() {
		return getParent();
	}
}
