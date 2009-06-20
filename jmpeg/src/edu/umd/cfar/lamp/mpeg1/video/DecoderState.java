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

class DecoderState extends ParserState
{
	private VideoDecoder   videoDecoder          = null;
	private SequenceHeader currentSequenceHeader = null;

	private int dct_dc_y_past  = 1024;
	private int dct_dc_cb_past = 1024;
	private int dct_dc_cr_past = 1024;

	private int past_intra_address = -2;
	private int previous_macroblock_address;

	private int quantizer_scale;

	private int currentYCbCr[];
	private int pastYCbCr[];
	private int futureYCbCr[];
	private int frameInCurrent = -1;
	private int frameInPast    = -1;
	private int frameInFuture  = -1;

	
	public DecoderState(VideoDecoder videoDecoder) throws IOException, MpegException
	{
		this.videoDecoder = videoDecoder;
		currentSequenceHeader = videoDecoder.getSequenceHeader(0);

		currentYCbCr = new int[getFrameWidth() * getFrameHeight()];
		pastYCbCr    = new int[getFrameWidth() * getFrameHeight()];
		futureYCbCr  = new int[getFrameWidth() * getFrameHeight()];

		Arrays.fill(currentYCbCr, 0xFF108080); // black
		Arrays.fill(pastYCbCr,    0xFF108080); // black
		Arrays.fill(futureYCbCr,  0xFF108080); // black
	}

	public void decodePicture(Bitstream bitstream) throws IOException, MpegException
	{
		picture.decode(bitstream, this);
	}
	
	public void decodeSlice(Bitstream bitstream) throws IOException, MpegException
	{
		slice.decode(bitstream, this);
	}
	
	public void decodeMacroblock(Bitstream bitstream) throws IOException, MpegException
	{
		macroblock.decode(bitstream, this);
	}
	
	public void decodeBlock(int blockNum, Bitstream bitstream) throws IOException, MpegException
	{
		this.blockNum = blockNum;
		block.decode(bitstream, this);
	}

	public void seek(int frame) throws IOException, MpegException
	{
		if (frame != frameInCurrent) // no need to decode the frame if you already have it
		{
			switch (videoDecoder.getPictureCodingType(frame))
			{
				case PictureCodingTypes.TYPE_I: decodeI(frame); break;
				case PictureCodingTypes.TYPE_P: decodeP(frame); break;
				case PictureCodingTypes.TYPE_B: decodeB(frame); break;
				case PictureCodingTypes.TYPE_D: decodeD(frame); break;
			}
		}
	}

	/**
	 * Decode a picture.
	 * Assumed the environment is correctly set up (right frames in right 
	 * buffers, etc)
	 * @param frame the frame to decode
	 * @throws IOException
	 * @throws MpegException
	 */
	private void decode(int frame) throws IOException, MpegException
	{
		currentSequenceHeader   = videoDecoder.getSequenceHeader(frame);
		long        position    = videoDecoder.getPosition(frame);
		VideoSource videoSource = videoDecoder.getVideoSource();
		videoSource.seek(position);
		Bitstream   bitstream   = new Bitstream(videoSource);
		decodePicture(bitstream);
		frameInCurrent = frame;
	}

	private void decodeIOrP(int frame) throws IOException, MpegException
	{
		switch (videoDecoder.getPictureCodingType(frame))
		{
			case PictureCodingTypes.TYPE_I: decodeI(frame); break;
			case PictureCodingTypes.TYPE_P: decodeP(frame); break;
		}
	}
	
	private void decodeI(int frame) throws IOException, MpegException
	{
		if (frameInPast == frame)
			swapCurrentWithPast();
		else if (frameInFuture == frame)
			swapCurrentWithFuture();
		else
			decode(frame);
	}

	private void decodeP(int frame) throws IOException, MpegException
	{
		// lastIOrP we want in the pastYCbCr buffer		
		int lastIOrP = videoDecoder.getLastIOrPFrame(frame);

		// speed optimization: no need to do all the recursive decoding if you've already got the frame
		if (frameInPast == frame)
			swapCurrentWithPast();
		else if (frameInFuture == frame)
			swapCurrentWithFuture();
		else // will have to decode at least the current frame
		{
			if (frameInCurrent == lastIOrP)
				swapCurrentWithPast();
			else if (frameInFuture == lastIOrP)
				swapPastWithFuture();
			else if (frameInPast != lastIOrP)
			{
				decodeIOrP(lastIOrP);
				swapCurrentWithPast();
			}
			decode(frame);
		}
	}

	private void decodeB(int frame) throws IOException, MpegException
	{
		if (frameInPast == frame)
			swapCurrentWithPast();
		else if (frameInFuture == frame)
			swapCurrentWithFuture();
		else
		{
			// lastIOrP we want in the futureYCbCr buffer
			int lastIOrP = videoDecoder.getLastIOrPFrame(frame);

			// secondLastIOrP we want in the pastYCbCr buffer
			int secondLastIOrP = -1;
			try
			{
				secondLastIOrP = videoDecoder.getLastIOrPFrame(lastIOrP);
			}
			catch (FrameNotFoundException fnfe)
			{
				// could be that the sequence begins with backward-predicted B frames
				//   in which case, basically treat it like a P frame but with lastIOrP in future instead of past
				decodeIOrP(lastIOrP);
				swapCurrentWithFuture();
				decode(frame);
				return;
			}

			if (frameInPast == secondLastIOrP)
			{
				if (frameInCurrent == lastIOrP)
				{
					swapCurrentWithFuture();
					decode(frame);
				}
				else // frame in current is not anything useful
				{
					if (frameInFuture == lastIOrP)
					{
						decode(frame);
					}
					else
					{
						decodeIOrP(lastIOrP);
						swapCurrentWithFuture();
						decode(frame);
					}
				}
			}
			else if (frameInPast == lastIOrP)
			{
				if (frameInCurrent == secondLastIOrP)
				{
					swapCurrentWithPast();
					swapCurrentWithFuture();
					decode(frame);
				}
				else // frameInCurrent is not anything useful
				{
					if (frameInFuture == secondLastIOrP)
					{
						swapPastWithFuture();
						decode(frame);
					}
					else // frameInFuture is not anything useful
					{
						swapPastWithFuture();
						decodeIOrP(secondLastIOrP);
						swapCurrentWithPast();
						decode(frame);
					}
				}
			}
			else // frameInPast is not anything useful
			{
				if (frameInCurrent == secondLastIOrP)
				{
					if (frameInFuture == lastIOrP)
					{
						swapCurrentWithPast();
						decode(frame);
					}
					else // frameInFuture is not anything useful
					{
						swapCurrentWithPast();
						decodeIOrP(lastIOrP);
						swapCurrentWithFuture();
						decode(frame);
					}
				}
				else if (frameInCurrent == lastIOrP)
				{
					if (frameInFuture == secondLastIOrP)
					{
						swapCurrentWithFuture();
						swapCurrentWithPast();
						decode(frame);
					}
					else // frameInFuture is not anything useful
					{
						swapCurrentWithFuture();
						decodeIOrP(secondLastIOrP);
						swapCurrentWithPast();
						decode(frame);
					}
				}
				else // frameInCurrent is not anything useful
				{
					if (frameInFuture == secondLastIOrP)
					{
						swapPastWithFuture();
						decodeIOrP(lastIOrP);
						swapCurrentWithFuture();
						decode(frame);
					}
					else if (frameInFuture == lastIOrP)
					{
						decodeIOrP(secondLastIOrP);
						swapCurrentWithPast();
						decode(frame);
					}
					else // frameInFuture is not anything useful
					{
						decodeIOrP(secondLastIOrP);
						swapCurrentWithPast();
						decodeIOrP(lastIOrP);
						swapCurrentWithFuture();
						decode(frame);
					}
				}
			}
		}
	}

	private void decodeD(int frame) throws IOException, MpegException
	{
	}

	public void resetDctDcPast()
	{
		dct_dc_y_past = dct_dc_cb_past = dct_dc_cr_past = 1024;
	}

	public void setQuantizerScale(int newValue)
	{
		quantizer_scale = newValue;
	}

	public int getQuantizerScale()
	{
		return quantizer_scale;
	}

	public void setPreviousMacroblockAddress(int newValue)
	{
		previous_macroblock_address = newValue;
	}

	public int getPreviousMacroblockAddress()
	{
		return previous_macroblock_address;
	}
	
	public int[][] getIntraQuantizerMatrix() throws IOException, MpegException
	{
		return currentSequenceHeader.getIntraQuantizerMatrix();
	}

	public int[][] getNonIntraQuantizerMatrix() throws IOException, MpegException
	{
		return currentSequenceHeader.getNonIntraQuantizerMatrix();
	}

	public int getMacroblockAddress()
	{
		return macroblock.getMacroblockAddress();
	}

	public int getMbWidth() throws IOException, MpegException
	{
		return currentSequenceHeader.getMbWidth();
	}
	
	public int getFrameWidth() throws IOException, MpegException
	{
		return currentSequenceHeader.getFrameWidth();
	}

	public int getFrameHeight() throws IOException, MpegException
	{
		return currentSequenceHeader.getFrameHeight();
	}

	public int getDctDcYPast()
	{
		return dct_dc_y_past;
	}

	public int getDctDcCbPast()
	{
		return dct_dc_cb_past;
	}

	public int getDctDcCrPast()
	{
		return dct_dc_cr_past;
	}

	public void setDctDcYPast(int newValue)
	{
		dct_dc_y_past = newValue;
	}

	public void setDctDcCbPast(int newValue)
	{
		dct_dc_cb_past = newValue;
	}

	public void setDctDcCrPast(int newValue)
	{
		dct_dc_cr_past = newValue;
	}

	public int getPastIntraAddress()
	{
		return past_intra_address;
	}

	public void setPastIntraAddress(int newValue)
	{
		past_intra_address = newValue;
	}

	public int[] getCurrentYCbCr() throws IOException, MpegException
	{
		return currentYCbCr;
	}

	public void setCurrentY(int index, int newY)
	{
		int pixel = currentYCbCr[index] & 0xFF00FFFF;
		pixel |= ((newY << 16) & 0x00FF0000);
		currentYCbCr[index] = pixel;
	}

	public void setCurrentCb(int index, int newCb)
	{
		int pixel = currentYCbCr[index] & 0xFFFF00FF;
		pixel |= ((newCb << 8) & 0x0000FF00);
		currentYCbCr[index] = pixel;
	}

	public void setCurrentCr(int index, int newCr)
	{
		int pixel = currentYCbCr[index] & 0xFFFFFF00;
		pixel |= (newCr & 0x000000FF);
		currentYCbCr[index] = pixel;
	}

	public void setCurrentY(int row, int col, int newY) throws IOException, MpegException
	{
		setCurrentY(row*getFrameWidth() + col, newY);
	}

	public void setCurrentCb(int row, int col, int newCb) throws IOException, MpegException
	{
		setCurrentCb(row*getFrameWidth() + col, newCb);
	}

	public void setCurrentCr(int row, int col, int newCr) throws IOException, MpegException
	{
		setCurrentCr(row*getFrameWidth() + col, newCr);
	}

	public int getPastY(int index)
	{
		return ((pastYCbCr[index] >>> 16) & 0x000000FF);
	}

	public int getPastCb(int index)
	{
		return ((pastYCbCr[index] >>> 8) & 0x000000FF);
	}

	public int getPastCr(int index)
	{
		return (pastYCbCr[index] & 0x000000FF);
	}

	public int getPastY(int row, int col) throws IOException, MpegException
	{
		return getPastY(row*getFrameWidth() + col);
	}

	public int getPastCb(int row, int col) throws IOException, MpegException
	{
		return getPastCb(row*getFrameWidth() + col);
	}
	
	public int getPastCr(int row, int col) throws IOException, MpegException
	{
		return getPastCr(row*getFrameWidth() + col);
	}

	public int getFutureY(int index)
	{
		return ((futureYCbCr[index] >>> 16) & 0x000000FF);
	}

	public int getFutureCb(int index)
	{
		return ((futureYCbCr[index] >>> 8) & 0x000000FF);
	}

	public int getFutureCr(int index)
	{
		return (futureYCbCr[index] & 0x000000FF);
	}

	public int getFutureY(int row, int col) throws IOException, MpegException
	{
		return getFutureY(row*getFrameWidth() + col);
	}

	public int getFutureCb(int row, int col) throws IOException, MpegException
	{
		return getFutureCb(row*getFrameWidth() + col);
	}
	
	public int getFutureCr(int row, int col) throws IOException, MpegException
	{
		return getFutureCr(row*getFrameWidth() + col);
	}

	public void resetReconForPrev()
	{
		macroblock.resetReconForPrev();
	}

	public int getReconRightFor()
	{
		return macroblock.getReconRightFor();
	}

	public int getReconDownFor()
	{
		return macroblock.getReconDownFor();
	}

	public void resetReconBackPrev()
	{
		macroblock.resetReconBackPrev();
	}

	public int getReconRightBack()
	{
		return macroblock.getReconRightBack();
	}

	public int getReconDownBack()
	{
		return macroblock.getReconDownBack();
	}

	public boolean getFullPelForwardVector()
	{
		return picture.getFullPelForwardVector();
	}

	public boolean getMacroblockMotionForward()
	{
		return macroblock.getMacroblockMotionForward();
	}

	public boolean getMacroblockMotionBackward()
	{
		return macroblock.getMacroblockMotionBackward();
	}

	public void copyMacroblockFromPastToCurrent(int macroblock_address) throws IOException, MpegException
	{
		int mb_row     = macroblock_address / getMbWidth();
		int mb_column  = macroblock_address % getMbWidth();
		int pel_row    = mb_row    * 16;
		int pel_col    = mb_column * 16;
		int frameWidth = getFrameWidth();
		for (int i = 0; i < 16; i++)
		{
			int pos = (pel_row+i)*frameWidth + pel_col;
			System.arraycopy(pastYCbCr, pos, currentYCbCr, pos, 16);
		}
	}

	public void fillInSkippedBPictureMacroblock(int macroblock_address) throws IOException, MpegException
	{
		int mb_row           = macroblock_address / getMbWidth();
		int mb_column        = macroblock_address % getMbWidth();
		int original_pel_row = mb_row    * 16;
		int original_pel_col = mb_column * 16;
		int pel_row          = 0;
		int pel_col          = 0;
		int frameWidth       = getFrameWidth();

		for (int j = 0; j < 6; j++)
		{
			pel_row = original_pel_row;
			pel_col = original_pel_col;
			switch (j)
			{
				case 1: pel_col += 8;               break; // upper right block
				case 2: pel_row += 8;               break; // lower left  block
				case 3: pel_row += 8; pel_col += 8; break; // lower right block
			}
			block.motionCompensateBPicture(this, j, pel_row, pel_col);
			block.draw(this, j, pel_row, pel_col);
		}
	}

	public void swapPastWithFuture()
	{
		int temp[]  = pastYCbCr;
		pastYCbCr   = futureYCbCr;
		futureYCbCr = temp;

		int tempFrame = frameInPast;
		frameInPast   = frameInFuture;
		frameInFuture = tempFrame;
	}

	public void swapCurrentWithPast()
	{
		int temp[]   = currentYCbCr;
		currentYCbCr = pastYCbCr;
		pastYCbCr    = temp;

		int tempFrame  = frameInCurrent;
		frameInCurrent = frameInPast;
		frameInPast    = tempFrame;
	}

	public void swapCurrentWithFuture()
	{
		int temp[]   = currentYCbCr;
		currentYCbCr = futureYCbCr;
		futureYCbCr  = temp;

		int tempFrame  = frameInCurrent;
		frameInCurrent = frameInFuture;
		frameInFuture  = tempFrame;
	}
}
