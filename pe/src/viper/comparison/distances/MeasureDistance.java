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

import viper.descriptors.*;
import viper.descriptors.attributes.*;

/**
 * Like Filterable, Distances are a way of associating
 * distance with data.
 */
public interface MeasureDistance extends Distance
{
  /**
   * Compute the distance. Only defined for static 
   * attributes and the like (read: FrameSpan).
   * @param alpha the alpha value
   * @param beta the beta value
   * @param cfd the file descriptor that alpha and beta are defined within
   * @return the distance between alpha and beta
   */
  public Number getDistance (Measurable alpha, 
                             Measurable beta,
                             CanonicalFileDescriptor cfd);

  /**
   * Compute the distance. Only defined for static 
   * attributes and the like (read: FrameSpan).
   * This one includes blackout and ignore
   * parameters - blackouts apply to regions
   * that are losses, and ignore are for
   * regions that are don't-care. For example,
   * "scene" data may be marked as don't-care,
   * or frames that don't meat a certain metric
   * are qualified as blackout.
   *
   * @param alpha the first value
   * @param beta the second value
   * @param blackout the bad region
   * @param ignore the region to ignore
   * @param cfd the file on which the measurables are defined
   * @return the distance between alpha and beta, given the 
   * constraints
   * @throws IgnoredValueException when the ignored region is such that
   *         it prevents evaluation of the distance.
   */
  public Number getDistance (Measurable alpha, 
                             Measurable beta,
			     Measurable blackout,
			     Measurable ignore,
                             CanonicalFileDescriptor cfd)
       throws IgnoredValueException;
}
