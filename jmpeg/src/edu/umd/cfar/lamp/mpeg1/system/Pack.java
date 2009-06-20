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
 *   Corresponds to pack() in ISO/IEC 11172-1.
 */
public class Pack implements Indexable
{
	private SystemClockReference system_clock_reference = new SystemClockReference();
	private int                  mux_rate               = 0;

	
	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		if (bitstream.getbits(32) != SystemStartCodes.PACK_START_CODE)
			throw new ParsingException("Expected pack_start_code not found.");
		if (bitstream.getbits(4)  != 2) // 0010 constant
			throw new ParsingException("Expected constant 0010 not found.");
		system_clock_reference.parse(bitstream);
		if (bitstream.getbits(1)  != 1) // marker bit
			throw new ParsingException("Expected marker bit not found.");
		if (bitstream.getbits(1)  != 1) // marker bit
			throw new ParsingException("Expected marker bit not found.");
		mux_rate = bitstream.getbits(22);
		if (bitstream.getbits(1)  != 1) // marker bit
			throw new ParsingException("Expected marker bit not found.");

		if (bitstream.nextbits(32) == SystemStartCodes.SYSTEM_HEADER_START_CODE)
		{
			parserState.parseSystemHeader(bitstream);
		}

		int next32 = bitstream.nextbits(32);
		int next24 = bitstream.nextbits(24);
		while ((next32 != SystemStartCodes.PACK_START_CODE) && (next32 != SystemStartCodes.ISO_11172_END_CODE) && (next24 == SystemStartCodes.PACKET_START_CODE_PREFIX))
		{
			parserState.parsePacket(bitstream);
			next32 = bitstream.nextbits(32);
			next24 = bitstream.nextbits(24);
		}
	}

	public void index(Bitstream bitstream, IndexerState indexerState, SystemIndex systemIndex) throws IOException
	{
		bitstream.skipbits(32 + 4 + 35 + 1 + 1 + 22 + 1);

		if (bitstream.nextbits(32) == SystemStartCodes.SYSTEM_HEADER_START_CODE)
		{
			indexerState.parseSystemHeader(bitstream);
		}

		int next32 = bitstream.nextbits(32);
		int next24 = bitstream.nextbits(24);
		while ((next32 != SystemStartCodes.PACK_START_CODE) && (next32 != SystemStartCodes.ISO_11172_END_CODE) && (next24 == SystemStartCodes.PACKET_START_CODE_PREFIX))
		{
			indexerState.indexPacket(bitstream, systemIndex);
			next32 = bitstream.nextbits(32);
			next24 = bitstream.nextbits(24);
		}
	}
	public int getMuxRate() {
		return mux_rate;
	}
}
