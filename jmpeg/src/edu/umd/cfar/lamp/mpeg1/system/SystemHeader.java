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
 *   Corresponds to system_header() in ISO/IEC 11172-1.
 */
public class SystemHeader implements Parsable
{
	int     rate_bound             = 0;
	int     audio_bound            = 0;
	boolean fixed_flag             = false;
	boolean CSPS_flag              = false;
	boolean system_audio_lock_flag = false;
	boolean system_video_lock_flag = false;
	int     video_bound            = 0;

	
	public void parse(Bitstream bitstream) throws IOException
	{
		if (bitstream.getbits(32) != SystemStartCodes.SYSTEM_HEADER_START_CODE)
			throw new ParsingException("Expected system_header_start_code not found.");
		int header_length = bitstream.getbits(16);
		if (bitstream.getbits(1)  != 1)
			throw new ParsingException("Expected marker bit not found.");
		rate_bound = bitstream.getbits(22);
		if (bitstream.getbits(1)  != 1)
			throw new ParsingException("Expected marker bit not found.");
		audio_bound = bitstream.getbits(6);
		fixed_flag = (bitstream.getbits(1) == 1);
		CSPS_flag  = (bitstream.getbits(1) == 1);
		system_audio_lock_flag = (bitstream.getbits(1) == 1);
		system_video_lock_flag = (bitstream.getbits(1) == 1);
		if (bitstream.getbits(1)  != 1)
			throw new ParsingException("Expected marker bit not found.");
		video_bound = bitstream.getbits(5);
		bitstream.skipbits(8); // reserved byte, should be 0xFF, but may be changed in future versions of ISO/IEC 11172
		while (bitstream.nextbits(1) == 1)
		{
			int stream_id = bitstream.getbits(8);
			if (bitstream.getbits(2) != 3)
				throw new ParsingException("Expected constant 11 not found.");
			boolean STD_buffer_bound_scale = (bitstream.getbits(1) == 1);
			int STD_buffer_size_bound = bitstream.getbits(13);
		}
	}
}
