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

package viper.api.impl;

import viper.api.*;
import viper.api.extensions.*;

/**
 * Implements some of the common elements of a minor change event object.
 */
public abstract class AbstractMinorChange extends AbstractViperChangeEvent implements MinorNodeChangeEvent {
	protected Node source;
	protected String localName;
	protected int index;
	
	/**
	 * {@inheritDoc}
	 */
	public Node getParent() {
		return source.getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUri() {
		return ViperParser.IMPL+localName;
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getIndexes() {
		return new int[] {index};
	}

}
