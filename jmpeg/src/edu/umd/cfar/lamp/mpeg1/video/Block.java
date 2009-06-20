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

class Block implements Decodable
{
	private static final int scan[][] =
		{ { 0,  1,  5,  6, 14, 15, 27, 28},
		  { 2,  4,  7, 13, 16, 26, 29, 42},
		  { 3,  8, 12, 17, 25, 30, 41, 43},
		  { 9, 11, 18, 24, 31, 40, 44, 53},
		  {10, 19, 23, 32, 39, 45, 52, 54},
		  {20, 22, 33, 38, 46, 51, 55, 60},
		  {21, 34, 37, 47, 50, 56, 59, 61},
		  {35, 36, 48, 49, 57, 58, 62, 63} };

	// precalculated cosine matrix for inverse DCT
	private static final double c[][] =
		{ {0.35355339059327373,  0.35355339059327373,  0.35355339059327373,  0.35355339059327373,  0.35355339059327373,  0.35355339059327373,  0.35355339059327373,  0.35355339059327373},
		  {0.49039264020161520,  0.41573480615127260,  0.27778511650980114,  0.09754516100806417, -0.09754516100806410, -0.27778511650980100, -0.41573480615127270, -0.49039264020161520},
		  {0.46193976625564337,  0.19134171618254492, -0.19134171618254486, -0.46193976625564337, -0.46193976625564340, -0.19134171618254517,  0.19134171618254500,  0.46193976625564326},
		  {0.41573480615127260, -0.09754516100806410, -0.49039264020161520, -0.27778511650980110,  0.27778511650980090,  0.49039264020161520,  0.09754516100806439, -0.41573480615127256},
		  {0.35355339059327380, -0.35355339059327373, -0.35355339059327384,  0.35355339059327370,  0.35355339059327384, -0.35355339059327334, -0.35355339059327356,  0.35355339059327330},
		  {0.27778511650980114, -0.49039264020161520,  0.09754516100806415,  0.41573480615127280, -0.41573480615127256, -0.09754516100806401,  0.49039264020161530, -0.27778511650980076},
		  {0.19134171618254492, -0.46193976625564340,  0.46193976625564326, -0.19134171618254495, -0.19134171618254528,  0.46193976625564337, -0.46193976625564320,  0.19134171618254478},
		  {0.09754516100806417, -0.27778511650980110,  0.41573480615127280, -0.49039264020161530,  0.49039264020161520, -0.41573480615127250,  0.27778511650980076, -0.09754516100806429} };

	// temporary matrix for inverse DCT
	private double temp[][] = new double[8][8];

	// matrices for decoding DCT coefficients
	private int dct_zz[]      = new int[8*8];
	private int dct_recon[][] = new int[8][8];

	// matrix for holding predicted pel values
	private int pel[][]       = new int[8][8];

	// VLC lookup objects
	private DCTCoefficients_Luminance   lumCoeff = new DCTCoefficients_Luminance();
	private DCTCoefficients_Chrominance chrCoeff = new DCTCoefficients_Chrominance();
	private DCTCoefficientEscapeLevel   escLevel = new DCTCoefficientEscapeLevel();
	private DCTCoefficientFirst         dctFirst = new DCTCoefficientFirst();
	private DCTCoefficientNext          dctNext  = new DCTCoefficientNext();

	// temporary object for holding parsed "level" and "run": used in place of the spec's dct_coeff_first and dct_coeff_next
	private DCTCoefficientValues        dct_coeff = new DCTCoefficientValues();

	// optimization: skip inverseDCT if all reconstructed DCT coefficients are 0
	private boolean skipIDCT = true;


	public void parse(Bitstream bitstream, ParserState parserState) throws IOException
	{
		int blockNum = parserState.getBlockNumber();
		boolean macroblock_intra = parserState.getMacroblockIntra();

		if (parserState.getPatternCode(blockNum))
		{
			if (macroblock_intra)
			{
				if (blockNum < 4) // luminance block (blocks 0-3 are the luminance blocks, blocks 4-5 are chrominance)
				{
					lumCoeff.parse(bitstream);
					int dct_dc_size_luminance = lumCoeff.getValue();

					if (dct_dc_size_luminance != 0)
					{
						int dct_dc_differential = bitstream.getbits(dct_dc_size_luminance);
					}
				}
				else // blockNum >= 4, so this is a chrominance block
				{
					chrCoeff.parse(bitstream);
					int dct_dc_size_chrominance = chrCoeff.getValue();

					if (dct_dc_size_chrominance != 0)
					{
						int dct_dc_differential = bitstream.getbits(dct_dc_size_chrominance);
					}
				}
			}
			else // not a Block in an intra-coded Macroblock
			{
				if (bitstream.nextbits(6) == 1) // VLC escape code
				{
					if (bitstream.getbits(6) != 1)
						throw new ParsingException("Expected constant 000001 not found.");

					// here, dct_coeff is used to mean the spec's dct_coeff_first
					dct_coeff.setRun(bitstream.getbits(6));
					escLevel.parse(bitstream);
					dct_coeff.setLevel(escLevel.getValue());
				}
				else
				{
					dctFirst.parse(bitstream);
					// dct_coeff is still dct_coeff_first here
					dct_coeff = dctFirst.getValue();
				}
			}
			
			if (parserState.getPictureCodingType() != PictureCodingTypes.TYPE_D)
			{
				while (bitstream.nextbits(2) != 2)
				{
					if (bitstream.nextbits(6) == 1) // VLC escape code
					{
						if (bitstream.getbits(6) != 1)
							throw new ParsingException("Expected constant 000001 not found.");

						// from here on, dct_coeff is used to mean the spec's dct_coeff_next
						dct_coeff.setRun(bitstream.getbits(6));
						escLevel.parse(bitstream);
						dct_coeff.setLevel(escLevel.getValue());
					}
					else
					{
						dctNext.parse(bitstream);
						dct_coeff = dctNext.getValue();
					}
				}

				if (bitstream.getbits(2) != 2)
					throw new ParsingException("Expected end_of_block not found.");
			}
		}
	}

	public void decode(Bitstream bitstream, DecoderState decoderState) throws IOException, MpegException
	{
		int blockNum = decoderState.getBlockNumber();
		boolean macroblock_intra = decoderState.getMacroblockIntra();
		int dct_coeff_i = 0;

		Arrays.fill(dct_zz, 0);

		int macroblock_address = decoderState.getMacroblockAddress();
		
		int mb_width = decoderState.getMbWidth();
		int mb_row   = macroblock_address / mb_width;
		int mb_col   = macroblock_address % mb_width;

		// pel_row and pel_col are not actually variable names from the spec
		int pel_row = mb_row * 16;
		int pel_col = mb_col * 16;

		switch (blockNum)
		{
			case 1: pel_col += 8;               break; // upper right block
			case 2: pel_row += 8;               break; // lower left  block
			case 3: pel_row += 8; pel_col += 8; break; // lower right block
		}

		// set to true here, and if any non-zero values are discovered, set to false
		skipIDCT = true;

		if (decoderState.getPatternCode(blockNum))
		{
			// parse
			if (macroblock_intra)
			{
				for (int i = 0; i < 8; i++)
				{
					Arrays.fill(pel[i], 0);
				}
				
				if (blockNum < 4) // luminance block (blocks 0-3 are the luminance blocks, blocks 4-5 are chrominance)
				{
					lumCoeff.parse(bitstream);
					int dct_dc_size_luminance = lumCoeff.getValue();

					if (dct_dc_size_luminance != 0)
					{
						int dct_dc_differential = bitstream.getbits(dct_dc_size_luminance);
						if ((dct_dc_differential&(1<<(dct_dc_size_luminance-1))) != 0)
							dct_zz[0] = dct_dc_differential;
						else
							dct_zz[0] = ((-1<<dct_dc_size_luminance) | (dct_dc_differential+1));
					}
				}
				else // blockNum >= 4, so this is a chrominance block
				{
					chrCoeff.parse(bitstream);
					int dct_dc_size_chrominance = chrCoeff.getValue();

					if (dct_dc_size_chrominance != 0)
					{
						int dct_dc_differential = bitstream.getbits(dct_dc_size_chrominance);
						if ((dct_dc_differential&(1<<(dct_dc_size_chrominance-1))) != 0)
							dct_zz[0] = dct_dc_differential;
						else
							dct_zz[0] = ((-1<<dct_dc_size_chrominance) | (dct_dc_differential+1));
					}
				}

				dct_coeff_i = 0;
			}
			else // not a Block in an intra-coded Macroblock
			{
				if (bitstream.nextbits(6) == 1) // VLC escape code
				{
					bitstream.skipbits(6);
					dct_coeff.setRun(bitstream.getbits(6));
					escLevel.parse(bitstream);
					dct_coeff.setLevel(escLevel.getValue());
				}
				else
				{
					dctFirst.parse(bitstream);
					dct_coeff = dctFirst.getValue();
				}

				dct_coeff_i = dct_coeff.getRun();
				dct_zz[dct_coeff_i] = dct_coeff.getLevel(); // negatives are handled in the VLC lookup
			}
			
			if (decoderState.getPictureCodingType() != PictureCodingTypes.TYPE_D)
			{
				while (bitstream.nextbits(2) != 2)
				{
					if (bitstream.nextbits(6) == 1) // VLC escape code
					{
						bitstream.skipbits(6);
						dct_coeff.setRun(bitstream.getbits(6));
						escLevel.parse(bitstream);
						dct_coeff.setLevel(escLevel.getValue());
					}
					else
					{
						dctNext.parse(bitstream);
						dct_coeff = dctNext.getValue();
					}

					dct_coeff_i += dct_coeff.getRun() + 1;
					dct_zz[dct_coeff_i] = dct_coeff.getLevel(); // negatives are handled in the VLC lookup
				}

				if (bitstream.getbits(2) != 2)
					throw new ParsingException("Expected end_of_block not found.");
			}

			// inverse quantize (fills in dct_recon[][] with DCT coefficients)
			int quantizer_scale = decoderState.getQuantizerScale();
			
			if (macroblock_intra)
			{
				int intra_quant[][] = decoderState.getIntraQuantizerMatrix();
				int past_intra_address = decoderState.getPastIntraAddress();

				for (int m = 0; m < 8; m++)
				{
					for (int n = 0; n < 8; n++)
					{
						int idx = scan[m][n];
						int scratch = (dct_zz[idx] * quantizer_scale * intra_quant[m][n]) / 8;
						if ((scratch & 1) == 0)
							scratch -= sign(scratch);
						if (scratch > 2047)
							scratch = 2047;
						if (scratch < -2048)
							scratch = -2048;
						dct_recon[m][n] = scratch;

						if (scratch != 0)
							skipIDCT = false;
					}
				}

				switch (blockNum)
				{
					case 0: // luminance (Y) block 0
						dct_recon[0][0] = dct_zz[0] * 8;
						if ((macroblock_address - past_intra_address) > 1)
							dct_recon[0][0] += 1024;
						else
							dct_recon[0][0] += decoderState.getDctDcYPast();
						decoderState.setDctDcYPast(dct_recon[0][0]);
						break;
					case 1: // luminance (Y) blocks 1,2,3 use the same algorithm
					case 2: // luminance (Y) blocks 1,2,3 use the same algorithm
					case 3: // luminance (Y) blocks 1,2,3 use the same algorithm
						dct_recon[0][0] = decoderState.getDctDcYPast() + (dct_zz[0] * 8);
						decoderState.setDctDcYPast(dct_recon[0][0]);
						break;
					case 4: // Cb block
						dct_recon[0][0] = dct_zz[0] * 8;
						if ((macroblock_address - past_intra_address) > 1)
							dct_recon[0][0] += 1024;
						else
							dct_recon[0][0] += decoderState.getDctDcCbPast();
						decoderState.setDctDcCbPast(dct_recon[0][0]);
						break;
					case 5: // Cr block
						dct_recon[0][0] = dct_zz[0] * 8;
						if ((macroblock_address - past_intra_address) > 1)
							dct_recon[0][0] += 1024;
						else
							dct_recon[0][0] += decoderState.getDctDcCrPast();
						decoderState.setDctDcCrPast(dct_recon[0][0]);
						break;
				}
			}
			else // non-intracoded macroblock
			{
				int non_intra_quant[][] = decoderState.getNonIntraQuantizerMatrix();
				
				for (int m = 0; m < 8; m++)
				{
					for (int n = 0; n < 8; n++)
					{
						int idx = scan[m][n];
						int scratch = (((2 * dct_zz[idx]) + sign(dct_zz[idx])) * quantizer_scale * non_intra_quant[m][n]) / 16;
						if ((scratch & 1) == 0)
							scratch -= sign(scratch);
						if (scratch > 2047)
							scratch = 2047;
						if (scratch < -2048)
							scratch = -2048;
						if (dct_zz[idx] == 0)
							scratch = 0;
						dct_recon[m][n] = scratch;

						if (scratch != 0)
							skipIDCT = false;
					}
				}
			}
		} // end if getPatternCode(blockNum)
		else
		{
			for (int i = 0; i < 8; i++)
			{
				Arrays.fill(dct_recon[i], 0);
			}
		}

		if (dct_recon[0][0] != 0)
			skipIDCT = false;

		if (!macroblock_intra)
		{
			switch (decoderState.getPictureCodingType())
			{
				case PictureCodingTypes.TYPE_P:
					getPelsFromPast(decoderState, blockNum, pel_row, pel_col);
					break;
				case PictureCodingTypes.TYPE_B:
					motionCompensateBPicture(decoderState, blockNum, pel_row, pel_col);
					break;
			}
		}

		if (!skipIDCT)
			inverseDCT();

		draw(decoderState, blockNum, pel_row, pel_col);
	}

	// According to spec page 11
	private int sign(int value)
	{
		return (value > 0) ? 1 : (value < 0) ? -1 : 0;
	}

	// According to spec page 11
	private int doubleSlash(int left, int right)
	{
		assert right == 2 || right == 4;
		int myresult;
		if (right == 2) {
			if (left < 0) {
				myresult = -((-(left+1)) >> 1);
			} else {
				myresult = (left+1) >> 1;
			}
		} else if (right == 4) {
			if (left < 0) {
				myresult = -((-(left+2)) >> 2);
			} else {
				myresult = (left+2) >> 2;
			}
		} else {
			float fresult = (float)left / (float)right;
			myresult = left / right;
			if (Math.abs(fresult - myresult) >= 0.5f)
			{
				myresult += (myresult >= 0) ? 1 : -1;
			}
		}

		return myresult;
	}
	
	public void motionCompensateBPicture(DecoderState decoderState, int blockNum, int pel_row, int pel_col) throws IOException, MpegException
	{
		if (decoderState.getMacroblockMotionForward())
		{
			getPelsFromPast(decoderState, blockNum, pel_row, pel_col);
			if (decoderState.getMacroblockMotionBackward())
				getPelsFromFuture(decoderState, blockNum, pel_row, pel_col, true);
		}
		else // no forward motion vector, non-intra, must have a backward motion vector
		{
			getPelsFromFuture(decoderState, blockNum, pel_row, pel_col, false);
		}
	}
	
	private void getPelsFromPast(DecoderState decoderState, int blockNum, int pel_row, int pel_col) throws IOException, MpegException
	{
		int recon_right_for = decoderState.getReconRightFor();
		int recon_down_for  = decoderState.getReconDownFor();
		
		int right_for = 0;
		int down_for  = 0;
		boolean right_half_for = false;
		boolean down_half_for  = false;
		int twoIplusTwoDownForPlusPelRow = 0;
		int twoJplusTwoRightForPlusPelCol = 0;

		switch (blockNum)
		{
			case 0: // all luminance blocks use the same formula
			case 1:
			case 2:
			case 3:
				right_for = recon_right_for >> 1;
				down_for  = recon_down_for  >> 1;
				right_half_for = ((recon_right_for - (2 * right_for)) != 0);
				down_half_for  = ((recon_down_for  - (2 * down_for))  != 0);

				for (int i = 0; i < 8; i++)
				{
					for (int j = 0; j < 8; j++)
					{
						if (!right_half_for)
						{
							if (!down_half_for)
								pel[i][j] = decoderState.getPastY(pel_row + i + down_for, pel_col + j + right_for);
							else
								pel[i][j] = doubleSlash(
									decoderState.getPastY(pel_row + i + down_for,     pel_col + j + right_for)
								  + decoderState.getPastY(pel_row + i + down_for + 1, pel_col + j + right_for), 2);
						}
						else // right_half_for is true
						{
							if (!down_half_for)
								pel[i][j] = doubleSlash(
									decoderState.getPastY(pel_row + i + down_for, pel_col + j + right_for)
								  + decoderState.getPastY(pel_row + i + down_for, pel_col + j + right_for + 1), 2);
							else
								pel[i][j] = doubleSlash(
									decoderState.getPastY(pel_row + i + down_for,     pel_col + j + right_for)
								  + decoderState.getPastY(pel_row + i + down_for + 1, pel_col + j + right_for)
								  + decoderState.getPastY(pel_row + i + down_for,     pel_col + j + right_for + 1)
								  + decoderState.getPastY(pel_row + i + down_for + 1, pel_col + j + right_for + 1), 4);
						}
					}
				}
				break;
			case 4:
				right_for = (recon_right_for / 2) >> 1;
				down_for  = (recon_down_for  / 2) >> 1;
				right_half_for = ((recon_right_for / 2 - (2 * right_for)) != 0);
				down_half_for  = ((recon_down_for  / 2 - (2 * down_for))  != 0);

				for (int i = 0; i < 8; i++)
				{
					twoIplusTwoDownForPlusPelRow = 2*(i + down_for) + pel_row;
					for (int j = 0; j < 8; j++)
					{
						twoJplusTwoRightForPlusPelCol = 2*(j + right_for) + pel_col;
						if (!right_half_for)
						{
							if (!down_half_for)
								pel[i][j] = decoderState.getPastCb(twoIplusTwoDownForPlusPelRow, twoJplusTwoRightForPlusPelCol);
							else
								pel[i][j] = doubleSlash(
									decoderState.getPastCb(twoIplusTwoDownForPlusPelRow,     twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCb(twoIplusTwoDownForPlusPelRow + 2, twoJplusTwoRightForPlusPelCol), 2);
						}
						else // right_half_for is true
						{
							if (!down_half_for)
								pel[i][j] = doubleSlash(
									decoderState.getPastCb(twoIplusTwoDownForPlusPelRow, twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCb(twoIplusTwoDownForPlusPelRow, twoJplusTwoRightForPlusPelCol + 2), 2);
							else
								pel[i][j] = doubleSlash(
									decoderState.getPastCb(twoIplusTwoDownForPlusPelRow,     twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCb(twoIplusTwoDownForPlusPelRow + 2, twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCb(twoIplusTwoDownForPlusPelRow,     twoJplusTwoRightForPlusPelCol + 2)
								  + decoderState.getPastCb(twoIplusTwoDownForPlusPelRow + 2, twoJplusTwoRightForPlusPelCol + 2), 4);
						}
					}
				}
				break;
			case 5:
				right_for = (recon_right_for / 2) >> 1;
				down_for  = (recon_down_for  / 2) >> 1;
				right_half_for = ((recon_right_for / 2 - (2 * right_for)) != 0);
				down_half_for  = ((recon_down_for  / 2 - (2 * down_for))  != 0);

				for (int i = 0; i < 8; i++)
				{
					twoIplusTwoDownForPlusPelRow = 2*(i + down_for) + pel_row;
					for (int j = 0; j < 8; j++)
					{
						twoJplusTwoRightForPlusPelCol = 2*(j + right_for) + pel_col;
						if (!right_half_for)
						{
							if (!down_half_for)
								pel[i][j] = decoderState.getPastCr(twoIplusTwoDownForPlusPelRow, twoJplusTwoRightForPlusPelCol);
							else
								pel[i][j] = doubleSlash(
									decoderState.getPastCr(twoIplusTwoDownForPlusPelRow,     twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCr(twoIplusTwoDownForPlusPelRow + 2, twoJplusTwoRightForPlusPelCol), 2);
						}
						else // right_half_for is true
						{
							if (!down_half_for)
								pel[i][j] = doubleSlash(
									decoderState.getPastCr(twoIplusTwoDownForPlusPelRow, twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCr(twoIplusTwoDownForPlusPelRow, twoJplusTwoRightForPlusPelCol + 2), 2);
							else
								pel[i][j] = doubleSlash(
									decoderState.getPastCr(twoIplusTwoDownForPlusPelRow,     twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCr(twoIplusTwoDownForPlusPelRow + 2, twoJplusTwoRightForPlusPelCol)
								  + decoderState.getPastCr(twoIplusTwoDownForPlusPelRow,     twoJplusTwoRightForPlusPelCol + 2)
								  + decoderState.getPastCr(twoIplusTwoDownForPlusPelRow + 2, twoJplusTwoRightForPlusPelCol + 2), 4);
						}
					}
				}
				break;
		}
	}

	private void getPelsFromFuture(DecoderState decoderState, int blockNum, int pel_row, int pel_col, boolean averageWithExisting) throws IOException, MpegException
	{
		int recon_right_back = decoderState.getReconRightBack();
		int recon_down_back  = decoderState.getReconDownBack();

		int right_back = 0;
		int down_back  = 0;
		boolean right_half_back = false;
		boolean down_half_back  = false;
		int twoIplusTwoDownBackPlusPelRow = 0;
		int twoJplusTwoRightBackPlusPelCol = 0;
		int result = 0;

		switch (blockNum)
		{
			case 0: // all luminance blocks use the same formula
			case 1:
			case 2:
			case 3:
				right_back = recon_right_back >> 1;
				down_back  = recon_down_back  >> 1;
				right_half_back = ((recon_right_back - (2 * right_back)) != 0);
				down_half_back  = ((recon_down_back  - (2 * down_back))  != 0);

				for (int i = 0; i < 8; i++)
				{
					for (int j = 0; j < 8; j++)
					{
						if (!right_half_back)
						{
							if (!down_half_back)
								result = decoderState.getFutureY(pel_row + i + down_back, pel_col + j + right_back);
							else
								result = doubleSlash(
									decoderState.getFutureY(pel_row + i + down_back,     pel_col + j + right_back)
								  + decoderState.getFutureY(pel_row + i + down_back + 1, pel_col + j + right_back), 2);
						}
						else // right_half_back is true
						{
							if (!down_half_back)
								result = doubleSlash(
									decoderState.getFutureY(pel_row + i + down_back, pel_col + j + right_back)
								  + decoderState.getFutureY(pel_row + i + down_back, pel_col + j + right_back + 1), 2);
							else
								result = doubleSlash(
									decoderState.getFutureY(pel_row + i + down_back,     pel_col + j + right_back)
								  + decoderState.getFutureY(pel_row + i + down_back + 1, pel_col + j + right_back)
								  + decoderState.getFutureY(pel_row + i + down_back,     pel_col + j + right_back + 1)
								  + decoderState.getFutureY(pel_row + i + down_back + 1, pel_col + j + right_back + 1), 4);
						}

						if (averageWithExisting)
							pel[i][j] = doubleSlash(result + pel[i][j], 2);
						else
							pel[i][j] = result;
					}
				}
				break;
			case 4:
				right_back = (recon_right_back / 2) >> 1;
				down_back  = (recon_down_back  / 2) >> 1;
				right_half_back = ((recon_right_back / 2 - (2 * right_back)) != 0);
				down_half_back  = ((recon_down_back  / 2 - (2 * down_back))  != 0);

				for (int i = 0; i < 8; i++)
				{
					twoIplusTwoDownBackPlusPelRow = 2*(i + down_back) + pel_row;
					for (int j = 0; j < 8; j++)
					{
						twoJplusTwoRightBackPlusPelCol = 2*(j + right_back) + pel_col;
						if (!right_half_back)
						{
							if (!down_half_back)
								result = decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow, twoJplusTwoRightBackPlusPelCol);
							else
								result = doubleSlash(
									decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow,     twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow + 2, twoJplusTwoRightBackPlusPelCol), 2);
						}
						else // right_half_back is true
						{
							if (!down_half_back)
								result = doubleSlash(
									decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow, twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow, twoJplusTwoRightBackPlusPelCol + 2), 2);
							else
								result = doubleSlash(
									decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow,     twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow + 2, twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow,     twoJplusTwoRightBackPlusPelCol + 2)
								  + decoderState.getFutureCb(twoIplusTwoDownBackPlusPelRow + 2, twoJplusTwoRightBackPlusPelCol + 2), 4);
						}

						if (averageWithExisting)
							pel[i][j] = doubleSlash(result + pel[i][j], 2);
						else
							pel[i][j] = result;
					}
				}
				break;
			case 5:
				right_back = (recon_right_back / 2) >> 1;
				down_back  = (recon_down_back  / 2) >> 1;
				right_half_back = ((recon_right_back / 2 - (2 * right_back)) != 0);
				down_half_back  = ((recon_down_back  / 2 - (2 * down_back))  != 0);

				for (int i = 0; i < 8; i++)
				{
					twoIplusTwoDownBackPlusPelRow = 2*(i + down_back) + pel_row;
					for (int j = 0; j < 8; j++)
					{
						twoJplusTwoRightBackPlusPelCol = 2*(j + right_back) + pel_col;
						if (!right_half_back)
						{
							if (!down_half_back)
								result = decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow, twoJplusTwoRightBackPlusPelCol);
							else
								result = doubleSlash(
									decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow,     twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow + 2, twoJplusTwoRightBackPlusPelCol), 2);
						}
						else // right_half_back is true
						{
							if (!down_half_back)
								result = doubleSlash(
									decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow, twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow, twoJplusTwoRightBackPlusPelCol + 2), 2);
							else
								result = doubleSlash(
									decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow,     twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow + 2, twoJplusTwoRightBackPlusPelCol)
								  + decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow,     twoJplusTwoRightBackPlusPelCol + 2)
								  + decoderState.getFutureCr(twoIplusTwoDownBackPlusPelRow + 2, twoJplusTwoRightBackPlusPelCol + 2), 4);
						}

						if (averageWithExisting)
							pel[i][j] = doubleSlash(result + pel[i][j], 2);
						else
							pel[i][j] = result;

					}
				}
				break;
		}
	}

	public void draw(DecoderState decoderState, int blockNum, int pel_row, int pel_col) throws IOException, MpegException
	{
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				int a,b,c,d;
				int result = pel[i][j];
				switch (blockNum)
				{
					case 0:
					case 1: 
					case 2:
					case 3:
						decoderState.setCurrentY(i + pel_row, j + pel_col, result);
						break;
					case 4: // blocks 4 (Cb) and 5 (Cr) need to be upsampled to 4x4 block size
						a = 2*i + pel_row;
						b = a + 1;
						c = 2*j + pel_col;
						d = c + 1;
						decoderState.setCurrentCb(a, c, result);
						decoderState.setCurrentCb(a, d, result);
						decoderState.setCurrentCb(b, c, result);
						decoderState.setCurrentCb(b, d, result);
						break;
					case 5:
						a = 2*i + pel_row;
						b = a + 1;
						c = 2*j + pel_col;
						d = c + 1;
						decoderState.setCurrentCr(a, c, result);
						decoderState.setCurrentCr(a, d, result);
						decoderState.setCurrentCr(b, c, result);
						decoderState.setCurrentCr(b, d, result);
						break;
				}
			}
		}
	}

	// inverse DCT (converts DCT coefficients to actual pel values)
	private void inverseDCT()
	{
		// inverse DCT implementation modified from one cribbed from Stephen Manley (http://www.nyx.net/~smanley/)
		double temp1;

		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				temp[i][j] = 0.0;

				for (int k = 0; k < 8; k++)
				{
					temp[i][j] += dct_recon[i][k] * c[k][j];
				}
			}
		}

		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				temp1 = 0.0;

				for (int k = 0; k < 8; k++)
				{
					temp1 += c[k][i] * temp[k][j];
				}

				pel[i][j] = VideoDecoder.clamp(pel[i][j] + (int)Math.round(temp1));
			}
		}
	}
}
