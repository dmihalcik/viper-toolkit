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

public abstract class SystemStartCodes
{
	/** Terminates a System stream.  32 bits.  (ISO/IEC 11172-1 Section 2.4.4.1) */
	public static final int ISO_11172_END_CODE       = 0x000001B9;

	/** Begins a Pack.  32 bits.  (ISO/IEC 11172-1 Section 2.4.4.2) */
	public static final int PACK_START_CODE          = 0x000001BA;

	/** Begins a System Header.  32 bits.  (ISO/IEC 11172-1 Section 2.4.4.2) */
	public static final int SYSTEM_HEADER_START_CODE = 0x000001BB;

	/** Begins a packet_start_code.  24 bits. */
	public static final int PACKET_START_CODE_PREFIX = 0x000001;
}
