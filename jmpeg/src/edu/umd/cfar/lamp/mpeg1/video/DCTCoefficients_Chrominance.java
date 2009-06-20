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

import java.io.*;

import edu.columbia.ee.flavor.*;
import edu.umd.cfar.lamp.mpeg1.*;

class DCTCoefficients_Chrominance implements Parsable
{
	private int value = 0;


	public int getValue()
	{
		return value;
	}
	
	public void parse(Bitstream bitstream) throws IOException
	{
        switch (bitstream.nextbits(2))
		{
        case 0:
            bitstream.skipbits(2);
            value = 0;
            break;
        case 1:
            bitstream.skipbits(2);
            value = 1;
            break;
        case 2:
            bitstream.skipbits(2);
            value = 2;
            break;
        default:
            switch (bitstream.nextbits(3))
			{
            case 6:
                bitstream.skipbits(3);
                value = 3;
                break;
            default:
                switch (bitstream.nextbits(4))
				{
                case 14:
                    bitstream.skipbits(4);
                    value = 4;
                    break;
                default:
                    switch (bitstream.nextbits(5))
					{
                    case 30:
                        bitstream.skipbits(5);
                        value = 5;
                        break;
                    default:
                        switch (bitstream.nextbits(6))
						{
                        case 62:
                            bitstream.skipbits(6);
                            value = 6;
                            break;
                        default:
                            switch (bitstream.nextbits(7))
							{
                            case 126:
                                bitstream.skipbits(7);
                                value = 7;
                                break;
                            default:
                                switch (bitstream.nextbits(8))
								{
                                case 254:
                                    bitstream.skipbits(8);
                                    value = 8;
                                    break;
                                default:
                                    throw new ParsingException("VLC decode for DCTCoefficients_Chrominance failed.");
                                }
                            }
                        }
                    }
                }
            }
        }
	}
}
