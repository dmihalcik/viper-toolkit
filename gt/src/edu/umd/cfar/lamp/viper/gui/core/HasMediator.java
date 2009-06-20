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

/**
 * A useful helper interface for objects that have a reference to
 * the viper UI mediator.
 */
public interface HasMediator {
	
	/**
	 * Gets the currntly associated UI mediator.
	 * @return a viper ui mediator
	 */
	public ViperViewMediator getMediator();
}
