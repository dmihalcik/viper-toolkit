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

package viper.api.extensions;

import viper.api.*;

/**
 * Sometimes it is useful to have a 'metadefault' value that
 * is not null. For example, the empty string or zero.
 */
public interface DefaultedAttrValueWrapper extends AttrValueWrapper {
	/**
	 * 
	 * @param container the attribute or attribute config that will refer
	 * to the value
	 * @return
	 */
	public Object getMetaDefault(Node container);
}
