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

import viper.api.time.*;


/**
 * An instance of some sort of metadata, an instantiation of a 
 * {@link Config}, that describes an event, object, or other abstract
 * of concrete data concerning a media file.
 * @see Config
 */
public interface Descriptor extends TemporalNode, Cloneable {
	/**
	 * Get the Config of this descriptor.
	 * @return the descriptor configuration
	 */
	public Config getConfig();

	/**
	 * Get the name of the descriptor.
	 * @return the descriptor name
	 */
	public String getDescName();

	/**
	 * Get the descriptor type.
	 * @see Config#FILE
	 * @see Config#CONTENT
	 * @see Config#OBJECT
	 * @return <code>Config.FILE</code>, <code>Config.OBJECT</code>, 
	 *       or <code>Config.</code>
	 */
	public int getDescType();

	/**
	 * Get the id of this instance.
	 * @return an integer that is unique for this descriptor Config
	 *         within a single sourcefile
	 */
	public int getDescId();

	/**
	 * Gets an {@link viper.api.time.InstantRange} which indicates 
	 * for which frames the descriptor is valid.
	 * @return range indicating for which frames this descriptor is 
	 *         valid
	 * @throws UnknownFrameRateException if this descriptor is stored
	 *   with respect to time and the sourcefile to which it is 
	 *   attached does not have a frame rate
	 */
	public InstantRange getValidRange();

	/**
	 * Gets all attributes for this descriptor. The preferred method
	 * is {@link #getAttributes}.
	 * @return <code>java.util.Collection</code> of 
	 *         {@link Attribute}s
	 * @deprecated
	 */
	public Collection getAttrList();
	
	/**
	 * Gets all attributes for this descriptor.
	 * @return <code>java.util.Iterator</code> of 
	 *         {@link Attribute}s
	 */
	public Iterator getAttributes();
	

	/**
	 * Gets a single attribute.
	 * @param name  the name of the attribute to retrieve
	 * @return the selected attribute, if it exists. Otherwise, returns <code>null</code>.
	 * @throws NoSuchElementException if there is no attribute with
	 *     the given name
	 */
	public Attribute getAttribute(String name);

	/**
	 * Gets a single attribute by AttrConfig.
	 * @param acfg the attribute to look for
	 * @return the selected attribute, if it exists. Otherwise, returns <code>null</code>.
	 */
	public Attribute getAttribute(AttrConfig acfg);

	/**
	 * Returns a deep copy of this descriptor, or the equivalent.
	 * It is possible that an implementation may offer copy-on-write
	 * semantics somehow, or implement a read-only api, in which case
	 * a deep copy is unnecessary / meaningless.
	 * @return a copy of the node
	 */
	public Object clone();

	/**
	 * Sets the Range, in Frame or Time, to the given Range. For new
	 * units of time, it allocates the default value to the dynamic 
	 * attributes (null if no default value).
	 * @param range the new range
	 * @throws UnknownFrameRateException
	 */
	public void setValidRange(InstantRange range) throws UnknownFrameRateException;

	/**
	 * Gets the sourcefile containing this descriptor.
	 * Shortcut for <code>(Sourcefile) desc.getParent()</code>.
	 * @return Sourcefile the media file that contains this descriptor
	 */
	public Sourcefile getSourcefile();
	
	/**
	 * Tells the descriptor whether to freeze changes to its interoplated over range.
	 * This causes set valid ranges and attribute changes to not remove a range from the 
	 * interp over range.   
	 * Should be set to true while doing interpolations, false otherwise.  
	 * @param b
	 */
	public void setFreezingInterp(boolean b);
	
	/**
	 * Notifies the descriptor that a change has occurred over the specified range.  This causes the descriptor
	 * to delete that range from its interpolated over range as long as setFreezingInterp(true) hasn't been
	 * called.  
	 * @param range
	 */
	public void notifyChangeOverRange(InstantInterval range);
	
	/**
	 * Gets the range of values where this descriptor has been interpolated over, and not set manually by the user
	 * @return The Interpolated range
	 */
	public InstantRange getInterpolatedOverRange();
	
	
	/**
	 * Sets the range where this descriptor has been interpolated over
	 * @param range The range where this descriptor has been interpolated over
	 */
	public void setInterpolatedOverRange(InstantRange range);
	
	/**
	 * Begins aggregating changes to the attribute, so that only one undo object 
	 * gets created.  Proper call order should be:
	 * startAggregating();
	 * changes to valid range, attribute changes
	 * finishAggregating(...);
	 */
	public void startAggregating();
	
	/**
	 * Ends an aggregate session, launching the undoable event
	 * @param undoable Whether to launch an undoable event - in case of an error, use false
	 */
	public void finishAggregating(boolean undoable);

}
