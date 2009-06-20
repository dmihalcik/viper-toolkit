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

class VideoSequence implements StateParsable
{
	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		NextStartCode.parse(bitstream);

		do
		{
			parserState.parseSequenceHeader(bitstream);
			do
			{
				parserState.parseGroupOfPictures(bitstream);
			}
			while (bitstream.nextbits(32) == VideoStartCodes.GROUP_START_CODE);
		}
		while (bitstream.nextbits(32) == VideoStartCodes.SEQUENCE_HEADER_CODE);

		if (bitstream.getbits(32) != VideoStartCodes.SEQUENCE_END_CODE)
			throw new ParsingException("Expected sequence_end_code not found.");
	}

	public static SequenceHeader getFirstSequenceHeader(Bitstream bitstream) throws IOException
	{
		NextStartCode.parse(bitstream);
		SequenceHeader sh = new SequenceHeader();
		sh.parse(bitstream);
		return sh;
	}
	
	public static void index(Bitstream bitstream, IndexerState indexerState, VideoIndex index) throws IOException, MpegException
	{
		NextStartCode.parse(bitstream);

		try
		{
			do
			{
				indexerState.indexSequenceHeader(bitstream, index);

				do
				{
					indexerState.indexGroupOfPictures(bitstream, index);
				}
				while (bitstream.nextbits(32) == VideoStartCodes.GROUP_START_CODE);
			}
			while (bitstream.nextbits(32) == VideoStartCodes.SEQUENCE_HEADER_CODE);
		
			bitstream.skipbits(32); // SEQUENCE_END_CODE
		}
		catch (FlIOException e)
		{
			if (!e.getMessage().equals("End of Data"))
			{
				throw e;
			}
		}

		index.complete();
	}
}