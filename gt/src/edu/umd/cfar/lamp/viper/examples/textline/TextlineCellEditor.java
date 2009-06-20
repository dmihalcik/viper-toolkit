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

import viper.api.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * JTable cell editor for the omega head model, which is basically an
 * oriented box with an ellipse inside. Currently, the parameters
 * are the center of the box, the width and height, and the orientation in 
 * degrees.
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 20, 2005
 */
public class TextlineCellEditor extends ViperDataFsmTextEditor {
	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.table.DataEditor#setNode(viper.api.Node)
	 */
	
	private TextlineModel tm;
	
	/**
	 * Saves the {@link TextlineModel} object before editing so we can use it after the edit
	 * @param n {@inheritDoc}
	 */
	public void setNode(Node n) {
		// differentiate between AttrConfigs and Attributes
		if (n instanceof AttrConfig) {
			tm = (TextlineModel) ((AttrConfig) n).getDefaultVal();
		} else if (n instanceof Attribute) {
			tm = (TextlineModel) getMediator().getAttributeValueAtCurrentFrame((Attribute) n);
		} else { // this shouldn't happen
			// TODO: throw error/log something
			tm = new TextlineModel();
		}
		super.setNode(n);
	}
	
	public TextlineCellEditor() {
		super(new IntegerListFSM(5)); // modified FSM that can handle 5 ints and a string
	}

	public Object parse(String s) {
		if (null == s || "".equals(s)) {
			return null;
		} else {
			try {
				int data[] = new int[5];
				String tok[] = s.split(" ", 6);
				
				// read in the first 5 tokens as ints
				for (int i = 0; i < data.length; i++)
					data[i] = Integer.parseInt(tok[i]);
				data[data.length-1] = data[data.length-1] % 360; // make sure degrees of rotation are in range
				
				// return a new TextlineModel object from the string data and the stored occlusions and offsets
				if(tm == null) { // NPE protection
					return new TextlineModel(data[0], data[1], data[2], data[3], data[4], "", new ArrayList(), new ArrayList());
				} else {
					return new TextlineModel(data[0], data[1], data[2], data[3], data[4], tm.getText(null), tm.getOcclusions(), tm.getWordOffsets());
				}
			
			} catch (BadAttributeDataException badx) {
				badx.printStackTrace();
				return null;
			} catch (NumberFormatException nfx) {
				throw new IllegalArgumentException("That wasn't a number - " + s);
			} catch (NoSuchElementException nsex) {
				throw new IllegalArgumentException("Not enough numbers in textline - " + s);
			}
		}
	}
}
