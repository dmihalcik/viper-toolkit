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

package edu.umd.cfar.lamp.chronicle.markers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;

/**
 * The pop-up that shows when you right-click on a 
 * marker.
 */
public class ChronicleMarkerPopUp extends JPopupMenu {
	private PCanvas canvas;
	private ChronicleViewer viewer;
	private double halo;
	private ChronicleMarkerNode n;
	private JMenuItem removeMenuItem;
	private JMenuItem gotoMenuItem;
	
	private void initMenu() {
		removeMenuItem = new JMenuItem("Remove");
		removeMenuItem.addActionListener(new RemoveAction());
		
		gotoMenuItem = new JMenuItem("Go To");
		gotoMenuItem.addActionListener(new HotlinkAction());

//		JMenuItem mt = new JMenuItem("Move To...");
//		mt.addActionListener(new MoveToAction());

		super.add(gotoMenuItem);
		super.add(removeMenuItem);
	}
	
	/**
	 * Creates the marker popup with the given label
	 * @param label
	 */
	public ChronicleMarkerPopUp(String label) {
		super(label);
		initMenu();
	}

	/**
	 * Creates the marker popup menu with the default label
	 */
	public ChronicleMarkerPopUp() {
		super("Marker");
		initMenu();
	}
	
	/**
	 * Displays this popup at the specified location, refering to the
	 * given node.
	 * @param x the x-coordinate of the mouse click
	 * @param y the y-coordinate of the mouse click
	 * @param where the component to contain the popup
	 * @param n the node the popup should refer to/modify
	 */
	public void show(int x, int y, Component where, ChronicleMarkerNode n) {
		this.n = n;
		if (n != null) {
			boolean currFrame = n.getModel().getLabel().equals(ChronicleViewer.CURR_FRAME_LABEL);
			gotoMenuItem.setEnabled(!currFrame);
			removeMenuItem.setEnabled(!currFrame);
			super.show(canvas, x, y);
		}	
	}
	
	/**
	 * @return
	 */
	public double getHalo() {
		return halo;
	}

	/**
	 * @return
	 */
	public ChronicleViewer getViewer() {
		return viewer;
	}

	/**
	 * @param d
	 */
	public void setHalo(double d) {
		halo = d;
	}

	/**
	 * @param viewer
	 */
	public void setViewer(ChronicleViewer viewer) {
		this.viewer = viewer;
	}


	/**
	 * Removes the current marker from the set of markers, if possible.
	 * Note that this should not be available for the 
	 * major moment marker.
	 */
	private class RemoveAction implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			ChronicleMarker m = n.getModel();
			m.getParentModel().removeMarker(m);
		}
	}

	/**
	 * Sets now to the current marker's position.
	 */
	private class HotlinkAction implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			ChronicleMarker m = n.getModel();
			viewer.getModel().setMajorMoment(m.getWhen());
		}
	}
	/**
	 * @return Returns the canvas.
	 */
	public PCanvas getCanvas() {
		return canvas;
	}
	/**
	 * @param canvas The canvas to set.
	 */
	public void setCanvas(PCanvas canvas) {
		this.canvas = canvas;
		this.setInvoker(this.canvas);
	}
}
