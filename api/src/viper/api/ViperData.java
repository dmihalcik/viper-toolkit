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
 * Classes that implement this interface act as the root for all 
 * manipulation of ViPER type metadata. A set of metadata is defined
 * by a set of Configs. The data itself is specific to a given media
 * file and is stored in a Sourcefile instance.
 */
public interface ViperData extends Node {
	/** The default qualifier to be used for the viper data namespace: "data:". */
	public static final String ViPER_DATA_QUALIFIER = "data:";
	/** The default qualifier to be used for the viper namespace: "". */
	public static final String ViPER_SCHEMA_QUALIFIER = "";

	/**
	 * Gets all of the configuration information for
	 * the metadata format.
	 * 
	 * @return <code>java.util.List</code> of {@link Config}s
	 */
	public List getAllConfigs();

	/**
	 * Gets all of the configuration information for
	 * the metadata format.
	 * 
	 * @return a java.util.Iterator of all {@viper.api.Config} type.
	 */
	public Iterator getConfigs();
	
	/**
	 * Gets an iterator over only descriptor 
	 * schemata of the given type.
	 * @param type e.g. <code>Config.CONTENT</code>
	 * @return an iterator over only descriptor 
	 * schemata of the given type
	 */
	public Iterator getConfigsOfType(int type);
	
	/**
	 * Gets the node that has, as its children, the
	 * schema nodes for each descriptor type.
	 * @return the Configs node
	 */
	public Configs getConfigsNode();

	/**
	 * Gets the node that has, as its children, the
	 * instance data for each described source media 
	 * file.
	 * @return the Sourcefiles node
	 */
	public Sourcefiles getSourcefilesNode();

	/**
	 * Gets all the sourcefiles.
	 * @return <code>java.util.List</code> of {@link Sourcefile}s
	 */
	public List getAllSourcefiles();

	/**
	 * Gets a all the sourcefiles.
	 * @return <code>java.util.Iterator</code> of {@link Sourcefile}s
	 */
	public Iterator getSourcefiles();
	/**
	 * Gets the configuration information for a given descriptor type.
	 * For example, <code>getConfig( Config.FILE, "Information" )</code>
	 * will get information about the attribute datatypes of the 
	 * FILE Information descriptor.
	 * 
	 * @param type Either <code>Config.FILE</code>, 
	 *   <code>Config.CONTENT</code> or <code>Config.OBJECT</code>
	 * @param name The name of the Descriptor configuration to 
	 *    retrieve.
	 * @return the config, if it exists, with the given
	 * name and type. <code>null</code> otherwise.
	 */
	public Config getConfig(int type, String name);

	/**
	 * Gets all of the metadata associated with the media file with
	 * the given name
	 * @param filename The name of the media file.
	 * @return Sourcefile object containing all of the metadata for the
	 *    specified file, or <code>null</code> if the file is not found.
	 */
	public Sourcefile getSourcefile(String filename);

	/**
	 * Creates a new Config object attached to this ViperData object.
	 * The Config object will have no attributes attached to it at
	 * first.
	 * 
	 * @param type Either <code>Config.FILE</code>, 
	 *   <code>Config.CONTENT</code> or <code>Config.OBJECT</code>
	 * @param name The name of the Descriptor configuration to 
	 *    retrieve.
	 * @return a Config object that describes the schema for a
	 *         descriptor with no attributes.
	 */
	public Config createConfig(int type, String name);

	/**
	 * Creates an empty sourcefile attached to this viper data root
	 * for the given file name.
	 * @param filename the name of the media file you wish to describe
	 * @return an empty set of metadata
	 * @throws IllegalArgumentException if <code>filename</code> is 
	 *    already in the data
	 */
	public Sourcefile createSourcefile(String filename);

	/**
	 * Removes the specified Sourcefile from the ViperData.
	 * @param filename the name of the media file you wish to remove
	 */
	public void removeSourcefile (String filename);

	/**
	 * The URI of the XML schema describing the 
	 * viper data format.
	 */
	public static final String ViPER_SCHEMA_URI = "http://lamp.cfar.umd.edu/viper#";
	
	/**
	 * The URI of the XML schema describing the 
	 * standard viper data types.
	 */
	public static final String ViPER_DATA_URI = "http://lamp.cfar.umd.edu/viperdata#";
}
