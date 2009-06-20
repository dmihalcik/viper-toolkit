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
 *   Corresponds to packet() in ISO/IEC 11172-1.
 */
public class Packet implements Indexable
{
	private int                   stream_id               = 0;
	private boolean               STD_buffer_scale        = false;
	private int                   STD_buffer_size         = 0;
	private PresentationTimeStamp presentation_time_stamp = new PresentationTimeStamp();
	private DecodingTimeStamp     decoding_time_stamp     = new DecodingTimeStamp();


	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		if (bitstream.getbits(24) != SystemStartCodes.PACKET_START_CODE_PREFIX)
			throw new ParsingException("Expected packet_start_code_prefix not found.");
		stream_id         = bitstream.getbits(8);
		int packet_length = bitstream.getbits(16);
		int bytesConsumed = 0;
		if (stream_id != StreamIDs.PRIVATE_STREAM_2)
		{
			while (bitstream.nextbits(8) == 0xFF)
			{
				if (bitstream.getbits(8) != 0xFF)
					throw new ParsingException("Expected stuffing byte not found.");
				bytesConsumed++;
			}
			
			if (bitstream.nextbits(2) == 1)
			{
				if (bitstream.getbits(2) != 1)
					throw new ParsingException("Expected constant 01 not found.");
				STD_buffer_scale = (bitstream.getbits(1) == 1);
				STD_buffer_size  = bitstream.getbits(13);
				bytesConsumed += 2;
			}

			if (bitstream.nextbits(4) == 2)
			{
				if (bitstream.getbits(4) != 2)
					throw new ParsingException("Expected constant 0010 not found.");
				presentation_time_stamp.parse(bitstream);
				if (bitstream.getbits(1) != 1)
					throw new ParsingException("Expected marker bit not found.");
				bytesConsumed += 5;
			}
			else if (bitstream.nextbits(4) == 3)
			{
				if (bitstream.getbits(4) != 3)
					throw new ParsingException("Expected constant 0011 not found.");
				presentation_time_stamp.parse(bitstream);
				if (bitstream.getbits(1) != 1)
					throw new ParsingException("Expected marker bit not found.");
				if (bitstream.getbits(4) != 1)
					throw new ParsingException("Expected constant 0001 not found.");
				decoding_time_stamp.parse(bitstream);
				if (bitstream.getbits(1) != 1)
					throw new ParsingException("Expected marker bit not found.");
				bytesConsumed += 10;
			}
			else
			{
				if (bitstream.getbits(8) != 0x0F)
					throw new ParsingException("Expected constant 00001111 not found at bit position " + (bitstream.getpos() - 8) + ".");
				bytesConsumed++;
			}
		}

		int N = packet_length - bytesConsumed;
		if (0 < N)
		{
			// skip over N bytes of packet data
			bitstream.getbits(8*N);
		}
	}

	public void index(Bitstream bitstream, IndexerState indexerState, SystemIndex systemIndex) throws IOException
	{
		bitstream.skipbits(24);
		stream_id         = bitstream.getbits(8);
		int packet_length = bitstream.getbits(16);
		int bytesConsumed = 0;
		if (stream_id != StreamIDs.PRIVATE_STREAM_2)
		{
			while (bitstream.nextbits(8) == 0xFF)
			{
				bitstream.skipbits(8);
				bytesConsumed++;
			}
			
			if (bitstream.nextbits(2) == 1)
			{
				bitstream.skipbits(16);
				bytesConsumed += 2;
			}

			if (bitstream.nextbits(4) == 2)
			{
				bitstream.skipbits(40);
				bytesConsumed += 5;
			}
			else if (bitstream.nextbits(4) == 3)
			{
				bitstream.skipbits(80);
				bytesConsumed += 10;
			}
			else
			{
				bitstream.skipbits(8);
				bytesConsumed++;
			}
		}
		
		int N = packet_length - bytesConsumed;
		long startPos = bitstream.getpos() / 8;

		int skip32 = N / 4;
		int skip1  = (N * 8) % 32;

		try
		{
		
			for (int i = 0; i < skip32; i++)
				bitstream.skipbits(32);
			bitstream.skipbits(skip1);
			systemIndex.addPacket(stream_id, startPos, N);
		}
		catch (FlIOException e)
		{
			if (e.getMessage().equals("End of Data"))
				systemIndex.addPacket(stream_id, startPos, bitstream.getpos() / 8);
			else
				throw e;
		}
	}
	boolean isStdBufferScale() {
		return STD_buffer_scale;
	}
	int getStdBufferSize() {
		return STD_buffer_size;
	}
}
