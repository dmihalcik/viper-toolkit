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

public abstract class VideoStartCodes
{
	/** 32 bits. */
	public static final int SEQUENCE_ERROR_CODE        = 0x000001B4;
	
	/** Terminates a video sequence.  32 bits. */
	public static final int SEQUENCE_END_CODE          = 0x000001B7;

	/** Begins a sequence header.  32 bits. */
	public static final int SEQUENCE_HEADER_CODE       = 0x000001B3;

	/** Begins extension data.  32 bits. */
	public static final int EXTENSION_START_CODE       = 0x000001B5;

	/** Begins user data.  32 bits. */
	public static final int USER_DATA_START_CODE       = 0x000001B2;

	/** Begins a Group Of Pictures.  32 bits. */
	public static final int GROUP_START_CODE           = 0x000001B8;

	/** Begins a Picture.  32 bits. */
	public static final int PICTURE_START_CODE         = 0x00000100;

	/** Prefix to a Slice start code.  24 bits. */
	public static final int SLICE_START_CODE_BEGIN     = 0x000001;

	/** Lowest Slice start code.  32 bits. */
	public static final int MIN_SLICE_START_CODE       = 0x00000101;

	/** Highest Slice start code.  32 bits. */
	public static final int MAX_SLICE_START_CODE       = 0x000001AF;


	/** 
	 * Determines if the given <code>code</code> is a Slice start code. 
	 * @param code the code to check
	 * @return <code>true</code> if the code is a slice start code
	 */
	public static final boolean isSliceStartCode(int code)
	{
		return ((code >= MIN_SLICE_START_CODE) && (code <= MAX_SLICE_START_CODE));
	}
}