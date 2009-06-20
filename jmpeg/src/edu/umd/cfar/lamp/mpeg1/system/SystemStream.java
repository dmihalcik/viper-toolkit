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
 *   Corresponds to iso11172_stream() in ISO/IEC 11172-1.
 */
public class SystemStream implements Indexable
{
	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		do
		{
			parserState.parsePack(bitstream);
		}
		while (bitstream.nextbits(32) == SystemStartCodes.PACK_START_CODE);

		if (bitstream.getbits(32) != SystemStartCodes.ISO_11172_END_CODE)
			throw new ParsingException("Expected iso_11172_end_code not found.");
	}

	public void index(Bitstream bitstream, IndexerState indexerState, SystemIndex systemIndex) throws IOException
	{
		try
		{
			do
			{
				indexerState.indexPack(bitstream, systemIndex);
			}
			while (bitstream.nextbits(32) == SystemStartCodes.PACK_START_CODE);

			bitstream.skipbits(32);
		}
		catch (FlIOException e)
		{
			if (!e.getMessage().equals("End of Data"))
			{
				throw e;
			}
		}
	}
}
