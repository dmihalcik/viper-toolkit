/*
 * Created on Feb 13, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.image.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;

public class ByteBufferAsIntBufferSource implements ImageProducer {
	private static Logger log = Logger.getLogger("edu.umd.cfar.lamp.viper.nmpeg");
	int width;
	int height;
	ColorModel model;
	ByteBuffer pixels;
	Hashtable properties;
	Vector theConsumers = new Vector();
	boolean animating;
	boolean fullbuffers;

	int[] target;
	
	public ByteBufferAsIntBufferSource(
		int w,
		int h,
		ColorModel cm,
		ByteBuffer pix) {
		width = w;
		height = h;
		model = cm;
		pixels = pix;
		properties = new Hashtable();
		target = new int[width * height];
	}

	/**
	 * Adds an ImageConsumer to the list of consumers interested in
	 * data for this image.
	 * @param ic the specified <code>ImageConsumer</code>
	 * @see ImageConsumer
	 */
	public synchronized void addConsumer(ImageConsumer ic) {
		if (theConsumers.contains(ic)) {
			return;
		}
		theConsumers.addElement(ic);
		try {
			initConsumer(ic);
			sendPixelsWithConstantTarget(ic, 0, 0, width, height);
			if (isConsumer(ic)) {
				ic.imageComplete(
					animating
						? ImageConsumer.SINGLEFRAMEDONE
						: ImageConsumer.STATICIMAGEDONE);
				if (!animating && isConsumer(ic)) {
					ic.imageComplete(ImageConsumer.IMAGEERROR);
					removeConsumer(ic);
				}
			}
		} catch (Exception e) {
			if (isConsumer(ic)) {
				log.log(Level.SEVERE, "Image Load Error", e);
				ic.imageComplete(ImageConsumer.IMAGEERROR);
			}
		}
	}

	public synchronized boolean isConsumer(ImageConsumer ic) {
		return theConsumers.contains(ic);
	}

	public synchronized void removeConsumer(ImageConsumer ic) {
		theConsumers.removeElement(ic);
	}

	public void startProduction(ImageConsumer ic) {
		addConsumer(ic);
	}

	public void requestTopDownLeftRightResend(ImageConsumer ic) {
		// Ignored.  The data is either single frame and already in TDLR
		// format or it is multi-frame and TDLR resends aren't critical.
	}

	public synchronized void setAnimated(boolean animated) {
		this.animating = animated;
		if (!animating) {
			Iterator iter = theConsumers.iterator();
			while (iter.hasNext()) {
				ImageConsumer ic = (ImageConsumer) iter.next();
				ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
				if (isConsumer(ic)) {
					ic.imageComplete(ImageConsumer.IMAGEERROR);
				}
			}
			theConsumers.removeAllElements();
		}
	}

	public synchronized void setFullBufferUpdates(boolean fullbuffers) {
		if (this.fullbuffers == fullbuffers) {
			return;
		}
		this.fullbuffers = fullbuffers;
		if (animating) {
			Iterator iter = theConsumers.iterator();
			while (iter.hasNext()) {
				ImageConsumer ic = (ImageConsumer) iter.next();
				ic.setHints(
					fullbuffers
						? (ImageConsumer.TOPDOWNLEFTRIGHT
							| ImageConsumer.COMPLETESCANLINES)
						: ImageConsumer.RANDOMPIXELORDER);
			}
		}
	}

	public void newPixels() {
		newPixels(0, 0, width, height, true);
	}

	public synchronized void newPixels(int x, int y, int w, int h) {
		newPixels(x, y, w, h, true);
	}

	/**
	 * Sends a rectangular region of the buffer of pixels to any
	 * ImageConsumers that are currently interested in the data for
	 * this image.
	 * If the framenotify parameter is true then the consumers are
	 * also notified that an animation frame is complete.
	 * This method only has effect if the animation flag has been
	 * turned on through the setAnimated() method.
	 * If the full buffer update flag was turned on with the
	 * setFullBufferUpdates() method then the rectangle parameters
	 * will be ignored and the entire buffer will always be sent.
	 * @param x the x coordinate of the upper left corner of the rectangle
	 * of pixels to be sent
	 * @param y the y coordinate of the upper left corner of the rectangle
	 * of pixels to be sent
	 * @param w the width of the rectangle of pixels to be sent
	 * @param h the height of the rectangle of pixels to be sent
	 * @param framenotify <code>true</code> if the consumers should be sent a
	 * {@link ImageConsumer#SINGLEFRAMEDONE SINGLEFRAMEDONE} notification
	 * @see ImageConsumer
	 * @see #setAnimated
	 * @see #setFullBufferUpdates
	 */
	public synchronized void newPixels(
		int x,
		int y,
		int w,
		int h,
		boolean framenotify) {
		if (animating) {
			if (fullbuffers) {
				x = y = 0;
				w = width;
				h = height;
			} else {
				if (x < 0) {
					w += x;
					x = 0;
				}
				if (x + w > width) {
					w = width - x;
				}
				if (y < 0) {
					h += y;
					y = 0;
				}
				if (y + h > height) {
					h = height - y;
				}
			}
			if ((w <= 0 || h <= 0) && !framenotify) {
				return;
			}
			Iterator iter = theConsumers.iterator();
			while (iter.hasNext()) {
				ImageConsumer ic = (ImageConsumer) iter.next();
				if (w > 0 && h > 0) {
					sendPixels(ic, x, y, w, h);
				}
				if (framenotify && isConsumer(ic)) {
					ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
				}
			}
		}
	}

	/**
	 * Changes to a new byte array to hold the pixels for this image.
	 * If the animation flag has been turned on through the setAnimated()
	 * method, then the new pixels will be immediately delivered to any
	 * ImageConsumers that are currently interested in the data for
	 * this image.
	 * @param newpix the new pixel array
	 * @param newmodel the specified <code>ColorModel</code>
	 * @see #newPixels(int, int, int, int, boolean)
	 * @see #setAnimated
	 */
	public synchronized void newPixels(
		ByteBuffer newpix,
		ColorModel newmodel) {
		this.pixels = newpix;
		this.model = newmodel;
		newPixels();
	}

	private void initConsumer(ImageConsumer ic) {
		if (isConsumer(ic)) {
			ic.setDimensions(width, height);
		}
		if (isConsumer(ic)) {
			ic.setProperties(properties);
		}
		if (isConsumer(ic)) {
			ic.setColorModel(model);
		}
		if (isConsumer(ic)) {
			ic.setHints(
				animating
					? (fullbuffers
						? (ImageConsumer.TOPDOWNLEFTRIGHT
							| ImageConsumer.COMPLETESCANLINES)
						: ImageConsumer.RANDOMPIXELORDER)
					: (ImageConsumer.TOPDOWNLEFTRIGHT
						| ImageConsumer.COMPLETESCANLINES
						| ImageConsumer.SINGLEPASS
						| ImageConsumer.SINGLEFRAME));
		}
	}

	private void sendPixelsWithConstantTarget(ImageConsumer ic, int x, int y, int w, int h) {
		if (!isConsumer(ic)) {
			return;
		}
		IntBuffer ib = pixels.asIntBuffer();

		int pixelscan = -w;
		int off = target.length + pixelscan;
		if (ib.limit() == target.length) {
			ib.get(target);
		} else {
			int boffset = 0;
			int r, g, b;
			for (int poffset = 0; poffset < target.length; poffset++) {
				r = pixels.get(boffset+0);
				g = pixels.get(boffset+1);
				b = pixels.get(boffset+2);
				target[poffset] = 0xff000000 | (r<<16) | (g<<8) | (b<<0);
				boffset += 3;
			}
		}
		//ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT);
		//callSetPixels(ic, x, y, w, h, off, pixelscan);
		ic.setPixels(x, y, w, h, model, target, off, pixelscan);
		ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
	}
	
	private void callSetPixels(ImageConsumer ic, int x, int y, int w, int h, int off, int pixelscan){
		ic.setPixels(x, y, w, h, model, target, off, pixelscan);
	}
	
	private void sendPixels(ImageConsumer ic, int x, int y, int w, int h) {
		if (!isConsumer(ic)) {
			return;
		}
		IntBuffer ib = pixels.asIntBuffer();
		int[] target = new int[w * h];

		int pixelscan = -w;
		int off = target.length + pixelscan;
		if (ib.limit() == target.length) {
			ib.get(target);
		} else {
			int boffset = 0;
			int r, g, b;
			for (int poffset = 0; poffset < target.length; poffset++) {
				r = pixels.get(boffset+0);
				g = pixels.get(boffset+1);
				b = pixels.get(boffset+2);
				target[poffset] = 0xff000000 | (r<<16) | (g<<8) | (b<<0);
				boffset += 3;
			}
		}
		ic.setPixels(x, y, w, h, model, target, off, pixelscan);
	}
}