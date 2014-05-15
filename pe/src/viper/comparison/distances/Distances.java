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

/**
 * Utility class containing a variety of different standard distance measures.
 */
public class Distances {
	
	/**
	 * An interface for simplifying implementation of a
	 * distance metric; instead of implementing all of 
	 * {@link AttrDistance}, you can just pass this to
	 * {@link Distances.HelperAttrDistance}. There are 
	 * also wrappers for MeasureDistance and regular Distance
	 * function objects.
	 */
	public static interface ValueDistance {
		
		/**
		 * Gets the distance between two instances of the
		 * appropriate attribute data type.
		 * @param alphaVal the target
		 * @param betaVal the candidate
		 * @param cfd information about the media file
		 * @return the distance between alpha and beta
		 */
		public Number helpGetDistance(Object alphaVal, Object betaVal,
				CanonicalFileDescriptor cfd);
	}
	
	/**
	 * An extended distance function object that 
	 * can deal with both don't-care values and 
	 * blackout values.
	 */
	public static interface SmartValueDistance {
		
		/**
		 * Gets the distance between two values, with
		 * some constraints.
		 * @param alphaVal the target
		 * @param betaVal the candidate
		 * @param blackoutVal values here are bad
		 * @param ignoreVal values here don't matter
		 * @param cfd information about the media
		 * @return the distance
		 * @throws IgnoredValueException if nothing was evaluated 
		 * due to the size of the ignored region
		 */
		public Number helpGetDistance(Object alphaVal, Object betaVal,
				Object blackoutVal, Object ignoreVal,
				CanonicalFileDescriptor cfd) throws IgnoredValueException;
	}
	
	/**
	 * Sometimes, it is convenient to save on computations
	 * for values that are compared over-and-over; this distance 
	 * functor allows cacheing operations when performing multiple
	 * comparisons between the same attributes.
	 */
	public static interface QuickValueDistance {
		
		/**
		 * This computes the distances using the given, possibly
		 * already used, comparison object.
		 * @param D the comparison object
		 * @return the new distance
		 */
		public Number helpGetDistance(Measurable.Difference D);
	}
	
	/**
	 * Converts a ValueDistance functor into a QuickValueDistance functor.
	 */
	private static class SimpleValWrap implements Distances.QuickValueDistance {
		private Distances.ValueDistance dist;
		
		SimpleValWrap(Distances.ValueDistance v) {
			dist = v;
		}
		
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			return dist.helpGetDistance(D.getAlpha(), D.getBeta(), D
					.getFileInformation());
		}
		
		/** @inheritDoc */
		public int hashCode() {
			return dist.hashCode();
		}
		
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof SimpleValWrap) {
				SimpleValWrap other = (SimpleValWrap) o;
				return dist == other.dist;
			}
			return false;
		}
	}
	
	/**
	 * Converts a SmartValueDistance functor into a QuickValueDistance
	 * functor.
	 */
	private static class SmartValWrap implements Distances.QuickValueDistance {
		private Distances.SmartValueDistance dist;
		SmartValWrap(Distances.SmartValueDistance v) {
			dist = v;
		}
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			try {
				return dist.helpGetDistance(D.getAlpha(), D.getBeta(), D
						.getBlackout(), D.getIgnore(), D.getFileInformation());
			} catch (IgnoredValueException ivx) {
				throw new RuntimeException("Unexpected exception: "
						+ ivx.getMessage());
			}
		}
		/** @inheritDoc */
		public int hashCode() {
			return dist.hashCode();
		}
		/** @inheritDoc */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof SmartValWrap) {
				SmartValWrap other = (SmartValWrap) o;
				return dist == other.dist;
			}
			return false;
		}
	}

	/**
	 * Converts one of the value distance function objects
	 * into a {@link MeasureDistance}.
	 */
	public static class HelperMeasureDistance extends HelperDistance
			implements
				MeasureDistance {
		
		/**
		 * Constructs a new measure distance from the given
		 * function object and parameters.
		 * @param d the distance functor
		 * @param name the name of the distance metric
		 * @param type the category of distance, e.g. {@link Distance#BALANCED}
		 * @param explanation a long explanation of the distance, for
		 * helper documentation and verbose output
		 * @param isDistance determines if the distance is really a distance 
		 * or if it is a score (bigger is better)
		 */
		public HelperMeasureDistance(Distances.ValueDistance d, String name,
				int type, String explanation, boolean isDistance) {
			super(new SimpleValWrap(d), name, type, explanation, isDistance);
		}
		
		/**
		 * Constructs a new measure distance from the given
		 * function object and parameters.
		 * @param d the distance functor
		 * @param name the name of the distance metric
		 * @param type the category of distance, e.g. {@link Distance#BALANCED}
		 * @param explanation a long explanation of the distance, for
		 * helper documentation and verbose output
		 * @param isDistance determines if the distance is really a distance 
		 * or if it is a score (bigger is better)
		 */
		public HelperMeasureDistance(Distances.SmartValueDistance d,
				String name, int type, String explanation, boolean isDistance) {
			super(new SmartValWrap(d), name, type, explanation, isDistance);
		}
		
		/**
		 * Constructs a new measure distance from the given
		 * function object and parameters.
		 * @param d the distance functor
		 * @param name the name of the distance metric
		 * @param type the category of distance, e.g. {@link Distance#BALANCED}
		 * @param explanation a long explanation of the distance, for
		 * helper documentation and verbose output
		 * @param isDistance determines if the distance is really a distance 
		 * or if it is a score (bigger is better)
		 */
		public HelperMeasureDistance(Distances.QuickValueDistance d,
				String name, int type, String explanation, boolean isDistance) {
			super(d, name, type, explanation, isDistance);
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Measurable alpha, Measurable beta,
				CanonicalFileDescriptor cfd) {
			Measurable.Difference D = null;
			try {
				D = helpGetDiff(alpha, beta, null, null, cfd, D);
			} catch (IgnoredValueException ivx) {
				throw new RuntimeException("Unexpected IgnoredValueException: "
						+ ivx.getMessage());
			}
			return getDistance(D);
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Measurable alpha, Measurable beta,
				Measurable blackout, Measurable ignore,
				CanonicalFileDescriptor cfd) throws IgnoredValueException {
			Measurable.Difference D = null;
			try {
				D = helpGetDiff(alpha, beta, blackout, ignore, cfd, D);
			} catch (IgnoredValueException ivx) {
				throw new RuntimeException("Unexpected IgnoredValueException: "
						+ ivx.getMessage());
			}
			return getDistance(D);
		}
	}

	/**
	 * Converts one of the value distance function objects
	 * into a {@link AttrDistance}.
	 */
	public static class HelperAttrDistance extends HelperDistance
			implements
				AttrDistance {
		/**
		 * Constructs a new measure distance from the given
		 * function object and parameters.
		 * @param d the distance functor
		 * @param name the name of the distance metric
		 * @param type the category of distance, e.g. {@link Distance#BALANCED}
		 * @param explanation a long explanation of the distance, for
		 * helper documentation and verbose output
		 * @param isDistance determines if the distance is really a distance 
		 * or if it is a score (bigger is better)
		 */
		public HelperAttrDistance(Distances.ValueDistance d, String name,
				int type, String explanation, boolean isDistance) {
			super(new SimpleValWrap(d), name, type, explanation, isDistance);
		}
		
		/**
		 * Constructs a new measure distance from the given
		 * function object and parameters.
		 * @param d the distance functor
		 * @param name the name of the distance metric
		 * @param type the category of distance, e.g. {@link Distance#BALANCED}
		 * @param explanation a long explanation of the distance, for
		 * helper documentation and verbose output
		 * @param isDistance determines if the distance is really a distance 
		 * or if it is a score (bigger is better)
		 */
		public HelperAttrDistance(Distances.SmartValueDistance d, String name,
				int type, String explanation, boolean isDistance) {
			super(new SmartValWrap(d), name, type, explanation, isDistance);
		}
		
		/**
		 * Constructs a new measure distance from the given
		 * function object and parameters.
		 * @param d the distance functor
		 * @param name the name of the distance metric
		 * @param type the category of distance, e.g. {@link Distance#BALANCED}
		 * @param explanation a long explanation of the distance, for
		 * helper documentation and verbose output
		 * @param isDistance determines if the distance is really a distance 
		 * or if it is a score (bigger is better)
		 */
		public HelperAttrDistance(Distances.QuickValueDistance d, String name,
				int type, String explanation, boolean isDistance) {
			super(d, name, type, explanation, isDistance);
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Attribute alpha, FrameSpan alphaSpan,
				Attribute beta, FrameSpan betaSpan, int frame,
				CanonicalFileDescriptor cfd) {
			Measurable.Difference D = null;
			try {
				D = helpGetDiff(alpha, alphaSpan, beta, betaSpan, null, null,
						null, null, frame, cfd, D);
			} catch (IgnoredValueException ivx) {
				throw new RuntimeException("Unexpected IgnoredValueException: "
						+ ivx.getMessage());
			}
			return getDistance(D);
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Attribute alpha, FrameSpan alphaSpan,
				Attribute beta, FrameSpan betaSpan, Attribute blackout,
				FrameSpan blackoutSpan, Attribute ignore, FrameSpan ignoreSpan,
				int frame, CanonicalFileDescriptor cfd)
				throws IgnoredValueException {
			Measurable.Difference D = null;
			try {
				D = helpGetDiff(alpha, alphaSpan, beta, betaSpan, blackout,
						blackoutSpan, ignore, ignoreSpan, frame, cfd, D);
			} catch (IgnoredValueException ivx) {
				throw new RuntimeException("Unexpected IgnoredValueException: "
						+ ivx.getMessage());
			}
			return getDistance(D);
		}
	}

	abstract static class HelperDistance implements Distance {
		protected Distances.QuickValueDistance dist;
		private String name;
		private String explanation;
		private int type;
		private boolean isDist = true;
		HelperDistance(Distances.QuickValueDistance d, String name,
				int type, String explanation, boolean isDistance) {
			this.dist = d;
			this.name = name;
			this.type = type;
			this.explanation = explanation;
			this.isDist = isDistance;
		}
		
		/**
		 * @inheritDoc
		 */
		public int getType() {
			return type;
		}
		
		/**
		 * @inheritDoc
		 */
		public String getExplanation() {
			return explanation;
		}
		
		/**
		 * @inheritDoc
		 */
		public String toString() {
			return name;
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Measurable.Difference D) {
			return dist.helpGetDistance(D);
		}
		
		/**
		 * @inheritDoc
		 */
		public int hashCode() {
			return type ^ name.hashCode() ^ dist.hashCode();
		}
		
		/**
		 * @inheritDoc
		 */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof HelperDistance) {
				HelperDistance other = (HelperDistance) o;
				return name == other.name && dist == other.dist;
			}
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public boolean isDistance() {
			return isDist;
		}
	}

	private static class EqualityDistance implements AttrDistance {
		
		/**
		 * @inheritDoc
		 */
		public int getType() {
			return Distance.BALANCED;
		}
		
		/**
		 * @inheritDoc
		 */
		public String toString() {
			return "e";
		}
		
		/**
		 * @inheritDoc
		 */
		public String getExplanation() {
			return "Equality measure";
		}
		
		/**
		 * @inheritDoc
		 */
		public boolean isDistance() {
			return true;
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Attribute alpha, FrameSpan alphaSpan,
				Attribute beta, FrameSpan betaSpan, Attribute blackout,
				FrameSpan blackoutSpan, Attribute ignore, FrameSpan ignoreSpan,
				int frame, CanonicalFileDescriptor cfd) {
			if (blackout != null
					&& blackout.getValue(blackoutSpan, frame) != null) {
				return new Integer(1);
			} else {
				return getDistance(alpha, alphaSpan, beta, betaSpan, frame, cfd);
			}
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Attribute alpha, FrameSpan alphaSpan,
				Attribute beta, FrameSpan betaSpan, int frame,
				CanonicalFileDescriptor cfd) {
			Object a = alpha.getValue(alphaSpan, frame);
			Object b = beta.getValue(betaSpan, frame);
			if (a == null || b == null) {
				return a == b ? new Integer(0) : new Integer(1);
			} else if ((a instanceof Object[]) && (b instanceof Object[])) {
				return Arrays.equals((Object[]) a, (Object[]) b) ? new Integer(
						0) : new Integer(1);
			} else if ((a instanceof int[]) && (b instanceof int[])) {
				return Arrays.equals((int[]) a, (int[]) b)
						? new Integer(0)
						: new Integer(1);
			} else if ((a instanceof double[]) && (b instanceof double[])) {
				return Arrays.equals((double[]) a, (double[]) b) ? new Integer(
						0) : new Integer(1);
			} else if ((a instanceof boolean[]) && (b instanceof boolean[])) {
				return Arrays.equals((boolean[]) a, (boolean[]) b)
						? new Integer(0)
						: new Integer(1);
			} else {
				return a.equals(b) ? new Integer(0) : new Integer(1);
			}
		}
		
		/**
		 * @inheritDoc
		 */
		public Number getDistance(Measurable.Difference D) {
			if (D.getBlackout() != null) {
				return new Integer(1);
			} else {
				if (D.getAlpha() == null || D.getBeta() == null) {
					return (D.getAlpha() == D.getBeta())
							? new Integer(0)
							: new Integer(1);
				} else {
					return (D.getAlpha().equals(D.getBeta()))
							? new Integer(0)
							: new Integer(1);
				}
			}
		}
	}
	private static EqualityDistance equalityDistanceSingleton = new Distances.EqualityDistance();

	/**
	 * Get an AttrDistance object that returns 0 when the attributes are equal
	 * (using .equals) or 1 when they are not.
	 * 
	 * @return the equality distance measure
	 */
	public static AttrDistance getEqualityDistance() {
		return equalityDistanceSingleton;
	}

	/**
	 * Computes the difference between two attributes
	 * using the given distance metric. This function takes
	 * care of things like dynamic attributes and null values.
	 * This one allows reclamation of a difference object.
	 * @param alpha the target attribute
	 * @param alphaSpan the target attribute's span
	 * @param beta the candidate attribute
	 * @param betaSpan the candidate attribute's framespan
	 * @param blackout the blackout data
	 * @param blackoutSpan when the blackout is defined
	 * @param ignore the don't-care data
	 * @param ignoreSpan the don't-care framespan
	 * @param frame the frame to compare
	 * @param cfd information about the media
	 * @param old cached difference object
	 * @return the new difference object, or the same one, changed
	 * @throws IgnoredValueException  if the whole of the data on the frame was ignored
	 */
	public static Measurable.Difference helpGetDiff(Attribute alpha,
			FrameSpan alphaSpan, Attribute beta, FrameSpan betaSpan,
			Attribute blackout, FrameSpan blackoutSpan, Attribute ignore,
			FrameSpan ignoreSpan, int frame, CanonicalFileDescriptor cfd,
			Measurable.Difference old) throws IgnoredValueException {
		if (alpha == null || alpha.getValue(alphaSpan, frame) == null) {
			return new Distances.DefaultDifference(null, (beta != null) ? beta
					.getValue(betaSpan, frame) : null, (blackout != null)
					? blackout.getValue(blackoutSpan, frame)
					: null, (ignore != null) ? ignore.getValue(ignoreSpan,
					frame) : null, cfd);

		} else {
			Measurable a = alpha.getValue(alphaSpan, frame);
			return a.getDifference((beta != null) ? beta.getValue(betaSpan,
					frame) : null, (blackout != null) ? blackout.getValue(
					blackoutSpan, frame) : null, (ignore != null) ? ignore
					.getValue(ignoreSpan, frame) : null, cfd, old);
		}
	}

	/**
	 * Computes the difference between two attributes
	 * using the given distance metric. This must operate
	 * on the attribute values directly
	 * @param alpha the target attribute
	 * @param beta the candidate attribute
	 * @param blackout the blackout data
	 * @param ignore the don't-care data
	 * @param cfd information about the media
	 * @param old cached difference object
	 * @return the difference
	 * @throws IgnoredValueException  if the whole of the data on the frame was ignored
	 */
	public static Measurable.Difference helpGetDiff(Measurable alpha,
			Measurable beta, Measurable blackout, Measurable ignore,
			CanonicalFileDescriptor cfd, Measurable.Difference old)
			throws IgnoredValueException {
		if (alpha == null) {
			return new Distances.DefaultDifference(null, beta, blackout,
					ignore, cfd);
		} else {
			return alpha.getDifference(beta, blackout, ignore, cfd, old);
		}
	}

	/**
	 * A simple differnece object, which keeps track of some information
	 * about a computed difference. This includes the compared value,
	 * any blackout or don't care values, and the file information.
	 */
	public static class DefaultDifference implements Measurable.Difference {
		
		/**
		 * Constructs a new difference object.
		 * @param alpha the target value
		 * @param beta the candidate value
		 * @param blackout the bad region
		 * @param ignore the ignored region
		 * @param cfd information about the containing file
		 */
		public DefaultDifference(Object alpha, Object beta, Object blackout,
				Object ignore, CanonicalFileDescriptor cfd) {
			this.alpha = alpha;
			this.beta = beta;
			this.blackout = blackout;
			this.ignore = ignore;
			this.cfd = cfd;
		}
		private Object alpha, beta, blackout, ignore;
		private CanonicalFileDescriptor cfd;
		
		/** @inheritDoc */
		public Object getAlpha() {
			return alpha;
		}
		
		/** @inheritDoc */
		public Object getBeta() {
			return beta;
		}
		
		/** @inheritDoc */
		public Object getBlackout() {
			return blackout;
		}
		
		/** @inheritDoc */
		public Object getIgnore() {
			return ignore;
		}
		
		/** @inheritDoc */
		public CanonicalFileDescriptor getFileInformation() {
			return cfd;
		}
	}

	private static HashMap<String, Map<String, Distance>> distanceMap = new HashMap<String, Map<String, Distance>>();
	
	/**
	 * Adds a new distance functor for the given attribute data 
	 * type.
	 * @param type the attribute data type
	 * @param d the distance functor to use on the data type
	 */
	public static final void putDistanceFunctorFor(String type, Distance d) {
		Map<String, Distance> fs = distanceMap.get(type);
		if (fs == null) {
			fs = new HashMap<String, Distance>();
		}
		fs.put(d.toString(), d);
		distanceMap.put(type, fs);
	}
	
	/**
	 * Tests to see if the specified metric name is known for
	 * the given attribute data type.
	 * @param type the attribute type
	 * @param s the name of the distance metric to look for
	 * @return true if <code>s</code> is the name of a known metric 
	 * for the type
	 * @throws UnknownDistanceException if the type is unknown.
	 * XXX: shouldn't this be the other way around?
	 */
	public static final boolean isDistanceFor(Measurable type, String s)
			throws UnknownDistanceException {
		Map<String, Distance> values = distanceMap.get(type.getType());
		if (values != null) {
			return values.containsKey(s.toLowerCase());
		} else {
			throw new UnknownDistanceException(type, s);
		}
	}
	
	/**
	 * Tests to see if the specified metric name is known for
	 * the given attribute data type.
	 * @param type the attribute type
	 * @param s the name of the distance metric to look for
	 * @return true if <code>s</code> is the name of a known metric 
	 * for the type
	 * @throws UnknownDistanceException if the type is unknown.
	 * XXX: shouldn't this be the other way around?
	 */
	public static final boolean isDistanceFor(String type, String s)
			throws UnknownDistanceException {
		check(type);
		Map<String, Distance> values = distanceMap.get(type);
		if (values != null) {
			return values.containsKey(s.toLowerCase());
		} else {
			throw new UnknownDistanceException("Attribute type not found: "
					+ type);
		}
	}
	
	/**
	 * Gets the distance functor for the given type.
	 * @param type the attribute data type
	 * @param metric the metric name
	 * @return the metric
	 * @throws UnknownDistanceException if the metric isn't found
	 */
	public static final Distance getDistanceFunctor(Measurable type,
			String metric) throws UnknownDistanceException {
		return Distances.getDistanceFunctor(type.getType(), metric);
	}

	/**
	 * Check to make sure that the specified attribute has been loaded into the
	 * virtual machine.
	 * 
	 * @param type
	 *            the name of the attribute to check
	 * @return <code>true</code> if the attribute has been or can be loaded
	 */
	public static final boolean check(String type) {
		if (!distanceMap.containsKey(type)) {
			// There are plenty of other ways attributes
			// are loaded - directly or by their children
			AbstractAttribute.loadAttributeType(type);
			return distanceMap.containsKey(type);
		}
		return true;
	}

	/**
	 * Gets the distance functor for the given type.
	 * @param type the attribute data type
	 * @param metric the metric name
	 * @return the metric
	 * @throws UnknownDistanceException if the metric isn't found
	 */
	public static final Distance getDistanceFunctor(String type, String metric)
			throws UnknownDistanceException {
		check(type);
		metric = metric.toLowerCase();
		if (!distanceMap.containsKey(type)) {
			throw new UnknownDistanceException("Attribute type not found: "
					+ type);
		} else {
			Map<String, Distance> distances = distanceMap.get(type);
			if (!distances.containsKey(metric)) {
				throw new UnknownDistanceException(type, metric);
			} else {
				return (Distance) distances.get(metric);
			}
		}
	}

	/**
	 * Copies all the distance functors from <code>from</code> attribute type
	 * to <code>to</code> attribute type.
	 * 
	 * @param to
	 *            the attribute type to get the copies
	 * @param from
	 *            the attribute type to receive the distance functor references
	 */
	public static final void useSameDistances(String to, String from) {
		check(from);
		Map<String, Distance> fromValues = distanceMap.get(from);
		Map<String, Distance> toValues = distanceMap.get(to);
		if (fromValues != null) {
			if (toValues != null) {
				toValues.putAll(fromValues);
			} else {
				toValues = new HashMap<String, Distance>();
				toValues.putAll(fromValues);
				distanceMap.put(to, toValues);
			}
		}
	}

	/**
	 * The coefficient to use with
	 * {@link #closedDistanceRangeToInfinite(double, double)}
	 * and {@link #infiniteDistanceRangeToClosed(double, double)}.
	 */
	public static double alpha = 0.1;

	/**
	 * Converts from the range zero to one to
	 * zero to the specified maximum. It uses
	 * a log value.
	 * @param distance the distance
	 * @param maximum the max
	 * @return the undamped distance
	 * @see #infiniteDistanceRangeToClosed(double, double)
	 */
	public static double closedDistanceRangeToInfinite(double distance,
			double maximum) {
		return (-alpha * Math.log(1 - (distance / maximum)));
	}

	/**
	 * Converts from a positive infinite range to the range from 
	 * zero to maxiumum. Uses an exponential conversion.
	 * @param distance a (possibly very great) distance 
	 * @param maximum the new maximum value.
	 * @return the clamped value
	 * @see #closedDistanceRangeToInfinite(double, double)
	 */
	public static double infiniteDistanceRangeToClosed(double distance,
			double maximum) {
		return (maximum - (maximum * Math.exp(-alpha * distance)));
	}

	/**
	 * The average statistic.
	 */
	public static final int MEAN = 0;

	/**
	 * The minimum value statistic.
	 */
	public static final int MINIMUM = 1;

	/**
	 * The median value statistic.
	 */
	public static final int MEDIAN = 2;

	/**
	 * The maximum value statistic.
	 */
	public static final int MAXIMUM = 3;

	static int defaultStatistic = Distances.MEAN;
	static double defaultSTolerance = 0;

	/**
	 * Set the default threshold for the level 3-statistic measure.
	 * 
	 * @param t
	 *            sets the default tolerance level
	 */
	public static void setDefaultSTolerance(double t) {
		defaultSTolerance = t;
	}
	
	/**
	 * Gets the default tolerance to apply to the statistic measure
	 * of a descriptor to count it as 'statisticked'.
	 * @return the default statistic tolerance
	 */
	public static double getDefaultSTolerance() {
		return defaultSTolerance;
	}

	/**
	 * Gets the default statistic to use when comparing descriptors
	 * at the 'statistic' level.
	 * @return {@link #MEDIAN}, for example
	 */
	public static int getDefaultStatistic() {
		return defaultStatistic;
	}
	
	/**
	 * Sets the default statistic to use when comparing descriptors
	 * at the 'statistic' level. The string must be either
	 * "mean", "minimum", "median", or "maximum".
	 * @param s {@link #MEDIAN}, for example
	 * @throws ImproperMetricException not a known metric. 
	 */
	public static void setDefaultStatistic(String s)
			throws ImproperMetricException {
		if (s.equalsIgnoreCase("mean"))
			Distances.defaultStatistic = Distances.MEAN;
		else if (s.equalsIgnoreCase("minimum"))
			Distances.defaultStatistic = Distances.MINIMUM;
		else if (s.equalsIgnoreCase("median"))
			Distances.defaultStatistic = Distances.MEDIAN;
		else if (s.equalsIgnoreCase("maximum"))
			Distances.defaultStatistic = Distances.MAXIMUM;
		else
			throw new ImproperMetricException(
					"Not an acceptable distance statistic: " + s);
	}
}