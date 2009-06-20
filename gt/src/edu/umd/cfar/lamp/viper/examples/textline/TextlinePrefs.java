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

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Mar 21, 2005
 *
 */
public class TextlinePrefs {
	private Properties props = new Properties();
	private static TextlinePrefs singleton = null;
	public static final String PROPS_FILE = System.getProperty("user.home")+"/.viper/TextlinePrefs.ini";
	private static Logger log = Logger.getLogger("edu.umd.cfar.lamp.viper.examples.textline");
	
	private TextlinePrefs() {
		try {
			props.load(new FileInputStream(PROPS_FILE));
		} catch (IOException e) {
			log.log(Level.WARNING, "Error while loading TextlinePrefs from file: " + PROPS_FILE, e);
		}
	}
	
	public static TextlinePrefs createPrefs() {
		if(singleton == null) singleton = new TextlinePrefs();
		return singleton;
	}

	public Properties getProperties() {
		return props;
	}
	
	// called whenever the prefs are changed
	public static void reset() {
		singleton = null;
	}
}
