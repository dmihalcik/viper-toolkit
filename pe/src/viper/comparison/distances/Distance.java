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

import viper.descriptors.attributes.*;

/**
 * Like Filterable, Distances are a way of associating
 * distance with data. Don't implement this directly -
 * instead use MeasureDistance or AttrDistance
 */
public interface Distance
{
  /**
   * Like number of pixels detected, this type of
   * metric is meant to be summed over all frames or
   * source files.
   */
  public static final int OVERALL_SUM = 0;

  /**
   * This metric is meant to be averaged over all
   * frames or media files.
   */
  public static final int OVERALL_MEAN = 1;

  /**
   * For framewise comparison, this type compares one
   * candidate to all targets.
   *
   * These types are averaged over all frames
   * and candidates.
   */
  public static final int CAND_V_TARGS = 3;

  /**
   * The reverse  of cand v targs, this type
   * indicates one target against all candidates
   * composed.
   */
  public static final int TARG_V_CANDS = 4;

  /**
   * A balanced distance metric gives the same
   * result if compared target to candidate or
   * candidate to target.
   */
  public static final int BALANCED = 5;

  /**
   * An explanatory message, like "Pixels matched", or
   * "Localized Object Count Recall".
   * @return this distance's explanatory message
   */
  public String getExplanation ();

  /**
   * Get what type of metric this is, per the list of static
   * int types.
   * @return one of BALANCED, CAND_V_TARGS, etc.
   */
  public int getType ();

  /**
   * Get the EPF name of the metric.
   * @return
   */
  public String toString ();

  /**
   * Gets the distance using cached distance data.
   * Has two major benefits: possible consolidation of
   * <code>IgnoredValueException</code>s and savings 
   * on recomputing, for example, the area of polygons.
   *
   * Calling
   * <code>
   *     getDistance (alpha, beta, blackout, ignore)
   * </code>
   * should have the same effect as
   * <code>
   *     getDistance (alpha.getDifference (beta, blackout, ignore))
   * </code>
   * @param D the distance of the difference
   * @return the distance
   */
  public Number getDistance (Measurable.Difference D);
  
  /**
   * Determines if this is a distance or a similarity quotient.
   * Similarity quotients are better the larger they are, but 
   * distances should be minimized.
   * @return <code>true</code> when 0 indicates a perfect match, and 
   *         distance varies proportionally to difference
   */
  public boolean isDistance ();
}
