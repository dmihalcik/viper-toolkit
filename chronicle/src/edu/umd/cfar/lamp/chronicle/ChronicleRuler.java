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

package edu.umd.cfar.lamp.chronicle;

import java.awt.*;
import java.util.*;

import org.apache.commons.lang.ObjectUtils;

import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;


/**
 * Displays rules across the header 
 */
public class ChronicleRuler extends PNode {
	private final ChronicleViewer viewer;
	private int rulerHeight = 32;
	private int minDistance = 8;
	private int minRuleHeight = 8;
	private int minWidth = 0;
	private long start = 0;
	private long end = 1;
	private long firstVisibleFrame = 0;
	private long lastVisibleFrame = 1;
	private int minFrameLabellingDistance = 256;

	private static long[] widths;
	static {
		widths = new long[3 * ((int) (Math.log(Long.MAX_VALUE) / Math.log(10)))];
		long t = 1;
		for (int i = 0; i < widths.length / 3; i++) {
			int q = i*3;
			widths[q]   = t;
			widths[q+1] = t*2;
			widths[q+2] = t*5;
			t *= 10;
		}
	}

	/**
	 * @param viewer the viewer to hold the ruler
	 */
	ChronicleRuler(ChronicleViewer viewer) {
		this.viewer = viewer;
	}
	
	private static int findChronicleWidthsApproximation(long toApprox) {
		int w = Arrays.binarySearch(ChronicleRuler.widths, toApprox); 
		if (w < 0) {
			w = -w - 1;
		}
		assert w >= 0;
		assert w < ChronicleRuler.widths.length;
		return w;
	}
	
	/**
	 * Resets the view with the new hashes
	 * @param paintContext
	 * @param c
	 */
	private void resetMinWidth(PPaintContext paintContext, PCanvas c) {
		InstantInterval ii = this.viewer.model.getFocus();
		start = ii.getStartInstant().longValue();
		assert start >= 0;
		end = ii.getEndInstant().longValue();
		long frameCount = Math.max(1, end - start); // number of frames in the video
		long pixCount = (long) this.viewer.getViewLength(); // number of pixels wide the ruler is
		long maxHashes = pixCount / minDistance; // maximum number of marks to display on ruler at any given time
		maxHashes = Math.max(1,maxHashes);
		long preferredHashWidth = end / maxHashes; // would prefer to draw every 'perfHashWidth'th frame
		minWidth = findChronicleWidthsApproximation(preferredHashWidth); // will draw every widths[minwidth] frame

		long pixPerFrame = pixCount / frameCount;
		long minX = Math.max(0, (long) paintContext.getLocalClip().getMinX());
		long maxX = (long) paintContext.getLocalClip().getMaxX();
		long thirdPix = c.getWidth();
		thirdPix /= 3;
		firstVisibleFrame = (minX * frameCount / pixCount) + start;
		lastVisibleFrame = (maxX * frameCount / pixCount) + 1 + start;
		firstVisibleFrame = Math.max(firstVisibleFrame, start);
		lastVisibleFrame = Math.min(lastVisibleFrame, end);
		assert firstVisibleFrame >= 0;
		assert lastVisibleFrame >= 0;
		assert lastVisibleFrame >= firstVisibleFrame;
		long preferredLabelWidth = viewer.pixel2instantDistance((int)thirdPix);
		minFrameLabellingDistance = findChronicleWidthsApproximation(preferredLabelWidth);
	}
	
	/**
	 * Gets how far from the top of the timeline that the
	 * line starts drawing. this is more useful than the 
	 * ruler line length, somehow.
	 * @param atMost
	 * @param myWidth
	 * @return
	 */
	private int getRuleOffset(int atMost, int myWidth) {
		int off = rulerHeight - minRuleHeight;
		double maxFractionHeight = 1;
		double heightRange = rulerHeight * maxFractionHeight; // max offset
		heightRange = Math.max(0, heightRange);
		double fraction = Math.log(widths[myWidth]) / Math.log(widths[atMost]);
		off -= (int) ((fraction * heightRange));
		return Math.max(0, off);
	}
	
	
	private void drawHashForFrame(int f, PPaintContext paintContext, int atLeast, int atMost, int myWidth) {
		Color color = width2color(myWidth);
		
		InstantInterval focus = this.viewer.model.getFocus();
		double frameOffset = f - focus.getStartInstant().doubleValue();
		double frameLocation = (frameOffset * this.viewer.getViewLength()) / focus.width();
		int x = (int) (frameLocation); //- paintContext.getLocalClip().getMinX());
		int y2 = 10000;
		int y1 = getRuleOffset(atMost, myWidth);

		drawLine(x, y1, x, y2, color, new BasicStroke(1), paintContext);
	}
	
	private Color[] lineColorTable = new Color[] {Color.black, Color.blue, Color.red};

	
	protected Color width2color(int myWidth) {
		// idea 5 = red, 2 = blue, 1 = black (
		return lineColorTable[myWidth % lineColorTable.length];
	}
	
	/**
	 * @param frameToLabel
	 * @param paintContext
	 * @param labelDist
	 */
	protected void drawLabelForFrame(long frameToLabel, PPaintContext paintContext, long labelDist) {
		textStamp.setText(String.valueOf(frameToLabel));
		double frameLocation = viewer.instant2pixelDistance(new Frame((int) frameToLabel-1));
		PBounds textBounds = textStamp.getBoundsReference();
		double x = frameLocation - (textBounds.width / 2);
		textStamp.setBounds(x, textBounds.y, textBounds.width, textBounds.height);
		textStamp.paint(paintContext);
	}

	private PTextLabel textStamp = new PTextLabel();
	private Paint backgroundColor;

	private void drawLine(int x1, int y1, int x2, int y2, Paint p, Stroke stroke, PPaintContext paintContext) {
		Graphics2D g2 = paintContext.getGraphics();
		Paint old = g2.getPaint();
		Stroke oldS = g2.getStroke();
		g2.setPaint(p);
		g2.setStroke(stroke);
		g2.drawLine(x1, y1, x2, y2);
		g2.setPaint(old);
		g2.setStroke(oldS);
	}
 	
	/**
	 * {@inheritDoc}
	 */
	public void paint(PPaintContext paintContext) {
		if (this.backgroundColor != null) {
			Graphics2D graph = paintContext.getGraphics();
			graph.setPaint(backgroundColor);
			graph.fill(getBoundsReference());
		}
		
		if (this.viewer.model != null && this.viewer.model.getFocus() != null) {
			resetMinWidth(paintContext, viewer.getScrollViews().columnHeader);
			int maxWidthRule = findChronicleWidthsApproximation(end);
			maxWidthRule -= 1;
			int i = (int) ((firstVisibleFrame + ChronicleRuler.widths[minWidth] - 1) / ChronicleRuler.widths[minWidth]);
			int firstI = i;
			int end = (int) (lastVisibleFrame / ChronicleRuler.widths[minWidth]);
			boolean[] found = new boolean[1 + end - i];
			int toAvoid = -1;
			if (minWidth % 3 == 1) {
				toAvoid = minWidth + 1;
			}
			for (int width = maxWidthRule; width >= minWidth; width--) {
				if (width != toAvoid) {
					int diff = (int) (ChronicleRuler.widths[width] / ChronicleRuler.widths[minWidth]);
					i = (int) ((firstVisibleFrame + ChronicleRuler.widths[width] - 1) / ChronicleRuler.widths[width]);
					end = (int) (lastVisibleFrame / ChronicleRuler.widths[width]);
					while (i <= end) {
						long frameNum = i*ChronicleRuler.widths[width];
						int borderNum = i * diff - firstI;
						if (!found[borderNum]) {
							drawHashForFrame((int) frameNum, paintContext, minWidth, maxWidthRule, width);
							found[borderNum] = true;
						}
						i++;
					}
				} else {
					// want to draw 5 rules on a 2 width box.
					// So draw all the fives that aren't divisible 
					// by two
					i = (int) ((firstVisibleFrame + ChronicleRuler.widths[width] - 1) / ChronicleRuler.widths[width]);
					end = (int) (lastVisibleFrame / ChronicleRuler.widths[width]);
					while (i <= end) {
						long frameNum = i*ChronicleRuler.widths[width];
						int fiveMultiple = (int) (frameNum / ChronicleRuler.widths[width-2]);
						// widths[width] == 5 * widths[width-2]
						if ((fiveMultiple & 1) == 1) {
							drawHashForFrame((int) frameNum, paintContext, minWidth, maxWidthRule, width);
						}
						i++;
					}
				}
			}
			boolean startsWithTwo = (minFrameLabellingDistance % 3) == 1;
			long fLabelDist = ChronicleRuler.widths[minFrameLabellingDistance];
			if (startsWithTwo) {
				Set already = paintFrameLabelsAtDistanceExcept(paintContext, fLabelDist, null);
				fLabelDist = ChronicleRuler.widths[minFrameLabellingDistance+1];
				paintFrameLabelsAtDistanceExcept(paintContext, fLabelDist, already);
			} else {
				paintFrameLabelsAtDistanceExcept(paintContext, fLabelDist, null);
			}
		}
	}

	/**
	 * Paints all the visible frames numbers that are multiples of 
	 * fLabelDist, except for those already listed in the exception set.
	 * @param paintContext
	 * @param fLabelDist
	 * @param except the frames not to draw. <code>null</code> if
	 * all will be drawn. Edited in place if not <code>null</code>.
	 * @return the exception set, modified in place to include
	 * the drawn frame numbers (as <code>Long</code>s)
	 */
	private Set paintFrameLabelsAtDistanceExcept(PPaintContext paintContext, long fLabelDist, Set except) {
		if (except == null) {
			except = new HashSet();
		}
		long frameToLabel = fLabelDist * (firstVisibleFrame / fLabelDist);
		if (frameToLabel <= 0) {
			frameToLabel = fLabelDist;
		}
		long widthInFrames = 2*fLabelDist + lastVisibleFrame - firstVisibleFrame;
		long lastFrameToLabel = frameToLabel + fLabelDist * (widthInFrames / fLabelDist);
		if (frameToLabel < lastFrameToLabel && firstVisibleFrame < lastVisibleFrame) {
			while (frameToLabel <= lastFrameToLabel) {
				if (except.add(new Long(frameToLabel))) {
					drawLabelForFrame(frameToLabel, paintContext, fLabelDist);
				}
				frameToLabel += fLabelDist;
			}
		}
		return except;
	}

	/**
	 * Get the minimum distance to place between tick marks.
	 * @return int
	 */
	public int getMinDistance() {
		return minDistance;
	}

	/**
	 * Sets the minimum distance to put between tick marks on
	 * the ruler.
	 * @param minDistance The minDistance to set
	 */
	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}
	public int getRulerHeight() {
		return rulerHeight;
	}
	public void setRulerHeight(int rulerHeight) {
		this.rulerHeight = rulerHeight;
		invalidatePaint();
	}

	public Paint getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Paint backgroundColor) {
		if (!ObjectUtils.equals(this.backgroundColor, backgroundColor)) {
			this.backgroundColor = backgroundColor;
			invalidatePaint();
		}
	}

	public Color[] getLineColorTable() {
		return lineColorTable;
	}

	public void setLineColorTable(Color[] lineColorTable) {
		this.lineColorTable = lineColorTable;
		invalidatePaint();
	}
}