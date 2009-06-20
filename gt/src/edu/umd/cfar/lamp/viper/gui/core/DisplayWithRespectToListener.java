/*
 * Created on May 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.core;

import java.util.*;

import javax.swing.event.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface DisplayWithRespectToListener extends EventListener {

	/**
	 * @param event
	 */
	public void displayWRTEventOccurred(ChangeEvent event) ;
}
