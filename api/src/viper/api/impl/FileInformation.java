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

package viper.api.impl;

import java.util.logging.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.time.*;

/**
 * This class holds a FILE Information descriptor and can perform 
 * some standard operations. Each <sourcefile> element can have its
 * own cfd. A cfd contains such information as frame size, number of
 * frames, and file names.
 */
public class FileInformation implements CanonicalFileDescriptor {
	private static final Logger log = Logger.getLogger("viper.api.impl");
	
	private static final String SOURCETYPE = "SOURCETYPE";
	private static final String NUMFRAMES = "NUMFRAMES";
	private static final String FRAMERATE = "FRAMERATE";
	private static final String H_FRAME_SIZE = "H-FRAME-SIZE";
	private static final String V_FRAME_SIZE = "V-FRAME-SIZE";

	private static final String[] allNames =
		{ SOURCETYPE, NUMFRAMES, FRAMERATE, H_FRAME_SIZE, V_FRAME_SIZE };

	/**
	 * Gets a new instance of the canonical file descriptor
	 * class definition.
	 * @return a new FILE Information config node
	 */
	public static final Config getCanonicalConfig() {
		return createCanonicalFileConfig();
	}

	private final static String SVALUE = ViperData.ViPER_DATA_URI + "svalue";
	private final static String LVALUE = ViperData.ViPER_DATA_URI + "lvalue";
	private final static String DVALUE = ViperData.ViPER_DATA_URI + "dvalue";
	private final static String FVALUE = ViperData.ViPER_DATA_URI + "fvalue";

	private static final ViperDataFactory DATA_FACTORY =
		new ViperDataFactoryImpl();

	private static final Config createCanonicalFileConfig() {
		Config c =
			new ConfigImpl("Information", Config.FILE);
		for (int i = 0; i < allNames.length; i++) {
			helpAddToProto(c, allNames[i]);
		}
		return c;
	}
	private static final void helpAddToProto(Config P, String type)
		throws IllegalArgumentException {
		ConfigImpl ci = (ConfigImpl) P;
		String uri;
		AttrValueWrapper p;
		if (false) {
			uri = SVALUE;
			p = DATA_FACTORY.getAttribute(uri);
		} else if (type.equals(SOURCETYPE)) {
			uri = LVALUE;
			p =
				new Lvalue(new String[] { "SEQUENCE", "FRAMES" });
		} else if (type.equals(FRAMERATE)) {
			uri = FVALUE;
			p = DATA_FACTORY.getAttribute(uri);
		} else if (
			type.equals(NUMFRAMES)
				|| type.equals(H_FRAME_SIZE)
				|| type.equals(V_FRAME_SIZE)) {
			uri = DVALUE;
			p = DATA_FACTORY.getAttribute(uri);
		} else {
			throw new IllegalArgumentException("Not a type supported: " + type);
		}
		AttrConfig temp =
			new AttrConfigImpl(ci, type, uri, false, null, p);
		ci.helpSetChild(ci.getNumberOfChildren(), temp, null, true);
	}

	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#getConfig()
	 */
	public Config getConfig() {
		return proto;
	}

	private Config proto;
	private Descriptor D;
	private MediaElement ref;

	/**
	 * Constructs a new FILE Information wrapper object.
	 */
	public FileInformation() {
		proto = createCanonicalFileConfig();
	}
	
	/**
	 * Adds the appropriate attribute config nodes to the 
	 * given descriptor config to make it a FILE Information node.
	 * @param prototype the config to change
	 */
	public static void initConfig(Config prototype) {
		for (int i = 0; i < allNames.length; i++) {
			if (!prototype.hasAttrConfig(allNames[i])) {
				helpAddToProto(prototype, allNames[i]);
			}
		}
	}
	/**
	 * Wraps thhe given descriptor class.
	 * @param prototype the descriptor class to change and wrap
	 */
	public FileInformation(Config prototype) {
		proto = prototype;
		initConfig(proto);
	}

	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#getDescriptor()
	 */
	public Descriptor getDescriptor() {
		return D;
	}

	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#getNumFrames()
	 */
	public int getNumFrames() {
		return helpGetDval(D, NUMFRAMES);
	}
	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#setNumFrames(int)
	 */
	public void setNumFrames(int numberOfFramesInClip) {
		setNumFrames(numberOfFramesInClip, true);
	}
	
	/**
	 * Allows non-undoable changing of the frame count.
	 * @param numberOfFramesInClip the new frame count
	 * @param undoable whether to allow undoing
	 */
	public void setNumFrames(int numberOfFramesInClip, boolean undoable) {
		if (numberOfFramesInClip > 0) {
			helpSetDval(D, NUMFRAMES, numberOfFramesInClip, undoable);
		} else {
			helpSetNull(D, NUMFRAMES, undoable);
		}
	}

	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#getMediaType()
	 */
	public String getMediaType() {
		return helpGetSval(D, SOURCETYPE);
	}
	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#setMediaType(java.lang.String)
	 */
	public void setMediaType(String mediaType) {
		setMediaType(mediaType, true);
	}
	/**
	 * Allows non-undoable setting of the media-type property.
	 * @param mediaType the new media type
	 * @param undoable whether to allow undoing
	 */
	public void setMediaType(String mediaType, boolean undoable) {
		helpSetSval(D, SOURCETYPE, mediaType, undoable);
	}

	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#getDimensions()
	 */
	public int[] getDimensions() {
		return new int[] {
			helpGetDval(D, H_FRAME_SIZE),
			helpGetDval(D, V_FRAME_SIZE)};
	}
	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#setDimensions(int, int)
	 */
	public void setDimensions(int width, int height) {
		helpSetDval(D, H_FRAME_SIZE, width, true);
		helpSetDval(D, V_FRAME_SIZE, height, true);
	}

	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#getFrameRate()
	 */
	public FrameRate getFrameRate() {
		return ref.getFrameRate();
	}
	/**
	 * @see viper.api.extensions.CanonicalFileDescriptor#setFrameRate(viper.api.time.FrameRate)
	 */
	public void setFrameRate(FrameRate rate) {
		setFrameRate(rate, true);
	}
	/**
	 * Allows non-undoable setting of the frame rate.
	 * @param rate the new rate
	 * @param undoable whether to allow undoing
	 */
	public void setFrameRate(FrameRate rate, boolean undoable) {
		ref.setFrameRate(rate);
		if (rate instanceof RationalFrameRate) {
			helpSetFval(D, FRAMERATE, ((RationalFrameRate) rate).getRate(), undoable);
		} else {
			log.warning("Cannot record non-rational frame rate: " + rate);
		}
	}

	private NodeListener nl = new NodeListener() {
		public void nodeChanged(NodeChangeEvent nce) {
			reset();
		}
		public void minorNodeChanged(MinorNodeChangeEvent mnce) {
			reset();
		}
		public void majorNodeChanged(MajorNodeChangeEvent mnce) {
			reset();
		}};

	/**
	 * Sets the canonical descriptor.
	 * @param cfd the canonical file descriptor to use as source/recipient
	 * of all information about the media file, such
	 * as frame rate 
	 */
	public void set(Descriptor cfd) {
		if (!proto.equals(cfd.getConfig())) {
			proto = cfd.getConfig();
			initConfig(proto);
		}
		D = cfd;
		ref = ((Sourcefile) D.getParent()).getReferenceMedia();
		if (D instanceof EventfulNode) {
			((EventfulNode) D).addNodeListener(nl);
		}
		reset();
	}
	
	private void reset() {
		double r = helpGetFval(D, FRAMERATE);
		FrameRate rate = ref.getFrameRate();
		if ((rate == null && r != 0) || ((rate instanceof RationalFrameRate) && ((RationalFrameRate) rate).getRate() != r)) {
			if (r == 0) {
				ref.setFrameRate(null);
			} else {
				ref.setFrameRate(new RationalFrameRate(r));
			}
		}
		int i = helpGetDval(D, NUMFRAMES);
		if (i >= 1) {
			ref.setSpan(new Span(new Frame(1), new Frame(1+i)));
		}
	}

	/**
	 * Gets the string value of the given svalue attribute.
	 * WARNING: Currently returns Strings with the value "NULL" as null.
	 * This should probably be changed when viper-gt finally implements quoting
	 * properly (eg null is keyword, so don't quote it)
	 * @param d the descriptor to get the value from
	 * @param attr the name of the attribute
	 * @return the string
	 */
	private static String helpGetSval(Descriptor d, String attr) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr != null) {
			return (String) tempAttr.getAttrValue();
		} else {
			return null;
		}
	}

	private static void helpSetSval(Descriptor d, String attr, String val, boolean undo) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			helpSet(tempAttr, val, undo);
		}
	}

	private static int helpGetDval(Descriptor d, String attr) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr != null) {
			Object o = tempAttr.getAttrValue();
			if (o == null || !(o instanceof Number)) {
				return 0;
			} else {
				return ((Number) o).intValue();
			}
		} else {
			return 0;
		}
	}
	private static void helpSetDval(Descriptor d, String attr, int val, boolean undo) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			Integer nv = new Integer(val);
			helpSet(tempAttr, nv, undo);
		}
	}
	private static void helpSet(Attribute tempAttr, Object nv, boolean undo) {
		if (undo || !(tempAttr instanceof AttributeImpl)) {
			tempAttr.setAttrValue(nv);
		} else {
			AttributeImpl ai = (AttributeImpl) tempAttr;
			ai.setAttrValueDontUndo(nv);
		}
	}

	private static double helpGetFval(Descriptor d, String attr) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr != null) {
			Object o = tempAttr.getAttrValue();
			if (o == null || !(o instanceof Number)) {
				return 0;
			} else {
				return ((Number) o).doubleValue();
			}
		} else {
			return 0;
		}
	}
	private static void helpSetFval(Descriptor d, String attr, double val, boolean undo) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			helpSet(tempAttr, new Double(val), undo);
		}
	}

	/**
	 * Sets the attribute value to NULL.
	 * @param d the descriptor to set the value on
	 * @param attr the name of the attribute to change
	 * @param undo if the action should be undoable
	 */
	private static void helpSetNull(Descriptor d, String attr, boolean undo) {
		Attribute tempAttr = d.getAttribute(attr);
		if (tempAttr == null) {
			throw new IllegalArgumentException("Not an attribute: " + attr);
		} else {
			helpSet(tempAttr, null, undo);
		}
	}
}
