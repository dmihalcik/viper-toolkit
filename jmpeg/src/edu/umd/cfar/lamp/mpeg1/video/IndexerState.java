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

class IndexerState extends ParserState
{
	public void indexSequenceHeader(Bitstream bitstream, VideoIndex videoIndex) throws IOException, MpegException
	{
		sh = new SequenceHeader();
		sh.parse(bitstream);
		videoIndex.addSequenceHeader(sh);
	}

	public void indexGroupOfPictures(Bitstream bitstream, VideoIndex videoIndex) throws IOException, MpegException
	{
		GroupOfPictures.index(bitstream, this, videoIndex);
	}

	public void indexPicture(Bitstream bitstream, VideoIndex videoIndex) throws IOException
	{
		picture.index(bitstream, videoIndex);
	}

	public void indexPicture(Bitstream bitstream, GroupOfPicturesIndex gopIndex) throws IOException
	{
		picture.index(bitstream, gopIndex);
	}
}
