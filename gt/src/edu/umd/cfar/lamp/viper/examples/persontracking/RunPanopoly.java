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


package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.io.*;

import javax.swing.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

public class RunPanopoly {
	/**
	 * Runs the apploader using the gt-config.n3 preferences file.
	 * @param args these args are passed untouched to the {@link AppLoader#main(String[])}
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		} catch (UnsupportedLookAndFeelException e1) {
		}
		String oldPrefs = System.getProperty("lal.prefs");
		if (oldPrefs == null || "".equals(oldPrefs)) {
			System.setProperty("lal.prefs", "gallery-config.n3");
		}
		try {
			AppLoader.main(args);
		} catch (RDFException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (PreferenceException e) {
			throw new RuntimeException(e);
		}
	}
}
