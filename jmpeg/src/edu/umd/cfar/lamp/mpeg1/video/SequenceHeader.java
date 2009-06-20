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
import java.util.*;

import edu.columbia.ee.flavor.*;
import edu.umd.cfar.lamp.mpeg1.*;

public class SequenceHeader implements Parsable
{
	public static final int VARIABLE_BITRATE = 0x3FFFF;
	
	private static final int default_intra_quantizer_matrix[][] =
		{ { 8, 16, 19, 22, 26, 27, 29, 34},
		  {16, 16, 22, 24, 27, 29, 34, 37},
		  {19, 22, 26, 27, 29, 34, 34, 38},
		  {22, 22, 26, 27, 29, 34, 37, 40},
		  {22, 26, 27, 29, 32, 35, 40, 48},
		  {26, 27, 29, 32, 35, 40, 48, 58},
		  {26, 27, 29, 34, 38, 46, 56, 69},
		  {27, 29, 35, 38, 46, 56, 69, 83} };
	
	private int     horizontal_size  = 0;
	private int     vertical_size    = 0;

	private int     mb_width         = 0;
	private int     mb_height        = 0;

	private PelAspectRatio   pel_aspect_ratio = new PelAspectRatio(1);
	private float   picture_rate     = 0.0f;
	private int     bit_rate         = 0;
	private int     vbv_buffer_size  = 0;
	private boolean constrained_parameters_flag     = false;
	private int     intra_quantizer_matrix[][]      = new int[8][8];
	private int     non_intra_quantizer_matrix[][]  = new int[8][8];


	public void writeIndex(DataOutput out) throws IOException
	{
		out.writeInt(horizontal_size);
		out.writeInt(vertical_size);
		out.writeInt(pel_aspect_ratio.getValue());
		out.writeFloat(picture_rate);
		out.writeInt(bit_rate);
		out.writeInt(vbv_buffer_size);
		out.writeBoolean(constrained_parameters_flag);
		
		boolean storeIntraQuantizerMatrix = false;
		for (int row = 0; row < 8; row++)
		{
			if (!Arrays.equals(intra_quantizer_matrix[row], default_intra_quantizer_matrix[row]))
			{
				storeIntraQuantizerMatrix = true;
				break;
			}
		}
		out.writeBoolean(storeIntraQuantizerMatrix);
		if (storeIntraQuantizerMatrix)
		{
			for (int row = 0; row < 8; row++)
			{
				for (int col = 0; col < 8; col++)
				{
					out.writeByte(intra_quantizer_matrix[row][col]);
				}
			}
		}

		boolean storeNonIntraQuantizerMatrix = false;
		for (int row = 0; row < 8; row++)
		{
			for (int col = 0; col < 8; col++)
			{
				if (non_intra_quantizer_matrix[row][col] != 16)
				{
					storeNonIntraQuantizerMatrix = true;
					break;
				}
			}
			if (storeNonIntraQuantizerMatrix)
				break;
		}
		out.writeBoolean(storeNonIntraQuantizerMatrix);
		if (storeNonIntraQuantizerMatrix)
		{
			for (int row = 0; row < 8; row++)
			{
				for (int col = 0; col < 8; col++)
				{
					out.writeByte(non_intra_quantizer_matrix[row][col]);
				}
			}
		}
	}
	
	public void readIndex(DataInput in, byte version) throws IOException, UnsupportedIndexVersionException
	{
		switch (version)
		{
			case 0x01:
				horizontal_size             = in.readInt();
				vertical_size               = in.readInt();

				mb_width  = (horizontal_size / 16) + ((horizontal_size % 16) > 0 ? 1 : 0);
				mb_height = (vertical_size   / 16) + ((vertical_size   % 16) > 0 ? 1 : 0);

				pel_aspect_ratio            = new PelAspectRatio(in.readInt());
				picture_rate                = in.readFloat();
				bit_rate                    = in.readInt();
				vbv_buffer_size             = in.readInt();
				constrained_parameters_flag = in.readBoolean();

				boolean loadIntraQuantizerMatrix = in.readBoolean();
				if (loadIntraQuantizerMatrix)
				{
					for (int row = 0; row < 8; row++)
					{
						for (int col = 0; col < 8; col++)
						{
							intra_quantizer_matrix[row][col] = in.readUnsignedByte();
						}
					}
				}
				else
				{
					for (int row = 0; row < 8; row++)
					{
						System.arraycopy(default_intra_quantizer_matrix[row], 0, intra_quantizer_matrix[row], 0, 8);
					}
				}

				boolean loadNonIntraQuantizerMatrix = in.readBoolean();
				if (loadNonIntraQuantizerMatrix)
				{
					for (int row = 0; row < 8; row++)
					{
						for (int col = 0; col < 8; col++)
						{
							non_intra_quantizer_matrix[row][col] = in.readUnsignedByte();
						}
					}
				}
				else
				{
					for (int row = 0; row < 8; row++)
					{
						Arrays.fill(non_intra_quantizer_matrix[row], 16);
					}
				}
				break;

			default:
				throw new UnsupportedIndexVersionException("version: " + Integer.toHexString(version));
		}
	}

	public int getFrameWidth()
	{
		return horizontal_size;
	}

	public int getFrameHeight()
	{
		return vertical_size;
	}

	public int getMbWidth()
	{
		return mb_width;
	}

	public int getMbHeight()
	{
		return mb_height;
	}

	public PelAspectRatio getPixelAspectRatio()
	{
		return pel_aspect_ratio;
	}

	public float getFrameRate()
	{
		return picture_rate;
	}

	public int getBitRate()
	{
		return bit_rate;
	}

	public int[][] getIntraQuantizerMatrix()
	{
		return intra_quantizer_matrix;
	}

	public int[][] getNonIntraQuantizerMatrix()
	{
		return non_intra_quantizer_matrix;
	}

	public void parse(Bitstream bitstream) throws IOException
	{
		if (bitstream.getbits(32) != VideoStartCodes.SEQUENCE_HEADER_CODE)
			throw new ParsingException("Expected sequence_header_code not found.");

		horizontal_size = bitstream.getbits(12);
		vertical_size   = bitstream.getbits(12);

		// mb_width is horizontal_size/16 with maybe one added if the horizontal_size is not a multiple of 16.
		// Same with mb_height and vertical_size.
		mb_width  = (horizontal_size / 16) + ((horizontal_size % 16) > 0 ? 1 : 0);
		mb_height = (vertical_size   / 16) + ((vertical_size   % 16) > 0 ? 1 : 0);

		pel_aspect_ratio = new PelAspectRatio();
		pel_aspect_ratio.parse(bitstream);

		PictureRate pr = new PictureRate();
		pr.parse(bitstream);
		picture_rate = pr.getValue();

		bit_rate = bitstream.getbits(18);

		if (bitstream.getbits(1) != 1)
			throw new ParsingException("Expected marker_bit not found.");

		vbv_buffer_size = bitstream.getbits(10);
		constrained_parameters_flag = (bitstream.getbits(1) == 1);

		boolean load_intra_quantizer_matrix = (bitstream.getbits(1) == 1);
		if (load_intra_quantizer_matrix)
		{
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
					intra_quantizer_matrix[i][j] = bitstream.getbits(8);
		}
		else // copy values from the default intra_quantizer_matrix
		{
			for (int i = 0; i < 8; i++)
				System.arraycopy(default_intra_quantizer_matrix[i], 0, intra_quantizer_matrix[i], 0, 8);
		}

		boolean load_non_intra_quantizer_matrix = (bitstream.getbits(1) == 1);
		if (load_non_intra_quantizer_matrix)
		{
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
					non_intra_quantizer_matrix[i][j] = bitstream.getbits(8);
		}
		else // fill with 16s (which is the default non_intra_quantizer_matrix)
		{
			for (int i = 0; i < 8; i++)
				Arrays.fill(non_intra_quantizer_matrix[i], 16);
		}

		NextStartCode.parse(bitstream);

		if (bitstream.nextbits(32) == VideoStartCodes.EXTENSION_START_CODE)
		{
			if (bitstream.getbits(32) != VideoStartCodes.EXTENSION_START_CODE)
				throw new ParsingException("Expected extension_start_code not found.");

			while (bitstream.nextbits(24) != 1)
			{
				int sequence_extension_data = bitstream.getbits(8);
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
	}
}
