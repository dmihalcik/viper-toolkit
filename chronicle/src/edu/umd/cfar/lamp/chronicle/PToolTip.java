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

package edu.umd.cfar.lamp.chronicle;

/**
 * A holder for the tooltip property, which should be associated with
 * PNodes so that the ChronicleViewer knows to display the appropriate
 * tooltip message when the mouse hovers over a node that has one.
 */
public class PToolTip  {
	/**
	 * Set this property on your node in the chronicle viewer to get
	 * its tooltip to display.
	 */
	public static final String TOOLTIP_PROPERTY = "tooltip";
}
