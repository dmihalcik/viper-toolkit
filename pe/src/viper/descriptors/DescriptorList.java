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

package viper.descriptors;

import java.util.*;

import org.w3c.dom.*;

import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A DescriptorList contains all Descriptors associated with a given file.
 *  
 */
public interface DescriptorList extends Cloneable, Collection {
	/**
	 * Sets the equivalency map of the list.
	 * 
	 * @param map
	 *            the new equivalency map of the list.
	 */
	public void setMap(Equivalencies map);

	/**
	 * Gets the Equivalency map for this list.
	 * 
	 * @return the equivalency map of the list.
	 */
	public Equivalencies getMap();

	/**
	 * Checks to see if a Descriptor with this category and title is in the
	 * list.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @return true if one or more are found
	 */
	public boolean hasDescriptor(String type, String name);

	/**
	 * Returns the first Descriptor in the list with the specified category and
	 * title. Returns null if none is found.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @return a Descriptor with the specified type and title
	 */
	public Iterator getNodesByType(String type, String name);

	/**
	 * Returns all Descriptors with the given id number.
	 * 
	 * @param idNumber
	 *            the number to look for
	 * @return the descriptors with the given id. Note that there can only be
	 *         one of each declared descriptor type
	 */
	public Iterator getNodesByID(int idNumber);

	/**
	 * Returns the Descriptor with the given id number and type.
	 * 
	 * @param type
	 *            the type of the descriptor
	 * @param name
	 *            the name of the descriptor
	 * @param idNumber
	 *            the id number of the descriptor
	 * @return the descriptor, if it exists. Otherwise, <code>null</code>.
	 */
	public Descriptor getNodeByID(String type, String name, int idNumber);

	/**
	 * Returns descriptors that contain the subspan, or some of the subspan,
	 * with their framespan set to the subspan intersected with the span.
	 * 
	 * @param subspan
	 *            the span to check
	 * @return all descriptors that intersect with the span
	 */
	public Iterator getNodesByFrame(FrameSpan subspan);

	/**
	 * Like getNodesByFrame, but instead, the Iterator returns copies of the
	 * Descriptor objects contained here, each cropped to contain no frames
	 * beyond the specified FrameSpan.
	 * 
	 * @param span
	 *            the span to get values cropped to
	 * @return cropped copies of the descriptors in the span
	 */
	public Iterator cropNodesToSpan(FrameSpan span);

	/**
	 * Prints out all Descriptor objects in the list.
	 * 
	 * @param output
	 *            where to print the information
	 */

	/**
	 * Returns the greatest frame index of all Descriptor objects.
	 * 
	 * @return the last frame index of all Descriptor objects
	 */
	public int getHighestFrame();

	/**
	 * Returns the least frame index of all Descriptor objects.
	 * 
	 * @return the first frame index of all Descriptor objects
	 */
	public int getLowestFrame();

	/**
	 * Gets all id numbers currently in use.
	 * 
	 * @return the id number list
	 */
	public List getIds();

	/**
	 * Returns an XML tree that is a list of Descriptors in XML format.
	 * 
	 * @param root
	 *            The document that will contain the tree
	 * @return A w3c Element node with lots of children
	 */
	public Element getXMLFormat(Document root);

	/**
	 * Adds a new descriptor to this set. Unlike a simple call to add, this will
	 * reset the object ID to be unique to this list.
	 * 
	 * @param desc
	 *            the descriptor to add
	 * @throws UnsupportedOperationException
	 */
	public void addDescriptor(Descriptor desc)
			throws UnsupportedOperationException;

	/**
	 * Gets the number of descriptors currently held by this list.
	 * 
	 * @return number of elements in list
	 */
	public int size();

	/**
	 * Get the <code>i</code> th descriptor in the list.
	 * 
	 * @param i
	 *            the offset into the list
	 * @throws IndexOutOfBoundsException
	 *             if there are not at least i+1 elements in the list
	 * @return Descriptor <code>i</code>
	 */
	public Object get(int i);

	/**
	 * Gets the parent set of descriptor lists.
	 * 
	 * @return the parent holder
	 */
	public DescriptorData getParent();

	/**
	 * Get a collection of all of the descriptors declared for this source file.
	 * 
	 * @return {@link java.util.Collection}of {@link viper.api.Descriptor}s
	 */
	public Collection getAllDescriptors();

	/**
	 * Gets file information. See all of the caveats therein -- it may be null,
	 * or it may not have any useful values.
	 * 
	 * @return the canonical file descriptor associated with this metadata
	 */
	public CanonicalFileDescriptor getFileInformation();

	/**
	 * Will try to set the canonical file descriptor. Note that any attributes
	 * that are not in the prototype that was parsed from file (or added later)
	 * should not be set. The policy on how to treat the SOURCEFILES attribute
	 * may be implementation dependent (use it for ordering, although it may
	 * also be used to remove files if you wish).
	 * 
	 * @param cfd
	 *            the new canonical file descriptor
	 */
	public void setFileInformation(CanonicalFileDescriptor cfd);
	
	/**
	 * Makes a copy of this list.
	 * @return a copy of this list
	 */
	public Object clone();
}