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
 * Sets the paramaters for a user-defined descriptor type.
 * A Config is analagous to a DS in MPEG-7, or a table schema in SQL.
 * It defines the attribute names and types of the descriptor, as well
 * as the descriptor name and type.
 *
 * There are three types of descriptor: File, Content, and Object.
 * They serve different purposes. File descriptors apply to an entire
 * file, containing such information as NUMFRAMES, FRAMERATE, etc.
 * Content descriptors apply to whole frames, describing events such
 * as cuts; only one Content descriptor with a given name can appear
 * at any moment in a file. Object descriptors can have dynamic 
 * values that change over time, and multiple Object instances can
 * appear at the same instance in time.
 * @see AttrConfig
 */
public interface Config extends Node {
	/**
	 * The FILE descriptor type. Only one of these may exist
	 * per file, and all of its attributes must be static.
	 */
	public static final int FILE = 1;

	/**
	 * The CONTENT descriptor type. Only one of any given
	 * defined type of CONTENT descriptor can exist on 
	 * a given frame, and its attributes must be static.
	 */
	public static final int CONTENT = 2;

	/**
	 * The OBJECT descriptor type.
	 */
	public static final int OBJECT = 3;

	/**
	 * Get the name of the descriptor.
	 * @return the descriptor name
	 */
	public String getDescName();

	/**
	 * Get the descriptor type.
	 * @return <code>FILE</code>, <code>OBJECT</code>, 
	 *       or <code>CONTENT</code>
	 */
	public int getDescType();

	/**
	 * Gets the configuration of each attribute attached to this
	 * descriptor. It is preferred to use {@link #getAttributeConfigs},
	 * as that allows more freedom in the implementation.
	 * @return a (possibly empty) <code>java.util.Collection</code> 
	 *         of <code>AttrConfig</code> objects
	 * @deprecated
	 */
	public Collection getAttrConfigs();
	
	
	/**
	 * Gets the configuration of each attribute attached to this
	 * descriptor.
	 * @return a (possibly empty) <code>java.util.Iterator</code> 
	 *         of <code>AttrConfig</code> objects
	 */
	public Iterator getAttributeConfigs();

	/**
	 * Gets a specific attribute configuration.
	 * @param name The attribute to get.
	 * @return the configuration for the attribute with the given name
	 * @throws NoSuchElementException if there is no attribute with
	 *     the given name
	 */
	public AttrConfig getAttrConfig(String name);

	/**
	 * Checks to see that a descriptor has an attribute 
	 * with the given name.
	 * @param name the name of the attribute to check for.
	 * @return <code>true</code> iff an attribute with the
	 *      specified name may exist in this descriptor.
	 */
	public boolean hasAttrConfig(String name);

	/**
	 * Creates a new attribute config attached to this Config instance.
	 * 
	 * @param name    the name for the new attribute, such as "FaceBox"
	 *                   or "Comment"
	 * @param type    the datatype to use, such as "http://viper#lvalue" or "bbox"
	 * @param dynamic <code>true</code> if the value can change over
	 *                   the life of an instance of a descriptor.
	 *                   Only valid for OBJECT descriptors.
	 * @param def     a default value
	 * @param params  any additional parameters that the data type
	 *                   must take, such as possible values for an
	 *                   lvalue
	 * @return the new <code>AttrConfig</code>, already attached to this Config
	 * @throws BadAttributeDataException if <code>def</code> is not
	 *    for attributes of type <code>type</code>
	 * @throws IllegalArgumentException if attaching a dynamic
	 *    attribute to a FILE or CONTENT <code>Config</code>, 
	 *    <code>type</code> is not a valid datatype, or
	 *    <code>params</code> does not apply to the datatype
	 */
	public AttrConfig createAttrConfig(
		String name,
		String type,
		boolean dynamic,
		Object def,
		AttrValueWrapper params)
		throws IllegalArgumentException;

	/**
	 * Gets an editor object for this node.
	 * Not all implementations of the API will be
	 * editable. Those versions should return <code>null</code>
	 * for this method.
	 * @return an editor object for this config.
	 */
	public Config.Edit getEditor();

	/**
	 * Interface for editing the properties of
	 * a descriptor <code>Config</code> object.
	 * Note that implementing these for live data
	 * is going to be a pain, so I am leaving the
	 * behaviour for that undefined at the moment.
	 * (For example, what happens when a descriptor
	 * for faces is turned into a FILE descriptor?
	 * Should any part of it be saved? Should it be 
	 * allowed?)
	 */
	public interface Edit {
		/**
		 * Set the descriptor type. This might
		 * have dangerous consequences for casting.
		 * 
		 * @param type OBJECT, CONTENT, etc.
		 */
		public void setDescType(int type);

		/**
		 * Set the descriptor name. You can't
		 * set it to an already existing name.
		 * 
		 * @param name The new descriptor name
		 */
		public void setDescName(String name);
	}
}
