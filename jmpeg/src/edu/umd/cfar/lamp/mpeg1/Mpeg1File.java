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
import java.util.*;

import edu.umd.cfar.lamp.mpeg1.system.*;
import edu.umd.cfar.lamp.mpeg1.video.*;

public class Mpeg1File 
{
	public final static byte  SYSTEM = 1;
	public final static byte  VIDEO  = 2;
	public final static byte  AUDIO  = 3;
	
	private byte              streamType;

	private File              file         = null;
	private Mpeg1SystemStream systemStream = null;
	

	public Mpeg1File(File file) throws IOException, UnsupportedStreamTypeException
	{
		this.file = file;
		
		streamType = getStreamType(file);

		if (isSystemFile())
			systemStream = new Mpeg1SystemStream(file);

		if (!isSystemOrVideoFile())
			throw new UnsupportedStreamTypeException("Only ISO/IEC 11172-1 (MPEG-1 System) and ISO/IEC 11172-2 (MPEG-1 Video) streams are supported.");
	}

	public File getFile()
	{
		return file;
	}
	
	public void readSystemIndex(File file) throws IOException, MpegException
	{
		if (isSystemFile())
			systemStream.readIndex(file);
	}

	public void readSystemIndex(InputStream in) throws IOException, MpegException
	{
		if (isSystemFile())
			systemStream.readIndex(in);
	}

	public void writeSystemIndex(File file) throws IOException, MpegException
	{
		if (isSystemFile())
			systemStream.writeIndex(file);
	}

	public void writeSystemIndex(OutputStream out) throws IOException, MpegException
	{
		if (isSystemFile())
			systemStream.writeIndex(out);
	}

	public void writeSystemIndex(Component parentComponent, Object message, File file) throws IOException, MpegException
	{
		if (isSystemFile())
			systemStream.writeIndex(parentComponent, message, file);
	}

	public void writeSystemIndex(Component parentComponent, Object message, OutputStream out) throws IOException, MpegException
	{
		if (isSystemFile())
			systemStream.writeIndex(parentComponent, message, out);
	}
	
	public static boolean isSystemFile(File file) throws IOException
	{
		return getStreamType(file) == SYSTEM;
	}

	public static boolean isVideoFile(File file) throws IOException
	{
		return getStreamType(file) == VIDEO;
	}

	public static boolean isSystemOrVideoFile(File file) throws IOException
	{
		byte type = getStreamType(file);
		return (type == SYSTEM) || (type == VIDEO);
	}

	public static byte getStreamType(File file) throws IOException
	{
		RandomAccessFile rafile = new RandomAccessFile(file, "r");
		byte result = 0;
		if (rafile.readInt() == SystemStartCodes.PACK_START_CODE)
			result = SYSTEM;
		else
		{
			for (long i = 0; i < rafile.length(); i++)
			{
				rafile.seek(i);
				int val = rafile.readInt();
				if (val == VideoStartCodes.SEQUENCE_HEADER_CODE)
				{
					result = VIDEO;
					break;
				}
				if (!((val == 0x00000000) || (val == 0x00000001)))
				{
					break;
				}
			}
		}
		rafile.close();
		return result;
	}

	public boolean isSystemFile()
	{
		return streamType == SYSTEM;
	}

	public boolean isVideoFile()
	{
		return streamType == VIDEO;
	}

	public boolean isSystemOrVideoFile()
	{
		return isSystemFile() || isVideoFile();
	}

	public boolean isAudioFile()
	{
		return streamType == AUDIO;
	}

	public Vector getStreamList() throws IOException, MpegException
	{
		if (isSystemFile())
		{
			return systemStream.getStreamList();
		}
		else
		{
			Vector result = new Vector(1);
			result.add(new Integer(0));
			return result;
		}
	}

	public Vector getVideoStreamList() throws IOException, MpegException
	{
		if (isSystemFile())
		{
			return systemStream.getVideoStreamList();
		}
		else
		{
			Vector result = new Vector(1);
			result.add(new Integer(0));
			return result;
		}
	}

	public Mpeg1VideoStream getVideoStream(int stream_id) throws IOException, MpegException
	{
		if (isSystemFile())
		{
			if (StreamIDs.isVideoStream(stream_id))
			{
				return new Mpeg1VideoStream(new VideoSource(new Mpeg1SystemStream(file, systemStream.getSystemIndex()), stream_id));
			}
			else
			{
				throw new StreamNotFoundException("Stream ID " + Integer.toHexString(stream_id).toUpperCase() + " is not a valid video stream ID.");
			}
		}
		else if (isVideoFile())
		{
			return new Mpeg1VideoStream(new VideoSource(file));
		}
		else
		{
			return null;
		}
	}

	public Mpeg1VideoStream getVideoStream() throws MpegException, IOException
	{
		if (isVideoFile())
		{
			return new Mpeg1VideoStream(new VideoSource(file));
		}
		else
		{
			return new Mpeg1VideoStream(
				new VideoSource( systemStream.copyStream(),
				((Integer)getVideoStreamList().get(0)).intValue() )
				);
		}
	}
}
