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

package edu.umd.cfar.lamp.apploader;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A JMenu that uses the preferences file to determine its contents.
 */
public class AppLoaderMenu {
	static JustMenuNodes justMenuNodes = new JustMenuNodes();

	private static long getPriority(Resource r) {
		if (r.hasProperty(MENU.priority)) {
			Statement prioStmt = r.getProperty(MENU.priority);
			return prioStmt.getLong();
		} else {
			return 0xff;
		}
	}
	
	private class ResetOnMenuDescriptionChange implements ModelListener {
		private Selector s = new Selector() {
			public boolean test(Statement arg0) {
				return arg0.getPredicate().getURI().startsWith(MENU.uri);
			}

			public boolean isSimple() {
				return false;
			}

			public Resource getSubject() {
				return null;
			}

			public Property getPredicate() {
				return null;
			}

			public RDFNode getObject() {
				return null;
			}
		};
		public void changeEvent(ModelEvent event) {
			try {
				resetMenu(core.getRootPane());
			} catch (PreferenceException e) {
				e.printStackTrace();
			}
		}
		/**
		 * Selects out a statement that is in the menu namespace
		 * @return {@inheritDoc}
		 */
		public Selector getSelector() {
			return s;
		}
		
	}

	private static Comparator SORT_BY_PRIORITY = new Comparator() {
		public int compare(Object r1, Object r2) {
			long p1 = getPriority((Resource) r1);
			long p2 = getPriority((Resource) r2);
			boolean neg1 = p1 < 0;
			boolean neg2 = p2 < 0;
			if (neg1 == neg2) {
				return p1 == p2 ? 0 : p1 < p2 ? -1 : 1;
			} else {
				return p1 < p2 ? 1 : -1;
			}
		}
	};

	private static class JustMenuNodes implements ExceptIterator.ExceptFunctor {
		/**
		 * Checks that RDF resources are menu nodes.
		 * 
		 * @param o
		 *            the resource to check
		 * @return <code>true</code> when the resource is a menu or menu item
		 * @see edu.umd.cfar.lamp.viper.util.ExceptIterator.ExceptFunctor#check(java.lang.Object)
		 */
		public boolean check(Object o) {
			if (o instanceof Resource) {
				Resource r = (Resource) o;
				return r.hasProperty(RDF.type, MENU.Menu)
						|| r.hasProperty(RDF.type, MENU.Item);
			}
			return false;
		}

	}

	private class MenuDataNode {
		private Resource self;
		private MenuElement item;

		/**
		 * Gets the hash code of the associated menu RDF resource.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return self == null ? 0 : self.hashCode();
		}

		/**
		 * True when the associated RDF nodes are equal.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof MenuDataNode) {
				MenuDataNode that = (MenuDataNode) o;
				return null == self ? null == that.self : self
						.equals(that.self);
			} else {
				return false;
			}
		}

		private Actionator thisAction;
		boolean groupOpen = false;
		private MenuDataNode parent;

		/**
		 * Creates a new menu node in the menu DAG.
		 * 
		 * @param core
		 *            the resource for the node
		 * @param rootMenu
		 *            the menu root
		 */
		public MenuDataNode(Resource core, JMenuBar rootMenu) {
			this.self = core;
			this.item = rootMenu;
		}

		private Component getMenuIAmInside() {
			if (parent == null) {
				return (Component) item;
			} else if (parent.item == null) {
				return parent.getMenuIAmInside();
			} else if (parent.item instanceof JMenu) {
				return (JMenu) parent.item;
			} else if (parent.item instanceof JMenuBar) {
				return (JMenuBar) parent.item;
			} else {
				return parent.getMenuIAmInside();
			}
		}

		private void closeGroup() {
			if (parent.groupOpen) {
				Component c = getMenuIAmInside();
				if (c instanceof JMenu) {
					((JMenu) c).addSeparator();
				}
				parent.groupOpen = false;
			}
		}

		private void openGroup() {
			Component c = getMenuIAmInside();
			if (c instanceof JMenu) {
				JMenu outer = (JMenu) c;
				if (outer.getItemCount() > 0) {
					// XXX: this will add seperators for empty groups!
					outer.addSeparator();
				}
				parent.groupOpen = true;
			}
		}

		/**
		 * Creates a new menu node from the given resource. This includes
		 * creating the swing menu item, if one exists.
		 * 
		 * @param myResource
		 *            the node to group things together
		 * @param parent
		 *            the parent node
		 * @throws PreferenceException
		 *             if there is an error while getting the RDF description of
		 *             the group
		 */
		public MenuDataNode(Resource myResource, MenuDataNode parent)
				throws PreferenceException {
			this.self = myResource;
			this.parent = parent;
			core.setLoadedBean(this.self, this);
			String itemName = prefs.getLocalizedString(self, RDFS.label);
			if (itemName == null) {
				throw new PreferenceException("Menu does not have a name: "
						+ self.toString());
			}
			if (myResource.hasProperty(RDF.type, MENU.Item)) {
				closeGroup();
				this.item = new JMenuItem(itemName);
				helpSetActionListenerOrDisable();
				prefs.addListener(new MenuItemChangeListener());
				addItemToParent();
			} else if (myResource.hasProperty(RDF.type, MENU.Group)) {
				item = null;
				openGroup();
			} else {
				closeGroup();
				item = new JMenu(itemName);
				prefs.addListener(new MenuGroupChangeListener());
				addItemToParent();
			}
			helpSetMnemonic();
		}

		/**
		 * Adds this.item to the parent list. Note that this should not be
		 * called for groups, or items without parent nodes. The item must first
		 * be instantiated.
		 */
		private void addItemToParent() {
			Component c = getMenuIAmInside();
			if (c instanceof JMenu) {
				((JMenu) c).add(getAsMenuItem());
			} else if (c instanceof JMenuBar) {
				((JMenuBar) c).add(getAsMenuItem());
			}
		}

		/**
		 * Tests to see if the node is a menu node or menu bar node.
		 * 
		 * @return if the node is an internal, but visible, node
		 */
		public boolean isMenu() {
			return item instanceof JMenuBar || item instanceof JMenu;
		}

		/**
		 * Tests to see if the node is a group.
		 * 
		 * @return if the node is a grouping node
		 */
		public boolean isGroup() {
			return item == null;
		}

		/**
		 * Tests to see if the node is a menu leaf (an item).
		 * 
		 * @return if the node is a menu item
		 */
		public boolean isItem() {
			return item instanceof JMenuItem && !(item instanceof JMenu);
		}

		private void addKids() throws PreferenceException {
			Iterator iter = getNodesAttachedTo(this.self);
			while (iter.hasNext()) {
				Resource r = (Resource) iter.next();
				MenuDataNode mdn = new MenuDataNode(r, this);
				if (!mdn.isItem()) {
					mdn.addKids();
				}
			}
		}

		private JMenuItem getAsMenuItem() {
			return (JMenuItem) this.item;
		}

		private void helpSetMnemonic() {
			String mnemonic = prefs
					.getLocalizedString(this.self, MENU.mnemonic);
			if (mnemonic != null) {
				getAsMenuItem().setMnemonic(mnemonic.charAt(0));
			}
		}
		private void helpSetActionListenerOrDisable() {
			// XXX-davidm: speed up enable/disable of actions by not resetting
			// entire menu, just changed part
			if (this.self.hasProperty(MENU.generates)) {
				try {
					Resource action = this.self.getProperty(MENU.generates)
							.getResource();
					thisAction = core.getActionForResource(action);
					ResetMenuOnActionChange rmoac = null;
					PropertyChangeListener[] L = thisAction
							.getPropertyChangeListeners();
					for (int i = 0; i < L.length; i++) {
						if (L[i] instanceof ResetMenuOnActionChange) {
							rmoac = (ResetMenuOnActionChange) L[i];
							break;
						}
					}
					if (rmoac == null) {
						rmoac = new ResetMenuOnActionChange();
						thisAction.addPropertyChangeListener(rmoac);
					}
					rmoac.items.put(getAsMenuItem(), null);
					getAsMenuItem().addActionListener(thisAction);
					String cmd = thisAction.getCommand();
					if (null != cmd) {
						getAsMenuItem().setActionCommand(cmd);
					}
					getAsMenuItem().setEnabled(thisAction.isEnabled());
					return;
				} catch (IllegalArgumentException iax) {
					getAsMenuItem().setEnabled(false);
					// XXX- should log this
				}
			}
			// must be greyed out
			getAsMenuItem().setEnabled(false);
		}

		/**
		 * Removes the action listener specified (actually, and actionator
		 * proxy) from the action list of the swing menu item that corresponds
		 * to this node.
		 * 
		 * @param alNode
		 *            the actionator description
		 * @throws PreferenceException
		 *             if there is an error while retrieving the actionator by
		 *             its description
		 */
		public void removeActionListener(Resource alNode)
				throws PreferenceException {
			getAsMenuItem().removeActionListener(
					core.findActionListener(alNode));
		}
		/**
		 * Adds the described action to the list of events that will be
		 * activated when the swing menu item corresponding to this menu node is
		 * selected.
		 * 
		 * @param alNode
		 *            the actionator description
		 * @throws PreferenceException
		 *             if there is a problem while retrieving the actionator by
		 *             its RDF description
		 */
		public void addActionListener(Resource alNode)
				throws PreferenceException {
			getAsMenuItem().addActionListener(core.findActionListener(alNode));
		}

		private abstract class AbstractItemChangeListener
				implements
					ModelListener {
			/** 
			 * @inheritDoc
			 * @return <code>null</code>
			 */
			public Selector getSelector() {
				return null;
			}
		}
		/**
		 * Deletes and changes this menu if it is altered.
		 */
		private class MenuItemChangeListener extends AbstractItemChangeListener {
			/**
			 * Resets the whole menu.
			 * 
			 * @see edu.umd.cfar.lamp.apploader.prefs.ModelListener#changeEvent(edu.umd.cfar.lamp.apploader.prefs.ModelEvent)
			 */
			public void changeEvent(ModelEvent event) {
				registerMenuReset(event);
			}
		}
		/**
		 * Deletes and changes this menu if it is altered.
		 */
		private class MenuGroupChangeListener
				extends
					AbstractItemChangeListener {
			/**
			 * Resets the whole menu.
			 * 
			 * @see edu.umd.cfar.lamp.apploader.prefs.ModelListener#changeEvent(edu.umd.cfar.lamp.apploader.prefs.ModelEvent)
			 */
			public void changeEvent(ModelEvent event) {
				registerMenuReset(event);
			}
		}
		/**
		 * @return JMenuItem
		 */
		public MenuElement getItem() {
			return item;
		}

		/**
		 * @return Resource
		 */
		public Resource getSelf() {
			return self;
		}

	}

	class ResetMenuOnActionChange implements PropertyChangeListener {
		WeakHashMap items;

		ResetMenuOnActionChange() {
			this.items = new WeakHashMap();
		}

		/**
		 * Resets the whole menu, or just the item, if something small changed.
		 * 
		 * @param evt
		 *            a description of the property change
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if ("enabled".equals(evt.getPropertyName())) {
				boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
				Iterator iter = items.keySet().iterator();
				while (iter.hasNext()) {
					JMenuItem curr = (JMenuItem) iter.next();
					if (curr != null) {
						curr.setEnabled(enabled);
					}
				}
				return;
			}
			try {
				resetMenu(core.getRootPane());
			} catch (PreferenceException e) {
				prefs.getLogger().log(Level.WARNING, "Unable to reset menu", e);
			}
		}
	};

	private ModelEvent lastMenuEvent;
	AppLoader core;
	private PrefsManager prefs;

	private void registerMenuReset(ModelEvent e) {
		if (!e.equals(lastMenuEvent)) {
			lastMenuEvent = e;
			try {
				// XXX-davidm: hack- menu reset only resets menu on root pane,
				// causing problems on macs
				resetMenu(core.getRootPane());
			} catch (PreferenceException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * Resets the menu attached to the given Swing frame to the menu this object
	 * describes.
	 * 
	 * @param frame
	 *            the frame to hold the menu
	 * @throws PreferenceException
	 *             if there is an error while parsing the menu from its RDF
	 *             description
	 */
	public void resetMenu(JRootPane frame) throws PreferenceException {
		MenuDataNode root = helpResetMenu(frame);
		frame.setJMenuBar((JMenuBar) root.getItem());
	}

	private Iterator getNodesAttachedTo(Resource R) {
		Iterator iter = prefs.model
				.listSubjectsWithProperty(MENU.attachment, R);
		ArrayList L = new ArrayList();
		while (iter.hasNext()) {
			L.add(iter.next());
		}
		Collections.sort(L, SORT_BY_PRIORITY);
		return L.iterator();
	}

	private MenuDataNode helpResetMenu(JRootPane frame) throws PreferenceException {
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			JMenuBar bar = frame.getJMenuBar();
			if (bar == null) {
				bar = new JMenuBar();
			} else {
				bar.removeAll();
			}
			MenuDataNode root = new MenuDataNode(LAL.Core, bar);
			root.addKids();
			return root;
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}

	/**
	 * @return Returns the core.
	 */
	public AppLoader getCore() {
		return core;
	}

	/**
	 * @param core
	 *            The core to set.
	 */
	public void setCore(AppLoader core) {
		this.core = core;
		this.prefs = core.getPrefs();
	}

}