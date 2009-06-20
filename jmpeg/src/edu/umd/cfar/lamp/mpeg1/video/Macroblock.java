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

/**
 * Corresponds to Macroblock Layer in ISO/IEC 11172-2 (Section 2.4.2.7)
 */
class Macroblock implements Decodable
{
	private int            macroblock_address              = 0;
	private MacroblockType macroblock_type                 = null;
	private boolean        pattern_code[]                  = {false,false,false,false,false,false}; // 6 elts for 6 blocks

	private int            recon_right_for                 = 0;
	private int            recon_down_for                  = 0;
	private int            recon_right_for_prev            = 0;
	private int            recon_down_for_prev             = 0;

	private int            recon_right_back                = 0;
	private int            recon_down_back                 = 0;
	private int            recon_right_back_prev           = 0;
	private int            recon_down_back_prev            = 0;

	// VLC lookup objects
	private MacroblockAddressIncrement mai  = new MacroblockAddressIncrement();
	private MacroblockType_IFrame      mtif = new MacroblockType_IFrame();
	private MacroblockType_PFrame      mtpf = new MacroblockType_PFrame();
	private MacroblockType_BFrame      mtbf = new MacroblockType_BFrame();
	private MacroblockType_DFrame      mtdf = new MacroblockType_DFrame();
	private MotionVector               mv   = new MotionVector();
	private MacroblockPattern          mp   = new MacroblockPattern();


	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		while (bitstream.nextbits(11) == 15) // 0000.0001.111
		{
			if (bitstream.getbits(11) != 15)
				throw new ParsingException("Expected macroblock_stuffing not found.");
		}
		
		int macroblock_address_increment = 0;
		while (bitstream.nextbits(11) == 8) // 0000.0001.000
		{
			if (bitstream.getbits(11) != 8)
				throw new ParsingException("Expected macroblock_escape not found.");
			macroblock_address_increment += 33;
		}

		mai.parse(bitstream);
		macroblock_address_increment += mai.getValue();

		switch (parserState.getPictureCodingType())
		{
			case PictureCodingTypes.TYPE_I:
				mtif.parse(bitstream);
				macroblock_type = mtif.getValue();
				break;
			case PictureCodingTypes.TYPE_P:
				mtpf.parse(bitstream);
				macroblock_type = mtpf.getValue();
				break;
			case PictureCodingTypes.TYPE_B:
				mtbf.parse(bitstream);
				macroblock_type = mtbf.getValue();
				break;
			case PictureCodingTypes.TYPE_D:
				mtdf.parse(bitstream);
				macroblock_type = mtdf.getValue();
				break;
			default: throw new ParsingException("Invalid picture_coding_type passed to Macroblock.");
		}

		if (getMacroblockQuant())
		{
			int quantizer_scale = bitstream.getbits(5);
		}

		if (getMacroblockMotionForward())
		{
			mv.parse(bitstream);
			int motion_horizontal_forward_code = mv.getValue();

			int forward_r_size = parserState.getForwardFCode() - 1;
			int forward_f      = 1 << forward_r_size;

			if ((forward_f != 1) && (motion_horizontal_forward_code != 0))
			{
				int motion_horizontal_forward_r = bitstream.getbits(forward_r_size);
			}

			mv.parse(bitstream);
			int motion_vertical_forward_code = mv.getValue();

			if ((forward_f != 1) && (motion_vertical_forward_code != 0))
			{
				int motion_vertical_forward_r = bitstream.getbits(forward_r_size);
			}
		}

		if (getMacroblockMotionBackward())
		{
			mv.parse(bitstream);
			int motion_horizontal_backward_code = mv.getValue();

			int backward_r_size = parserState.getBackwardFCode() - 1;
			int backward_f      = 1 << backward_r_size;			

			if ((backward_f != 1) && (motion_horizontal_backward_code != 0))
			{
				int motion_horizontal_backward_r = bitstream.getbits(backward_r_size);
			}

			mv.parse(bitstream);
			int motion_vertical_backward_code = mv.getValue();

			if ((backward_f != 1) && (motion_vertical_backward_code != 0))
			{
				int motion_vertical_backward_r = bitstream.getbits(backward_r_size);
			}
		}

		int coded_block_pattern = 0;
		if (getMacroblockPattern())
		{
			mp.parse(bitstream);
			coded_block_pattern = mp.getValue();
		}

		for (int i = 0; i < 6; i++)
		{
			pattern_code[i] = false;
			if ((coded_block_pattern & (1 << (5 - i))) != 0)
				pattern_code[i] = true;			
			if (getMacroblockIntra())
				pattern_code[i] = true;
		}					

		for (int j = 0; j < 6; j++)
		{
			parserState.parseBlock(j, bitstream);
		}

		if (parserState.getPictureCodingType() == PictureCodingTypes.TYPE_D)
		{
			if (bitstream.getbits(1) != 1)
				throw new ParsingException("Expected end_of_macroblock not found.");
		}
	}

	public void decode(Bitstream bitstream, DecoderState decoderState) throws IOException, MpegException
	{
		int backward_f_code = decoderState.getBackwardFCode();
		int forward_f_code  = decoderState.getForwardFCode();
		
		while (bitstream.nextbits(11) == 15) // 0000.0001.111 (stuffing)
		{
			bitstream.skipbits(11);
		}

		int macroblock_address_increment = 0;
		while (bitstream.nextbits(11) == 8) // 0000.0001.000 (macroblock_escape)
		{
			bitstream.skipbits(11);
			macroblock_address_increment += 33;
		}

		mai.parse(bitstream);
		macroblock_address_increment += mai.getValue();
		
		int previous_macroblock_address = decoderState.getPreviousMacroblockAddress();

		macroblock_address = previous_macroblock_address + macroblock_address_increment;

		int picture_coding_type = decoderState.getPictureCodingType();

		resetReconFor();
		resetReconBack();
		
		// skipped macroblocks
		if (macroblock_address_increment > 1)
		{
			decoderState.resetDctDcPast();
			
			switch (picture_coding_type)
			{
				case PictureCodingTypes.TYPE_P:
					resetReconForPrev();
					for (int i = 1; i < macroblock_address_increment; i++)
					{
						decoderState.copyMacroblockFromPastToCurrent(previous_macroblock_address + i);
					}
					break;
				case PictureCodingTypes.TYPE_B:
					recon_right_for  = recon_right_for_prev;
					recon_down_for   = recon_down_for_prev;
					recon_right_back = recon_right_back_prev;
					recon_down_back  = recon_down_back_prev;
					for (int i = 1; i < macroblock_address_increment; i++)
					{
						decoderState.fillInSkippedBPictureMacroblock(previous_macroblock_address + i);
					}
					break;
			}
		}

		switch (picture_coding_type)
		{
			case PictureCodingTypes.TYPE_I:
				mtif.parse(bitstream);
				macroblock_type = mtif.getValue();
				break;
			case PictureCodingTypes.TYPE_P:
				mtpf.parse(bitstream);
				macroblock_type = mtpf.getValue();
				break;
			case PictureCodingTypes.TYPE_B:
				mtbf.parse(bitstream);
				macroblock_type = mtbf.getValue();
				break;
			case PictureCodingTypes.TYPE_D:
				mtdf.parse(bitstream);
				macroblock_type = mtdf.getValue();
				break;
			default: throw new ParsingException("Invalid picture_coding_type passed to Macroblock.");
		}

		if (!getMacroblockIntra())
			decoderState.resetDctDcPast();

		if (getMacroblockQuant())
			decoderState.setQuantizerScale(bitstream.getbits(5));

		if (getMacroblockMotionForward())
		{
			mv.parse(bitstream);
			int motion_horizontal_forward_code = mv.getValue();

			int motion_horizontal_forward_r = 0;
			int motion_vertical_forward_r = 0;

			int forward_r_size = forward_f_code - 1;
			int forward_f      = 1 << forward_r_size;

			if ((forward_f != 1) && (motion_horizontal_forward_code != 0))
				motion_horizontal_forward_r = bitstream.getbits(forward_r_size);

			mv.parse(bitstream);
			int motion_vertical_forward_code = mv.getValue();

			if ((forward_f != 1) && (motion_vertical_forward_code != 0))
				motion_vertical_forward_r = bitstream.getbits(forward_r_size);

			// from section 2.4.4.2 "Predictive-coded macroblocks in P-pictures"
			int complement_horizontal_forward_r;
			int complement_vertical_forward_r;

			if ((forward_f == 1) || (motion_horizontal_forward_code == 0))
				complement_horizontal_forward_r = 0;
			else
				complement_horizontal_forward_r = forward_f - 1 - motion_horizontal_forward_r;

			if ((forward_f == 1) || (motion_vertical_forward_code == 0))
				complement_vertical_forward_r = 0;
			else
				complement_vertical_forward_r = forward_f - 1 - motion_vertical_forward_r;

			int right_little;
			int right_big;
			
			right_little = motion_horizontal_forward_code * forward_f;
			if (right_little == 0)
			{
				right_big = 0;
			}
			else
			{
				if (right_little > 0)
				{
					right_little -= complement_horizontal_forward_r;
					right_big = right_little - (32 * forward_f);
				}
				else
				{
					right_little += complement_horizontal_forward_r;
					right_big = right_little + (32 * forward_f);
				}
			}

			int down_little;
			int down_big;

			down_little = motion_vertical_forward_code * forward_f;
			if (down_little == 0)
			{
				down_big = 0;
			}
			else
			{
				if (down_little > 0)
				{
					down_little -= complement_vertical_forward_r;
					down_big = down_little - (32 * forward_f);
				}
				else
				{
					down_little += complement_vertical_forward_r;
					down_big = down_little + (32 * forward_f);
				}
			}

			int max = ( 16 * forward_f) - 1;
			int min = (-16 * forward_f);

			int new_vector;
			
			new_vector = recon_right_for_prev + right_little;
			if ((new_vector <= max) && (new_vector >= min))
				recon_right_for = recon_right_for_prev + right_little;
			else
				recon_right_for = recon_right_for_prev + right_big;
			recon_right_for_prev = recon_right_for;

			boolean full_pel_forward_vector = decoderState.getFullPelForwardVector();
			if (full_pel_forward_vector)
				recon_right_for <<= 1;

			new_vector = recon_down_for_prev + down_little;
			if ((new_vector <= max) && (new_vector >= min))
				recon_down_for = recon_down_for_prev + down_little;
			else
				recon_down_for = recon_down_for_prev + down_big;
			recon_down_for_prev = recon_down_for;

			if (full_pel_forward_vector)
				recon_down_for <<= 1;
			// end of stuff from section 2.4.4.2
		}

		if (getMacroblockMotionBackward())
		{
			mv.parse(bitstream);
			int motion_horizontal_backward_code = mv.getValue();

			int motion_horizontal_backward_r = 0;
			int motion_vertical_backward_r = 0;

			int backward_r_size = backward_f_code - 1;
			int backward_f      = 1 << backward_r_size;			

			if ((backward_f != 1) && (motion_horizontal_backward_code != 0))
				motion_horizontal_backward_r = bitstream.getbits(backward_r_size);

			mv.parse(bitstream);
			int motion_vertical_backward_code = mv.getValue();

			if ((backward_f != 1) && (motion_vertical_backward_code != 0))
				motion_vertical_backward_r = bitstream.getbits(backward_r_size);

			// from section 2.4.4.3 "Predictive-coded macroblocks in B-pictures"
			int complement_horizontal_backward_r;
			int complement_vertical_backward_r;

			if ((backward_f == 1) || (motion_horizontal_backward_code == 0))
				complement_horizontal_backward_r = 0;
			else
				complement_horizontal_backward_r = backward_f - 1 - motion_horizontal_backward_r;

			if ((backward_f == 1) || (motion_vertical_backward_code == 0))
				complement_vertical_backward_r = 0;
			else
				complement_vertical_backward_r = backward_f - 1 - motion_vertical_backward_r;

			int right_little;
			int right_big;
			
			right_little = motion_horizontal_backward_code * backward_f;
			if (right_little == 0)
			{
				right_big = 0;
			}
			else
			{
				if (right_little > 0)
				{
					right_little -= complement_horizontal_backward_r;
					right_big = right_little - (32 * backward_f);
				}
				else
				{
					right_little += complement_horizontal_backward_r;
					right_big = right_little + (32 * backward_f);
				}
			}

			int down_little;
			int down_big;

			down_little = motion_vertical_backward_code * backward_f;
			if (down_little == 0)
			{
				down_big = 0;
			}
			else
			{
				if (down_little > 0)
				{
					down_little -= complement_vertical_backward_r;
					down_big = down_little - (32 * backward_f);
				}
				else
				{
					down_little += complement_vertical_backward_r;
					down_big = down_little + (32 * backward_f);
				}
			}

			int max = ( 16 * backward_f) - 1;
			int min = (-16 * backward_f);

			int new_vector;
			
			new_vector = recon_right_back_prev + right_little;
			if ((new_vector <= max) && (new_vector >= min))
				recon_right_back = recon_right_back_prev + right_little;
			else
				recon_right_back = recon_right_back_prev + right_big;
			recon_right_back_prev = recon_right_back;

			boolean full_pel_backward_vector = decoderState.getFullPelForwardVector();
			if (full_pel_backward_vector)
				recon_right_back <<= 1;

			new_vector = recon_down_back_prev + down_little;
			if ((new_vector <= max) && (new_vector >= min))
				recon_down_back = recon_down_back_prev + down_little;
			else
				recon_down_back = recon_down_back_prev + down_big;
			recon_down_back_prev = recon_down_back;

			if (full_pel_backward_vector)
				recon_down_back <<= 1;
			// end of stuff from section 2.4.4.3
		}

		int coded_block_pattern = 0;
		if (getMacroblockPattern())
		{
			mp.parse(bitstream);
			coded_block_pattern = mp.getValue();
		}

		for (int i = 0; i < 6; i++)
		{
			pattern_code[i] = false;
			if ((coded_block_pattern & (1 << (5 - i))) != 0)
				pattern_code[i] = true;			
			if (getMacroblockIntra())
				pattern_code[i] = true;
		}					

		for (int j = 0; j < 6; j++)
		{
			decoderState.decodeBlock(j, bitstream);
		}

		if (picture_coding_type == PictureCodingTypes.TYPE_D)
		{
			bitstream.skipbits(1);
		}

		decoderState.setPreviousMacroblockAddress(macroblock_address);

		if (getMacroblockIntra())
		{
			decoderState.setPastIntraAddress(macroblock_address);
			if (picture_coding_type == PictureCodingTypes.TYPE_B)
			{
				resetReconForPrev();
				resetReconBackPrev();
			}
		}

		if ((picture_coding_type == PictureCodingTypes.TYPE_P) && (!getMacroblockMotionForward()))
			resetReconForPrev();
	}

	public boolean getMacroblockQuant()
	{
		return macroblock_type.getMacroblockQuant();
	}

	public boolean getMacroblockPattern()
	{
		return macroblock_type.getMacroblockPattern();
	}

	public boolean getMacroblockIntra()
	{
		return macroblock_type.getMacroblockIntra();
	}

	public boolean getMacroblockMotionForward()
	{
		return macroblock_type.getMacroblockMotionForward();
	}

	public boolean getMacroblockMotionBackward()
	{
		return macroblock_type.getMacroblockMotionBackward();
	}

	public int getMacroblockAddress()
	{
		return macroblock_address;
	}

	public boolean getPatternCode(int i)
	{
		return pattern_code[i];
	}

	public void resetReconForPrev()
	{
		recon_right_for_prev = recon_down_for_prev = 0;
	}

	public int getReconRightFor()
	{
		return recon_right_for;
	}

	public int getReconDownFor()
	{
		return recon_down_for;
	}

	public void resetReconFor()
	{
		recon_right_for = recon_down_for = 0;
	}

	public int getReconRightBack()
	{
		return recon_right_back;
	}

	public int getReconDownBack()
	{
		return recon_down_back;
	}

	public void resetReconBack()
	{
		recon_right_back = recon_down_back = 0;
	}

	public void resetReconBackPrev()
	{
		recon_right_back_prev = recon_down_back_prev = 0;
	}
}
