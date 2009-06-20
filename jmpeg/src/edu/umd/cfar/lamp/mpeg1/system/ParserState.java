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

import edu.columbia.ee.flavor.*;

public class ParserState 
{
	protected SystemHeader systemHeader = new SystemHeader();
	protected Pack         pack         = new Pack();
	protected Packet       packet       = new Packet();

	public void parseSystemHeader(Bitstream bitstream) throws IOException
	{
		systemHeader.parse(bitstream);
	}
	
	public void parsePack(Bitstream bitstream) throws IOException
	{
		pack.parse(bitstream, this);
	}

	public void parsePacket(Bitstream bitstream) throws IOException
	{
		packet.parse(bitstream, this);
	}
}
