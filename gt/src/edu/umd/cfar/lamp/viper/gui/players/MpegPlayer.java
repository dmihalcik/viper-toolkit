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
import java.util.*;
import java.util.logging.*;

import org.apache.xerces.utils.Base64;

import viper.api.time.*;
import viper.api.time.Frame;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.mpeg1.*;
import edu.umd.cfar.lamp.viper.geometry.*;

class MpegPlayer extends DataPlayerHelper {
	private Mpeg1VideoStream player;
	private Frame now;
	private Span span;
	private FrameRate rate;
	private Rational pixRatio;
	private File dataFile;
	
	private static String uri = "http://viper-toolkit.sourceforge.net/products/jmpeg#";
	private static Property hasSystemIndex =
		ResourceFactory.createProperty(uri + "hasSystemIndex");
	private static Property hasVideoIndex =
		ResourceFactory.createProperty(uri + "hasSystemIndex");
	
	public MpegPlayer(File dataFile) throws IOException {
		super(dataFile.getName());
		this.dataFile = dataFile;
		if (!dataFile.exists()) {
			throw new FileNotFoundException("Cannot find MPEG: " + dataFile);
		}
	}
	
	private static byte[] b642bytes(Literal l) {
		return Base64.decode(l.getLexicalForm().getBytes());
	}
	private static Literal bytes2b64(byte[] data, Model m) {
		data = Base64.encode(data);
		try {
			return m.createTypedLiteral(new String(data, "UTF-8"), XSD.base64Binary.getURI());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		if (player == null) {
			Model toAdd = new ModelMem();
			Model toRemove = new ModelMem();
			prefs.model.enterCriticalSection(ModelLock.READ);
			try {
				Mpeg1File mpeg = new Mpeg1File(dataFile);
				Resource fileR  = null;
				if (prefs != null) {
					fileR = prefs.model.getResource(dataFile.toURI().toString());
				}
				if (mpeg.isSystemFile() && prefs != null) {
					if (prefs.model.contains(fileR, hasSystemIndex)) {
						Literal l = prefs.model.getProperty(fileR, hasSystemIndex).getLiteral();
						byte[] data = b642bytes(l);
						InputStream is = new ByteArrayInputStream(data);
						mpeg.readSystemIndex(is);
					} else {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						mpeg.writeSystemIndex(os);
						byte[] data = os.toByteArray();
						Literal l = bytes2b64(data, toAdd);
						toAdd.add(fileR, hasSystemIndex, l);
					}
				}
				player = mpeg.getVideoStream();
				if (prefs != null) {
					if (prefs.model.contains(fileR, hasVideoIndex)) {
						Literal l = prefs.model.getProperty(fileR, hasVideoIndex).getLiteral();
						byte[] data = b642bytes(l);
						InputStream is = new ByteArrayInputStream(data);
						player.readIndex(is);
					} else {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						player.writeIndex(os);
						byte[] data = os.toByteArray();
						Literal l = bytes2b64(data, toAdd);
						toAdd.add(fileR, hasVideoIndex, l);
					}
				}
				span =
					new Span(
						new Frame(1),
						new Frame((int) player.getNumFrames() + 1));
				now = (Frame) span.getStart();
				rate = new RationalFrameRate(player.getFrameRate());
				pixRatio = player.getPixelAspectRatio().getRationalValue();
				logger.fine ("Created new mpeg player with rate " + getRate());
			} catch (UnsupportedStreamTypeException ustx) {
				throw new IOException(ustx.getMessage());
			} catch (MpegException mx) {
				throw new IOException(mx.getMessage());
			} finally {
				prefs.model.leaveCriticalSection();
			}
			if (prefs != null && (!toAdd.isEmpty() || !toRemove.isEmpty())) {
				prefs.changeUser(toRemove, toAdd);
			}
		}
	}
	
	private void makeSureIsInitialized() {
		try { 
			initialize();
		}catch (IOException iox) {
			throw new RuntimeException(iox);
		}
	}

	protected Image helpGetImage(Frame f) throws IOException, InterruptedException {
		initialize();
		getImageType(f);
		try {
			synchronized(player) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				player.seek(f.getFrame() - 1);
				return player.getImage();
			}
		} catch (MpegException mx) {
			throw new IOException(mx.getLocalizedMessage());
		}
	}
	public Span getSpan() {
		makeSureIsInitialized();
		return span;
	}
	public Instant getNow() {
		makeSureIsInitialized();
		return now;
	}
	public void setNow(Instant i) {
		makeSureIsInitialized();
		Frame newNow = rate.asFrame(i);
		if (span.contains(newNow)) {
			now = newNow;
		} else {
			throw new NoSuchElementException(
				"Frame " + newNow + " not in range " + span);
		}
	}
	public FrameRate getRate() {
		makeSureIsInitialized();
		return rate;
	}
	
	public void destroy() {
		super.destroy();
		// XXX close mpeg stream
	}

	public Rational getPixelAspectRatio() {
		makeSureIsInitialized();
		return pixRatio;
	}
	public void setPrefs(PrefsManager prefs) {
		super.setPrefs(prefs);
		if (player == null) {
			makeSureIsInitialized();
		}
	}
	/**
	 * 
	 * @param i instant to check
	 * @return {@link edu.umd.cfar.lamp.viper.gui.players.DataPlayer#I_FRAME i-frame}
	 */
	public String getImageType(Instant i) {
		Frame f = rate.asFrame(i);
		try {
			int type = player.getVideoDecoder().getPictureCodingType(f.intValue() - 1);
			switch(type) {
				case 1:
					return I_FRAME;
				case 2:
					return P_FRAME;
				case 3:
					return B_FRAME;
				default:
					return UNKNOWN_FRAME;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while determining image type for frame " + f, e);
		} catch (MpegException e) {
			logger.log(Level.SEVERE, "Error while determining image type for frame " + f, e);
		}
		return EMPTY_FRAME;
	}
}
