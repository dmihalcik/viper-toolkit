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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import lizard.tiff.Tiff;
import viper.api.time.Frame;
import viper.api.time.FrameRate;
import viper.api.time.Instant;
import viper.api.time.RationalFrameRate;
import viper.api.time.Span;

class InfoPlayer extends DataPlayerHelper {
	public static int JPG_TYPE = 0, LIST_TYPE = 1, MPEG_TYPE = 2,
			TIFF_TYPE = 3, OTHER_TYPE = 4;
	private List<String> files;
	private Span span = new Span(new Frame(0), new Frame(1));
	private FrameRate rate = new RationalFrameRate(1);
	private Frame now;
	private File relativeTo;

	private String nextFreeLine(BufferedReader r) throws IOException {
		String s = r.readLine();
		while (null != s && 0 == s.length()) {
			s = r.readLine();
		}
		return s;
	}

	public InfoPlayer(File dataFile) throws IOException {
		super(dataFile.getName());
		relativeTo = dataFile.getParentFile();

		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		try {
			int infoVersion = 2;
			files = new ArrayList<String>();
			String s = nextFreeLine(reader);
			if (null == s) {
				return;
			}
			// Check for the version string.
			if (s.startsWith("#VIPER_VERSION_")) {
				String svers = s.substring(15);
				String[] split = svers.split("\\D+");
				if (split.length > 0) {
					infoVersion = Integer.parseInt(split[0]);
				}
			}
			if (infoVersion < 3) {
				throw new UnsupportedOperationException(
						"Only supports version 3 and tries to parse greater of info file");
			} else if (infoVersion > 3) {
				logger.severe("Unknown version of .info file, " + infoVersion
						+ ", attempting to parse as version 3");
			}

			// Version 3.0 has the following file format:
			// <int> - Decoder type (ie 1 for LIST_TYPE, etc.
			// <int> <int> - min and max frame number
			s = nextFreeLine(reader);
			if (null == s) {
				return;
			}
			try {
				int inputtype = Integer.parseInt(s);
				initFrameList(inputtype, reader);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						".info file has unrecognized string");
			}
		} finally {
			reader.close();
		}
	}

	private void initFrameList(int type, BufferedReader reader) {
		try {
			String s;
			while (null != (s = nextFreeLine(reader))) {
				files.add(s);
			}
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "Error while reading list of files.", ioe);
		}

		span = new Span(new Frame(1), new Frame(files.size() + 1));
	}

	public Image helpGetImage(Frame f) throws IOException {
		String fullPath = files.get(f.getFrame() - 1);
		File imFile = new File(relativeTo, fullPath);
		String normalizedPath = fullPath.toLowerCase();
		if (normalizedPath.endsWith(".bmp") && QuicktimePlayer.TEST()) {
			QuicktimePlayer qt = new QuicktimePlayer(imFile);
			return qt.getImage();
		} else if (normalizedPath.endsWith(".jpg")
				|| normalizedPath.endsWith(".jpeg")) {
			return new ImageIcon(imFile.toURI().toURL()).getImage();
		} else if (normalizedPath.endsWith(".tif")
				|| normalizedPath.endsWith(".tiff")) {
			FileInputStream fis = new FileInputStream(imFile);
			try {
				Tiff tif = new Tiff();
				tif.readInputStream(fis);
				return tif.getImage(0);
			} finally {
				fis.close();
			}
		}
		return ImageIO.read(imFile);
	}

	public Span getSpan() {
		return span;
	}

	public Instant getNow() {
		return now;
	}

	public void setNow(Instant i) {
		Frame newNow = rate.asFrame(i);
		if (span.contains(newNow)) {
			now = newNow;
		} else {
			throw new NoSuchElementException("Frame " + newNow
					+ " not in range " + span);
		}
	}

	public FrameRate getRate() {
		return rate;
	}
}
