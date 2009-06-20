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

import java.util.*;

import viper.api.*;

/**
 * Event object that is passed whenever a node in a ViperData tree is 
 * modified.
 * The implementation should probably extend java.util.EventObject, 
 * but there seems to be no way to enforce that.
 */
public interface ViperChangeEvent {
	/**
	 * Gets the nearest parent to all changes to the data tree.
	 * @return the parent node, or <code>null</code> if this is
	 * a root or unattached node
	 */
	public Node getParent();
	/**
	 * Gets the source of the change.
	 * @return
	 */
	public Object getSource();
	
	/**
	 * Gets a unique id for the type of change. Note that 
	 * different instances of the same kind of change will
	 * all return the same URI.
	 * @return
	 */
	public String getUri();

	/**
	 * Gets the indeces of the changes beneath the parent.
	 * @return
	 */
	public int[] getIndexes();
	
	/**
	 * Gets a string-specified annotation of the event.
	 * @param prop the property name
	 * @return the property value, if found; 
	 * <code>null</code>, otherwise
	 */
	public Object getProperty(String prop);
	
	/**
	 * Lists all the property names associated with the event.
	 * @return the property names
	 */
	public Iterator listProperties();
}
