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

package viper.comparison;

import java.io.*;
import java.util.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class represents the differences between two Descriptors.
 */
public class Comparison implements Cloneable {
	Descriptor T, C;
	private double score;

	/**
	 * distances = new double[attributeCount][C.getFrameSpan().size()]; The
	 * distance between each evaluated attribute at each matching frame.
	 */
	private DistanceHolder[] distances = new DistanceHolder[0];

	/**
	 * Average of all attribute distances.
	 */
	private double[] statistics = new double[0];
	private double distance; // FS for level 3

	FrameSpan matchSpan = null;
	FrameSpan unionSpan = null;

	/**
	 * The level of analysis that this Comparison has passed.
	 */
	private int level;

	/**
	 * What sort of match filtering this Comparison has survived.
	 */
	private int filterLevel = CompFilter.NONE;

	/**
	 * Indicates that the two descriptors are of incompatable type.
	 */
	public static final int UNCOMPARABLE = -2;

	/**
	 * Indicates that the two descriptors are compatable, but that no testing
	 * has been performed.
	 */
	public static final int STARTED = -1;

	/**
	 * Indicates that there exists some compatability, if only in frame range,
	 * between the descriptors.
	 */
	public static final int MATCHED = 0;

	/**
	 * Indicates that the frame ranges overlap within the desired tolerances.
	 */
	public static final int DETECTED = 1;

	/**
	 * Indicates that the measured attributes are similar enough on enough
	 * frames.
	 */
	public static final int LOCALIZED = 2;

	/**
	 * Indicates that the desired distance statistics, when computed on the
	 * shared frames, are within requested tolerances.
	 */
	public static final int STATISTICED = 3;

	/**
	 * Indicates that the match is finished, meaning, it has made it through the
	 * matching.
	 */
	public static final int COMPLETE = 4;

	/**
	 * Get the level of the comparison, e.g. {@link #LOCALIZED}.
	 * 
	 * @return the level the comparison has made it to
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Determine if the comparison is still active, or at what level it was
	 * removed from the pool of possible matches.
	 * 
	 * @return the level it was removed.
	 */
	public int getFilterLevel() {
		return filterLevel;
	}

	/**
	 * Sets the level the comparison was marked as invalid.
	 * @param i the new removal level
	 */
	public void setFilterLevel(int i) {
		filterLevel = i;
	}

	/**
	 * Gets a formatted string corresponding to the 
	 * given comparison level
	 * @param levelType the comparison level, e.g. {@link #MATCHED}
	 * @return a right-justified level title
	 */
	public static String levelTitle(int levelType) {
		switch (levelType) {
			case UNCOMPARABLE :
				return ("UNCOMPARABLE");
			case STARTED :
				return ("     STARTED");
			case MATCHED :
				return ("     MATCHED");
			case DETECTED :
				return ("    DETECTED");
			case LOCALIZED :
				return ("   LOCALIZED");
			case STATISTICED :
				return (" STATISTICED");
			case COMPLETE :
				return ("    COMPLETE");
			default :
				return ("   BAD LEVEL");
		}
	}

	/**
	 * This constructor takes in the type of Descriptor to cr DSD - Initial
	 * value is -2, if the are not the same category, -1 if they are.
	 * 
	 * @param target
	 *            The target, or ground truth, descriptor.
	 * @param candidate
	 *            The candidate, or results file, descriptor.
	 * @param map
	 *            the name equivalencies
	 */
	public Comparison(Descriptor target, Descriptor candidate, Equivalencies map) {
		reset(target, candidate, map);
	}

	/**
	 * Resets the comparison between two descriptors
	 * to {@link #UNCOMPARABLE} or {@link #STARTED}.
	 * @param target the target descriptor
	 * @param candidate the candidate descriptor
	 * @param map the ontology mapping
	 */
	public void reset(Descriptor target, Descriptor candidate, Equivalencies map) {
		C = candidate;
		T = target;
		if (!T.sameCategoryAs(C, map)) {
			level = UNCOMPARABLE;
			distance = Double.POSITIVE_INFINITY;
			matchSpan = null;
			unionSpan = null;
			distance = Double.POSITIVE_INFINITY;
		} else {
			matchSpan = C.getFrameSpan().intersect(T.getFrameSpan());
			unionSpan = C.getFrameSpan().union(T.getFrameSpan());
			level = STARTED;
			distance = 1.0; // FS needed for level 3
		}
		score = distance;
		distances = new DistanceHolder[0];
		filterLevel = CompFilter.NONE;
	}

	/**
	 * Creates a new empty comparison. Call
	 * {@link #reset(Descriptor, Descriptor, Equivalencies) reset}
	 * to get it going.
	 */
	public Comparison() {
		super();
	}

	/**
	 * Creates a copy of the <code>Comparison</code>, but not of the
	 * Descriptors.
	 * <em>If you are going to alter the Descriptors, make certain to copy
	 * them and call <code>setPointers</code> to change them.</em>
	 * 
	 * @return a copy of the comparison object
	 */
	public Object clone() {
		Comparison c = new Comparison();
		c.C = C;
		c.T = T;
		c.matchSpan = (FrameSpan) matchSpan.clone();
		c.unionSpan = (FrameSpan) unionSpan.clone();
		c.level = level;
		c.filterLevel = filterLevel;
		c.distance = distance;
		c.score = score;

		c.distances = new DistanceHolder[distances.length];
		for (int i = 0; i < distances.length; i++) {
			c.distances[i] = (DistanceHolder) distances[i].clone();
		}

		c.statistics = new double[statistics.length];
		for (int i = 0; i < statistics.length; i++) {
			c.statistics[i] = statistics[i];
		}

		return c;
	}

	/**
	 * Sets the Descriptor pointers. Necessary for cloning, unfortunately.
	 * 
	 * @param target
	 *            the target for the comparison
	 * @param candidate
	 *            the candidate for the comparison
	 */
	protected void setPointers(Descriptor target, Descriptor candidate) {
		C = candidate;
		T = target;
	}

	/**
	 * Sorts comparisons in order of quality. That is, better matches are sorted
	 * higher than poor matches. FIXME: This is currently only partialy
	 * implemented.
	 * 
	 * @param o -
	 *            the Comparison to compare this with
	 * @return 0 if the distances are equal. Less than o or greater gives - and +,
	 *         respectively. For example, a LOCALIZED comparison is greater than
	 *         a DETECTED comparison, and a LOCALIZED comparison with 18 valid
	 *         frames is greater than a LOCALIZED comparison with 17 valid
	 *         frames.
	 * @throws ClassCastException
	 * @throws BadDataException
	 */
	public double compareTo(Object o) throws BadDataException {
		if (this == o) {
			return 0;
		} else {
			Comparison other = (Comparison) o;
			if (this.level != other.level) {
				return other.level - this.level;
			} else
				switch (level) {
					case MATCHED :
						return 0;
					case DETECTED :
					case LOCALIZED :
						return (other.matchSpan == null ? (matchSpan == null
								? 0
								: 1) : (matchSpan == null ? -1 : matchSpan
								.size()
								- other.matchSpan.size()));
					default :
						return this.distance - other.distance;
				}
		}
	}

	/**
	 * This takes a new comparison to the specified level.
	 * 
	 * @param C
	 *            the comparison to promote
	 * @param level
	 *            the level to take it to, e.g. {@link Comparison#LOCALIZED}
	 * @param cfd
	 *            the source media descriptor
	 * @param epf
	 *            the scoping rules
	 * @return <code>true</code> if the comparison made it to the level
	 */
	static public boolean takeComparisonToThisLevel(Comparison C, int level,
			CanonicalFileDescriptor cfd, EvaluationParameters.ScopeRules epf) {
		return (((level < Comparison.MATCHED) || C.match(epf))
				&& ((level < Comparison.DETECTED) || C.detect(cfd, epf))
				&& ((level < Comparison.LOCALIZED) || C.localize(cfd, epf)) && ((level < Comparison.STATISTICED) || C
				.statistical(epf)));
	}

	/**
	 * Returns a String representation of the Descriptor. Contains EOL
	 * characters.
	 * 
	 * @return a String containing the category, name, ID number, span, and all
	 *         scoped Attributes
	 */
	public String toString() {
		switch (level) {
			case MATCHED :
				return ("No match");
			case DETECTED :
			case LOCALIZED :
			case STATISTICED :
				try {
					// FIXME doesn't know about cfd
					Measurable.Difference D = Distances.helpGetDiff(T
							.getFrameSpan(), C.getFrameSpan(), unionSpan
							.minus(matchSpan), null, null, null);
					Distance overlap = Distances.getDistanceFunctor(matchSpan,
							"overlap");
					Distance dice = Distances.getDistanceFunctor(matchSpan,
							"dice");
					Distance extent = Distances.getDistanceFunctor(matchSpan,
							"extent");
					return "DISTANCE: "
							+ distance
							+ ", OVERLAP: "
							+ overlap.getDistance(D)
							+ ", DICE = "
							+ dice.getDistance(D)
							+ ", EXTENT = "
							+ extent.getDistance(D)
							+ ", (START: "
							+ (T.getFrameSpan().beginning() - matchSpan
									.beginning()) + ", END: "
							+ (T.getFrameSpan().ending() - matchSpan.ending())
							+ ")";
				} catch (IgnoredValueException ivx) {
					return "DISTANCE: " + distance + ", IGNORED FRAMES";
				}
		}
		return "No match";
	}

	/**
	 * Returns a String containing the percentage.
	 * 
	 * @return " Match is xx%\n" or null if the percentage has not been
	 *         calculated
	 */
	public String matchingPercentage() {
		if ((score > 0) && (score < 100))
			return "   Match is " + score + " \n";
		return null;
	}

	/**
	 * Moves the comparison from {@link #STARTED}
	 * to {@link #MATCHED}, if possible.
	 * @param epf the rules and metrics for evaluation
	 * @return if the comparison counts as a match
	 */
	public boolean match(EvaluationParameters.ScopeRules epf) {
		boolean hit = epf.comparable(C, T);
		if (!hit) {
			return false;
		}
		hit = (T.getFrameSpan().overlap(C.getFrameSpan()) > 0);

		if (!hit) {
			distance = Double.POSITIVE_INFINITY;
			return (false);
		}

		try {
			Measurable.Difference D = T.getFrameSpan().getDifference(
					C.getFrameSpan(), null, null, null);
			AttrMeasure M = epf.getMeasure(T, " framespan");
			if (M == null || M.getMetric() == null) {
				throw new RuntimeException(
						"No frame span distance specified in evaluation.");
			}
			distance = epf.getMeasure(T, " framespan").getMetric().getDistance(
					D).doubleValue();
		} catch (IgnoredValueException ivx) {
			throw new RuntimeException("Unexpected exception: "
					+ ivx.getMessage());
		}

		level = MATCHED;
		return true;
	}

	/**
	 * Compares the framespans of the target and candidate 
	 * descriptors.
	 * @param cfd required information about the enclosing file,
	 * like its frame span or frame rate 
	 * @param epf rules and metrics for performing the comparison
	 * @return <code>true</code> if the comparison counts as a 
	 * detection
	 * @throws IllegalStateException if this comparison isn't
	 * at the level {@link #MATCHED} 
	 */
	public boolean detect(CanonicalFileDescriptor cfd,
			EvaluationParameters.ScopeRules epf) throws IllegalStateException {
		if (level != MATCHED) {
			throw new IllegalStateException(
					"Detection can only follow matching, not " + level);
		}

		boolean hit = true;
		AttrMeasure meas = epf.getMeasure(T, " framespan");

		try {
			Measurable.Difference D = T.getFrameSpan().getDifference(
					C.getFrameSpan(), null, null, cfd);
			distance = meas.getMetric().getDistance(D).doubleValue();
		} catch (IgnoredValueException ivx) {
			throw new RuntimeException("Unexpected exception: "
					+ ivx.getMessage());
		}
		hit = meas.thresh(distance);

		if (!hit) {
			distance = Double.POSITIVE_INFINITY;
			return false;
		}
		level = DETECTED;
		matchSpan = C.getFrameSpan().intersect(T.getFrameSpan());
		return true;
	}

	/**
	 * This performs localization. Level 2 - match frame by frame attributes.
	 * After, distance is set to the dice coefficient between the target frames
	 * and all the candidate frames that survive localization.
	 * 
	 * @param cfd
	 *            the containing media
	 * @param epf
	 *            the scoping rules
	 * @return <code>true</code> if the comparison is localized
	 * @throws IllegalStateException
	 *             if trying to localize a comparison that is not marked as
	 *             DETECTED
	 */
	public boolean localize(CanonicalFileDescriptor cfd,
			EvaluationParameters.ScopeRules epf) {
		if (level != DETECTED) {
			throw new IllegalStateException(
					"Localization can only follow detection, not " + level);
		}
		Map measures = epf.getAllMeasuresFor(T);

		distances = new DistanceHolder[measures.size() - 1];
		// But take back one Kadam to honor the Hebrew God whose Ark this is
		// Sorry... I mean, don't count the " framespan" measure.

		AttrMeasure meas;
		int scopedIndex = 0;
		for (Iterator iter = measures.entrySet().iterator(); iter.hasNext();) {
			// FIXME need to do this in the proper order - don't use entryset!
			Map.Entry curr = (Map.Entry) iter.next();
			String attribName = (String) curr.getKey();
			meas = (AttrMeasure) curr.getValue();
			if (!" framespan".equals(attribName)) {
				Attribute targAttr = T.getAttribute(attribName, epf.getMap());
				Attribute candAttr = C.getAttribute(attribName, epf.getMap());
				try {
					distances[scopedIndex] = new DistanceHolder();
					if (targAttr.isDynamic() || candAttr.isDynamic()) {
						FrameSpan inspan = meas.calculateDistancesAndThresh(
								targAttr, T.getFrameSpan(), candAttr, C
										.getFrameSpan(), cfd,
								distances[scopedIndex]);
						matchSpan.intersectWith(inspan);
					} else {
						try {
							AttributeValue t = targAttr.getStaticValue();
							Measurable.Difference D = Distances.helpGetDiff(t,
									candAttr.getStaticValue(), null, null, cfd,
									null);
							distances[scopedIndex].set(0, 1, meas
									.distanceAgainst(D));
						} catch (IgnoredValueException ivx) {
							distances[scopedIndex].set(0, 1, Double.NaN);
						}
						boolean passed = meas.thresh(distances[scopedIndex]
								.get(0));
						if (!passed) {
							matchSpan.intersectWith(null);
							// Since there is no span matching, is there is no
							// need to calculate the rest? This might not be
							// true.
						} //  else leave matchspan as is
					} // else static
				} catch (MethodNotSupportedException mnsx) {
					System.err.println("Error during localization: "
							+ mnsx.getMessage());
				}
				scopedIndex++;
			} // if not framespan
		} // for each attribute measure

		/***********************************************************************
		 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		 * This number of frames must be thresholded the same way it is in
		 * DETECT. - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		 */
		//NOTE: TO DAVE DOERMANN This should be changed to deal with proper
		// overlap
		// and etc... Also take a look at toString() method, wanted to do
		// something
		// different with the way those calculations are performed.
		meas = epf.getMeasure(T, " framespan");
		try {
			FrameSpan blackout = unionSpan.minus(matchSpan);
			Measurable.Difference D = T.getFrameSpan().getDifference(
					C.getFrameSpan(), blackout, null, cfd);
			distance = meas.getMetric().getDistance(D).doubleValue();
		} catch (IgnoredValueException ivx) {
			throw new RuntimeException(ivx.getMessage());
		}

		if ((matchSpan.numFrames() == 0) || !meas.thresh(distance)) {
			return false;
		}
		level = LOCALIZED;
		return true;
	}

	/**
	 * Performs Statistical, or Level 3, of the object evaluation type.
	 * "statistics" vector 0: Average 1: Minimum 2: Median 3: Maximum
	 * 
	 * This routine needs to be modified to threshold based on the average or
	 * maximum of each attribute according to level3_metric
	 * 
	 * Scores is one per attribute byFrame is one per frame
	 * 
	 * @param epf
	 *            the scoping rules
	 * @return <code>true</code> if the comparison has made it to the level
	 *         successfully
	 * @throws IllegalStateException
	 *             if the comparison isn't localized
	 */
	public boolean statistical(EvaluationParameters.ScopeRules epf) {
		if (level != LOCALIZED) {
			throw new IllegalStateException(
					"Statistical match can only follow localization");
		}

		/// This maintains the sum of all distances between each frame
		score = 0;
		statistics = new double[]{0.0, 0.0, 0.0, 0.0};

		AttrMeasure meas = epf.getMeasure(T, " framespan");
		int stat = meas.getStatType();
		double tolerance = meas.getStatThreshold();

		for (int i = 0; i < distances.length; i++) {
			score += distances[i].getAverage();
			statistics[Distances.MINIMUM] = distances[i].getMinimum();
			statistics[Distances.MEDIAN] = distances[i].getMedian();
			statistics[Distances.MAXIMUM] = distances[i].getMaximum();
		}
		statistics[Distances.MEAN] = score;
		this.distance = statistics[stat];

		if (this.distance >= 1.0) {
			return false;
		}

		for (int i = 0; i < distances.length; i++) {
			if (distances[i].getStat(stat) > tolerance) {
				return false;
			}
		}

		level = STATISTICED;
		return true;
	}

	/**
	 * Gets a formatted string indicating how far apart
	 * the target and candidate are in terms of frame span
	 * and each measured attribute.
	 * @param epf the rules for evaluation
	 * @return a raw-formatted sequence of distances
	 */
	public String getDistances(EvaluationParameters.ScopeRules epf) {
		if (distances.length == 0) {
			return "";
		}
		StringBuffer S = new StringBuffer();
		int stat = epf.getMeasure(C, " framespan").getStatType(); //FIXME use
																  // seperate
																  // stats for
																  // each attr
		for (int i = 0; i < distances.length; i++) {
			S.append(distances[i].getStat(stat)).append(' ');
		}
		return S.substring(0, S.length() - 1);
	}

	/**
	 * Prints, in verbose format, information about the candidate and how close
	 * it matches the target.
	 * 
	 * @param output
	 *            The stream to accept the text.
	 * @param padding
	 *            A prefix to put before each line (useful for formatting)
	 * @param epf
	 *            the scoping rules
	 */
	public void printCandidate(PrintWriter output, String padding,
			EvaluationParameters.ScopeRules epf) {
		printSelf(C, output, padding, epf);
		output.println(padding + "---------------------------------------");
		output.println(padding + toString());
	}

	/**
	 * Prints out the descriptor and its distance information in verbose/pretty
	 * format.
	 * 
	 * @param desc
	 *            the descriptor associated wtih the comparison to pring
	 * @param output
	 *            the stream that accepts the output
	 * @param padding
	 *            places this string before each line of output
	 * @param epf
	 *            the scope rules
	 */
	private void printSelf(Descriptor desc, PrintWriter output, String padding,
			EvaluationParameters.ScopeRules epf) {
		output.println(padding + desc.getFullName() + " " + desc.getID() + " "
				+ desc.getFrameSpan());
		int attributeOffset = 0;
		for (Iterator iter = epf.getInScopeAttributesFor(desc); iter.hasNext();) {
			String attrName = (String) iter.next();
			Attribute current = desc.getAttribute(attrName, epf.getMap());
			output.print(padding + "   " + current);
			if (distances != null && distances.length > 0) {
				output.print(padding + "\t\tDISTANCE(S): ");
				if (current.isDynamic()) {
					output.print(distances[attributeOffset]);
				} else {
					output.print(distances[attributeOffset].get(0));
				}
				output.println();
			}
			if (distances != null && distances.length > 0) {
				output.println(padding + "\t\tAVERAGE: "
						+ distances[attributeOffset].getAverage());
			}
			attributeOffset++;
		}
	}

	/**
	 * Returns the distance metric calculated in the match() function during
	 * level 0 testing. This will primarily used for level 3 and level 4.
	 * 
	 * @return the distance measurement (how far from original this is). 1 =
	 *         bad, 0 = good
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Allows to set the level to whatever is passed in. Mostly used only for
	 * Level 3 comparissons since all the calculations are done in the
	 * CompMatrix class.
	 * 
	 * @param new_level
	 *            the int value of the level to which you want to set this level
	 *            to.
	 */
	protected void setLevel(int new_level) {
		level = new_level;
	}

	/**
	 * Returns the distance measure of the specified attribute name.
	 * 
	 * @param name
	 *            the Attribute name that you requesting a distance measure for
	 * @return the double value that is the distance. Returns 0.0 as default.
	 */
	public double getDistanceFor(String name) {
		int index = T.getAttributeIndex(name);
		if (index < 0 || distances == null) {
			return 0;
		} else
			return distances[index].getAverage();
	}
}