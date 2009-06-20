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

package viper.comparison;

import java.io.*;
import java.util.*;

import viper.descriptors.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * An evaluation is a a high-level method of comparing 
 * two sets of descriptors. These include: object matching
 * evaluation, frame-by-frame evaluation at the pixel level,
 * and keyed-object tracking evaluation. Each evaluation 
 * technique has its benefits.
 */
public interface Evaluation {
	/**
	 * Parse the vreader section.
	 * 
	 * @param reader
	 *            VReader, already pointing at the begin evaluation directive.
	 * @param dcfgs
	 *            the target data - the evaluation uses the target names
	 * @throws IOException
	 */
	public void parseEvaluation(VReader reader, DescriptorConfigs dcfgs)
			throws IOException;

	/**
	 * Return a map of DescPrototypes to their evaluations.
	 * 
	 * @return a map of DescPrototypes to their evaluations
	 */
	public Map getMeasureMap();

	/**
	 * Print out information about evaluation
	 * 
	 * @param out
	 *            the output stream
	 */
	public void printMetricsTo(PrintWriter out);

	/**
	 * Print evaluation info in terse format
	 * 
	 * @param out
	 *            the raw stream
	 */
	public void printRawMetricsTo(PrintWriter out);

	/**
	 * Gets the name of the evalutation type.
	 * @return the name of the evaluation
	 */
	public String getName();

	/**
	 * Get an estimate on how long it will take to perform the evaluation.
	 * printEvaluation should call revealer.tick() exactly this many times.
	 * 
	 * @return the length of the ticker
	 */
	public int getTickerSize();

	/**
	 * Print out the results of this evaluation.
	 * 
	 * Since the evaluation operates on a CompMatrix level, the
	 * Evaluation.Information being passed back allows totals to be printed out
	 * at the end.
	 * 
	 * @return the evaluation information
	 */
	public Evaluation.Information printEvaluation();

	/**
	 * Prints the header information, usually about what
	 * metrics and rules were used in the evaluation.
	 */
	public void printHeader();
	
	/**
	 * Prints the footer information, including overall
	 * scores.
	 * @param total the total
	 */
	public void printFooter(Evaluation.Information total);

	/**
	 * Sets where human-readable information should be printed.
	 * @param output the human readable output stream
	 */
	public void setOutput(PrintWriter output);
	
	/**
	 * Gets the human readable output stream.
	 * @return the human readable output stream
	 */
	public PrintWriter getOutput();

	/**
	 * Sets the machine readable raw output stream.
	 * @param raw the machine readable stream
	 */
	public void setRaw(PrintWriter raw);

	/**
	 * Set to print headers or not. 
	 * @param on true to print headers
	 */
	public void setPrintingHeaders(boolean on);
	
	/**
	 * Tests to see if the header and footer should be
	 * printed out.
	 * @return if the header and footer should be printed
	 */
	public boolean isPrintingHeaders();

	/**
	 * Sets the ui for progress
	 * @param ticker the progress ui
	 */
	public void setTicker(Revealer ticker);

	/**
	 * Sets the matrix to evaluate.
	 * @param cm the matrix to evaluate
	 */
	public void setMatrix(CompMatrix cm);

	/**
	 * Evaluation.Information class for describing evaluation results
	 */
	public static interface Information {
		/**
		 * Indicates if anything was found. Useful for avoiding 0/0 errors.
		 * 
		 * @return <code></code> if the comparison graph was non-empty
		 */
		public boolean hasInformation();

		/**
		 * Get output suitable for .out files; may include new lines.
		 * 
		 * @return output suitable for .out files
		 */
		public String toVerbose();

		/**
		 * Get output suitable for .raw files. Must follow format given by
		 * getLayout().
		 * 
		 * @return output suitable for .raw files
		 */
		public String toString();

		/**
		 * Gets the layout for the toString format.
		 * 
		 * @return the layout string
		 */
		public String getLayout();

		/**
		 * Sums another information to this one, usually from two different
		 * CompMatrices.
		 * 
		 * @param other
		 *            the information to add to <code>this</code>
		 */
		public void add(Evaluation.Information other);

		/**
		 * Get a map of chart titles (actually, distance objects) to Datasets.
		 * 
		 * @param seriesName
		 *            Associate the series in each dataset with this name.
		 * @return the chart titles to data sets
		 */
		public Map getDatasets(String seriesName);
	}
}

