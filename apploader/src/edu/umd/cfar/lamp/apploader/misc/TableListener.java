/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader.misc;

import java.util.*;

/**
 * Listener to receive higher-level table events.
 */
public interface TableListener extends EventListener {
	/**
	 * Context click (right-click).
	 * @param e
	 */
	public void contextClick(TableEvent e);
	/**
	 * Action click (double-left-click).
	 * @param e
	 */
	public void actionClick(TableEvent e);
	/**
	 * Simple click (single-left-click).
	 * @param e
	 */
	public void click(TableEvent e);
	/**
	 * Alt-click (center-click).
	 * @param e
	 */
	public void altClick(TableEvent e);
}
