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

package edu.umd.cfar.lamp.mpeg1;

import java.awt.*;
import java.io.*;

import edu.umd.cfar.lamp.mpeg1.video.*;

public class Mpeg1VideoStream
{
	private VideoDecoder videoDecoder = null;
	
	public Mpeg1VideoStream(VideoSource videoSource) throws IOException, MpegException
	{
		videoDecoder = new VideoDecoder(videoSource);
	}

	public int getStreamID()
	{
		return videoDecoder.getStreamID();
	}

	public void writeIndex(File file) throws IOException, MpegException
	{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		writeIndex(bos);
		bos.close();
	}

	public void readIndex(File file) throws IOException, MpegException
	{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		readIndex(bis);
		bis.close();
	}

	public void writeIndex(OutputStream out) throws IOException, MpegException
	{
		index();
		videoDecoder.writeIndex(new DataOutputStream(out));
	}

	public void readIndex(InputStream in) throws IOException, MpegException
	{
		videoDecoder.readIndex(new DataInputStream(in));
	}

	public void index() throws IOException, MpegException
	{
		videoDecoder.index();
	}

	public void seek(int frame) throws IOException, MpegException
	{
		videoDecoder.seek(frame);
	}

	public int getFrameWidth() throws IOException, MpegException
	{
		return videoDecoder.getFrameWidth();
	}

	public int getFrameHeight() throws IOException, MpegException
	{
		return videoDecoder.getFrameHeight();
	}

	public int getBitRate() throws IOException, MpegException
	{
		return videoDecoder.getBitRate();
	}

	public boolean isVariableBitRate() throws IOException, MpegException
	{
		return getBitRate() == SequenceHeader.VARIABLE_BITRATE;
	}

	public PelAspectRatio getPixelAspectRatio() throws IOException, MpegException
	{
		return videoDecoder.getPixelAspectRatio();
	}

	public float getFrameRate() throws IOException, MpegException
	{
		return videoDecoder.getFrameRate();
	}

	public int getNumFrames() throws IOException, MpegException
	{
		return videoDecoder.getNumFrames();
	}
	
	// Returns the byte position of the current frame.
	public long getPosition() throws IOException, MpegException
	{
		return videoDecoder.getPosition();
	}

	// Returns the picture_coding_type of the current frame.
	public int getPictureCodingType() throws IOException, MpegException
	{
		return videoDecoder.getPictureCodingType();
	}
	
	public Image getImage() throws IOException, MpegException
	{
		return videoDecoder.getImage();
	}
	/**
	 * Gets the video decoder for this stream.
	 * @return Returns the videoDecoder.
	 */
	public VideoDecoder getVideoDecoder() {
		return videoDecoder;
	}
}
