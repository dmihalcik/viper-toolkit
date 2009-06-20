package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolox.swing.*;

/**
 */
public class ScrollableViperDataCanvas extends PScrollPane {
	private ViperDataCanvas interior;
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");
	
	public ScrollableViperDataCanvas() throws IOException {
		super (new ViperDataCanvas());
		interior = (ViperDataCanvas) super.getViewport().getView();
		this.setPreferredSize(interior.getPreferredSize());
	}

	public ViperDataCanvas getInterior() {
		return interior;
	}
	public ViperViewMediator getMediator() {
		return interior.getMediator();
	}

	/**
	 * @param canvas
	 */
	public void setInterior(ViperDataCanvas canvas) {
		interior = canvas;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) throws IOException {
		interior.setMediator(mediator);
	}
	
	/**
	 * @see ViperDataCanvas#getSetZoomLevelActionListener()
	 * @return Returns the setZoomLevelActionListener.
	 */
	public ActionListener getSetZoomLevelActionListener() {
		return interior.getSetZoomLevelActionListener();
	}
}
