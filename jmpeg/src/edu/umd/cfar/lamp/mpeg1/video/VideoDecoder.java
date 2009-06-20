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

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import edu.umd.cfar.lamp.mpeg1.*;

public class VideoDecoder
{
	private DecoderState decoderState = null;
	private VideoIndex   videoIndex   = new VideoIndex(this);
	private VideoSource  videoSource  = null;

	private int          currentFrame = -1;


	public VideoDecoder(VideoSource videoSource) throws IOException, MpegException
	{
		this.videoSource = videoSource.copySource();
	}

	public int getStreamID()
	{
		return videoSource.getStreamID();
	}
	
	public void writeIndex(DataOutput out) throws IOException, MpegException
	{
		index();
		videoIndex.writeIndex(out);
	}

	public void readIndex(DataInput in) throws IOException, MpegException
	{
		videoIndex.readIndex(in);
	}

	public VideoSource getVideoSource()
	{
		return videoSource;
	}
	
	private void initDecoderState() throws IOException, MpegException
	{
		if (decoderState == null)
		{
			getFirstSequenceHeader();
			decoderState = new DecoderState(this);
		}
	}
	
	public SequenceHeader getSequenceHeader(int frame) throws IOException, MpegException
	{
		index();
		return videoIndex.getSequenceHeader(frame);
	}

	private void getFirstSequenceHeader() throws IOException, MpegException
	{
		videoIndex.getFirstSequenceHeader();
	}

	public int getFrameWidth() throws IOException, MpegException
	{
		getFirstSequenceHeader();
		return videoIndex.getFrameWidth();
	}

	public int getFrameHeight() throws IOException, MpegException
	{
		getFirstSequenceHeader();
		return videoIndex.getFrameHeight();
	}

	public int getBitRate() throws IOException, MpegException
	{
		getFirstSequenceHeader();
		return videoIndex.getBitRate();
	}

	public PelAspectRatio getPixelAspectRatio() throws IOException, MpegException
	{
		getFirstSequenceHeader();
		return videoIndex.getPixelAspectRatio();
	}

	public float getFrameRate() throws IOException, MpegException
	{
		getFirstSequenceHeader();
		return videoIndex.getFrameRate();
	}

	public int getNumFrames() throws IOException, MpegException
	{
		index();
		return videoIndex.getNumFrames();
	}

	public int getCurrentFrame()
	{
		return currentFrame;
	}
	
	public void seek(int frame) throws IOException, MpegException
	{
		if (frame < 0)
			throw new FrameNotFoundException();

		index();
		initDecoderState();

		if (frame != getCurrentFrame())
		{
			decoderState.seek(frame);
			currentFrame = frame;
		}
	}

	public long getPosition() throws IOException, MpegException
	{
		return getPosition(getCurrentFrame());
	}

	public long getPosition(int frame) throws IOException, MpegException
	{
		index();
		return videoIndex.getPositionOfFrame(frame);
	}

	public int getPictureCodingType() throws IOException, MpegException
	{
		return getPictureCodingType(getCurrentFrame());
	}

	public int getPictureCodingType(int frame) throws IOException, MpegException
	{
		index();
		return videoIndex.getPictureCodingTypeOfFrame(frame);
	}

	public int getLastIOrPFrame() throws IOException, MpegException
	{
		return getLastIOrPFrame(getCurrentFrame());
	}
	
	public int getLastIOrPFrame(int frame) throws IOException, MpegException
	{
		index();
		return videoIndex.getLastIOrPFrame(frame);
	}

	public void index() throws IOException, MpegException
	{
		videoIndex.index();
	}

	public Image getImage() throws IOException, MpegException
	{
		int pixelData[] = convertYCbCrToRGB(decoderState.getCurrentYCbCr());
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(getFrameWidth(),getFrameHeight(),pixelData,0,getFrameWidth()));
	}

	private int[] convertYCbCrToRGB(int pixelData[])
	{
		int result[] = new int[pixelData.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = convertYCbCrToRGB(pixelData[i]);
		}
		return result;
	}

	/**
	 * Converts YCbCr pixel data into RGB pixel data.
	 * @param packedYCbCrValue YCbCr pixel value.  First byte is discarded, second byte is Y, third byte is Cb, fourth byte is Cr.
	 * @return rgb value
	 */
	private int convertYCbCrToRGB(int packedYCbCrValue)
	{
		int Y  = (packedYCbCrValue & 0x00FF0000) >>> 16;
		int Cb = (packedYCbCrValue & 0x0000FF00) >>>  8;
		int Cr = (packedYCbCrValue & 0x000000FF);
		return convertYCbCrToRGB(Y, Cb, Cr);
	}

	/**
	 * Converts YCbCr pixel data into RGB pixel data.
	 * @param Y  value
	 * @param Cb value
	 * @param Cr value
	 * @return the RGB value
	 */
	private int convertYCbCrToRGB(int Y, int Cb, int Cr)
	{
		float Ycalc  = (1.164f * (Y - 16));
		int   Cbcalc = (Cb - 128);
		int   Crcalc = (Cr - 128);
		
		int red   = (int)(Ycalc + 1.402f   * Crcalc);
		int green = (int)(Ycalc - 0.34414f * Cbcalc - 0.71414f * Crcalc);
		int blue  = (int)(Ycalc + 1.772f   * Cbcalc);
		

		red   = clamp(red);
		green = clamp(green);
		blue  = clamp(blue);
		return packPixel(red, green, blue);
	}

	/** 
	 * Constrains color component values to the 0..255 range. 
	 * @param componentValue the value to clamp
	 * @return the clamped value
	 */
	public static int clamp(int componentValue)
	{
		return (componentValue < 0) ? 0 : (componentValue > 255) ? 255 : componentValue;
	}
	
	/** 
	 * Packs three color components into one integer.  Color 
	 * components should be <code>clamp()</code>ed. 
	 * @param component1 the first color channel value
	 * @param component2 the second color channel value
	 * @param component3 the third color channel value
	 * @return the packed color
	 */
	private int packPixel(int component1, int component2, int component3)
	{
		return (int)(0xFF000000 | (component1 << 16) | (component2 << 8) | (component3));
	}
}
