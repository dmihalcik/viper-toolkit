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

package edu.umd.cfar.lamp.viper.gui.table;

import java.util.*;

import javax.swing.table.*;

import viper.api.*;
import viper.api.time.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * Viper table model presents a view for a table of all instances of a given
 * descriptor config.
 * 
 * It offers a few additional features, like the ability to hide columns (the
 * attributes, the desc id, and the p/v box columns) and sort by columns. The
 * attributes require an entry in the ViperSorters lookup table to be sortable.
 * 
 * The canonical columns are BY_PROPAGATING, BY_VALID, and BY_ID, which are
 * constants, and the attributes themselves, which align with the getChild(int)
 * methods of the descriptor. These numbers are used for the 'display' methods,
 * but all other methods use the 'real' model column numbering (note that even
 * this isn't the highest level; there is still the columnmodel to worry about,
 * which allows the user to reorder the columns).
 *  
 */
public abstract class ViperTableModel extends AbstractTableModel {
	/**
	 * @param cfg
	 */
	public ViperTableModel(Config cfg) {
		this.cfg = cfg;
		this.sortOrder = new LinkedHashMap();
		this.sortOrder.put(new Integer(BY_ID), Boolean.TRUE);
		this.sortOrder.put(new Integer(BY_VALID), Boolean.TRUE);
		this.sortOrder.put(new Integer(BY_PROPAGATING), Boolean.TRUE);
		this.descs = new ArrayList();
		this.resetDescs();
		this.resortDescs();
	}

	public void resortDescs() {
		Collections.sort(this.descs, sort);
	}

	/**
	 * Get the descriptors this table should be showing. If there is no current
	 * file selected, it returns the empty iterator. If the 'showInvalid' field
	 * is set, it returns all descriptors of the appropriate type. If it is only
	 * showing valid descriptors, it returns all descriptors that have the valid
	 * bit set at the currently displayed frame (the major moment).
	 * 
	 * @return all descriptors that the table should display
	 */
	private Iterator whatShouldBeShowing() {
		ViperViewMediator mediator = getMediator();
		Sourcefile sf = mediator == null ? null : mediator.getCurrFile();
		if (sf == null) {
			return Collections.EMPTY_LIST.iterator();
		} else if (mediator.isShowingInvalid() || cfg.getDescType() == Config.FILE) {
			return sf.getDescriptorsBy(cfg);
		} else {
			Instant now = getMediator().getMajorMoment();
			return sf.getDescriptorsBy(cfg, now);
		}
	}

	/**
	 * Makes sure that the currently displayed list of descriptors is in sync
	 * with the background. This is like telling Windows Explorer to 'Refresh'
	 * (F5). Right now, this must be called manually, for all changes to what
	 * should be displayed.
	 * 
	 * @return <code>true</code> if the table should reset its column widths
	 */
	public boolean resetDescs() {
		ViperViewMediator mediator = getMediator();
		Sourcefile sf = mediator == null ? null : mediator.getCurrFile();
		Descriptor oldDesc = (descs.size() > 0)
				? (Descriptor) descs.get(0)
				: null;
		if (sf == null) {
			// No sourcefile selected; display empty
			this.descs.clear();
		} else if (oldDesc != null && !sf.equals(oldDesc.getParent())) {
			// switched sourcefile; reload all descriptors
			this.descs.clear();
			Iterator iter = whatShouldBeShowing();
			while (iter.hasNext()) {
				Object o = iter.next();
				descs.add(o);
			}
			resortDescs();
			return false;
		} else {
			// Adds the new descriptors to the end of the list,
			// removing all descriptors that have been removed
			// from the current file. This follows the windows
			// explorer approach to sorting files, where the sort
			// only applies to files that existed at the time of
			// the sort, and new files are added at the bottom.
			Iterator iter = whatShouldBeShowing();
			Set shownAlready = new HashSet();
			shownAlready.addAll(descs); // get Descs (a List) as a Set
			while (iter.hasNext()) {
				Object o = iter.next();
				boolean found = shownAlready.remove(o);
				if (!found) {
					descs.add(o);
				}
			}
			descs.removeAll(shownAlready);
		}
		return false;
	}

	public abstract ViperViewMediator getMediator();

	public final static int BY_ID = -1;
	public final static int BY_VALID = -2;
	public final static int BY_PROPAGATING = -3;

	private LinkedHashMap sortOrder;
	private List descs;
	private Config cfg;

	private Comparator sort = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (o1.equals(o2)) {
				return 0;
			}
			Descriptor a = (Descriptor) o1;
			Descriptor b = (Descriptor) o2;
			Iterator iter = sortOrder.keySet().iterator();
			Integer[] order = new Integer[sortOrder.size()];
			int countDown = order.length;
			while (iter.hasNext()) {
				order[--countDown] = (Integer) iter.next();
			}
			for (int i = 0; i < order.length; i++) {
				Integer p = order[i];
				Boolean ascending = (Boolean) sortOrder.get(p);
				int c = compareBy(a, b, p.intValue(), ascending.booleanValue());
				if (c != 0) {
					return c;
				}
			}
			return 0;
		}
		public int compareBy(Descriptor a, Descriptor b, int by,
				boolean ascending) {
			int score = 0;
			Instant now = getMediator().getMajorMoment();
			switch (by) {
				case BY_ID :
					score = a.getDescId() - b.getDescId();
					break;
				case BY_VALID :
					boolean aValid = a.getRange().contains(now);
					boolean bValid = b.getRange().contains(now);
					if (aValid == bValid) {
						score = 0;
					} else if (aValid) {
						score = 1;
					} else {
						score = -1;
					}
					break;
				case BY_PROPAGATING :
					PropagateInterpolateModule p = getMediator()
							.getPropagator();
					boolean aProp = p.isPropagatingThis(a);
					boolean bProp = p.isPropagatingThis(b);
					if (aProp == bProp) {
						score = 0;
					} else if (aProp) {
						score = 1;
					} else {
						score = -1;
					}
					break;
				default :
					// sort by attribute
					Attribute aAt = (Attribute) a.getChild(by);
					Object aVal = aAt.getAttrValueAtInstant(now);
					Attribute bAt = (Attribute) b.getChild(by);
					Object bVal = bAt.getAttrValueAtInstant(now);
					String type = aAt.getAttrConfig().getAttrType();
					if (aVal == bVal) {
						score = 0;
					} else if (aVal == null) {
						score = 1;
					} else if (bVal == null) {
						score = -1;
					} else if (ViperSorters.getCmpFor(type) != null) {
						score = ViperSorters.getCmpFor(type)
								.compare(aVal, bVal);
					} else if (aVal instanceof Comparable) {
						Comparable aCmp = (Comparable) aVal;
						score = aCmp.compareTo(bVal);
					} else {
						score = 0;
					}
			}
			return ascending ? score : -score;
		}
	};

	public Config getConfig() {
		return cfg;
	}
	protected void setConfig(Config cfg) {
		this.cfg = cfg;
	}

	public int getColumnForAttribute(Attribute a) {
		AttrConfig ac = a.getAttrConfig();
		Node config = ac.getParent();
		int internalCol = config.indexOf(ac);
		return getExternalColumn(internalCol);
	}

	public Class getColumnClass(int idx) {
		int icol = getInternalColumn(idx);
		switch (icol) {
			case BY_ID :
				return Descriptor.class;
			case BY_PROPAGATING :
			case BY_VALID :
				return Boolean.class;
			default :
				return Attribute.class;
		}
	}
	public String getColumnName(int idx) {
		int icol = getInternalColumn(idx);
		switch (icol) {
			case BY_ID :
				return "ID";
			case BY_PROPAGATING :
				return "P";
			case BY_VALID :
				return "V";
			default :
				AttrConfig ac = (AttrConfig) cfg.getChild(icol);
				if (ac.isDynamic()) {
					return ac.getAttrName();
				} else {
					return "*" + ac.getAttrName();
				}
		}
	}

	public void sortBy(int col) {
		Integer by = new Integer(getInternalColumn(col));
		boolean asc = true;
		if (sortOrder.containsKey(by)) {
			Boolean b = (Boolean) sortOrder.remove(by);
			asc = !b.booleanValue();
		}
		sortOrder.put(by, Boolean.valueOf(asc));
		this.resortDescs();
	}

	public int getColumnCount() {
		if (null == getConfig()) {
			return 0;
		}
		return 3 + cfg.getNumberOfChildren() - ignored.size();
	}
	int getInternalColumn(int externalColumn) {
		for (int i = -3, atCount = cfg.getNumberOfChildren(); i < atCount; i++) {
			if (!ignored.contains(new Integer(i))) {
				if (externalColumn == 0) {
					return i;
				} else {
					externalColumn--;
				}
			}
		}
		return externalColumn;
	}
	int getExternalColumn(int internalColumn) {
		int offset = 0;
		for (int i = -3; i < internalColumn && i < 0; i++) {
			if (!ignored.contains(new Integer(i))) {
				offset++;
			}
		}
		if (internalColumn < 0) {
			return offset;
		} else {
			return offset + internalColumn;
		}
	}
	public int getRowCount() {
		return descs.size();
	}
	public Object getValueAt(int row, int col) {
		Descriptor d = getDescriptorAtRow(row);
		int icol = getInternalColumn(col);
		switch (icol) {
			case BY_ID :
				return d;
			case BY_VALID :
				return new Boolean(getMediator().isThisValidNow(d));
			case BY_PROPAGATING :
				return new Boolean(getMediator().getPropagator()
						.isPropagatingThis(d));
			default :
				return d.getChild(icol);
		}
	}
	public Sourcefile getCurrFile() {
		return getMediator().getCurrFile();
	}
	public Descriptor getDescriptorAtRow(int row) {
		if (row == -1) {
			return null;
		}
		return (Descriptor) descs.get(row);
	}
	public AttrConfig getAttributeForColumn(int col) {
		int icol = getInternalColumn(col);
		if (icol < 0 || icol >= getConfig().getNumberOfChildren()) {
			return null;
		} else {
			return (AttrConfig) getConfig().getChild(icol);
		}
	}
	public int getRowForDescriptor(Descriptor d) {
		return descs.indexOf(d);
		//		return Collections.binarySearch(descs, d, sort); May be unsorted
	}

	public boolean isCellEditable(int row, int col) {
		PrefsManager prefs = getMediator().getPrefs();
		int icol = getInternalColumn(col);
		if (icol == BY_PROPAGATING || icol == BY_VALID) {
			return true;
		} else if (null != prefs && icol != BY_ID) {
			Attribute a = (Attribute) getValueAt(row, col);
			AttrConfig ac = a.getAttrConfig();
			prefs.model.enterCriticalSection(ModelLock.READ);
			try {
				Resource r = prefs.model.getResource(ac.getAttrType());
				if (null != r) {
					return r.hasProperty(PROPS.editor);
				}
			} finally {
				prefs.model.leaveCriticalSection();
			}
		}
		return false;
	}

	public void setValueAt(Object val, int row, int col) {
		int icol = getInternalColumn(col);
		Descriptor d;
		if (icol == BY_PROPAGATING) {
			d = getDescriptorAtRow(row);
			PropagateInterpolateModule p = getMediator().getPropagator();
			if (((Boolean) val).booleanValue()) {
				p.startPropagating(d);
			} else {
				p.stopPropagating(d);
			}
		} else if (icol == BY_VALID) {
			d = getDescriptorAtRow(row);
			Instant now = getMediator().getCurrentFrame();
			InstantRange r = (InstantRange) d.getValidRange().clone();
			if (((Boolean) val).booleanValue()) {
				r.add(now, now.next());
			} else {
				r.remove(now, now.next());
			}
			d.setValidRange(r);
		} else {
			PrefsManager prefs = getMediator().getPrefs();
			if (null != prefs && col > 0) {
				getMediator().setAttributeValueAtCurrentFrame(val,
						(Attribute) getValueAt(row, col));
			}
		}
	}

	public void removeSortBy(int col) {
		sortOrder.remove(new Integer(col));
	}

	private Set ignored = new HashSet();
	public void setDisplayOfColumn(int col, boolean b) {
		if (b) {
			ignored.remove(new Integer(col));
		} else {
			ignored.add(new Integer(col));
		}
	}
	public boolean getDisplayOfColumn(int col) {
		return !ignored.contains(new Integer(col));
	}
}