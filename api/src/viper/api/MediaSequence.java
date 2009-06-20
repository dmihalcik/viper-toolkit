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

import java.util.*;

import viper.api.time.*;

/**
 * Sequence of media files to be treated as a single file.
 */
public interface MediaSequence extends MediaElement {
	/**
	 * Gets <code>String</code> names of the individual components. 
	 * @return the Sourcefile components of this sequence, unless it is an
	 * atomic sourcefile, in which case, this returns <code>null</code>.
	 */
	public Iterator getComponentsOfSequence();
	
	/**
	 * Add a sourcefile to the end of this sequence. 
	 * @param element the sourcefile. Note that you shouldn't add
	 * a sequence that contains this sequence as a subsequence.
	 */
	public void addElementToSequence(Sourcefile element);


	/**
	 * Add a sourcefile at the specified location within this sequence. 
	 * @param index 
	 * @param element the sourcefile. Note that you shouldn't add
	 * a sequence that contains this sequence as a subsequence.
	 */
	public void addElementToSequence(int index, Sourcefile element);
	
	/**
	 * Removes the media element from the sequence at the specified index 
	 * @param index the index of the element to remove
	 */
	public void removeElement(int index);
	
	/**
	 * Gets the index of the first instance of the subsequence.
	 * @param element a sourcefile to search for in this playlist. 
	 * @return
	 */
	public int findElement(Sourcefile element);
	
	/**
	 * Gets the first moment of the indexth element.
	 * @param index the media element to check
	 * @throws IndexOutOfBoundsException
	 * @return <code>Instant</code> at which the element begins
	 */
	public Instant startOf(int index);
}
