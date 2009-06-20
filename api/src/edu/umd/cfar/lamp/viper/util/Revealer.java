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

package edu.umd.cfar.lamp.viper.util;

import java.io.*;
import java.util.*;

/**
 * Shows a series of dots, or a message, over a period of time, similar to the
 * dots that run across the screen in an id installer or the old PKZip.
 * 
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 */
public class Revealer {
	/** The thing to display. */
	private String originalUnit;
	/**
	 * The thing that is actually displayed. It might be longer than
	 * originalUnit.
	 */
	private String unit;
	/** Every time ticks reaches size, another unit is revealed. */
	private int size = 10;
	/**
	 * Ticks counts the number of times tick() has been called since last
	 * revealing.
	 */
	private int ticks;
	/** Where to print the output. */
	private PrintWriter out;
	/** How many ticks are counted at each call to tick(). */
	private int tickSize = 1;
	/** How many dots are wanted. */
	private int count;

	/** Error messages to print out when finished */
	private LinkedList errs = new LinkedList();

	/**
	 * Constructs a new Revealer that will print 10 dots and expects 10 calls
	 * to tick.
	 */
	public Revealer() {
		size = 10;
		count = 10;
		originalUnit = unit = ".";
		out = new PrintWriter(System.out);
	}

	private void processTicks() {
		if (ticks >= size) {
			do {
				ticks -= size;
				out.print(unit);
			} while (ticks >= size);
		}
		out.flush();
	}

	/**
	 * Indicate a single tick.
	 */
	public void tick() {
		ticks += tickSize;
		processTicks();
	}

	/**
	 * Indicate multiple ticks. Useful for when maxticks is too large.
	 * 
	 * @param times
	 *            the number of ticks that have passed.
	 */
	public void tick(int times) {
		ticks += tickSize * times;
		processTicks();
	}

	/**
	 * Creates a new revealer that, given total ticks, will print out the
	 * specified String count number of times to System.out.
	 * 
	 * @param total
	 *            The expected number of calls to tick.
	 * @param count
	 *            The wanted number of units to be printed.
	 * @param unit
	 *            The String to print out.
	 */
	public Revealer(int total, int count, String unit) {
		this(total, count, unit, new PrintWriter(System.out));
	}

	/**
	 * Creates a new revealer that, given total ticks, will print out the
	 * specified String count number of times to System.out.
	 * 
	 * @param total
	 *            The expected number of calls to tick.
	 * @param count
	 *            The wanted number of units to be printed.
	 * @param unit
	 *            The String to print out.
	 * @param output
	 *            Where to print the revealed stuff.
	 */
	public Revealer(int total, int count, String unit, PrintWriter output) {
		originalUnit = unit;
		this.unit = unit;
		if (total <= 0) {
			size = count;
		} else if (total < count) {
			tickSize = count / total;
			size = tickSize;
			for (int i = 1; i < tickSize; i++)
				this.unit += originalUnit;
		} else {
			size = total / count;
		}
		this.count = count;
		ticks = 0;
		out = output;
	}

	/**
	 * Print an end of line character.
	 */
	public void finish() {
		out.println();
		for (Iterator iter = errs.iterator(); iter.hasNext();) {
			out.println(iter.next().toString());
		}
	}

	/**
	 * Sends the error message to the list of errors 
	 * occurred during this reveal
	 * @param err the error message
	 */
	public void sendError(String err) {
		errs.add(err);
	}

	/**
	 * Reset the expected number of calls to ticks.
	 * 
	 * @param total
	 *            The new expected number of calls to tick().
	 */
	public void setTotal(int total) {
		unit = originalUnit;
		if (total <= 0) {
			size = count;
		} else if (total < count) {
			tickSize = count / total;
			size = tickSize;
			for (int i = 1; i < tickSize; i++)
				this.unit += originalUnit;
		} else {
			size = total / count;
		}
	}
}
