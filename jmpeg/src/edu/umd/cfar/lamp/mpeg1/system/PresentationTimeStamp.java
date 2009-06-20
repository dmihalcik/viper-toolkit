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
 *   Corresponds to presentation_time_stamp in ISO/IEC 11172-1.<br>
 *   <br>
 *   "The presentation_time_stamp (PTS) is a 33-bit number coded in three
 *    separate fields.  It indicates the intended time of presentation in
 *    the system target decoder of the presentation unit that corresponds
 *    to the first access unit that commences in the packet.  The value
 *    of PTS is measured in the number of periods of a 90kHz system clock
 *    with a tolerance specified in 2.4.2.  Using the notation of 2.4.2
 *    the value encoded in the presentation_time_stamp is:<br>
 *   <br>
 *        PTS = NINT (system_clock_frequency * (tp_n(k))) & 2^33<br>
 *    where<br>
 *        tp_n(k) is the presentation time of presentation unit P_n(k).<br>
 *   <br>
 *        P_n(k) is the presentation unit corresponding to the first access
 *        unit that commences in the packet data.  An access unit commences
 *        in the packet if the first byte of a video picture start code or
 *        the first byte of the synchronization word of an audio frame
 *        (see ISO/IEC 11172-2 and ISO/IEC 11172-3) is present in the packet
 *        data.<br>
 *   <br>
 *    If there is filtering in audio, it is assumed by the system model that
 *    filtering introduces no delay, hence the sample referred to by PTS at
 *    encoding is the same sample referred to PTS at decoding."<br>
 *   <br>
 *    - ISO/IEC 11172-1 Section 2.4.4.3
 */
public class PresentationTimeStamp extends BaseTimeStamp
{
}
