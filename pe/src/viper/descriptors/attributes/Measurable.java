/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.descriptors.attributes;

import viper.comparison.distances.*;
import viper.descriptors.*;

/**
 * An interface for a data type that knows its own metric objects.
 */
public interface Measurable {
	
	/**
	 * Gets the type name.
	 * @return the viper attribute type name for the measurable object.
	 */
	public String getType();

	/**
	 * For performance reasons, takes an optional (could be null)
	 * Measurable.Difference to replace the data from.
	 * 
	 * Calling <code>
	 *     getDistance (alpha, beta, blackout, ignore)
	 * </code> should
	 * have the same effect as <code>
	 *     getDistance (alpha.getDifference (beta, blackout, ignore))
	 * </code>
	 * (Except for if alpha is null... then it will use
	 * Distances.DefaultDifference
	 * 
	 * @param beta
	 *            the difference to get against
	 * @param blackout
	 *            the measurable to count as bad
	 * @param ignore
	 *            the measurable region to ignore
	 * @param cfd
	 *            the media file descriptor
	 * @param old
	 *            an old difference to copy into, if possible, like the array
	 *            passed to list.toArray()
	 * @return the difference between this and beta
	 * @throws IgnoredValueException
	 *             if everything is ignored
	 */
	public Measurable.Difference getDifference(Measurable beta,
			Measurable blackout, Measurable ignore,
			CanonicalFileDescriptor cfd, Measurable.Difference old)
			throws IgnoredValueException;

	/**
	 * Not optimized version. Usually will just call
	 * <code>getDifference (..., null)</code>.
	 * 
	 * @param beta
	 *            the value to subtract/get the difference from
	 * @param blackout
	 *            the bad region
	 * @param ignore
	 *            the ignored region
	 * @param cfd
	 *            the media's information
	 * @see Measurable#getDifference(Measurable, Measurable, Measurable,
	 *      CanonicalFileDescriptor, Measurable.Difference)
	 * @return the difference between this and beta, given the constraints
	 * @throws IgnoredValueException
	 *             if this and beta are totally ignored
	 */
	public Measurable.Difference getDifference(Measurable beta,
			Measurable blackout, Measurable ignore, CanonicalFileDescriptor cfd)
			throws IgnoredValueException;

	/**
	 * For attributes that have several different distance metrics, it is often
	 * convenient to save some calculations; this interface is for keeping the
	 * computed data around for a bit.
	 * 
	 * TODO-davidm: change return values from Object to Measurable
	 */
	public static interface Difference {
		/**
		 * Gets the target value.
		 * @return the target value
		 */
		public Object getAlpha();

		/**
		 * Gets the candidate object.
		 * @return the candidate measurable
		 */
		public Object getBeta();

		/**
		 * Gets the blackout value.
		 * @return the blackout value
		 */
		public Object getBlackout();

		/**
		 * Gets the value to ignore/region to ignore.
		 * @return the don't care value
		 */
		public Object getIgnore();

		/**
		 * Gets information about the source media file where the 
		 * object originated.
		 * @return the source media description
		 */
		public CanonicalFileDescriptor getFileInformation();
	}
}