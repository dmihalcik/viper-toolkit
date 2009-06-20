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

package edu.umd.cfar.lamp.apploader.misc;

import java.awt.*;

import javax.swing.*;


/**
 * A JPanel with a Sourcefile Pulldown at the top.
 */
public class PanelWithToolbar extends JPanel {
	private JToolBar toolBar;
	private JComponent component;
	public PanelWithToolbar() {
		super(new BorderLayout());
		toolBar = new JToolBar();
		component = new JButton();
		helpRedoLayout();
	}
	private void helpRedoLayout() {
		this.removeAll();
		
        add(toolBar, BorderLayout.PAGE_START);
        add(component, BorderLayout.CENTER);
	}
	public JComponent getComponent() {
		return this.component;
	}
	public void setComponent(JComponent jc) {
		component = jc;
		helpRedoLayout();
	}
	public JToolBar getToolBar() {
		helpRedoLayout();
		return toolBar;
	}
	public void setToolBar(JToolBar toolBar) {
		this.toolBar = toolBar;
		helpRedoLayout();
	}
}
