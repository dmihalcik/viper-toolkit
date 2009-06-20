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

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A set of descriptors that can be associated with multiple source media files.
 */
public interface DescriptorData //extends viper.api.ViperData
{
	/**
	 * Gets the Equivalency map for this list.
	 * 
	 * @return the Equivalency map for this list
	 */
	public Equivalencies getMap();
	/**
	 * Sets the equivalency map of the list.
	 * 
	 * @param map
	 *            the new Equivalency map for this list
	 */
	public void setMap(Equivalencies map);

	/**
	 * The URI used while parsing XML data.
	 */
	public static final String NAMESPACE_URI = "http://lamp.cfar.umd.edu/viper#";

	/**
	 * Return the DescriptorList who describe the media with the specified
	 * filename or URL String.
	 * 
	 * @param fileName
	 *            the file to get the metadata of
	 * @return the descriptors for the given file
	 */
	public DescriptorList getForFile(String fileName);

	/**
	 * Returns the URL/filenames of all the media this DescriptorData describes.
	 * 
	 * @return the names of all the described files
	 */
	public Iterator getFileNames();

	/**
	 * Removes the given file name and its corresponding metadata.
	 * 
	 * @param filename
	 *            the file to remove
	 * @return the metadata for the file
	 */
	public DescriptorList removeFile(String filename);

	/**
	 * Adds a new URL/filename to the set that this object describes (Optional).
	 * 
	 * @param S
	 *            the name of the file to add
	 * @return true, if it was added
	 * @throws UnsupportedOperationException
	 */
	public boolean addFileName(String S) throws UnsupportedOperationException;

	/**
	 * Replaces the DesciptorList associated to a given filename with the
	 * DescriptorList that is passed in. Should be implemented if addFileName()
	 * is implemented.
	 * 
	 * @param S
	 *            the name of the file to change
	 * @param l
	 *            the new value
	 * @return the previous value
	 * @throws UnsupportedOperationException
	 */
	public DescriptorList setDataForFile(String S, DescriptorList l)
			throws UnsupportedOperationException;

	/**
	 * Reads in Descriptor data from the specified list of filenames with the
	 * specified config info.
	 * 
	 * @param allFiles
	 *            Vector of Strings containing the names of the files to search
	 *            for config information
	 * @param cfgs
	 *            the Descriptor configuration information
	 * @param limits
	 *            A RuleHolder to determine which descriptors to read in
	 * @param map
	 *            the equivalencies to use while parsing
	 */
	public void parseData(List allFiles, DescriptorConfigs cfgs,
			RuleHolder limits, Equivalencies map);

	/**
	 * Reads in Descriptor data from the specified XML input source.
	 * 
	 * @param input
	 *            the XML input source
	 * @param map
	 *            the attribute/descriptor equivalencies
	 * @param limits
	 *            A RuleHolder to determine which descriptors to read in
	 * @param cfgs
	 *            if this is <code>null</code>, the parser is looks for the
	 *            &lt;config&gt; element and generates its own config
	 *            information. If you want to do things like limitation parsing
	 *            and evaluations, you will have to parse the config info first.
	 * @param logfile
	 *            where error and notifications are written
	 * @throws IOException
	 * @throws BadDataException
	 */
	public void parseData(InputSource input, Equivalencies map,
			RuleHolder limits, DescriptorConfigs cfgs, PrintWriter logfile)
			throws IOException, BadDataException;

	/**
	 * Returns a string for .raw output summarizing what media files this
	 * descriptor uses and so forth. The data is formatted as
	 * 
	 * <PRE>
	 * 
	 * Directory Sequence mapping (list of frame / filename pairs) SEQUENCE |
	 * FRAMES Number of Frames
	 * 
	 * </PRE>
	 * 
	 * @return the file information in raw format
	 */
	public String getInformation();

	/**
	 * Determines if this describes more than one media file.
	 * 
	 * @return <code>true</code> if this describes more than one media file
	 */
	public boolean isMultifile();

	/**
	 * Returns the list as one long DescriptorList.
	 * 
	 * @return the list as one long DescriptorList, with the frame spans shifted
	 *         so the media files are serialized
	 */
	public DescriptorList getFlattenedData();

	/**
	 * Gets the <code>DescriptorConfigs</code> associated with this data.
	 * 
	 * @return the <code>DescriptorConfigs</code> associated with this data
	 */
	public DescriptorConfigs getDescriptorConfigs();

	/**
	 * Adds the data from the other DescriptorData to this one.
	 * 
	 * @param other
	 *            the data to merge from
	 */
	public void merge(DescriptorData other);

	/**
	 * Gets the data in ViPER XML format. Probably should be moved into own
	 * class...
	 * 
	 * @param root
	 *            the document to use while creating the element
	 * @return the viper root element
	 */
	public Element getXMLFormat(Document root);

	/**
	 * Resets all object ids. Useful for mixing and matching descriptors from
	 * different sources.
	 */
	public void resetIds();
}

