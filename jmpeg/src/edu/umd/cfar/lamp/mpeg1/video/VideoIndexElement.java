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

/** Represents one Group of Pictures within a <code>VideoIndex</code>. */
public class VideoIndexElement implements Comparable
{
	private long startPosition;
	private int  startPicture;
	private int  numPictures;
	private int  sequenceHeader;

	private VideoIndex           videoIndex;
	private GroupOfPicturesIndex gopIndex    = null;


	public VideoIndexElement(long startPosition, int startPicture, int numPictures, int sequenceHeader, VideoIndex videoIndex) throws IOException, MpegException
	{
		this.startPosition  = startPosition;
		this.startPicture   = startPicture;
		this.numPictures    = numPictures;
		this.sequenceHeader = sequenceHeader;
		this.videoIndex     = videoIndex;
	}

	public void writeIndex(DataOutput out) throws IOException
	{
		out.writeLong(startPosition);
		out.writeInt(numPictures);
		out.writeInt(sequenceHeader);
	}
	
	public int compareTo(Object o)
	{
		VideoIndexElement other = (VideoIndexElement)o;

		if (getStartPosition() < other.getStartPosition())
			return -1;

		if (getStartPosition() > other.getStartPosition())
			return 1;

		return 0;
	}
	
	public boolean containsPicture(int picture)
	{
		return (findPicture(picture) == 0);
	}
	
	public int findPicture(int picture)
	{
		if (picture < getStartPicture())
			return -1;

		if (picture > getLastPicture())
			return 1;
	
		return 0;
	}

	public String toString()
	{
		return "(Position: " + startPosition + ", First Picture: " + startPicture + ", Number of Pictures: " + numPictures + ")";
	}
	
	public long getStartPosition()
	{
		return startPosition;
	}

	public int getStartPicture()
	{
		return startPicture;
	}

	public int getNumPictures()
	{
		return numPictures;
	}

	public int getLastPicture()
	{
		return startPicture + numPictures - 1;
	}

	public int getSequenceHeader()
	{
		return sequenceHeader;
	}

	public boolean gopIndexed()
	{
		return (gopIndex != null);
	}

	public void indexGop() throws IOException, MpegException
	{
		if (!gopIndexed())
		{
			gopIndex = new GroupOfPicturesIndex();
			VideoSource videoSource = videoIndex.getVideoSource().copySource();
			videoSource.seek(startPosition);
			GroupOfPictures.index(new Bitstream(videoSource), videoIndex.getIndexerState(), gopIndex);
			videoSource.close();
		}
	}

	/**
	 * @param n  the frame
	 * @return the byte position (in the video stream) of the 
	 * (zero-based) <code>n</code>th picture (frame). 
	 * @throws IOException
	 * @throws MpegException
	 */
	public long getPositionOfPicture(int n) throws IOException, MpegException
	{
		if (!containsPicture(n))
			throw new FrameNotFoundException("Frame " + n + " not in " + this);

		indexGop();
		
		return gopIndex.getPositionOfPicture(n - getStartPicture()) + getStartPosition();
	}

	public int getLastIOrPPicture(int n) throws IOException, MpegException
	{
		int comparison = findPicture(n);
		if (comparison < 0)
			throw new FrameNotFoundException("Searching the wrong VideoIndexElement.");
		indexGop();
		if (comparison > 0)
			return gopIndex.getLastIOrPPicture() + getStartPicture();
		else
			return gopIndex.getLastIOrPPicture(n - getStartPicture()) + getStartPicture();
	}

	public byte getPictureCodingTypeOfPicture(int n) throws IOException, MpegException
	{
		if (!containsPicture(n))
			throw new FrameNotFoundException();

		indexGop();

		return gopIndex.getTypeOfPicture(n - getStartPicture());
	}
}
