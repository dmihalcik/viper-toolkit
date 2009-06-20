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

package edu.umd.cfar.lamp.mpeg1.system;

import edu.umd.cfar.lamp.mpeg1.*;

public class StreamNotFoundException extends MpegException
{
	public StreamNotFoundException()
	{
		super();
	}

	public StreamNotFoundException(String message)
	{
		super(message);
	}
}
