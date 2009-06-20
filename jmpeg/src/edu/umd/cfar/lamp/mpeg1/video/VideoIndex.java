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

/** Indexes the Groups of Pictures within a Video Sequence. */
public class VideoIndex
{
	public static int  MAGIC_NUMBER = 0x11172201; // for ISO/IEC 11172-2 version 1
	
	private Vector         index               = null;
	private Vector         sequenceHeaderIndex = new Vector();
	private SequenceHeader firstSequenceHeader = null;
	private VideoDecoder   videoDecoder        = null;
	private IndexerState   indexerState        = new IndexerState();


	public VideoIndex(VideoDecoder videoDecoder)
	{
		this.videoDecoder = videoDecoder;
	}

	public int getLastSequenceHeader()
	{
		return sequenceHeaderIndex.size()-1;
	}

	public void writeIndex(DataOutput out) throws IOException, MpegException
	{
		index();

		out.writeInt(MAGIC_NUMBER);
		
		int numSequenceHeaders = sequenceHeaderIndex.size();
		out.writeInt(numSequenceHeaders);
		for (int i = 0; i < numSequenceHeaders; i++)
		{
			((SequenceHeader)sequenceHeaderIndex.get(i)).writeIndex(out);
		}
		int numGOPs = index.size();
		out.writeInt(numGOPs);
		for (int i = 0; i < numGOPs; i++)
		{
			((VideoIndexElement)index.get(i)).writeIndex(out);
		}
	}

	public void readIndex(DataInput in) throws IOException, MpegException
	{
		int  magicNumber = in.readInt();
		byte version     = (byte)(magicNumber & 0xFF); // version is last byte of magic number

		if ((magicNumber >>> 8) != (MAGIC_NUMBER >>> 8)) // compare first 3 bytes of magic number
			throw new IndexException("Invalid magic number: " + Integer.toHexString(magicNumber));

		switch (version)
		{
			case 0x01:
				index = new Vector();
				int numSequenceHeaders = in.readInt();
				for (int i = 0; i < numSequenceHeaders; i++)
				{
					SequenceHeader sh = new SequenceHeader();
					sh.readIndex(in, version);
					addSequenceHeader(sh);
				}
				int numGOPs = in.readInt();
				for (int i = 0; i < numGOPs; i++)
				{
					long startPosition  = in.readLong();
					int  numPictures    = in.readInt();
					int  sequenceHeader = in.readInt();
					addGroupOfPictures(startPosition, numPictures, sequenceHeader);
				}
				break;
			default:
				throw new UnsupportedIndexVersionException("version " + Integer.toHexString(version));
		}
	}
	
	public void complete()
	{
		Collections.sort(index);
	}

	public VideoDecoder getVideoDecoder()
	{
		return videoDecoder;
	}

	public VideoSource getVideoSource()
	{
		return videoDecoder.getVideoSource();
	}
	
	public IndexerState getIndexerState()
	{
		return indexerState;
	}

	public int getMbWidth(int frame) throws IOException, MpegException
	{
		return getSequenceHeader(frame).getMbWidth();
	}

	public int[][] getIntraQuantizerMatrix(int frame) throws IOException, MpegException
	{
		return getSequenceHeader(frame).getIntraQuantizerMatrix();
	}

	public int[][] getNonIntraQuantizerMatrix(int frame) throws IOException, MpegException
	{
		return getSequenceHeader(frame).getNonIntraQuantizerMatrix();
	}

	public void index() throws IOException, MpegException
	{
		if (!indexed())
		{
			index = new Vector();
			VideoSequence.index(new Bitstream(getVideoSource().copySource()), indexerState, this);
		}
	}

	public boolean indexed()
	{
		return (index != null);
	}
	
	public void getFirstSequenceHeader() throws IOException, MpegException
	{
		if (firstSequenceHeader == null)
		{
			VideoSource videoSource = getVideoSource().copySource();
			firstSequenceHeader = VideoSequence.getFirstSequenceHeader(new Bitstream(videoSource));
			videoSource.close();
		}
	}

	public SequenceHeader getSequenceHeader(int frame) throws IOException, MpegException
	{
		index();
		int gop = getGroupOfPicturesNumberForFrame(frame);
		VideoIndexElement vie = (VideoIndexElement)index.get(gop);
		return (SequenceHeader)sequenceHeaderIndex.get(vie.getSequenceHeader());
	}

	public void addSequenceHeader(SequenceHeader sequenceHeader) throws IOException, MpegException
	{
		sequenceHeaderIndex.add(sequenceHeader);
	}
	
	public void addGroupOfPictures(long startPosition, int numPictures, int sequenceHeader) throws IOException, MpegException
	{
		int  startPicture;
		try
		{
			VideoIndexElement lastElement = (VideoIndexElement)index.lastElement();
			startPicture  = lastElement.getStartPicture() + lastElement.getNumPictures();
		}
		catch (NoSuchElementException nsee)
		{
			startPicture  = 0;
		}
		index.add(new VideoIndexElement(startPosition, startPicture, numPictures, sequenceHeader, this));
	}

	public int getFrameWidth() throws IOException, MpegException
	{
		int currentFrame = videoDecoder.getCurrentFrame();
		if (!indexed() || (currentFrame < 0))
		{
			getFirstSequenceHeader();
			return firstSequenceHeader.getFrameWidth();
		}
		else
			return getSequenceHeader(currentFrame).getFrameWidth();
	}

	public int getFrameHeight() throws IOException, MpegException
	{
		int currentFrame = videoDecoder.getCurrentFrame();
		if (!indexed() || (currentFrame < 0))
		{
			getFirstSequenceHeader();
			return firstSequenceHeader.getFrameHeight();
		}
		else
			return getSequenceHeader(currentFrame).getFrameHeight();
	}

	public int getBitRate() throws IOException, MpegException
	{
		int currentFrame = videoDecoder.getCurrentFrame();
		if (!indexed() || (currentFrame < 0))
		{
			getFirstSequenceHeader();
			return firstSequenceHeader.getBitRate();
		}
		else
			return getSequenceHeader(currentFrame).getBitRate();
	}

	public PelAspectRatio getPixelAspectRatio() throws IOException, MpegException
	{
		int currentFrame = videoDecoder.getCurrentFrame();
		if (!indexed() || (currentFrame < 0))
		{
			getFirstSequenceHeader();
			return firstSequenceHeader.getPixelAspectRatio();
		}
		else
			return getSequenceHeader(currentFrame).getPixelAspectRatio();
	}

	public float getFrameRate() throws IOException, MpegException
	{
		int currentFrame = videoDecoder.getCurrentFrame();
		if (!indexed() || (currentFrame < 0))
		{
			getFirstSequenceHeader();
			return firstSequenceHeader.getFrameRate();
		}
		else
			return getSequenceHeader(currentFrame).getFrameRate();
	}

	/**
	 * Gets the group of pictures for the given frame
	 * @param frame the frame
	 * @return 0-based number of the Group Of Pictures containing 
	 * the given (0-based) frame. 
	 * @throws FrameNotFoundException
	 */
	public int getGroupOfPicturesNumberForFrame(int frame) throws FrameNotFoundException
	{
		// binary search
		int low  = 0;
		int high = index.size() - 1;
		while (low <= high)
		{
			int mid = (low + high) / 2;
			int c = ((VideoIndexElement)index.elementAt(mid)).findPicture(frame);
			if (c < 0)
				high = mid - 1;
			else if (c > 0)
				low = mid + 1;
			else
				return mid;
		}
		throw new FrameNotFoundException("frame " + frame);
	}

	public long getStartPosition(int groupOfPicturesNumber)
	{
		return ((VideoIndexElement)index.elementAt(groupOfPicturesNumber)).getStartPosition();
	}

	public int getStartFrame(int groupOfPicturesNumber)
	{
		return ((VideoIndexElement)index.elementAt(groupOfPicturesNumber)).getStartPicture();
	}
	
	public int getNumFrames()
	{
		if (index == null)
			return 0;
		
		if (index.size() == 0)
			return 0;

		VideoIndexElement lastElement = (VideoIndexElement)index.lastElement();
		return lastElement.getLastPicture() + 1; // 0-based, so have to add 1
	}

	public long getPositionOfFrame(int n) throws IOException, MpegException
	{
		int gopNumber = getGroupOfPicturesNumberForFrame(n);
		VideoIndexElement vie = (VideoIndexElement)index.elementAt(gopNumber);
		return vie.getPositionOfPicture(n);
	}

	public int getLastIOrPFrame(int n) throws IOException, MpegException
	{
		int gopNumber = getGroupOfPicturesNumberForFrame(n);
		VideoIndexElement vie = (VideoIndexElement)index.elementAt(gopNumber);
	
		try
		{
			return vie.getLastIOrPPicture(n);
		}
		catch (FrameNotFoundException fnfe)
		{
			if (gopNumber == 0)
				throw new FrameNotFoundException();

			vie = (VideoIndexElement)index.elementAt(gopNumber-1);
			return vie.getLastIOrPPicture(n);
		}
	}

	public byte getPictureCodingTypeOfFrame(int n) throws IOException, MpegException
	{
		int gopNumber = getGroupOfPicturesNumberForFrame(n);
		VideoIndexElement vie = (VideoIndexElement)index.elementAt(gopNumber);
		return vie.getPictureCodingTypeOfPicture(n);
	}

	public String toString()
	{
		String result = new String();
		Iterator iter = index.iterator();
		while (iter.hasNext())
		{
			result += iter.next().toString() + "\n";
		}
		return result;
	}
}
