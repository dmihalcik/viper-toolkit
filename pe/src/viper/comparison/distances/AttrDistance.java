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
public interface AttrDistance extends Distance
{
  /**
   * Compute the distance.
   * @param alpha the alpha attribute
   * @param alphaSpan the alpha attribute's span
   * @param beta the beta attribute
   * @param betaSpan the beta attribute's span
   * @param frame the frame to get the distance in
   * @param cfd the source media
   * @return the distance
   */
  public Number getDistance (Attribute alpha, FrameSpan alphaSpan,
                             Attribute beta, FrameSpan betaSpan,
                             int frame, CanonicalFileDescriptor cfd);

  /**
   * Compute the distance, with blackout and ignore.
   * Note that blackout and ignore may be set to <code>null</code>
   * if there is nothing to black out or ignore.
   * @param alpha the alpha attribute
   * @param alphaSpan the alpha attribute's span
   * @param beta the beta attribute
   * @param betaSpan the beta attribute's span
   * @param blackout the blackout region
   * @param blackoutSpan the blackout region's span
   * @param ignore the region to ignore 
   * @param ignoreSpan the ignored region's span
   * @param frame the frame to get the distance in
   * @param cfd the source media
   * @return the distance, given the constraints
   * @throws IgnoredValueException when the ignored region is such that
   *         it prevents evaluation of the distance.
   */
  public Number getDistance (Attribute alpha, FrameSpan alphaSpan,
                             Attribute beta, FrameSpan betaSpan,
			     Attribute blackout, FrameSpan blackoutSpan,
                             Attribute ignore, FrameSpan ignoreSpan,
                             int frame, CanonicalFileDescriptor cfd)
       throws IgnoredValueException;
}

