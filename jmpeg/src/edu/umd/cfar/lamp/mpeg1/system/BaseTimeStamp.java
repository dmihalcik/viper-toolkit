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

import java.io.*;

import edu.columbia.ee.flavor.*;
import edu.umd.cfar.lamp.mpeg1.*;

/**
 *   Base class for all time stamp classes (they're all basically identical).
 */
public class BaseTimeStamp implements Parsable
{
	protected int field1 = 0;
	protected int field2 = 0;
	protected int field3 = 0;
	
	public void parse(Bitstream bitstream) throws IOException
	{
		field1 = bitstream.getbits(3);
		if (bitstream.getbits(1) != 1)
			throw new ParsingException("Expected marker bit not found.");
        field2 = bitstream.getbits(15);
		if (bitstream.getbits(1) != 1)
			throw new ParsingException("Expected marker bit not found.");
        field3 = bitstream.getbits(15);
	}
}
