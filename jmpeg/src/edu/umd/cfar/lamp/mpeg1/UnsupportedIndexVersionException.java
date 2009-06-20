/***************************************
 *            ViPER-MPEG               *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *             MPEG-1 Decoder          *
 * Distributed under the LGPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.mpeg1;

public class UnsupportedIndexVersionException extends IndexException
{
	public UnsupportedIndexVersionException()
	{
		super();
	}

	public UnsupportedIndexVersionException(String message)
	{
		super(message);
	}
}
