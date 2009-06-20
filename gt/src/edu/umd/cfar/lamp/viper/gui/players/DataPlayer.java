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
import java.awt.image.*;
import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.imageio.*;
import javax.swing.*;

import viper.api.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Heavily modified version of viper.gui.DataPlayer to be more oop and so forth.
 * /Volumes/hfs/csi-lobby-test-08.dv /Volumes/hfs/csi-lobby-test-09.dv /Volumes/hfs/magified
 * @author Felix Sukhenko
 * @author davidm
 * @version 0.1.0 03/07/03
 */
public abstract class DataPlayer implements ListIterator {
	public static final String UNKNOWN_FRAME = "Unknown Frame Type";

	public static final String I_FRAME = "I-Frame";

	public static final String P_FRAME = "P-Frame";

	public static final String B_FRAME = "B-Frame";

	public static final String EMPTY_FRAME = "Empty Frame";

	public static final Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.core.player");

	private static Map DECODERS = new HashMap();

	private static Class[] MPEG_LOAD = new Class[] { 
		NativePlayer.class, MpegPlayer.class, QuicktimePlayer.class, JmfPlayer.class,
			NotFoundPlayer.class };

	private static Class[] INFO_LOAD = new Class[] { InfoPlayer.class,
			NotFoundPlayer.class };

	private static Class[] AVI_LOAD = new Class[] { JmfPlayer.class,
			QuicktimePlayer.class, NotFoundPlayer.class };

	private static Class[] MOV_LOAD = new Class[] { QuicktimePlayer.class,
			JmfPlayer.class, NotFoundPlayer.class };
	
	private static Class[] ELSE_LOAD = new Class[] { StaticImagePlayer.class,
			NativePlayer.class, MpegPlayer.class, QuicktimePlayer.class,
			JmfPlayer.class, NotFoundPlayer.class };
	static {
		loadDefaultDecoderOrderings(DECODERS);
	}

	private static final String[] MPEG_FILE_EXTS = new String[] {"mpeg,mpg,mpv"};
	private static final String[] WINDOWS_FILE_EXTS = new String[] {"avi,wmf"};
	private static final String[] QUICKTIME_FILE_EXTS = new String[] {"mp4,mov,3g,dv"};
	private static final String[] INFO_FILE_EXTS = new String[] {"info"};
	private static final String[] STATIC_FILE_EXTS = new String[] {"png,jpeg,jpg,gif,bmp,tiff,tif,pbm"};
	
	
	public static final ExtensionFilter MPEG_FILE_FILTER = new ExtensionFilter(Arrays.asList(MPEG_FILE_EXTS), "MPEG 1/2 Movies (mpg;mpeg;mpv)");
	public static final ExtensionFilter WINDOWS_FILE_FILTER = new ExtensionFilter(Arrays.asList(WINDOWS_FILE_EXTS), "Windows Media (avi;wmf)");
	public static final ExtensionFilter QUICKTIME_FILE_FILTER = new ExtensionFilter(Arrays.asList(QUICKTIME_FILE_EXTS), "Quicktime Movies (mov;dv;3g)");
	public static final ExtensionFilter INFO_FILE_FILTER = new ExtensionFilter(Arrays.asList(INFO_FILE_EXTS), "Image Collections (info)");
	public static final ExtensionFilter STATIC_FILE_FILTER = new ExtensionFilter(Arrays.asList(STATIC_FILE_EXTS), "Static Images (jpeg;png;gif)");
	
	private static void loadDefaultDecoderOrderings(Map m) {
		m.put("mpeg", MPEG_LOAD);
		m.put("mpg", MPEG_LOAD);
		m.put("info", INFO_LOAD);
		m.put("mov", MOV_LOAD);
		m.put("dv", MOV_LOAD);
		m.put("avi", AVI_LOAD);
	}

	protected DataPlayer() {
	}

	/**
	 * Factory method for data players.
	 * @param canonicalUri
	 * 			The uri the data player represents. Essentially a unique id for the file, 
	 *           it really uses the File object to load the video.
	 * @param dataFile
	 *            the media file location on disk (needed)
	 * @param mediator
	 *            the mediator referring to the media file
	 * @return the data player
	 */
	public static DataPlayer createDataPlayer(URI canonicalUri, File dataFile, PrefsManager prefs) {
		// First, determine the data type of 'dataFile'
		DataPlayer toReturn = null;
		if (dataFile != null) {
			String fname = dataFile.getName();
			String normalizedFname = fname.toLowerCase();
			int i = normalizedFname.lastIndexOf(".") + 1;
			String extension = "";
			if (0 < i) {
				extension = normalizedFname.substring(i);
			}
			toReturn = getPlayerByExtension(extension, dataFile, prefs);
		} else {
			toReturn = new NotFoundPlayer();
		}
		toReturn.setURI(canonicalUri);
		assert canonicalUri != null;
		return toReturn;
	}

	private static final DataPlayer tryToUsePlayer(Class type, File dataFile) {
		try {
			if (InfoPlayer.class.equals(type)) {
				return new InfoPlayer(dataFile);
			} else if (StaticImagePlayer.class.equals(type)) {
				return new StaticImagePlayer(dataFile);
			} else if (NativePlayer.class.equals(type)) {
				return new NativePlayer(dataFile.getAbsolutePath());
			} else if (MpegPlayer.class.equals(type)) {
				return new MpegPlayer(dataFile);
			} else if (QuicktimePlayer.class.equals(type)) {
				return new QuicktimePlayer(dataFile);
			} else if (JmfPlayer.class.equals(type)) {
				return new JmfPlayer(dataFile);
			} else { // if (NotFoundPlayer.class};
				return new NotFoundPlayer();
			}
		} catch (UnsatisfiedLinkError ule) {
			logger.log(Level.WARNING, "Cannot load decoder: " + type, ule);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception while trying decoder: " + type
					+ "\n\t on file: " + dataFile, e);
		}
		return null;
	}

	private static final DataPlayer getPlayerByExtension(String extension,
			File dataFile, PrefsManager prefs) {
		DataPlayer dh = null;
		Class[] order = ELSE_LOAD;
		if (DECODERS.containsKey(extension)) {
			order = (Class[]) DECODERS.get(extension);
		}
		for (int i = 0; i < order.length; i++) {
			dh = tryToUsePlayer(order[i], dataFile);
			if (dh != null) {
				try {
					dh.setPrefs(prefs);
					logger.warning("Success opening " + dataFile + " with "
							+ order[i].getName());
					break;
				} catch (RuntimeException rx) {

				}
			}
		}
		return dh;
	}

	/**
	 * Function returns the image with the current index.
	 * 
	 * @return current Image.
	 */
	abstract public Image getImage();

	/**
	 * Get the image at the given instant.
	 * 
	 * @param i
	 * @return
	 * @throws UnknownFrameRateException
	 */
	abstract public Image getImage(Instant i);

	/**
	 * Gets the interval of the video clip
	 * 
	 * @return
	 */
	abstract public Span getSpan();

	/**
	 * Get the currently loaded instant.
	 * 
	 * @return
	 */
	abstract public Instant getNow();

	/**
	 * Set the currently loaded instant/frame.
	 * 
	 * @param i
	 * @throws UnknownFrameRateException
	 */
	abstract public void setNow(Instant i);

	/**
	 * Sets the media element that this describes. This is useful for data types
	 * that don't have accurate frame rates or spans.
	 * 
	 * @param element
	 *            the new metadata for the file.
	 */
	abstract public void setElement(MediaElement element);

	/**
	 * Gets the frame rate of the video.
	 * 
	 * @return <code>null</code> if the frame rate is unknown
	 */
	abstract public FrameRate getRate();

	/**
	 * Gets the pixel aspect ratio.
	 * 
	 * @return the aspect ratio of a single pixel (of the current frame,
	 *         although I doubt it will change over the course of a video
	 */
	abstract public Rational getPixelAspectRatio();

	/**
	 * Gets the type of image at location i.
	 * 
	 * @param i
	 *            the instant to check
	 * @return One of the valid image types, e.g. I_FRAME, B_FRAME, etc.
	 */
	abstract public String getImageType(Instant i);

	/**
	 * Does any cleanup required.
	 */
	abstract public void destroy();

	/**
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

	public abstract PrefsManager getPrefs();

	public abstract void setPrefs(PrefsManager prefs);

	public abstract void setMediator(ViperViewMediator mediator);

	public static class ExtractFrames {
		private static void printUsage() {
			System.err
					.println("DataPlayer.ExtractFrames (options) <inputFile> <outputDir>");
			System.err.println(" --span=ss:xx");
			// System.out.println(" --prefix=<output prefix>");
			// System.out.println(" --decoder=<java class name of concrete class
			// that extends DataPlayer>");
			// System.out.println(" ");
		}

		public static void main(String[] args) {
			// eg /Users/davidmihalcik/Movies/single-person-lab-test.dv
			// /Users/davidmihalcik/Movies/magified
			PrefsManager pref = new PrefsManager();
			if (args.length < 2) {
				printUsage();
				return;
			}
			boolean argErr = false;
			Span cropSpan = new Span(Frame.ALPHA, Frame.OMEGA);
			int fileStartIndex = args.length - 2;
			int dirIndex = args.length - 1;
			for (int i = 0; i < args.length - 2; i++) {
				if (args[i].startsWith("--span=")) {
					cropSpan = Span.parseFrameSpan(args[i].substring("--span="
							.length()));
				} else if ("--".equals(args[i])) {
					fileStartIndex = i + 1;
					break;
				} else if (!args[i].startsWith("--")) {
					fileStartIndex = i;
					break;
				} else {
					System.err.println("Unrecognized option: " + args[i]);
					argErr = true;
				}
			}
			if (argErr) {
				printUsage();
				return;
			}
			File mainOutputDirectory = new File(args[dirIndex]);
			if (!mainOutputDirectory.exists()) {
				mainOutputDirectory.mkdirs();
			}
			File[] Fi = new File[dirIndex - fileStartIndex];
			for (int i = fileStartIndex; i < dirIndex; i++) {
				Fi[i] = new File(args[i]);
			}

			boolean framesFound;
			try {
				for (int k = 0; k < Fi.length; k++) {
					File outputDir = new File(mainOutputDirectory, Fi[k].getName());
					if (!outputDir.exists()) {
						outputDir.mkdirs();
					}
					do {
						// Some dataPlayers have memory leaks that prevent them
						// from
						// decoding more than some number of frames. Here we set
						// the number of frames to use with one decoder before
						// loading a new one. (Pretty much only necessary for
						// quicktime for java on a mac, I hope2.)
						int HACK_LIMIT = 40;
						framesFound = false;
						DataPlayer d = DataPlayer.createDataPlayer(Fi[k].toURI(), Fi[k], pref);
						InstantInterval i = d.getRate().asFrame(d.getSpan());
						if (i.isEmpty()) {
							System.err.println("No frames found in file");
							return;
						}
						Instant si = i.getStart()
								.compareTo(cropSpan.getStart()) > 0 ? i
								.getStartInstant() : cropSpan.getStartInstant();
						Instant ei = i.getEnd().compareTo(cropSpan.getEnd()) < 0 ? i
								.getEndInstant()
								: cropSpan.getEndInstant();
						if (ei.compareTo(si) <= 0) {
							System.err
									.println("No frames found in file corresponding to the given crop span");
							return;
						}
						i = new Span(si, ei);
						NumberFormat format = new DecimalFormat("00000");
						long ERROR_SIZE = 20000;
						// Iterator iter =
						// ImageIO.getImageWritersBySuffix("jpeg");
						// ImageWriter imWriter= (ImageWriter) iter.next();
						int hack = 0;
						for (Instant index = i.getStartInstant(); hack < HACK_LIMIT
								&& i.contains(index)
								&& index.longValue() < ERROR_SIZE; index = (Instant) index
								.next()) {
							File outFile = new File(outputDir, "camera2-"
									+ format.format(index.longValue()) + ".png");
							if (outFile.exists()) {
								continue;
							}
							Image temp = d.getImage(index);
							BufferedImage im;
							if (temp instanceof BufferedImage) {
								im = (BufferedImage) temp;
							} else {
								im = toBufferedImage(temp);
							}
							try {
								ImageIO.write(im, "png", outFile);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							framesFound = true;
							hack++;
							Thread.yield();
						}
					} while (framesFound);
				}
			} finally {
				System.exit(0);
			}
		}

		// This method returns true if the specified image has transparent
		// pixels
		public static boolean hasAlpha(Image image) {
			// If buffered image, the color model is readily available
			if (image instanceof BufferedImage) {
				BufferedImage bimage = (BufferedImage) image;
				return bimage.getColorModel().hasAlpha();
			}

			// Use a pixel grabber to retrieve the image's color model;
			// grabbing a single pixel is usually sufficient
			PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {
			}

			// Get the image's color model
			ColorModel cm = pg.getColorModel();
			return cm.hasAlpha();
		}

	}
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent
		// Pixels
		boolean hasAlpha = false;// hasAlpha(image);

		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image
					.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image
					.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}
	
	
	private URI uri;
	
	public void setURI (URI uri) {
		this.uri = uri;
		assert uri != null;
	}

	public URI getURI() {
		return uri;
	}
}