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


package edu.umd.cfar.lamp.viper.examples.persontracking.images;

import java.awt.*;
import java.awt.image.*;

public class SmartImageUtilities {
	public static final int NLEVELS = 4;
	public static final int NSTEP = 256 / NLEVELS;
	public static final int NColors = NLEVELS * NLEVELS * NLEVELS;
	public static int Distance = 2;
	
	public static float rgb2lum(BufferedImage im, int x, int y) {
		if (im == null) {
			return 1;
		}
		Color c = new Color(im.getRGB(x,y));
		float[] hsb = new float[3];
		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
		return hsb[2];
	}
	
	public static int[][][] buildCorrelogram(BufferedImage bi, BufferedImage bi_BGS) {
		int[][][] correlogram = new int[NColors][NColors][Distance];
		int[][] sum = new int[NColors][Distance];
		int mid = (int) (-16777216 * 0.5);

		String blackvalue = "11111111000000000000000000000000";
		try {
			int count = 0;
			for (int x = 0; x < bi.getWidth(); x++) {
				for (int y = 0; y < bi.getHeight(); y++) {
					// for each x,y pixel in the slice
					final int sourceRGB = bi.getRGB(x, y);
					final float alpha = rgb2lum(bi_BGS, x, y);
					if (.1 < alpha) {
						final Color sourceColor = new Color(sourceRGB);
						final int sourceBucket = color2bucket(sourceColor);

						for (int l = 0; l < Distance; l++) {
							for (int d1 = -Distance; d1 <= Distance; d1++) {
								for (int d2 = -Distance; d2 <= Distance; d2++) {
									if ((l == Math.abs(d1))
											|| (l == Math.abs(d2))) {
										if ((x + d1 >= 0)
												&& (x + d1 < bi.getWidth())
												&& (y + d2 >= 0)
												&& (y + d2 < bi.getHeight())) {
											final int targetRGB = bi.getRGB(x + d1, y + d2);
											final Color targetC = new Color(sourceRGB);
											final int targetBucket = color2bucket(targetC);
											correlogram[sourceBucket][targetBucket][l]++;
											sum[sourceBucket][l]++;
											count++;
										}
									}
								}
							}
						}
					}
				}
			}
			int v = 0;
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < NColors; i++) {
			for (int j = 0; j < NColors; j++) {
				for (int k = 0; k < Distance; k++) {
					if (sum[i][k] == 0) {
						correlogram[i][j][k] = 0;
					}
					else {
						correlogram[i][j][k] = (int) (100 * correlogram[i][j][k] / sum[i][k]);
					}
				}
			}
		}
		
		return correlogram;
	}

	/**
	 * @param c
	 * @return
	 */
	private static int color2bucket(Color c) {
		final int redBucket = c.getRed() >> 6;
		final int greenBucket = c.getGreen() >> 6;
		final int blueBucket = c.getBlue() >> 6;
		final int rval = (redBucket << 4) | (greenBucket << 2) | (blueBucket);
		assert rval < NColors;
		assert 0 <= rval;
		return rval;
	}

	
	public static int correlogramSimilarity(int[][][] cgramA, int[][][] cgramB) {
		int D = 0;
		int N = 0;
		for (int i = 0; i < NColors; i++) {
			for (int j = 0; j < NColors; j++) {
				for (int k = 0; k < Distance; k++) {
					D = D + Math.abs(cgramA[i][j][k] - cgramB[i][j][k]);
					N = N + cgramA[i][j][k] + cgramB[i][j][k];
				}
			}
		}
		return (int) (100 * (1.0 - ((float) D / N)));
	}

	public static Dimension smartResize(int width, int height, int MAX_WIDTH) {
		int tX = width;
		int tY = height;
		if (MAX_WIDTH < width || MAX_WIDTH < height) {
			if (height < width) {
				// wide image
				tX = MAX_WIDTH;
				tY = height * MAX_WIDTH / width;
			} else if (height == width){
				tX = MAX_WIDTH; 
				tY = MAX_WIDTH;
			} else {
				// tall image
				tX = width * MAX_WIDTH / height;
				tY = MAX_WIDTH;
			}
		}
		return new Dimension(tX, tY);
	}

	public static Dimension smartResize(int width, int height, int MAX_WIDTH, int MAX_HEIGHT) {
		if (MAX_WIDTH == MAX_HEIGHT) {
			return smartResize(width, height, MAX_WIDTH);
		}
		int tX = width;
		int tY = height;
		if (MAX_WIDTH < width || MAX_HEIGHT < height) {
			double xFactor = (double) MAX_WIDTH / width;
			double yFactor = (double) MAX_HEIGHT / height;
			double factor = (xFactor < yFactor) ? xFactor : yFactor;
			tX = (int) (width * factor);
			tY = (int) (height * factor);
		}
		return new Dimension(tX, tY);
	}
}
