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

import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class holds a FILE Information descriptor and can perform some standard
 * operations. Each <sourcefile>element can have its own cfd. A cfd contains
 * such information as frame size, number of frames, and file names.
 */
public class CanonicalFileDescriptor implements Cloneable {
	private static final String SOURCEDIR = "SOURCEDIR";
	private static final String SOURCEFILES = "SOURCEFILES";
	private static final String SOURCETYPE = "SOURCETYPE";
	private static final String NUMFRAMES = "NUMFRAMES";
	private static final String H_FRAME_SIZE = "H-FRAME-SIZE";
	private static final String V_FRAME_SIZE = "V-FRAME-SIZE";

	private static final String[] allNames = {SOURCEDIR, SOURCEFILES,
			SOURCETYPE, NUMFRAMES, H_FRAME_SIZE, V_FRAME_SIZE};

	private static final DescPrototype PROTO;
	static {
		try {
			PROTO = new DescPrototype("FILE", "Information");
			for (int i = 0; i < allNames.length; i++) {
				helpAddToProto(PROTO, allNames[i]);
			}
		} catch (BadDataException bdx) {
			throw new IllegalArgumentException(bdx.getMessage());
		}
	}

	/**
	 * Gets a copy of the prototype for a canonical file descriptor. 
	 * @return the file descriptor
	 */
	public static final DescPrototype getPrototype() {
		return (DescPrototype) PROTO.clone();
	}

	/**
	 * Gets a copy of this file descriptor, which may have
	 * more attributes than the static prototype file descriptor.
	 * @return a copy of the user enhanced canonical file 
	 * descriptor
	 */
	public DescPrototype getThisPrototype() {
		return (DescPrototype) proto.clone();
	}

	private static final void helpAddToProto(DescPrototype P, String type)
			throws IllegalArgumentException {
		try {
			if (type.equals(SOURCEDIR) || type.equals(SOURCEFILES)) {
				P.addAttribute(type, "svalue");
			} else if (type.equals(SOURCETYPE)) {
				P.addAttribute(type, "lvalue", "SEQUENCE FRAMES");
			} else if (type.equals(NUMFRAMES) || type.equals(H_FRAME_SIZE)
					|| type.equals(V_FRAME_SIZE)) {
				P.addAttribute(type, "dvalue");
			} else {
				throw new IllegalArgumentException("Not a type supported: "
						+ type);
			}
		} catch (BadDataException bdx) {
			throw new IllegalArgumentException(bdx.getMessage());
		}
	}

	private DescPrototype proto;
	private Descriptor D;

	/** Extra attributes */
	private Set extras = new HashSet();

	/** 
	 * Constructs a new canonical file descriptor from the
	 * {@link #PROTO static prototype}.
	 */ 
	public CanonicalFileDescriptor() {
		proto = (DescPrototype) PROTO.clone();
		clear();
	}

	/**
	 * Constructs a new canonical file descriptor from the
	 * given prototype.
	 * @param proto the prototype to use. This will be modified
	 * to contain all of the prototypical file descriptor
	 * attributes.
	 */
	public CanonicalFileDescriptor(DescPrototype proto) {
		proto = (DescPrototype) proto.clone();
		extras = proto.getAttribNames();
		for (int i = 0; i < allNames.length; i++) {
			if (!extras.remove(allNames[i])) {
				helpAddToProto(proto, allNames[i]);
			}
		}

		this.proto = proto;
		clear();
	}

	/** @inheritDoc */
	public Object clone() {
		CanonicalFileDescriptor copy = new CanonicalFileDescriptor();
		copy.proto = (DescPrototype) this.proto.clone();
		copy.clear();
		copy.D = (Descriptor) this.D.clone();
		return copy;
	}

	/**
	 * Gets the instance of the file descriptor.
	 * @return the file descriptor
	 */
	public Descriptor getDescriptor() {
		return (Descriptor) D.clone();
	}

	/**
	 * Merges the given cfd with this cfd. Note that SOURCEFILES does not
	 * change.
	 * 
	 * @param other
	 *            the cfd to add to this cfd
	 */
	public void add(CanonicalFileDescriptor other) {
		if (other == null)
			return;

		setNumFrames(getNumFrames() + other.getNumFrames());

		String aMedia = getMediaType(), bMedia = other.getMediaType();
		setMediaType((aMedia == null) ? bMedia : (bMedia == null)
				? aMedia
				: (bMedia.equals(aMedia)) ? aMedia : "SEQUENCE");

		Set S = getSourceDirectorySet();
		S.addAll(other.getSourceDirectorySet());
		setSourceDirectorySet(S);

		int[] aDim = getDimensions(), bDim = other.getDimensions();
		setDimensions(Math.max(aDim[0], bDim[0]), Math.max(aDim[1], bDim[1]));

		for (Iterator attrs = extras.iterator(); attrs.hasNext();) {
			String name = (String) attrs.next();
			Attribute a = D.getAttribute(name);
			Attribute b = other.D.getAttribute(name);

			if (b != null && a != null) {
				String aVal = a.getValueToString();
				String bVal = b.getValueToString();
				if (aVal.equals("NULL") && !bVal.equals("NULL")) {
					try {
						a.setValue(bVal);
					} catch (BadDataException bdx) {
						System.err.println("Error: Invalid data"
								+ bdx.getMessage());
					}
				} else if (!aVal.equals(bVal)) {
					System.err
							.println("Error: Cannot combine multiple FILE attributes with different values: "
									+ aVal + "; " + bVal);
				}
			}
		}
	}

	/**
	 * Sets the identification number of the underlying descriptor.
	 * @param id the new ID number
	 */
	public void setID(int id) {
		D.setID(id);
	}

	/**
	 * Gets the number of frames in the video.
	 * @return the number of frames, according to the file descriptor
	 */
	public int getNumFrames() {
		return helpGetDval(D, "NUMFRAMES");
	}
	
	/**
	 * Sets the number of frames in the video.
	 * @param numberOfFramesInClip the new number of frames 
	 */
	public void setNumFrames(int numberOfFramesInClip) {
		if (numberOfFramesInClip > 0) {
			helpSetDval(D, "NUMFRAMES", numberOfFramesInClip);
		} else {
			helpSetNull(D, "NUMFRAMES");
		}
	}

	/**
	 * Gets the media type of the video.
	 * @return the media type, according to the file descriptor
	 */
	public String getMediaType() {
		return helpGetSval(D, "SOURCETYPE");
	}

	/**
	 * Sets the media type of the video.
	 * @param mediaType the new media type
	 */
	public void setMediaType(String mediaType) {
		helpSetSval(D, "SOURCETYPE", mediaType);
	}

	/**
	 * Gets the list of source media files.
	 * @return the list of source media files, according to the 
	 * file descriptor
	 */
	public String getSourceFiles() {
		return helpGetSval(D, "SOURCEFILES");
	}
	
	/**
	 * Sets the list of source media files.
	 * @param sourceFiles the new list of source media files
	 */
	public void setSourceFiles(String sourceFiles) {
		helpSetSval(D, "SOURCEFILES", sourceFiles);
	}

	/**
	 * Gets the source directories.
	 * @return the source directories
	 */
	public Set getSourceDirectorySet() {
		Set dirs = new HashSet();
		String sd = getSourceDirectory();
		if (sd != null) {
			StringTokenizer st = new StringTokenizer(sd, ""
					+ File.pathSeparatorChar);
			while (st.hasMoreTokens()) {
				dirs.add(st.nextToken());
			}
		}
		return dirs;
	}
	
	/**
	 * Sets the source directories.
	 * @param dirs the directories containing
	 * the files named in the sourcefiles attribute
	 */
	public void setSourceDirectorySet(Set dirs) {
		if (dirs != null && dirs.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (Iterator iter = dirs.iterator(); iter.hasNext();) {
				sb.append(iter.next()).append(File.pathSeparatorChar);
			}
			setSourceDirectory(sb.substring(0, sb.length() - 1));
		} else {
			setSourceDirectory(null);
		}
	}
	
	/**
	 * Gets the source directory attribute value.
	 * @return the source directory
	 */
	public String getSourceDirectory() {
		return helpGetSval(D, "SOURCEDIR");
	}
	
	/**
	 * Sets the source directory attribute value.
	 * @param sourceDir the new source directory
	 */
	public void setSourceDirectory(String sourceDir) {
		helpSetSval(D, "SOURCEDIR", sourceDir);
	}

	/**
	 * Gets the x/y dimension.
	 * @return the dimensions, in x/y
	 */
	public int[] getDimensions() {
		return new int[]{helpGetDval(D, "H-FRAME-SIZE"),
				helpGetDval(D, "V-FRAME-SIZE")};
	}
	
	/**
	 * Sets the dimensions
	 * @param width the new width
	 * @param height the new height
	 */
	public void setDimensions(int width, int height) {
		helpSetDval(D, "H-FRAME-SIZE", width);
		helpSetDval(D, "V-FRAME-SIZE", height);
	}

	/**
	 * Clears the cfd value.
	 */
	public void clear() {
		D = proto.create();
	}

	/**
	 * Sets the canonical file descriptor to the given FILE Information
	 * descriptor
	 * 
	 * @param cfd
	 *            the new descriptor
	 * @return a list of errors in the descriptor format.
	 */
	public String set(Descriptor cfd) {
		return set(cfd, false);
	}

	/**
	 * Sets the canonical file descriptor to the given FILE Information
	 * descriptor
	 * 
	 * @param cfd
	 *            the new descriptor
	 * @param fixSourcefileThatStartAt0
	 *            When true, if the SOUCEFILES attribute starts at frame 0, add
	 *            one to all numbers in the string.
	 * @return a list of errors in the descriptor format.
	 */
	public String set(Descriptor cfd, boolean fixSourcefileThatStartAt0) {
		if (cfd == null) {
			clear();
			return null;
		}
		String error = "";
		String sourceFileNames = helpGetSval(cfd, "SOURCEFILES");
		if (sourceFileNames == null) {
			sourceFileNames = helpGetSval(cfd, "SOURCES");
			if (sourceFileNames == null) {
				error += "\nNo FILE Information descriptor has no SOURCEFILES attribute.";
			} else {
				sourceFileNames = "1 " + sourceFileNames;
			}
		} else if (fixSourcefileThatStartAt0
				&& sourceFileNames.startsWith("0 ")) {
			StringBuffer b = new StringBuffer();
			StringTokenizer sfs = new StringTokenizer(sourceFileNames);
			while (sfs.hasMoreTokens()) {
				try {
					int nextFrame = Integer.parseInt(sfs.nextToken());
					String nextFile = sfs.nextToken();
					b.append(nextFrame + 1).append(' ');
					b.append(nextFile).append(' ');
				} catch (NoSuchElementException nsex) {
					error += "\nUnmatched frame number in SOURCEFILES attribute";
				}
			}
			sourceFileNames = b.substring(0, b.length() - 1);
		} else {
			sourceFileNames = sourceFileNames.trim();
		}

		if (null == sourceFileNames) {
			error += "\nNo SOURCEFILES attribute found.";
		}

		// Set sourcefiles, etc.
		D = proto.create();
		setSourceFiles(sourceFileNames);
		setNumFrames(helpGetDval(cfd, NUMFRAMES));
		setMediaType(helpGetSval(cfd, SOURCETYPE));
		setSourceDirectory(helpGetSval(cfd, SOURCEDIR));
		setDimensions(helpGetDval(cfd, H_FRAME_SIZE), helpGetDval(cfd,
				V_FRAME_SIZE));

		for (Iterator names = extras.iterator(); names.hasNext();) {
			String name = (String) names.next();
			Attribute curr = D.getAttribute(name);
			AttributeValue from = cfd.getAttribute(name).getStaticValue();
			try {
				if (from == null) {
					curr.setStaticValue(null);
				} else {
					curr.setStaticValue(from);
				}
			} catch (BadDataException bdx) {
				error += "\nError getting file descriptor value: " + curr;
			}
		}

		if (error.length() > 0) {
			return error.substring(1);
		} else {
			return null;
		}
	}

	/**
	 * WARNING: Currently returns Strings with the value "NULL" as null. This
	 * should probably be changed when viper-gt finally implements quoting
	 * properly (eg null is keyword, so don't quote it)
	 * 
	 * @param d
	 *            the descriptor
	 * @param attr
	 *            the attribute name
	 * @return the vale of d's attr attribute
	 */
	private static String helpGetSval(Descriptor d, String attr) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr != null) {
			String s = tempAttr.getValueToString();
			if (s.equals("NULL")) {
				s = null;
			}
			return s;
		} else {
			return null;
		}
	}

	private static void helpSetSval(Descriptor d, String attr, String val) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			try {
				tempAttr.setValue(val);
			} catch (BadDataException bdx) {
				throw new IllegalArgumentException(bdx.getMessage());
			}
		}
	}

	private static int helpGetDval(Descriptor d, String attr) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr != null) {
			AttributeValue o = tempAttr.getValue(null, 0);
			if (o == null || !(o instanceof Number)) {
				return 0;
			} else {
				return ((Number) o).intValue();
			}
		} else {
			return 0;
		}
	}
	private static void helpSetDval(Descriptor d, String attr, int val) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			try {
				tempAttr.setValue(String.valueOf(val));
			} catch (BadDataException bdx) {
				throw new NumberFormatException(bdx.getMessage());
			}
		}
	}

	/**
	 * Sets the attribute value to NULL.
	 * 
	 * @param d
	 *            the descriptor on which to set
	 * @param attr
	 *            the attribute to set to null
	 */
	private static void helpSetNull(Descriptor d, String attr) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			try {
				tempAttr.setValue(null);
			} catch (BadDataException bdx) {
				throw new NumberFormatException(bdx.getMessage());
			}
		}
	}
}