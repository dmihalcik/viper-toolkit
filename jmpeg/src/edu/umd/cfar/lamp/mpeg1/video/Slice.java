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

public class Slice implements Decodable
{
	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		if (!VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)))
		{
			bitstream.skipbits(32);
			throw new ParsingException("Expected slice_start_code not found.");
		}
		bitstream.skipbits(24);
		int slice_vertical_position = bitstream.getbits(8); // last 8 bits of slice_start_code

		int quantizer_scale = bitstream.getbits(5);
		if (quantizer_scale == 0)
			throw new ParsingException("Field quantizer_scale out of range.  Acceptable values: [1..31]  Parsed value: " + quantizer_scale);

		while (bitstream.nextbits(1) == 1)
		{
			if (bitstream.getbits(1) != 1)
				throw new ParsingException("Expected extra_bit_slice not found (should be 1, is 0).");

			int extra_information_slice = bitstream.getbits(8);
		}

		if (bitstream.getbits(1) != 0)
			throw new ParsingException("Expected extra_bit_slice not found (should be 0, is 1).");

		do
		{
			parserState.parseMacroblock(bitstream);
		}
		while (bitstream.nextbits(23) != 0);

		NextStartCode.parse(bitstream);
	}

	public void decode(Bitstream bitstream, DecoderState decoderState) throws IOException, MpegException
	{
		decoderState.resetDctDcPast();
		decoderState.setPastIntraAddress(-2);
		decoderState.resetReconForPrev();
		decoderState.resetReconBackPrev();

		bitstream.skipbits(24); // first 24 bits of SLICE_START_CODE

		int slice_vertical_position = bitstream.getbits(8);
		int mb_width = decoderState.getMbWidth();
		decoderState.setPreviousMacroblockAddress( (slice_vertical_position-1)*mb_width-1 );

		decoderState.setQuantizerScale(bitstream.getbits(5));
		
		while (bitstream.nextbits(1) == 1)
		{
			bitstream.skipbits(1 + 8); // extra_bit_slice(1 bit) + extra_information_slice(8 bits)
		}

		bitstream.skipbits(1); // extra_bit_slice

		do
		{
			decoderState.decodeMacroblock(bitstream);
		}
		while (bitstream.nextbits(23) != 0);

		NextStartCode.parse(bitstream);
	}
}
