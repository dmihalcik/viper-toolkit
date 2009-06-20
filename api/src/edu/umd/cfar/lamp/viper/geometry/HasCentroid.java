/*
 * Created on May 18, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.geometry;

/**
 * Interface for all geometric objects that have some
 * concept of a 'centroid'. It isn't that necessary
 * that the centroid be completely in agreement with 
 * the geometric definition, that is, the 'center of 
 * mass'.
 * 
 * @author clin
 */
public interface HasCentroid {
	/**
	 * Gets a copy of the centroid. 
	 * @return the centroid of the object
	 */
	public Pnt getCentroid() ;
}
