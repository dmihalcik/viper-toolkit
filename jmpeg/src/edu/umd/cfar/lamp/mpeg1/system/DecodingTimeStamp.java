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
 *   Corresponds to decoding_time_stamp in ISO/IEC 11172-1.<br>
 *   <br>
 *   "The decoding_time_stamp (DTS) is a 33-bit number coded in three
 *    separate fields.  It indicates the intended time of decoding in the
 *    system target decoder of the first access unit that commences in the
 *    packet.  The value of DTS is measured in the number of periods of a
 *    90kHz system clock with a tolerance specified in 2.4.2.  Using the
 *    notation of 2.4.2 the value encoded in the decoding_time_stamp is:<br>
 *   <br>
 *        DTS = NINT (system_clock_frequency * (td_n(j))) % 2^33<br>
 *    where<br>
 *        td_n(j) is the decoding time of access unit A_n(j).<br>
 *   <br>
 *        A_n(j) is the first access unit that commences in the packet data.
 *        An access unit commences in the packet if the first byte of a video
 *        picture start code or the first byte of the synchronization word of
 *        an audio frame (see ISO/IEC 11172-2 and ISO/IEC 11172-3) is present
 *        in the packet data."<br>
 *   <br>
 *    - ISO/IEC 11172-1 Section 2.4.4.3
 */
public class DecodingTimeStamp extends BaseTimeStamp
{
}
