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

package edu.umd.cfar.lamp.viper.util.reader;

import java.io.*;
import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class offers a "Preprocessor" to ViPER data files, removing comments
 * and expanding #includes. It also contains some knowledge of the gtf data
 * format, so it should be checked for accuracy if the descriptor format is
 * changed greatly.
 * 
 * @since Version 1.1. Before, this was called GTReader.
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @author Charles Lin
 */
public class VReader implements ErrorWriter {
	/**
	 * This class is used to represent a line of text or a sequence of lines
	 * connected by '\' characters. It supports various operations to treat
	 * lines and multilines in a consistent manner.
	 */
	protected class TextLine {
		Vector lines = new Vector();
		String current;
		int lineNumber = 0;
		boolean multi = false;
		boolean done = true;

		/**
		 * This appends a string to the beginning of the current line. Useful
		 * for commenting things out, and in general prefixing lines.
		 * 
		 * @param prefix
		 *            the string to insert onto the current line.
		 */
		public void insert(String prefix) {
			current = prefix + current;
			if ((lines != null) && (lines.size() > 0))
				lines.set(0, prefix + ((String) lines.get(0)));
		}

		/**
		 * This returns the current multiline with the line number appended to
		 * the front. If it is more than two lines, it will put ". . ." between
		 * the first and last lines. The format is " 1234567. " + line.
		 * 
		 * @return The line with the line number before it, and split, like
		 *         jikes.
		 */
		public String numbered() {
			if (multi && (lines.size() > 1)) {
				if (lines.size() == 2) {
					return (
						StringHelp.padLeft(10, (lineNumber - 1) + ". ")
							+ lines.get(0)
							+ "\\\n"
							+ StringHelp.padLeft(10, lineNumber + ". ")
							+ lines.get(1));
				} else {
					return (
						StringHelp.padLeft(
							10,
							(lineNumber - lines.size() + 1) + ". ")
							+ lines.get(0)
							+ "\\\n              . . .\n"
							+ StringHelp.padLeft(10, lineNumber + ". ")
							+ lines.get(lines.size() - 1));
				}
			} else {
				return (StringHelp.padLeft(10, lineNumber + ". ") + current);
			}
		}

		/**
		 * This prints out only the lines containing character offsets between
		 * <code>start</code> and <code>end</code>, inclusive, using the
		 * same format as {@link #numbered()}.
		 * 
		 * @param start
		 *            The character offset of the beginning of the error.
		 * @param end
		 *            The character offset of the end of the error.
		 * @return The current line marked up for locating the error, like
		 *         jikes
		 */
		public String numbered(int start, int end) {
			if (multi) {
				int startLine = getLineNum(start);
				int endLine = getLineNum(end);
				if ((endLine - startLine) > 1) {
					StringBuffer buff = new StringBuffer();
					buff.append(
						StringHelp.underliner(
							true,
							false,
							getColumn(start),
							((String) lines.get(startLine - lineNumber))
								.length()));
					buff
						.append("\n")
						.append(StringHelp.padLeft(10, startLine + ". "))
						.append(lines.get(lineNumber - startLine))
						.append("\\\n");
					if ((endLine - startLine) > 2) {
						buff.append("              . . .\n");
					}
					buff
						.append(StringHelp.padLeft(10, endLine + ". "))
						.append(lines.get(lineNumber - endLine))
						.append("\n");
					buff.append(
						StringHelp.underliner(false, true, 0, getColumn(end)));
					return buff.toString();
				} else {
					return (
						StringHelp.padLeft(10, endLine + ". ")
							+ lines.get(lineNumber - endLine)
							+ "\n          "
							+ StringHelp.underliner(
								true,
								true,
								getColumn(start),
								getColumn(end)));
				}
			} else {
				return (
					numbered()
						+ "\n          "
						+ StringHelp.underliner(true, true, start, end));
			}
		}

		/**
		 * Returns <code>true</code> if the current line contains no data.
		 * 
		 * @return <code>true</code> if the current line is empty.
		 */
		public boolean empty() {
			return (current == null) || (current.length() <= 0);
		}

		/**
		 * Set the current line number. The first line is always 1, and counts
		 * up whenever a new line is added. Use this when starting deeper into
		 * a file, when going back, etc.
		 * 
		 * @param lineNumber
		 *            The new line number.
		 */
		public void setLineNum(int lineNumber) {
			this.lineNumber = lineNumber;
		}

		/**
		 * This returns what column in a multiline the specified character
		 * would be found. Mutliline returns a sequence of connected lines as a
		 * single string, so this is necessary to know what actual column in
		 * the file a specific string started.
		 * @param offset the character
		 * @return the column of the offset into the multiline section
		 */
		public int getColumn(int offset) {
			if (multi) {
				int i = 0;
				try {
					while (offset > ((String) lines.get(i)).length()) {
						offset -= ((String) lines.get(i)).length();
						i++;
					}
				} catch (IndexOutOfBoundsException ioobx) {
					throw new StringIndexOutOfBoundsException("Column specified beyond end of line.");
				}
			}
			return offset;
		}

		/**
		 * Returns the line number of this line or the last line if this is a
		 * multiline.
		 * @return the last line of the multiline
		 */
		public int getLineNum() {
			return lineNumber;
		}

		/**
		 * This finds the line number of a specific character. This is useful
		 * for printing out errors and warnings.
		 * @param start the line number of the character in the multiline segment
		 * @return the line number
		 */
		public int getLineNum(int start) {
			if (multi) {
				int i = 0;
				try {
					while (start > ((String) lines.get(i)).length()) {
						start -= ((String) lines.get(i)).length();
						i++;
					}
				} catch (IndexOutOfBoundsException ioobx) {
					throw new StringIndexOutOfBoundsException("Column specified beyond end of line.");
				}
				return (lineNumber + i - lines.size());
			} else {
				return lineNumber;
			}
		}

		/**
		 * Returns the String representation of the current line or mutliline
		 * as one whole line.
		 * @return 
		 */
		public String toString() {
			if (!done) {
				StringBuffer retVal = new StringBuffer(lines.get(0).toString());
				for (int i = 1; i < lines.size(); i++)
					retVal.append(lines.get(i));
				return retVal.toString();
			} else {
				return current;
			}
		}

		/**
		 * This clears the current multiline. (empty will return true)
		 */
		public void clear() {
			lines.clear();
			current = null;
			multi = false;
			done = true;
		}

		/**
		 * Returns true if the current line does not end in a continuation.
		 * @return <code>false</code> if the current line continues
		 */
		public boolean done() {
			return done;
		}

		/**
		 * Adds the next line to the current multiline, replacing it if the
		 * last one was done, appending to it if it wasn't, and returning true
		 * if it is now done.
		 * @param nextLine puts the next line into the multiline
		 * @return <code>isDone()</code>
		 */
		public boolean add(String nextLine) {
			lineNumber++;
			if ((nextLine == null) || (nextLine.length() < 1))
				return false;

			int slashIndex = nextLine.indexOf('\\');
			while ((slashIndex != -1)
				&& (nextLine.length() > slashIndex + 1)) {
				if (Character.isWhitespace(nextLine.charAt(slashIndex + 1)))
					break;
				slashIndex = nextLine.indexOf('\\', slashIndex + 1);
			}

			if (done) {
				if (slashIndex >= 0) {
					multi = true;
					lines.add(nextLine.substring(0, slashIndex));
					done = false;
				} else {
					multi = false;
					current = nextLine;
				}
			} else {
				if (slashIndex < 0) {
					lines.add(nextLine);
					current = toString();
					done = true;
				} else {
					lines.add(nextLine.substring(0, slashIndex));
				}
			}
			return done;
		}

		/**
		 * Equivalent to toString.startsWith, but faster on lines that are not
		 * finished.
		 * @param prefix the prefix to check for
		 * @return <code>toString().startsWith(prefix)</code>
		 */
		public boolean startsWith(String prefix) {
			if (empty()) {
				return false;
			} else if (done) {
				return current.startsWith(prefix);
			} else {
				int i = 0;
				do {
					if (prefix.length() > ((String) lines.get(i)).length()) {
						if (!((String) lines.get(i))
							.equals(
								prefix.substring(
									0,
									((String) lines.get(i)).length()))) {
							return false;
						} else {
							prefix =
								prefix.substring(
									((String) lines.get(i)).length());
							i++;
						}
					} else {
						return ((String) lines.get(i)).startsWith(prefix);
					}
				} while ((prefix.length() > 0) && (i < lines.size()));
				return (prefix.length() == 0);
			}
		}
	}

	String dir = null;
	String file = null;

	Stack includePaths;
	Stack includeHandles;
	Stack lineNumbers;

	TextLine currentLine = new TextLine();

	String searchType = null;
	boolean filtering = false;

	Stack lineTypeStack = null;

	private boolean echo_all = false;
	private boolean echo_err = true;
	private boolean echo_warn = true;
	private boolean echo_bad = true;
	private boolean echo_totals = false;

	int total_err = 0;
	int total_warn = 0;
	int total_generr = 0;

	int errorTab = 14;

	PrintWriter out;

	/**
	 * Initializes a new VReader with the file specified.
	 * @param pathName the file to open
	 * @throws IOException if there is an error while opening/reading the file
	 */
	public VReader(String pathName) throws IOException {
		lineTypeStack = new Stack();
		pathName = pathName.trim();

		int lastSlash = pathName.lastIndexOf(File.separatorChar);

		if (lastSlash != -1) {
			dir = pathName.substring(0, lastSlash);
			file = pathName.substring(lastSlash + 1);
		} else {
			dir = null;
			file = pathName;
		}

		String path = null;
		if (dir != null) {
			path = dir + File.separatorChar + file;
		} else {
			path = file;
		}

		out = new PrintWriter(System.err, true);

		// Open file so user doesn't have to
		RandomAccessFile fileHandle = new RandomAccessFile(new File(path), "r");
		includePaths = new Stack();
		includeHandles = new Stack();
		lineNumbers = new Stack();

		includePaths.push(path);
		includeHandles.push(fileHandle);
	}

	/**
	 * Initializes a new VReader with the file specified by directory and file
	 * name.
	 * @param directory the directory name
	 * @param fileName the file name
	 * @throws IOException
	 */
	public VReader(String directory, String fileName) throws IOException {
		lineTypeStack = new Stack();
		dir = directory;
		if (dir.charAt(dir.length() - 1) == File.separatorChar) {
			dir = dir.substring(0, directory.length() - 1);
		}
		file = fileName;

		out = new PrintWriter(System.err, true);

		String path = dir + File.separatorChar + file;

		// Open file so user doesn't have to
		RandomAccessFile fileHandle = new RandomAccessFile(new File(path), "r");
		includePaths.push(path);
		includeHandles.push(fileHandle);
	}

	/**
	 * Creates a new VReader that is block-type agnostic.
	 * @param paths  List of files to search in order (as if they were a single file)
	 */
	public VReader(List paths) {
		this(paths, null);
	}

	/**
	 * Opens a new VReader looking for the specified block type.
	 * @param paths List of files to search in order (as if they were a single file)
	 * @param returnAs block type to pay attention to, e.g. EVALUATION
	 */
	public VReader(List paths, String returnAs) {
		searchType = returnAs;
		includeHandles = new Stack();
		includePaths = new Stack();
		lineNumbers = new Stack();
		lineTypeStack = new Stack();

		out = new PrintWriter(System.err, true);

		for (int i = 0; i < paths.size(); i++) {
			try {
				// Open file so user doesn't have to
				RandomAccessFile fileHandle =
					new RandomAccessFile(new File((String) paths.get(i)), "r");
				includeHandles.push(fileHandle);
				lineNumbers.push(new Integer(0));
			} catch (IOException iox) {
				printGeneralError("Unable to open " + paths.get(i));
				paths.remove(i--);
			}
		}
		lineNumbers.pop();
		for (int i = 0; i < paths.size(); i++)
			includePaths.push(paths.get(i));
	}

	/**
	 * Sets the output stream.
	 * @param pw the new output stream
	 */
	public void setOutput(PrintWriter pw) {
		out = pw;
	}

	/**
	 * Sets the output stream.
	 * @param ps the new output stream
	 */
	public void setOutput(PrintStream ps) {
		out = new PrintWriter(ps, true);
	}

	/**
	 * closes the input file
	 * @throws IOException
	 */
	public void close() throws IOException {
		while (!includeHandles.empty())
			 ((RandomAccessFile) includeHandles.pop()).close();
	}

	protected void finalize() throws IOException {
		while (!includeHandles.empty())
			 ((RandomAccessFile) includeHandles.pop()).close();
	}

	/**
	 * Change the error switches.
	 * @param all report all lines as they are parsed
	 * @param err report errors
	 * @param warn report warnings
	 * @param bad report the bad stuff
	 * @param totals print warning and error totals at the end
	 */
	public void changeSwitches(
		boolean all,
		boolean err,
		boolean warn,
		boolean bad,
		boolean totals) {
		echo_all = all;
		echo_err = err;
		echo_warn = warn;
		echo_bad = bad;
		echo_totals = totals;
	}

	/**
	 * Checks to see if any file handles remain
	 * @return true if more files remain
	 */
	public boolean fileHandleSet() {
		return !includeHandles.empty();
	}

	/**
	 * Reset the count of warning and errors.
	 */
	public void resetTotals() {
		total_err = total_warn = total_generr = 0;
	}

	/**
	 * Gets the current line number of the cursor.
	 * @return the cursor's line number
	 */
	public int getCurrentLineNumber() {
		return (currentLine.getLineNum());
	}

	/**
	 * Get the current line as a String. It may
	 * contain new line characters, thanks to those slashes
	 * at the end of lines.
	 * @return the current line
	 */
	public String getCurrentLine() {
		return (currentLine.toString());
	}

	/**
	 * like gotoNextRealLine, but also returns the line.
	 * 
	 * @return the current line, as a string.
	 * @throws IOException
	 * @throws EOFException
	 */
	public String advanceToNextRealLine() throws IOException, EOFException {
		gotoNextRealLine();
		return getCurrentLine();
	}

	/**
	 * Advances the cursor to the next line.
	 * @throws IOException
	 * @throws EOFException
	 */
	public void gotoNextRealLine() throws IOException, EOFException {
		do {
			gotoNextLine();
		} while (!currentLineReal());
	}

	/**
	 * Advance to the first occurance of the begin directive for section <code>name</code>.
	 * Does nothing if the current line is the requested begin directive.
	 * Therefore, to go to the third #BEGIN_ALPHA in a file, make sure to call
	 * gotoNextLine() after each call to this method.
	 * 
	 * @param name
	 *            Case-sensitive begin directive name - usually all-caps.
	 * @return <code>true</code> if the directive was found. Otherwise, the
	 *         reader has advanced to the end of the file.
	 */
	public boolean advanceToBeginDirective(String name) {
		String nextBegin = advanceToBeginDirective();
		while (nextBegin != null && !nextBegin.equals(name)) {
			try {
				gotoNextLine();
			} catch (IOException ex) {
				return false;
			}
			nextBegin = advanceToBeginDirective();
		}
		return nextBegin != null;
	}

	/**
	 * The block type to look for.
	 * @param name the block to look for.
	 */
	public void payAttentionTo(String name) {
		searchType = name;
	}

	/**
	 * Advances to next begin directive; noop if current line is a begin
	 * directive.
	 * 
	 * @return The section name that this begin directive marks, eg DATA or
	 *         CONFIG If there is an IOException, this returns <code>null</code>
	 */
	public String advanceToBeginDirective() {
		try {
			while (!isBeginDirective(currentLine)) {
				gotoNextLine();
			}
		} catch (IOException iox) {
			return null;
		}
		return currentLine.toString().substring("#BEGIN_".length());
	}

	/**
	 * Checks to see if the current line contains a 
	 * <code>#BEGIN_</code> directive
	 * @return if the line is a begin directive
	 */
	public boolean currentLineIsBeginDirective() {
		return (isBeginDirective(currentLine));
	}

	/**
	 * Checks to see if the specified line contains a 
	 * <code>#BEGIN_</code> directive
	 * @param line the line to check
	 * @return if the line is a begin directive
	 */
	public static boolean isBeginDirective(TextLine line) {
		return (line.startsWith("#BEGIN_"));
	}

	/**
	 * Checks to see if the current line contains an 
	 * <code>#END_</code> directive
	 * @return if the line is an end directive
	 */
	public boolean currentLineIsEndDirective() {
		return (isEndDirective(currentLine));
	}

	/**
	 * Checks to see if the specified line contains an 
	 * <code>#END_</code> directive
	 * @param line the line to check
	 * @return if the line is an end directive
	 */
	public static boolean isEndDirective(TextLine line) {
		if (line == null)
			return (true);

		return (line.startsWith("#END_"));
	}

	String[] extractBeginType(String line) {
		int beginIndex = line.indexOf("#BEGIN_");
		String beginType = line.substring(beginIndex + 7);
		beginType = beginType.trim();

		String[] result = new String[1];
		result[0] = beginType;

		return (result);
	}

	String[] extractEndType(String line) {
		int endIndex = line.indexOf("#END_");
		String endType = line.substring(endIndex + 7);
		endType = endType.trim();

		String[] result = new String[1];
		result[0] = endType;

		return (result);
	}

	String gotoNextLine() throws IOException, EOFException {
		String s = null;
		boolean skip;
		do {
			skip = false;
			if (includeHandles.empty()) {
				throw (new EOFException("EOF reached"));
			}

			try {
				String temp;
				currentLine.clear();
				do {
					temp =
						VReader.removeComments(
							((RandomAccessFile) includeHandles.peek())
								.readLine());
					if (temp == null) {
						break;
					}
				} while (!currentLine.add(temp));
			} catch (EOFException eofx) {
				out.println("!!!!!!\n!EOFX!\n!!!!!?");
				((RandomAccessFile) includeHandles.peek()).close();
				if (lineNumbers.empty()) {
					includePaths.pop();
					includeHandles.pop();
					if (searchType != null)
						return ("#END_" + searchType);
					else
						throw new EOFException("Cannot get the next line.");
				} else {
					currentLine.setLineNum(
						((Integer) lineNumbers.pop()).intValue());
					includePaths.pop();
					includeHandles.pop();
					skip = true;
					continue;
				}
			}

			if (!currentLine.empty()) {
				s = currentLine.toString().trim();
				if (s.startsWith("#")) {
					try {
						processPound(s);
					} catch (IOException iox) {
						printError("Pound (#) IO  Problem");
					}
				}
			} else {
				((RandomAccessFile) includeHandles.peek()).close();
				if (lineNumbers.empty()) {
					if (searchType != null) {
						lineTypeStack = new Stack();
						currentLine.add("#END_" + searchType);
						if (echo_all) {
							out.println(currentLine.numbered());
						}
						return currentLine.toString();
					} else {
						throw (new EOFException("Can't get the next line."));
					}
				} else {
					currentLine.setLineNum(
						((Integer) lineNumbers.pop()).intValue());
					includePaths.pop();
					includeHandles.pop();
					skip = true;
					continue;
				}
			}

			if (searchType != null) {
				if ((!lineTypeStack.empty())
					&& (((String) lineTypeStack.peek()).equals(searchType))) {
					if ((filtering) && (s.startsWith("#")))
						return gotoNextLine();
					else {
						filtering = true;
						s = "#BEGIN_" + searchType;
					}
				} else {
					s = null;
					skip = true;
				}
			}
		}
		while (skip);
		if (echo_all) {
			out.println(currentLine.numbered());
		}
		return s;
	}

	boolean processPound(String line) throws IOException {
		line = removeComments(line);
		line = line.trim();
		if (line.toUpperCase().startsWith("#BEGIN_")) {
			if (lineTypeStack.empty())
				lineTypeStack.push(
					line.substring("#BEGIN_".length()).toUpperCase());
			else if (
				!((String) lineTypeStack.peek()).equalsIgnoreCase(
					line.substring(7))) {
				printWarning(
					"Beginning "
						+ line.substring("#BEGIN_".length()).toUpperCase()
						+ " data inside "
						+ ((String) lineTypeStack.peek())
						+ " data.");
				if (searchType == null) {
					try {
						advanceToEndOfMisplacedData(
							line.substring("#BEGIN_".length()));
					} catch (EOFException eofx) {
						throw (new EOFException("Cannot get the next line."));
					}
				} else
					lineTypeStack.push(
						line.substring("#BEGIN_".length()).toUpperCase());
			} else {
				lineTypeStack.push(
					line.substring("#BEGIN_".length()).toUpperCase());
				currentLine.insert("//");
			}
		} else if (line.toUpperCase().startsWith("#END_")) {
			if (lineTypeStack.empty()) {
				printWarning(
					"Attempting to terminate "
						+ line.substring("#END_".length()).toUpperCase()
						+ " when not inside data.");
			} else if (
				!((String) lineTypeStack.peek()).equalsIgnoreCase(
					line.substring(5))) {
				printError(
					"Attempting to terminate "
						+ line.substring("#END_".length()).toUpperCase()
						+ " data inside "
						+ ((String) lineTypeStack.peek())
						+ " data.");
			} else {
				lineTypeStack.pop();
				if ((searchType != null) || (!lineTypeStack.empty()))
					currentLine.insert("//");
			}
		} else if (line.toUpperCase().startsWith("#INCLUDE ")) {
			RandomAccessFile fileHandle;
			line =
				currentLine.toString().substring("#INCLUDE ".length()).trim();
			if (line.startsWith("\"")) {
				line = line.substring(1, line.lastIndexOf("\""));
				// FIXME debackslashify quotes only
			}
			int newIndex = line.indexOf(File.separatorChar);
			if (newIndex != 0) {
				String oldDirectory = (String) includePaths.peek();
				int oldIndex = oldDirectory.lastIndexOf(File.separatorChar);
				if (oldIndex > 0) {
					oldDirectory = oldDirectory.substring(0, oldIndex);
					//chop off filename
					while (line.startsWith("..") && oldDirectory != null) {
						oldIndex = oldDirectory.lastIndexOf(File.separatorChar);
						if (oldIndex > 0) {
							oldDirectory = oldDirectory.substring(0, oldIndex);
						} else {
							oldDirectory = null;
						}
						line = line.substring(3);
					}
					if (oldDirectory != null) {
						line = oldDirectory + File.separatorChar + line;
					}
				}
			}
			File includeFile = new File(line);
			if (!includeFile.canRead()) {
				String errS = "Included file \"" + line + "\" cannot be read.";
				printError(errS);
				return false;
			}
			try {
				fileHandle = new RandomAccessFile(includeFile, "r");
			} catch (IOException iox) {
				String errS =
					"Included file \"" + line + "\" unable to be opened.";
				printError(
					errS,
					"#INCLUDE ".length(),
					currentLine.toString().length() - 1);
				throw (new IOException(errS));
			}
			includePaths.push(line);
			includeHandles.push(fileHandle);
			lineNumbers.push(new Integer(currentLine.getLineNum()));
			currentLine.setLineNum(0);
			currentLine.insert("//");
		} else {
			printError("Unknown #.");
		}
		return (true);
	}

	boolean advanceToEndOfMisplacedData(String dataType)
		throws EOFException, IOException {
		String line = null;
		int total = 1;
		boolean retry;
		int lineNum = currentLine.getLineNum();
		while (total > 0) {
			do {
				try {
					retry = false;
					line =
						((RandomAccessFile) includeHandles.peek()).readLine();
					lineNum++;
				} catch (EOFException eofx) {
					if (includePaths.empty())
						throw (new EOFException("Cannot get the next line."));
					else {
						lineNum = ((Integer) lineNumbers.pop()).intValue();
						includePaths.pop();
						includeHandles.pop();
						retry = true;
					}
				}
			} while (retry);
			if (line == null)
				return (false);
			if (line.equalsIgnoreCase("#BEGIN_" + dataType))
				total++;
			else if (line.equalsIgnoreCase("#END_" + dataType))
				total--;
		}
		currentLine.setLineNum(lineNum);
		return (true);
	}

	/**
	 * Is current line "real"? That is, does it contain information?
	 * @return true, if the current line is not empty.
	 */
	boolean currentLineReal() {
		if ((currentLine == null) || currentLine.empty())
			return (false);

		String line = new String(currentLine.toString());

		line = line.trim();
		if (line.length() == 0)
			return (false);
		else
			return (true);
	}

	/**
	 * Tests to see if the cursor is on a descriptor line
	 * @return true if the line looks like the start of a descriptor
	 */
	public boolean currentLineIsDescriptor() {
		return (VReader.isDescriptorLine(currentLine));
	}

	/**
	 * Tests to see if the line starts with a 
	 * descriptor type keyword, such as CONTENT
	 * or OBJECT.
	 * @param line the line to check
	 * @return <code>true</code> if the line starts
	 *  with a descriptor keyword
	 */
	public static boolean isDescriptorLine(TextLine line) {
		String temp = line.toString().trim();

		int spaceIndex = temp.indexOf(' ');
		String descType = "";
		if (spaceIndex != -1) {
			descType = temp.substring(0, spaceIndex);
		} else {
			descType = temp;
		}

		return (isKeyword(descType));
	}

	/**
	 * Removes C++ style comments from a string. Assumes the String is only one
	 * line long. This is the same as <code>s.substring (0, s.indexOf ("//"))</code>.
	 * @param s the line of code that might contain a C++ comment
	 * @return the line without the comment
	 */
	static String removeComments(String s) {
		if (s == null)
			return (null);

		int ci = s.indexOf("//");
		if (ci != -1) {
			return (s.substring(0, ci));
		}
		return (s);
	}

	/**
	 * Checks to see if the line at which the VReader is pointing contains
	 * well-formed Attribute data.
	 * 
	 * @return <code>true</code> if the current line looks like Attribute
	 *         data.
	 */
	public boolean currentLineIsAttribute() {
		return (isAttributeLine(currentLine.toString(), true));
	}

	/**
	 * Checks to see if the line at which the VReader is pointing contains
	 * well-formed Attribute data.
	 * 
	 * @param val
	 *            if you want to print errors if it is not an Attribute line.
	 * @return <code>true</code> if the current line looks like Attribute
	 *         data.
	 */
	public boolean currentLineIsAttribute(boolean val) {
		return (isAttributeLine(currentLine.toString(), val));
	}

	/**
	 * Checks attribute by looking for colon, and making sure attribute name is
	 * not a descriptor type.
	 * 
	 * @param line
	 *            The line to check
	 * @param printErr
	 *            Set if you want to print errors if it is not an Attribute
	 *            line.
	 * @return <code>true</code> if it looks like an attribute.
	 */
	public boolean isAttributeLine(String line, boolean printErr) {
		String newLine = new String(line);
		newLine = newLine.trim();

		int colonIndex = newLine.indexOf(':');

		if (colonIndex == -1) {
			String[] elements = StringHelp.splitSpaces(newLine);

			if (!StringHelp.isLegalIdentifier(elements[0])) {
				if (printErr)
					printError(
						"Attribute name, '"
							+ elements[0]
							+ "', must be alphanumeric",
						line.indexOf(elements[0]),
						line.indexOf(elements[0]) + elements[0].length());
			} else {
				if (printErr) {
					printError(
						"Expecting ':' after attribute name, "
							+ "'"
							+ elements[0]
							+ "'",
						line.indexOf(elements[0]),
						line.indexOf(elements[0]) + elements[0].length());
				}
			}
			return (false);
		}

		String attributeName = newLine.substring(0, colonIndex);
		attributeName = attributeName.trim();

		if (!isKeyword(attributeName))
			return (true);
		else
			return (false);
	}

	/**
	 * Returns true if this is one of the Descriptor keywords. (FILE, OBJECT,
	 * etc.) N.B. This is case sensitive.
	 * 
	 * @param name
	 *            A string to check against the list of keywords.
	 * @return <code>true</code> if name is a Descriptor keyword (category).
	 */
	public static boolean isKeyword(String name) {
		return (
			name.equals("FILE")
				|| name.equals("CONTENT")
				|| name.equals("OBJECT")
				|| name.equals("RELATION"));
	}

	/**
	 * This adds the file name to the line number. Like javac!
	 * 
	 * @param i
	 *            the current line number.
	 * @return A String of the form <code><i>FileName</i>:<i>Line</i></code>
	 */
	private String enhanceALineNumber(int i) {
		if (includePaths.empty())
			return (new Integer(i).toString());
		return (includePaths.peek() + ":" + i);
	}

	/**
	 * This adds the file name to the given line number / column number pair.
	 * 
	 * @param i the current line number
	 * @param columnNumber the column of the start of the error or warning
	 * @return A String of the form <code><i>FileName</i>:<i>Line</i>:<i>Column</i></code>
	 */
	private String enhanceACharacterNumber(int i, int columnNumber) {
		if (includePaths.empty())
			return (null);
		return (includePaths.peek() + ":" + i + ":" + columnNumber);
	}

	/**
	 * @see ErrorWriter#printError(String)
	 */
	public void printError(String message) {
		total_err++;
		if (!echo_err)
			return;

		String lineNumber = enhanceALineNumber(currentLine.getLineNum()) + ":";
		int lineDiff = errorTab - lineNumber.length();
		for (int i = 0; i < lineDiff; i++)
			lineNumber += " ";
		if (echo_bad && !echo_all) {
			out.println("");
			out.println(currentLine.numbered());
		}
		out.println(lineNumber + "  Error: " + message);
	}

	/**
	 * @see ErrorWriter#printErrorAtLineNumber(String, int)
	 */
	public void printErrorAtLineNumber(String message, int lineNum) {
		total_err++;
		if (!echo_err)
			return;
		String lineNumber = enhanceALineNumber(lineNum) + ":";

		int lineDiff = errorTab - lineNumber.length();
		for (int i = 0; i < lineDiff; i++) {
			lineNumber += " ";
		}
		out.println(lineNumber + "  Error: " + message);
	}

	/**
	 * @see ErrorWriter#printWarning(String)
	 */
	public void printWarning(String message) {
		total_warn++;
		if (!echo_warn)
			return;
		String lineNumber = enhanceALineNumber(currentLine.getLineNum()) + ":";

		int lineDiff = errorTab - lineNumber.length();
		for (int i = 0; i < lineDiff; i++) {
			lineNumber += " ";
		}
		if (echo_bad && !echo_all) {
			out.println("");
			out.println(currentLine.numbered());
		}
		out.println(lineNumber + "Warning: " + message);
	}

	/**
	 * @see ErrorWriter#printWarningAtLineNumber(String, int)
	 */
	public void printWarningAtLineNumber(String message, int lineNum) {
		total_warn++;
		if (!echo_warn)
			return;

		String lineNumber = enhanceALineNumber(lineNum) + ":";

		int lineDiff = errorTab - lineNumber.length();
		for (int i = 0; i < lineDiff; i++) {
			lineNumber += " ";
		}
		out.println(lineNumber + "Warning: " + message);
	}

	/**
	 * @see ErrorWriter#printError(String, int, int)
	 */
	public void printError(String message, int start, int stop) {
		total_err++;
		if (!echo_err)
			return;

		String lineNumber =
			enhanceACharacterNumber(
				currentLine.getLineNum(start),
				currentLine.getColumn(start))
				+ ":";
		int lineDiff = errorTab - lineNumber.length();
		for (int i = 0; i < lineDiff; i++)
			lineNumber += " ";
		if (echo_bad && !echo_all) {
			out.println("");
			out.println(currentLine.numbered(start, stop));
		}
		out.println(lineNumber + "  Error: " + message);
	}

	/**
	 * @see ErrorWriter#printWarning(String, int, int)
	 */
	public void printWarning(String message, int start, int stop) {
		total_warn++;
		if (!echo_warn)
			return;
		String lineNumber =
			enhanceACharacterNumber(
				currentLine.getLineNum(start),
				currentLine.getColumn(start))
				+ ":";

		int lineDiff = errorTab - lineNumber.length();
		for (int i = 0; i < lineDiff; i++) {
			lineNumber += " ";
		}
		if (echo_bad && !echo_all) {
			out.println("");
			out.println(currentLine.numbered(start, stop));
		}
		out.println(lineNumber + "Warning: " + message);
	}

	/**
	 * @see ErrorWriter#printGeneralError(String)
	 */
	public void printGeneralError(String message) {
		total_generr++;
		String lineNumber = "";

		int lineDiff = 16 - 14;
		for (int i = 0; i < lineDiff; i++) {
			lineNumber += " ";
		}
		out.println("");
		out.println(lineNumber + "General Error: " + message);
		out.println("");
	}

	/**
	 * @see ErrorWriter#printErrorTotals()
	 */
	public void printErrorTotals() {
		if (!echo_totals)
			return;
		if ((total_err == 0) && (total_warn == 0) && (total_generr == 0))
			out.println("No errors or warnings.");
		else
			out.println(
				total_err
					+ " Errors, "
					+ total_warn
					+ " Warnings, "
					+ total_generr
					+ " General Errors.");
	}
}
