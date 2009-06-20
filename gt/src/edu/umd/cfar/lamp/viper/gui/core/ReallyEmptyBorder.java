/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.viper.gui.core;

import javax.swing.border.*;

/**
 * An empty border with zero margin size.
 */
public class ReallyEmptyBorder extends EmptyBorder {
	/**
	 * Constructs the empty border.
	 */
	public ReallyEmptyBorder() {
		super(0,0,0,0);
	}
}
