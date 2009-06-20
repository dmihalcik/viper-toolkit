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

class DCTCoefficientEscapeLevel implements Parsable
{
	private int level = 0;

	
	public void parse(Bitstream bitstream) throws IOException
	{
		switch (bitstream.nextbits(8))
		{
		case 129:
            bitstream.skipbits(8);
            level = -127;
			break;
        case 130:
            bitstream.skipbits(8);
            level = -126;
			break;
        case 131:
            bitstream.skipbits(8);
            level = -125;
			break;
        case 132:
            bitstream.skipbits(8);
            level = -124;
			break;
        case 133:
            bitstream.skipbits(8);
            level = -123;
			break;
        case 134:
            bitstream.skipbits(8);
            level = -122;
			break;
        case 135:
            bitstream.skipbits(8);
            level = -121;
			break;
        case 136:
            bitstream.skipbits(8);
            level = -120;
			break;
        case 137:
            bitstream.skipbits(8);
            level = -119;
			break;
        case 138:
            bitstream.skipbits(8);
            level = -118;
			break;
        case 139:
            bitstream.skipbits(8);
            level = -117;
			break;
        case 140:
            bitstream.skipbits(8);
            level = -116;
			break;
        case 141:
            bitstream.skipbits(8);
            level = -115;
			break;
        case 142:
            bitstream.skipbits(8);
            level = -114;
			break;
        case 143:
            bitstream.skipbits(8);
            level = -113;
			break;
        case 144:
            bitstream.skipbits(8);
            level = -112;
			break;
        case 145:
            bitstream.skipbits(8);
            level = -111;
			break;
        case 146:
            bitstream.skipbits(8);
            level = -110;
			break;
        case 147:
            bitstream.skipbits(8);
            level = -109;
			break;
        case 148:
            bitstream.skipbits(8);
            level = -108;
			break;
        case 149:
            bitstream.skipbits(8);
            level = -107;
			break;
        case 150:
            bitstream.skipbits(8);
            level = -106;
			break;
        case 151:
            bitstream.skipbits(8);
            level = -105;
			break;
        case 152:
            bitstream.skipbits(8);
            level = -104;
			break;
        case 153:
            bitstream.skipbits(8);
            level = -103;
			break;
        case 154:
            bitstream.skipbits(8);
            level = -102;
			break;
        case 155:
            bitstream.skipbits(8);
            level = -101;
			break;
        case 156:
            bitstream.skipbits(8);
            level = -100;
			break;
        case 157:
            bitstream.skipbits(8);
            level = -99;
			break;
        case 158:
            bitstream.skipbits(8);
            level = -98;
			break;
        case 159:
            bitstream.skipbits(8);
            level = -97;
			break;
        case 160:
            bitstream.skipbits(8);
            level = -96;
			break;
        case 161:
            bitstream.skipbits(8);
            level = -95;
			break;
        case 162:
            bitstream.skipbits(8);
            level = -94;
			break;
        case 163:
            bitstream.skipbits(8);
            level = -93;
			break;
        case 164:
            bitstream.skipbits(8);
            level = -92;
			break;
        case 165:
            bitstream.skipbits(8);
            level = -91;
			break;
        case 166:
            bitstream.skipbits(8);
            level = -90;
			break;
        case 167:
            bitstream.skipbits(8);
            level = -89;
			break;
        case 168:
            bitstream.skipbits(8);
            level = -88;
			break;
        case 169:
            bitstream.skipbits(8);
            level = -87;
			break;
        case 170:
            bitstream.skipbits(8);
            level = -86;
			break;
        case 171:
            bitstream.skipbits(8);
            level = -85;
			break;
        case 172:
            bitstream.skipbits(8);
            level = -84;
			break;
        case 173:
            bitstream.skipbits(8);
            level = -83;
			break;
        case 174:
            bitstream.skipbits(8);
            level = -82;
			break;
        case 175:
            bitstream.skipbits(8);
            level = -81;
			break;
        case 176:
            bitstream.skipbits(8);
            level = -80;
			break;
        case 177:
            bitstream.skipbits(8);
            level = -79;
			break;
        case 178:
            bitstream.skipbits(8);
            level = -78;
			break;
        case 179:
            bitstream.skipbits(8);
            level = -77;
			break;
        case 180:
            bitstream.skipbits(8);
            level = -76;
			break;
        case 181:
            bitstream.skipbits(8);
            level = -75;
			break;
        case 182:
            bitstream.skipbits(8);
            level = -74;
			break;
        case 183:
            bitstream.skipbits(8);
            level = -73;
			break;
        case 184:
            bitstream.skipbits(8);
            level = -72;
			break;
        case 185:
            bitstream.skipbits(8);
            level = -71;
			break;
        case 186:
            bitstream.skipbits(8);
            level = -70;
			break;
        case 187:
            bitstream.skipbits(8);
            level = -69;
			break;
        case 188:
            bitstream.skipbits(8);
            level = -68;
			break;
        case 189:
            bitstream.skipbits(8);
            level = -67;
			break;
        case 190:
            bitstream.skipbits(8);
            level = -66;
			break;
        case 191:
            bitstream.skipbits(8);
            level = -65;
			break;
        case 192:
            bitstream.skipbits(8);
            level = -64;
			break;
        case 193:
            bitstream.skipbits(8);
            level = -63;
			break;
        case 194:
            bitstream.skipbits(8);
            level = -62;
			break;
        case 195:
            bitstream.skipbits(8);
            level = -61;
			break;
        case 196:
            bitstream.skipbits(8);
            level = -60;
			break;
        case 197:
            bitstream.skipbits(8);
            level = -59;
			break;
        case 198:
            bitstream.skipbits(8);
            level = -58;
			break;
        case 199:
            bitstream.skipbits(8);
            level = -57;
			break;
        case 200:
            bitstream.skipbits(8);
            level = -56;
			break;
        case 201:
            bitstream.skipbits(8);
            level = -55;
			break;
        case 202:
            bitstream.skipbits(8);
            level = -54;
			break;
        case 203:
            bitstream.skipbits(8);
            level = -53;
			break;
        case 204:
            bitstream.skipbits(8);
            level = -52;
			break;
        case 205:
            bitstream.skipbits(8);
            level = -51;
			break;
        case 206:
            bitstream.skipbits(8);
            level = -50;
			break;
        case 207:
            bitstream.skipbits(8);
            level = -49;
			break;
        case 208:
            bitstream.skipbits(8);
            level = -48;
			break;
        case 209:
            bitstream.skipbits(8);
            level = -47;
			break;
        case 210:
            bitstream.skipbits(8);
            level = -46;
			break;
        case 211:
            bitstream.skipbits(8);
            level = -45;
			break;
        case 212:
            bitstream.skipbits(8);
            level = -44;
			break;
        case 213:
            bitstream.skipbits(8);
            level = -43;
			break;
        case 214:
            bitstream.skipbits(8);
            level = -42;
			break;
        case 215:
            bitstream.skipbits(8);
            level = -41;
			break;
        case 216:
            bitstream.skipbits(8);
            level = -40;
			break;
        case 217:
            bitstream.skipbits(8);
            level = -39;
			break;
        case 218:
            bitstream.skipbits(8);
            level = -38;
			break;
        case 219:
            bitstream.skipbits(8);
            level = -37;
			break;
        case 220:
            bitstream.skipbits(8);
            level = -36;
			break;
        case 221:
            bitstream.skipbits(8);
            level = -35;
			break;
        case 222:
            bitstream.skipbits(8);
            level = -34;
			break;
        case 223:
            bitstream.skipbits(8);
            level = -33;
			break;
        case 224:
            bitstream.skipbits(8);
            level = -32;
			break;
        case 225:
            bitstream.skipbits(8);
            level = -31;
			break;
        case 226:
            bitstream.skipbits(8);
            level = -30;
			break;
        case 227:
            bitstream.skipbits(8);
            level = -29;
			break;
        case 228:
            bitstream.skipbits(8);
            level = -28;
			break;
        case 229:
            bitstream.skipbits(8);
            level = -27;
			break;
        case 230:
            bitstream.skipbits(8);
            level = -26;
			break;
        case 231:
            bitstream.skipbits(8);
            level = -25;
			break;
        case 232:
            bitstream.skipbits(8);
            level = -24;
			break;
        case 233:
            bitstream.skipbits(8);
            level = -23;
			break;
        case 234:
            bitstream.skipbits(8);
            level = -22;
			break;
        case 235:
            bitstream.skipbits(8);
            level = -21;
			break;
        case 236:
            bitstream.skipbits(8);
            level = -20;
			break;
        case 237:
            bitstream.skipbits(8);
            level = -19;
			break;
        case 238:
            bitstream.skipbits(8);
            level = -18;
			break;
        case 239:
            bitstream.skipbits(8);
            level = -17;
			break;
        case 240:
            bitstream.skipbits(8);
            level = -16;
			break;
        case 241:
            bitstream.skipbits(8);
            level = -15;
			break;
        case 242:
            bitstream.skipbits(8);
            level = -14;
			break;
        case 243:
            bitstream.skipbits(8);
            level = -13;
			break;
        case 244:
            bitstream.skipbits(8);
            level = -12;
			break;
        case 245:
            bitstream.skipbits(8);
            level = -11;
			break;
        case 246:
            bitstream.skipbits(8);
            level = -10;
			break;
        case 247:
            bitstream.skipbits(8);
            level = -9;
			break;
        case 248:
            bitstream.skipbits(8);
            level = -8;
			break;
        case 249:
            bitstream.skipbits(8);
            level = -7;
			break;
        case 250:
            bitstream.skipbits(8);
            level = -6;
			break;
        case 251:
            bitstream.skipbits(8);
            level = -5;
			break;
        case 252:
            bitstream.skipbits(8);
            level = -4;
			break;
        case 253:
            bitstream.skipbits(8);
            level = -3;
			break;
        case 254:
            bitstream.skipbits(8);
            level = -2;
			break;
        case 255:
            bitstream.skipbits(8);
            level = -1;
			break;
        case 1:
            bitstream.skipbits(8);
            level = 1;
			break;
        case 2:
            bitstream.skipbits(8);
            level = 2;
			break;
        case 3:
            bitstream.skipbits(8);
            level = 3;
			break;
        case 4:
            bitstream.skipbits(8);
            level = 4;
			break;
        case 5:
            bitstream.skipbits(8);
            level = 5;
			break;
        case 6:
            bitstream.skipbits(8);
            level = 6;
			break;
        case 7:
            bitstream.skipbits(8);
            level = 7;
			break;
        case 8:
            bitstream.skipbits(8);
            level = 8;
			break;
        case 9:
            bitstream.skipbits(8);
            level = 9;
			break;
        case 10:
            bitstream.skipbits(8);
            level = 10;
			break;
        case 11:
            bitstream.skipbits(8);
            level = 11;
			break;
        case 12:
            bitstream.skipbits(8);
            level = 12;
			break;
        case 13:
            bitstream.skipbits(8);
            level = 13;
			break;
        case 14:
            bitstream.skipbits(8);
            level = 14;
			break;
        case 15:
            bitstream.skipbits(8);
            level = 15;
			break;
        case 16:
            bitstream.skipbits(8);
            level = 16;
			break;
        case 17:
            bitstream.skipbits(8);
            level = 17;
			break;
        case 18:
            bitstream.skipbits(8);
            level = 18;
			break;
        case 19:
            bitstream.skipbits(8);
            level = 19;
			break;
        case 20:
            bitstream.skipbits(8);
            level = 20;
			break;
        case 21:
            bitstream.skipbits(8);
            level = 21;
			break;
        case 22:
            bitstream.skipbits(8);
            level = 22;
			break;
        case 23:
            bitstream.skipbits(8);
            level = 23;
			break;
        case 24:
            bitstream.skipbits(8);
            level = 24;
			break;
        case 25:
            bitstream.skipbits(8);
            level = 25;
			break;
        case 26:
            bitstream.skipbits(8);
            level = 26;
			break;
        case 27:
            bitstream.skipbits(8);
            level = 27;
			break;
        case 28:
            bitstream.skipbits(8);
            level = 28;
			break;
        case 29:
            bitstream.skipbits(8);
            level = 29;
			break;
        case 30:
            bitstream.skipbits(8);
            level = 30;
			break;
        case 31:
            bitstream.skipbits(8);
            level = 31;
			break;
        case 32:
            bitstream.skipbits(8);
            level = 32;
			break;
        case 33:
            bitstream.skipbits(8);
            level = 33;
			break;
        case 34:
            bitstream.skipbits(8);
            level = 34;
			break;
        case 35:
            bitstream.skipbits(8);
            level = 35;
			break;
        case 36:
            bitstream.skipbits(8);
            level = 36;
			break;
        case 37:
            bitstream.skipbits(8);
            level = 37;
			break;
        case 38:
            bitstream.skipbits(8);
            level = 38;
			break;
        case 39:
            bitstream.skipbits(8);
            level = 39;
			break;
        case 40:
            bitstream.skipbits(8);
            level = 40;
			break;
        case 41:
            bitstream.skipbits(8);
            level = 41;
			break;
        case 42:
            bitstream.skipbits(8);
            level = 42;
			break;
        case 43:
            bitstream.skipbits(8);
            level = 43;
			break;
        case 44:
            bitstream.skipbits(8);
            level = 44;
			break;
        case 45:
            bitstream.skipbits(8);
            level = 45;
			break;
        case 46:
            bitstream.skipbits(8);
            level = 46;
			break;
        case 47:
            bitstream.skipbits(8);
            level = 47;
			break;
        case 48:
            bitstream.skipbits(8);
            level = 48;
			break;
        case 49:
            bitstream.skipbits(8);
            level = 49;
			break;
        case 50:
            bitstream.skipbits(8);
            level = 50;
			break;
        case 51:
            bitstream.skipbits(8);
            level = 51;
			break;
        case 52:
            bitstream.skipbits(8);
            level = 52;
			break;
        case 53:
            bitstream.skipbits(8);
            level = 53;
			break;
        case 54:
            bitstream.skipbits(8);
            level = 54;
			break;
        case 55:
            bitstream.skipbits(8);
            level = 55;
			break;
        case 56:
            bitstream.skipbits(8);
            level = 56;
			break;
        case 57:
            bitstream.skipbits(8);
            level = 57;
			break;
        case 58:
            bitstream.skipbits(8);
            level = 58;
			break;
        case 59:
            bitstream.skipbits(8);
            level = 59;
			break;
        case 60:
            bitstream.skipbits(8);
            level = 60;
			break;
        case 61:
            bitstream.skipbits(8);
            level = 61;
			break;
        case 62:
            bitstream.skipbits(8);
            level = 62;
			break;
        case 63:
            bitstream.skipbits(8);
            level = 63;
			break;
        case 64:
            bitstream.skipbits(8);
            level = 64;
			break;
        case 65:
            bitstream.skipbits(8);
            level = 65;
			break;
        case 66:
            bitstream.skipbits(8);
            level = 66;
			break;
        case 67:
            bitstream.skipbits(8);
            level = 67;
			break;
        case 68:
            bitstream.skipbits(8);
            level = 68;
			break;
        case 69:
            bitstream.skipbits(8);
            level = 69;
			break;
        case 70:
            bitstream.skipbits(8);
            level = 70;
			break;
        case 71:
            bitstream.skipbits(8);
            level = 71;
			break;
        case 72:
            bitstream.skipbits(8);
            level = 72;
			break;
        case 73:
            bitstream.skipbits(8);
            level = 73;
			break;
        case 74:
            bitstream.skipbits(8);
            level = 74;
			break;
        case 75:
            bitstream.skipbits(8);
            level = 75;
			break;
        case 76:
            bitstream.skipbits(8);
            level = 76;
			break;
        case 77:
            bitstream.skipbits(8);
            level = 77;
			break;
        case 78:
            bitstream.skipbits(8);
            level = 78;
			break;
        case 79:
            bitstream.skipbits(8);
            level = 79;
			break;
        case 80:
            bitstream.skipbits(8);
            level = 80;
			break;
        case 81:
            bitstream.skipbits(8);
            level = 81;
			break;
        case 82:
            bitstream.skipbits(8);
            level = 82;
			break;
        case 83:
            bitstream.skipbits(8);
            level = 83;
			break;
        case 84:
            bitstream.skipbits(8);
            level = 84;
			break;
        case 85:
            bitstream.skipbits(8);
            level = 85;
			break;
        case 86:
            bitstream.skipbits(8);
            level = 86;
			break;
        case 87:
            bitstream.skipbits(8);
            level = 87;
			break;
        case 88:
            bitstream.skipbits(8);
            level = 88;
			break;
        case 89:
            bitstream.skipbits(8);
            level = 89;
			break;
        case 90:
            bitstream.skipbits(8);
            level = 90;
			break;
        case 91:
            bitstream.skipbits(8);
            level = 91;
			break;
        case 92:
            bitstream.skipbits(8);
            level = 92;
			break;
        case 93:
            bitstream.skipbits(8);
            level = 93;
			break;
        case 94:
            bitstream.skipbits(8);
            level = 94;
			break;
        case 95:
            bitstream.skipbits(8);
            level = 95;
			break;
        case 96:
            bitstream.skipbits(8);
            level = 96;
			break;
        case 97:
            bitstream.skipbits(8);
            level = 97;
			break;
        case 98:
            bitstream.skipbits(8);
            level = 98;
			break;
        case 99:
            bitstream.skipbits(8);
            level = 99;
			break;
        case 100:
            bitstream.skipbits(8);
            level = 100;
			break;
        case 101:
            bitstream.skipbits(8);
            level = 101;
			break;
        case 102:
            bitstream.skipbits(8);
            level = 102;
			break;
        case 103:
            bitstream.skipbits(8);
            level = 103;
			break;
        case 104:
            bitstream.skipbits(8);
            level = 104;
			break;
        case 105:
            bitstream.skipbits(8);
            level = 105;
			break;
        case 106:
            bitstream.skipbits(8);
            level = 106;
			break;
        case 107:
            bitstream.skipbits(8);
            level = 107;
			break;
        case 108:
            bitstream.skipbits(8);
            level = 108;
			break;
        case 109:
            bitstream.skipbits(8);
            level = 109;
			break;
        case 110:
            bitstream.skipbits(8);
            level = 110;
			break;
        case 111:
            bitstream.skipbits(8);
            level = 111;
			break;
        case 112:
            bitstream.skipbits(8);
            level = 112;
			break;
        case 113:
            bitstream.skipbits(8);
            level = 113;
			break;
        case 114:
            bitstream.skipbits(8);
            level = 114;
			break;
        case 115:
            bitstream.skipbits(8);
            level = 115;
			break;
        case 116:
            bitstream.skipbits(8);
            level = 116;
			break;
        case 117:
            bitstream.skipbits(8);
            level = 117;
			break;
        case 118:
            bitstream.skipbits(8);
            level = 118;
			break;
        case 119:
            bitstream.skipbits(8);
            level = 119;
			break;
        case 120:
            bitstream.skipbits(8);
            level = 120;
			break;
        case 121:
            bitstream.skipbits(8);
            level = 121;
			break;
        case 122:
            bitstream.skipbits(8);
            level = 122;
			break;
        case 123:
            bitstream.skipbits(8);
            level = 123;
			break;
        case 124:
            bitstream.skipbits(8);
            level = 124;
			break;
        case 125:
            bitstream.skipbits(8);
            level = 125;
			break;
        case 126:
            bitstream.skipbits(8);
            level = 126;
			break;
        case 127:
            bitstream.skipbits(8);
            level = 127;
			break;
        default:
            switch (bitstream.nextbits(16))
			{
            case 32769:
                bitstream.skipbits(16);
                level = -255;
                break;
            case 32770:
                bitstream.skipbits(16);
                level = -254;
                break;
            case 32771:
                bitstream.skipbits(16);
                level = -253;
                break;
            case 32772:
                bitstream.skipbits(16);
                level = -252;
                break;
            case 32773:
                bitstream.skipbits(16);
                level = -251;
                break;
            case 32774:
                bitstream.skipbits(16);
                level = -250;
                break;
            case 32775:
                bitstream.skipbits(16);
                level = -249;
                break;
            case 32776:
                bitstream.skipbits(16);
                level = -248;
                break;
            case 32777:
                bitstream.skipbits(16);
                level = -247;
                break;
            case 32778:
                bitstream.skipbits(16);
                level = -246;
                break;
            case 32779:
                bitstream.skipbits(16);
                level = -245;
                break;
            case 32780:
                bitstream.skipbits(16);
                level = -244;
                break;
            case 32781:
                bitstream.skipbits(16);
                level = -243;
                break;
            case 32782:
                bitstream.skipbits(16);
                level = -242;
                break;
            case 32783:
                bitstream.skipbits(16);
                level = -241;
                break;
            case 32784:
                bitstream.skipbits(16);
                level = -240;
                break;
            case 32785:
                bitstream.skipbits(16);
                level = -239;
                break;
            case 32786:
                bitstream.skipbits(16);
                level = -238;
                break;
            case 32787:
                bitstream.skipbits(16);
                level = -237;
                break;
            case 32788:
                bitstream.skipbits(16);
                level = -236;
                break;
            case 32789:
                bitstream.skipbits(16);
                level = -235;
                break;
            case 32790:
                bitstream.skipbits(16);
                level = -234;
                break;
            case 32791:
                bitstream.skipbits(16);
                level = -233;
                break;
            case 32792:
                bitstream.skipbits(16);
                level = -232;
                break;
            case 32793:
                bitstream.skipbits(16);
                level = -231;
                break;
            case 32794:
                bitstream.skipbits(16);
                level = -230;
                break;
            case 32795:
                bitstream.skipbits(16);
                level = -229;
                break;
            case 32796:
                bitstream.skipbits(16);
                level = -228;
                break;
            case 32797:
                bitstream.skipbits(16);
                level = -227;
                break;
            case 32798:
                bitstream.skipbits(16);
                level = -226;
                break;
            case 32799:
                bitstream.skipbits(16);
                level = -225;
                break;
            case 32800:
                bitstream.skipbits(16);
                level = -224;
                break;
            case 32801:
                bitstream.skipbits(16);
                level = -223;
                break;
            case 32802:
                bitstream.skipbits(16);
                level = -222;
                break;
            case 32803:
                bitstream.skipbits(16);
                level = -221;
                break;
            case 32804:
                bitstream.skipbits(16);
                level = -220;
                break;
            case 32805:
                bitstream.skipbits(16);
                level = -219;
                break;
            case 32806:
                bitstream.skipbits(16);
                level = -218;
                break;
            case 32807:
                bitstream.skipbits(16);
                level = -217;
                break;
            case 32808:
                bitstream.skipbits(16);
                level = -216;
                break;
            case 32809:
                bitstream.skipbits(16);
                level = -215;
                break;
            case 32810:
                bitstream.skipbits(16);
                level = -214;
                break;
            case 32811:
                bitstream.skipbits(16);
                level = -213;
                break;
            case 32812:
                bitstream.skipbits(16);
                level = -212;
                break;
            case 32813:
                bitstream.skipbits(16);
                level = -211;
                break;
            case 32814:
                bitstream.skipbits(16);
                level = -210;
                break;
            case 32815:
                bitstream.skipbits(16);
                level = -209;
                break;
            case 32816:
                bitstream.skipbits(16);
                level = -208;
                break;
            case 32817:
                bitstream.skipbits(16);
                level = -207;
                break;
            case 32818:
                bitstream.skipbits(16);
                level = -206;
                break;
            case 32819:
                bitstream.skipbits(16);
                level = -205;
                break;
            case 32820:
                bitstream.skipbits(16);
                level = -204;
                break;
            case 32821:
                bitstream.skipbits(16);
                level = -203;
                break;
            case 32822:
                bitstream.skipbits(16);
                level = -202;
                break;
            case 32823:
                bitstream.skipbits(16);
                level = -201;
                break;
            case 32824:
                bitstream.skipbits(16);
                level = -200;
                break;
            case 32825:
                bitstream.skipbits(16);
                level = -199;
                break;
            case 32826:
                bitstream.skipbits(16);
                level = -198;
                break;
            case 32827:
                bitstream.skipbits(16);
                level = -197;
                break;
            case 32828:
                bitstream.skipbits(16);
                level = -196;
                break;
            case 32829:
                bitstream.skipbits(16);
                level = -195;
                break;
            case 32830:
                bitstream.skipbits(16);
                level = -194;
                break;
            case 32831:
                bitstream.skipbits(16);
                level = -193;
                break;
            case 32832:
                bitstream.skipbits(16);
                level = -192;
                break;
            case 32833:
                bitstream.skipbits(16);
                level = -191;
                break;
            case 32834:
                bitstream.skipbits(16);
                level = -190;
                break;
            case 32835:
                bitstream.skipbits(16);
                level = -189;
                break;
            case 32836:
                bitstream.skipbits(16);
                level = -188;
                break;
            case 32837:
                bitstream.skipbits(16);
                level = -187;
                break;
            case 32838:
                bitstream.skipbits(16);
                level = -186;
                break;
            case 32839:
                bitstream.skipbits(16);
                level = -185;
                break;
            case 32840:
                bitstream.skipbits(16);
                level = -184;
                break;
            case 32841:
                bitstream.skipbits(16);
                level = -183;
                break;
            case 32842:
                bitstream.skipbits(16);
                level = -182;
                break;
            case 32843:
                bitstream.skipbits(16);
                level = -181;
                break;
            case 32844:
                bitstream.skipbits(16);
                level = -180;
                break;
            case 32845:
                bitstream.skipbits(16);
                level = -179;
                break;
            case 32846:
                bitstream.skipbits(16);
                level = -178;
                break;
            case 32847:
                bitstream.skipbits(16);
                level = -177;
                break;
            case 32848:
                bitstream.skipbits(16);
                level = -176;
                break;
            case 32849:
                bitstream.skipbits(16);
                level = -175;
                break;
            case 32850:
                bitstream.skipbits(16);
                level = -174;
                break;
            case 32851:
                bitstream.skipbits(16);
                level = -173;
                break;
            case 32852:
                bitstream.skipbits(16);
                level = -172;
                break;
            case 32853:
                bitstream.skipbits(16);
                level = -171;
                break;
            case 32854:
                bitstream.skipbits(16);
                level = -170;
                break;
            case 32855:
                bitstream.skipbits(16);
                level = -169;
                break;
            case 32856:
                bitstream.skipbits(16);
                level = -168;
                break;
            case 32857:
                bitstream.skipbits(16);
                level = -167;
                break;
            case 32858:
                bitstream.skipbits(16);
                level = -166;
                break;
            case 32859:
                bitstream.skipbits(16);
                level = -165;
                break;
            case 32860:
                bitstream.skipbits(16);
                level = -164;
                break;
            case 32861:
                bitstream.skipbits(16);
                level = -163;
                break;
            case 32862:
                bitstream.skipbits(16);
                level = -162;
                break;
            case 32863:
                bitstream.skipbits(16);
                level = -161;
                break;
            case 32864:
                bitstream.skipbits(16);
                level = -160;
                break;
            case 32865:
                bitstream.skipbits(16);
                level = -159;
                break;
            case 32866:
                bitstream.skipbits(16);
                level = -158;
                break;
            case 32867:
                bitstream.skipbits(16);
                level = -157;
                break;
            case 32868:
                bitstream.skipbits(16);
                level = -156;
                break;
            case 32869:
                bitstream.skipbits(16);
                level = -155;
                break;
            case 32870:
                bitstream.skipbits(16);
                level = -154;
                break;
            case 32871:
                bitstream.skipbits(16);
                level = -153;
                break;
            case 32872:
                bitstream.skipbits(16);
                level = -152;
                break;
            case 32873:
                bitstream.skipbits(16);
                level = -151;
                break;
            case 32874:
                bitstream.skipbits(16);
                level = -150;
                break;
            case 32875:
                bitstream.skipbits(16);
                level = -149;
                break;
            case 32876:
                bitstream.skipbits(16);
                level = -148;
                break;
            case 32877:
                bitstream.skipbits(16);
                level = -147;
                break;
            case 32878:
                bitstream.skipbits(16);
                level = -146;
                break;
            case 32879:
                bitstream.skipbits(16);
                level = -145;
                break;
            case 32880:
                bitstream.skipbits(16);
                level = -144;
                break;
            case 32881:
                bitstream.skipbits(16);
                level = -143;
                break;
            case 32882:
                bitstream.skipbits(16);
                level = -142;
                break;
            case 32883:
                bitstream.skipbits(16);
                level = -141;
                break;
            case 32884:
                bitstream.skipbits(16);
                level = -140;
                break;
            case 32885:
                bitstream.skipbits(16);
                level = -139;
                break;
            case 32886:
                bitstream.skipbits(16);
                level = -138;
                break;
            case 32887:
                bitstream.skipbits(16);
                level = -137;
                break;
            case 32888:
                bitstream.skipbits(16);
                level = -136;
                break;
            case 32889:
                bitstream.skipbits(16);
                level = -135;
                break;
            case 32890:
                bitstream.skipbits(16);
                level = -134;
                break;
            case 32891:
                bitstream.skipbits(16);
                level = -133;
                break;
            case 32892:
                bitstream.skipbits(16);
                level = -132;
                break;
            case 32893:
                bitstream.skipbits(16);
                level = -131;
                break;
            case 32894:
                bitstream.skipbits(16);
                level = -130;
                break;
            case 32895:
                bitstream.skipbits(16);
                level = -129;
                break;
            case 32896:
                bitstream.skipbits(16);
                level = -128;
                break;
            case 128:
                bitstream.skipbits(16);
                level = 128;
                break;
            case 129:
                bitstream.skipbits(16);
                level = 129;
                break;
            case 130:
                bitstream.skipbits(16);
                level = 130;
                break;
            case 131:
                bitstream.skipbits(16);
                level = 131;
                break;
            case 132:
                bitstream.skipbits(16);
                level = 132;
                break;
            case 133:
                bitstream.skipbits(16);
                level = 133;
                break;
            case 134:
                bitstream.skipbits(16);
                level = 134;
                break;
            case 135:
                bitstream.skipbits(16);
                level = 135;
                break;
            case 136:
                bitstream.skipbits(16);
                level = 136;
                break;
            case 137:
                bitstream.skipbits(16);
                level = 137;
                break;
            case 138:
                bitstream.skipbits(16);
                level = 138;
                break;
            case 139:
                bitstream.skipbits(16);
                level = 139;
                break;
            case 140:
                bitstream.skipbits(16);
                level = 140;
                break;
            case 141:
                bitstream.skipbits(16);
                level = 141;
                break;
            case 142:
                bitstream.skipbits(16);
                level = 142;
                break;
            case 143:
                bitstream.skipbits(16);
                level = 143;
                break;
            case 144:
                bitstream.skipbits(16);
                level = 144;
                break;
            case 145:
                bitstream.skipbits(16);
                level = 145;
                break;
            case 146:
                bitstream.skipbits(16);
                level = 146;
                break;
            case 147:
                bitstream.skipbits(16);
                level = 147;
                break;
            case 148:
                bitstream.skipbits(16);
                level = 148;
                break;
            case 149:
                bitstream.skipbits(16);
                level = 149;
                break;
            case 150:
                bitstream.skipbits(16);
                level = 150;
                break;
            case 151:
                bitstream.skipbits(16);
                level = 151;
                break;
            case 152:
                bitstream.skipbits(16);
                level = 152;
                break;
            case 153:
                bitstream.skipbits(16);
                level = 153;
                break;
            case 154:
                bitstream.skipbits(16);
                level = 154;
                break;
            case 155:
                bitstream.skipbits(16);
                level = 155;
                break;
            case 156:
                bitstream.skipbits(16);
                level = 156;
                break;
            case 157:
                bitstream.skipbits(16);
                level = 157;
                break;
            case 158:
                bitstream.skipbits(16);
                level = 158;
                break;
            case 159:
                bitstream.skipbits(16);
                level = 159;
                break;
            case 160:
                bitstream.skipbits(16);
                level = 160;
                break;
            case 161:
                bitstream.skipbits(16);
                level = 161;
                break;
            case 162:
                bitstream.skipbits(16);
                level = 162;
                break;
            case 163:
                bitstream.skipbits(16);
                level = 163;
                break;
            case 164:
                bitstream.skipbits(16);
                level = 164;
                break;
            case 165:
                bitstream.skipbits(16);
                level = 165;
                break;
            case 166:
                bitstream.skipbits(16);
                level = 166;
                break;
            case 167:
                bitstream.skipbits(16);
                level = 167;
                break;
            case 168:
                bitstream.skipbits(16);
                level = 168;
                break;
            case 169:
                bitstream.skipbits(16);
                level = 169;
                break;
            case 170:
                bitstream.skipbits(16);
                level = 170;
                break;
            case 171:
                bitstream.skipbits(16);
                level = 171;
                break;
            case 172:
                bitstream.skipbits(16);
                level = 172;
                break;
            case 173:
                bitstream.skipbits(16);
                level = 173;
                break;
            case 174:
                bitstream.skipbits(16);
                level = 174;
                break;
            case 175:
                bitstream.skipbits(16);
                level = 175;
                break;
            case 176:
                bitstream.skipbits(16);
                level = 176;
                break;
            case 177:
                bitstream.skipbits(16);
                level = 177;
                break;
            case 178:
                bitstream.skipbits(16);
                level = 178;
                break;
            case 179:
                bitstream.skipbits(16);
                level = 179;
                break;
            case 180:
                bitstream.skipbits(16);
                level = 180;
                break;
            case 181:
                bitstream.skipbits(16);
                level = 181;
                break;
            case 182:
                bitstream.skipbits(16);
                level = 182;
                break;
            case 183:
                bitstream.skipbits(16);
                level = 183;
                break;
            case 184:
                bitstream.skipbits(16);
                level = 184;
                break;
            case 185:
                bitstream.skipbits(16);
                level = 185;
                break;
            case 186:
                bitstream.skipbits(16);
                level = 186;
                break;
            case 187:
                bitstream.skipbits(16);
                level = 187;
                break;
            case 188:
                bitstream.skipbits(16);
                level = 188;
                break;
            case 189:
                bitstream.skipbits(16);
                level = 189;
                break;
            case 190:
                bitstream.skipbits(16);
                level = 190;
                break;
            case 191:
                bitstream.skipbits(16);
                level = 191;
                break;
            case 192:
                bitstream.skipbits(16);
                level = 192;
                break;
            case 193:
                bitstream.skipbits(16);
                level = 193;
                break;
            case 194:
                bitstream.skipbits(16);
                level = 194;
                break;
            case 195:
                bitstream.skipbits(16);
                level = 195;
                break;
            case 196:
                bitstream.skipbits(16);
                level = 196;
                break;
            case 197:
                bitstream.skipbits(16);
                level = 197;
                break;
            case 198:
                bitstream.skipbits(16);
                level = 198;
                break;
            case 199:
                bitstream.skipbits(16);
                level = 199;
                break;
            case 200:
                bitstream.skipbits(16);
                level = 200;
                break;
            case 201:
                bitstream.skipbits(16);
                level = 201;
                break;
            case 202:
                bitstream.skipbits(16);
                level = 202;
                break;
            case 203:
                bitstream.skipbits(16);
                level = 203;
                break;
            case 204:
                bitstream.skipbits(16);
                level = 204;
                break;
            case 205:
                bitstream.skipbits(16);
                level = 205;
                break;
            case 206:
                bitstream.skipbits(16);
                level = 206;
                break;
            case 207:
                bitstream.skipbits(16);
                level = 207;
                break;
            case 208:
                bitstream.skipbits(16);
                level = 208;
                break;
            case 209:
                bitstream.skipbits(16);
                level = 209;
                break;
            case 210:
                bitstream.skipbits(16);
                level = 210;
                break;
            case 211:
                bitstream.skipbits(16);
                level = 211;
                break;
            case 212:
                bitstream.skipbits(16);
                level = 212;
                break;
            case 213:
                bitstream.skipbits(16);
                level = 213;
                break;
            case 214:
                bitstream.skipbits(16);
                level = 214;
                break;
            case 215:
                bitstream.skipbits(16);
                level = 215;
                break;
            case 216:
                bitstream.skipbits(16);
                level = 216;
                break;
            case 217:
                bitstream.skipbits(16);
                level = 217;
                break;
            case 218:
                bitstream.skipbits(16);
                level = 218;
                break;
            case 219:
                bitstream.skipbits(16);
                level = 219;
                break;
            case 220:
                bitstream.skipbits(16);
                level = 220;
                break;
            case 221:
                bitstream.skipbits(16);
                level = 221;
                break;
            case 222:
                bitstream.skipbits(16);
                level = 222;
                break;
            case 223:
                bitstream.skipbits(16);
                level = 223;
                break;
            case 224:
                bitstream.skipbits(16);
                level = 224;
                break;
            case 225:
                bitstream.skipbits(16);
                level = 225;
                break;
            case 226:
                bitstream.skipbits(16);
                level = 226;
                break;
            case 227:
                bitstream.skipbits(16);
                level = 227;
                break;
            case 228:
                bitstream.skipbits(16);
                level = 228;
                break;
            case 229:
                bitstream.skipbits(16);
                level = 229;
                break;
            case 230:
                bitstream.skipbits(16);
                level = 230;
                break;
            case 231:
                bitstream.skipbits(16);
                level = 231;
                break;
            case 232:
                bitstream.skipbits(16);
                level = 232;
                break;
            case 233:
                bitstream.skipbits(16);
                level = 233;
                break;
            case 234:
                bitstream.skipbits(16);
                level = 234;
                break;
            case 235:
                bitstream.skipbits(16);
                level = 235;
                break;
            case 236:
                bitstream.skipbits(16);
                level = 236;
                break;
            case 237:
                bitstream.skipbits(16);
                level = 237;
                break;
            case 238:
                bitstream.skipbits(16);
                level = 238;
                break;
            case 239:
                bitstream.skipbits(16);
                level = 239;
                break;
            case 240:
                bitstream.skipbits(16);
                level = 240;
                break;
            case 241:
                bitstream.skipbits(16);
                level = 241;
                break;
            case 242:
                bitstream.skipbits(16);
                level = 242;
                break;
            case 243:
                bitstream.skipbits(16);
                level = 243;
                break;
            case 244:
                bitstream.skipbits(16);
                level = 244;
                break;
            case 245:
                bitstream.skipbits(16);
                level = 245;
                break;
            case 246:
                bitstream.skipbits(16);
                level = 246;
                break;
            case 247:
                bitstream.skipbits(16);
                level = 247;
                break;
            case 248:
                bitstream.skipbits(16);
                level = 248;
                break;
            case 249:
                bitstream.skipbits(16);
                level = 249;
                break;
            case 250:
                bitstream.skipbits(16);
                level = 250;
                break;
            case 251:
                bitstream.skipbits(16);
                level = 251;
                break;
            case 252:
                bitstream.skipbits(16);
                level = 252;
                break;
            case 253:
                bitstream.skipbits(16);
                level = 253;
                break;
            case 254:
                bitstream.skipbits(16);
                level = 254;
                break;
            case 255:
                bitstream.skipbits(16);
                level = 255;
                break;
			default:
				throw new ParsingException("VLC decode for DCTCoefficientEscapeLevel failed.");
			}
		}
	}

	public int getValue()
	{
		return level;
	}
}
