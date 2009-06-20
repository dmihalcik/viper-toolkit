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

package edu.umd.cfar.lamp.viper.examples.persontracking.images;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;

import org.apache.commons.collections.*;

import edu.oswego.cs.dl.util.concurrent.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

/**
 * Memory pool for images, so you don't have to call 'getSubImage'
 * or whatever over and over again. For each image you have, it takes
 * an index object and a function object that is used to generate the 
 * image. Weak references to the images are stored, and thumbnails 
 * are kept by a weak hash map by key. This means that the images 
 * themselves may be scavanged at any time, but thumbnails and 
 * function objects will only be scavanged when the key is missing.
 * So, keep a reference to the key somewhere if you plan on
 * ever getting the image out of the archive! The person
 * gallery uses ImageSlice objects, which are only available as 
 * canonical objects, so this is an example to follow.
 * <p>
 * Also, it should be noted that the Calleable objects will be invoked
 * in another thread. While two calleables for the same image will never
 * run concurrently, any number of callables for unique images may run
 * at the same time. (While, due to memory limitations, the current
 * executor task only invokes one at a time, will probably not remain true.)    
 * 
 * @author davidm
 */
public class ImageArchive {
	private static final int THUMB_SIZE = 16;

	/**
	 * 
	 */
	private Map delegate = Collections.synchronizedMap(new WeakHashMap());
	
	private Map thumbnails = Collections.synchronizedMap(new WeakHashMap());

	private volatile Thread extractionThread = null;
	
	private SortedSet delegateOrder = Collections
			.synchronizedSortedSet(new TreeSet());

	private QueuedExecutor executor = null;//new QueuedExecutor();

	public class ImageState {
		private volatile SoftReference fullVersion = new SoftReference(null);
		
		private boolean holdingOn = false;
		
		public synchronized void holdOn() {
			if (!holdingOn) {
				holdingOn = true;
				hardThumbnail = getSmallVersion();
				hardFullVersion = getFullVersion();
			}
		}
		
		public synchronized void holdOff() {
			holdingOn = false;
			hardThumbnail = null;
			hardFullVersion = null;
		}
		
		private Image hardThumbnail = null;
		private Image hardFullVersion = null;

		private Dimension2D size;

		private Callable create;

		private Object key;
		
		private List whenLoaded = new ArrayList();

		/**
		 * Warning: with holdOff, this might not be true for long.
		 * @return
		 */
		public boolean isLoaded() {
			BufferedImage i = (BufferedImage) getFullVersion();
			return i != null && i.getWidth() == size.getWidth() && i.getHeight() == size.getHeight();
		}
		
		final private Callable creationTask = new Callable() {
			
			
			public Object call() {
				BufferedImage oldImage = (BufferedImage) getFullVersion();
				if (isLoaded()) {
					for (int i = 0; i < whenLoaded.size(); i++) {
						((Predicate) whenLoaded.get(i)).evaluate(ImageState.this);
					}
					return oldImage;
				}
				if (ImageState.this != delegate.get(key)) {
					assert false : "This image state is not the value for " + key;
					for (int i = 0; i < whenLoaded.size(); i++) {
						((Predicate) whenLoaded.get(i)).evaluate(ImageState.this);
					}
					return oldImage;
				}
				try {
					BufferedImage fullImage = (BufferedImage) create.call();
					if (fullImage == null) {
						return null;
					}
					if (fullImage.getWidth() != size.getWidth() ||fullImage.getHeight() != size.getHeight()) {
						System.out.println("Image size was incorrect");
						fullImage = PImage.toBufferedImage(fullImage.getScaledInstance((int) size.getWidth(),
								(int) size.getHeight(), Image.SCALE_DEFAULT), true);

					}
					synchronized (thumbnails) {
						if (!thumbnails.containsKey(key)) {
							Dimension t = SmartImageUtilities.smartResize(fullImage
									.getWidth(), fullImage.getHeight(), THUMB_SIZE);
							Image thumb = fullImage.getScaledInstance(t.width,
									t.height, Image.SCALE_DEFAULT);
							thumbnails.put(key, thumb);
							if (holdingOn) {
								hardThumbnail = thumb;
							}
						}
					}
					fullVersion = new SoftReference(fullImage);
					if (holdingOn) {
						hardFullVersion = fullImage;
					}
					for (int i = 0; i < whenLoaded.size(); i++) {
						((Predicate) whenLoaded.get(i)).evaluate(ImageState.this);
					}
					return fullImage;
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}
				return null;
			}
		};

		/**
		 * @param size
		 * @param create
		 * @param whenLoaded 
		 */
		public ImageState(Object key, Dimension2D size, Callable create, Collection whenLoaded) {
			super();
			this.size = size;
			this.create = create;
			this.key = key;
			this.whenLoaded.addAll(whenLoaded);
		}

		public Dimension2D getSize() {
			return size;
		}

		public Image getSmallVersion() {
			if (holdingOn) {
				if (null == hardThumbnail) {
					hardThumbnail = (Image) thumbnails.get(key);
				}
				return hardThumbnail;
			}
			return (Image) thumbnails.get(key);
		}

		public Image getFullVersion() {
			if (holdingOn) {
				if (null == hardFullVersion) {
					hardFullVersion = (Image) fullVersion.get();
				}
				return hardFullVersion;
			}
			return (Image) fullVersion.get();
		}

		public Image getBestImageAvailable() {
			Image i = (Image) fullVersion.get();
			return (i != null) ? i : getSmallVersion();
		}
	}

	public void clear() {
		delegate.clear();
		delegateOrder.clear();
		thumbnails.clear();
	}

	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	public ImageState get(Object key) {
		return (ImageState) delegate.get(key);
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * Paints the image at the given location.
	 * @param paintContext
	 * @param b
	 * @param key
	 * @param imageFunction
	 * @param hold
	 * @return
	 */
	public boolean paint(final PPaintContext paintContext, final PBounds b, final Object key,
			Callable imageFunction, boolean hold) {
		ImageState is = (ImageState) get(key);
		if (is == null) {
			System.out.println("image state lost");
			put(key, imageFunction, new Dimension((int) b.getWidth(), (int) b.getHeight()), null);
			return false;
		}
		is.holdOn();
		try {
			Image i = is.getBestImageAvailable();
			if (i == null) {
				return false;
			}
			double iw = i.getWidth(null);
			double ih = i.getHeight(null);
			Graphics2D g2 = paintContext.getGraphics();

			if (b.x != 0 || b.y != 0 || b.width != iw || b.height != ih) {
				g2.translate(b.x, b.y);
				g2.scale(b.width / iw, b.height / ih);
				g2.drawImage(i, 0, 0, null);
				g2.scale(iw / b.width, ih / b.height);
				g2.translate(-b.x, -b.y);
			} else {
				g2.drawImage(i, 0, 0, null);
			}
			return true;
		} finally {
			if (!hold) {
				is.holdOff();
			}
		}
	}

	public ImageState put(Object key, ImageState value) {
		assert key != null;
		ImageState old = (ImageState) delegate.put(key, value);
		delegateOrder.add(key);
		if (extractionThread == null) {
			extractionThread = new Thread(extractImages, "ImageArchiveExtractor");
			extractionThread.start();
		}
		return old;
	}

	public ImageState put(Object key, Callable getImageFunction, Dimension2D size, Predicate whenLoaded) {
		ImageState old = (ImageState) delegate.get(key);
		Runtime rt = Runtime.getRuntime();
		Collection whenLoadedList;
		if (whenLoaded == null) {
			if (old == null) {
				whenLoadedList = Collections.EMPTY_SET;
			} else {
				whenLoadedList = old.whenLoaded;
			}
		} else {
			if (old == null) {
				whenLoadedList = Collections.singleton(whenLoaded);
			} else {
				whenLoadedList = new ArrayList(old.whenLoaded.size() + 1);
				whenLoadedList.addAll(old.whenLoaded);
				whenLoadedList.add(whenLoaded);
			}
		}
		ImageState is = new ImageState(key, size, getImageFunction, whenLoadedList);
		if (old != null) {
			Image oldImage = old.getFullVersion();
			if (oldImage != null) {
				is.fullVersion = new SoftReference(oldImage);
			}
		}
		put(key, is);
		return old;
	}

	public Object remove(Object key) {
		delegateOrder.remove(key);
		return delegate.remove(key);
	}

	public int size() {
		return delegate.size();
	}

	public Collection values() {
		return delegate.values();
	}

	/**
	 * An infinite loop that goes through looking for things to decode. It tries
	 * to do things on some semblance of an order.
	 */
	private Runnable extractImages = new Runnable() {
		public void run() {
			try {
				while (true) {
					boolean memoryProblems = Runtime.getRuntime().freeMemory() < (1 << 20);
					while (delegateOrder.isEmpty() || memoryProblems) {
						try {
							Thread.sleep(500);
							if  (memoryProblems) {
								// It is apparent that if I'm always running low of memory then
								// I must be keeping references around that I should not be - namely 
								// to fullImages.
							}
							memoryProblems = Runtime.getRuntime().freeMemory() < (1 << 20);
						} catch (InterruptedException ix) {
							// 
						}
					}
	
					// Do one pass, copying keys to another set, then swapping them.
					Object key = delegateOrder.first();
					final ImageState is = (ImageState) get(key);
					delegateOrder.remove(key);
					if (is == null) {
						System.out.println("huh?");
						continue;
					}
					try {
						if (executor != null) {
							executor.execute(new Runnable(){
								public void run() {
									try {
										is.creationTask.call();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						} else {
							is.creationTask.call();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} catch (OutOfMemoryError oome) {
						Runtime.getRuntime().gc();
					}
				}
			} finally {
				System.out.println("Exiting image archive extractor!!!");
			}
		}
	};
}
