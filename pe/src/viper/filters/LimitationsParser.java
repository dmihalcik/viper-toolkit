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

/************************************************************************* *
 ************************************************************************* *
 * File:        LimitationsParser.java
 * Purpose:     To be able to parse out the the limitations which will be used
 *              to figure out the subset from the test set and the result set
 *              which will be used for the comparissons.
 *
 * Written by:  Felix Sukhenko
 * Date:        12/1998
 * Notes:	(any usage or compilation notes)
 * ************************************************************************ *
 * Modification Log:
 *
 * DATE       WHO                MODIFICATION    
 *
 * ************************************************************************ *
 *      Copyright (C) 1997 by the University of Maryland, College Park
 *
 *		Laboratory for Language and Media Processing
 *		   Institute for Advanced Computer Studies
 *	       University of Maryland, College Park, MD  20742
 *
 *  email: lamp@cfar.umd.edu               http: documents.cfar.umd.edu/LAMP
 * ************************************************************************ *
 * ************************************************************************ */
package viper.filters;

import java.io.*;
import java.util.*;

import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * Purpose: Will parse out the limitations which will be used to figure out the
 * subset from the test set and the result set which will be used for
 * comparissons in the gtfC.
 */
public class LimitationsParser implements ErrorWriter {

	private long m_line_number, m_file_length, m_current_position;

	private PrintWriter m_out;

	private RandomAccessFile m_reader;

	private RuleHolder m_rule_holder;

	/**
	 * Creates a new limitations/rules parser.
	 */
	public LimitationsParser() {
		m_out = new PrintWriter(System.err, true);
	}

	/**
	 * Parses the file given for the block name that is given and for the
	 * limitations information that is found in that block.
	 * 
	 * @param file_path
	 *            The path to the file to parse.
	 * @param block_name
	 *            The filter block name, eg GROUND_FILTER
	 * @param d_vector
	 *            The descriptor configs to use.
	 * @return RuleHolder containing any rules in the block.
	 */
	public RuleHolder parseFile(String file_path, String block_name,
			DescriptorList d_vector) {
		// Should also have a DescriptorList passed to it
		try {
			m_rule_holder = new RuleHolder();
			if (openFile(file_path))
				parseFileFor(block_name, d_vector);
		} catch (IOException iox) {
			m_out.println("Failed to open or parse the file due to: "
					+ iox.getMessage());
			iox.printStackTrace();
		}
		return m_rule_holder;
	}

	/**
	 * Parses the current block in the VReader for the limitations information
	 * that is found in that block.
	 * 
	 * @param reader
	 *            The VReader, pointing to the begin directive.
	 * @param dcfgs
	 *            The descriptor configs to use.
	 * @return RuleHolder containing any rules in the block.
	 * @throws IOException
	 *             if parsing the file fails for some reason
	 */
	public static RuleHolder parse(VReader reader, DescriptorConfigs dcfgs)
			throws IOException {
		RuleHolder ruleH = new RuleHolder();
		String new_line = reader.advanceToNextRealLine();
		while (!reader.currentLineIsEndDirective()) {
			StringTokenizer tokenizer = new StringTokenizer(new_line);
			int tokens = tokenizer.countTokens();
			if (tokens < 2) {
				reader
						.printError("Descriptor type should have type and name, with optional span filter.");
				new_line = reader.advanceToNextRealLine();
				continue;
			}

			String category = tokenizer.nextToken();
			String name = tokenizer.nextToken();
			if (!Descriptor.isCategory(category)) {
				reader.printError("Not a valid descriptor category, "
						+ category + ", skipping.");
				new_line = reader.advanceToNextRealLine();
				continue;
			}

			Iterator relevantIter = dcfgs.getNodesByType(category, name);
			if (!relevantIter.hasNext()) {
				reader.printError("Not a descriptor type: " + category + " "
						+ name);
				new_line = reader.advanceToNextRealLine();
				continue;
			}
			Descriptor relevant = (Descriptor) relevantIter.next();

			if (tokens > 2) { // parse framespan filter
				int colon_index = new_line.indexOf(":");
				if (colon_index < 0) {
					reader.printWarning("DESCRIPTOR type has more than 2 words"
							+ " and no ':'. The last will be ignored.");
				} else {
					try {
						new_line = new_line.substring(colon_index + 1, new_line
								.length());
						new_line = new_line.trim();
						Filterable.Rule parsed_rule = parseRule(new_line,
								new FrameSpan(), reader);
						if (parsed_rule != null) {
							ruleH.addRule(category + name, " framespan",
									parsed_rule);
						}
					} catch (BadDataException bdx) {
						reader.printError(bdx.getMessage());
					}
				}
			} // Parsing framespan filter

			new_line = reader.advanceToNextRealLine();
			boolean isNewDescFilter = Descriptor
					.isCategory(new StringTokenizer(new_line).nextToken());
			while (!isNewDescFilter && !reader.currentLineIsEndDirective()) {
				int colon_index = new_line.indexOf(":");
				if (colon_index > 0) {
					String attribute = new_line.substring(0, colon_index)
							.trim();
					String rule = new_line.substring(colon_index + 1,
							new_line.length()).trim();
					try {
						Filterable toFilter = relevant.getFilterable(attribute);
						if (toFilter == null) {
							reader.printError("Unrecognized attribute name: "
									+ attribute);
						} else {
							Filterable.Rule parsed_rule = parseRule(rule,
									toFilter, reader);
							if (parsed_rule == null) {
								reader.printError("Filter parse error");
							} else {
								ruleH.addRule(category + name, attribute,
										parsed_rule);
							}
						}
					} catch (BadDataException bdx) {
						reader.printError(bdx.getMessage());
					}
				} else {
					reader
							.printError("Attribute filter missing colon; ignored.");
				}
				new_line = reader.advanceToNextRealLine();
				isNewDescFilter = Descriptor.isCategory(new StringTokenizer(
						new_line).nextToken());
			} // while an attribute line
		} // while not an end directive
		return ruleH;
	}

	/**
	 * The function that prints an error to the output stream.
	 * 
	 * @param error
	 *            the error that will be printed
	 */
	public void printError(String error) {
		m_out.println("LINE " + m_line_number
				+ ": ERROR WHILE PARSING FOR LIMITS.\n\t" + error);
		m_out.flush();
	}

	/**
	 * The function that prints an error and a line number of the file in which
	 * the error has occured.
	 * 
	 * @param error
	 *            the error that will be printed
	 * @param line_num
	 *            the line number
	 */
	public void printErrorAtLineNumber(String error, int line_num) {
		m_out.println("LINE " + line_num
				+ ": ERROR WHILE PARSING FOR LIMITS.\n\t" + error);
	}

	/**
	 * @inheritDoc
	 */
	public void printError(String message, int start, int stop) {
		printError(message);
	}

	/**
	 * @inheritDoc
	 */
	public void printWarning(String message, int start, int stop) {
		printWarning(message);
	}

	/**
	 * The function that will print the total number of errors that occured in
	 * the file.
	 */
	public void printErrorTotals() {
	}

	/**
	 * The function that prints a general error to the output file.
	 * 
	 * @param error
	 *            the error message
	 */
	public void printGeneralError(String error) {
		m_out.println("ERROR WHILE PARSING FOR LIMITS.\n\t" + error);
	}

	/**
	 * The function that print a warning message to the output file.
	 * 
	 * @param warning
	 *            the warning message to be printed
	 */
	public void printWarning(String warning) {
		m_out.println("LINE " + m_line_number
				+ "WARNING WHILE PARSING FOR LIMITS.\n\t" + warning);
	}

	/**
	 * The function that prints a warning message and the line number that is
	 * attributed to this warning.
	 * 
	 * @param warning
	 *            the warning message to be printed
	 * @param line_num
	 *            the line number that this error occured at
	 */
	public void printWarningAtLineNumber(String warning, int line_num) {
		m_out.println("LINE " + line_num
				+ ": ERROR WHILE PARSING FOR LIMITS.\n\t" + warning);
	}

	/**
	 * Function that allows you to set a different output to which warnings and
	 * errors will be written to. By default, System.out (or the standard output )
	 * will be used.
	 * 
	 * @param output
	 *            the java.io.PrintWriter stream that you want errors to be
	 *            written to.
	 */
	public void setOutput(PrintWriter output) {
		m_out = output; // Setting the output to a new stream
	}

	/**
	 * The following was used to conduct the unit test on this class. public
	 * static void main( String argv[] ) { try { LimitationsParser p = new
	 * LimitationsParser(); p.parseFile("TestParse", "TESTSET"); } catch
	 * (IOException iox) { System.out.println("DID NOT PARSE DUE TO
	 * "+iox.getMessage()); iox.printStackTrace(); } System.exit(0); }
	 */

	/***************************************************************************
	 * PRIVATE FUNCTIONS *
	 **************************************************************************/
	/**
	 * Will find the block with the name that was passed in.
	 * 
	 * @param block_name
	 *            name of the block
	 * @return true if foundthe block #BEGIN_block_name in the text
	 * @throws IOException
	 *             comes from RandomAccessFile
	 */
	private boolean findBlock(String block_name) throws IOException {
		String block_header = "#BEGIN_" + block_name;
		String line;
		while (notAtEOF()) {
			line = readNextLine();
			if (line.equalsIgnoreCase(block_header))
				return true; // FOUND HEADER
		}
		return false; // HEADER NOT FOUND
	}

	/**
	 * Will check if the line read is the one that is supposed to end the
	 * symentic block that was started by #BEGIN_block_name.
	 * 
	 * @param line
	 *            that was read in from the input file
	 * @param block_name
	 *            name of the block that was being parsed for
	 * @return true if the line read equalsn the #END_block_name
	 */
	private boolean isBlockEnd(String line, String block_name) {
		return (line.equalsIgnoreCase("#END_" + block_name));
	}

	/**
	 * Checks to see if m_reader has reached the EOF marked yet.
	 * 
	 * @return true if there are more items left in the parser
	 */
	private boolean notAtEOF() {
		return (m_current_position < m_file_length);
	}

	/**
	 * Will try to open the file passed to it and generate any errors if there
	 * are problems with that action.
	 * 
	 * @param file_name
	 *            the name of the file to open
	 * @return true if could open the file otherwise false
	 * @throws IOException
	 *             comes from the RandomAccessFile
	 */
	private boolean openFile(String file_name) throws IOException {
		File new_file = new File(file_name);
		if (new_file.exists() && new_file.isFile()) {
			m_reader = new RandomAccessFile(new_file, "r"); // open file for
															// reading
			m_file_length = m_reader.length(); // getting length of file
			m_current_position = m_reader.getFilePointer(); // getting pos of
															// pointer
			m_line_number = 1;
			return true;
		} else {
			printGeneralError("File : " + file_name
					+ " not found. No limitations read!");
			return false;
		}
	}

	/**
	 * Will parse the opened file for the limitations conditions and create a
	 * data structure that will hold this type of information.
	 * 
	 * @param block_name
	 *            the name of the block that contains the limitations
	 * @param d_vector
	 *            the Descriptor Vector
	 * @throws IOException
	 *             thrown from the RandomAccessFile class
	 */
	private void parseFileFor(String block_name, DescriptorList d_vector)
			throws IOException {
		// First we want to find the block that contains the our block
		if (findBlock(block_name)) {
			// Next we read the descriptors until we hit the end.
			String new_line = readNextLine();
			while (!isBlockEnd(new_line, block_name) && notAtEOF())
				if (new_line.startsWith("OBJECT")
						|| new_line.startsWith("CONTENT")) {
					StringTokenizer tokenizer = new StringTokenizer(new_line);
					int tokens = tokenizer.countTokens();
					if (tokens < 2) {
						printError("DESCRIPTOR type should have more than 2 words.");
						new_line = readNextLine();
					} else {
						String category = tokenizer.nextToken();
						String name = tokenizer.nextToken();
						Iterator relevantIter = d_vector.getNodesByType(
								category, name);
						if (!relevantIter.hasNext()) {
							printError("Not a descriptor type: " + category
									+ " " + name + " SKIPPED!");
							new_line = readNextLine();
						} else {
							Descriptor relevant = (Descriptor) relevantIter
									.next();
							if (tokens > 2) {
								int colon_index = new_line.indexOf(":");
								if (colon_index < 0) {
									printWarningAtLineNumber(
											"DESCRIPTOR type has more than 2 words"
													+ " and no ':'. The last will be ignored.",
											(int) m_line_number);
								} else {
									try {
										Filterable.Rule parsed_rule = parseRule(
												new_line.substring(
														colon_index + 1,
														new_line.length())
														.trim(),
												new FrameSpan(), this);
										if (parsed_rule != null)
											m_rule_holder.addRule(category
													+ name, " framespan",
													parsed_rule);
									} catch (BadDataException bdx) {
										printError(bdx.getMessage());
									}
								}
							}
							new_line = readNextLine();
							while ((new_line.indexOf(":") > 0) && notAtEOF()) {
								int colon_index = new_line.indexOf(":");
								String attribute = new_line.substring(0,
										colon_index).trim();
								String rule = new_line.substring(
										colon_index + 1, new_line.length())
										.trim();
								try {
									Filterable.Rule parsed_rule = parseRule(
											rule, relevant
													.getFilterable(attribute),
											this);
									if (parsed_rule != null)
										m_rule_holder.addRule(category + name,
												attribute, parsed_rule);
								} catch (BadDataException bdx) {
									printError(bdx.getMessage());
								}
								new_line = readNextLine();
							}
						}
					}
				} else {
					if (new_line.length() > 0) {
						StringTokenizer st = new StringTokenizer(new_line);
						if (st.hasMoreTokens()
								&& !st.nextToken().startsWith("//")) {
							printError("Malformed line: " + new_line);
						}
					}
					new_line = readNextLine();
				}
		}
	}

	static private Filterable.Rule parseRule(String unparsed_rule,
			Filterable attribute, ErrorWriter err) throws BadDataException {
		if (attribute != null) {
			try {
				Filterable.Rule t = Rules.getComplexRule(attribute,
						unparsed_rule, err);
				return t;
			} catch (IllegalArgumentException iax) {
				err.printError(iax.getMessage());
				return null;
			}
		} else { //if attribute wasn't found, return null
			err.printError("No Attribute found to match this rule.");
			return null;
		}
	}

	/**
	 * Will read the next line in the input, trim it (get rid of any excess
	 * white space characters from the beginning and end of the line) and get
	 * rid of any comments on that line (i.e. anything starting with //).
	 * 
	 * @return line read
	 * @throws IOException
	 */
	private String readNextLine() throws IOException {
		String line = m_reader.readLine();
		m_line_number++;
		m_current_position = m_reader.getFilePointer();
		return removeComments(line.trim());
	}

	/**
	 * Will remove comments from a String and return anon commented string
	 * 
	 * @param candidate
	 *            to decomment.
	 * @return the uncommented string
	 */
	private String removeComments(String candidate) {
		if (candidate == null)
			return null;
		int ci = candidate.indexOf("//");
		if (ci != -1)
			return candidate.substring(0, ci);
		return candidate;
	}
}