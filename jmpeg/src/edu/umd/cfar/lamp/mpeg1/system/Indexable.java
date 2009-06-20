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

interface Indexable extends StateParsable
{
	public void index(Bitstream bitstream, IndexerState indexerState, SystemIndex systemIndex) throws IOException;
}
