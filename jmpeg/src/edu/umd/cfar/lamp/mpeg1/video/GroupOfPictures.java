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

class GroupOfPictures implements StateParsable
{
	private TimeCode time_code   = new TimeCode();
	private boolean  closed_gop  = false;
	private boolean  broken_link = false;


	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		if (bitstream.getbits(32) != VideoStartCodes.GROUP_START_CODE)
			throw new ParsingException("Expected group_start_code not found.");

		time_code.parse(bitstream);

		closed_gop  = (bitstream.getbits(1) == 1);
		broken_link = (bitstream.getbits(1) == 1);

		NextStartCode.parse(bitstream);

		if (bitstream.nextbits(32) == VideoStartCodes.EXTENSION_START_CODE)
		{
			if (bitstream.getbits(32) != VideoStartCodes.EXTENSION_START_CODE)
				throw new ParsingException("Expected extension_start_code not found.");

			while (bitstream.nextbits(24) != 1)
			{
				int group_extension_data = bitstream.getbits(8);
			}

			NextStartCode.parse(bitstream);
		}

		if (bitstream.nextbits(32) == VideoStartCodes.USER_DATA_START_CODE)
		{
			if (bitstream.getbits(32) != VideoStartCodes.USER_DATA_START_CODE)
				throw new ParsingException("Expected user_data_start_code not found.");

			while (bitstream.nextbits(24) != 1)
			{
				int user_data = bitstream.getbits(8);
			}

			NextStartCode.parse(bitstream);
		}

		do
		{
			parserState.parsePicture(bitstream);
		}
		while (bitstream.nextbits(32) == VideoStartCodes.PICTURE_START_CODE);
	}

	public static void index(Bitstream bitstream, IndexerState indexerState, GroupOfPicturesIndex index) throws IOException
	{
		bitstream.skipbits(32 + 25 + 2);

		NextStartCode.parse(bitstream);

		int start_code = 0;
		// skip until next PICTURE_START_CODE
		do
		{
			NextStartCode.parse(bitstream);
			start_code = bitstream.nextbits(32);
			if (start_code != VideoStartCodes.PICTURE_START_CODE)
				bitstream.skipbits(32);
		} while (start_code != VideoStartCodes.PICTURE_START_CODE);

		try
		{
			do
			{
				indexerState.indexPicture(bitstream, index);
			}
			while (bitstream.nextbits(32) == VideoStartCodes.PICTURE_START_CODE);
		}
		catch (FlIOException e)
		{
			if (!e.getMessage().equals("End of Data"))
			{
				throw e;
			}
		}
	}
	
	public static void index(Bitstream bitstream, IndexerState indexerState, VideoIndex index) throws IOException, MpegException
	{
		long startPosition = bitstream.getpos() / 8;
		int  numPictures   = 0;
		
		if (bitstream.getbits(32) != VideoStartCodes.GROUP_START_CODE)
			throw new ParsingException("Expected group_start_code not found.");

		bitstream.skipbits(25 + 2);

		int start_code = 0;
		// skip until next PICTURE_START_CODE
		do
		{
			NextStartCode.parse(bitstream);
			start_code = bitstream.nextbits(32);
			if (start_code != VideoStartCodes.PICTURE_START_CODE)
				bitstream.skipbits(32);
		} while (start_code != VideoStartCodes.PICTURE_START_CODE);

		try
		{
			do
			{
				indexerState.indexPicture(bitstream, index);
				numPictures++;
			}
			while (bitstream.nextbits(32) == VideoStartCodes.PICTURE_START_CODE);
		}
		catch (FlIOException e)
		{
			if (!e.getMessage().equals("End of Data"))
			{
				throw e;
			}
		}

		index.addGroupOfPictures(startPosition, numPictures, index.getLastSequenceHeader());
	}
}
