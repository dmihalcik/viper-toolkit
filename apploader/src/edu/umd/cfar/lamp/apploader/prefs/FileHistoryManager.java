/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader.prefs;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;

/**
 * Manages the file history list.
 * 
 * @author davidm
 * @since May 23, 2003
 */
public class FileHistoryManager {
	private final class MruFileModelListener implements ModelListener {
		private Selector s = new Selector() {
			public boolean test(Statement stmt) {
				return MRU.viewedOn.equals(stmt.getPredicate());
			}

			public boolean isSimple() {
				return false;
			}

			public Resource getSubject() {
				return null;
			}

			public Property getPredicate() {
				return MRU.viewedOn;
			}

			public RDFNode getObject() {
				return null;
			}
		};

		public void changeEvent(ModelEvent event) {
			reset();
		}

		/**
		 * Selects out a statement that is in the mru namespace
		 * 
		 * @return {@inheritDoc}
		 */
		public Selector getSelector() {
			return s;
		}
	}

	private PrefsManager prefs;

	private int MRULength = 4;

	private Resource parentResource;

	private Resource actionListenerResource;

	/**
	 * Removes all older view entries from the file history.
	 */
	public void clean() {
		// TODO:davidm remove old stuff from history
	}

	/**
	 * Gets the user-friendly title of the given URI. Basically, this means
	 * converting from file: URIs into file names.
	 * 
	 * @param f
	 *            the URI
	 * @return the pretty name
	 */
	public static String getFileTitle(URI f) {
		String title = "Untitled";
		if (f != null) {
			if ("file".equals(f.getScheme())) {
				title = f.getPath();
				if (title.startsWith("/")) {
					title = title.substring(1);
				}
			} else {
				title = f.toASCIIString();
			}
		}
		return title;
	}

	private long lastTouch = 0;
	
	/**
	 * Update the file history to indicate that the file at the given URI was
	 * visited now.
	 * 
	 * @param uri
	 *            the uri to put a new 'viewed date' on
	 */
	public void touch(URI uri) {
		Model toAdd = new ModelMem();
		Model toRemove = null;
		Resource described = toAdd.createResource(uri.toASCIIString());
		Calendar c = new Iso8601Calendar();
		long nowMillis = System.currentTimeMillis();
		if (nowMillis <= lastTouch) {
			nowMillis = lastTouch+1;
			lastTouch = nowMillis;
		}
		Date dateNow = new Date(nowMillis);
		c.setTime(dateNow);
		String timeNow = c.toString();
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			if (!prefs.model.contains(described, MRU.name)) {
				toAdd.add(described, RDF.type, MRU.VisitedFile);
				toAdd.add(described, MRU.name, getFileTitle(uri));
			} else {
				toRemove = new ModelMem();
				Statement stmt = prefs.model.getProperty(described,
						MRU.viewedOn);
				toRemove.add(stmt);
			}
		} finally {
			prefs.model.leaveCriticalSection();
		}
		toAdd.add(described, MRU.viewedOn, toAdd.createTypedLiteral(timeNow,
				XSDDatatype.XSDdateTime));
		prefs.changeUser(toRemove, toAdd);
	}

	/**
	 * Touches the file at the given file path.
	 * 
	 * @param fname
	 *            the file to mark as touched
	 */
	public void touchFile(String fname) {
		URI uri;
		if (fname.startsWith("file://")) {
			uri = URI.create(fname);
		} else {
			uri = new File(fname).toURI();
		}
		touch(uri);
	}

	private static XSDDateTime getViewedOn(Resource r) {
		Literal lit = r.getProperty(MRU.viewedOn).getLiteral();
		return (XSDDateTime) lit.getDatatype().parse(lit.getString());
	}

	/**
	 * Sorts RDF nodes based on their mru:viewedOn date.
	 */
	public static Comparator SORT_XSDATE_TIME = new Comparator() {
		public int compare(Object aO, Object bO) {
			XSDDateTime a = getViewedOn((Resource) aO);
			XSDDateTime b = getViewedOn((Resource) bO);
			Logger logger = Logger
					.getLogger("edu.umd.cfar.lamp.apploader.prefs");
			if (a == null || b == null) {
				if (a == null && b == null) {
					return 0;
				} else if (a == null) {
					return 1;
				} else {
					return -1;
				}
			}
			Calendar aC = a.asCalendar();
			Calendar bC = b.asCalendar();
			if (aC.before(bC)) {
				return 1;
			} else if (bC.before(aC)) {
				return -1;
			} else {
				logger.warning("Cannot determine order for dates: " + a
						+ " :: " + b);
				return 0;
			}
		}
	};

	private MruFileModelListener mruListener = new MruFileModelListener();

	/**
	 * Saves the history list back to the user preference space.
	 */
	public void resetMostRecentlyViewedList() {
		prefs.model.enterCriticalSection(ModelLock.READ);
		Model toAdd = new ModelMem();
		Model toRemove = new ModelMem();
		try {
			ResIterator mruMenuItemRs = prefs.model
				.listSubjectsWithProperty(MENU.attachment, getParentResource());
			while (mruMenuItemRs.hasNext()) {
				Resource menuItemToRemove = mruMenuItemRs.nextResource();
				StmtIterator menuStmts = menuItemToRemove.listProperties();
				while (menuStmts.hasNext()) {
					toRemove.add(menuStmts.nextStatement());
				}
			}
			
			ResIterator mruFileRs = prefs.model
					.listSubjectsWithProperty(MRU.viewedOn);
			Vector v = new Vector();
			while (mruFileRs.hasNext()) {
				v.add(mruFileRs.next());
			}
			Resource[] resar = (Resource[]) v.toArray(new Resource[v.size()]);
			Arrays.sort(resar, SORT_XSDATE_TIME);
			for (int i = 0; i < getMRULength() && i < resar.length; i++) {
				String mnem = String.valueOf(i + 1);
				String label = mnem + " "
						+ prefs.getLocalizedString(resar[i], MRU.name);
				Resource r = toAdd.createResource();
				r.addProperty(RDF.type, MENU.Item);
				r.addProperty(RDFS.label, label);
				r.addProperty(MENU.mnemonic, mnem);
				r.addProperty(MENU.priority, -200 + i);
				r.addProperty(MENU.attachment, getParentResource());

				Resource activatorR = toAdd.createResource();
				activatorR.addProperty(LAL.actionCommand, resar[i].getURI());
				activatorR
						.addProperty(LAL.sendsTo, getActionListenerResource());
				r.addProperty(MENU.generates, activatorR);
			}
		} finally {
			prefs.model.leaveCriticalSection();
		}
		if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
			prefs.changeTemporary(toRemove, toAdd);
		}
	}

	/**
	 * Gets the apploader preference manager.
	 * 
	 * @return the preference manager
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * Sets the preference manager. This must be set for this to be useful, as
	 * that is where the RDF data store is.
	 * 
	 * @param manager
	 *            the preference manager
	 */
	public void setPrefs(PrefsManager manager) {
		if (prefs != null) {
			prefs.removeListener(mruListener);
		}
		prefs = manager;
		reset();
		if (prefs != null) {
			prefs.addListener(mruListener);
		}
	}

	/**
	 * Get the number of items to keep in the MRU list.
	 * 
	 * @return the number of items to keep in the MRU list
	 */
	public int getMRULength() {
		return MRULength;
	}

	/**
	 * Sets the number of items to save in the MRU list.
	 * 
	 * @param i
	 *            the number of items to keep in the MRU list
	 */
	public void setMRULength(int i) {
		if (i != MRULength) {
			MRULength = i;
			resetMostRecentlyViewedList();
		}
	}

	/**
	 * The resource representing the 'open file' bean; this is used to set up
	 * the recently used menu items.
	 * 
	 * @return the open file bean's uri
	 */
	public Resource getActionListenerResource() {
		return actionListenerResource;
	}

	/**
	 * Gets the resource representing the item (usually an lal:menu or an
	 * lal:group) where the MRU menu is to be attached.
	 * 
	 * @return the attachment point for the menu
	 */
	public Resource getParentResource() {
		return parentResource;
	}

	/**
	 * Sets the action that will be invoked when a user clicks on an item in the
	 * MRU menu.
	 * 
	 * @param resource
	 *            the file open bean's uri
	 */
	public void setActionListenerResource(Resource resource) {
		actionListenerResource = resource;
		reset();
	}

	/**
	 * Sets where in the menu the list should be attached.
	 * 
	 * @param resource
	 *            the menu, group, or (don't you think about it) root pane
	 *            container to attach the menu to
	 */
	public void setParentResource(Resource resource) {
		parentResource = resource;
		reset();
	}

	private void reset() {
		if (getParentResource() != null && getActionListenerResource() != null
				&& getPrefs() != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					resetMostRecentlyViewedList();
				}
			});
		}
	}
}
