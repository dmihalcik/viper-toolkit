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

package edu.umd.cfar.lamp.viper.gui.core;

import java.io.*;
import java.util.*;

import javax.swing.filechooser.FileFilter;

/**
 * Filters file based on a set of valid extensions.
 * Used for file selection dialogs.
 */
public class ExtensionFilter extends FileFilter {
	/**
	 * The file name extensions.
	 */
	public Set exts;
	
	/**
	 * The string describing this set of extensions.
	 */
	public String desc;
	
	/**
	 * Creates a new filter for the given set of extensions, 
	 * with the given textual description.
	 * @param exts The extensions to let through the filter
	 * @param desc Textual description for the filter selection dialog combo box
	 */
	public ExtensionFilter (Collection exts, String desc) {
		this.exts = new HashSet();
		this.exts.addAll(exts);
		this.desc = desc;
	}

	/**
	 * Tests to see if the file has an 
	 * acceptable extension.
	 * @param f the file to check
	 * @return if the file has an acceptable name
	 */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		} 
		String fname = f.getName().toLowerCase(); 
		int i = fname.lastIndexOf(".");
		if (i > -1) {
			fname = fname.substring(i+1);
		}
		return exts.contains(fname);
	}

	/**
	 * @inheritDoc
	 */
	public String getDescription() {
		return desc;
	}
}
