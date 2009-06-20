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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.awt.*;

import javax.swing.*;

import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Mar 21, 2005
 *
 * Displays the preferences dialog that customizes
 * the Textline objects: font sizes, text positions, etc.
 * 
 */
public class TextlinePrefsDialog extends JPanel {
	private ViperViewMediator mediator;
	private JLabel test;
	
	public TextlinePrefsDialog() {
		test = new JLabel("Under Construction");
		add(test);
		setPreferredSize(new Dimension(320,240));
	}
	
	/**
	 * @return Returns the mediator.
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}
	/**
	 * @param mediator The mediator to set.
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}
}
