package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.io.*;
import java.util.logging.*;

import quicktime.*;
import quicktime.app.time.*;
import quicktime.app.view.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.clocks.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * To handle other files supported by Quicktime for Java.
 * @author davidm
 */
class QuicktimePlayer extends DataPlayerHelper  {
	private static boolean TESTED = false;
	private static boolean IS_USABLE = true;
	
	public static boolean TEST() {
		if (!TESTED) {
			TESTED = true;
			try {
				IS_USABLE = false;
				QTSession.open();
				IS_USABLE = true;
			} catch (QTException e) {
			} catch (UnsatisfiedLinkError ule) {
			}
		}
		return IS_USABLE;
	}
	private Dimension imageSize;
	private QTImageProducer qtProducer;
	private MoviePlayer mp;
	private Region all;

	private Instant now;
	private FrameRate rate;
	private FrameRate qtRate;
	private Span span;

	public QuicktimePlayer(File dataFile) throws IOException {
		super(dataFile.getName());
		if (!IS_USABLE) {
			throw new IOException("Quicktime for Java is either missing or broken");
		}
		TESTED = true;
		try {
			QTSession.open();
		} catch (QTException e1) {
			IS_USABLE = false;
			logger.severe(e1.getLocalizedMessage());
			throw new IOException("Cannot start quicktime process");
		} catch (UnsatisfiedLinkError ule) {
			IS_USABLE = false;
			logger.severe(ule.getLocalizedMessage());
			throw new IOException("Quicktime for Java is not installed.");
		}
		QTFile qfile = new QTFile(dataFile);
		Movie m;
		TimeInfo ti;
		try {
			m = Movie.fromFile(OpenMovieFile.asRead(qfile));
			m.setPlayHints(StdQTConstants.hintsScrubMode, 0);
			int timeNow = m.getTime();
			float playRate = m.getPreferredRate();
			m.prePreroll(timeNow, playRate);
			m.preroll(timeNow, playRate);

			TaskAllMovies.addMovieAndStart();
			m.setActive (true);  
			// a total all out assault on trying to extract the frame rate
			Track visualTrack = m.getIndTrackType(1, StdQTConstants.videoMediaType, StdQTConstants.movieTrackCharacteristic);
			if (visualTrack == null) {
				visualTrack = m.getIndTrackType(1, StdQTConstants.visualMediaCharacteristic, StdQTConstants.movieTrackCharacteristic);
			}
			double framesPerSecond = 29.997;
			int ticksPerSecond = m.getTimeScale();
			int ticksPerFrame = 0;
			boolean foundRate = false;
			long bigNumber = 1000000;
			if (visualTrack != null) {
				Media vtMedia = visualTrack.getMedia();
				if (!(vtMedia instanceof VideoMedia)) {
					vtMedia = new VideoMedia(visualTrack, ticksPerSecond);
				}
				framesPerSecond = ((VideoMedia) vtMedia).getVideoHandler().getStatistics();
				foundRate = (framesPerSecond > 0 && framesPerSecond < bigNumber);
				if (!foundRate) {
					int mTime = m.getTime();
					ti = visualTrack.getNextInterestingTime(StdQTConstants.nextTimeMediaSample, mTime, 1);
					ticksPerFrame = ti.duration;
					foundRate = ticksPerFrame > 0 && ticksPerFrame < bigNumber;
					if (!foundRate) {
						framesPerSecond = ticksPerSecond % 30 == 0 ? 30 : 29.997;
					}
				}
			}
			mp = new MoviePlayer(m);
			TimeBase tb = mp.getTimeBase();
			
			if (ticksPerFrame <= 0) {
				ticksPerFrame = (int) ((double) ticksPerSecond / framesPerSecond);
			} else {
				framesPerSecond = (double) ticksPerSecond / ticksPerFrame;
			}
			Rational framesPerTick = new Rational(1, ticksPerFrame);
			Rational framesPerNano = new Rational((long) (framesPerSecond * bigNumber), bigNumber * bigNumber);
			qtRate = new RationalFrameRate(framesPerTick);
			rate = new RationalFrameRate(framesPerNano);

			Time start = new Time(tb.getStartTime());
			Time end = new Time(tb.getStopTime());
			this.span = new Span(start, end);
			
			// this is the size of the image - this will become the size of the frame
			QDRect r = m.getBox();
			all = new Region(r);
			imageSize = new Dimension(r.getWidth(), r.getHeight());
			qtProducer = new QTImageProducer(mp, imageSize);
		} catch (QTException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new IOException("Error while loading file");
		}
	}
	protected Image helpGetImage(Frame f) throws IOException {
		try {
			Time newTime = qtRate.asTime(f);
			mp.setTime(newTime.intValue());
			logger.warning("Setting frame to " + f + " at time " + newTime);
			qtProducer.redraw(all);
		} catch (StdQTException e) {
			logger.severe(e.getLocalizedMessage());
			throw new IOException ("Error while decoding frame " + f);
		} catch (QTException e) {
			logger.severe(e.getLocalizedMessage());
			throw new IOException ("Error while decoding frame " + f);
		}
		return Toolkit.getDefaultToolkit().createImage(qtProducer);
	}
	public Span getSpan() {
		return span;
	}
	public Instant getNow() {
		return now;
	}
	public void setNow(Instant i) {
		now = rate.asTime(i);
	}
	public FrameRate getRate() {
		return rate;
	}


	public void destroy() {
		super.destroy();
		QTSession.close();
	}
}
