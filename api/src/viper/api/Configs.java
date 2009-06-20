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

/**
 * This is a placeholder to allow the ViPER API to be isomorphic
 * to the XML format, and allow each element, including the root
 * ViperData element, conform to the Node interface. Its children 
 * are all of the Config nodes, and its parent is a ViperData node.
 * It is a sibling with the Sourcefiles node.
 */
public interface Configs extends Node {
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
	 * @return the descriptor configuration, if it exists. Otherwise, returns <code>null</code>.
	 */
	public Config getConfig(int type, String name);
}
