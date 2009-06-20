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
 * Composable.java
 *
 * Created on June 25, 2002, 8:39 PM
 */

package viper.descriptors.attributes;

/**
 *
 * @author  davidm
 */
public interface Composable {

    /**
     * Indicates that the composition is order dependent, such as string
     * concatenation.
     */
    public static final int ORDERED = 1;

    /**
     * Indicates that the composition is order independent, such as
     * region unions or bags of words.
     */
    public static final int UNORDERED = 0;

    /**
     * Generate the composition of this object with its partner. If ordered, 
     * this is the object on the "left," whatever that means.
     * @param partner
     * @return new Composable object from the this and the passed partner
     * @throws UncomposableException
     * @throws ClassCastException
     */
    public Composable compose(Composable partner) throws UncomposableException;
    
    /**
     * Get the type of composition to try.
     * @return <code>ORDERED</code> or <code>UNORDERED</code>
     */
    public int getCompositionType();
}
