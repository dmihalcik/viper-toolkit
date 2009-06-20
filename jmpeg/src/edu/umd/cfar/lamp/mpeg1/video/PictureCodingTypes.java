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

public abstract class PictureCodingTypes
{
	public static final int TYPE_I = 1;
	public static final int TYPE_P = 2;
	public static final int TYPE_B = 3;
	public static final int TYPE_D = 4;

	public static final boolean isValidPictureCodingType(int picture_coding_type)
	{
		switch (picture_coding_type)
		{
			case TYPE_I: 
			case TYPE_P: 
			case TYPE_B: 
			case TYPE_D: return true;
			default:     return false;
		}
	}

	public static final char getChar(int picture_coding_type)
	{
		switch (picture_coding_type)
		{
			case TYPE_I: return 'I';
			case TYPE_P: return 'P';
			case TYPE_B: return 'B';
			case TYPE_D: return 'D';
			default: return '\0';
		}
	}
}
