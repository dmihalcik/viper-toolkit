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
 *   Corresponds to system_clock_reference in ISO/IEC 11172-1.<br>
 *   <br>
 *   "The system_clock_reference (SCR) is a 33-bit number coded in three
 *    separate fields.  It indicates the intended time of arrival of the
 *    last byte of the system_clock_reference field at the input of the
 *    system target decoder.  The value of the SCR is measured in the number
 *    of periods of a 90kHz system clock with a tolerance specified in 2.4.2.
 *    Using the notation of 2.4.2, the value encoded in
 *    the system_clock_reference is:<br>
 *   <br>
 *        SCR(i) = NINT (system_clock_frequency * (tm(i))) % 2^33<br>
 *   <br>
 *        for i such that M(i) is the last byte of the coded
 *        system_clock_reference field."<br>
 *   <br>
 *    - ISO/IEC 11172-1 Section 2.4.4.2
 */
public class SystemClockReference extends BaseTimeStamp
{
}
