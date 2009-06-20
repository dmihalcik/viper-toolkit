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

import javax.swing.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Runs the viper-gt application. 
 * 
 * To enable assertions, send this to the vm:
 * <pre>
 *    -ea
 * </pre>
 * To enable 'play nice with os x':
 * <pre>
 *    -Dapple.laf.useScreenMenuBar=true -Xdock:name="ViPER-GT" -Xdock:icon="common/icons/lamp.icns"
 * </pre>
 */
public class RunGT {

	/**
	 * Runs the apploader using the gt-config.n3 preferences file.
	 * @param args these args are passed untouched to the {@link AppLoader#main(String[])}
	 */
	public static void main(String[] args) {
		// XXX: hack to work around current inability to tile non-simple polygons
		PolyList.CARVE_POLYS = false;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		} catch (UnsupportedLookAndFeelException e1) {
		}
		String oldPrefs = System.getProperty("lal.prefs");
		if (oldPrefs == null || "".equals(oldPrefs)) {
			System.setProperty("lal.prefs", "gt-config.n3");
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
