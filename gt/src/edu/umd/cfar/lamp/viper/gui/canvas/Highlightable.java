/*
 * Created on Jun 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface Highlightable {
	Color getUnselectedColor() ;
	Stroke getUnselectedStroke() ;
	Color getSelectedColor() ;
	Stroke getSelectedStroke() ;
	Color getHighlightColor() ;
	Stroke getThickHighlightStroke() ;
	Stroke getMediumHighlightStroke() ;
	Stroke getThinHighlightStroke() ;
	Color getSelectedDisplayWRTColor() ;
	Color getUnselectedDisplayWRTColor() ;
	Stroke getDisplayWRTStroke() ;
	Color getUnselectedHandleColor() ;
	Color getHandleColor() ;
}
