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

import viper.api.*;
import edu.umd.cs.piccolo.*;

/**
 * Provides a way for a Creator class to display
 * its information on the canvas, through a single PNode.
 */
public interface CreatorAssistant {
	/**
	 * Adds shape to canvas so you can see it.
	 * @param node the shape you wish to display.
	 */ 
	public void addShape( PNode node );
		
	/**
	 * Removes the shape set in 'addShape'.
	 */
	public void removeShape();

	/**
	 * A helper method called to set the shape to the attribute value
	 * This will cause the mediator to reload everything on the frame,
	 * so we should remove the node. 
	 * @param obj the value to set in the attribute
	 * @param selectedAttr the attribute to modify.
	 */
	public void setAttrValueInMediator( Object obj, Attribute selectedAttr ) ;
	
	/**
	 * Switch to the editor.
	 *
	 */
	public void switchListener() ;
}
