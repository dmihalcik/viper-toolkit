/*
 * Created on Jun 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.awt.event.*;

/**
 * @author jnewman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Viewer extends Frame{
	private Image image;

	public Viewer(Image image) {
		this.image = image;
		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(image, 0);
		try
		{
			mediaTracker.waitForID(0);
		}
		catch (InterruptedException ie)
		{
			System.err.println(ie);
			System.exit(1);
		}
		addWindowListener(new WindowAdapter() {
      		public void windowClosing(WindowEvent e) {
        		System.exit(0);
      		}
		});
		setSize(image.getWidth(null) + 300, image.getHeight(null) + 300);
		setVisible(true);
	}

	public void paint(Graphics graphics) {
		graphics.drawImage(image, 0, 0, null);
	}

	public void setImage(Image i){
		image = i;
		repaint();
	}
	
	
	public static void main(String[] args) {
		return;
	}
}
