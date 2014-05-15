package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import viper.api.time.Frame;
import EDU.oswego.cs.dl.util.concurrent.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Intermediate class that implements image caching and 
 * most of the generic methods. Subclasses must implement
 * the Instant tracking methods (getNow, setNow, getSpan,
 * getRate) and getImage. It also implements 
 * asynchronous decoding, if you wish.
 * 
 * @author davidm
 */
abstract class DataPlayerHelper extends DataPlayer {
	private final List lastFrameCache = Collections.synchronizedList(new ArrayList());
	private int cacheSize = 1;

	/** keeps to keep weak references to used images */
	private final Map weakCache = Collections.synchronizedMap(new WeakHashMap());

	protected PrefsManager prefs;
	/**
	 * @return Returns the prefs.
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}
	/**
	 * @param prefs The prefs to set.
	 */
	public void setPrefs(PrefsManager prefs) {
		this.prefs = prefs;
	}
	private boolean synchronous = true;

	/** keeps to keep references to used images */
	private final Map strongCache = Collections.synchronizedMap(new HashMap());

	/** 
	 * Notified when the image is asynchronously decoded.
	 */
	private ViperViewMediator mediator;

	/**
	 * The image that has been decoded that was
	 * most recently asked for/closest to the 
	 */
	private ImageHolder mostRecentImage = new ImageHolder();
	
	public Image getMostRecentImage() {
		return mostRecentImage.get();
	}
	public void setMostRecentImage(Image i) {
		mostRecentImage.set(i);
	}
	
	private static class ImageHolder {
        private Image value;
        synchronized Image get() { return value; }
        synchronized void set(Image i) { value = i; }
	}

	private final FrameWorkerQueue frameLoader;
	
	private static class FrameWorkerQueue implements Executor {
		protected final Slot nextFrameToDecode;
		protected final Thread bgThread;
		
		protected void finalize() throws Throwable {
			if (bgThread.isAlive()) {
				bgThread.interrupt();
				bgThread.join();
			}
			super.finalize();
		}

		public void execute(Runnable r) throws InterruptedException {
			try {
				nextFrameToDecode.put(r);
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		
		public FrameWorkerQueue(final String name) {
			nextFrameToDecode = new Slot();
			Runnable runLoop = new Runnable() {
				public void run() {
					try {
						while (true) {
							((Runnable) nextFrameToDecode.take()).run();
							if (Thread.currentThread().isInterrupted()) {
								throw new InterruptedException();
							}
						}
					} catch(InterruptedException ie) {
						logger.log(Level.FINE, bgThread.getName() + " interrupted while decoding a frame", ie);
					} catch (Exception e) {
						logger.log(Level.WARNING, "Error while decoding a frame in " + bgThread.getName(), e);
					}
				}
			};
			bgThread = new Thread(runLoop, "DataPlayerWorkerThread(" + name + ")");
			bgThread.start();
		}
	}
	
	private Runnable notifyEventThread = new Runnable() {
		public void run() {
			if (mediator != null) {
				mediator.fireMediaChange();
			}
		}
	};
	
	private class LoadFrame implements Runnable {
		private Frame when;

		public LoadFrame(Frame when) {
			this.when = when;
		}
		public void run() {
			boolean shouldNotifyThread = false;
			if (retrieveImageFromCache(when) != null) {
				shouldNotifyThread = true;
			} else {
				Image img;
				try {
					synchronized(this) {
						img = helpGetImage(when);
					}
					if (img != null) {
						// the thread was not interrupted
						// and the decode succeeded
						cacheImage(when, img);
						shouldNotifyThread = true;
					}
				} catch (IOException e) {
					logger.log(Level.WARNING, "Error while decoding frame " + when, e);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if (shouldNotifyThread) {
				EventQueue.invokeLater(notifyEventThread);
			}
		}
	}
	

	private synchronized void cacheImage(Frame when, Image img) {
		setMostRecentImage(img);
		if (!lastFrameCache.remove(when) && lastFrameCache.size() == cacheSize) {
			strongCache.remove(lastFrameCache.remove(cacheSize-1));
		}
		lastFrameCache.add(0,when);
		strongCache.put(when, img);
		weakCache.put(when, new SoftReference(img));
	}

	protected DataPlayerHelper(String name) {
		super();
		frameLoader = new FrameWorkerQueue(name);
	}

	public Image getImage() {
		return getImage(getNow());
	}
	public Image getImage(Instant i) {
		Frame f = getRate().asFrame(i);
		Image currImage = retrieveImageFromCache(f);
		if (Thread.interrupted()) {
			return getMostRecentImage();
		}
		if (currImage != null) {
			logger.finer("Found something in the cache!");
			return currImage;
		}
		if (synchronous || mostRecentImage == null) {
			try {
				synchronized(this) {
					currImage = helpGetImage(f);
				}
				if (Thread.interrupted() && currImage == null) {
					return null;
				}
				cacheImage(f, currImage);
				return currImage;
			} catch (IOException iox) {
				logger.log(Level.SEVERE, "Error while reading media.", iox);
				return getMostRecentImage();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return getMostRecentImage();
			}
		} else {
			try {
				frameLoader.execute(new LoadFrame(f));
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "Interrupted while trying to decode " + f, e);
			}
			return getMostRecentImage();
		}
	}
	/**
	 * @param f
	 * @return
	 */
	private synchronized Image retrieveImageFromCache(Frame f) {
		if (strongCache.containsKey(f)) {
			Image r = (Image) strongCache.get(f);
			if (r != null) {
				lastFrameCache.remove(f);
				lastFrameCache.add(0, f);
				logger.finer("Found something in the cache!");
				return r;
			}
			SoftReference ref = (SoftReference) weakCache.get(f);
			if (ref != null) {
				r = (Image) ref.get();
				if (r != null) {
					logger.finer("Found something in the cache!");
					return r;
				}
			}
		}
		return null;
	}

	abstract protected Image helpGetImage(Frame f) throws IOException, InterruptedException;

	public boolean hasNext() {
		return getNow().compareTo(getSpan().getEndInstant().previous()) < 0;
	}
	public Object next() {
		if (hasNext()) {
			setNow((Instant) getNow().next());
			return getImage();
		} else {
			throw new NoSuchElementException(
				"Index out of bounds (" + getSpan() + "): " + getNow().next());
		}
	}
	public int nextIndex() {
		return getRate().asFrame((Instant) getNow().next()).getFrame();
	}
	public boolean hasPrevious() {
		return getNow().compareTo(getSpan().getStart()) > 0;
	}
	public Object previous() {
		if (hasPrevious()) {
			setNow((Instant) getNow().previous());
			return getImage();
		} else {
			throw new NoSuchElementException(
				"Index out of bounds ("
					+ getSpan()
					+ "): "
					+ getNow().previous());
		}
	}
	public int previousIndex() {
		return getRate().asFrame((Instant) getNow().previous()).getFrame();
	}
	public void add(Object o) {
		throw new UnsupportedOperationException();
	}
	public void remove() {
		throw new UnsupportedOperationException();
	}
	public void set(Object o) {
		throw new UnsupportedOperationException();
	}

	public void destroy() {
		strongCache.clear();
		weakCache.clear();
		lastFrameCache.clear();
		strongCache.clear();
		weakCache.clear();
	}

	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}


	/**
	 * Returns the default pixel aspect ratio of one.
	 * @return 1
	 */
	public Rational getPixelAspectRatio() {
		return new Rational(1);
	}
	/**
	 * The default implementation says that every frame is an i-frame.
	 * Override this to do something useful.
	 * @param i instant to check
	 * @return {@link edu.umd.cfar.lamp.viper.gui.players.DataPlayer#I_FRAME i-frame}
	 */
	public String getImageType(Instant i) {
		return I_FRAME;
	}
	
	/**
	 * Since real data players should know their own metadata, the
	 * default behaviour of this method is to ignore it.
	 * @param element {@inheritDoc} 
	 */
	public void setElement(MediaElement element) {
	}
}
