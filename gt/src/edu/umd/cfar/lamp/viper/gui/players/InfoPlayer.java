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
import java.util.List;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;

import lizard.tiff.*;
import viper.api.time.*;
import viper.api.time.Frame;


class InfoPlayer extends DataPlayerHelper {
	public static int JPG_TYPE = 0,
		LIST_TYPE = 1,
		MPEG_TYPE = 2,
		TIFF_TYPE = 3,
		OTHER_TYPE = 4;
	private List files;
	private Span span;
	private FrameRate rate = new RationalFrameRate(1);
	private Frame now;
	private File relativeTo;
	
	public InfoPlayer(File dataFile) throws IOException {
		super(dataFile.getName());
		FileReader reader = new FileReader(dataFile);
		relativeTo = dataFile.getParentFile();
		try {
			StreamTokenizer input = new StreamTokenizer(reader);
			int infoVersion = 2;

			input.wordChars('!', ')');
			input.wordChars('_', '_');
			input.nextToken();

			if (input.ttype == StreamTokenizer.TT_WORD) {
				// Check for the version string.
				if (input.sval.startsWith("#VIPER_VERSION_")) {
					String svers = input.sval.substring(15);
					String[] split = svers.split("\\D+");
					if (split.length > 0) {
						infoVersion = Integer.parseInt(split[0]);
					}
				}
			}
			if (infoVersion < 3) {
				throw new UnsupportedOperationException("Only supports version 3 and tries to parse greater of info file");
			} else if (infoVersion > 3) {
				logger.severe(
					"Unknown version of .info file, "
						+ infoVersion
						+ ", attempting to parse as version 3");
			}

			//	Version 3.0 has the following file format:
			//	 <int>				- Decoder type (ie 1 for LIST_TYPE, etc.
			//  <int> <int>		- min and max frame number
			input.nextToken();
			if (input.ttype == StreamTokenizer.TT_NUMBER) {
				int inputtype = (int) input.nval;
				initFrameList(inputtype, input);
			} else {
				throw new IllegalArgumentException(".info file has unrecognized string");
			}
		} finally {
			reader.close();
		}
	}

	private void initFrameList(int type, StreamTokenizer input) {
		boolean done = false;
		files = new Vector();

		input.resetSyntax();
		input.wordChars('/', '/');
		input.wordChars('0', '0');
		input.wordChars('1', '9');
		input.wordChars('A', 'Z');
		input.wordChars('a', 'z');
		input.wordChars('\u00A0', '\u00FF');
		input.wordChars('.', '.');
		input.wordChars('_', '_');
		input.wordChars('-', '-');
		input.whitespaceChars('\u0000', '\u0020');
		try {
			while (done == false) {

				input.nextToken(); // Take care of the eoln
				if (input.ttype == StreamTokenizer.TT_WORD) {
					files.add(input.sval);
				} else if (input.ttype == StreamTokenizer.TT_NUMBER) {
					logger.finest ("temp: ");
				} else if (input.ttype == StreamTokenizer.TT_EOF) {
					done = true;
				}
			}
		} catch (IOException ioe) {
			logger.severe("Error while reading list of files: " + ioe.getLocalizedMessage());
		}

		span = new Span(new Frame(1), new Frame(files.size() + 1));
	}

	public Image helpGetImage(Frame f) throws IOException {
		String fullPath = (String) files.get(f.getFrame()-1);
		File imFile = new File(relativeTo, fullPath);
		String normalizedPath = fullPath.toLowerCase();
		if (normalizedPath.endsWith(".bmp") && QuicktimePlayer.TEST()) {
			QuicktimePlayer qt = new QuicktimePlayer(imFile);
			return qt.getImage();
		} else if (normalizedPath.endsWith(".jpg")
			|| normalizedPath.endsWith(".jpeg")) {
			return new ImageIcon(imFile.toURL()).getImage();
		} else if (
			normalizedPath.endsWith(".tif")
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
		FileImageInputStream imFileStream = new FileImageInputStream(imFile);
		try {
			return ImageIO.read(imFileStream);
		} finally {
			imFileStream.close();
		}
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
			throw new NoSuchElementException(
				"Frame " + newNow + " not in range " + span);
		}
	}
	public FrameRate getRate() {
		return rate;
	}
}
