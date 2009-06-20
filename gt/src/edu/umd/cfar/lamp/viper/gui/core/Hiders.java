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

import java.util.*;

import javax.swing.event.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

class Hiders extends AbstractViperSelection implements NodeVisibilityManager {
	private static final int DEFAULT_HIDING_TYPE = NodeVisibilityManager.VISIBLE;
	private static Integer[] V = new Integer[] {new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4)};
	private Map lockedThings;
	private int[] hidingTypes;
	private Set hidingConfigs;
	private Set descs;
	private Set atCfgs;
	private Set ats;
	Hiders() {
		lockedThings = new HashMap();
		hidingTypes = new int[0];
		hidingConfigs = new HashSet();
		descs = new HashSet();
		atCfgs = new HashSet();
		ats = new HashSet();
	}

	private int helpGetVisibilityOf(Object o) {
		Integer i = (Integer) lockedThings.get(o);
		if (i == null) {
			return DEFAULT_HIDING_TYPE;
		}
		return i.intValue();
	}
	public int getTypeVisibility(int type) {
		return helpGetVisibilityOf(V[type]);
	}
	public int getConfigVisibility(Config c) {
		return Math.min(getTypeVisibility(c.getDescType()), helpGetVisibilityOf(c));
	}
	public int getDescriptorVisibility(Descriptor d) {
		return Math.min(getConfigVisibility(d.getConfig()), helpGetVisibilityOf(d));
	}
	public int getAttrConfigVisibility(AttrConfig ac) {
		return Math.min(getConfigVisibility((Config) ac.getParent()), helpGetVisibilityOf(ac));
	}
	public int getAttributeVisibility(Attribute a) {
		int min1 = Math.min(getDescriptorVisibility((Descriptor) a.getParent()), helpGetVisibilityOf(a));
		return Math.min(min1, getAttrConfigVisibility(a.getAttrConfig()));
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean isSelected(Node n) {
		// gee, don't you wish you had dynamic dispatch now?
		int visibility = VISIBLE;
		if (n instanceof Config) {
			visibility = getConfigVisibility((Config) n);
		} else if (n instanceof Descriptor) {
			visibility = getDescriptorVisibility((Descriptor) n);
		} else if (n instanceof Attribute) {
			visibility = getAttributeVisibility((Attribute) n);
		} else if (n instanceof AttrConfig) {
			visibility = getAttrConfigVisibility((AttrConfig) n);
		}
		return visibility == VISIBLE || visibility == RANGE_LOCKED;
	}

	/** @inheritDoc  */
	public boolean setVisibilityByType(int type, int visible) {
		Integer typeBox = V[type];
		if (visible == DEFAULT_HIDING_TYPE && lockedThings.containsKey(typeBox)) {
			// Want to make the type visible, and it is currently not
			int nlen = hidingTypes.length-1;
			writeLock();
			try {
				if (nlen == 0) {
					hidingTypes = new int[0];
				} else {
					int[] nt = new int[nlen];
					for (int i = 0, c = 0; i < hidingTypes.length; i++) {
						if (hidingTypes[i] != type) {
							nt[c++] = hidingTypes[i];
						}
					}
					hidingTypes = nt;
				}
				fireChangeEvent(new ChangeEvent(this));
			} finally {
				writeUnlock();
			}
			return true;
		} else if (visible != DEFAULT_HIDING_TYPE) {
			Integer oldVisibilityBox = (Integer) lockedThings.get(typeBox);
			int oldVisibility = oldVisibilityBox == null ? DEFAULT_HIDING_TYPE : oldVisibilityBox.intValue();
			if (oldVisibility != visible) {
				// Want to make the type locked/invisible, and it is currently not the appropriate type of visibility
				try {
					if (oldVisibility == DEFAULT_HIDING_TYPE) {
						int nlen = hidingTypes.length+1;
						if (nlen == 1) {
							hidingTypes = new int[] {type};
						} else {
							int[] nt = new int[nlen];
							System.arraycopy(hidingTypes, 0, nt, 0, hidingTypes.length);
							nt[hidingTypes.length] = type;
							hidingTypes = nt;
						}
					}
					lockedThings.put(typeBox, V[visible]);
					fireChangeEvent(new ChangeEvent(this));
				} finally {
					writeUnlock();
				}
				return true;
			}
		}
		return false;
	}
	private boolean helpSetV(Set s, Node n, int visible) {
		boolean changed = false;
		writeLock();
		try {
			if (visible == DEFAULT_HIDING_TYPE) {
				changed = s.remove(n);
				lockedThings.remove(n);
			} else {
				s.add(n);
				Object oldVisible = lockedThings.get(n);
				Integer newVisible = V[visible];
				changed = !newVisible.equals(oldVisible);
				lockedThings.put(n, newVisible);
			}
			if (changed) {
				fireChangeEvent(new ChangeEvent(this));
			}
		} finally {
			writeUnlock();
		}
		return changed;
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean setVisibilityByConfig(Config cfg, int visible) {
		return helpSetV(hidingConfigs, cfg, visible);
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean setVisibilityByDescriptor(Descriptor desc, int visible) {
		return helpSetV(descs, desc, visible);
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean setVisibilityByAttrConfig(AttrConfig acfg, int visible) {
		return helpSetV(atCfgs, acfg, visible);
	}
	
	/** @inheritDoc */
	public boolean setVisibilityByAttribute(Attribute a, int visible) {
		return helpSetV(ats, a, visible);
	}

	
	/** @inheritDoc */
	public boolean showAll() {
		if (lockedThings.isEmpty()) {
			return false;
		}
		writeLock();
		try {
			if (hidingTypes.length > 0) {
				hidingTypes = new int[0];
			}
			lockedThings.clear();
			hidingConfigs.clear();
			descs.clear();
			atCfgs.clear();
			ats.clear();
			fireChangeEvent(new ChangeEvent(this));
		} finally {
			writeUnlock();
		}
		return true;
	}
	
	/**
	 * @inheritDoc
	 */
	public int[] getHidingTypes() {
		int[] r = new int[hidingTypes.length];
		System.arraycopy(hidingTypes, 0, r, 0, r.length);
		return r;
	}
	
	/**
	 * @inheritDoc
	 */
	public Config[] getHidingConfigs() {
		Config[] c = new Config[hidingConfigs.size()];
		return (Config[]) hidingConfigs.toArray(c);
	}
	
	/**
	 * @inheritDoc
	 */
	public AttrConfig[] getHidingAttrConfigs() {
		AttrConfig[] c = new AttrConfig[atCfgs.size()];
		return (AttrConfig[]) atCfgs.toArray(c);
	}
	
	/**
	 * @inheritDoc
	 */
	public Descriptor[] getHidingDescriptors() {
		Descriptor[] c = new Descriptor[descs.size()];
		return (Descriptor[]) descs.toArray(c);
	}
	
	/**
	 * @inheritDoc
	 */
	public Attribute[] getHidingAttributes() {
		Attribute[] c = new Attribute[ats.size()];
		return (Attribute[]) ats.toArray(c);
	}

	/** @inheritDoc */
	public boolean isEmpty() {
		return lockedThings.isEmpty();
	}

	public Object clone() {
			Hiders copy = (Hiders) super.clone();
			copy.atCfgs = new HashSet();
			copy.atCfgs.addAll(atCfgs);
			copy.ats = new HashSet();
			copy.ats.addAll(ats);
			copy.descs = new HashSet();
			copy.descs.addAll(descs);
			copy.hidingConfigs = new HashSet();
			copy.hidingConfigs.addAll(hidingConfigs);
			copy.hidingTypes = new int[hidingTypes.length];
			System.arraycopy(hidingTypes, 0, copy.hidingTypes, 0, hidingTypes.length);
			copy.lockedThings = new HashMap();
			copy.lockedThings.putAll(lockedThings);
			return copy;
	}
	
	
}
