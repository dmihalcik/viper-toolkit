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

package edu.umd.cfar.lamp.viper.gui.canvas;

import edu.umd.cs.piccolo.*;

/**
 * An interface to a tool to convert from a datatype Object
 * to a PNode.
 * 
 * @author davidm
 */
public interface ViewableAttribute {
	/**
	 * Gets a piccolo node representing the object, to be laid out
	 * on a ViperDataFrameView.
	 * 
	 * @param o
	 * @return PNode
	 */
	public PNode getViewable(Object o);
	
	/**
	 * In order to avoid unnecessary object creation, this method
	 * takes an old PNode that was returned with either 
	 * getViewable or this method and returns a reference to it,
	 * updating the internals at the time. Note that, like 
	 * toArray, the return reference is only to the same location
	 * as the old value if it is more convenient and efficient than
	 * creating a new object.
	 * 
	 * @param o   The current value of the attribute.
	 * @param old The old view node.
	 * @return PNode A new view node.
	 */
	public PNode updateViewable(Object o, PNode old);
}
