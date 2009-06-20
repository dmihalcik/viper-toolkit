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

class MacroblockPattern implements Parsable
{
	private int value = 0;

	
	public void parse(Bitstream bitstream) throws IOException
	{
		switch (bitstream.nextbits(3))
		{
		case 7:
			bitstream.skipbits(3);
			value = 60;
			break;
		default:
			switch (bitstream.nextbits(4))
			{
			case 13:
				bitstream.skipbits(4);
				value = 4;
				break;
			case 12:
				bitstream.skipbits(4);
				value = 8;
				break;
			case 11:
				bitstream.skipbits(4);
				value = 16;
				break;
			case 10:
				bitstream.skipbits(4);
				value = 32;
				break;
			default:
				switch (bitstream.nextbits(5))
				{
				case 19:
					bitstream.skipbits(5);
					value = 12;
					break;
				case 18:
					bitstream.skipbits(5);
					value = 48;
					break;
				case 17:
					bitstream.skipbits(5);
					value = 20;
					break;
				case 16:
					bitstream.skipbits(5);
					value = 40;
					break;
				case 15:
					bitstream.skipbits(5);
					value = 28;
					break;
				case 14:
					bitstream.skipbits(5);
					value = 44;
					break;
				case 13:
					bitstream.skipbits(5);
					value = 52;
					break;
				case 12:
					bitstream.skipbits(5);
					value = 56;
					break;
				case 11:
					bitstream.skipbits(5);
					value = 1;
					break;
				case 10:
					bitstream.skipbits(5);
					value = 61;
					break;
				case 9:
					bitstream.skipbits(5);
					value = 2;
					break;
				case 8:
					bitstream.skipbits(5);
					value = 62;
					break;
				default:
					switch (bitstream.nextbits(6))
					{
					case 15:
						bitstream.skipbits(6);
						value = 24;
						break;
					case 14:
						bitstream.skipbits(6);
						value = 36;
						break;
					case 13:
						bitstream.skipbits(6);
						value = 3;
						break;
					case 12:
						bitstream.skipbits(6);
						value = 63;
						break;
					default:
						switch (bitstream.nextbits(7))
						{
						case 23:
							bitstream.skipbits(7);
							value = 5;
							break;
						case 22:
							bitstream.skipbits(7);
							value = 9;
							break;
						case 21:
							bitstream.skipbits(7);
							value = 17;
							break;
						case 20:
							bitstream.skipbits(7);
							value = 33;
							break;
						case 19:
							bitstream.skipbits(7);
							value = 6;
							break;
						case 18:
							bitstream.skipbits(7);
							value = 10;
							break;
						case 17:
							bitstream.skipbits(7);
							value = 18;
							break;
						case 16:
							bitstream.skipbits(7);
							value = 34;
							break;
						default:
							switch (bitstream.nextbits(8))
							{
							case 31:
								bitstream.skipbits(8);
								value = 7;
								break;
							case 30:
								bitstream.skipbits(8);
								value = 11;
								break;
							case 29:
								bitstream.skipbits(8);
								value = 19;
								break;
							case 28:
								bitstream.skipbits(8);
								value = 35;
								break;
							case 27:
								bitstream.skipbits(8);
								value = 13;
								break;
							case 26:
								bitstream.skipbits(8);
								value = 49;
								break;
							case 25:
								bitstream.skipbits(8);
								value = 21;
								break;
							case 24:
								bitstream.skipbits(8);
								value = 41;
								break;
							case 23:
								bitstream.skipbits(8);
								value = 14;
								break;
							case 22:
								bitstream.skipbits(8);
								value = 50;
								break;
							case 21:
								bitstream.skipbits(8);
								value = 22;
								break;
							case 20:
								bitstream.skipbits(8);
								value = 42;
								break;
							case 19:
								bitstream.skipbits(8);
								value = 15;
								break;
							case 18:
								bitstream.skipbits(8);
								value = 51;
								break;
							case 17:
								bitstream.skipbits(8);
								value = 23;
								break;
							case 16:
								bitstream.skipbits(8);
								value = 43;
								break;
							case 15:
								bitstream.skipbits(8);
								value = 25;
								break;
							case 14:
								bitstream.skipbits(8);
								value = 37;
								break;
							case 13:
								bitstream.skipbits(8);
								value = 26;
								break;
							case 12:
								bitstream.skipbits(8);
								value = 38;
								break;
							case 11:
								bitstream.skipbits(8);
								value = 29;
								break;
							case 10:
								bitstream.skipbits(8);
								value = 45;
								break;
							case 9:
								bitstream.skipbits(8);
								value = 53;
								break;
							case 8:
								bitstream.skipbits(8);
								value = 57;
								break;
							case 7:
								bitstream.skipbits(8);
								value = 30;
								break;
							case 6:
								bitstream.skipbits(8);
								value = 46;
								break;
							case 5:
								bitstream.skipbits(8);
								value = 54;
								break;
							case 4:
								bitstream.skipbits(8);
								value = 58;
								break;
							default:
								switch (bitstream.nextbits(9))
								{
								case 7:
									bitstream.skipbits(9);
									value = 31;
									break;
								case 6:
									bitstream.skipbits(9);
									value = 47;
									break;
								case 5:
									bitstream.skipbits(9);
									value = 55;
									break;
								case 4:
									bitstream.skipbits(9);
									value = 59;
									break;
								case 3:
									bitstream.skipbits(9);
									value = 27;
									break;
								case 2:
									bitstream.skipbits(9);
									value = 39;
									break;
								default:
									throw new ParsingException("VLC decode for MacroblockPattern failed.");
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
