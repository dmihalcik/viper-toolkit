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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.util.*;
import org.apache.commons.lang.builder.*;
import viper.api.Attribute;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Represents the underlying model for the Textline object. 
 * All current occlusions are stored as IntPairs in the 
 * occlusions ArrayList. All word bounding lines are stored 
 * as positive x offsets from the left side of the box in
 * wordOffsets ArrayList. The characters on which words are
 * split are determined by WORD_DELIM_REGEX.
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 12, 2005
 *
 */

public class TextlineModel implements BoxInformation, HasCentroid, Moveable {
	private OrientedBox obox;
	private ArrayList occlusions;
	private ArrayList wordOffsets;
	private String text;
	private Attribute textPointer;
	private final String WORD_DELIM_REGEX = "[ ]"; /** All characters to treat as word delimiters */
	
	/**
	 * Default constructor
	 */
	public TextlineModel() {
		obox = new OrientedBox(0,0,0,0,0);
		occlusions = new ArrayList();
		wordOffsets = new ArrayList();
		setText("", null);
		textPointer = null;
	}
	
	/** 
	 * Constructor with dimensions only (probably most commonly used)
	 * @param x the x-coordinate of the origin
	 * @param y the y-coordinate of the origin
	 * @param width the width of the box
	 * @param height the height of the box
	 * @param rotation the orientation of the box (in positive degrees)
	 */
	public TextlineModel(int x, int y, int width, int height, int rotation) {
		this();
		obox = new OrientedBox(x, y, width, height, rotation);
		occlusions = new ArrayList();
		wordOffsets = new ArrayList();
	}
	
	/** 
	 * Constructor with dimensions and text
	 * @param x the x-coordinate of the origin
	 * @param y the y-coordinate of the origin
	 * @param width the width of the box
	 * @param height the height of the box
	 * @param rotation the orientation of the box (in positive degrees)
	 * @param text the text contained by the box
	 */
	public TextlineModel(int x, int y, int width, int height, int rotation, String textIn) {
		this();
		obox = new OrientedBox(x, y, width, height, rotation);
		occlusions = new ArrayList();
		wordOffsets = new ArrayList();
		setText(textIn, null);
	}

	/**
	 * Constructor with dimensions, text, and occlusion and offset lists
	 * (used by AttributeWrapperTextline for XML deserialization)
	 * 
	 * @param x the x-coordinate of the origin
	 * @param y the y-coordinate of the origin
	 * @param width the width of the box
	 * @param height the height of the box
	 * @param rotation the orientation of the box (in positive degrees)
	 * @param text the text contained by the box
	 * @param occ the ArrayList object containing the occlusions
	 * @param off the ArrayList object containing the word offsets
	 */
	public TextlineModel(int x, int y, int width, int height, int rotation, String textIn, ArrayList occ, ArrayList off) {
		this();
		obox = new OrientedBox(x, y, width, height, rotation);
		occlusions = (ArrayList) occ.clone();
		wordOffsets = (ArrayList) off.clone();
		setText(textIn, null);
	}

	/**
	 * Constructor with dimensions as array, text, and occlusion and offset lists
	 * (used by TextlineInterpolator to create the interpolated object)
	 * 
	 * @param oboxArray the array of 5 ints describing the OrientedBox
	 * @param text the text contained by the box
	 * @param occ the ArrayList object containing the occlusions
	 * @param off the ArrayList object containing the word offsets
	 */
	public TextlineModel(int[] oboxArray, String textIn, ArrayList occ, ArrayList off) {
		this();
		obox = new OrientedBox(oboxArray);
		occlusions = (ArrayList) occ.clone();
		wordOffsets = (ArrayList) off.clone();
		setText(textIn, null);
	}
	
	/** Adds a new word offset */
	public void addWordOffset(int offset) {
		wordOffsets.add(new Integer(offset));
	}
	
	/** Removes the given offset from the list (first match only) 
	 * @return true if found and removed, false if not found */
	public boolean removeWordOffset(int offset) {
		int i = wordOffsets.indexOf(new Integer(offset));
		if(i != -1) {
			wordOffsets.remove(i);
			return true;
		} else return false;
	}
	
	/** Adds a new occlusion pair -- convenience method for two ints */
	public void addOcclusion(int start, int end) {
		occlusions.add(new IntPair(start,end));
	}
	
	/** Adds a new occlusion pair */
	public void addOcclusion(IntPair occl) {
		occlusions.add(occl);
	}
	
	/** Removes the given occlusion from the list (first match only) 
	 * @return true if found and removed, false if not found */
	public boolean removeOcclusion(IntPair occl) {
		int i = occlusions.indexOf(occl);
		if(i != -1) {
			occlusions.remove(i);
			return true;
		} else return false;
	}

	/**
	 * Directly change the value of this textbox.
	 * @param x the x-coordinate of the origin
	 * @param y the y-coordinate of the origin
	 * @param width the width of the box
	 * @param height the height of the box
	 * @param rotation the orientation of the box (in positive degrees)
	 */
	public void set(int x, int y, int width, int height, int rotation) {
		obox.set(x, y, width, height, rotation);
	}

	/**
	 * @param text The text to set.
	 * @deprecated since text may be dynamic
	 */
	public void setText(String text, Instant when) {
		this.text = text;
		
		// do we need this line?
		//if(textPointer != null) textPointer.setAttrValue(text);
		
		// make sure that all extra offsets are deleted
		for(int i = getMaxBoundaries(when); i < wordOffsets.size(); /* no i++ on purpose*/ ) {
			wordOffsets.remove(i);
		}
	}
	
	public void setTextPointer(Attribute a) {
		textPointer = a;
	}
	
	/**
	 * @return Returns the text.
	 */
	public String getText(Instant i) {
		String retval = null;
		if(textPointer == null) {
			retval = text;
		} else if (i != null && textPointer.getAttrConfig().isDynamic()){
			retval = (String) textPointer.getAttrValueAtInstant(i);
		} else {
			retval = (String) textPointer.getAttrValue();
		}
		if(retval == null) return "";
		else return retval;
	}
	
	/**
	 * @return Returns the array of words.
	 */
	public String[] getWords(Instant i) {
		return getText(i).split(this.WORD_DELIM_REGEX);
	}
	
	/**
	 * @return Returns the array of words with placeholders for occluded characters
	 */
	public String[] getOccludedWords(Instant i) {
		// IMPORTANT: Return normal words if no occlusions present
		if(occlusions.size() == 0) {
			return getWords(i);
		}
		
		String text = getText(i);
		char letters[] = text.toCharArray();
		int a = 0, inrange = 2;
		IntPair currOcc = (IntPair) occlusions.get(a++);
		for(int idx = 0; idx < letters.length; idx++) {
			inrange = currOcc.rangeContains(idx);
			// if outside the range to the right, move on to the next pair
			if(inrange == 1) {
				if(a >= occlusions.size()) break; // exit loop if no occlusions left
				currOcc = (IntPair) occlusions.get(a++);
				inrange = currOcc.rangeContains(idx); // IMPORTANT: re-check
			}
			// if inside the range, then we need to occlude BUT NOT if it's a space
			if(inrange == 0 && letters[idx] != ' ') {
				letters[idx] = '-';
			}
			// nothing happens if out of range to the left--we wait until we get there
		}
		return (new String(letters)).split(this.WORD_DELIM_REGEX);
	}
	
	/**
	 * @return Returns the maximum number of possible word boundaries
	 */
	public int getMaxBoundaries(Instant i) {
		return getWords(i).length - 1;
	}

	/**
	 * @return Returns the height.
	 */
	public int getHeight() {
		return obox.getHeight();
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return obox.getWidth();
	}

	/**
	 * @return Returns the x.
	 */
	public int getX() {
		return obox.getX();
	}

	/**
	 * @return Returns the y.
	 */
	public int getY() {
		return obox.getY();
	}

	/**
	 * @return Returns the rotation factor.
	 */
	public int getRotation() {
		return obox.getRotation();
	}
	
	/**
	 * @return Returns the occlusions.
	 */
	public ArrayList getOcclusions() {
		return occlusions;
	}
	/**
	 * @return Returns the wordOffsets.
	 */
	public ArrayList getWordOffsets() {
		return wordOffsets;
	}

	/**
	 * @return Returns the occlusions as a space-separated string
	 */
	public String getOcclusionsAsStr() {
		String str = "";
		for(int i = 0; i < occlusions.size(); i++) {
			str += occlusions.get(i).toString() + " ";
		}
		return str;
	}
	/**
	 * @return Returns the wordOffsets as a space-separated string
	 */
	public String getWordOffsetsAsStr() {
		String str = "";
		for(int i = 0; i < wordOffsets.size(); i++) {
			str += wordOffsets.get(i).toString() + " ";
		}
		return str;
	}

	/**
	 * @param occlusions The occlusions to set.
	 */
	public void setOcclusions(ArrayList occlusions) {
		this.occlusions = occlusions;
	}
	
	/**
	 * @param wordOffsets The wordOffsets to set.
	 */
	public void setWordOffsets(ArrayList wordOffsets) {
		this.wordOffsets = wordOffsets;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		TextlineModel tm = new TextlineModel();
		tm.obox = (OrientedBox) obox.clone();
		tm.occlusions = (ArrayList) occlusions.clone();
		tm.wordOffsets = (ArrayList) wordOffsets.clone();
		tm.textPointer = textPointer;
		return tm;
	}
	
	/**
	 * @param point
	 * @return
	 */
	public boolean contains(Pnt point) {
		return obox.contains(point);
	}
	/**
	 * @return
	 */
	
	public Pnt getCentroid() {
		return obox.getCentroid();
	}
	/**
	 * @param q1
	 * @return
	 */
	
	public Pnt getNearIntersection(Pnt q1) {
		return obox.getNearIntersection(q1);
	}
	
	/**
	 * @param direction
	 * @param distance
	 * @return
	 */
	public Moveable move(int direction, int distance) {
		return obox.move(direction, distance);
	}
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public Moveable shift(int x, int y) {
		return obox.shift(x, y);
	}
	
	/**
	 * Gets a String representation of the textbox
	 * @return a space delimited string of integers and text 
	 */
	public String toString() {
		String ret = obox.toString();
		// TODO: return extra info during debugging:
		//ret += " Offsets: " + getWordOffsetsAsStr();
		//ret += " Occlusions: " + getOcclusionsAsStr();
		return ret;
	}
	
	// returns the underlying OrientedBox
	public OrientedBox getObox() {
		return obox;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TextlineModel) {
			TextlineModel o = (TextlineModel) obj;
			return new EqualsBuilder().append(obox, o.obox).append(wordOffsets, o.wordOffsets).append(occlusions, o.occlusions).isEquals();
		}
		return false;
	}
	public int hashCode() {
		// XXX: Consider putting some sort of label into the hashcode
		return new HashCodeBuilder().append(obox).append(wordOffsets).append(occlusions).toHashCode();
	}
	
	/**
	 * @return Returns the textPointer.
	 */
	public Attribute getTextPointer() {
		return textPointer;
	}
}
