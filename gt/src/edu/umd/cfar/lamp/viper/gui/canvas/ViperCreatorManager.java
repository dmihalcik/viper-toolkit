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

package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;

/**
 * 
 * @author clin
 */
public class ViperCreatorManager extends PInputManager {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");

	private ViperDataPLayer layer;

	public ViperCreatorManager(ViperDataPLayer layerIn) {
		layer = layerIn;
	}

	public void mouseClicked(PInputEvent e) {
		logger.finer("Mouse clicked");
		if (isDetached())
			return;
		pauseIfPossible();

		layer.getActiveCreator().mouseClicked(e);
	}

	/**
	 * This method pauses the mediator, if possible, and 
	 * is called by the mouse click and mouse pressed methods
	 * automatically. 
	 */
	public void pauseIfPossible() {
		ViperViewMediator m = layer.getMediator();
		if (m != null && m.getPlayControls() != null) {
			m.getPlayControls().humanPause();
		}
	}

	public void mousePressed(PInputEvent e) {
		if (isDetached())
			return;

		logger.finer("Mouse pressed " + position(e.getPosition()));
		pauseIfPossible();

		layer.getActiveCreator().mousePressed(e);
		//layer.invalidatePaint() ;
	}

	public void mouseReleased(PInputEvent e) {
		logger.fine("Mouse released " + position(e.getPosition()));
		if (isDetached())
			return;

		Object o = layer.getActiveCreator();
		pauseIfPossible();
		logger.fine("Mouse released 2 " + position(e.getPosition()));
		assert o == layer.getActiveCreator();
		layer.getActiveCreator().mouseReleased(e);
	}

	/**
	 * @return
	 */
	private boolean isDetached() {
		return layer.getActiveCreator() == null || layer.getActiveCreator().getAttribute().getRoot() == null;
	}

	public void mouseDragged(PInputEvent e) {
		// System.out.println( "Mouse dragged" ) ;
		if (isDetached())
			return;

		layer.getActiveCreator().mouseDragged(e);
	}

	public void mouseMoved(PInputEvent e) {
		//	System.out.println( "Mouse moved" ) ;
		if (isDetached())
			return;

		layer.getActiveCreator().mouseMoved(e);
	}

	public void keyPressed(PInputEvent event) {
		super.keyPressed(event);
		if (isDetached())
			return;

		layer.getActiveCreator().keyPressed(event);
	}

	public void keyReleased(PInputEvent event) {
		super.keyReleased(event);
		if (isDetached())
			return;

		layer.getActiveCreator().keyReleased(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.piccolo.event.PBasicInputEventHandler#keyTyped(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void keyTyped(PInputEvent event) {
		// TODO Auto-generated method stub
		super.keyTyped(event);

		if (isDetached())
			return;

		layer.getActiveCreator().keyPressed(event);
	}

	public String position(Point2D pt) {
		String s = "( " + pt.getX() + ", " + pt.getY() + " )";
		return s;
	}
}

