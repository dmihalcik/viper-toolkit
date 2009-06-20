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

import viper.api.AttrConfig;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Apr 22, 2005
 *
 */
public interface LinkedAttrValueParser extends ExtendedAttrValueParser {
	public void setAttrConfig(AttrConfig ac);
}
