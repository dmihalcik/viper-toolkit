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

package edu.umd.cfar.lamp.viper.gui.core;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Checks to see if the given ViPER data node is currently visible
 * according to the filters. Note that hiding applies down
 * the heirarchy: a Config node can be marked 
 * as interesting even if none of its kids are, but an attribute
 * won't be interesting if its containing descriptor or describing
 * config isn't.
 * 
 * @author David Mihalcik
 */
public interface NodeVisibilityManager extends ViperSelection {
	
	/**
	 * A type visibility used to indicate that the node shouldn't be changed
	 * or displayed.
	 */
	public static final int HIDDEN  = 0;
	/**
	 * A type visibility used to indicate that the node should be
	 * displayed, but not editable. It might be a good idea to display
	 * these with some sort of annotation or other modification to indicate
	 * their state.
	 */
	public static final int LOCKED  = 1;
	
	/**
	 * Indicates that, while the data is unlocked, the range over which
	 * the data holds should be. XXX: Perhaps this should be replaced with
	 * a more general 'Constrained' type?
	 */
	public static final int RANGE_LOCKED = 2;
	
	/**
	 * Indicates the standard state of visibility.
	 */
	public static final int VISIBLE = 3;
	
	/** rotates between visible, locked and hidden states. 
	 * <code>state[i+1] = ROTATE_VISIBILITY[state[i]]</code>*/
	public static final int[] ROTATE_VISIBILITY = new int[] {VISIBLE, HIDDEN, HIDDEN, LOCKED};
	
	/** rotates between range_locked and visible states */
	public static final int[] ROTATE_RANGE_VISIBILITY = new int[] {HIDDEN, LOCKED, VISIBLE, RANGE_LOCKED};

	/**
	 * Reveal all the hidden nodes.
	 * @return if this changed the set of hidden nodes
	 */
	public abstract boolean showAll();
	
	/**
	 * Gets the types of descriptors that are hidden.
	 * @return the types, e.g. {@link Config#CONTENT}
	 */
	public abstract int[] getHidingTypes();
	
	/**
	 * Gets the descriptor types that are currently hidden.
	 * This doesn't include those configs whose type is
	 * hidden.
	 * @return the descriptor types that are hidden
	 */
	public abstract Config[] getHidingConfigs();
	
	/**
	 * Gets the hidden attribute configs.
	 * This won't return any of the ones whose parents are hidden.
	 * @return the specifically marked as hidden attribute types
	 */
	public abstract AttrConfig[] getHidingAttrConfigs();
	
	/**
	 * Gets the specifically hidden descriptor instances.
	 * This won't return any of the ones whose config is marked
	 * as hidden.
	 * @return the hidden descriptors.
	 */
	public abstract Descriptor[] getHidingDescriptors();
	
	/**
	 * Gets the specifically hidden attributes.
	 * This won't return any whose attribute config or 
	 * parent descriptor is hidden.
	 * @return the hidden attributes
	 */
	public abstract Attribute[] getHidingAttributes();
	
	/**
	 * Gets the visibility level of the descriptor class.
	 * @return the visibility of the requested item
	 */
	public abstract int getTypeVisibility(int type);
	
	/**
	 * Gets the visibility level of the descriptor class.
	 * @return the visibility of the requested item
	 */
	public abstract int getConfigVisibility(Config c);
	
	/**
	 * Gets the visibility level of the attribute type.
	 * @return the visibility of the requested item
	 */
	public abstract int getAttrConfigVisibility(AttrConfig ac);
	
	/**
	 * Gets the visibility level of the descriptor.
	 * @return the visibility of the requested item
	 */
	public abstract int getDescriptorVisibility(Descriptor d);
	
	/**
	 * Gets the visibility level of the attribute.
	 * @return the visibility of the requested item
	 */
	public abstract int getAttributeVisibility(Attribute a);

	/**
	 * Sets the visibility of the type of config.
	 * @param type the type, e.g. {@link Config#OBJECT}
	 * @param visible false for hidden
	 * @return if the set of hiders changed
	 */
	public abstract boolean setVisibilityByType(int type, int visible);
	
	/**
	 * Sets the visibility of the given class of descriptors.
	 * @param cfg the descriptor class
	 * @param visible false to hide
	 * @return if the set of hiders changed
	 */
	public abstract boolean setVisibilityByConfig(Config cfg, int visible);
	
	/**
	 * Sets the visibility of the given descriptor.
	 * @param desc the descriptor instance
	 * @param visible false to hide
	 * @return if the set of hiders changed
	 */
	public abstract boolean setVisibilityByDescriptor(
		Descriptor desc,
		int visible);
	
	/**
	 * Sets the visibility of the given attribute definition.
	 * @param acfg the set of attributes to hide
	 * @param visible false to hide
	 * @return if the set of hiders changed
	 */
	public abstract boolean setVisibilityByAttrConfig(
		AttrConfig acfg,
		int visible);
	
	/**
	 * Sets the visibility of the given attribute.
	 * @param a the attribute to hide or reveal
	 * @param visible false to hide
	 * @return if the set of hiders changed
	 */
	public abstract boolean setVisibilityByAttribute(
		Attribute a,
		int visible);
}