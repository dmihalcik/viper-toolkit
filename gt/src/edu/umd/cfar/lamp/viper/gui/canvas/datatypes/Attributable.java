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

package edu.umd.cfar.lamp.viper.gui.canvas.datatypes;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This is to be used on a class that extends PPath and implements Attributable.
 * It allows the Attribute and PPath to be kept togetber.
 *     setAttribute() is used to associate an attribute with the extended PPath
 *     getAttribute() returns back the attribute that was set
 *     getUpdatedAttribute() computes a current version of the attribute
 *        and returns it
 *        
 * @author clin
 *
 */
public interface Attributable {
	public void setAttribute( Attribute attr ) ;
	public Attribute getAttribute() ;
	
	/**
	 * Gets the current instant the 
	 * @return
	 */
	public Instant getInstant();
	
	public void setInstant(Instant i); 
	
	/**
	 * synchronize the PNode with the Attribute
	 * @return 
	 */
	public Object getUpdatedAttribute() ;
	
	/**
	 * @param properties
	 */
	public void setDisplayProperties(ShapeDisplayProperties properties);

	/**
	 * @param properties
	 */
	public void setHighlightDisplayProperties(ShapeDisplayProperties properties);

	/**
	 * @param properties
	 */
	public void setHandleDisplayProperties(ShapeDisplayProperties properties);
}
