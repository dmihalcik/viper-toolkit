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

package viper.api;

import java.util.*;

/**
 * In order to make accessing the ViPER data more straigtforward, the
 * Node interface acts as a datatype for treating ViPER data as a tree.
 */
public interface Node {
	
	/**
	 * Gets all the Node children of this Node.
	 * @return a java.util.Iterator through all the node's children
	 */
	public Iterator getChildren();
	
	/**
	 * Gets the parent node. Root nodes return <code>null</code>
	 * @return the node's parent
	 */
	public Node getParent();
	
	/**
	 * Gets the count of children attached to this node.
	 * @return The number of children currently attached to the node
	 */
	public int getNumberOfChildren();

	/**
	 * Gets the index of the given child node.
	 * @param n The node to search for 
	 * @return -1 if node does not have child n 
	 */
	public int indexOf(Node n);
	/**
	 * Checks to see if this node has the specified node among its
	 * children.
	 * @param n The node to check for.
	 * @return <code>true</code> if node <code>n</code> is a child of this node
	 */
	public boolean hasChild(Node n);
	
	/// Optional operations
	/**
	 * (Optional) Remove child at offset i.
	 * @param i child to remove
	 * @throws UnsupportedOperationException
	 */
	public void removeChild(int i);

	/**
	 * (Optional) Remove child n.
	 * @param n child to remove.
	 * @throws UnsupportedOperationException
	 */
	public void removeChild(Node n);
	/**
	 * (Optional) Sets the child at i to n.
	 * @param i the index of the child to replace
	 * @param n the new value of the child
	 * @throws UnsupportedOperationException
	 */
	public void setChild(int i, Node n);

	/**
	 * (Optional) Adds a child in some arbitrary position.
	 * @param n the node to add
	 * @throws UnsupportedOperationException
	 */
	public void addChild(Node n);
	
	/**
	 * Gets the i<sup>th</sup> child.
	 * @param i the index of the child to get
	 * @return the child
	 * @throws IndexOutOfBoundsException
	 */
	public Node getChild(int i);
	
	/**
	 * Gets the root of the tree.
	 * @return the node at the root of the tree.
	 */
	public ViperData getRoot();
	
	/**
	 * Gets the timestamp that indicates when this node, or one of its children,
	 * was most recently modified.
	 * @return
	 */
	public long getLastModifiedTime();
}
