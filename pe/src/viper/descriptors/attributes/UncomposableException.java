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
 * UncomposableException.java
 *
 * Created on June 25, 2002, 8:39 PM
 */

package viper.descriptors.attributes;

/**
 * Thrown when trying to compose two composable attributes, but, for whatever
 * reason, they don't like it.
 * 
 * @author  davidm
 */
public class UncomposableException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>UncomposableException</code> without detail message.
     */
    public UncomposableException() {
    }
    
    
    /**
     * Constructs an instance of <code>UncomposableException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public UncomposableException(String msg) {
        super(msg);
    }
}
