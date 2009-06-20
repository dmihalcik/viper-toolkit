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

public class IndexerState extends ParserState 
{
	public void indexPack(Bitstream bitstream, SystemIndex systemIndex) throws IOException
	{
		pack.index(bitstream, this, systemIndex);
	}

	public void indexPacket(Bitstream bitstream, SystemIndex systemIndex) throws IOException
	{
		packet.index(bitstream, this, systemIndex);
	}
}
