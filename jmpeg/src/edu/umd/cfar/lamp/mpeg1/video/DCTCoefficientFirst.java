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

class DCTCoefficientFirst implements Parsable
{
	private DCTCoefficientValues value = null;


	public DCTCoefficientValues getValue()
	{
		return value;
	}
	
	public void parse(Bitstream bitstream) throws IOException
	{
		int run   = 0;
		int level = 0;
		
		switch (bitstream.nextbits(2))
		{
        case 2:
            bitstream.skipbits(2);
            run = 0;
            level = 1;
            break;
        case 3:
            bitstream.skipbits(2);
            run = 0;
            level = -1;
            break;
        default:
            switch (bitstream.nextbits(4))
			{
            case 6:
                bitstream.skipbits(4);
                run = 1;
                level = 1;
                break;
            case 7:
                bitstream.skipbits(4);
                run = 1;
                level = -1;
                break;
            default:
                switch (bitstream.nextbits(5)) {
                case 8:
                    bitstream.skipbits(5);
                    run = 0;
                    level = 2;
                    break;
                case 9:
                    bitstream.skipbits(5);
                    run = 0;
                    level = -2;
                    break;
                case 10:
                    bitstream.skipbits(5);
                    run = 2;
                    level = 1;
                    break;
                case 11:
                    bitstream.skipbits(5);
                    run = 2;
                    level = -1;
                    break;
                default:
                    switch (bitstream.nextbits(6))
					{
                    case 10:
                        bitstream.skipbits(6);
                        run = 0;
                        level = 3;
                        break;
                    case 11:
                        bitstream.skipbits(6);
                        run = 0;
                        level = -3;
                        break;
                    case 14:
                        bitstream.skipbits(6);
                        run = 3;
                        level = 1;
                        break;
                    case 15:
                        bitstream.skipbits(6);
                        run = 3;
                        level = -1;
                        break;
                    case 12:
                        bitstream.skipbits(6);
                        run = 4;
                        level = 1;
                        break;
                    case 13:
                        bitstream.skipbits(6);
                        run = 4;
                        level = -1;
                        break;
                    default:
                        switch (bitstream.nextbits(7))
						{
                        case 12:
                            bitstream.skipbits(7);
                            run = 1;
                            level = 2;
                            break;
                        case 13:
                            bitstream.skipbits(7);
                            run = 1;
                            level = -2;
                            break;
                        case 14:
                            bitstream.skipbits(7);
                            run = 5;
                            level = 1;
                            break;
                        case 15:
                            bitstream.skipbits(7);
                            run = 5;
                            level = -1;
                            break;
                        case 10:
                            bitstream.skipbits(7);
                            run = 6;
                            level = 1;
                            break;
                        case 11:
                            bitstream.skipbits(7);
                            run = 6;
                            level = -1;
                            break;
                        case 8:
                            bitstream.skipbits(7);
                            run = 7;
                            level = 1;
                            break;
                        case 9:
                            bitstream.skipbits(7);
                            run = 7;
                            level = -1;
                            break;
                        default:
                            switch (bitstream.nextbits(8))
							{
                            case 12:
                                bitstream.skipbits(8);
                                run = 0;
                                level = 4;
                                break;
                            case 13:
                                bitstream.skipbits(8);
                                run = 0;
                                level = -4;
                                break;
                            case 8:
                                bitstream.skipbits(8);
                                run = 2;
                                level = 2;
                                break;
                            case 9:
                                bitstream.skipbits(8);
                                run = 2;
                                level = -2;
                                break;
                            case 14:
                                bitstream.skipbits(8);
                                run = 8;
                                level = 1;
                                break;
                            case 15:
                                bitstream.skipbits(8);
                                run = 8;
                                level = -1;
                                break;
                            case 10:
                                bitstream.skipbits(8);
                                run = 9;
                                level = 1;
                                break;
                            case 11:
                                bitstream.skipbits(8);
                                run = 9;
                                level = -1;
                                break;
                            default:
                                switch (bitstream.nextbits(9))
								{
                                case 76:
                                    bitstream.skipbits(9);
                                    run = 0;
                                    level = 5;
                                    break;
                                case 77:
                                    bitstream.skipbits(9);
                                    run = 0;
                                    level = -5;
                                    break;
                                case 66:
                                    bitstream.skipbits(9);
                                    run = 0;
                                    level = 6;
                                    break;
                                case 67:
                                    bitstream.skipbits(9);
                                    run = 0;
                                    level = -6;
                                    break;
                                case 74:
                                    bitstream.skipbits(9);
                                    run = 1;
                                    level = 3;
                                    break;
                                case 75:
                                    bitstream.skipbits(9);
                                    run = 1;
                                    level = -3;
                                    break;
                                case 72:
                                    bitstream.skipbits(9);
                                    run = 3;
                                    level = 2;
                                    break;
                                case 73:
                                    bitstream.skipbits(9);
                                    run = 3;
                                    level = -2;
                                    break;
                                case 78:
                                    bitstream.skipbits(9);
                                    run = 10;
                                    level = 1;
                                    break;
                                case 79:
                                    bitstream.skipbits(9);
                                    run = 10;
                                    level = -1;
                                    break;
                                case 70:
                                    bitstream.skipbits(9);
                                    run = 11;
                                    level = 1;
                                    break;
                                case 71:
                                    bitstream.skipbits(9);
                                    run = 11;
                                    level = -1;
                                    break;
                                case 68:
                                    bitstream.skipbits(9);
                                    run = 12;
                                    level = 1;
                                    break;
                                case 69:
                                    bitstream.skipbits(9);
                                    run = 12;
                                    level = -1;
                                    break;
                                case 64:
                                    bitstream.skipbits(9);
                                    run = 13;
                                    level = 1;
                                    break;
                                case 65:
                                    bitstream.skipbits(9);
                                    run = 13;
                                    level = -1;
                                    break;
                                default:
                                    switch (bitstream.nextbits(11))
									{
                                    case 20:
                                        bitstream.skipbits(11);
                                        run = 0;
                                        level = 7;
                                        break;
                                    case 21:
                                        bitstream.skipbits(11);
                                        run = 0;
                                        level = -7;
                                        break;
                                    case 24:
                                        bitstream.skipbits(11);
                                        run = 1;
                                        level = 4;
                                        break;
                                    case 25:
                                        bitstream.skipbits(11);
                                        run = 1;
                                        level = -4;
                                        break;
                                    case 22:
                                        bitstream.skipbits(11);
                                        run = 2;
                                        level = 3;
                                        break;
                                    case 23:
                                        bitstream.skipbits(11);
                                        run = 2;
                                        level = -3;
                                        break;
                                    case 30:
                                        bitstream.skipbits(11);
                                        run = 4;
                                        level = 2;
                                        break;
                                    case 31:
                                        bitstream.skipbits(11);
                                        run = 4;
                                        level = -2;
                                        break;
                                    case 18:
                                        bitstream.skipbits(11);
                                        run = 5;
                                        level = 2;
                                        break;
                                    case 19:
                                        bitstream.skipbits(11);
                                        run = 5;
                                        level = -2;
                                        break;
                                    case 28:
                                        bitstream.skipbits(11);
                                        run = 14;
                                        level = 1;
                                        break;
                                    case 29:
                                        bitstream.skipbits(11);
                                        run = 14;
                                        level = -1;
                                        break;
                                    case 26:
                                        bitstream.skipbits(11);
                                        run = 15;
                                        level = 1;
                                        break;
                                    case 27:
                                        bitstream.skipbits(11);
                                        run = 15;
                                        level = -1;
                                        break;
                                    case 16:
                                        bitstream.skipbits(11);
                                        run = 16;
                                        level = 1;
                                        break;
                                    case 17:
                                        bitstream.skipbits(11);
                                        run = 16;
                                        level = -1;
                                        break;
                                    default:
                                        switch (bitstream.nextbits(13))
										{
                                        case 58:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = 8;
                                            break;
                                        case 59:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = -8;
                                            break;
                                        case 48:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = 9;
                                            break;
                                        case 49:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = -9;
                                            break;
                                        case 38:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = 10;
                                            break;
                                        case 39:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = -10;
                                            break;
                                        case 32:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = 11;
                                            break;
                                        case 33:
                                            bitstream.skipbits(13);
                                            run = 0;
                                            level = -11;
                                            break;
                                        case 54:
                                            bitstream.skipbits(13);
                                            run = 1;
                                            level = 5;
                                            break;
                                        case 55:
                                            bitstream.skipbits(13);
                                            run = 1;
                                            level = -5;
                                            break;
                                        case 40:
                                            bitstream.skipbits(13);
                                            run = 2;
                                            level = 4;
                                            break;
                                        case 41:
                                            bitstream.skipbits(13);
                                            run = 2;
                                            level = -4;
                                            break;
                                        case 56:
                                            bitstream.skipbits(13);
                                            run = 3;
                                            level = 3;
                                            break;
                                        case 57:
                                            bitstream.skipbits(13);
                                            run = 3;
                                            level = -3;
                                            break;
                                        case 36:
                                            bitstream.skipbits(13);
                                            run = 4;
                                            level = 3;
                                            break;
                                        case 37:
                                            bitstream.skipbits(13);
                                            run = 4;
                                            level = -3;
                                            break;
                                        case 60:
                                            bitstream.skipbits(13);
                                            run = 6;
                                            level = 2;
                                            break;
                                        case 61:
                                            bitstream.skipbits(13);
                                            run = 6;
                                            level = -2;
                                            break;
                                        case 42:
                                            bitstream.skipbits(13);
                                            run = 7;
                                            level = 2;
                                            break;
                                        case 43:
                                            bitstream.skipbits(13);
                                            run = 7;
                                            level = -2;
                                            break;
                                        case 34:
                                            bitstream.skipbits(13);
                                            run = 8;
                                            level = 2;
                                            break;
                                        case 35:
                                            bitstream.skipbits(13);
                                            run = 8;
                                            level = -2;
                                            break;
                                        case 62:
                                            bitstream.skipbits(13);
                                            run = 17;
                                            level = 1;
                                            break;
                                        case 63:
                                            bitstream.skipbits(13);
                                            run = 17;
                                            level = -1;
                                            break;
                                        case 52:
                                            bitstream.skipbits(13);
                                            run = 18;
                                            level = 1;
                                            break;
                                        case 53:
                                            bitstream.skipbits(13);
                                            run = 18;
                                            level = -1;
                                            break;
                                        case 50:
                                            bitstream.skipbits(13);
                                            run = 19;
                                            level = 1;
                                            break;
                                        case 51:
                                            bitstream.skipbits(13);
                                            run = 19;
                                            level = -1;
                                            break;
                                        case 46:
                                            bitstream.skipbits(13);
                                            run = 20;
                                            level = 1;
                                            break;
                                        case 47:
                                            bitstream.skipbits(13);
                                            run = 20;
                                            level = -1;
                                            break;
                                        case 44:
                                            bitstream.skipbits(13);
                                            run = 21;
                                            level = 1;
                                            break;
                                        case 45:
                                            bitstream.skipbits(13);
                                            run = 21;
                                            level = -1;
                                            break;
                                        default:
                                            switch (bitstream.nextbits(14))
											{
                                            case 52:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = 12;
                                                break;
                                            case 53:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = -12;
                                                break;
                                            case 50:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = 13;
                                                break;
                                            case 51:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = -13;
                                                break;
                                            case 48:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = 14;
                                                break;
                                            case 49:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = -14;
                                                break;
                                            case 46:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = 15;
                                                break;
                                            case 47:
                                                bitstream.skipbits(14);
                                                run = 0;
                                                level = -15;
                                                break;
                                            case 44:
                                                bitstream.skipbits(14);
                                                run = 1;
                                                level = 6;
                                                break;
                                            case 45:
                                                bitstream.skipbits(14);
                                                run = 1;
                                                level = -6;
                                                break;
                                            case 42:
                                                bitstream.skipbits(14);
                                                run = 1;
                                                level = 7;
                                                break;
                                            case 43:
                                                bitstream.skipbits(14);
                                                run = 1;
                                                level = -7;
                                                break;
                                            case 40:
                                                bitstream.skipbits(14);
                                                run = 2;
                                                level = 5;
                                                break;
                                            case 41:
                                                bitstream.skipbits(14);
                                                run = 2;
                                                level = -5;
                                                break;
                                            case 38:
                                                bitstream.skipbits(14);
                                                run = 3;
                                                level = 4;
                                                break;
                                            case 39:
                                                bitstream.skipbits(14);
                                                run = 3;
                                                level = -4;
                                                break;
                                            case 36:
                                                bitstream.skipbits(14);
                                                run = 5;
                                                level = 3;
                                                break;
                                            case 37:
                                                bitstream.skipbits(14);
                                                run = 5;
                                                level = -3;
                                                break;
                                            case 34:
                                                bitstream.skipbits(14);
                                                run = 9;
                                                level = 2;
                                                break;
                                            case 35:
                                                bitstream.skipbits(14);
                                                run = 9;
                                                level = -2;
                                                break;
                                            case 32:
                                                bitstream.skipbits(14);
                                                run = 10;
                                                level = 2;
                                                break;
                                            case 33:
                                                bitstream.skipbits(14);
                                                run = 10;
                                                level = -2;
                                                break;
                                            case 62:
                                                bitstream.skipbits(14);
                                                run = 22;
                                                level = 1;
                                                break;
                                            case 63:
                                                bitstream.skipbits(14);
                                                run = 22;
                                                level = -1;
                                                break;
                                            case 60:
                                                bitstream.skipbits(14);
                                                run = 23;
                                                level = 1;
                                                break;
                                            case 61:
                                                bitstream.skipbits(14);
                                                run = 23;
                                                level = -1;
                                                break;
                                            case 58:
                                                bitstream.skipbits(14);
                                                run = 24;
                                                level = 1;
                                                break;
                                            case 59:
                                                bitstream.skipbits(14);
                                                run = 24;
                                                level = -1;
                                                break;
                                            case 56:
                                                bitstream.skipbits(14);
                                                run = 25;
                                                level = 1;
                                                break;
                                            case 57:
                                                bitstream.skipbits(14);
                                                run = 25;
                                                level = -1;
                                                break;
                                            case 54:
                                                bitstream.skipbits(14);
                                                run = 26;
                                                level = 1;
                                                break;
                                            case 55:
                                                bitstream.skipbits(14);
                                                run = 26;
                                                level = -1;
                                                break;
                                            default:
                                                switch (bitstream.nextbits(15))
												{
                                                case 62:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 16;
                                                    break;
                                                case 63:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -16;
                                                    break;
                                                case 60:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 17;
                                                    break;
                                                case 61:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -17;
                                                    break;
                                                case 58:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 18;
                                                    break;
                                                case 59:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -18;
                                                    break;
                                                case 56:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 19;
                                                    break;
                                                case 57:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -19;
                                                    break;
                                                case 54:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 20;
                                                    break;
                                                case 55:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -20;
                                                    break;
                                                case 52:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 21;
                                                    break;
                                                case 53:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -21;
                                                    break;
                                                case 50:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 22;
                                                    break;
                                                case 51:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -22;
                                                    break;
                                                case 48:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 23;
                                                    break;
                                                case 49:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -23;
                                                    break;
                                                case 46:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 24;
                                                    break;
                                                case 47:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -24;
                                                    break;
                                                case 44:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 25;
                                                    break;
                                                case 45:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -25;
                                                    break;
                                                case 42:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 26;
                                                    break;
                                                case 43:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -26;
                                                    break;
                                                case 40:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 27;
                                                    break;
                                                case 41:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -27;
                                                    break;
                                                case 38:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 28;
                                                    break;
                                                case 39:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -28;
                                                    break;
                                                case 36:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 29;
                                                    break;
                                                case 37:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -29;
                                                    break;
                                                case 34:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 30;
                                                    break;
                                                case 35:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -30;
                                                    break;
                                                case 32:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = 31;
                                                    break;
                                                case 33:
                                                    bitstream.skipbits(15);
                                                    run = 0;
                                                    level = -31;
                                                    break;
                                                default:
                                                    switch (bitstream.nextbits(16))
													{
                                                    case 48:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 32;
                                                        break;
                                                    case 49:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -32;
                                                        break;
                                                    case 46:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 33;
                                                        break;
                                                    case 47:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -33;
                                                        break;
                                                    case 44:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 34;
                                                        break;
                                                    case 45:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -34;
                                                        break;
                                                    case 42:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 35;
                                                        break;
                                                    case 43:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -35;
                                                        break;
                                                    case 40:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 36;
                                                        break;
                                                    case 41:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -36;
                                                        break;
                                                    case 38:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 37;
                                                        break;
                                                    case 39:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -37;
                                                        break;
                                                    case 36:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 38;
                                                        break;
                                                    case 37:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -38;
                                                        break;
                                                    case 34:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 39;
                                                        break;
                                                    case 35:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -39;
                                                        break;
                                                    case 32:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = 40;
                                                        break;
                                                    case 33:
                                                        bitstream.skipbits(16);
                                                        run = 0;
                                                        level = -40;
                                                        break;
                                                    case 62:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 8;
                                                        break;
                                                    case 63:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -8;
                                                        break;
                                                    case 60:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 9;
                                                        break;
                                                    case 61:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -9;
                                                        break;
                                                    case 58:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 10;
                                                        break;
                                                    case 59:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -10;
                                                        break;
                                                    case 56:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 11;
                                                        break;
                                                    case 57:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -11;
                                                        break;
                                                    case 54:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 12;
                                                        break;
                                                    case 55:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -12;
                                                        break;
                                                    case 52:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 13;
                                                        break;
                                                    case 53:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -13;
                                                        break;
                                                    case 50:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = 14;
                                                        break;
                                                    case 51:
                                                        bitstream.skipbits(16);
                                                        run = 1;
                                                        level = -14;
                                                        break;
                                                    default:
                                                        switch (bitstream.nextbits(17))
														{
                                                        case 38:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = 15;
                                                            break;
                                                        case 39:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = -15;
                                                            break;
                                                        case 36:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = 16;
                                                            break;
                                                        case 37:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = -16;
                                                            break;
                                                        case 34:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = 17;
                                                            break;
                                                        case 35:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = -17;
                                                            break;
                                                        case 32:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = 18;
                                                            break;
                                                        case 33:
                                                            bitstream.skipbits(17);
                                                            run = 1;
                                                            level = -18;
                                                            break;
                                                        case 40:
                                                            bitstream.skipbits(17);
                                                            run = 6;
                                                            level = 3;
                                                            break;
                                                        case 41:
                                                            bitstream.skipbits(17);
                                                            run = 6;
                                                            level = -3;
                                                            break;
                                                        case 52:
                                                            bitstream.skipbits(17);
                                                            run = 11;
                                                            level = 2;
                                                            break;
                                                        case 53:
                                                            bitstream.skipbits(17);
                                                            run = 11;
                                                            level = -2;
                                                            break;
                                                        case 50:
                                                            bitstream.skipbits(17);
                                                            run = 12;
                                                            level = 2;
                                                            break;
                                                        case 51:
                                                            bitstream.skipbits(17);
                                                            run = 12;
                                                            level = -2;
                                                            break;
                                                        case 48:
                                                            bitstream.skipbits(17);
                                                            run = 13;
                                                            level = 2;
                                                            break;
                                                        case 49:
                                                            bitstream.skipbits(17);
                                                            run = 13;
                                                            level = -2;
                                                            break;
                                                        case 46:
                                                            bitstream.skipbits(17);
                                                            run = 14;
                                                            level = 2;
                                                            break;
                                                        case 47:
                                                            bitstream.skipbits(17);
                                                            run = 14;
                                                            level = -2;
                                                            break;
                                                        case 44:
                                                            bitstream.skipbits(17);
                                                            run = 15;
                                                            level = 2;
                                                            break;
                                                        case 45:
                                                            bitstream.skipbits(17);
                                                            run = 15;
                                                            level = -2;
                                                            break;
                                                        case 42:
                                                            bitstream.skipbits(17);
                                                            run = 16;
                                                            level = 2;
                                                            break;
                                                        case 43:
                                                            bitstream.skipbits(17);
                                                            run = 16;
                                                            level = -2;
                                                            break;
                                                        case 62:
                                                            bitstream.skipbits(17);
                                                            run = 27;
                                                            level = 1;
                                                            break;
                                                        case 63:
                                                            bitstream.skipbits(17);
                                                            run = 27;
                                                            level = -1;
                                                            break;
                                                        case 60:
                                                            bitstream.skipbits(17);
                                                            run = 28;
                                                            level = 1;
                                                            break;
                                                        case 61:
                                                            bitstream.skipbits(17);
                                                            run = 28;
                                                            level = -1;
                                                            break;
                                                        case 58:
                                                            bitstream.skipbits(17);
                                                            run = 29;
                                                            level = 1;
                                                            break;
                                                        case 59:
                                                            bitstream.skipbits(17);
                                                            run = 29;
                                                            level = -1;
                                                            break;
                                                        case 56:
                                                            bitstream.skipbits(17);
                                                            run = 30;
                                                            level = 1;
                                                            break;
                                                        case 57:
                                                            bitstream.skipbits(17);
                                                            run = 30;
                                                            level = -1;
                                                            break;
                                                        case 54:
                                                            bitstream.skipbits(17);
                                                            run = 31;
                                                            level = 1;
                                                            break;
                                                        case 55:
                                                            bitstream.skipbits(17);
                                                            run = 31;
                                                            level = -1;
                                                            break;
                                                        default:
                                                            throw new ParsingException("VLC decode for DCTCoefficientFirst failed.");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

		value = new DCTCoefficientValues(run, level);
	}
}
