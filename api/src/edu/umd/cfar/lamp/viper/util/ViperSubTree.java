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

package edu.umd.cfar.lamp.viper.util;

import java.util.*;

import viper.api.*;

/**
 * Represents some selection of elements from a viper tree.
 * This interface represents a looser interpretation of 
 * a viper tree as a DAG, with the <code>Config</code> and 
 * <code>AttrConfig</code> nodes being parallel to the 
 * <code>Sourcefile</code> node. 
 */
public interface ViperSubTree extends ViperSelection {
	/**
	 * Gets the root these elements are selected from.
	 * @return the root node
	 */
	public ViperData getRoot();

	/**
	 * Only nodes beneath some subset of the nodes of the
	 * specified type are selected.
	 * @param type one of the viper.api interfaces,
	 * e.g. <code>Sourcefile.class</code>
	 * @return <code>false</code> if there is no filtering
	 * by the given type (i.e. all children of selected
	 * parents are selected)
	 */
	public boolean isFilteredBy(Class type);

	/**
	 * Determines if the given type is allowed to have
	 * more than one selection.
	 * @param type one of the viper.api interfaces,
	 * e.g. <code>Sourcefile.class</code>
	 * @return <code>false</code> if only one, all, or no
	 * elements may be selected from the given type
	 */
	public boolean isMultipleSelectionAllowedOn(Class type);
	
	/**
	 * Gets the selections of the given viper node type.
	 * @param c the node type, e.g. {@link Sourcefile Sourcefile.class}
	 * @return an iterator over all, if any, nodes are specifically selected 
	 * in the tree of the given type
	 */
	public Iterator getSelectedBy(Class c);

	/**
	 * Get all the sourcefile nodes currently in the selection.
	 * @return the selected sourcefile nodes
	 */
	public Iterator getSourcefiles();

	/**
	 * Get all the descriptor configuration nodes currently in the selection.
	 * @return the selected descriptor configuration nodes
	 */
	public Iterator getConfigs();

	/**
	 * Get all the attribute configuration nodes currently in the selection.
	 * @return the selected attribute configuration nodes
	 */
	public Iterator getAttrConfigs();

	/**
	 * Get all the descriptor nodes currently in the selection.
	 * @return the selected descriptor nodes
	 */
	public Iterator getDescriptors();

	/**
	 * Get all the attribute nodes currently in the selection.
	 * @return the selected attribute nodes
	 */
	public Iterator getAttributes();
	
	/**
	 * Get all the first sourcefile node in the selection.
	 * @return the first selected sourcefile node
	 */
	public Sourcefile getFirstSourcefile();
	
	/**
	 * Get all the first descriptor schema node in the selection.
	 * @return the first selected descriptor schema node
	 */
	public Config getFirstConfig();
	
	/**
	 * Get all the first attribute schema node in the selection.
	 * @return the first selected attribute schema node
	 */
	public AttrConfig getFirstAttrConfig();
	
	/**
	 * Get all the first descriptor node in the selection.
	 * @return the first selected descriptor node
	 */
	public Descriptor getFirstDescriptor();
	
	/**
	 * Get all the first attribute node in the selection.
	 * @return the first selected attribute node
	 */
	public Attribute getFirstAttribute();
}
