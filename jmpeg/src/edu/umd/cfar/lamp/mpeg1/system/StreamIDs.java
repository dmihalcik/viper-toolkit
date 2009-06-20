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

package edu.umd.cfar.lamp.mpeg1.system;

/**
 *  Based on "Table 1 -- stream_id table" in ISO/IEC 11172-1 Section 2.4.4.2 (p. 24)
 */
public abstract class StreamIDs 
{
	public static final int ALL_AUDIO_STREAMS        = 0xB8;
	public static final int ALL_VIDEO_STREAMS        = 0xB9;

	public static final int RESERVED_STREAM          = 0xBC;
	public static final int PRIVATE_STREAM_1         = 0xBD;
	public static final int PADDING_STREAM           = 0xBE;
	public static final int PRIVATE_STREAM_2         = 0xBF;

	public static final int MIN_AUDIO_STREAM         = 0xC0;
	public static final int MAX_AUDIO_STREAM         = 0xDF;

	public static final int MIN_VIDEO_STREAM         = 0xE0;
	public static final int MAX_VIDEO_STREAM         = 0xEF;

	public static final int MIN_RESERVED_DATA_STREAM = 0xF0;
	public static final int MAX_RESERVED_DATA_STREAM = 0xFF;


	public static final String getStreamName(int stream_id)
	{
		if (isAudioStream(stream_id))
		{
			return "Audio Stream " + (stream_id - MIN_AUDIO_STREAM);
		}
		else if (isVideoStream(stream_id))
		{
			return "Video Stream " + (stream_id - MIN_VIDEO_STREAM);
		}
		else if (isReservedDataStream(stream_id))
		{
			return "Reserved Data Stream " + (stream_id - MIN_RESERVED_DATA_STREAM);
		}

		switch (stream_id)
		{
			case RESERVED_STREAM:   return "Reserved Stream";
			case PRIVATE_STREAM_1:  return "Private Stream 1";
			case PADDING_STREAM:    return "Padding Stream";
			case PRIVATE_STREAM_2:  return "Private Stream 2";
			case ALL_AUDIO_STREAMS: return "All audio streams";
			case ALL_VIDEO_STREAMS: return "All video streams";
		}

		return "Invalid Stream ID: " + Integer.toHexString(stream_id).toUpperCase();
	}
	
	public static final boolean isAudioStream(int stream_id)
	{
		return ((stream_id >= MIN_AUDIO_STREAM) && (stream_id <= MAX_AUDIO_STREAM));
	}

	public static final boolean isVideoStream(int stream_id)
	{
		return ((stream_id >= MIN_VIDEO_STREAM) && (stream_id <= MAX_VIDEO_STREAM));
	}

	public static final boolean isReservedDataStream(int stream_id)
	{
		return ((stream_id >= MIN_RESERVED_DATA_STREAM) && (stream_id <= MAX_RESERVED_DATA_STREAM));
	}
}
