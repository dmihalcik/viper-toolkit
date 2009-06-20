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

package viper.descriptors.attributes;

/**
 * This is thrown when an Attribute or Descriptor tries to use
 * an unspecified distance metric. For example, attempting to 
 * find the dice coefficient of two strings would throw this 
 * type of exception.
 */
public class ImproperMetricException extends Exception
{
  /**
   * Constructs a new ImproperMetricException with no detail message.
   */
  public ImproperMetricException()
  {
  }

  /**
   * Constructs a new ImproperMetricException with
   * the given detail message.
   * @param s The detail message to print with the error.
   */
  public ImproperMetricException(String s)
  {
    super (s);
  }
}
