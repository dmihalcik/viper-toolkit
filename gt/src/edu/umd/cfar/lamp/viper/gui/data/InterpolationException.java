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

package edu.umd.cfar.lamp.viper.gui.data;


/**
 * Abstract class that super-classes all exceptions that can be generated through
 * interpolation process.  There are two things that can go wrong when interpo-
 * lating.
 *
 * 1)Interpolation alogorythims has not been written for the shape that is
 * beaing interpolated
 * 2)User leaves "null" for an implemented attribute in a frame that is either
 * begining or ending frame of interpolation bar. The program needs two key
 * frames to figure out what to put in the middle.
 *
 */
public class InterpolationException extends Exception
{
	public InterpolationException(String s)
	{
		super(s);
	}
}
