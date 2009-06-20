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

package viper.api;

import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * An extension of AttrValueWrapper that also contains 
 * {@link viper.api.time.InstantInterval} information. These are the items
 * returned by the Iterator in {@link viper.api.Attribute#iterator};
 * Static attributes return an iterator with one value. The span
 * is the extrema of the descriptor's range. However, dynamic attributes
 * will return values for each distinct section. If any value is 
 * <code>null</code>, it will be skipped (i.e. a static value that is 
 * <code>null</code> will return an empty iterator).
 */
public interface DynamicAttributeValue extends InstantInterval, DynamicValue {
}
