/*
 * Created on May 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.core;

import javax.swing.event.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Manages changes to and the current dwrt attribute.
 */
public class DisplayWithRespectToManager {
	private EventListenerList listenerList = new EventListenerList() ;
	private ChangeEvent event = null ;
	private Attribute attr = null ;
	private Instant instant = null ;
	
	/**
	 * Adds a listener for changes to which attribute/if an 
	 * attribute is selected for dwrt.
	 * @param listener the new listener
	 */
	public void 
	addDisplayWRTListener( DisplayWithRespectToListener listener )
	{
		listenerList.add(DisplayWithRespectToListener.class, listener ) ;
	}
	
	/**
	 * Removes the dwrt change listener.
	 * @param listener the listener to remove
	 */
	public void 
	removeDisplayWRTListener( DisplayWithRespectToListener listener )
	{
		listenerList.remove(DisplayWithRespectToListener.class, listener ) ;
	}
	
	protected void fireDisplayWRTEvent()
	{
		// This code is basically the code from Class EventListenerList
		// in the Java API (see java.sun.com and go to Java API page)
		Object [] listeners = listenerList.getListenerList() ;
		for ( int i = listeners.length - 2 ; i >= 0; i -= 2)
		{
			if ( listeners[i] == DisplayWithRespectToListener.class )
			{
				if ( event == null )
					event = new ChangeEvent( this ) ;
				((DisplayWithRespectToListener) listeners[i+1]).
				     displayWRTEventOccurred(event) ;	
			}
		}
	}
	
	
	/**
	 * Set the dwrt attribute, and the frame it was set on.
	 * @param a the new dwrt attribute. Set it to <code>null</code>
	 * to remove the dwrt
	 * @param inst the new instant
	 */
	public void setAttribute( Attribute a, Instant inst )
	{
		if ( attr != a )
		{
			attr = a ;
			instant = inst ;
			fireDisplayWRTEvent() ;
		}
	}

	/**
	 * Get the current attribute that things should be done
	 * with respect to.
	 * @return the dwrt attribute; <code>null</code> if none is 
	 * selected
	 */
	public Attribute getAttribute()
	{
		return attr ;
	}
	
	/**
	 * Tests to see if there is no dwrt attribute.
	 * @return if there is no dwrt attribute
	 */
	public boolean isAttributeNull()
	{
		return attr == null ;
	}
	
	/**
	 * Gets the dwrt point, for translation dwrt, even if the attribute
	 * doesn't have a value at the given point. This allows for
	 * consistancy when playing/editing where the value is null.
	 * @param i the instant to get the value at
	 * @return the projected/interpolated value
	 */
	public Pnt getSmoothedAttrValueAtInstant(Instant i) {
		if (attr == null) {
			return null;
		}
		if (attr.getAttrConfig().isDynamic()) {
			TemporalRange r = attr.getRange();
			if (r.isEmpty()) {
				return null;
			}
			if (r.contains(i)) {
				return ((HasCentroid) attr.getAttrValueAtInstant(i)).getCentroid();
			}
			Instant alpha = (Instant) r.firstBefore(i);
			Instant beta = (Instant) r.firstAfter(i);
			if (alpha != null) {
				alpha = (Instant) r.endOf(alpha);
				alpha = (Instant) alpha.previous();
			} else {
				// nothing before now
				return ((HasCentroid) attr.getAttrValueAtInstant(beta)).getCentroid();
			}
			if (beta == null) {
				// nothing after now
				return ((HasCentroid) attr.getAttrValueAtInstant(alpha)).getCentroid();
			}
			// interpolate between alpha and beta to point at i.
			Pnt a = ((HasCentroid) attr.getAttrValueAtInstant(alpha)).getCentroid();
			Pnt b = ((HasCentroid) attr.getAttrValueAtInstant(beta)).getCentroid();
			Rational x = interpolate(a.x, alpha, b.x, beta, i);
			Rational y = interpolate(a.y, alpha, b.y, beta, i);
			return new Pnt(x, y);
		} else {
			return ((HasCentroid) attr.getAttrValue()).getCentroid();
		}
	}
	private static Rational interpolate(Rational a, Instant iA, Rational b, Instant iB, Instant now) {
		long iaL = iA.longValue();
		long ibL = iB.longValue();
		long nowL = now.longValue();
		long weightA;
		long weightB;
		if (iaL < ibL) {
			assert iaL < nowL;
			assert nowL < ibL;
			weightA = nowL - iaL;
			weightB = ibL - nowL;
		} else {
			assert iaL > ibL;
			assert iaL > nowL;
			assert nowL > ibL;
			weightA = iaL - nowL;
			weightB = nowL - ibL;
		}
		return weightedAverage(a, weightA, b, weightB);
	}
	private static Rational weightedAverage(Rational a, long weightA, Rational b, long weightB) {
		Rational w1 = new Rational(weightA);
		Rational.multiply(a, w1, w1);
		Rational w2 = new Rational(weightB);
		Rational.multiply(b, w2, w2);
		Rational.plus(w1, w2, w1);
		w2.setTo(weightA + weightB);
		Rational.divide(w1, w2, w1);
		return w1;
	}
}
