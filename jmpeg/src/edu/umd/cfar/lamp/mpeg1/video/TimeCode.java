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

class TimeCode implements Parsable
{
	private boolean drop_frame_flag    = false;
	private int     time_code_hours    = 0;
	private int     time_code_minutes  = 0;
	private int     time_code_seconds  = 0;
	private int     time_code_pictures = 0;


	public void parse(Bitstream bitstream) throws IOException
	{
		drop_frame_flag    = (bitstream.getbits(1) == 1);

		time_code_hours    = bitstream.getbits(5);
		if (time_code_hours > 23)
			throw new ParsingException("Field time_code_hours value is out of range.  Acceptable values: [0..23]  Parsed value: " + time_code_hours);

		time_code_minutes  = bitstream.getbits(6);
		if (time_code_minutes > 59)
			throw new ParsingException("Field time_code_minutes value is out of range.  Acceptable values: [0..59]  Parsed value: " + time_code_minutes);

		if (bitstream.getbits(1) != 1)
			throw new ParsingException("Expected marker bit not found.");
		
		time_code_seconds  = bitstream.getbits(6);
		if (time_code_seconds > 59)
			throw new ParsingException("Field time_code_seconds value is out of range.  Acceptable values: [0..59]  Parsed value: " + time_code_seconds);

		time_code_pictures = bitstream.getbits(6);
		if (time_code_pictures > 59)
			throw new ParsingException("Field time_code_pictures value is out of range.  Acceptable values: [0..59]  Parsed value: " + time_code_pictures);
	}

	public boolean getDropFrameFlag()
	{
		return drop_frame_flag;
	}

	public int getHours()
	{
		return time_code_hours;
	}

	public int getMinutes()
	{
		return time_code_minutes;
	}

	public int getSeconds()
	{
		return time_code_seconds;
	}

	public int getPictures()
	{
		return time_code_pictures;
	}
}
