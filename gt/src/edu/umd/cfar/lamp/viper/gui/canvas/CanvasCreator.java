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
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class CanvasCreator  extends PInputManager {
	private CreatorAssistant asst ;
	private Attribute attr ;
	
	Highlightable colorTable = HighlightSingleton.colorTable ;
	
	/**
	 * 
	 */
	public CanvasCreator( CreatorAssistant asst,
						  Attribute attr ) {
		this.asst = asst ;
		this.attr = attr ;
	}
	
//	public CreatorAssistant getCreatorAssistant()
//	{
//		return asst ;
//	}
	
	public abstract String getName() ;
	
	/**
	 * When the canvas loses focus, the shape should be "cancelled"
	 * For an obox, this means removing the partly constructed shape.
	 * For a bbox, this means the same.  For a polygon, it means to consider
	 * the current shape complete and add it (this is because of the difficulty
	 * of drawing a many sided polygon)
	 */
	public void stop() {
//		asst.switchListener() ; // switch from creator to editor
		asst.removeShape() ;
	}
	
	// Set the color and stroke when shape is selected
	public abstract void displaySelected() ;

	/**
	 * @return
	 */
	public Attribute getAttribute() {
		return attr;
	}

	/**
	 * @param attribute
	 */
	public void setAttribute(Attribute attribute) {
		attr = attribute;
	}

	/**
	 * @return
	 */
	public CreatorAssistant getAssistant() {
		return asst;
	}

	public void setAttrValueInMediator( Object val )
	{
		asst.setAttrValueInMediator( val, attr ) ;
	}

	public Highlightable getColorTable()
	{
		return colorTable ;
	}
}
