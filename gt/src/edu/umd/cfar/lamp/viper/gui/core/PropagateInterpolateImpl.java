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
import java.util.logging.*;

import viper.api.*;
import viper.api.impl.*;
import viper.api.impl.Util;
import viper.api.time.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * An implementation of the propagation module.
 */
public class PropagateInterpolateImpl implements PropagateInterpolateModule {

	/**
	 * URI for identifying propagation edit events.
	 */
	public static final String PROPAGATE = ViperParser.IMPL + "Propagate";

	/**
	 * URI for identifying interpolation edit events.
	 */
	public static final String INTERPOLATE = ViperParser.IMPL + "Interpolate";

	private static Logger log = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.core");

	private ViperViewMediator mediator;
	private Set propagatingDescriptors;
	private Set propagatingAttributes;
	/// Both those that should and should not be propagating
	private Set explicitAttributes;
	private Sourcefile oldFile;
	private Instant oldMoment;
	private static Selector INTERPOLATOR_SELECTOR = new Selector() {
		public boolean test(Statement s) {
			return s.getPredicate().equals(GT.interpolator);
		}
		public boolean isSimple() {
			return true;
		}
		public Resource getSubject() {
			return null;
		}
		public Property getPredicate() {
			return GT.interpolator;
		}
		public RDFNode getObject() {
			return null;
		}
	};
	private ModelListener prefsListener = new ModelListener() {
		public void changeEvent(ModelEvent event) {
			initializeInterpolators();
		}

		public Selector getSelector() {
			return INTERPOLATOR_SELECTOR;
		}
	};
	
	private Map INTERPOLATORS = new HashMap();

	/**
	 * Constructs a new p/i module. It stil needs to be connected to a viper
	 * mediator.
	 */
	public PropagateInterpolateImpl() {
		propagatingDescriptors = new HashSet();
		propagatingAttributes = new HashSet();
		explicitAttributes = new HashSet();
		listeners = new HashSet();
	}

	private ViperMediatorChangeListener cvl = new ViperMediatorChangeListener() {
		public void frameChanged(ViperMediatorChangeEvent e) {
			Sourcefile currFile = mediator.getCurrFile();
			Instant currMoment = mediator.getMajorMoment();
			if (oldFile != null && oldFile.equals(currFile)) {
				oldFile = currFile;
				if (oldMoment != null && !oldMoment.equals(currMoment)) {
					Instant m1 = oldMoment;
					Instant m2 = currMoment;
					oldMoment = currMoment;
					if (oldMoment != null) {
						propagate(m1, m2);
					}
				}
			} else {
				// remove all propagators
				oldMoment = currMoment;
				stopPropagating();
			}
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			Sourcefile currFile = mediator.getCurrFile();
			if (currFile == null ? (oldFile != null) : (!currFile
					.equals(oldFile))) {
				oldFile = currFile;
				oldMoment = null;
				stopPropagating();
			}
		}

		public void currFileChanged(ViperMediatorChangeEvent e) {
			stopPropagating();
			oldFile = mediator.getCurrFile();
			oldMoment = mediator.getMajorMoment();
		}

		public void schemaChanged(ViperMediatorChangeEvent e) {
			stopPropagating();
			oldFile = mediator.getCurrFile();
			oldMoment = mediator.getMajorMoment();
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
		}
	};

	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.removeViperMediatorChangeListener(cvl);
			if (this.mediator.getPrefs() != null) {
				this.mediator.getPrefs().removeListener(prefsListener);
			}
		}
		this.mediator = mediator;
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(cvl);
			if (this.mediator.getPrefs() != null) {
				this.mediator.getPrefs().addListener(prefsListener);
			}
			oldFile = this.mediator.getCurrFile();
			oldMoment = this.mediator.getMajorMoment();
		}
		stopPropagating();
		initializeInterpolators();
	}
	

	/**
	 * 
	 */
	private void initializeInterpolators() {
		INTERPOLATORS.clear();
		if (mediator == null) {
			return;
		}
		StmtIterator iter = mediator.getPrefs().model.listStatements(null, GT.interpolator, (String) null);
		while (iter.hasNext()) {
			Statement curr = iter.nextStatement();
			Resource dataType = curr.getSubject();
			try {
				Object interpolator = mediator.getPrefs().getCore().rdfNodeToValue(curr.getObject());
				INTERPOLATORS.put(dataType.toString(), interpolator);
			} catch (PreferenceException e) {
				log.log(Level.SEVERE, "Error while loading interpolator for " + dataType, e);
			}
		}
	}


	/** @inheritDoc */
	public boolean isPropagatingThis(Descriptor d) {
		return propagatingDescriptors.contains(d);
	}

	/** @inheritDoc */
	public Set getPropagatingDescriptors() {
		return Collections.unmodifiableSet(propagatingDescriptors);
	}

	/** @inheritDoc */
	public void startPropagating(Descriptor desc) {
		if (!propagatingDescriptors.contains(desc)) {
			propagatingDescriptors.add(desc);
			fireListChangedEvent();
		}
	}

	/** @inheritDoc */
	public void stopPropagating() {
		if (!propagatingAttributes.isEmpty()
				|| !propagatingDescriptors.isEmpty()) {
			propagatingAttributes.clear();
			explicitAttributes.clear();
			propagatingDescriptors.clear();
			fireListChangedEvent();
		}
	}

	/** @inheritDoc */
	public void stopPropagating(Descriptor desc) {
		if (propagatingDescriptors.contains(desc)) {
			propagatingDescriptors.remove(desc);
			fireListChangedEvent();
		}
	}

	/**
	 * Get the interpolation function object for the given data type.
	 * 
	 * @param type
	 *            the data type
	 * @return the interpolation object associated with the type
	 */
	public Interpolator getInterpolatorForType(String type) {
		return (Interpolator) INTERPOLATORS.get(type);
	}

	private void helpInterpolateAttribute(Attribute attribute, Instant start, Span s,
			Interpolator magic) throws InterpolationException {
		AttrConfig ac = attribute.getAttrConfig();
		FrameRate rate = mediator.getCurrFile().getReferenceMedia()
				.getFrameRate();
		Frame begin = rate.asFrame(s.getStartInstant());
		Frame end = rate.asFrame(s.getEndInstant());
		int size = end.intValue() - begin.intValue() - 1;
		if (ac.isDynamic() && (size > 0)) {
			if (magic == null) {
				if (size == 1) {
					helpPropagateAttribute(attribute, start, s);
				} else {
					Frame middle = new Frame((int) ((end.longValue() + begin
							.longValue()) / 2));
					Span firstHalf = new Span(begin, middle);
					Span secondHalf = new Span(middle, end);
					assert !firstHalf.isEmpty();
					assert !secondHalf.isEmpty();
					helpPropagateAttribute(attribute, begin, firstHalf);
					helpPropagateAttribute(attribute, end, secondHalf);
				}
			} else {
				Object sVal = attribute.getAttrValueAtInstant(begin);
				Object eVal = attribute.getAttrValueAtInstant(end);
				if (sVal == null || eVal == null) {
					helpInterpolateAttribute(attribute, start, s, null);
				} else {
					DisplayWithRespectToManager dwrt = mediator
							.getDisplayWRTManager();
					Attribute wrt = dwrt.getAttribute();
					if (attribute.equals(wrt)) {
						wrt = null;
					}
					if (wrt != null && (sVal instanceof Moveable)) {
						Pnt sWrt = dwrt.getSmoothedAttrValueAtInstant(begin);
						sVal = ((Moveable) sVal).shift(-sWrt.x.intValue(),
								-sWrt.y.intValue());
						Pnt eWrt = dwrt.getSmoothedAttrValueAtInstant(end);
						eVal = ((Moveable) eVal).shift(-eWrt.x.intValue(),
								-eWrt.y.intValue());
					}
					Object[] bookends = new Object[]{sVal, eVal};
					ArbitraryIndexList vals = magic.interpolate(bookends,
							new long[]{size}, null);

					//attribute.startAggregating();
					for (Iterator i = vals.iterator(); i.hasNext();) {
						DynamicValue dval = (DynamicValue) i.next();
						int startLong = ((Long) dval.getStart()).intValue()
								+ begin.intValue();
						int endLong = ((Long) dval.getEnd()).intValue()
								+ begin.intValue();

						Span ns = new Span(new Frame(startLong), new Frame(
								endLong));
						if (wrt != null && (sVal instanceof Moveable)
								&& (dval.getValue() instanceof Moveable)) {
							Iterator j = dwrt.getAttribute()
									.getAttrValuesOverSpan(s);
							SortedSet changes = new TreeSet();
							changes.add(ns.getStart());
							changes.add(ns.getEnd());
							while (j.hasNext()) {
								DynamicValue dvForJ = (DynamicValue) j.next();
								changes.add(dvForJ.getStart());
								changes.add(dvForJ.getEnd());
							}
							Iterator changeIter = changes.iterator();
							Frame s1 = (Frame) changeIter.next();
							do {
								Pnt sWrt = dwrt
										.getSmoothedAttrValueAtInstant(s1);
								Moveable nval = (Moveable) dval.getValue();
								nval = nval.shift(sWrt.x.intValue(), sWrt.y
										.intValue());
								attribute.aggregateSetAttrValueAtSpan(nval, ns);
							} while (changeIter.hasNext());
						} else {
							attribute.aggregateSetAttrValueAtSpan(dval.getValue(), ns);
						}
					}
					//attribute.finishAggregating(true);
				}
			}
		}
	}

	private void helpPropagateAttribute(Attribute a, Instant start, Span s) {
		if (!mediator.getHiders().isSelected(a)) {
			return;
		}
		AttrConfig ac = a.getAttrConfig();
		if (ac.isDynamic()) {
			Object val = a.getAttrValueAtInstant(start);
			DisplayWithRespectToManager dwrt = mediator.getDisplayWRTManager();
			Attribute wrt = dwrt.getAttribute();
			if (a.equals(wrt)) {
				wrt = null;
			}
			if (wrt != null && (val instanceof Moveable)) {
				Iterator iter = wrt.getAttrValuesOverSpan(s);
				Pnt p = dwrt.getSmoothedAttrValueAtInstant(start);
				Moveable mVal = (Moveable) val;
				while (iter.hasNext()) {
					DynamicAttributeValue v = (DynamicAttributeValue) iter
							.next();
					Pnt p2 = ((HasCentroid) v.getValue()).getCentroid();
					Rational x = new Rational();
					Rational y = new Rational();
					Rational.minus(p2.getX(), p.getX(), x);
					Rational.minus(p2.getY(), p.getY(), y);
					a.setAttrValueAtSpan(
							mVal.shift(x.intValue(), y.intValue()), v);
				}
			} else {
				a.setAttrValueAtSpan(val, s);
			}
		}
	}

	private void helpPropagateDescriptor(Descriptor d, Instant start, Span s) {
		if (mediator.getHiders().getDescriptorVisibility(d) == NodeVisibilityManager.RANGE_LOCKED) {
			helpPropagateChildAttributes(d, start, s);
			return;
		}
		if (!mediator.getHiders().isSelected(d)) {
			return;
		}
		InstantRange oldRange = (InstantRange) d.getValidRange().clone();
		Iterator selectedRange = Collections.singleton(s).iterator();
		
		if (mediator.getChronicleSelectionModel() != null) {
			TemporalRange selectedTime = mediator.getChronicleSelectionModel().getSelectedTime();
			if (selectedTime != null) {
				selectedRange = selectedTime.iterator(s);
				if (!selectedRange.hasNext()) {
					return;
				}
				if (!selectedTime.contains(start)) {
					if (start.equals(s.getStart())) {
						// going to the right
						start = (Instant) oldRange.firstAfter(start);
						if (start == null) {
							return;
						}
						s = (Span) s.change(start, s.getEnd());
					} else {
						// going to the left
						Instant beforeStart = (Instant) oldRange.firstBefore(start);
						if (beforeStart == null) {
							return;
						}
						Instant endOfReplacement = (Instant) oldRange.endOf(beforeStart);
						start = (Instant) endOfReplacement.previous();
						s = (Span) s.change(s.getStart(), endOfReplacement);
						// Since the iterator 'selectedRange' is non-empty, 
						// we know this element must exist.
					}
					if (s.isEmpty()) {
						return;
					}
				}
			}
		}

		if (oldRange.contains(start)) {
			if (!oldRange.contains(s)) {
				while (selectedRange.hasNext()) {
					oldRange.add(selectedRange.next());
				}
				d.setValidRange(oldRange);
			}
			helpPropagateChildAttributes(d, start, s);
		} else if (!oldRange.contains(start)) {
			while (selectedRange.hasNext()) {
				oldRange.remove(selectedRange.next());
			}
			d.setValidRange(oldRange);
		}
	}

	/**
	 * @param d
	 * @param start
	 * @param s
	 */
	private void helpPropagateChildAttributes(Descriptor d, Instant start, Span s) {
		if (d.getDescType() == Config.OBJECT) {
			Iterator iter = d.getAttributes();
			while (iter.hasNext()) {
				Attribute a = (Attribute) iter.next();
				if (!explicitAttributes.contains(a)) {
					helpPropagateAttribute(a, start, s);
				}
			}
		}
	}


	/** @inheritDoc */
	public void interpolate(Instant start, Instant end) {
		if (!propagatingDescriptors.isEmpty()) {
			interpolateDescriptors(propagatingDescriptors.iterator(), start,
					end);
		}
	}

	/** @inheritDoc */
	public void propagate(final Instant start, final Instant end) {
		if (propagatingDescriptors.isEmpty() && propagatingAttributes.isEmpty()) {
			return;
		}
		ViperData v = mediator.getViperData();
		Sourcefiles sf = v.getSourcefilesNode();
		Runnable propagate = new Runnable() {
			public void run() {
				propagateDescriptors(propagatingDescriptors.iterator(), start,
						end);
				propagateAttributes(propagatingAttributes.iterator(), start,
						end);
			}
		};
		Object[] tprops = new Object[]{"start", start, "end", end};
		Util.tryTransaction(propagate, sf, PROPAGATE, tprops);
	}

	/** @inheritDoc */
	public void propagateDescriptors(final Iterator descs, final Instant start,
			final Instant end) {
		assert start != null;
		assert end != null;
		if (!descs.hasNext()) {
			return;
		}
		ViperData v = mediator.getViperData();
		Sourcefiles sf = v.getSourcefilesNode();
		Runnable propagate = new Runnable() {
			public void run() {
				Span s = startStop2Interval(start, end);
				s = new Span(s.getStartInstant(), (Instant) s.getEndInstant()
						.next());
				while (descs.hasNext()) {
					Descriptor d = (Descriptor) descs.next();
					helpPropagateDescriptor(d, start, s);
				}
			}
		};
		Object[] tprops = new Object[]{"start", start, "end", end};
		Util.tryTransaction(propagate, sf, PROPAGATE, tprops);
	}

	/** @inheritDoc */
	public void propagateAttributes(final Iterator ats, final Instant start,
			final Instant end) {
		Sourcefiles sf = mediator.getViperData().getSourcefilesNode();
		Runnable propagate = new Runnable() {
			public void run() {
				Span s = startStop2Interval(start, end);
				while (ats.hasNext()) {
					Attribute a = (Attribute) ats.next();
					helpPropagateAttribute(a, start, s);
				}
			}
		};
		Object[] tprops = new Object[]{"start", start, "end", end};
		Util.tryTransaction(propagate, sf, PROPAGATE, tprops);
	}

	/** @inheritDoc */
	public void interpolateDescriptors(final Iterator descs,
			final Instant start, final Instant stop) {
		//Sourcefiles sf = mediator.getViperData().getSourcefilesNode();
		//Runnable interpolate = new Runnable() {
			//public void run() {
				Span newSpan = startStop2Interval(start, stop);
				while (descs.hasNext()) {
					Descriptor d = (Descriptor) descs.next();
					InstantRange newDescriptorRange = d.getValidRange();
					if (mediator.getHiders().getDescriptorVisibility(d) > NodeVisibilityManager.RANGE_LOCKED) {
						Iterator selectedRange = Collections.singleton(newSpan).iterator();
						if (mediator.getChronicleSelectionModel() != null) {
							if (mediator.getChronicleSelectionModel().getSelectedTime() != null) {
								selectedRange = mediator.getChronicleSelectionModel().getSelectedTime().iterator(newSpan);
							}
						}
						if (!newDescriptorRange.contains(newSpan)) {
							while(selectedRange.hasNext()) {
								newDescriptorRange.add(selectedRange.next());
							}
							d.setValidRange(newDescriptorRange);
						}
					}
					Iterator ats = d.getAttributes();
					while (ats.hasNext()) {
						Attribute a = (Attribute) ats.next();
						Interpolator magic = getInterpolatorForType(a
								.getAttrConfig().getAttrType());
						a.startAggregating();
						try {
							helpInterpolateAttribute(a, start, newSpan, magic);
						} catch (InterpolationException e) {
							log
									.severe("Error while interpolating descriptors: "
											+ e.getLocalizedMessage());
							throw new RuntimeException(e);
						} finally {
							a.finishAggregating(true);
						}
					}
				}
//			}
//		};
//		Object[] tprops = new Object[]{"start", start, "stop", stop};
//		Util.tryTransaction(interpolate, sf, INTERPOLATE, tprops);
	}


	/** @inheritDoc */
	public void interpolateAttributes(final Iterator ats, final Instant start,
			final Instant stop) {
		Sourcefiles sf = mediator.getViperData().getSourcefilesNode();
		Runnable interpolate = new Runnable() {
			public void run() {
				Span s = startStop2Interval(start, stop);
				while (ats.hasNext()) {
					Attribute a = (Attribute) ats.next();
					Interpolator magic = getInterpolatorForType(a
							.getAttrConfig().getAttrType());
					try {
						helpInterpolateAttribute(a, start, s, magic);
					} catch (InterpolationException e) {
						log.severe("Error while interpolating attributes: "
								+ e.getLocalizedMessage());
						throw new RuntimeException(e);
					}
				}
			}
		};
		Object[] tprops = new Object[]{"start", start, "stop", stop};
		Util.tryTransaction(interpolate, sf, INTERPOLATE, tprops);
	}

	private Span startStop2Interval(Instant start, Instant stop) {
		if (start.compareTo(stop) < 0) {
			return new Span(start, stop);
			// don't have to set to start.next, since setting something
			// to its old value isn't a bad thing
		} else {
			return new Span(stop, start);
		}
	}

	private void fireListChangedEvent() {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			PropagateListener pl = (PropagateListener) iter.next();
			pl.listChanged();
		}
	}

	// XXX-davidm: replace this set with an event listener list
	private Set listeners;

	/** @inheritDoc */
	public void addPropagateListener(PropagateListener pl) {
		listeners.add(pl);
	}

	/** @inheritDoc */
	public void removePropagateListener(PropagateListener pl) {
		listeners.remove(pl);
	}

	/**
	 * Stops the attribute from propagating, even if its descriptor is
	 * propagating.
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.core.PropagateInterpolateModule#startPropagating(viper.api.Attribute)
	 */
	public void startPropagating(Attribute attr) {
		if (!propagatingDescriptors.contains(attr.getParent())) {
			propagatingAttributes.add(attr);
			explicitAttributes.add(attr);
		} else {
			explicitAttributes.remove(attr);
		}
	}

	/**
	 * Starts the attribute propagating, even if its descriptor is not
	 * propagating.
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.core.PropagateInterpolateModule#stopPropagating(viper.api.Attribute)
	 */
	public void stopPropagating(Attribute attr) {
		propagatingAttributes.remove(attr);
		if (propagatingDescriptors.contains(attr.getParent())) {
			explicitAttributes.add(attr);
		}
	}
}