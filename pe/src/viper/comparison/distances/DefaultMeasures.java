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

/*
 * DefaultMeasures.java
 *
 * Created on May 6, 2002, 4:50 PM
 */

package viper.comparison.distances;

import java.util.*;

import viper.descriptors.attributes.*;

/**
 * Stores the default measure information for each 
 * attribute type. These will be used when the user
 * requests an evaluation on a type, but doesn't specify
 * the metric and/or tolerance.
 */
public final class DefaultMeasures {
	private static class DefaultInformation {
		double tolerance;
		String metric;
		String compositionType;
	}

	private static HashMap defaults = new HashMap();

	/**
	 * Gets the default tolerance for the attribute type.
	 * @param attrType the data type
	 * @return the tolerance, or not-a-number, if none exists
	 */
	public static double getDefaultToleranceFor(String attrType) {
		DefaultInformation defs = (DefaultInformation) defaults.get(attrType);
		if (defs != null) {
			return defs.tolerance;
		} else {
			return Double.NaN;
		}
	}
	
	/**
	 * Gets the current tolerance setting for the attribute type. 
	 * It doesn't really make sense to change this without
	 * changing the metric, as well.
	 * @param attr the attribute
	 * @return the tolerance for descriptor localiation
	 */
	public static double getDefaultToleranceFor(AbstractAttribute attr) {
		return getDefaultToleranceFor(attr.getType());
	}
	
	/**
	 * Gets the current tolerance setting for the attribute type. 
	 * It doesn't really make sense to change this without
	 * changing the metric, as well.
	 * @param attrType the attribute data type
	 * @param val the new default localization tolerance for the 
	 * attribute
	 */
	public static void setDefaultToleranceFor(String attrType, double val) {
		Distances.check(attrType);
		DefaultInformation defs = (DefaultInformation) defaults.get(attrType);
		if (defs != null) {
			defs.tolerance = val;
		} else {
			defs = new DefaultInformation();
			defs.tolerance = val;
			defaults.put(attrType, defs);
		}
	}
	
	/**
	 * Gets the current tolerance setting for the attribute type. 
	 * It doesn't really make sense to change this without
	 * changing the metric, as well.
	 * @param attr the attribute data type
	 * @param val the new default localization tolerance for the 
	 * attribute
	 */
	public static void setDefaultToleranceFor(AbstractAttribute attr, double val) {
		setDefaultToleranceFor(attr.getType(), val);
	}

	/**
	 * Sets the default metric for the given attribute data type.
	 * @param attrType the type of data
	 * @param metric the new metric
	 * @throws ImproperMetricException if the metric is not valid
	 * for the given attribute type
	 */
	public static void setDefaultMetricFor(String attrType, String metric)
			throws ImproperMetricException {
		if (!Distances.isDistanceFor(attrType, metric)) {
			throw new ImproperMetricException(metric
					+ " is not a valid metric " + "for " + attrType
					+ " attributes.");
		}
		DefaultInformation defs = (DefaultInformation) defaults.get(attrType);
		if (defs != null) {
			defs.metric = metric.toLowerCase();
		} else {
			defs = new DefaultInformation();
			defs.metric = metric.toLowerCase();
			defaults.put(attrType, defs);
		}
	}
	
	/**
	 * Sets the default metric for the given attribute data type.
	 * @param attr the type of data
	 * @param metric the new metric
	 * @throws ImproperMetricException if the metric is not valid
	 * for the given attribute type
	 */
	public static void setDefaultMetricFor(AbstractAttribute attr, String metric)
			throws ImproperMetricException {
		setDefaultMetricFor(attr.getType(), metric);
	}

	/**
	 * Gets the default metric for the given attribute data type.
	 * @param attrType the type of data
	 * @return the default metric
	 */
	public static String getDefaultMetricFor(String attrType) {
		DefaultInformation defs = (DefaultInformation) defaults.get(attrType);
		if (defs != null) {
			return defs.metric;
		} else {
			return "E";
		}
	}
	
	/**
	 * Gets the default metric for the given attribute data type.
	 * @param attr the type of data
	 * @return the default metric
	 */
	public static String getDefaultMetricFor(AbstractAttribute attr) {
		return getDefaultMetricFor(attr.getType());
	}
}