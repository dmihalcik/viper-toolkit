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


class MacroblockType_DFrame implements Parsable
{
	private MacroblockType value = new MacroblockType();


	public void parse(Bitstream bitstream) throws IOException
	{
		boolean
			macroblock_quant,
			macroblock_motion_forward,
			macroblock_motion_backward,
			macroblock_pattern,
			macroblock_intra;
		
		switch (bitstream.nextbits(1))
		{
		case 1:
			bitstream.skipbits(1);
			macroblock_quant = false;
			macroblock_motion_forward = false;
			macroblock_motion_backward = false;
			macroblock_pattern = false;
			macroblock_intra = true;
			break;
		default:
			throw new ParsingException("VLC decode for MacroblockType_DFrame failed.");
		}
		value.setValues(macroblock_quant, macroblock_motion_forward, macroblock_motion_backward, macroblock_pattern, macroblock_intra);
	}

	public MacroblockType getValue()
	{
		return value;
	}
}
