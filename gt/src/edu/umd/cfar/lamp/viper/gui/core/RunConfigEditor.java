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
import java.util.logging.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * A main-method class for opening the viper config editor.
 */
public class RunConfigEditor {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.core");

	/**
	 * Runs the application loader with the gtc-config 
	 * init file.
	 * @param args these are passed directly to the application
	 * loader
	 */
	public static void main(String[] args) {
		System.setProperty("lal.prefs", "gtc-config.n3");
		try {
			AppLoader.main(args);
		} catch (RDFException rdfx) {
			logger.log(Level.SEVERE, "Error starting viper-gt.", rdfx);
		} catch (IOException iox) {
			logger.log(Level.SEVERE, "Error starting viper-gt.", iox);
		} catch (PreferenceException px) {
			logger.log(Level.SEVERE, "Error starting viper-gt", px);
		}
	}
}
