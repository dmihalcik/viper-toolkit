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
 * A simple subtree that can have multiple
 * children at each node, unlike the chain-version that
 * abstractViperSubtree supports.
 */
public abstract class AbstractMultipleViperSubTree extends AbstractViperSubTree {
	/**
	 * {@inheritDoc}
	 */
	public Sourcefile getFirstSourcefile() {
		Iterator iter = getSourcefiles();
		if (iter.hasNext()) {
			return (Sourcefile) iter.next();
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Config getFirstConfig() {
		Iterator iter = getConfigs();
		if (iter.hasNext()) {
			return (Config) iter.next();
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public AttrConfig getFirstAttrConfig() {
		Iterator iter = getAttrConfigs();
		if (iter.hasNext()) {
			return (AttrConfig) iter.next();
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Descriptor getFirstDescriptor() {
		Iterator iter = getDescriptors();
		if (iter.hasNext()) {
			return (Descriptor) iter.next();
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Attribute getFirstAttribute() {
		Iterator iter = getAttributes();
		if (iter.hasNext()) {
			return (Attribute) iter.next();
		}
		return null;
	}
}
