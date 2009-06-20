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

/**
 * This interface should be used by all classes that perform 
 * parsing functions to write compiler-style errors. 
 * It is a set of standard error printing functions.
 * Classes that generate the errors should use specify the column numbers
 * if possible, as it makes the errors easier to understand.
 * I would recommend that implementors use the 
 * {@link StringHelp#underliner(boolean starts, boolean ends, 
 * int start, int end) StringHelp.underliner()} to implement the
 * *AtLineNumber functions.
 *
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 */
public interface ErrorWriter
{
  /**
   * Specify where to send the output. Usually, it is System.err,
   * but it could be a log file.
   *
   * @param pw A PrintWriter associated with the proper stream.
   */
  public void setOutput (PrintWriter pw);

  /**
   * Print an error at a specific place inside the current line.
   * Errors are something that prevents the file
   * from being understood usefully.
   *
   * @param message the message to print with the error
   * @param start the column where the error begins
   * @param stop the column where the error ends
   */
  public void printError (String message, int start, int stop);

  /**
   * Print an error. Errors are something that prevents the file
   * from being understood usefully. This is used when the error
   * refers to the whole line or the parser is unable to determine
   * where the error occurred in the line.
   *
   * @param message the message to print with the error
   * @see #printError(String message, int start, int stop)
   */
  public void printError (String message);

  /**
   * Print an error at a a specific line number.
   * Errors are something that prevents the file
   * from being understood usefully. This is used when the error
   * refers to a line that has already been parsed, but the has only 
   * now been proven incorrect.
   *
   * @param message The message to print with the error.
   * @param lineNum The line where the error occured.
   */
  public void printErrorAtLineNumber (String message, int lineNum);

  /**
   * Print an warning at a specific place inside the current line.
   *A warning may make the parser generate 
   * an improper representation or otherwise misinterpret 
   * the data, but not necessarily.
   *
   * @param message the message to print with the warning
   * @param start the column where the questionable part begins
   * @param stop the column where the questionable part ends
   */
  public void printWarning (String message, int start, int stop);

  /**
   * Print an warning. A warning may make the parser generate 
   * an improper representation or otherwise misinterpret 
   * the data, but not necessarily. This is used when the error
   * refers to the whole line or the parser is unable to determine
   * where the fault occurred in the line.
   *
   * @param message the message to print with the warning
   */
  public void printWarning (String message);

  /**
   * Print an warning at a a specific line number.
   * A warning may make the parser generate 
   * an improper representation or otherwise misinterpret 
   * the data, but not necessarily. This is used when the error
   * refers to a line that has already been parsed, but the has only 
   * now been discovered to be invalid.
   *
   * @param message the message to print with the warning
   * @param lineNum The line where the error occured.
   */
  public void printWarningAtLineNumber (String message, int lineNum);

  /**
   * Prints an error that is unconnected to a line number.
   * For example, error opening file, I/O exception, etc
   *
   * @param message the message to print with the warning
   */
  public void printGeneralError (String message);

  /**
   * Print out the error count totals, usually in the form
   * <PRE>
   *  x Errors, y Warnings, z General Errors
   * </PRE>
   */
  public void printErrorTotals ();
}


