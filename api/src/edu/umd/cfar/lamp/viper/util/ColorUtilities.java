/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/


package edu.umd.cfar.lamp.viper.util;
import java.awt.*;
import java.util.*;


/**
 * A quick hack for placeholder colors, these color utility methods
 * and static items allow quick access to colors by their English name.
 * @author davidm
 */
public class ColorUtilities {
	/**
	 * A map of web color names, e.g. <q>antiquewhite</q> or <q>salmon</q>.
	 * For a comprehensive list, see the <a href="http://www.w3.org/TR/css3-color/">w3c's
	 * css color module documenation</a>.
	 */
	public static Map WEB_COLOR_NAMES;
	public static Color translucent = new Color(0,0,0,0);

	private static void init() {
		if (WEB_COLOR_NAMES != null) 
			return;
		String[] names =
			new String[] {
				"aliceblue",
				"antiquewhite",
				"aqua",
				"aquamarine",
				"azure",
				"beige",
				"bisque",
				"black",
				"blanchedalmond",
				"blue",
				"blueviolet",
				"brown",
				"burlywood",
				"cadetblue",
				"chartreuse",
				"chocolate",
				"coral",
				"cornflowerblue",
				"cornsilk",
				"crimson",
				"cyan",
				"darkblue",
				"darkcyan",
				"darkgoldenrod",
				"darkgray",
				"darkgreen",
				"darkgrey",
				"darkkhaki",
				"darkmagenta",
				"darkolivegreen",
				"darkorange",
				"darkorchid",
				"darkred",
				"darksalmon",
				"darkseagreen",
				"darkslateblue",
				"darkslategray",
				"darkslategrey",
				"darkturquoise",
				"darkviolet",
				"deeppink",
				"deepskyblue",
				"dimgray",
				"dimgrey",
				"dodgerblue",
				"firebrick",
				"floralwhite",
				"forestgreen",
				"fuchsia",
				"gainsboro",
				"ghostwhite",
				"gold",
				"goldenrod",
				"gray",
				"green",
				"greenyellow",
				"grey",
				"honeydew",
				"hotpink",
				"indianred",
				"indigo",
				"ivory",
				"khaki",
				"lavender",
				"lavenderblush",
				"lawngreen",
				"lemonchiffon",
				"lightblue",
				"lightcoral",
				"lightcyan",
				"lightgoldenrodyellow",
				"lightgray",
				"lightgreen",
				"lightgrey",
				"lightpink",
				"lightsalmon",
				"lightseagreen",
				"lightskyblue",
				"lightslategray",
				"lightslategrey",
				"lightsteelblue",
				"lightyellow",
				"lime",
				"limegreen",
				"linen",
				"magenta",
				"maroon",
				"mediumaquamarine",
				"mediumblue",
				"mediumorchid",
				"mediumpurple",
				"mediumseagreen",
				"mediumslateblue",
				"mediumspringgreen",
				"mediumturquoise",
				"mediumvioletred",
				"midnightblue",
				"mintcream",
				"mistyrose",
				"moccasin",
				"navajowhite",
				"navy",
				"oldlace",
				"olive",
				"olivedrab",
				"orange",
				"orangered",
				"orchid",
				"palegoldenrod",
				"palegreen",
				"paleturquoise",
				"palevioletred",
				"papayawhip",
				"peachpuff",
				"peru",
				"pink",
				"plum",
				"powderblue",
				"purple",
				"red",
				"rosybrown",
				"royalblue",
				"saddlebrown",
				"salmon",
				"sandybrown",
				"seagreen",
				"seashell",
				"sienna",
				"silver",
				"skyblue",
				"slateblue",
				"slategray",
				"slategrey",
				"snow",
				"springgreen",
				"steelblue",
				"tan",
				"teal",
				"thistle",
				"tomato",
				"turquoise",
				"violet",
				"wheat",
				"white",
				"whitesmoke",
				"yellow",
				"yellowgreen" };
		int[][] values = new int[][] { { 240, 248, 255 }, {
				250, 235, 215 }, {
				0, 255, 255 }, {
				127, 255, 212 }, {
				240, 255, 255 }, {
				245, 245, 220 }, {
				255, 228, 196 }, {
				0, 0, 0 }, {
				255, 235, 205 }, {
				0, 0, 255 }, {
				138, 43, 226 }, {
				165, 42, 42 }, {
				222, 184, 135 }, {
				95, 158, 160 }, {
				127, 255, 0 }, {
				210, 105, 30 }, {
				255, 127, 80 }, {
				100, 149, 237 }, {
				255, 248, 220 }, {
				220, 20, 60 }, {
				0, 255, 255 }, {
				0, 0, 139 }, {
				0, 139, 139 }, {
				184, 134, 11 }, {
				169, 169, 169 }, {
				0, 100, 0 }, {
				169, 169, 169 }, {
				189, 183, 107 }, {
				139, 0, 139 }, {
				85, 107, 47 }, {
				255, 140, 0 }, {
				153, 50, 204 }, {
				139, 0, 0 }, {
				233, 150, 122 }, {
				143, 188, 143 }, {
				72, 61, 139 }, {
				47, 79, 79 }, {
				47, 79, 79 }, {
				0, 206, 209 }, {
				148, 0, 211 }, {
				255, 20, 147 }, {
				0, 191, 255 }, {
				105, 105, 105 }, {
				105, 105, 105 }, {
				30, 144, 255 }, {
				178, 34, 34 }, {
				255, 250, 240 }, {
				34, 139, 34 }, {
				255, 0, 255 }, {
				220, 220, 220 }, {
				248, 248, 255 }, {
				255, 215, 0 }, {
				218, 165, 32 }, {
				128, 128, 128 }, {
				0, 128, 0 }, {
				173, 255, 47 }, {
				128, 128, 128 }, {
				240, 255, 240 }, {
				255, 105, 180 }, {
				205, 92, 92 }, {
				75, 0, 130 }, {
				255, 255, 240 }, {
				240, 230, 140 }, {
				230, 230, 250 }, {
				255, 240, 245 }, {
				124, 252, 0 }, {
				255, 250, 205 }, {
				173, 216, 230 }, {
				240, 128, 128 }, {
				224, 255, 255 }, {
				250, 250, 210 }, {
				211, 211, 211 }, {
				144, 238, 144 }, {
				211, 211, 211 }, {
				255, 182, 193 }, {
				255, 160, 122 }, {
				42, 178, 170 }, {
				135, 206, 250 }, {
				119, 136, 153 }, {
				119, 136, 153 }, {
				176, 196, 222 }, {
				255, 255, 224 }, {
				0, 255, 0 }, {
				60, 205, 50 }, {
				250, 240, 230 }, {
				255, 0, 255 }, {
				128, 0, 0 }, {
				102, 205, 170 }, {
				0, 0, 205 }, {
				186, 85, 211 }, {
				147, 112, 219 }, {
				60, 179, 113 }, {
				123, 104, 238 }, {
				0, 250, 154 }, {
				72, 209, 204 }, {
				199, 21, 133 }, {
				25, 25, 112 }, {
				245, 255, 250 }, {
				255, 228, 225 }, {
				255, 228, 181 }, {
				255, 222, 173 }, {
				0, 0, 128 }, {
				253, 245, 230 }, {
				128, 128, 0 }, {
				107, 142, 35 }, {
				255, 165, 0 }, {
				255, 69, 0 }, {
				218, 112, 214 }, {
				238, 232, 170 }, {
				152, 251, 152 }, {
				175, 238, 238 }, {
				219, 112, 147 }, {
				255, 239, 213 }, {
				255, 218, 185 }, {
				205, 133, 63 }, {
				255, 192, 203 }, {
				221, 160, 221 }, {
				176, 224, 230 }, {
				128, 0, 128 }, {
				255, 0, 0 }, {
				188, 143, 143 }, {
				65, 105, 225 }, {
				139, 69, 19 }, {
				250, 128, 114 }, {
				244, 164, 96 }, {
				56, 139, 87 }, {
				255, 245, 238 }, {
				160, 82, 45 }, {
				192, 192, 192 }, {
				135, 206, 235 }, {
				106, 90, 205 }, {
				112, 128, 144 }, {
				212, 128, 144 }, {
				255, 250, 250 }, {
				0, 255, 127 }, {
				70, 130, 180 }, {
				210, 180, 140 }, {
				1, 128, 128 }, {
				216, 191, 216 }, {
				255, 99, 71 }, {
				74, 224, 208 }, {
				238, 130, 238 }, {
				245, 222, 179 }, {
				255, 255, 255 }, {
				245, 245, 245 }, {
				255, 255, 0 }, {
				154, 205, 50 }, };
		WEB_COLOR_NAMES = new HashMap();
		for (int i = 0; i < names.length; i++) {
			ColorUtilities.WEB_COLOR_NAMES.put(
				names[i],
				new Color(values[i][0], values[i][1], values[i][2]));
		}
	}

	/**
	 * Gets the java color corresponding to the given CSS color name.
	 * @param name the color name, e.g. <q>khaki</q> or <q>burlywood</q>
	 * @return the matching java color, or <code>null</code> if no color
	 * matches. XXX: Should it throw an illegal argument exception?
	 */
	public static Color getColor(String name) {
		init(); //somehow, invoking this method doesn't call the static initializer? 
		assert name != null;
		assert WEB_COLOR_NAMES != null;

		return (Color) WEB_COLOR_NAMES.get(name.toLowerCase());
	}
	
	
	/**
	 * Converts from the given RGB triplet into a corresponding
	 * XYZ triplet.
	 * @param RGB red, green and blue components, in the range from 0 to 1
	 * @return the XYZ color components
	 */
	public static double[] rgb2xyz(double[] RGB) {
		double[] xyz = new double[3];
		double[] rgb = new double[3];
		System.arraycopy(RGB, 0, rgb, 0, 3);
		for (int i = 0; i < 3; i++) {
			if (rgb[i] > 0.04045 ) {
				rgb[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
			} else {
				rgb[i] = rgb[i] / 12.92;
			}
		}

		xyz[0] = rgb[0] * 0.4124 + rgb[1] * 0.3576 + rgb[2] * 0.1805;
		xyz[1] = rgb[0] * 0.2126 + rgb[1] * 0.7152 + rgb[2] * 0.0722;
		xyz[2] = rgb[0] * 0.0193 + rgb[1] * 0.1192 + rgb[2] * 0.9505;
		return xyz;
	}
	
	/**
	 * Converts from XYZ to RGB.
	 * @param XYZ a color in XYZ space
	 * @return the color, converted to RBG space
	 */
	public static double[] xyz2rgb(double[] XYZ) {
		double[] xyz = new double[3];
		double[] rgb = new double[3];
		double[] refXYZ = new double[] {0.95047, 1, 0.108883};
		System.arraycopy(XYZ, 0, xyz, 0, 3);
		for (int i = 0; i < 3; i++) {
			xyz[i] = Math.min(xyz[i], refXYZ[i]);
			xyz[i] = Math.max(0, xyz[i]);
		}
		rgb[0] = xyz[0] *  3.2406 + xyz[1] * -1.5372 + xyz[2] * -0.4986;
		rgb[1] = xyz[0] * -0.9689 + xyz[1] *  1.8758 + xyz[2] *  0.0415;
		rgb[2] = xyz[0] *  0.0557 + xyz[1] * -0.2040 + xyz[2] *  1.0570;
		for (int i = 0; i < 3; i++) {
			if (rgb[i] > 0.0031308 ) {
				rgb[i] = 1.055 * Math.pow(rgb[i], (1 / 2.4)) - 0.055;
			} else {
				rgb[i] = 12.92 * rgb[i];
			}
		}
		return rgb;
	}
	
	public static Color changeAlpha(Color c, double alpha) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha*255));
	}
}
