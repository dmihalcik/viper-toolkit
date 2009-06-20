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

class Picture implements Decodable
{
	private int     temporal_reference       = 0;
	private int     picture_coding_type      = 0;
	private int     vbv_delay                = 0;
	private boolean full_pel_forward_vector  = false;
	private int     forward_f_code           = 0;
	private boolean full_pel_backward_vector = false;
	private int     backward_f_code          = 0;


	public int getTemporalReference()
	{
		return temporal_reference;
	}

	public int getPictureCodingType()
	{
		return picture_coding_type;
	}

	public int getVBVDelay()
	{
		return vbv_delay;
	}

	public boolean getFullPelForwardVector()
	{
		return full_pel_forward_vector;
	}

	public int getForwardFCode()
	{
		return forward_f_code;
	}

	public boolean getFullPelBackwardVector()
	{
		return full_pel_backward_vector;
	}

	public int getBackwardFCode()
	{
		return backward_f_code;
	}

	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		if (bitstream.getbits(32) != VideoStartCodes.PICTURE_START_CODE)
			throw new ParsingException("Expected picture_start_code not found.");

		temporal_reference = bitstream.getbits(10);

		picture_coding_type = bitstream.getbits(3);
		if (!PictureCodingTypes.isValidPictureCodingType(picture_coding_type))
			throw new ParsingException("Invalid picture_coding_type (" + picture_coding_type + ").");

		vbv_delay = bitstream.getbits(16);

		if ((picture_coding_type == PictureCodingTypes.TYPE_P) || (picture_coding_type == PictureCodingTypes.TYPE_B))
		{
			full_pel_forward_vector = (bitstream.getbits(1) == 1);
			forward_f_code = bitstream.getbits(3);
		}

		if (picture_coding_type == PictureCodingTypes.TYPE_B)
		{
			full_pel_backward_vector = (bitstream.getbits(1) == 1);
			backward_f_code = bitstream.getbits(3);
		}

		while (bitstream.nextbits(1) == 1)
		{
			if (bitstream.getbits(1) != 1)
				throw new ParsingException("Expected extra_bit_picture not found (should be 1, is 0).");

			int extra_information_picture = bitstream.getbits(8);
		}

		if (bitstream.getbits(1) != 0)
			throw new ParsingException("Expected extra_bit_picture not found (should be 0, is 1).");

		NextStartCode.parse(bitstream);

		if (bitstream.nextbits(32) == VideoStartCodes.EXTENSION_START_CODE)
		{
			if (bitstream.getbits(32) != VideoStartCodes.EXTENSION_START_CODE)
				throw new ParsingException("Expected extension_start_code not found.");

			while (bitstream.nextbits(24) != 1)
			{
				int picture_extension_data = bitstream.getbits(8);
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
			parserState.parseSlice(bitstream);
		}
		while (VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)));
	}

	public void index(Bitstream bitstream, VideoIndex index) throws IOException
	{
		if (bitstream.getbits(32) != VideoStartCodes.PICTURE_START_CODE)
			throw new ParsingException("Expected picture_start_code not found.");

		boolean done = false;
		do
		{
			NextStartCode.parse(bitstream);
			if (VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)))
				done = true;
			else
				bitstream.skipbits(32);
		} while (!done);

		done = false;
		do
		{
			NextStartCode.parse(bitstream);
			if (VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)))
				bitstream.skipbits(32);
			else
				done = true;
		} while (!done);
	}

	public void index(Bitstream bitstream, GroupOfPicturesIndex index) throws IOException
	{
		long startPosition = bitstream.getpos() / 8;

		bitstream.skipbits(32);
		temporal_reference = bitstream.getbits(10);
		picture_coding_type = bitstream.getbits(3);

		boolean done = false;
		do
		{
			NextStartCode.parse(bitstream);
			if (VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)))
				done = true;
			else
				bitstream.skipbits(32);
		} while (!done);

		done = false;
		do
		{
			NextStartCode.parse(bitstream);
			if (VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)))
				bitstream.skipbits(32);
			else
				done = true;
		} while (!done);

		index.addPicture(startPosition, (bitstream.getpos()/8)-startPosition, (byte)picture_coding_type, temporal_reference);
	}

	public void decode(Bitstream bitstream, DecoderState decoderState) throws IOException, MpegException
	{
		bitstream.skipbits(32); // PICTURE_START_CODE

		temporal_reference  = bitstream.getbits(10);
		picture_coding_type = bitstream.getbits(3);
		vbv_delay           = bitstream.getbits(16);

		if ((picture_coding_type == PictureCodingTypes.TYPE_P) || (picture_coding_type == PictureCodingTypes.TYPE_B))
		{
			full_pel_forward_vector = (bitstream.getbits(1) == 1);
			forward_f_code = bitstream.getbits(3);
		}

		if (picture_coding_type == PictureCodingTypes.TYPE_B)
		{
			full_pel_backward_vector = (bitstream.getbits(1) == 1);
			backward_f_code = bitstream.getbits(3);
		}

		while (bitstream.nextbits(1) == 1)
		{
			bitstream.skipbits(1 + 8); // extra_bit_picture(1 bit) + extra_information_picture(8 bits)
		}

		bitstream.skipbits(1); // extra_bit_picture

		NextStartCode.parse(bitstream);

		if (bitstream.nextbits(32) == VideoStartCodes.EXTENSION_START_CODE)
		{
			bitstream.skipbits(32); // EXTENSION_START_CODE

			while (bitstream.nextbits(24) != 1)
			{
				bitstream.skipbits(8);
			}

			NextStartCode.parse(bitstream);
		}

		if (bitstream.nextbits(32) == VideoStartCodes.USER_DATA_START_CODE)
		{
			bitstream.skipbits(32); // USER_DATA_START_CODE

			while (bitstream.nextbits(24) != 1)
			{
				bitstream.skipbits(8);
			}

			NextStartCode.parse(bitstream);
		}

		do
		{
			try
			{
				decoderState.decodeSlice(bitstream);
			}
			catch (Exception e)
			{
				NextStartCode.parse(bitstream);
			}
		}
		while (VideoStartCodes.isSliceStartCode(bitstream.nextbits(32)));
	}

}
