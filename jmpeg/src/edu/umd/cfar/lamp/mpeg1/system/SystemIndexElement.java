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

import java.io.*;

public class SystemIndexElement
{
	private long systemStreamDataStartPosition;
	private long elementaryStreamDataStartPosition;
	private int packetDataLength;


	public SystemIndexElement(long systemStreamDataStartPosition, long elementaryStreamDataStartPosition, int packetDataLength)
	{
		this.systemStreamDataStartPosition     = systemStreamDataStartPosition;
		this.elementaryStreamDataStartPosition = elementaryStreamDataStartPosition;
		this.packetDataLength                  = packetDataLength;
	}
	
	public void writeIndex(DataOutput out) throws IOException
	{
		out.writeLong(systemStreamDataStartPosition);
		out.writeInt(packetDataLength);
	}

	/** 
	 * Determines whether or not the <code>SystemIndexElement</code> 
	 * contains the given byte (<code>bytePosition</code>) of the 
	 * <i>elementary</i> stream of this packet. 
	 * @param bytePosition the byte position
	 * @return
	 */
	public boolean containsByte(long bytePosition)
	{
		return (bytePosition >= elementaryStreamDataStartPosition) && (bytePosition < (elementaryStreamDataStartPosition + packetDataLength));
	}

	/**
	 * Checks to see that the position is contained within.
	 * @param bytePosition byte offset to look for
	 * @return -1 if <code>bytePosition</code>  < the position of this packet in the elementary stream,<br>
	 *  0 if <code>bytePosition</code> == the position of this packet in the elementary stream,<br>
	 *  1 if <code>bytePosition</code>  > the position of this packet in the elementary stream.
	 */
	public int findByte(long bytePosition)
	{
		if (containsByte(bytePosition))
			return 0;

		if (bytePosition < elementaryStreamDataStartPosition)
			return -1;

		return 1;
	}

	public String toString()
	{
		return "(System: " + systemStreamDataStartPosition + ", Elementary: " + elementaryStreamDataStartPosition + ", Length: " + packetDataLength + ")";
	}
	
	public long getSystemStreamDataStartPosition()
	{
		return systemStreamDataStartPosition;
	}
		
	public long getElementaryStreamDataStartPosition()
	{
		return elementaryStreamDataStartPosition;
	}

	public int getPacketDataLength()
	{
		return packetDataLength;
	}
}
