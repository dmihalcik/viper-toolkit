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

class ParserState 
{
	protected SequenceHeader  sh             = new SequenceHeader();
	protected GroupOfPictures gop            = new GroupOfPictures();
	protected Picture         picture        = new Picture();
	protected Slice           slice          = new Slice();
	protected Macroblock      macroblock     = new Macroblock();
	protected Block           block          = new Block();

	protected int             blockNum       = 0;


	public void parseSequenceHeader(Bitstream bitstream) throws IOException
	{
		sh.parse(bitstream);
	}
	
	public void parseGroupOfPictures(Bitstream bitstream) throws IOException
	{
		gop.parse(bitstream, this);
	}

	public void parsePicture(Bitstream bitstream) throws IOException
	{
		picture.parse(bitstream, this);
	}

	public void parseSlice(Bitstream bitstream) throws IOException
	{
		slice.parse(bitstream, this);
	}

	public void parseMacroblock(Bitstream bitstream) throws IOException
	{
		macroblock.parse(bitstream, this);
	}

	public void parseBlock(int blockNum, Bitstream bitstream) throws IOException
	{
		this.blockNum = blockNum;
		block.parse(bitstream, this);
	}

	public boolean getPatternCode(int i)
	{
		return macroblock.getPatternCode(i);
	}

	public boolean getMacroblockIntra()
	{
		return macroblock.getMacroblockIntra();
	}

	public int getBlockNumber()
	{
		return blockNum;
	}

	public int getPictureCodingType()
	{
		return picture.getPictureCodingType();
	}

	public int getForwardFCode()
	{
		return picture.getForwardFCode();
	}

	public int getBackwardFCode()
	{
		return picture.getBackwardFCode();
	}

	public void setSequenceHeader(SequenceHeader sh)
	{
		this.sh = sh;
	}

	public SequenceHeader getSequenceHeader()
	{
		return sh;
	}
}
