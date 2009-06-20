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

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CanvasDir {
	String dirName ;
	boolean isStretched ;
	
	public static final CanvasDir TOP_LEFT 
		= new CanvasDir( "TOP_LEFT", false ) ;
	public static final CanvasDir TOP_RIGHT 
		= new CanvasDir( "TOP_RIGHT", false ) ;
	public static final CanvasDir BOTTOM_LEFT 
		= new CanvasDir( "BOTTOM_LEFT", false ) ;
	public static final CanvasDir BOTTOM_RIGHT 
		= new CanvasDir( "BOTTOM_RIGHT", false ) ;
	public static final CanvasDir LEFT 
		= new CanvasDir( "LEFT", true ) ;
	public static final CanvasDir RIGHT 
		= new CanvasDir( "RIGHT", true ) ;
	public static final CanvasDir TOP 
		= new CanvasDir( "TOP", true ) ;
	public static final CanvasDir BOTTOM 
		= new CanvasDir( "BOTTOM", true ) ;
	public static final CanvasDir INTERIOR 
		= new CanvasDir( "INTERIOR", true ) ;
	public static final CanvasDir NONE 
		= new CanvasDir( "NONE", false ) ;
	
	private CanvasDir( String name, boolean isStretched ) {
		dirName = name ;
		this.isStretched = isStretched ;
	}
	
	public boolean isStretched()
	{
		return isStretched ;
	}
	
	public String getName() 
	{
		return dirName ;
	}
	
	public String toString()
	{
		return dirName ;
	}
}


