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

import java.io.*;

public class ParsingException extends IOException
{
	public ParsingException()
	{
		super();
	}

	public ParsingException(String message)
	{
		super(message);
	}
}
