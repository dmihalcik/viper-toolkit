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

package viper.api.extensions;

import viper.api.*;

/**
 * A Sourcefile node that references a CFD should also 
 * implement this interface. It is useful to provide a standard
 * FILE descriptor that contains some basic information
 * about all video files that is useful in evaluation and 
 * editing.
 */
public interface CanonicalSourcefile extends Sourcefile {
	/**
	 * Gets the canonical file descriptor associated with this 
	 * sourcefile.
	 * @return the information associated with this source file
	 */
	public abstract CanonicalFileDescriptor getCanonicalFileDescriptor();
}
