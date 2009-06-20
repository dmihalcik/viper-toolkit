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
 * Contains all of the descriptors for a specific media file (source 
 * file).
 */
public interface Sourcefile extends TemporalNode {
	
	/**
	 * Gets the media element that this sourcefile node describes.
	 * @return The media element, useful for looking up and getting information
	 * about the actual file.
	 */
	public MediaElement getReferenceMedia();
	
	/**
	 * Get a collection of all of the descriptors declared for this 
	 * source file.
	 * Will soon be deprecated in favor of {@link #getDescriptors}.
	 * 
	 * @return {@link java.util.Collection} of {@link viper.api.Descriptor}s
	 */
	public Collection getAllDescriptors();
	
	
	/**
	 * Get a specific, known descriptor by its unique identity: its 
	 * type, name, and identity number.
	 * @param type  FILE, CONTENT, or OBJECT
	 * @param name  the name of the descriptor
	 * @param id    the id of the desired descriptor
	 * @return The descriptor that matches the parameters
	 * @throws NoSuchElementException if no descriptor with the 
	 *    specified combination exists
	 */
	public Descriptor getDescriptor(int type, String name, int id);
	
	/**
	 * Get a specific, known descriptor by its unique identity: its 
	 * definition and identity number.
	 * @param cfg the type of descriptor
	 * @param id    the id of the desired descriptor
	 * @return The descriptor that matches the parameters
	 * @throws NoSuchElementException if no descriptor with the 
	 *    specified combination exists
	 */
	public Descriptor getDescriptor(Config cfg, int id);

	/**
	 * Gets all descriptors declared for this source file.
	 * @return {@link java.util.Collection} of {@link viper.api.Descriptor}s
	 */
	public Iterator getDescriptors();

	/**
	 * Get all of the descriptors that are valid in some section
	 * of the given {@link viper.api.time.InstantInterval}. Note that the current
	 * implementation has a bug, as it just checks the Descriptor's 
	 * reported Range, not the Ranges of its dynamic attributes, which
	 * may bear no relation to it (although the Descriptor range is supposed
	 * to be a superset). 
	 * @param i The span to search.
	 * @return Iterator over all descriptors that are valid for some
	 *         frame in the given set of frames
	 * @throws UnknownFrameRateException if data is in time format and
	 *     there is no way to convert
	 */
	public Iterator getDescriptorsBy(InstantInterval i);

	/**
	 * Get all of the descriptors that are valid during
	 * the given {@link viper.api.time.Instant}. Note that the current
	 * implementation has a bug, as it just checks the Descriptor's 
	 * reported Range, not the Ranges of its dynamic attributes, which
	 * may bear no relation to it (although the Descriptor range is supposed
	 * to be a superset). 
	 * @param i The moment to check.
	 * @return Iterator over all descriptors that are valid for some
	 *         frame in the given set of frames
	 * @throws UnknownFrameRateException if data is in time format and
	 *     there is no way to convert
	 */
	public Iterator getDescriptorsBy(Instant i);

	/**
	 * Get all of the descriptors that are of the current Descriptor
	 * type. These are {@link Config#FILE}, {@link Config#OBJECT} and 
	 * {@link Config#CONTENT}.
	 *  
	 * @param type the descriptor type to search for
	 * @return Iterator of all descriptors of the type
	 */
	public Iterator getDescriptorsBy(int type);

	/**
	 * Get all of the descriptors that implement are members of the given
	 * class of Descriptors.
	 * 
	 * @param c the descriptor type to search for
	 * @return Iterator of all descriptors of the type
	 */
	public Iterator getDescriptorsBy(Config c);

	/**
	 * Gets a collection of all descriptors of a given Config
	 * at the specified time.
	 * @param c the config to get the instances of
	 * @param i the time to look for 
	 * @return Descriptors that are of the given type and valid at the given time
	 */
	public Iterator getDescriptorsBy(Config c, Instant i);

	/**
	 * Gets a collection of all descriptors of a given Config
	 * that are valid at some point within at the specified time.
	 * @param c the config to get the instances of
	 * @param i the time to look for 
	 * @return Descriptors that are of the given type and valid for some 
	 * subset of the given time
	 */
	public Iterator getDescriptorsBy(Config c, InstantInterval i);

	/**
	 * Get all of the descriptors within a given Span. Note that the current
	 * implementation has a bug, as it just checks the Descriptor's 
	 * reported Range, not the Ranges of its dynamic attributes, which
	 * may bear no relation to it (although the Descriptor range is supposed
	 * to be a superset). 
	 * @param s The span to search.
	 * @return collection of all descriptors that are valid for some
	 *         frame in the given set of frames
	 * @throws UnknownFrameRateException if data is in time format and
	 *     there is no way to convert
	 */
	public Collection getDescsByTime(InstantInterval s);

	/**
	 * Get all of the descriptors that say they have data at the
	 * given Instant.
	 * @param i  the time to search for
	 * @return collection of all descriptors that are valid for this
	 *         instant 
	 * @throws UnknownFrameRateException if data is in frame format and
	 *     there is no way to convert
	 */
	public Collection getDescsByTime(Instant i);

	/**
	 * Gets a collection of all descriptors with a given type, 
	 * presumably FILE or CONTENT.
	 * @param type  FILE, CONTENT, or OBJECT 
	 * @see Config
	 * @return Collection of Descriptors
	 * @deprecated
	 */
	public Collection getDescByType(int type);

	/**
	 * Gets a collection of all descriptors of a given Config.
	 * @param c the config to get the instances of
	 * @return Collection of Descriptors
	 * @deprecated
	 */
	public Collection getDescByName(Config c);

	/**
	 * Creates a new descriptor attached to this source file with 
	 * the given Config. Generates a new Descriptor id automatically.
	 * Note that the descriptor, by default, is invalid.
	 * You will have to set the valid range yourself.
	 * @param c  the Config that defines the descriptor
	 * @return new Descriptor defined by the given Config
	 */
	public Descriptor createDescriptor(Config c);
	
	/**
	 * Creates a new descriptor attached to this source file with 
	 * the given Config and id number. FIXME: Should throw an exception
	 * if id is already used, but now just adds two with the same descriptor?
	 * @param c  the Config that defines the descriptor
	 * @param id the descriptor id to assign the descriptor
	 * @return new Descriptor defined by the given Config
	 */
	public Descriptor createDescriptor(Config c, int id);
}
