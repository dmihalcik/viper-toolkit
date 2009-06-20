package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.util.*;

import viper.api.time.*;
import viper.api.time.Frame;

/**
 * To handle other files supported by JMF.
 * @author davidm
 */
class JmfPlayer extends DataPlayerHelper implements ControllerListener {
	private Processor jmfPlayer;
	private FramePositioningControl jmfController;

	//	Objects to deal with state-transition blocking for the jmfplayer.
	private Object waitSync = new Object();
	private boolean stateTransitionOK = true;

	private Instant now;
	private FrameRate rate;
	private Span span;

	public JmfPlayer(File dataFile) throws IOException {
		super(dataFile.getName());
		try {
			jmfPlayer =
				Manager.createProcessor(new MediaLocator(dataFile.toURL()));
			jmfPlayer.addControllerListener(this);
		} catch (NoProcessorException npx) {
			logger.severe ("Failed to open file " + dataFile.getName());
			throw new IOException("No media processor for file: " + dataFile);
		}

		jmfPlayer.prefetch();
		if (!waitForState(Controller.Prefetched)) {
			logger.severe ("Failed to prefetch the processor.");
			throw new IOException(
				"Could not prefetch media processor for file: " + dataFile);
		}

		jmfController =
			(FramePositioningControl) jmfPlayer.getControl(
				"javax.media.control.FramePositioningControl");
		span =
			new Span(
				new viper.api.time.Time(0),
				new viper.api.time.Time(
					jmfPlayer.getDuration().getNanoseconds()));
		rate = new MediaFrameRate(jmfController);
		now = new viper.api.time.Time(0);
	}

	/**
	 * Block until the processor has transitioned to the given state.
	 * Return false if the transition failed.
	 * @param state the processor state to wait for
	 * @return if the media player transitioned to the state correctly
	 */
	private boolean waitForState(int state) {
		synchronized (waitSync) {
			while (jmfPlayer.getState() != state && stateTransitionOK) {
				try {
					waitSync.wait();
				} catch (InterruptedException e) {
					logger.fine("Interrupted JMF decoder while waiting");
				}
			}
		}
		return stateTransitionOK;
	}
	public void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof ConfigureCompleteEvent
			|| evt instanceof RealizeCompleteEvent
			|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof EndOfMediaEvent) {
		} else if (evt instanceof StopByRequestEvent) {

		}
	}

	protected Image helpGetImage(Frame f) throws IOException {
		FrameGrabbingControl fgc =
			(FrameGrabbingControl) jmfPlayer.getControl(
				"javax.media.control.FrameGrabbingControl");
		if (fgc == null) {
			logger.warning("Cannot grab frames for this video, for some reason");
			return null;
		}
		jmfController.seek(f.getFrame());
		Buffer buffer = fgc.grabFrame();
		BufferToImage bti = new BufferToImage((VideoFormat) buffer.getFormat());
		return bti.createImage(buffer);
	}

	public Span getSpan() {
		return span;
	}
	public Instant getNow() {
		return now;
	}
	public void setNow(Instant i) {
		i = rate.asTime(i);
		if (span.contains(i)) {
			now = i;
		} else {
			throw new NoSuchElementException(
				"Time out of media duration: " + i);
		}
	}
	public FrameRate getRate() {
		return rate;
	}
}
