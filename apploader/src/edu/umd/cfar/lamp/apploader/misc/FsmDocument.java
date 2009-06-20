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

package edu.umd.cfar.lamp.apploader.misc;

import javax.swing.text.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 10, 2003
 */
public  class FsmDocument extends PlainDocument {
	private StringParserFSM fsm;

	public String getValidPart() {
		fsm.reset();
		try {
			String text = getText(0, getLength());
			fsm.addString(text);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return fsm.getValidString();
	}

	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {
		fsm.reset();
		fsm.addString(getText(0, offs));
		fsm.addString(str);
		
		String newtext = fsm.toString();
		assert newtext.length() >= offs;
		if (newtext.length() <= offs) {
			// no changes
			return;
		}

		String rest = getText(offs, getLength()-offs);
		if (!fsm.addString(rest)) {
			// problem with rest - need to remove it.
			super.remove(offs, getLength()-offs);
		}
		String toInsert = newtext.substring(offs);
		super.insertString(offs, toInsert, a);
	}

	/**
	 * @return
	 */
	public StringParserFSM getFsm() {
		return fsm;
	}

	/**
	 * @param parserFSM
	 */
	public void setFsm(StringParserFSM parserFSM) {
		fsm = parserFSM;
	}
}
