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

package viper.comparison.distances;

import java.util.*;

import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;
/**
 * Associates distance and metric with a specific type of attribute for a
 * specific descriptor to Object Analysis type evaluation.
 */
public class AttrMeasure {
	private Distance metric = null;
	private double threshold = Double.NaN;
	private int statistic;
	private double statThreshold;

	/**
	 * Gets the statistic type associated with the measure,
	 * e.g. {@link Distances#MEDIAN}.
	 * @return the statistic
	 */
	public int getStatType() {
		return statistic;
	}
	
	/**
	 * Gets the threshold to apply on the given statstic
	 * to count as successfuly statisticked.
	 * @return the statistic threshold, for the value
	 * of appropriate statistic type for the per-frame
	 * distances
	 */
	public double getStatThreshold() {
		return statThreshold;
	}

	/**
	 * Constructs a new measure with the given metric
	 * and localization threshold, with the default
	 * statistic and statistic tolerance.
	 * @param metric the metric
	 * @param tol the localization threshold
	 */
	public AttrMeasure(Distance metric, double tol) {
		this.metric = metric;
		this.threshold = tol;

		statistic = Distances.getDefaultStatistic();
		statThreshold = Distances.getDefaultSTolerance();
	}

	/**
	 * Gets the default measure for the attribute.
	 * 
	 * @param attr
	 *            the attribute type
	 * @throws IllegalStateException
	 *             if the default metric is <code>null</code>
	 */
	public AttrMeasure(String attr) {
		threshold = DefaultMeasures.getDefaultToleranceFor(attr);
		String metricName = DefaultMeasures.getDefaultMetricFor(attr);
		metric = Distances.getDistanceFunctor(attr, metricName);
		if (metric == null) {
			throw new IllegalStateException("Default metric for " + attr
					+ " is null!!!");
		}
		statistic = Distances.getDefaultStatistic();
		statThreshold = Distances.getDefaultSTolerance();
	}

	/**
	 * Constructs a new measure by parsing the measure
	 * in text/epf format. For example: <code>[ metric threshold ]</code>.
	 * @param attr the attribute to apply to
	 * @param st the text to parse
	 * @throws ImproperMetricException if there is a parsing
	 * error or a metric is not found
	 */
	public AttrMeasure(String attr, StringTokenizer st)
			throws ImproperMetricException {
		String S;
		statistic = Distances.getDefaultStatistic();
		statThreshold = Distances.getDefaultSTolerance();
		try {
			S = st.nextToken();
			if (S.equals(":")) {
				// unexpected colon
				S = st.nextToken();
			}
			if (S.charAt(0) == '[') {
				if (S.length() == 1) {
					S = st.nextToken();
				} else {
					S = S.substring(1);
				}
				if (S.charAt(S.length() - 1) == ']') {
					S = S.substring(0, S.length() - 1);
				}

				if (S.length() > 0) {
					if ("-".equals(S)) {
						metric = Distances.getDistanceFunctor(attr,
								DefaultMeasures.getDefaultMetricFor(attr));
					} else if ((Character.isDigit(S.charAt(0)))
							|| ((S.length() > 1) && (S.charAt(0) == '.') && (Character
									.isDigit(S.charAt(1))))) {
						this.threshold = Double.parseDouble(S);
					} else {
						this.metric = Distances.getDistanceFunctor(attr, S);
					}
				}

				S = st.nextToken();
				if (S.charAt(S.length() - 1) == ']') {
					S = S.substring(0, S.length() - 1);
				}

				if (S.length() > 0) {
					if (!S.equals("-")) {
						this.threshold = Double.parseDouble(S);
					} else {
						threshold = DefaultMeasures
								.getDefaultToleranceFor(attr);
					}
				}
			} else {
				throw new ImproperMetricException(
						"Unexpected string (looking for '['): " + S);
			}
		} catch (NoSuchElementException nsex) {
			if (threshold == Double.NaN) {
				threshold = DefaultMeasures.getDefaultToleranceFor(attr);
			}
			if (metric == null) {
				metric = Distances.getDistanceFunctor(attr, DefaultMeasures
						.getDefaultMetricFor(attr));
			}
		}
	}

	/**
	 * Tests to see if this is the same kind of measure as 
	 * the parameter.
	 * @param o the measure to test against
	 * @return true if they have the same metric, statistic,
	 * and localization and statistic thresholds
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o instanceof AttrMeasure) {
			AttrMeasure other = (AttrMeasure) o;
			return metric.equals(other.metric) && threshold == other.threshold
					&& statistic == other.statistic
					&& statThreshold == other.statThreshold;
		} else
			return false;
	}
	
	/**
	 * Computes a hashcode on the metric, statistic, and 
	 * thresholds.
	 * @return a hash code
	 */
	public int hashCode() {
		long v = Double.doubleToLongBits(threshold);
		int ret = (int) (v ^ (v >>> 32));
		ret ^= (metric == null ? 0 : metric.hashCode());
		v = Double.doubleToLongBits(statThreshold);
		ret ^= (int) (v ^ (v >>> 32));
		ret ^= statistic;
		return ret;
	}

	/**
	 * Prints out the metric and threshold, without
	 * the brackets. Threshold is either a number or
	 * the string <q>(INFINITY)</q>
	 * @return <q><i>metric</i> <i>threshold</i></q>
	 */
	public String toString() {
		if (threshold >= Integer.MAX_VALUE) {
			return metric + " (INFINITY)";
		} else {
			return metric + " " + threshold;
		}
	}

	/**
	 * Sets the localization tolerance.
	 * @param tol the new localization tolerance
	 */
	public final void setTolerance(double tol) {
		threshold = tol;
	}
	
	/**
	 * Gets the localization tolerance.
	 * @return the localization tolerance
	 */
	public final double getTolerance() {
		return threshold;
	}

	/**
	 * Sets the metric.
	 * @param M the new metric
	 */
	public final void setMetric(Distance M) {
		metric = M;
	}
	
	/**
	 * Gets the metric.
	 * @return the metric
	 */
	public final Distance getMetric() {
		return metric;
	}

	/**
	 * Determines if the given distance value is less
	 * than the current tolerance. It deals with
	 * the case that the distance may be a score
	 * (larger = better) or a distance (smaller = better).
	 * Although I generally prefer distances, there will
	 * be people who wish to do it the other way.
	 * @param value the distance value (or score)
	 * @return if the value is considered good enough
	 * to count as a localization
	 */
	public boolean thresh(double value) {
		if (metric.isDistance()) {
			return isBoundedDistanceBeneathThreshold(threshold, value);
		} else {
			return threshold <= value;
		}
	}

	private static boolean isBoundedDistanceBeneathThreshold(double tolerance,
			double distance) {
		return (tolerance == 1.0) ? (distance < 1.0) : (distance <= tolerance);
	}

	private static final ValueSpan nextSpan(Iterator iter) {
		return iter.hasNext() ? (ValueSpan) iter.next() : null;
	}

	/**
	 * Calculate the distances between the two attributes using this
	 * attribute measure. It returns the set of frames that were 
	 * successfully localized.
	 * @param target the target attribute
	 * @param targetSpan the framespan of the target attribute's 
	 * descriptor
	 * @param candidate the candidate attribute
	 * @param candSpan the framespan of the candidate attribute's 
	 * parent descriptor
	 * @param cfd information about the media file
	 * @param dh this will hold the computed distance information
	 * @return the frames that qualify as localized
	 * @throws MethodNotSupportedException if there is an error 
	 * in the data types or in the metric
	 */
	public FrameSpan calculateDistancesAndThresh(Attribute target,
			FrameSpan targetSpan, Attribute candidate, FrameSpan candSpan,
			CanonicalFileDescriptor cfd, DistanceHolder dh)
			throws MethodNotSupportedException {
		distances = dh;
		if (metric instanceof AttrDistance) {
			FrameSpan matchSpan = targetSpan.intersect(candSpan);
			if (matchSpan.size() <= 0)
				return null;

			Iterator targIter = target.getValues(targetSpan);
			ValueSpan currTarg = nextSpan(targIter);

			Iterator candIter = candidate.getValues(candSpan);
			ValueSpan currCand = nextSpan(candIter);

			// The basic idea is that currCand and currTarg represent the
			// current values and spans for candidates and targets. If one
			// span is entirely before the other, it has to catch up.
			// The 'now' pointer points at the start of the current span
			// we are evaluating. A null value means that the now value has
			// fallen off the end of the target/candidate attribute.
			long now = 0;
			while (currCand != null || currTarg != null) {
				try {
					AttributeValue t, c;
					long start, end;

					long possibleNow = (currTarg == null ? (currCand == null
							? 0
							: currCand.getStart()) : (currCand == null
							? currTarg.getStart()
							: Math
									.min(currTarg.getStart(), currCand
											.getStart())));
					if (now < possibleNow) {
						now = possibleNow;
					}

					if (currCand == null || now < currCand.getStart()) {
						// if the candidate is null at 'now'
						t = currTarg.getValue();
						c = null;
						start = currTarg.getStart();
						if (currCand == null
								|| currCand.getStart() > currTarg.getEnd()) {
							end = currTarg.getEnd();
							currTarg = nextSpan(targIter);
						} else {
							end = currCand.getStart() - 1;
						}
					} else if (currTarg == null || now < currTarg.getStart()) {
						// if the target is null at 'now'
						t = null;
						c = currCand.getValue();
						start = currCand.getStart();
						if (currTarg == null
								|| currTarg.getStart() > currCand.getEnd()) {
							end = currCand.getEnd();
							currCand = nextSpan(candIter);
						} else {
							end = currTarg.getStart() - 1;
						}
					} else { // currCand and currTarg overlap
						if (now == currTarg.getStart()
								&& now == currCand.getStart()) {
							t = currTarg.getValue();
							c = currCand.getValue();
							start = currTarg.getStart();
							end = Math
									.min(currTarg.getEnd(), currCand.getEnd());
						} else if (now == currTarg.getStart()) {
							t = currTarg.getValue();
							start = now;
							if (currCand.getStart() < now) {
								c = currCand.getValue();
								end = Math.min(currTarg.getEnd(), currCand
										.getEnd());
							} else {
								c = null;
								end = Math.min(currTarg.getEnd(), currCand
										.getStart() - 1);
							}
						} else { // now == currCand.getStart()
							c = currCand.getValue();
							start = now;
							if (currTarg.getStart() < now) {
								t = currTarg.getValue();
								end = Math.min(currTarg.getEnd(), currCand
										.getEnd());
							} else {
								t = null;
								end = Math.min(currCand.getEnd(), currTarg
										.getStart() - 1);
							}
						}
						if (currTarg.getEnd() <= end) {
							currTarg = nextSpan(targIter);
						}
						if (currCand.getEnd() <= end) {
							currCand = nextSpan(candIter);
						}
					}
					now = end + 1;
					Measurable.Difference diff = Distances.helpGetDiff(t, c,
							null, null, cfd, null);
					double d = metric.getDistance(diff).doubleValue();
					dh.set(start, now, d); // careful; dh's ends are exclusive
					// while spans' ends are inclusive

					if (!thresh(d)) {
						matchSpan.clear((int) start, (int) end);
					}
				} catch (IgnoredValueException ivx) {
					System.err.println("IVX: " + ivx.getMessage());
				}
			} // While still has some dynamic value

			return matchSpan;
		} else {
			throw new MethodNotSupportedException(
					"Cannot calculate distance and thresh without an AttrDistance");
		}
	}

	/**
	 * Compares attributes or framespans by the metric this is set to use.
	 * 
	 * @param D
	 *            the difference comparison
	 * @return the value of the metric's {@link Distance#getDistance}method.
	 * @throws MethodNotSupportedException
	 */
	public double distanceAgainst(Measurable.Difference D)
			throws MethodNotSupportedException {
		try {
			return metric.getDistance(D).doubleValue();
		} catch (ClassCastException ccx) {
			throw new MethodNotSupportedException(ccx.getMessage());
		}
	}

	private DistanceHolder distances;
	
	/**
	 * Gets the current distances.
	 * @return the distances from the last call to
	 * {@link #calculateDistancesAndThresh(Attribute, FrameSpan, Attribute, FrameSpan, CanonicalFileDescriptor, DistanceHolder)}.
	 */
	public DistanceHolder getDistances() {
		return distances;
	}
	
	/**
	 * Gets the current statistics
	 * @return the statistics from the last call to
	 * {@link #calculateDistancesAndThresh(Attribute, FrameSpan, Attribute, FrameSpan, CanonicalFileDescriptor, DistanceHolder)}.
	 */
	public double[] getStatistics() {
		double[] stats = new double[4];
		stats[Distances.MAXIMUM] = distances.getMaximum();
		stats[Distances.MINIMUM] = distances.getMinimum();
		stats[Distances.MEAN] = distances.getAverage();
		stats[Distances.MEDIAN] = distances.getMedian();
		return stats;
	}

	/**
	 * compares two dynamic attributes
	 * 
	 * @param target
	 *            the target attribute
	 * @param gSpan
	 *            the target attribute span
	 * @param candidate
	 *            the candidate attribute
	 * @param fSpan
	 *            the candidate span
	 * @param cfd
	 *            the source media description
	 * @return the distance
	 * @throws MethodNotSupportedException
	 */
	public double distanceAgainstDynamic(Attribute target, FrameSpan gSpan,
			Attribute candidate, FrameSpan fSpan, CanonicalFileDescriptor cfd)
			throws MethodNotSupportedException {
		if (metric instanceof AttrDistance) {
			calculateDistancesAndThresh(target, gSpan, candidate, fSpan, cfd,
					new DistanceHolder());
			return (distances.getAverage());
		} else {
			throw new MethodNotSupportedException(
					"Cannot calculate dynamic attribute distance without an AttrDistance");
		}
	}

	/**
	 * Tests that this measure is valid for a given attribute. Of course, it
	 * might not work, anyway, as it only goes by metric name and not
	 * implementation.
	 * 
	 * @param attr
	 *            the attribute type to see if this metric is valid for
	 * @return <code>true</code> if this measure is defined for the specified
	 *         attribute type
	 */
	public boolean isValidFor(String attr) {
		return AttrMeasure.isValidTolerance(threshold)
				&& Distances.isDistanceFor(attr, metric.toString());
	}

	/**
	 * Tests to see if the given measure is valid for the 
	 * given attribute type.
	 * @param attr the attribute type
	 * @param meas the measure
	 * @return <code>true</code> when the measure 
	 * works
	 */
	public static boolean isValidMeasure(String attr, AttrMeasure meas) {
		return AttrMeasure.isValidTolerance(meas.getTolerance())
				&& Distances.isDistanceFor(attr, meas.getMetric().toString());
	}

	/**
	 * Tests to see if the tolerance is a valid number.
	 * @param value the tolerance
	 * @return if the number is between zero and positive infinity
	 */
	public static boolean isValidTolerance(double value) {
		return (value >= 0.0 && value <= Double.MAX_VALUE && value != Double.NaN);
	}
}