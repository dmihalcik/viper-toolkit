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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * Displays the about information for the program.
 */
public class AboutDialog extends JDialog {
	private PrefsManager prefs;
	private JButton dismiss;
	private JTextArea text;
	
	/**
	 * @throws java.awt.HeadlessException
	 */
	public AboutDialog() throws HeadlessException {
		super();
		commonInit();
	}

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public AboutDialog(Dialog owner) throws HeadlessException {
		super(owner,true);
		commonInit();
	}
	
	private void commonInit() {
		dismiss = new JButton("Dismiss");
		dismiss.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}});
		text = new JTextArea("About This Program");
		text.setEditable(false);
		this.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER));
		this.getContentPane().add(text);
		this.getContentPane().add(dismiss);
	}


	/**
	 * @return
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * @param manager
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
		String name = "About " + prefs.getLocalizedString(LAL.Core, RDFS.label);
		String description = prefs.getLocalizedString(LAL.Core, DC_11.description);
		this.setTitle(name);
		this.text.setText(description);
	}
}
