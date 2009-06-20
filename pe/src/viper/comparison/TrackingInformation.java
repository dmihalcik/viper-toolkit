/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.comparison;

import java.util.*;

import viper.comparison.distances.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Collects scores for the tracking evaluatoin type
 */
public class TrackingInformation implements Evaluation.Information {
	private List objectIds;
	private boolean overlaps = false;

	/**
	 * Create a new tracking information object with the given default id.
	 * 
	 * @param ID
	 *            the descriptor id to associate with the tracking information
	 */
	public TrackingInformation(Object ID) {
		objectIds = new LinkedList();
		objectIds.add(ID);
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasInformation() {
		return objectIds != null && objectIds.size() > 0;
	}

	/**
	 * @inheritDoc
	 */
	public String toVerbose() {
		StringBuffer sb = new StringBuffer();
		if (overlaps) {
			if (objectIds.size() == 1) {
				sb.append("For object ").append(objectIds.get(0)).append("\n");
			} else {
				sb.append("For all objects\n");
			}
			for (Iterator names = order.iterator(); names.hasNext();) {
				AttrDistPair curr = (AttrDistPair) names.next();
				sb.append(curr.getDist().getExplanation()).append(": ");
				sb.append(this.getValue(curr) / objectIds.size()).append("\n");
			}
			// FIXME need seperate methods in distance, getAverage and
			// getAverageExplanation
		} else {
			if (objectIds.size() == 1) {
				sb.append("For object ").append(objectIds.get(0)).append(
						", no match was found\n");
			} else {
				sb.append("For all objects, no temporal overlaps were found\n");
			}
			for (Iterator names = order.iterator(); names.hasNext();) {
				AttrDistPair curr = (AttrDistPair) names.next();
				sb.append(curr.getDist().getExplanation()).append(": 0.0\n");
				// FIXME perhaps should add 'get default/no match' method as
				// well?
			}
		}
		return sb.toString();
	}

	/**
	 * @inheritDoc
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (objectIds.size() == 1) {
			sb.append(objectIds.get(0)).append(" ");
		} else {
			sb.append("TOTAL ");
		}
		if (overlaps) {
			for (Iterator names = order.iterator(); names.hasNext();) {
				sb.append(
						this.getValue((AttrDistPair) names.next())
								/ objectIds.size()).append(" ");
			}
		} else {
			for (int i = 0; i < order.size(); i++) {
				sb.append(0.0).append(" ");
			}
		}
		return sb.toString();
	}

	/**
	 * @inheritDoc
	 */
	public String getLayout() {
		if (order.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Iterator names = order.iterator(); names.hasNext();) {
			sb.append(names.next().toString()).append(" ");
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * @inheritDoc
	 */
	public void add(Evaluation.Information other) {
		TrackingInformation ti = (TrackingInformation) other;
		if (ti != null) {
			if (ti.isAllFinite()) {
				if (objectIds.size() > 0 && ti.objectIds.size() > 0) {
					overlaps = overlaps || ti.overlaps;
					objectIds.addAll(ti.objectIds);

					for (Iterator names = (order.size() > ti.order.size()
							? order
							: ti.order).iterator(); names.hasNext();) {
						AttrDistPair key = (AttrDistPair) names.next();
						Double val = smartAdd((Number) this.values.get(key),
								(Number) ti.values.get(key));
						setValue(key, val);
					}
				} else if (ti.objectIds.size() > 0) {
					overlaps = ti.overlaps;
					objectIds.addAll(ti.objectIds);

					order.clear();
					order.addAll(ti.order);
					values.putAll(ti.values);
				}
			}
		}
	}

	private Double smartAdd(Number a, Number b) {
		if (a == null || b == null) {
			if (a == null && b == null) {
				return new Double(0);
			} else {
				return new Double((a == null ? b : a).doubleValue());
			}
		} else if (a instanceof Double) {
			return smartAdd((Double) a, (Double) b);
		} else if (a instanceof Float) {
			return smartAdd((Float) a, (Float) b);
		} else {
			return new Double(a.doubleValue() + b.doubleValue());
		}
	}

	private Double smartAdd(Double a, Double b) {
		if (a.isNaN())
			return b;
		else if (b.isNaN())
			return a;
		else
			return new Double(a.doubleValue() + b.doubleValue());
	}
	private Double smartAdd(Float a, Float b) {
		if (a.isNaN())
			return new Double(b.floatValue());
		else if (b.isNaN())
			return new Double(a.floatValue());
		else
			return new Double(a.doubleValue() + b.doubleValue());
	}

	private List order = new LinkedList();
	
	/**
	 * Sets the order of output.
	 * 
	 * @param order a list of <code>Distance</code> functors
	 */
	public void setOrder(List order) {
		this.order = order;
	}
	
	/**
	 * Gets the order of output.
	 * 
	 * @return a list of <code>Distance</code> functors
	 */
	public List getOrder() {
		return order;
	}

	private static class AttrDistPair extends Pair {
		String getAttr() {
			return (String) getFirst();
		}
		Distance getDist() {
			return (Distance) getSecond();
		}
		AttrDistPair(String attr, Distance d) {
			super(attr, d);
		}
	}

	/** maps AttrDistPairs to Double values */
	private Map values = new HashMap();

	/**
	 * Sets the value of this information
	 * 
	 * @param attrName
	 *            the attribute name
	 * @param metric
	 *            the metric used for tracking
	 * @param value
	 *            the value of the track on the given attribute
	 */
	public void setValue(String attrName, Distance metric, double value) {
		setValue(new AttrDistPair(attrName, metric), new Double(value));
	}

	/**
	 * Sets the value of this information
	 * 
	 * @param attrName
	 *            the attribute name
	 * @param metric
	 *            the metric used for tracking
	 * @param value
	 *            the value of the track on the given attribute
	 */
	public void setValue(String attrName, Distance metric, Number value) {
		setValue(new AttrDistPair(attrName, metric), value);
	}

	/**
	 * Sets the value of this information.
	 * 
	 * @param key the attribute/distance pair
	 * @param value
	 *            the value of the track on the given attribute
	 */
	private void setValue(AttrDistPair key, Number value) {
		overlaps = true;
		if (!values.containsKey(key)) {
			order.add(key);
		}
		values.put(key, value);
	}

	/**
	 * Adds a new metric to the collection.
	 * @param attrName the attribute for the metric
	 * @param metric the distance metric
	 */
	public void addMetric(String attrName, Distance metric) {
		AttrDistPair key = new AttrDistPair(attrName, metric);
		if (!values.containsKey(key)) {
			order.add(key);
		}
	}

	/**
	 * Gets the current value of the metric on the
	 * specified attribute type.
	 * @param attrName the attribute
	 * @param metric the metric
	 * @return the current aggregate value
	 */
	public double getValue(String attrName, Distance metric) {
		return getValue(new AttrDistPair(attrName, metric));
	}

	private double getValue(AttrDistPair key) {
		Object c = values.get(key);
		if (c != null) {
			return ((Double) c).doubleValue();
		} else {
			return 0;
		}
	}

	private boolean isAllFinite() {
		for (Iterator iter = values.values().iterator(); iter.hasNext();) {
			Object curr = iter.next();
			if (curr != null) {
				double c = ((Double) curr).doubleValue();
				if (c >= Double.POSITIVE_INFINITY) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public Map getDatasets(String name) {
		return new HashMap();
	}
}