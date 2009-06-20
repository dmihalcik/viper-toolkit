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

package edu.umd.cfar.lamp.viper.gui.core;

import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Interface for beans that support interpolating a given attribute
 * data type.
 */
public interface Interpolator {
	/**
	 * Interpolates between the given objects, returning 
	 * a Z.length + sum(length) length array of objects.
	 * @param Z the values to interpolate between
	 * @param length the length of the interpolations, such that 
	 * <code>length.length == Z.length-1</code>
	 * @param method the interpolation method
	 * @return the interpolated values
	 * @throws InterpolationException
	 */
	public ArbitraryIndexList interpolate(Object[] Z, long[] length, String method) throws InterpolationException;
}
