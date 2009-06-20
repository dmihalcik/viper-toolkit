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

import viper.api.*;

/**
 * A selection model for a single attribute.
 */
public class AttributeSelection extends AbstractSingleViperSubTree {
	private Attribute attribute;
	
	/**
	 * Sets the selected attribute. This also selects
	 * the path to the root from that attribute.
	 * @param attribute the attribute to select
	 */
	public void setAttribute(Attribute attribute) {
		if (this.attribute != attribute) {
			writeLock();
			try {
				this.attribute = attribute;
			} finally {
				writeUnlock();
			}
		}
	}
	
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getRoot()
	 */
	public ViperData getRoot() {
		if (attribute == null) return null;
		return attribute.getRoot();
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstSourcefile()
	 */
	public Sourcefile getFirstSourcefile() {
		if (attribute == null) return null;
		return (Sourcefile) attribute.getParent().getParent();
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstConfig()
	 */
	public Config getFirstConfig() {
		if (attribute == null) return null;
		return getFirstDescriptor().getConfig();
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstAttrConfig()
	 */
	public AttrConfig getFirstAttrConfig() {
		if (attribute == null) return null;
		return attribute.getAttrConfig();
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstDescriptor()
	 */
	public Descriptor getFirstDescriptor() {
		if (attribute == null) return null;
		return attribute.getDescriptor();
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstAttribute()
	 */
	public Attribute getFirstAttribute() {
		return attribute;
	}

	/** @inheritDoc */
	public boolean isEmpty() {
		return attribute == null;
	}
}
