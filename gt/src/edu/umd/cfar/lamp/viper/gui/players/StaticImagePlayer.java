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

package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.io.*;

import javax.imageio.*;
import javax.imageio.stream.*;

import viper.api.time.*;
import viper.api.time.Frame;


class StaticImagePlayer extends DataPlayerHelper {
	private static Span span = new Span(new Frame(1),new Frame(2));
	private static FrameRate rate = new RationalFrameRate(1);
	private Image img;
	private Instant now;
	
	/**
	 * Tests to see if there is an image reader for 
	 * the given mime type.
	 * @param s the mime type
	 * @return if imageio believes a reader exists for the 
	 * mime type
	 */
	public static boolean isStaticImageMimeType(String s) {
		return ImageIO.getImageReadersByMIMEType(s).hasNext();
	}

	/**
	 * Uses the file suffix to check to see
	 * if an image reader exists for the file.
	 * @param f the file to check
	 * @return if a reader exists for files with the given
	 * suffix (part after the last .)
	 * XXX: should this check every set after a ., a la apache
	 * variants?
	 */
	public static boolean isStaticImageFile(File f) {
		String name = f.getName().toLowerCase();
		int i = name.lastIndexOf(".");
		if (i < 0) {
			return false;
		}
		String suffix = name.substring(i+1);
		return ImageIO.getImageReadersBySuffix(suffix).hasNext();
	}
	
	StaticImagePlayer(File dataFile) throws IOException {
		super(dataFile.getName());
		FileImageInputStream imFileStream = new FileImageInputStream(dataFile);
		img = ImageIO.read(imFileStream);
		if (img == null) {
			throw new IllegalArgumentException("Not a known file type: " + dataFile);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Image helpGetImage(Frame f) throws IOException {
		return img;
	}

	/**
	 * @inheritDoc
	 */
	public Span getSpan() {
		return span;
	}
	
	/**
	 * @inheritDoc
	 */
	public FrameRate getRate() {
		return rate;
	}

	/**
	 * @inheritDoc
	 */
	public Instant getNow() {
		return now;
	}

	/**
	 * @inheritDoc
	 */
	public void setNow(Instant i) {
		this.now = i;
	}
}
