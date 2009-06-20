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

class NextStartCode
{
	public static void parse(Bitstream bitstream) throws IOException
	{
		bitstream.align(8);
		while (bitstream.nextbits(24) != 1)
		{
			bitstream.skipbits(8);
		}
	}
}
