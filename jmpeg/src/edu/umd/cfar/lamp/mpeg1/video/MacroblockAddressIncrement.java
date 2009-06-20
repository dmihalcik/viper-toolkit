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

class MacroblockAddressIncrement implements Parsable
{
	private int value = 0;
	

	public void parse(Bitstream bitstream) throws IOException
	{
		switch (bitstream.nextbits(1))
		{
		case 1:
			bitstream.skipbits(1);
			value = 1;
			break;
		default:
			switch (bitstream.nextbits(3))
			{
			case 3:
				bitstream.skipbits(3);
				value = 2;
				break;
			case 2:
				bitstream.skipbits(3);
				value = 3;
				break;
			default:
				switch (bitstream.nextbits(4))
				{
				case 3:
					bitstream.skipbits(4);
					value = 4;
					break;
				case 2:
					bitstream.skipbits(4);
					value = 5;
					break;
				default:
					switch (bitstream.nextbits(5))
					{
					case 3:
						bitstream.skipbits(5);
						value = 6;
						break;
					case 2:
						bitstream.skipbits(5);
						value = 7;
						break;
					default:
						switch (bitstream.nextbits(7))
						{
						case 7:
							bitstream.skipbits(7);
							value = 8;
							break;
						case 6:
							bitstream.skipbits(7);
							value = 9;
							break;
						default:
							switch (bitstream.nextbits(8))
							{
							case 11:
								bitstream.skipbits(8);
								value = 10;
								break;
							case 10:
								bitstream.skipbits(8);
								value = 11;
								break;
							case 9:
								bitstream.skipbits(8);
								value = 12;
								break;
							case 8:
								bitstream.skipbits(8);
								value = 13;
								break;
							case 7:
								bitstream.skipbits(8);
								value = 14;
								break;
							case 6:
								bitstream.skipbits(8);
								value = 15;
								break;
							default:
								switch (bitstream.nextbits(10))
								{
								case 23:
									bitstream.skipbits(10);
									value = 16;
									break;
								case 22:
									bitstream.skipbits(10);
									value = 17;
									break;
								case 21:
									bitstream.skipbits(10);
									value = 18;
									break;
								case 20:
									bitstream.skipbits(10);
									value = 19;
									break;
								case 19:
									bitstream.skipbits(10);
									value = 20;
									break;
								case 18:
									bitstream.skipbits(10);
									value = 21;
									break;
								default:
									switch (bitstream.nextbits(11))
									{
									case 35:
										bitstream.skipbits(11);
										value = 22;
										break;
									case 34:
										bitstream.skipbits(11);
										value = 23;
										break;
									case 33:
										bitstream.skipbits(11);
										value = 24;
										break;
									case 32:
										bitstream.skipbits(11);
										value = 25;
										break;
									case 31:
										bitstream.skipbits(11);
										value = 26;
										break;
									case 30:
										bitstream.skipbits(11);
										value = 27;
										break;
									case 29:
										bitstream.skipbits(11);
										value = 28;
										break;
									case 28:
										bitstream.skipbits(11);
										value = 29;
										break;
									case 27:
										bitstream.skipbits(11);
										value = 30;
										break;
									case 26:
										bitstream.skipbits(11);
										value = 31;
										break;
									case 25:
										bitstream.skipbits(11);
										value = 32;
										break;
									case 24:
										bitstream.skipbits(11);
										value = 33;
										break;
									default:
										throw new ParsingException("VLC decode for MacroblockAddressIncrement failed.");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public int getValue()
	{
		return value;
	}
}