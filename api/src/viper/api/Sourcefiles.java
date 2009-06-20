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

/**
 * The Sourcefiles node is a child of the ViperData node.
 * It represents a collection of Sourcefile elements.
 * This interface is mostly redundant with the ViperData 
 * interface, which already provides access to each Sourcefile.
 * However, it provides a uniform structure for accessing the 
 * data as a tree. It is a child of a ViperData node and a 
 * sibling to a Configs node.
 */
public interface Sourcefiles extends Node {

}
