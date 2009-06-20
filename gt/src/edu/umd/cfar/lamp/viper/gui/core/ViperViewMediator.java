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

package edu.umd.cfar.lamp.viper.gui.core;

import java.awt.*;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import viper.api.Descriptor;
import viper.api.extensions.*;
import viper.api.extensions.CanonicalFileDescriptor;
import viper.api.impl.*;
import viper.api.impl.Util;
import viper.api.time.*;
import viper.api.time.Frame;
import viper.descriptors.*;

import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.apploader.undo.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.players.*;
import edu.umd.cfar.lamp.viper.gui.remote.ViperControls;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Stands between a set of viper metadata and the user interface, 
 * providing information about user focus and limiting the scope that 
 * the user has to pay attention to. This is pretty ad hoc; it is 
 * certainly conceivable that the user would want multiple focii, or 
 * provide multiple filters. Later, we may move to a stream 
 * transformation type view, but this is just to test out some ideas.
 * 
 * @author davidm
 */
public class ViperViewMediator {
	private PrefsManager prefs;
	private ViperData v;
	private URI backingFile;
	private DataPlayer dp;
	private Instant majorMoment;
	private InstantInterval focusInterval;
	private ChronicleMarkerModel markerModel;
	private FileHistoryManager historyManager;
	private File lastDirectory;

	private ViperSelectionSetWithPrimarySelection selection;
	private PropagateInterpolateModule propagator = new PropagateInterpolateImpl();
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.core");

	private EventListenerForUndoHistory ul = new EventListenerForUndoHistory();
	
	private ViperControls playControls;

	/**
	 * Creates a new ViperViewMediator without any data
	 */
	public ViperViewMediator() {
		this.selection = new ViperSelectionSetWithPrimarySelection();
		this.dp = new NotFoundPlayer();
		resetToNewData();
		getPrimarySelection().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				ViperSubTreeChangedEvent e = (ViperSubTreeChangedEvent) event;
				// The goal of this change listener is to generate
				// 'currFileChanged'
				// events, something which really shouldn't be necessary,
				// anyway.
				final Sourcefile currFile = getCurrFile();
				final Sourcefile addedFile = e.getAddedNodes().getFirstSourcefile();
				if ((currFile == null) ? (addedFile != null) : (!currFile
						.equals(addedFile))) {
					// this is an old event; selection is now something else
					return;
				}
				final Sourcefile removedFile = e.getRemovedNodes()
						.getFirstSourcefile();
				if ((addedFile == null) ? (removedFile == null) : (addedFile
						.equals(removedFile))) {
					// this is not a currFileChanged event; some other part of
					// the selection has changed
					return;
				}
				Runnable r = new Runnable() {
					public void run() {
						fireCurrFileChangedAndChangeFile(currFile, addedFile, removedFile);
					}
				};
				SwingUtilities.invokeLater(r);
			}

			/**
			 * @param currFile
			 * @param addedFile
			 * @param removedFile
			 */
			private void fireCurrFileChangedAndChangeFile(Sourcefile currFile, Sourcefile addedFile, Sourcefile removedFile) {
				if (currFile == null) {
					if (removedFile != null) {
						clearTimeFocus();
						resetEnabledActions();
					}
					fireCurrFileChange();
				} else if (removedFile != addedFile) {
					MediaElement currFileInfo = currFile.getReferenceMedia();
					URI fname = currFileInfo.getSourcefileIdentifier();
					File f = null;
					URI localFname = getLocalPathToFile(fname);
					try {
						f = new File(localFname);
					} catch (IllegalArgumentException iax) {
						f = null;
					}
					if (f == null || !f.exists()) {
						logger.warning("Cannot find file: " + fname);
						File[] P = LostFileFinder.getDefaultSearchPaths();
						if (getLastDirectory() != null) {
							File[] exP = new File[P.length + 1];
							exP[0] = getLastDirectory();
							System.arraycopy(P, 0, exP, 1, P.length);
							P = exP;
						}
						LostFileFinder.getSearchDialog(fname, P,
								new WhenSearchCompletes(fname, currFileInfo), getPrefs()
										.getCore());
						clearTimeFocus();
						fireCurrFileChange();
					} else {
						openCanonicalFileAsLocalFile(fname, f.toURI(), currFileInfo);
						logger.finest("::: found as " + f);
					}
				} else {
					fireCurrFileChange();
				}
			}
		});
		resetEnabledActions();
		clearTimeFocus();
		this.vp = new ViperParser();
		this.markerModel = new DefaultChronicleMarkerModel();
	}

	private ViperParser vp;
	
	/**
	 * Gets the type factory, used for associating data types with 
	 * attributes, that is currently in use.
	 * @return the type factory
	 */
	public ViperDataFactory getDataFactory() {
		return vp.getTypeFactory();
	}
	
	/**
	 * Sets the attribute type factory. The attribute type
	 * factory allows different data types to be used with
	 * attributes, and provides parsing and serialization
	 * support for those types.
	 * @param fact the new data type factory
	 */
	public void setDataFactory(ViperDataFactory fact) {
		vp.setTypeFactory(fact);
	}

	/**
	 * Gets the URI of the currently displayed
	 * metadata file.
	 * @return the currently loaded file's URI.
	 * @see LOCAL_HISTORY#Untitled
	 */
	public URI getFileName() {
		return backingFile;
	}
	
	
	private void setBackingFileNameWithoutReload(URI f) {
		backingFile = f;
		prefs.getCore().setWindowDocumentTitle(LAL.Core, FileHistoryManager.getFileTitle(f));
	}

	/**
	 * Opens the file. Warning: is asynchronous; when finished, it calls
	 * setDataPlayer. If you want to open a file synchronously, construct your
	 * own DataPlayer.
	 * 
	 * @param canonical the name of the media file found in the 
	 * viper data file
	 * @param local the actual location the file is stored on
	 * the user's disk 
	 */
	public void openCanonicalFileAsLocalFile(URI canonical, URI local, MediaElement fileInfo) {
		if (canonical.equals(getCurrFileName())) {
			Loader l = new Loader(local, fileInfo);
			l.start();
			AppLoader appFrame = getPrefs().getCore();
			JOptionPane msg = new JOptionPane("Loading media file:\n" + local,
					JOptionPane.INFORMATION_MESSAGE);
			JDialog dialog = msg.createDialog(appFrame, "Loading...");
			l.setContainer(dialog);
			if (local != null) { // XXX should not show 'loading' dialog if already loaded by now
				dialog.show();
			}
			resetEnabledActions();
		}
	}
	
	/**
	 * Adds  the given mapping from canonical file name to
	 * local file name.
	 * @param canonical the name that is found in the sourcefile
	 * element of the viper data file
	 * @param local the local name of the file
	 */
	public void putCanonicalToLocalMapping(URI canonical, URI local) {
		// Now put the name into the preference db
		Model toAdd = null;
		Model toRemove = null;
		getPrefs().model.enterCriticalSection(ModelLock.READ);
		try {
			Resource r = getPrefs().model.getResource(canonical.toString());
			if (r.hasProperty(GT.fileLocation)) {
				r = r.getProperty(GT.fileLocation).getResource();
				URI oldMap = new URI(r.toString());
				if (!oldMap.equals(local)) {
					toRemove = new ModelMem();
					Resource trCan = toRemove.createResource(canonical
							.toString());
					Resource trLocal = toRemove.createResource(oldMap
							.toString());
					toRemove.add(trCan, GT.fileLocation, trLocal);

					toAdd = new ModelMem();
					Resource cannonicalR = toAdd.createResource(canonical
							.toString());
					Resource localR = toAdd.createResource(local.toString());
					toAdd.add(cannonicalR, GT.fileLocation, localR);
				}
			} else {
				toAdd = new ModelMem();
				Resource cannonicalR = toAdd.createResource(canonical
						.toString());
				Resource localR = toAdd.createResource(local.toString());
				toAdd.add(cannonicalR, GT.fileLocation, localR);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} finally {
			getPrefs().model.leaveCriticalSection();
		}
		if (toRemove != null || toAdd != null) {
			getPrefs().changeUser(toRemove, toAdd);
		}
	}
	private class Loader extends SwingWorker {
		private URI local;
		private Window dialog;
		private MediaElement fileInfo;

		Loader(URI localFile, MediaElement fileInfo) {
			super();
			this.local = localFile;
			this.fileInfo = fileInfo;
		}
		
		/** @inheritDoc */
		public Object construct() {
			// XXX: Uses mediator in another thread. Dangerous.
			// XXX: should allow for interruptions
			return DataPlayer.createDataPlayer(fileInfo.getSourcefileIdentifier(), local == null ? null : new File(local), getPrefs());
		}
		
		/** @inheritDoc */
		public void finished() {
			DataPlayer player = (DataPlayer) get();
			player.setMediator(ViperViewMediator.this);
			player.setElement(fileInfo);

			setDataPlayer(player);
			if (ViperViewMediator.this.dp instanceof NotFoundPlayer) {
				if (local == null) {
					logger.log(Level.WARNING, "Could not find file: " + fileInfo.getSourcefileName());
				} else {
					logger.log(Level.WARNING, "Could not open file: " + local);
				}
			} else {
				putCanonicalToLocalMapping(fileInfo.getSourcefileIdentifier(), local);
			}
			fireCurrFileChange();
			if (dialog != null) {
				dialog.setVisible(false);
				dialog.dispose();
			}
			super.finished();
		}

		/**
		 * Gets the 'finding file' dialog associated with this
		 * worker thread.
		 * @return the window
		 */
		public Window getContainer() {
			return dialog;
		}

		/**
		 * Sets the 'finding file' dialog associated with this
		 * worker thread.
		 * @param frame the window
		 */
		public void setContainer(Window frame) {
			dialog = frame;
		}

	}

	/**
	 * Sets the data player associated with this mediator.
	 * @param dp the media player bean
	 */
	private void setDataPlayer(DataPlayer dp) {
		this.dp = dp;
		this.focusInterval = dp.getSpan();
		assert this.focusInterval.getStart().compareTo(this.focusInterval.getEnd()) < 0;
		MediaElement me = getCurrFile().getReferenceMedia();
		me.setSpan(this.focusInterval);
		this.majorMoment = lastInstantForFile.recall(me);
		if (getCurrFile() instanceof CanonicalSourcefile) {
			CanonicalSourcefile s = (CanonicalSourcefile) getCurrFile();
			CanonicalFileDescriptor cfd = s.getCanonicalFileDescriptor();

			if (cfd != null) {
				FrameRate rate = this.dp.getRate();
				if (cfd instanceof FileInformation) {
					((FileInformation) cfd).setFrameRate(rate, false);
				} else {
					cfd.setFrameRate(rate);
				}

				Frame a = rate.asFrame((Instant) this.dp.getSpan().getStart());
				Frame z = rate.asFrame((Instant) this.dp.getSpan().getEnd());
				int numFrames = z.intValue() - a.intValue();
				if (cfd instanceof FileInformation) {
					((FileInformation) cfd).setNumFrames(numFrames, false);
				} else {
					cfd.setNumFrames(numFrames);
				}
				// TODO set dimensions, mime/type, md5 hash, etc.
			}
		}
	}
	
	/**
	 * Finds the local version of the file. If no
	 * local version is present, returns the canonical
	 * version.
	 * @param canonical the file to look up
	 * @return the best chance at finding the file
	 */
	public URI getLocalPathToFile(URI canonical) {
		getPrefs().model.enterCriticalSection(ModelLock.READ);
		try {
			Resource r = getPrefs().model.getResource(canonical.toString());
			if (r.hasProperty(GT.fileLocation)) {
				r = r.getProperty(GT.fileLocation).getResource();
				try {
					return new URI(r.toString());
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
			return canonical;
		} finally {
			getPrefs().model.leaveCriticalSection();
		}
	}

	/**
	 * Load the metadata file from the given URI and set it to be the currently
	 * editing file. It also sets the first media file described in the file to
	 * be the focal file.
	 * 
	 * @param f
	 *            The file t load
	 * @throws IOException
	 *             If the file cannot load, or if has errors. If the focal file
	 *             cannot be loaded, no exception is thrown.
	 */
	public void setFileName(URI f) throws IOException {
		if (v instanceof EventfulNode) {
			((EventfulNode) v).removeNodeListener(ul);
		}
		if (undoHistoryManager != null) {
			undoHistoryManager.setHistorySize(0);
		}
		backingFile = f;
		prefs.getCore().setWindowModified(LAL.Core, false);
		String t = FileHistoryManager.getFileTitle(f);
		prefs.getCore().setWindowDocumentTitle(LAL.Core, t);

		if (backingFile != null) {
			v = vp.parseFromTextFile(backingFile);
			selection.setRoot(v);
			if (v instanceof EventfulNode) {
				((EventfulNode) v).addNodeListener(ul);
			}
		} else {
			resetToNewData();
		}
		fireSchemaChange(null);

		if (v.getSourcefilesNode().getNumberOfChildren() > 0) {
			String firstFile = ((Sourcefile) v.getAllSourcefiles().get(0))
					.getReferenceMedia().getSourcefileName();
			try {
				setFocalFile(firstFile == null ? null : new URI(firstFile));
			} catch (IOException e) {
				logger.severe("Cannot load " + firstFile + "\n\t"
						+ e.getLocalizedMessage());
			} catch (URISyntaxException e) {
				logger.severe("Invalid file name: '" + firstFile + "'\n\t"
						+ e.getLocalizedMessage());
			}
		} else {
			setFocalFile(null);
		}
	}
	
	private void resetToNewData() {
		v = new ViperDataImpl();
		selection.setRoot(v);
		Config cfg = v.createConfig(Config.FILE, "Information");
		FileInformation.initConfig(cfg);
		v.getConfigsNode().addChild(cfg);
		if (v instanceof EventfulNode) {
			((EventfulNode) v).addNodeListener(ul);
		}
	}

	private EventListenerList listeners = new EventListenerList();
	
	/**
	 * Adds a listener for ui notifications.
	 * @param l the ui listener
	 */
	public void addViperMediatorChangeListener(ViperMediatorChangeListener l) {
		listeners.add(ViperMediatorChangeListener.class, l);
	}
	
	/**
	 * Removes a listener.
	 * @param l the listener to remove
	 */
	public void removeViperMediatorChangeListener(ViperMediatorChangeListener l) {
		listeners.remove(ViperMediatorChangeListener.class, l);
	}
	
	/**
	 * Indicate that the underlying media has changed, although
	 * the metadata has not.
	 */
	public void fireMediaChange() {
		Object[] L = listeners.getListenerList();
		ViperMediatorChangeEvent eo = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ViperMediatorChangeListener.class) {
				if (eo == null)
					eo = new ViperMediatorChangeEvent(this);
				((ViperMediatorChangeListener) L[i + 1]).mediaChanged(eo);
			}
		}
	}
	
	/**
	 * Indicate that the currently selected sourcefile is
	 * no longer selected, and that another one might be.
	 */
	public void fireCurrFileChange() {
		Object[] L = listeners.getListenerList();
		ViperMediatorChangeEvent eo = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ViperMediatorChangeListener.class) {
				if (eo == null)
					eo = new ViperMediatorChangeEvent(this);
				((ViperMediatorChangeListener) L[i + 1]).currFileChanged(eo);
			}
		}
	}
	
	/**
	 * Indicate that the there is a new current frame of interest.
	 */
	public void fireFrameChange() {
		Object[] L = listeners.getListenerList();
		ViperMediatorChangeEvent eo = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ViperMediatorChangeListener.class) {
				if (eo == null)
					eo = new ViperMediatorChangeEvent(this);
				((ViperMediatorChangeListener) L[i + 1]).frameChanged(eo);
			}
		}
	}
	
	/**
	 * Indicate that the instance data of viper has changed.
	 * @param vce the change event that caused this
	 * to be called.
	 */
	public void fireDataChange(ViperChangeEvent vce) {
		Object[] L = listeners.getListenerList();
		ViperMediatorChangeEvent eo = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ViperMediatorChangeListener.class) {
				if (eo == null)
					eo = new ViperMediatorChangeEvent(this, vce);
				((ViperMediatorChangeListener) L[i + 1]).dataChanged(eo);
			}
		}
	}
	
	/**
	 * Indicate that the schema of the file, and possibly
	 * wide sections of the instance data, has changed.
	 * @param vce the wrapped viper api change event,
	 * if any
	 */
	public void fireSchemaChange(ViperChangeEvent vce) {
		Object[] L = listeners.getListenerList();
		ViperMediatorChangeEvent eo = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ViperMediatorChangeListener.class) {
				if (eo == null)
					eo = new ViperMediatorChangeEvent(this, vce);
				((ViperMediatorChangeListener) L[i + 1]).schemaChanged(eo);
			}
		}
	}

	/**
	 * Indicate that the given file has been used, moving
	 * it to the top of the MRU list.
	 * @param filename the file to touch
	 */
	public void modifyMostRecentlyUsed(URI filename) {
		if (null != getHistoryManager() && filename != null) {
			getHistoryManager().touch(filename);
		}
	}

	/**
	 * Gets the current source media file that 
	 * the user is annotating.
	 * @return the current media file of interest. May
	 * be <code>null</code>.
	 */
	public String getFocalFile() {
		return (getCurrFile() == null) ? null : getCurrFile()
				.getReferenceMedia().getSourcefileName();
	}

	private class WhenSearchCompletes implements LostFileFinder.SearchCompleted {
		private URI oldURI;
		private MediaElement fileInfo;
		WhenSearchCompletes(URI oldURI, MediaElement fileInfo) {
			this.oldURI = oldURI;
			this.fileInfo = fileInfo;
		}
		
		/**
		 * Does nothing.
		 */
		public void canceled() {
			openCanonicalFileAsLocalFile(oldURI, null, fileInfo);
		}
		
		/**
		 * Opens the found file.
		 * @param f {@inheritDoc}
		 */
		public void found(File f) {
			setLastDirectory(f);
			openCanonicalFileAsLocalFile(oldURI, f.toURI(), fileInfo);
		}
	}

	private void clearTimeFocus() {
		this.dp = new NotFoundPlayer();
		Sourcefile sf = this.getCurrFile();
		if (sf != null) {
			InstantInterval ii = sf.getReferenceMedia().getSpan();
			if (ii != null) {
				this.focusInterval = ii;
				this.majorMoment = ii.getStartInstant();
			} else {
				this.focusInterval = new Span(new Frame(1), new Frame(2));
				this.majorMoment = new Frame(1);
			}
		} else {
			this.focusInterval = new Span(new Frame(1), new Frame(2));
			this.majorMoment = null;
		}
	}

	/**
	 * Sets the media file the user is currently annotating.
	 * @param fname the name of the file to view
	 * @throws IOException if there is an error while loading
	 * the file
	 */
	public void setFocalFile(URI fname) throws IOException {
		if (getCurrFile() != null && this.majorMoment != null) {
			lastInstantForFile.remember(getCurrFile().getReferenceMedia(),
					majorMoment);
		}

		String stringFname = (null == fname) ? null : fname.toString();
		selection.setTo(getViperData().getSourcefile(stringFname));
	}

	/**
	 * Gets the data associated with the current Sourcefile of interest.
	 * 
	 * @return Sourcefile the Sourcefile that the Mediator currently regards as
	 *         the focus
	 */
	public Sourcefile getCurrFile() {
		return getPrimarySelection().getFirstSourcefile();
	}

	/**
	 * Gets the current media file name as a URI
	 * @return the source media name
	 */
	public URI getCurrFileName() {
		try {
			return new URI(getCurrFile().getReferenceMedia()
					.getSourcefileName());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the underlying annotation information.
	 * @return the viper data object the user is currently editing 
	 */
	public ViperData getViperData() {
		return v;
	}

	/**
	 * Gets the view of the media that currently has focus.
	 * 
	 * @return DataPlayer Gets the DataPlayer for the current sourcefile
	 */
	public DataPlayer getDataPlayer() {
		return dp;
	}

	/**
	 * Gets the span of interest. Currently, this is just
	 * the span of the loaded media file.
	 * @return the media file's span
	 */
	public InstantInterval getFocusInterval() {
		assert focusInterval != null;
		return focusInterval;
	}

	/// keeps track of what frame the user last viewed on each sourcefile
	private MomentRememberer lastInstantForFile = new MomentRememberer();

	private class MomentRememberer {
		private Map media2moment;
		MomentRememberer() {
			media2moment = new HashMap();
		}
		void remember(MediaElement me, Instant when) {
			media2moment.put(me, when);
		}
		Instant recall(MediaElement which) {
			if (media2moment.containsKey(which)) {
				return (Instant) media2moment.get(which);
			} else {
				return which.getSpan().getStartInstant();
			}
		}
	}

	/**
	 * Gets the current frame/time of interest. This
	 * is the one that is displayed in the frame view or
	 * on the spreadsheet, for example.
	 * @return the currrent Instant of interset
	 */
	public Instant getMajorMoment() {
		return majorMoment;
	}

	/**
	 * Gets the {@link #getMajorMoment() major moment} as
	 * a {@link Frame}.
	 * @return the current frame of interest
	 */
	public Frame getCurrentFrame() {
		Sourcefile sf = getCurrFile();
		Instant currI = getMajorMoment();
		if (sf == null || currI == null) {
			return null;
		}
		FrameRate rate = sf.getReferenceMedia().getFrameRate();
		if (currI != null) {
			return rate.asFrame(getMajorMoment());
		}
		return new Frame(1);
	}

	/**
	 * Gets a single frame interval surrounding the current instant of interest.
	 * 
	 * @param frameBased true asks for the interval in terms of 
	 * frames, while false will give the interval in time. 
	 * @return the interval, in frame or time as requested
	 */
	public InstantInterval getCurrInterval(boolean frameBased) {
		Instant now = this.getCurrentFrame();
		InstantInterval appropriate = new Span(now, (Instant) now.next());
		FrameRate rate = this.getCurrFile().getReferenceMedia().getFrameRate();
		if (!frameBased) {
			appropriate = rate.asTime(appropriate);
		}
		return appropriate;
	}

	/**
	 * Sets the majorMoment.
	 * 
	 * @param majorMoment
	 *            The majorMoment to set
	 */
	public void setMajorMoment(Instant majorMoment) {
		boolean changed = false;
		if (majorMoment == null) {
			changed = this.majorMoment != null;
		} else {
			changed = !majorMoment.equals(this.majorMoment);
		}
		if (changed) {
			this.majorMoment = majorMoment;
			fireFrameChange();
		}
	}

	/**
	 * Gets an action that will open a new, empty metadata
	 * file.
	 * @return an "Open New File" action object
	 */
	public ActionListener getNewFileActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveDiscardOrCancelIfChanged(new Runnable() {
					public void run() {
						try {
							modifyMostRecentlyUsed(getFileName());
							setFileName(null);
						} catch (IOException iox) {
							// Won't happen
							throw new RuntimeException(iox);
						}
					}
				});
			}
		};
	}

	/**
	 * Brings up the save/discard/cancel prompt if there are any items in the
	 * action history (ie the file has been altered since it was
	 * opened/created); otherwise, just runs the Runnable. If there is no
	 * history manager, it always prompts the user.
	 * 
	 * @param ok
	 *            To execute if the user doesn't cancel.
	 */
	public void saveDiscardOrCancelIfChanged(Runnable ok) {
		boolean changed;
		if (undoHistoryManager != null) {
			changed = undoHistoryManager.hasChanged();
		} else {
			changed = true;
		}
		if (changed) {
			saveDiscardOrCancel(ok);
		} else {
			ok.run();
		}
	}

	/**
	 * Prompts the user if she would like to save the changes, discard the
	 * changes, or cancel the current action. If the user selects save or
	 * discard, the given runnable is executed. Note, it is executed in the ui
	 * thread.
	 * 
	 * @param ok
	 *            to run if the user selects 'okay'
	 */
	public void saveDiscardOrCancel(Runnable ok) {
		JDialog dialog = new JDialog(this.getPrefs().getCore(), true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel content = (JPanel) dialog.getContentPane();
		SpringLayout layout = new SpringLayout();
		content.setLayout(layout);

		JLabel label = new JLabel(
				"The metadata has been modified. Save changes?");
		content.add(label);

		JButton save = new JButton("Save");
		save.addActionListener(new MaybeSave(true, ok, dialog));
		content.add(save);

		JButton discard = new JButton("Discard");
		discard.addActionListener(new MaybeSave(false, ok, dialog));
		content.add(discard);

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new MaybeSave(false, null, dialog));
		content.add(cancel);

		int lead = 5;
		SpringLayout.Constraints dialogCons = layout.getConstraints(dialog
				.getContentPane());
		SpringLayout.Constraints labelCons = layout.getConstraints(label);
		SpringLayout.Constraints saveCons = layout.getConstraints(save);
		SpringLayout.Constraints discardCons = layout.getConstraints(discard);
		SpringLayout.Constraints cancelCons = layout.getConstraints(cancel);
		Spring leading = Spring.constant(lead);
		Spring border = Spring.constant(lead * 2);

		labelCons.setX(border);
		labelCons.setY(border);

		Spring y = Spring.sum(labelCons.getConstraint(SpringLayout.SOUTH),
				border);
		Spring x_end = labelCons.getConstraint(SpringLayout.EAST);
		saveCons.setY(y);
		discardCons.setY(y);
		cancelCons.setY(y);

		Spring x = border;
		saveCons.setX(x);
		x = Spring.sum(leading, saveCons.getConstraint(SpringLayout.EAST));
		discardCons.setX(x);
		x = Spring.sum(leading, discardCons.getConstraint(SpringLayout.EAST));
		cancelCons.setX(x);
		x = cancelCons.getConstraint(SpringLayout.EAST);
		x_end = Spring.max(x, x_end);

		y = Spring.sum(border, discardCons.getConstraint(SpringLayout.SOUTH));
		dialogCons.setConstraint(SpringLayout.SOUTH, y);
		dialogCons.setConstraint(SpringLayout.EAST, Spring.sum(border, x_end));
		dialog.pack();
		dialog.setLocationRelativeTo(this.getPrefs().getCore());
		dialog.setVisible(true);
	}

	private class MaybeSave implements ActionListener {
		private boolean save;
		private Runnable toRun;
		private Window frame;
		MaybeSave(boolean save, Runnable toRun, Window toClose) {
			this.save = save;
			this.toRun = toRun;
			frame = toClose;
		}
		
		/**
		 * Saves the file if the user wants it so.
		 * @param e {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			boolean doneAsAsked;
			doneAsAsked = save ? save() : true;
			if (toRun != null && doneAsAsked) {
				toRun.run();
			}
			frame.setVisible(false);
			frame.dispose();
		}
	}

	/**
	 * Gets an action that will open the recently viewed file
	 * referenced in the action command.
	 * @return the MRU file opener action
	 */
	public ActionListener getRecentlyViewedFileActionListener() {
		return recentListener;
	}
	private ActionListener recentListener = new RecentlyViewedFileActionListener();
	private class RecentOpener extends Thread {
		private String fname;
		private Component owner;
		RecentOpener(String fname, Component owner) {
			this.fname = fname;
			this.owner = owner;
		}
		/**
		 * Opens the requested file, if possible.
		 */
		public void run() {
			if (fname == null || fname.length() == 0) {
				logger
						.severe("Error loading recently viewed file: No filename specified");
			} else {
				try {
					if (owner != null) {
						owner.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
					}
					URI newUri = URI.create(fname);
					URI oldUri = getFileName();
					setFileName(newUri);
					modifyMostRecentlyUsed(oldUri);
					modifyMostRecentlyUsed(newUri);
				} catch (IOException e) {
					logger.severe("Error while loading " + fname + "\n\t"
							+ e.getLocalizedMessage());
				} catch (IllegalArgumentException iax) {
					logger.severe("Error in " + fname + "\n\t"
							+ iax.getLocalizedMessage());
				} finally {
					if (owner != null) {
						owner.setCursor(Cursor.getDefaultCursor());
					}
				}
			}
		}
	}
	private Component getTopmost(Component comp) {
		if (comp.getParent() == null) {
			return comp;
		} else {
			return getTopmost(comp.getParent());
		}
	}
	
	/**
	 * Gets an action that will redo the last undone action
	 * that hasn't been redone, if one exists.
	 * @return a redo action object
	 */
	public RedoActionListener getRedoActionListener() {
		return redoActionListener;
	}
	
	/**
	 * Gets an action that will undo the last action.
	 * @return an undo action object
	 */
	public UndoActionListener getUndoActionListener() {
		return undoActionListener;
	}

	private RedoActionListener redoActionListener = new RedoActionListener();
	private UndoActionListener undoActionListener = new UndoActionListener();
	private class UndoActionListener extends AbstractAction {
		UndoActionListener() {
			super();
			setEnabled(false);
		}
		/**
		 * Undo action.
		 * @param event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			if (undoHistoryManager != null) {
				undoHistoryManager.undo();
			}
		}
	}
	
	private class RedoActionListener extends AbstractAction {
		RedoActionListener() {
			super();
			setEnabled(false);
		}

		/**
		 * Redo action.
		 * @param event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			if (undoHistoryManager != null) {
				undoHistoryManager.redo();
			}
		}
	}
	private class RecentlyViewedFileActionListener implements ActionListener {
		/**
		 * Opens the specified file.
		 * @param event uses the action command to select the file
		 */
		public void actionPerformed(ActionEvent event) {
			String fname = event.getActionCommand();
			Object o = event.getSource();
			Component comp = null;
			if (o instanceof Component) {
				comp = (Component) o;
				comp = getTopmost(comp);
			}
			Thread runner = new RecentOpener(fname, comp);
			saveDiscardOrCancelIfChanged(new ThreadWrapper(runner));
		}
	}
	private class ThreadWrapper implements Runnable {
		private Thread runner;
		ThreadWrapper(Thread t) {
			runner = t;
		}
		/**
		 * Invokes the thread in process, during the
		 * next run of the UI loop.
		 */
		public void run() {
			SwingUtilities.invokeLater(runner);
		}
	}

	/**
	 * Gets the 'open file' action.
	 * @return an open file action
	 */
	public ActionListener getOpenFileActionListener() {
		return openListener;
	}
	private ActionListener openListener = new OpenActionListener();
	private class Opener extends Thread {
		/**
		 * Brings up a dialog, asking the user what file to open.
		 * The file may then be opened.
		 */
		public void run() {
			JFileChooser dialog = new JFileChooser();
			if (getLastDirectory() != null) {
				dialog.setCurrentDirectory(getLastDirectory());
			}
			dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			prefs.model.enterCriticalSection(ModelLock.READ);
			try {
				try {
					ResIterator iter = prefs.model.listSubjectsWithProperty(
							RDF.type, GT.ChoosableFile);
					while (iter.hasNext()) {
						Resource curr = (Resource) iter.next();
						StringBuffer desc = new StringBuffer();

						String label = prefs.getLocalizedString(curr,
								RDFS.label);
						if (label != null) {
							desc.append(label).append(' ');
						}

						List exts = new LinkedList();
						desc.append("(");
						StmtIterator extIter = curr
								.listProperties(GT.extension);
						while (extIter.hasNext()) {
							Statement currStmt = (Statement) extIter.next();
							String next = currStmt.getString();
							desc.append(next);
							exts.add(next);
							if (extIter.hasNext()) {
								desc.append(File.pathSeparatorChar);
							}
						}
						desc.append(")");
						dialog.addChoosableFileFilter(new ExtensionFilter(exts,
								desc.toString()));
					}
				} finally {
					prefs.model.leaveCriticalSection();
				}
				int returnValue = dialog.showDialog(null, "Open Annotation File");
				switch (returnValue) {
					case JFileChooser.APPROVE_OPTION :
						File f = dialog.getSelectedFile();
						// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						try {
							URI furi = f.toURI();
							URI oldUri = getFileName();
							setLastDirectory(f);
							setFileName(furi);
							modifyMostRecentlyUsed(oldUri);
							modifyMostRecentlyUsed(furi);
						} finally {
							// setCursor(Cursor.getPredefinedCursor(Cursor.PREDEFINED_CURSOR));
						}
						break;
				}
			} catch (RDFException e) {
				logger.severe("Error while saving MRU file information: "
						+ e.getLocalizedMessage());
			} catch (IOException e) {
				logger.severe("Error while opening file: "
						+ e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Copies the given descriptor and adds it to the descriptor's
	 * parent sourcefile node.
	 * @param instance the descriptor to copy
	 * @return the new descriptor
	 */
	public Descriptor duplicateDescriptor(final Descriptor instance) {
		final Config cfg = instance.getConfig();
		final Sourcefile sf = (Sourcefile) instance.getParent();
		if (cfg.getDescType() != Config.OBJECT) {
			throw new IllegalArgumentException(
					"Can only duplicate OBJECT descriptors");
		}
		final Descriptor[] box = new Descriptor[1];
		Runnable duplicate = new Runnable() {
			public void run() {
				Descriptor e = sf.createDescriptor(cfg);
				if (e != null) {
					e.setValidRange((InstantRange) instance.getValidRange()
							.clone());
					for (Iterator iter = instance.getAttributes(); iter
							.hasNext();) {
						Attribute d_i = (Attribute) iter.next();
						AttrConfig acfg = d_i.getAttrConfig();
						Attribute e_i = e.getAttribute(acfg);
						if (acfg.isDynamic()) {
							Iterator vals = d_i.getAttrValuesOverWholeRange();
							while (vals.hasNext()) {
								DynamicAttributeValue curr;
								curr = (DynamicAttributeValue) vals.next();
								e_i.setAttrValueAtSpan(curr.getValue(), curr);
							}
						} else {
							e_i.setAttrValue(d_i.getAttrValue());
						}
					}
					box[0] = e;
				} else {
					logger.severe("Could not create instance of " + cfg);
				}
			}
		};
		String uri = ViperParser.IMPL + "duplicateDescriptor";
		Object[] properties = new Object[0];
		Util.tryTransaction(duplicate, sf, uri, properties);
		return box[0];
	}

	/// Creates a duplicate of the currently selected instance (if possible)
	private ActionListener duplicateInstanceActionListener = new DuplicateInstanceActionListener();
	private class DuplicateInstanceActionListener implements ActionListener {
		/**
		 * @inheritDoc
		 */
		public void actionPerformed(ActionEvent ae) {
			Descriptor instance = getPrimarySelection().getFirstDescriptor();
			if (instance != null && instance.getDescType() == Config.OBJECT) {
				selection.setTo(duplicateDescriptor(instance));
			} else {
				logger.warning("No Object descriptor selected to duplicate");
			}
		}
	}

	/// Creates a new instance of the selected descriptor config
	private ActionListener createInstanceActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			final Config cfg = getPrimarySelection().getFirstConfig();
			final Sourcefile sf = getCurrFile();
			final Descriptor[] newDescriptor = new Descriptor[1];
			if (cfg != null && sf != null) {
				Runnable createDesc = new Runnable() {
					public void run() {
						Descriptor d = sf.createDescriptor(cfg);
						if (d != null) {
							// Initialize the valid frames for the descriptor
							if (cfg.getDescType() != Config.FILE) {
								InstantRange range = new InstantRange();
								Frame start = getCurrentFrame();
								range.add(start, start.next());
								d.setValidRange(range);
								if (cfg.getDescType() == Config.OBJECT) {
									Iterator iter = d.getAttributes();
									while (iter.hasNext()) {
										Attribute a = (Attribute) iter.next();
										if (a.getAttrConfig().isDynamic()) {
											Object v = a.getAttrConfig()
													.getDefaultVal();
											if (v != null) {
												Span s = new Span(start,
														(Frame) start.next());
												a.setAttrValueAtSpan(v, s);
											}
										}
									}
								}
							}
							newDescriptor[0] = d;
						} else {
							logger
									.severe("Could not create instance of "
											+ cfg);
						}
					}
				};
				String uri = ViperParser.IMPL + "createDescriptor";
				Object[] properties = new Object[]{};
				Util.tryTransaction(createDesc, sf, uri, properties);
				if (newDescriptor[0] != null) {
					selection.setTo(newDescriptor[0]);
				}
			} else {
				logger.warning("No descriptor type selected to create");
			}
		}
	};

	/// Remove the currently selected descriptor
	private ActionListener deleteInstanceActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			// Find the selected descriptor, and remove it from the
			// viper api heirarchy
			Sourcefile sf = getPrimarySelection().getFirstSourcefile();
			Descriptor toDelete = getPrimarySelection().getFirstDescriptor();
			if (sf != null && toDelete != null) {
				int i = sf.indexOf(toDelete);
				sf.removeChild(toDelete);
				i = Math.min(i, sf.getNumberOfChildren()-1);
				if (i >= 0) {
					selection.setTo(sf.getChild(i));
				}
			} else {
				logger.warning("No descriptor selected to remove");
			}
		}
	};

	private ActionListener selectDescriptorConfigActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			if (null == v || v.getConfigsNode().getNumberOfChildren() == 0) {
				return;
			}
			String dir = ae.getActionCommand();
			Config oldCfg = getPrimarySelection().getFirstConfig();
			Configs parent = v.getConfigsNode();
			int offset = 0;
			if (null != oldCfg) {
				if (parent.getNumberOfChildren() == 1) {
					return;
				}
				offset = parent.indexOf(oldCfg);
				if (dir.startsWith("prev")) {
					offset--;
					if (offset < 0) {
						offset = parent.getNumberOfChildren() - 1;
					}
				} else {
					offset++;
					if (offset >= parent.getNumberOfChildren()) {
						offset = 0;
					}
				}
			}
			Config newCfg = (Config) v.getConfigsNode().getChild(offset);
			if (newCfg != oldCfg) {
				selection.setTo(newCfg);
			}
		}
	};

	/**
	 * Gets the next config of the same type in order.
	 */
	private ActionListener selectDescriptorInstanceActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			if (null == v || null == getCurrFile()
					|| getCurrFile().getNumberOfChildren() == 0) {
				return;
			}
			Config cfg = getPrimarySelection().getFirstConfig();
			if (null == cfg) { // no config selected
				selectDescriptorConfigActionListener.actionPerformed(ae);
				cfg = getPrimarySelection().getFirstConfig();
				if (null == cfg) { // no configs available
					return;
				}
			}
			String dir = ae.getActionCommand();
			Descriptor oldDesc = getPrimarySelection().getFirstDescriptor();
			Attribute oldAttr = getPrimarySelection().getFirstAttribute();
			Descriptor newDesc = null;
			Attribute newAttr = null;
			boolean seekRequired = true;
			if (oldDesc == null) {
				if (dir.startsWith("prev")) {
					int last = getCurrFile().getNumberOfChildren() - 1;
					newDesc = (Descriptor) getCurrFile().getChild(last);
				} else {
					newDesc = (Descriptor) getCurrFile().getChild(0);
				}
				seekRequired = !newDesc.getConfig().equals(cfg);
			}
			if (seekRequired) {
				int offset = getCurrFile().indexOf(oldDesc);
				int totalChildren = getCurrFile().getNumberOfChildren();
				int count = 0;
				if (dir.startsWith("prev")) {
					while (count++ < totalChildren) {
						offset++;
						if (offset >= getCurrFile().getNumberOfChildren()) {
							offset = 0;
						}
						newDesc = (Descriptor) getCurrFile().getChild(offset);
						if (newDesc.getConfig().equals(cfg)) {
							break;
						}
					}
				} else {
					while (count++ < totalChildren) {
						offset--;
						if (offset < 0) {
							offset = getCurrFile().getNumberOfChildren() - 1;
						}
						newDesc = (Descriptor) getCurrFile().getChild(offset);
						if (newDesc.getConfig().equals(cfg)) {
							break;
						}
					}
				}
				if (newDesc == null) {
					return;
				}
				if (oldAttr != null
						&& newDesc.getConfig().equals(oldDesc.getConfig())) {
					newAttr = newDesc.getAttribute(oldAttr.getAttrConfig());
				}
			}
			if (newAttr == null) {
				selection.setTo(newDesc);
			} else {
				selection.setTo(newAttr);
			}
		}
	};

	private ActionListener selectAttributeActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			if (null == v || null == getCurrFile()) {
				return;
			}
			String dir = ae.getActionCommand();
			Attribute curr = getPrimarySelection().getFirstAttribute();
			Descriptor d = getPrimarySelection().getFirstDescriptor();
			if (d == null) { // no descriptor has been selected
				selectDescriptorInstanceActionListener.actionPerformed(ae);
				d = getPrimarySelection().getFirstDescriptor();
				if (null == d) { // no descriptors available
					return;
				}
			}
			if (0 == d.getNumberOfChildren()) {
				return;
			} else if (1 == d.getNumberOfChildren()) {
				if (curr == null) {
					curr = (Attribute) d.getChild(0);
				}
			} else {
				int offset;
				if (dir.startsWith("prev")) {
					if (curr == null) {
						offset = 0;
					} else {
						offset = d.indexOf(curr) + 1;
						if (offset >= d.getNumberOfChildren()) {
							offset = 0;
						}
					}
				} else {
					if (curr == null) {
						offset = d.getNumberOfChildren() - 1;
					} else {
						offset = d.indexOf(curr) - 1;
						if (offset < 0) {
							offset = d.getNumberOfChildren() - 1;
						}
					}
				}
				curr = (Attribute) d.getChild(0);
			}
			selection.setTo(curr);
		}
	};

	private String flipNorthSouth(String dir) {
		return dir.replace('S', 'Z').replace('N', 'S').replace('Z', 'N');
	}

	private int string2direction(String dir) {
		String cardinal = "N E S W";
		String secondary = " NESESWNW";
		if (dir.length() == 1) {
			return cardinal.indexOf(dir);
		} else if (dir.length() == 2) {
			return secondary.indexOf(dir);
		} else {
			return -1;
		}
	}

	private MoveAttributeActionListener moveAttributeActionListener = new MoveAttributeActionListener();
	private class MoveAttributeActionListener implements ActionListener {
		/**
		 * Moves the attribute, using the action command string to
		 * indicate the direction and distance.
		 * @param  event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			Attribute sa = getPrimarySelection().getFirstAttribute();
			if (sa != null) {
				Object value = null;
				Frame now = getCurrentFrame();

				value = sa.getAttrValueAtInstant(now);
				if (value instanceof Moveable) {
					Moveable val = (Moveable) value;
					String cmd = event.getActionCommand();
					try {
						StringTokenizer tok = new StringTokenizer(cmd);

						String dirS = tok.nextToken();
						int dir = string2direction(flipNorthSouth(dirS));
						if (dir == -1) {
							logger
									.severe("Not a valid direction for MoveAction: "
											+ dirS);
							return;
						}
						String distS = tok.nextToken();
						int dist = Integer.parseInt(distS);

						Moveable newVal = val.move(dir, dist);

						if (sa.getAttrConfig().isDynamic()) {
							Span s = new Span(now, now.go(1));
							sa.setAttrValueAtSpan(newVal, s);
						} else { // static
							sa.setAttrValue(newVal);
						}
					} catch (NoSuchElementException ex) {
						logger.severe("Not enough arguments in MoveAction");
						return;
					} catch (NumberFormatException ex) {
						logger.severe("Distance in MoveAction not integer");
						return;
					}

				}

			}
		}
	}

	private class OpenActionListener implements ActionListener {
		/**
		 * Tries to open a file, if the user saves
		 * the current changes.
		 * @param event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			Thread t = new Opener();
			saveDiscardOrCancelIfChanged(new ThreadWrapper(t));
		}
	}
	
	/**
	 * Gets a 'Save, Discard, or Cancel' dialog up.
	 * @return an action attemptor
	 */
	public AttemptToPerformAction getSaveDiscardOrCancelDialogVeto() {
		return new AttemptToPerformAction() {
			public void attempt(final Runnable r) {
				saveDiscardOrCancelIfChanged(new Runnable() {
					public void run() {
						modifyMostRecentlyUsed(getFileName());
						r.run();
					}
				});
			}
		};
	}

	/**
	 * Gets the 'Save Current File' action object.
	 * @return a save action
	 */
	public ActionListener getSaveActionListener() {
		return saveListener;
	}
	private boolean saveAs() {
		JFileChooser dialog = new JFileChooser();
		if (getLastDirectory() != null) {
			dialog.setCurrentDirectory(getLastDirectory());
		}
		dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		dialog.setSelectedFile(makeFileNameInDirectory(dialog.getCurrentDirectory()));

		try {
			int returnValue = dialog.showSaveDialog(null);
			switch (returnValue) {
				case JFileChooser.APPROVE_OPTION :
					File f = dialog.getSelectedFile();
					if (!f.canWrite() && !(f.getName().endsWith(".gtf") || f.getName().endsWith(".xgtf") || f.getName().endsWith(".xml"))) {
						f = new File(f.getAbsoluteFile() + ".xgtf");
					}
					setLastDirectory(f);
					return saveToFile(f);
			}
		} catch (RDFException e) {
			logger.severe("Error while saving MRU file information: "
					+ e.getLocalizedMessage());
		} catch (IOException e) {
			logger
					.severe("Error while saving file: "
							+ e.getLocalizedMessage());
		}
		return false;
	}
	
	/**
	 * Makes a new file name in the given directory.
	 * If the file has already been saved with a given name, it
	 * uses the form "[old name] 01.xgtf", where 01 is the first unique
	 * number. If it hasn't been saved, but contains only one source media
	 * file, it uses the name "[source]-[sourcetype] 01.xgtf", where the number is 
	 * dropped if there is no conflict. If no source has been added, it uses the name 
	 * template, and if multiple, untitled.
	 *  
	 * @param currentDirectory
	 * @return
	 */
	private File makeFileNameInDirectory(File currentDirectory) {
		String root = "Untitled";
		if (getFileName() != null && !"".equals(getFileName())) {
			root = new File(getFileName()).getName();
		} else if (getViperData().getAllSourcefiles().isEmpty()) {
			root = "Template";
		} else if (getViperData().getAllSourcefiles().size() == 1 && getDataPlayer() != null) {
			root = new File(getDataPlayer().getURI()).getName();
			root = root.replaceAll("\\.", "\\-");
		}
		File f = new File(currentDirectory, root + ".xgtf");
		int x = 1;
		NumberFormat nf = new DecimalFormat("00");
		while (f.exists()) {
			f = new File(currentDirectory, root + " " + nf.format(x++) + ".xgtf");
		}
		return f;
	}

	/**
	 * Tries to save to the given file name.
	 * @param f the file to save to
	 * @return <code>true</code> if the file saved successfully
	 * @throws IOException
	 */
	private boolean saveToFile(File f) throws IOException {
  		File tmpSave = File.createTempFile("_gtf-", f.getName(), f.getParentFile());
 		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmpSave), "UTF-8"));
  		try {
  			XmlSerializer.toWriter(getViperData(), pw);
  		} finally {
			pw.close();
		}
		if (f.exists() && !f.delete()) {
			return false;
		}
		if (tmpSave.renameTo(f)) {
			setBackingFileNameWithoutReload(f.toURI());
			modifyMostRecentlyUsed(getFileName());
			undoHistoryManager.markSavedNow();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes all entries in the local history. This cleans up some file space.
	 * Until we have a local history manager, this should be called after
	 * checking to see if there is a lost autosaved file when the program is
	 * started.
	 */
	private void scrubLocalHistory() {
		Model toRemove = new ModelMem();
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			Iterator allEntries = prefs.model
					.listSubjectsWithProperty(LOCAL_HISTORY.savedAs);
			if (!allEntries.hasNext()) {
				return;
			}

			while (allEntries.hasNext()) {
				Resource entryR = (Resource) allEntries.next();
				Statement fileStmt = entryR.getProperty(LOCAL_HISTORY.savedAs);
				toRemove.add(fileStmt);
				toRemove.add(entryR.getProperty(LOCAL_HISTORY.timeStamp));
				toRemove.add(entryR.getProperty(LOCAL_HISTORY.forFile));

				Resource fileR = fileStmt.getResource();
				File f;
				try {
					f = new File(new URI(fileR.getURI()));
					f.delete();
				} catch (URISyntaxException e) {
					logger.log(Level.WARNING,
							"Unable to delete specified local history entry: "
									+ fileR, e);
				}
			}
		} finally {
			prefs.model.leaveCriticalSection();
		}
		if (!toRemove.isEmpty()) {
			prefs.changeUser(toRemove, null);
		}
	}

	private Object[] findMostRecentFileFromTimestamp(Property p) {
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			ResIterator iter = prefs.model.listSubjectsWithProperty(p);
			Resource max = null;
			Calendar maxSaved = null;
			while (iter.hasNext()) {
				Resource n = (Resource) iter.next();
				Literal lit = n.getProperty(p).getLiteral();
				Calendar nTime = ((XSDDateTime) lit.getDatatype().parse(
						lit.getString())).asCalendar();
				if (maxSaved == null || maxSaved.before(nTime)) {
					max = n;
					maxSaved = nTime;
				}
			}
			if (max != null) {
				return new Object[]{max, maxSaved};
			}
			return null;
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}

	/**
	 * Checks to see if a lost file exists. If so, asks the user if he wants to
	 * load it.
	 */
	private void checkForLostFile() {
		Model model = prefs.model;
		AppLoader core = this.getPrefs().getCore();
		final JDialog dialog = new JDialog(core, true);
		boolean good = false;
		try {
			model.enterCriticalSection(ModelLock.READ); // or ModelLock.WRITE
			try {
				Object[] lastAutomaticEntry = findMostRecentFileFromTimestamp(LOCAL_HISTORY.timeStamp);
				if (lastAutomaticEntry == null) {
					return;
				}

				Object[] lastManualEntry = findMostRecentFileFromTimestamp(MRU.viewedOn);
				boolean foundAutosave = false;
				if (lastManualEntry == null) {
					foundAutosave = true;
				} else {
					Calendar autoTime = (Calendar) lastAutomaticEntry[1];
					Calendar manualTime = (Calendar) lastManualEntry[1];
					foundAutosave = autoTime.after(manualTime);
					//DateFormat f = DateFormat.getInstance();
					//System.out.println(f.format(autoTime.getTime()) + " after
					// "+ f.format(manualTime.getTime()) + " == " +
					// foundAutosave);
				}
				if (!foundAutosave) {
					return;
				}
				try {
					Resource localHistoryEntryR = (Resource) lastAutomaticEntry[0];
					Resource fileNameR = prefs.model.getProperty(
							localHistoryEntryR, LOCAL_HISTORY.forFile)
							.getResource();
					final URI localFileURI = new URI(localHistoryEntryR
							.getProperty(LOCAL_HISTORY.savedAs).getResource()
							.getURI());
					final URI backingURI = LOCAL_HISTORY.Untitled
							.equals(fileNameR) ? null : new URI(fileNameR
							.getURI());

					String question = "<html>The program exited without saving ";
					if (backingURI == null) {
						question += "your data to a file";
					} else {
						question += '"' + fileNameR.getLocalName() + '"';
					}
					question += ".<br>Would you like to continue editing it?";

					dialog
							.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					JPanel content = (JPanel) dialog.getContentPane();
					SpringLayout layout = new SpringLayout();
					content.setLayout(layout);

					JLabel label = new JLabel(question);
					content.add(label);

					JButton clean = new JButton("Start Clean");
					clean.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							dialog.dispose();
							scrubLocalHistory();
						}
					});
					content.add(clean);

					JButton reload = new JButton("Continue Editing");
					reload.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								setFileName(localFileURI);
								setBackingFileNameWithoutReload(backingURI);
								resetEnabledActions();
							} catch (IOException e1) {
								logger.log(Level.SEVERE,
										"Unable to reload autosaved file", e1);
							}
							dialog.dispose();
						}
					});
					content.add(reload);

					int lead = 5;
					SpringLayout.Constraints dialogCons = layout
							.getConstraints(dialog.getContentPane());
					SpringLayout.Constraints labelCons = layout
							.getConstraints(label);
					SpringLayout.Constraints saveCons = layout
							.getConstraints(clean);
					SpringLayout.Constraints discardCons = layout
							.getConstraints(reload);
					Spring leading = Spring.constant(lead);
					Spring border = Spring.constant(lead * 2);

					labelCons.setX(border);
					labelCons.setY(border);

					Spring y = Spring.sum(labelCons
							.getConstraint(SpringLayout.SOUTH), border);
					Spring x_end = labelCons.getConstraint(SpringLayout.EAST);
					saveCons.setY(y);
					discardCons.setY(y);

					Spring x = border;
					saveCons.setX(x);
					x = Spring.sum(leading, saveCons
							.getConstraint(SpringLayout.EAST));
					discardCons.setX(x);
					x = Spring.sum(leading, discardCons
							.getConstraint(SpringLayout.EAST));
					x_end = Spring.max(x, x_end);

					y = Spring.sum(border, discardCons
							.getConstraint(SpringLayout.SOUTH));
					dialogCons.setConstraint(SpringLayout.SOUTH, y);
					dialogCons.setConstraint(SpringLayout.EAST, Spring.sum(
							border, x_end));
					dialog.pack();
					dialog.setLocationRelativeTo(this.getPrefs().getCore());
				} catch (URISyntaxException e2) {
					logger
							.log(
									Level.SEVERE,
									"Unexpected URI error while converting Jena URI to Java URI",
									e2);
				}
				good = true;
			} finally {
				model.leaveCriticalSection();
			}
		} finally {
			if (good) {
				dialog.setVisible(true);
			} else {
				dialog.dispose();
			}
		}
	}

	private long whenNextAutosaveShouldOccurr = 0;
	private static final long AUTOSAVE_TIMER = 1024 * 64; // about a minute

	private boolean autosave() {
		whenNextAutosaveShouldOccurr = System.currentTimeMillis()
				+ AUTOSAVE_TIMER;
		logger.fine("Autosaving...");
		if (!undoHistoryManager.hasChanged()) {
			return true;
		}
		Model toAdd = new ModelMem();
		Resource entry = toAdd.createResource();
		Resource forFile;
		if (getFileName() == null || getFileName().equals("")) {
			forFile = LOCAL_HISTORY.Untitled;
		} else {
			forFile = toAdd.createResource(getFileName().toASCIIString());
		}

		Calendar c = new Iso8601Calendar();
		c.setTime(new Date());
		String timeNow = c.toString();
		Literal nowL = toAdd.createTypedLiteral(timeNow,
				XSDDatatype.XSDdateTime);

		File histFile;
		try {
			File userDir = new File(prefs.getUserDirectory());
			if (userDir == null) {
				userDir = new File(System.getProperty("user.dir"));
			}
			histFile = File.createTempFile("_gtf-", ".xgtf", userDir);
			XmlSerializer.toWriter(getViperData(), new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(histFile), "UTF-8")));
			Resource savedAs = toAdd.createResource(histFile.toURI()
					.toASCIIString());

			toAdd.add(entry, LOCAL_HISTORY.forFile, forFile);
			toAdd.add(entry, LOCAL_HISTORY.timeStamp, nowL);
			toAdd.add(entry, LOCAL_HISTORY.savedAs, savedAs);
			prefs.changeUser(null, toAdd);
			return true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while autosaving", e);
		} catch (PreferenceException e) {
			logger
					.log(Level.SEVERE, "Preference exception while autosaving",
							e);
		}
		return false;
	}
	private boolean save() {
		if (getFileName() == null || getFileName().equals("")) {
			return saveAs();
		} else {
			try {
				File f = new File(getFileName());
				return saveToFile(f);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	private ActionListener saveListener = new SaveMetadataActionListener();
	private class SaveMetadataActionListener implements ActionListener {
		/**
		 * Saves the current metadata. If the file is "Untitled", 
		 * it will prompt for a file name.
		 * @param event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			save();
		}
	}

	/**
	 * Gets the 'Save File As...' action object.
	 * @return a 'Save File As...' action object
	 */
	public ActionListener getSaveAsActionListener() {
		return saveAsListener;
	}
	private ActionListener saveAsListener = new SaveAsMetadataActionListener();
	private class SaveAsMetadataActionListener implements ActionListener {
		/**
		 * Saves the file to a new file name.
		 * @param event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			saveAs();
		}
	}
	
	private Action exportCurrentFileListener = new AbstractAction() {
		/**
		 * Exports the currently selected sourcefile
		 * as a .gtf/text file.
		 * @param event {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent event) {
			if (getCurrFile() != null) {
				try {
					if (ImportExport.getStartDirectory() == null) {
						ImportExport.setStartDirectory(getLastDirectory());
					}
					if (ImportExport.exportSourcefileAsGTF(getCurrFile())) {
						setLastDirectory(ImportExport.getStartDirectory());
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error while exporting", e);
				} catch (BadDataException e) {
					logger.log(Level.SEVERE, "Error while exporting", e);
				}
			}
		}
	};
	
	/**
	 * Gets an action object for the 'export current file as 
	 * a .gtf file' option.
	 * @return an 'export' action
	 */
	public ActionListener getExportCurrentFileActionListener() {
		return exportCurrentFileListener;
	}
	private ActionListener importGTFFileListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			if (ImportExport.getStartDirectory() == null) {
				ImportExport.setStartDirectory(getLastDirectory());
			}
			if (ImportExport.importDataFromGTF(getCurrFile())) {
				setLastDirectory(ImportExport.getStartDirectory());
			}
		}
	};
	
	/**
	 * Gets an action object for the 'import all data from an
	 * existing xgtf' option.
	 * @return an 'import' action
	 */
	public ActionListener getImportXGTFActionListener() {
		return importXGTFListener;
	}
	private ActionListener importXGTFListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			if (ImportExport.getStartDirectory() == null) {
				ImportExport.setStartDirectory(getLastDirectory());
			}
			if (ImportExport.importAllDataFromXGTF(getViperData())) {
				setLastDirectory(ImportExport.getStartDirectory());
			}
		}
	};
	
	/**
	 * Gets an action object for the 'Import GTF into Current 
	 * Media File' menu option.
	 * @return an import gtf action object
	 */
	public ActionListener getImportGTFFileActionListener() {
		return importGTFFileListener;
	}
	private ActionListener importConfigListener = new ActionListener() {
		/**
		 * Imports a user-specified file into the current file.
		 */
		public void actionPerformed(ActionEvent event) {
			try {
				if (ImportExport.getStartDirectory() == null) {
					ImportExport.setStartDirectory(getLastDirectory());
				}
				if (ImportExport.importConfig(getViperData())) {
					setLastDirectory(ImportExport.getStartDirectory());
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE,
						"Error while attempting to import config from a file",
						e);
			}
		}
	};
	
	/**
	 * Gets an action object that will import the viper
	 * schema of an existing file.
	 * @return an import config action object
	 */
	public ActionListener getImportConfigActionListener() {
		return importConfigListener;
	}

	/**
	 * Gets the associated application loader
	 * preference manager.
	 * @return the preferences
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * Sets the associated preference manager.
	 * @param manager a set of preferences that describes
	 * the applications and the user preferences for it
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				checkForLostFile();
			}
		});
	}

	/**
	 * Gets the MRU file history manager.
	 * @return the file history
	 */
	public FileHistoryManager getHistoryManager() {
		return historyManager;
	}

	/**
	 * Sets the file history manager to use with
	 * the application.
	 * @param manager the file history
	 */
	public void setHistoryManager(FileHistoryManager manager) {
		historyManager = manager;
	}

	private ActionHistoryPane undoHistoryManager;
	
	/**
	 * Gets the undo/redo action history.
	 * @return the undo/redo panel
	 */
	public ActionHistoryPane getActionHistory() {
		return undoHistoryManager;
	}
	
	/**
	 * Sets the action history panel.
	 * @param ahp the new event history panel
	 */
	public void setActionHistory(ActionHistoryPane ahp) {
		undoHistoryManager = ahp;
	}
	private void setToFirstFile() {
		Iterator iter = v.getSourcefiles();
		if (iter.hasNext()) {
			Sourcefile nf = (Sourcefile) iter.next();
			try {
				setFocalFile(new URI(nf.getReferenceMedia().getSourcefileName()));
			} catch (IOException e) {
				logger.severe("Error while setting to first file: "
						+ e.getLocalizedMessage());
			} catch (URISyntaxException e) {
				assert false : e.getMessage();
			}
		} else {
			selection.clear();
			setMajorMoment(null);
		}
	}
	private void resetEnabledActions() {
		boolean enabled = getCurrFile() != null;
		exportCurrentFileListener.setEnabled(enabled);
		displayClipFileDialogActionListener.setEnabled(enabled);
		
	}
	private void verifyCurrFile() {
		final Sourcefile sf = getCurrFile();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (sf != null) {
					MediaElement oldCurr = sf.getReferenceMedia();
					Sourcefile nf = v
							.getSourcefile(oldCurr.getSourcefileName());
					if (nf == null || !nf.getReferenceMedia().equals(oldCurr)) {
						setToFirstFile();
					}
				} else {
					// curr file was null; set to something, if something is
					// found.
					setToFirstFile();
				}
				resetEnabledActions();
			}
		});
	}
	private class EventListenerForUndoHistory implements NodeListener {
		private void handle(ViperChangeEvent e) {
			if (undoHistoryManager != null) {
				if (e instanceof ViperUndoableEvent) {
					ViperUndoableEvent ue = (ViperUndoableEvent) e;
					ViperUndoableEditWrapper w = new ViperUndoableEditWrapper(
							ue);
					LalUndoableEditWrapper w2 = new LalUndoableEditWrapper(w,
							prefs);
					undoHistoryManager.addAction(w2);
				} else {
					logger.fine("Non-undoable event occurred");
				}
				boolean canRedo = undoHistoryManager.canRedo();
				boolean canUndo = undoHistoryManager.canUndo();
				redoActionListener.setEnabled(canRedo);
				undoActionListener.setEnabled(canUndo);
				prefs.getCore().setWindowModified(LAL.Core,
						undoHistoryManager.hasChanged());
			}
			if (System.currentTimeMillis() > whenNextAutosaveShouldOccurr) {
				prefs.addLeftoverAction(new Runnable() {
					public void run() {
						autosave();
					}
				});
			}
			Node n = e.getParent();
			if (n == null) {
				verifyCurrFile();
				fireSchemaChange(e);
			} else if (n instanceof ViperData) {
				if (!(e.getSource() instanceof Config)) {
					verifyCurrFile();
				}
				if (e.getSource() instanceof Sourcefiles) {
					fireDataChange(e);
				} else {
					fireSchemaChange(e);
				}
			} else if (n instanceof AttrConfig || n instanceof Config
					|| n instanceof Configs) {
				fireSchemaChange(e);
			} else {
				if (n instanceof Sourcefiles || n instanceof Sourcefile) {
					verifyCurrFile();
				}
				fireDataChange(e);
			}
		}
		
		/** @inheritDoc */
		public void nodeChanged(NodeChangeEvent nce) {
			handle(nce);
		}
		/** @inheritDoc */
		public void minorNodeChanged(MinorNodeChangeEvent mnce) {
			handle(mnce);
		}
		/** @inheritDoc */
		public void majorNodeChanged(MajorNodeChangeEvent mnce) {
			handle(mnce);
		}
	}
	static class ViperUndoableEditWrapper implements LabeledUndoableEdit {
		private ViperUndoableEvent.Undoable u;
		private String uri;
		private Object source;
		ViperUndoableEditWrapper(ViperUndoableEvent vue) {
			this.u = vue.getUndoable();
			this.uri = vue.getUri();
			this.source = vue.getSource();
		}

		/** @inheritDoc */
		public void die() {
			u.die();
		}
		/** @inheritDoc */
		public void redo() {
			u.redo();
		}
		/** @inheritDoc */
		public void undo() {
			u.undo();
		}
		/** @inheritDoc */
		public boolean canRedo() {
			return u.canRedo();
		}
		/** @inheritDoc */
		public boolean canUndo() {
			return u.canUndo();
		}

		/** @inheritDoc */
		public String getUri() {
			return uri;
		}

		/** @inheritDoc */
		public String toString() {
			return u.toString();
		}

		/** @inheritDoc */
		public Object getSource() {
			return source;
		}
		
		/** @inheritDoc */
		public Object getClient() {
			return u;
		}
	}

	/**
	 * @return ActionListener
	 */
	public ActionListener getCreateInstanceActionListener() {
		return createInstanceActionListener;
	}

	/**
	 * Gets an action listener that tries to delete the currently selected
	 * descriptor, if it exists.
	 * 
	 * @return ActionListener
	 */
	public ActionListener getDeleteInstanceActionListener() {
		return deleteInstanceActionListener;
	}

	/**
	 * Gets an action listener that tries to copy the selected object
	 * descriptor, if it exists.
	 * 
	 * @return ActionListener
	 */
	public ActionListener getDuplicateInstanceActionListener() {
		return duplicateInstanceActionListener;
	}

	/**
	 * @return ActionListener
	 */
	public ActionListener getSelectAttributeActionListener() {
		return selectAttributeActionListener;
	}

	/**
	 * @return ActionListener
	 */
	public ActionListener getSelectDescriptorConfigActionListener() {
		return selectDescriptorConfigActionListener;
	}

	/**
	 * @return ActionListener
	 */
	public ActionListener getSelectDescriptorInstanceActionListener() {
		return selectDescriptorInstanceActionListener;
	}

	/**
	 * @return MoveAttributeActionListener
	 */
	public MoveAttributeActionListener getMoveAttributeActionListener() {
		return moveAttributeActionListener;
	}

	/**
	 * Sets the value of an attribute (or the default value of an attribute, if
	 * val is an AttrConfig). Also knows about dynamic v. static, and uses the
	 * MajorMoment of the mediator to set dynamic attributes for one frame only.
	 * Follows the same rules as {@link setAttributeValueAtSpan(Object,Attribute,InstantInterval)}
	 * 
	 * @param val
	 *            the new value
	 * @param a
	 *            the attribute on which to set the value
	 */
	public void setAttributeValueAtCurrentFrame(Object val, Attribute a) {
		Instant moment = getMajorMoment();
		if (moment != null) {
			if (a.getRoot() == null) {
				throw new IllegalStateException("Can only set the value of attached attributes");
			}
			Sourcefile sf = (Sourcefile) a.getParent().getParent();
			String focusFile = getFocalFile();
			if (sf.getReferenceMedia().getSourcefileName().equals(focusFile)) {
				Object old = a.getAttrValueAtInstant(moment);
				if (val == null ? old != null : !val.equals(old)) {
					Span span = new Span(moment, (Instant) moment.next());
					setAttributeValueAtSpan(val, a, span);
				}
			}
		}
	}
	
	/**
	 * Returns the attribute value at the current frame; null if it doesn't exist
	 * @param a the attribute to get
	 * @return the attribute value at the current frame
	 */
	public Object getAttributeValueAtCurrentFrame(Attribute a) {
		return a.getAttrValueAtInstant(getCurrentFrame());
	}

	/**
	 * Sets the value of an attribute (or the default value of an attribute, if
	 * val is an AttrConfig).
	 * 
	 * Note that in the case of nillable attributes, setting the 
	 * value to 'null' won't change the descriptor's valid range.
	 * For convenience, setting the attribute to non-null changes 
	 * the enclosing descriptor instance's range to include the span.
	 * However, when the descriptor's validity range is locked, this won't
	 * be modified. Alternatively, if there is a selected region, the
	 * frames outside the selection won't be marked as valid (although
	 * the attribute values will be modified).
	 * 
	 * Also, you will only be able to set values on spans that are contained 
	 * by the enclosing descriptor (e.g. valid frames). You can cheat
	 * by passing a TemporalRange object as the value; this will
	 * just replace the current attribute value.
	 * 
	 * @param val
	 *            the new value
	 * @param a
	 *            the attribute on which to set the value
	 * @param span
	 *            the instant interval at which to set the value
	 */
	public void setAttributeValueAtSpan(Object val, Attribute a,
			InstantInterval span) {
		AttrConfig ac = a.getAttrConfig();
		NodeVisibilityManager H = getHiders();
		if (H.getAttributeVisibility(a) < NodeVisibilityManager.RANGE_LOCKED) {
			throw new IllegalStateException("Cannot modify a locked attribute.");
		}
		if (ac.isDynamic() && !(val instanceof TemporalRange)) {
			Descriptor d = a.getDescriptor();
			InstantRange r = d.getValidRange();
			if (val != null && !r.contains(span) && H.getDescriptorVisibility(d) > NodeVisibilityManager.RANGE_LOCKED) {
				Iterator selectedRange = Collections.singleton(span).iterator();
				if (getChronicleSelectionModel() != null) {
					TemporalRange selectedTime = getChronicleSelectionModel().getSelectedTime();
					if (selectedTime != null) {
						selectedRange = selectedTime.iterator(span);
					}
				}
				if (selectedRange.hasNext()) {
					while (selectedRange.hasNext()) {
						r.add(selectedRange.next());
					}
					d.setValidRange(r);
				}
			}
			a.setAttrValueAtSpan(val, span);
		} else {
			a.setAttrValue(val);
		}
	}

	/**
	 * Tests to see if the descriptor is valid at the current 
	 * frame/time of user interest.
	 * @param d the descriptor
	 * @return if the descriptor is valid at the current moment
	 */
	public boolean isThisValidNow(Descriptor d) {
		if (d.getConfig().getDescType() == Config.FILE) {
			return true;
		}
		InstantRange range = d.getValidRange();
		Instant now = getMajorMoment();
		if (now == null || getCurrFile() == null || getCurrFile().getReferenceMedia() == null) {
			return false;
		}
		FrameRate rate = getCurrFile().getReferenceMedia().getFrameRate();
		if (range.isTimeBased()) {
			now = rate.asTime(now);
		} else {
			now = rate.asFrame(now);
		}
		return range.contains(now);
	}

	/**
	 * Gets the propagation object.
	 * @return an object that supports propagation
	 * and interpolation of the currently loaded data set
	 */
	public PropagateInterpolateModule getPropagator() {
		return propagator;
	}

	private Hiders hiders = new Hiders();

	/**
	 * Gets the manager of the set of hidden items.
	 * @return an object which controls which 
	 * descriptors or attributes should be hidden
	 */
	public NodeVisibilityManager getHiders() {
		return hiders;
	}
	
	/**
	 * Gets the currently selected metadata item.
	 * @return the selection object
	 */
	public ViperSelectionSet getSelection() {
		return selection;
	}
	
	/**
	 * Gets the current top-selection of the 
	 * user. This is the most recent selected
	 * element, and contained within the selection
	 * returned in {@link #getSelection()}
	 * @return the last element selected
	 */
	public ViperSubTree getPrimarySelection() {
		return selection.getPrimary();
	}

	/**
	 * Returns the model that should be used to display markers on a chronicle
	 * view, if one is attatched
	 * 
	 * @return the marker model, or the set of user and system defined moments
	 *         on the currently viewed source file that are important for some
	 *         reason or other.
	 */
	public ChronicleMarkerModel getMarkerModel() {
		return markerModel;
	}

	/**
	 * Sets the marker model. Doesn't set the marker model on the displayed
	 * chronicle; rather, the displayed chronicle should call this to allow
	 * other components access to its marker model.
	 * 
	 * @param model
	 *            the model that is the reference marker model
	 */
	public void setMarkerModel(ChronicleMarkerModel model) {
		markerModel = model;
	}

	/*
	 * When viewing a video, you can display with respect to the centroid of a
	 * selected shape. For example, suppose you are tracking a person using a
	 * bounding box (whose size doesnt change). You can make the box stay still
	 * (using the display with respect to viewing) while the image moves around.
	 * The manager is an object which you can attach listeners whenever the user
	 * decides to enter or exit this viewing mode. When an attribute is updated,
	 * it must be updated using this manager which can then fire an event to the
	 * listeners.
	 */
	DisplayWithRespectToManager dwrtManager = new DisplayWithRespectToManager();

	/**
	 * Use this method to access the display wrt manager, and then you can add
	 * listeners to it. The DWRT manager controls what the current 'display with
	 * respect to' object is; this attribute is used to center or resize the 
	 * display, keeping the element fixed in location, orientation, or even shape.
	 * @return the dwrt manager
	 */
	public DisplayWithRespectToManager getDisplayWRTManager() {
		return dwrtManager;
	}

	/**
	 * Displays the current hotkey bindings in a new
	 * window.
	 */
	public void displayHotkeyBindings() {
		JEditorPane pane = new JEditorPane("text/html", getPrefs().getCore()
				.getHotkeyManager().toHtml());
		pane.setEditable(false);
		JScrollPane scroll = new JScrollPane(pane);
		JFrame popup = new JFrame("Hotkey Bindings");
		popup.setSize(600, 480);
		popup.getContentPane().setLayout(new GridLayout(1, 1));
		popup.getContentPane().add(scroll);
		popup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		popup.setVisible(true);
	}

	/**
	 * Gets an action that displays the current
	 * hotkey bindings.
	 * @return a 'display hotkey bindings' action object
	 */
	public ActionListener getDisplayHotkeyBindingsActionListener() {
		return displayHotkeyBindingsActionListener;
	}
	private ActionListener displayHotkeyBindingsActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			displayHotkeyBindings();
		}
	};

	private boolean showingInvalid = true;

	/**
	 * Tests to see if the user should
	 * care about descriptors that are
	 * invalid at the current frame of 
	 * interest.
	 * @return if client views should show information
	 * about objects that aren't valid at the 
	 * current major moment
	 */
	public boolean isShowingInvalid() {
		return showingInvalid;
	}
	/**
	 * Turns on/off the invalid descriptors in the
	 * spreadsheet view.
	 * @param showingInvalid the new value
	 */
	public void setShowingInvalid(boolean showingInvalid) {
		if (this.showingInvalid != showingInvalid) {
			this.showingInvalid = showingInvalid;
			fireDataChange(null);
		}
	}
	
	/**
	 * Gets an action that toggles the 'showing invalid'
	 * property.
	 * @return an action which shows/hides the invalid 
	 * descriptors in the spreadsheet, and any other
	 * appropriate view
	 */
	public ActionListener getToggleDisplayInvalidActionListener() {
		return toggleDisplayInvalidActionListener;
	}
	private ActionListener toggleDisplayInvalidActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			setShowingInvalid(!isShowingInvalid());
		}
	};
	public void setPropagator(PropagateInterpolateModule propagator) {
		this.propagator = propagator;
	}
	
	/**
	 * Get the last directory the user referenced.
	 * @return the most recently used directory
	 */
	public File getLastDirectory() {
		return lastDirectory;
	}
	/**
	 * Sets the last directory the user referenced.
	 * @param lastDirectory the most recently used directory
	 */
	public void setLastDirectory(File lastDirectory) {
		if (lastDirectory.isFile()) {
			lastDirectory = lastDirectory.getParentFile();
		}
		this.lastDirectory = lastDirectory;
	}

	private ChronicleSelectionModel chronicleSelectionModel = null;
	public ChronicleSelectionModel getChronicleSelectionModel() {
		return chronicleSelectionModel;
	}
	public void setChronicleSelectionModel(
			ChronicleSelectionModel chronicleSelectionModel) {
		this.chronicleSelectionModel = chronicleSelectionModel;
	}

	public ViperControls getPlayControls() {
		return playControls;
	}

	public void setPlayControls(ViperControls playControls) {
		this.playControls = playControls;
	}
	
	/**
	 * Gets an action that displays the clip file dialog box
	 * @return a 'display clip file dialog box' action object
	 */
	public ActionListener getDisplayClipFileDialogActionListener() {
		return displayClipFileDialogActionListener;
	}
	private Action displayClipFileDialogActionListener = new DisplayClipFileDialogActionListener(this);

	
	public void addNewSourcefile(JComponent parent) {
		ViperData v = this.getViperData();
		if (null != v) {
			JFileChooser dialog = new JFileChooser();
			if (this.getLastDirectory()!=null) {
				dialog.setCurrentDirectory(this.getLastDirectory());
			}
			dialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			dialog.setApproveButtonText("Add Media File");
			dialog.setApproveButtonMnemonic('a');
			dialog.addChoosableFileFilter(DataPlayer.INFO_FILE_FILTER);
			dialog.addChoosableFileFilter(DataPlayer.MPEG_FILE_FILTER);
			dialog.addChoosableFileFilter(DataPlayer.QUICKTIME_FILE_FILTER);
			dialog.addChoosableFileFilter(DataPlayer.WINDOWS_FILE_FILTER);
			dialog.addChoosableFileFilter(DataPlayer.STATIC_FILE_FILTER);
			dialog.setFileFilter(dialog.getAcceptAllFileFilter());
			int returnValue = dialog.showDialog(parent, "Add Media File");
			switch (returnValue) {
				case JFileChooser.APPROVE_OPTION :
					File f = dialog.getSelectedFile();
					this.setLastDirectory(f);
					URI fname = f.toURI();
					if (null == v.getSourcefile(fname.toString())) {
						logger.fine(
							"Creating sourcefile for " + fname);
						v.createSourcefile(fname.toString());
					}
					break;
			}
		}
	}

}