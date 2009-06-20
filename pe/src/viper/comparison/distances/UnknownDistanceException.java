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
 * UnknownDistanceException.java
 *
 * Created on May 29, 2002, 3:01 PM
 */

package viper.comparison.distances;

/**
 * Runtime exception that is thrown when trying to get a distance that has not
 * been registered. Often thrown due to misspelling or something.
 * @author  davidm
 */
public class UnknownDistanceException extends java.lang.ClassCastException {
    
    /**
     * Creates a new instance of <code>UnknownDistanceException</code> without 
     * detail message.
     */
    public UnknownDistanceException() {
    }

    /**
     * Constructs an instance of <code>UnknownDistanceException</code> with the
     * specified detail message.
     * @param msg the detail message.
     */
    public UnknownDistanceException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>UnknownDistanceException</code> with the
     * specified detail message.
     * @param measurable The measurable type that should support the given distance
     * @param distance The name of the distance functor that was not found.
     */
    public UnknownDistanceException(String measurable, String distance) {
        super("Data of type " + measurable + " does not have a distance named " + distance);
    }

    /**
     * Constructs an instance of <code>UnknownDistanceException</code> with the
     * specified detail message.
     * @param measurable The measurable type that should support the given distance
     * @param distance The name of the distance functor that was not found.
     */
    public UnknownDistanceException(viper.descriptors.attributes.Measurable measurable, String distance) {
        super("Data of type " + measurable + " does not have a distance named " + distance);
    }
}
