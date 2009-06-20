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

package edu.umd.cfar.lamp.mpeg1.video;

import java.io.*;

import edu.columbia.ee.flavor.*;
import edu.umd.cfar.lamp.mpeg1.*;

class PictureRate implements Parsable
{
	private float value = 0.0f;
	

	public void parse(Bitstream bitstream) throws IOException
	{
		switch (bitstream.nextbits(4))
		{
		case 0:
			throw new ParsingException("Value 0 for PictureRate forbidden.");
		case 1:
			bitstream.skipbits(4);
			value = 23.976f;
			break;
		case 2:
			bitstream.skipbits(4);
			value = 24f;
			break;
		case 3:
			bitstream.skipbits(4);
			value = 25f;
			break;
		case 4:
			bitstream.skipbits(4);
			value = 29.97f;
			break;
		case 5:
			bitstream.skipbits(4);
			value = 30f;
			break;
		case 6:
			bitstream.skipbits(4);
			value = 50f;
			break;
		case 7:
			bitstream.skipbits(4);
			value = 59.94f;
			break;
		case 8:
			bitstream.skipbits(4);
			value = 60f;
			break;
		default:
			throw new ParsingException("Value " + bitstream.nextbits(4) + " for PictureRate reserved.");
		}
	}

	public float getValue()
	{
		return value;
	}
}
