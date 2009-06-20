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


package edu.umd.cfar.lamp.apploader;

/**
 * Interface for script extensions to the application.
 * 
 */
public interface AppLoaderScript {
	public void run(AppLoader application);
	public String getScriptName();
}
