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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.batik.svggen.*;
import org.apache.xerces.dom.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.descriptors.attributes.*;

import com.jrefinery.chart.*;
import com.jrefinery.data.*;

import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;
/** 
 * This class offers a function to compare two Descriptor data files.
 * 
 * <h3>Running the software</h3>
 * <p><code>java viper.comparison.ViperPE <i>-options</i></code>
 * <br>Options include:
 * <ul>
 *   <li> -pr <i>properties file</i> : set the properties file. This
 *            specifies defaults that can be overriden in the command line.
 *            (See below)</li>
 *   <li> -b <i>file name root</i> : the default name to check for all file
 *            types </li>
 *   <li> -g <i>truth file</i> : the name of the ground truth file </li>
 *   <li> -r <i>results file</i>: the name of the results file </li>
 *   <li> -epf <i>evaluation parameters file</i> : the name of the file with
 *             equivalencies, evaluations, and filters</li>
 *   <li> -gc <i>ground truth configuration file</i> : if the truth config is
 *            in a separate file, then set this to that file</li>
 *   <li> -rc <i>results data configuration file</i> : if the results config
 *            is in a seperate file, then set this to that file</li>
 *   <li> -o <i>verbose output file</i> : set this to '-' to direct to
 *           <code>System.out</code></li>
 *   <li> -raw <i>raw data output file</i> : where to output the raw format
 *             data</li>
 *   <li> -L# <i>level of matching</i> : change the level specified in the
 *            properties file, eg L1, L2, etc.</li>
 *   <li> -P<b>property</b> <i>value</i> : override a specific property-value
 *             pair from the properties file</li>
 * </ul>
 * </p>
 *
 * <h3>Properties File Format</h3>
 * <UL>
 *   <LI>File Configuration section
 *     <ul>
 *       <li>gtconfig_file: where the truth config is stored.
 *        Not necessary if the same as gt_file </li>
 *       <li>gt_file: where the truth data (Target set) is stored.</li>
 *       <li>resultsconfig_file: where the results config info is
 *        stored. Not necessary if the same as results_file.</li>
 *       <li>results_file: where the results data (Candidate set) is stored.</li>
 *       <li>epf_file: name of the Evaluation Parameters file. It contains which
 *        attributes to evaluate, typewise metrics and tolerances, and
 *        equivalencies between the result and truth naming conventions.</li>
 *       <li>output_file: where to print the verbose output. Defaults to System out.</li>
 *       <li>raw_file: Where to print the raw output. Defaults to no raw output.</li>
 *       <li>base: the base file name, same as -b option</li>
 *     </ul>
 *   </LI>
 *   <LI>Formatting
 *     <ul>
 *       <li>verbose: true or false - prints out distance coefficients</li>
 *       <li>attrib_width: the width of the data to print in the output file</li>
 *     </ul>
 *   </LI>
 *   <LI>Descriptor-wide Metric Configuration
 *     <ul>
 *       <li>level: what level to take the comparison.
 *         <ul>
 *           <li>1 = Detection: the objects overlap temporally better than some threshold
 *              see range_tol, rmetric_default</li>
 *           <li>2 = Localization
 *              performs detection using only the frames whose attributes
 *              meet necessary thresholds</li>
 *           <li>3 = Statistical Comparison
 *              the average/median/max/min, depending on which was
 *              selected, meets a certain threshold</li>
 *         </ul>
 *       </li>
 *       <li>target_match: How to crop the set of possibles at the end - object evaluation only.
 *         <ul>
 *           <li>ALL, DEFAULT = No cropping</li>
 *           <li>SINGLE-GREEDY = Only allow one Candidate for each Target</li>
 *           <li>SINGLE-BEST = Allow only one Candidate for each Target and
 *              vice-versa</li>
 *           <li>MULTI-BEST = Allow multiple Candidates to be mapped to a single
 *              Target using composition</li>
 *         </ul>
 *       </li>
 *       <li>range_metric: the distance metric to use for the frame overlap test
 *         <ul>
 *           <li>dice = 1- Dice coefficient (2 & intersection / sum of candidate
 *              and target)</li>
 *           <li>overlap = 1 - amount of candidate on target / size of target</li>
 *           <li>E = equality (0 if equal, 1 if not)</li>
 *         </ul>
 *       </li>
 *       <li>range_tol: comparisons that indicate the distance between two 
 *         descriptor's frame span is greater than this will be dropped.</li>
 *       <li>level3_metric: the statistic to use for statistical comparison.
 *         either minimum, maximum, median, or average</li>
 *       <li>level3_tol: the tolerance to use on the generated distance statistic
 *         Anything less than or equal to this will still count as a valid
 *         comparison.</li>
 *     </ul>
 *   </LI>
 *   <LI>Attribute Metric and Tolerance Configuration
 *     <ul>
 *       <li>svalue_metric: The default metric for comparison of svalues.
 *         <ul>
 *           <li>L = Levenshtein (edit) distance</li>
 *           <li>H = Hamming (Strings of different length are counted as failures)</li>
 *           <li>E = equality (either 0 or 1)</li>
 *         </ul>
 *       </li>
 *       <li>bvalue_tol: tolerance for bvalue attributes</li>
 *       <li>dvalue_tol: tolerance for dvalue attributes</li>
 *       <li>fvalue_tol: tolerance for fvalue attributes</li>
 *       <li>svalue_tol: tolerance for svalue attributes</li>
 *       <li>point_tol: tolerance for point attributes</li>
 *       <li>circle_tol: tolerance for circle attributes</li>
 *       <li>bbox_tol: tolerance for bbox attributes</li>
 *       <li>obox_tol: tolerance for obox attributes</li>
 *       <li>lvalue_tol: tolerance for lvalue attributes</li>
 *     </ul>
 *   </LI>
 * </UL>
 * 
 * @author David Doermann
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @author Felix Suhenko
 */
public class ViperPE {
	static final int BAD_COMMAND_LINE = 0;

	static final int BAD_GTF_FILE = 1;
	static final int BAD_LOG_FILE = 2;
	static final int BAD_OUTPUT_FILE = 3;

	static final int BAD_XML_DATA = 5;
	static final int BAD_GTF_DATA = 6;

	static final int NO_CLASS_DEF_FOUND = 7;

	/**
	 * Print usage.
	 */
	public static void printUsage() {
		printUsage(ViperPE.BAD_COMMAND_LINE, null);
	}

	/** 
	 * Prints out some of the basic flags. For more, see the javadoc
	 * or the README.
	 * @param error the error type
	 * @param trace the throwable associated with the error
	 */
	public static void printUsage(int error, Throwable trace) {
		String err = "\nUsage: ViperPE [OPTIONS] [ -g <GT file>]";
		switch (error) {
			case ViperPE.BAD_COMMAND_LINE :
				if (trace != null) {
					err += "\n" + trace.getMessage() + "\n";
				}
				err += "\nOPTIONS:\n"
					+ "    -b    <BASE> set a base for .gtf, rdf, .o, and .l files \n"
					+ "    -r    <RESULTS file> results file with which  GTF will be compared\n"
					+ "            If not given standard input is used\n"
					+ "    -o    <OUTPUT file> name of the file into which you wish the\n"
					+ "            output to be stored. '-' is standard output [default]\n"
					+ "    -l    <LOG file> name of the file into which the warnings\n"
					+ "            will be written.  '-' is standard output [default]\n"
					+ "    -gc   <GT CONFIG file> name of the file which holds the\n"
					+ "            ground truth configuration information\n"
					+ "    -rc   <RESULTS CONFIG file> name of the file which holds the\n"
					+ "            result file configuration information\n"
					+ "    -epf  <EVALUATION PARAMETERS file>\n"
					+ "    -pr   <PROPERTIES file>\n"
					+ "    -L    <#> Level at which to evaluate\n"
					+ "              (0=None, 1=Temporal Match, 2=Frame Constrained,\n"
					+ "               3=Attribute Constrained)\n"
					+ "    -raw  <RAW OUTPUT file> name of the file into which you wish\n"
					+ "            to store the raw format of the output\n"
					+ "    -P<property> <value>  Override a specific property\n"
					+ "\n";
				break;

			case ViperPE.BAD_LOG_FILE :
				err += "\nError opening log file for output";
				if (trace != null) {
					err += ": " + trace.getMessage();
				}
				err
					+= "\n Log file is specified with the '-l <fname>.log' command line option"
					+ "\n    or the 'log_file' property";
				break;
			case ViperPE.BAD_OUTPUT_FILE :
				err += "\nError opening output file";
				if (trace != null) {
					err += ": " + trace.getMessage();
				}
				err
					+= "\n Output file is specified with the '-o <fname>.out' command line option"
					+ "\n    or the 'output_file' property, and defaults to standard output.";
				break;
			case ViperPE.BAD_GTF_FILE :
				err += "\nError opening GTF file for output.";
				if (trace != null) {
					err += ": " + trace.getMessage();
				}
				break;

			case ViperPE.BAD_GTF_DATA :
				err += "\nError reading GTF format data";
				if (trace != null) {
					err += ": " + trace.getMessage();
				}
				break;
			case ViperPE.BAD_XML_DATA :
				err += "\nError reading XML format data";
				if (trace != null) {
					err += ": " + trace.getMessage();
				}
				break;

			case ViperPE.NO_CLASS_DEF_FOUND :
				err += "\nError in configuration: Cannot find java class '"
					+ trace
					+ "'"
					+ "\nThis is most likely due to a missing Java archive"
					+ "\nfile in your CLASSPATH variable.  To fix this,"
					+ "\nmake sure to run 'perl config.pl' (or 'cscript "
					+ "\nconfig.js' on Windows) from your viper directory."
					+ "\nOn Unix, you will have to source 'viper.config'"
					+ "\nbefore trying to run the software again.";
				trace.printStackTrace();
				break;
			default :
				err += "\nUnknown error message: " + error;
				trace.printStackTrace();
		}
		err += "\nFor more detailed documentation, peruse the docs directory."
			+ "\n\nReport bugs to viper-bugs@cfar.umd.edu";
		System.err.println(err);
		System.exit(error);
	}

	/** 
	 * Prints out version information. It does not include any
	 * RCS variables.
	 */
	public static void printVersion() {
		System.err.println("ViPER Performance Evaluation Utility version 1.2");
		System.exit(0);
	}

	/**
	 * Parses the options into a hashmap.
	 * @param args the command line arguments
	 * @return the argument map, or <code>null</code> if the 
	 * arguments are malformed. (It also prints out usage in 
	 * the latter case.)
	 */
	public static Map getOptions(String[] args) {
		try {
			HashMap opts = new HashMap();
			for (int index = 0; index < args.length; index += 2) {
				if ("--help".equals(args[index]) || "-h".equals(args[index])) {
					printUsage();
				} else if (
					"--version".equals(args[index])
						|| "-v".equals(args[index])) {
					printVersion();
				} else {
					opts.put(args[index], args[index + 1]);
				}
			}
			return opts;
		} catch (ArrayIndexOutOfBoundsException aioobx) {
			printUsage(BAD_COMMAND_LINE, aioobx);
			return null;
		}
	}
	
	/**
	 * The main program block of the command line version of the Ground 
	 * Truth File Comparison software.
	 *
	 * @param args The command line arguments are specified at the top.
	 *    For the properties, see the <a href="package-summary.html">package listing</a>.
	 */
	public static void main(String[] args) {
		PrintWriter logFile = null;
		try {
			String baseFileName = null;
			String propertiesFileName = null;

			// Input all of the properties from the command line or Properties file
			Properties props = new Properties();
			props.put("log_file", "-");
			props.put("output_file", "-");
			props.put("target_match", "ALL");

			props.put("range_metric", "dice");
			props.put("level3_metric", "minimum");

			props.put("range_tol", "0");
			props.put("level3_tol", "0");
			props.put("level", "3");
			props.put("verbose", "false");
			props.put("attrib_width", "0");

			/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
			   Process Command Line Arguments
			 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
			if (args.length < 2) {
				System.err.println("\nERROR: Insufficient Arguments");
				printUsage();
			} else {
				Map options = getOptions(args);
				if (options.containsKey("-b")) {
					baseFileName = (String) options.remove("-b");
				}
				if (options.containsKey("-pr")) {
					propertiesFileName = (String) options.remove("-pr");
				} else if (baseFileName != null) {
					propertiesFileName = baseFileName + ".pr";
				}

				if (propertiesFileName != null
					&& !"".equals(propertiesFileName)) {
					try {
						props.load(new FileInputStream(propertiesFileName));
					} catch (FileNotFoundException fnfx) {
						System.err.println("\nERROR: " + fnfx.getMessage());
						printUsage();
					} catch (IOException iox) {
						System.err.println(
							"IO Exception while reading properties file.");
					}
				}
				if (baseFileName == null) {
					baseFileName = props.getProperty("base");
				}

				if (baseFileName != null) {
					props.put("gt_file", baseFileName + ".gtf");
					props.put("results_file", baseFileName + ".rdf");
					props.put("output_file", baseFileName + ".out");
					props.put("epf_file", baseFileName + ".epf");
				}
				String[][] switchMap = new String[][] { { "-g", "gt_file" }, {
						"-r", "results_file" }, {
						"-o", "output_file" }, {
						"-epf", "epf_file" }, {
						"-gc", "gtconfig_file" }, {
						"-rc", "resultsconfig_file" }, {
						"-l", "log_file" }, {
						"-raw", "raw_file" }, {
						"-graph", "graph_file_template" }, {
						"-L", "level" }
				};
				for (int i = 0; i < switchMap.length; i++) {
					if (options.containsKey(switchMap[i][0])) {
						props.put(
							switchMap[i][1],
							options.remove(switchMap[i][0]));
					}
				}
				if (options.size() > 0) {
					for (Iterator iter = options.entrySet().iterator();
						iter.hasNext();
						) {
						Map.Entry curr = (Map.Entry) iter.next();
						String currSwitch = (String) curr.getKey();
						String currVal = (String) curr.getValue();
						if (!currSwitch.startsWith("-P")) {
							printUsage(BAD_COMMAND_LINE, null);
						} else {
							props.put(currSwitch.substring(2), currVal);
						}
					}
					if (options.size() > 0) {
						printUsage();
					}
				}
			}

			ViperPE evaluator = new ViperPE();
			evaluator.setProperties(props);
			evaluator.run();
		} catch (NoClassDefFoundError ncdfe) {
			printUsage(ViperPE.NO_CLASS_DEF_FOUND, ncdfe);
		} catch (java.lang.ExceptionInInitializerError xiix) {
			xiix.printStackTrace();
		} catch (BadDataException bdx) {
			printUsage(ViperPE.BAD_COMMAND_LINE, bdx);
		} catch (ClassCastException ccx) {
			ccx.printStackTrace();
		} catch (Throwable rx) {
			printUsage(-1, rx);
		} finally {
			if (logFile != null)
				logFile.close();
		}
	}

	private String resultsFileName = null;
	private String gtFileName = null;
	private String gtcFileName = null;
	private String resultscFileName = null;
	private String epfFileName = null;
	private String outputFileName = null;
	private String logFileName = null;
	private String rawFileName = null;
	private String graphFileNameTemplate = null;

	int level = -1; // Default level
	int match = 0; // Default match ALL
	boolean verboseFlag = false; //
	/**
	 * Processes a properties file into ViperPE internal state variables.
	 */
	public ViperPE() {
	}
	
	/**
	 * Sets the properties associated with this run of the 
	 * performance evaluator.
	 * @param props the properties
	 * @throws BadDataException if some of the properties contain
	 * errors in syntax of viper data
	 * @throws ImproperMetricException if some of the properties
	 * contain references to unknowm metrics, or if they initialize
	 * the metrics incorrectly
	 */
	public void setProperties(Properties props)
		throws BadDataException, ImproperMetricException {
		//Handle Attribute defaults for tolerance, metric, etc. 
		Enumeration enumeration = props.propertyNames();
		while (enumeration.hasMoreElements()) {
			String current = (String) enumeration.nextElement();
			try {
				if (current.endsWith("_tol")) {
					if (current.equals("range_tol")) {
						DefaultMeasures.setDefaultToleranceFor(
							" framespan",
							Double.parseDouble(
								(String) props.get("range_tol")));
					} else if (current.equals("level3_tol")) {
						Distances.setDefaultSTolerance(
							Double.parseDouble(
								(String) props.get("level3_tol")));
					} else {
						String attrType =
							current.substring(0, current.length() - 4);
						if (Attribute.isType(attrType)) {
							DefaultMeasures.setDefaultToleranceFor(
								attrType,
								Double.parseDouble(
									(String) props.get(current)));
						} else {
							throw new BadDataException(
								"Tolerance for unknown attribute in properties : "
									+ current);
						}
					}
				} else if (current.endsWith("_metric")) {
					if (current.equals("range_metric")) {
						DefaultMeasures.setDefaultMetricFor(
							" framespan",
							props.getProperty("range_metric"));
					} else if (current.equals("level3_metric")) {
						Distances.setDefaultStatistic(
							props.getProperty("level3_metric"));
					} else if (current.equals("string_metric")) {
						DefaultMeasures.setDefaultMetricFor(
							"svalue",
							props.getProperty(current));
					} else {
						String attrType =
							current.substring(0, current.length() - 7);
						if (Attribute.isType(attrType)) {
							DefaultMeasures.setDefaultMetricFor(
								attrType,
								props.getProperty(current));
						} else {
							throw new ImproperMetricException(
								"Metric for unknown attribute in properties: "
									+ current);
						}
					}
				}
			} catch (ImproperMetricException imx) {
				throw new ImproperMetricException(
					imx.getMessage()
						+ "\n\tIn properties file, on key : "
						+ current);
			}
		}

		gtcFileName = props.getProperty("config_file");
		gtFileName = props.getProperty("gt_file");
		if (gtFileName == null) {
			throw new BadDataException("Must specify a target (ground truth data) file.");
		}
		resultscFileName = props.getProperty("resultsconfig_file");
		resultsFileName = props.getProperty("results_file");
		if (resultsFileName == null) {
			throw new BadDataException("Must specify a candidate (result data) file.");
		}

		epfFileName = props.getProperty("epf_file");
		if (epfFileName == null) {
			throw new BadDataException("Must specify an evaluation parameters file.");
		}

		// Set Object Evaluation parameters: LEVEL and TARGET MATCH
		try {
			if (level < 0)
				level = Integer.parseInt(props.getProperty("level"));
			if (level < 0) {
				level = 2;
			}
		} catch (NumberFormatException nfx) {
			System.err.println(
				"Invalid evaluation level: " + props.getProperty("level"));
			System.err.println(
				"Must be a numeric value, eg "
					+ Comparison.LOCALIZED
					+ " for Localization.");
		}
		try {
			match =
				CompFilter.getCroppingType(props.getProperty("target_match"));
		} catch (IllegalArgumentException iax) {
			System.err.println(iax.getMessage());
			System.err.println(
				"target_match must be a cropping type, "
					+ "eg SINGLE or MULTIPLE");
		}

		// Set output parameters
		logFileName = props.getProperty("log_file");
		outputFileName = props.getProperty("output_file");
		rawFileName = props.getProperty("raw_file");
		graphFileNameTemplate = props.getProperty("graph_file_template");
		verboseFlag =
			Boolean.valueOf(props.getProperty("verbose")).booleanValue();
		try {
			Attribute.setOutputWidth(
				Integer.parseInt(props.getProperty("attrib_width")));
		} catch (NumberFormatException nfx) {
			System.err.println(
				"Invalid maximum output width: "
					+ props.getProperty("attrib_width"));
			System.err.println("Must be a numeric value; set to 0 for none.");
		}
	}

	/**
	 * Opens the file with the specified name for writing. Overwrites the file
	 * if it exists. Silently fails if there is an I/O exception by setting to
	 * null.
	 * If filename is null, an empty string, or an invalid file, this returns
	 * <code>null</code>. If the string is a dash, "-", it returns System.out.
	 * @param fileName The file to open for writing.
	 * @return A new <code>PrintWriter</code> or <code>null</code>
	 */
	public static PrintWriter openFileForWriting(String fileName) {
		if ((fileName != null) && (!fileName.equals(""))) {
			try {
				if (fileName.equals("-")) {
					return new PrintWriter(System.out, true);
				} else {
					return new PrintWriter(new FileWriter(fileName));
				}
			} catch (IOException iox) {
				System.err.println("ERROR: " + iox.getMessage());
			}
		}
		return null;
	}

	/**
	 * Reads in config information from a gtf file.
	 * @param configFileName File name of the config file - only applicable
	 *    to old file format, which can take multiple files and glue them
	 *    together
	 * @param dataFileName The data file. If old format, will use config file
	 *    if is not null, and this otherwise. If xml format, only uses data
	 *    file.
	 * @return the parsed configuration information
	 * @throws BadDataException if the parseConfig or addDesConfig method
	 *    throws it, or if an xml file is given two file names.
	 * @throws IOException if there is an io exception while parsing
	 */
	public static DescriptorConfigs parseDescriptorConfig(
		String configFileName,
		String dataFileName)
		throws IOException, BadDataException {
		boolean formatIsXML =
			StringHelp.isXMLFormat(configFileName, dataFileName);
		DescriptorConfigs cfgs = null;
		if (!formatIsXML) {
			Vector files = new Vector();
			cfgs = new DescriptorConfigs(null);
			if (configFileName != null) {
				files.addElement(configFileName);
			} else {
				files.addElement(dataFileName);
			}
			cfgs.parseConfig(files, false, true, true, true, true);
		} else {
			if (configFileName != null) {
				throw new BadDataException(
					"XML Format doesn't allow seperate config file: "
						+ configFileName);
			}
			cfgs = new DescriptorConfigs(null);
			cfgs.addDesConfig(new InputSource(dataFileName));
		}
		return cfgs;
	}

	/**
	 * Reads in data information from a gtf file.
	 * @param cfgs Configs, presumably from parseDescriptorConfig
	 * @param dataFileName The data file to parse.
	 * @param epf used to run the input filter
	 * @param log recipient of the various error and notification messsages
	 * @return the data taken from the given file that meets the filters
	 * @throws BadDataException if the parseConfig or addDesConfig method
	 *    throws it, or if an xml file is given two file names.
	 * @throws IOException if there is an io exception while parsing
	 */
	public static DescriptorData parseDescriptorData(
		DescriptorConfigs cfgs,
		String dataFileName,
		EvaluationParameters epf,
		PrintWriter log)
		throws BadDataException, IOException {
		boolean formatIsXML = StringHelp.isXMLFormat(dataFileName);
		DescriptorData data = new DescHolder();
		if (!formatIsXML) {
			Vector files = new Vector();
			files.addElement(dataFileName);
			data.parseData(
				files,
				cfgs,
				epf.getTargetInputFilter(),
				epf.getMap());
		} else {
			data.parseData(
				new InputSource(dataFileName),
				epf.getMap(),
				epf.getTargetInputFilter(),
				cfgs,
				log);
		}
		return data;
	}

	/**
	 * Runs the application.
	 */
	public void run() {
		PrintWriter logFile = null;
		PrintWriter rawFile = null;
		PrintWriter outFile = null;

		Date timeNow = new Date();
		DateFormat timeFmt = new SimpleDateFormat("hh:mm:ss");

		if (outputFileName != null) {
			System.err.println(
				"****** NOTE: All output directed to: "
					+ outputFileName
					+ " ******");
		}

		// Open all output files
		rawFile = openFileForWriting(rawFileName);
		logFile = openFileForWriting(logFileName);
		outFile = openFileForWriting(outputFileName);

		// Print out Input Parameters
		if (outFile != null) {
			outFile.print (StringHelp.banner("INPUT PARAMETERS", 53));
			outFile.println();
			outFile.println("Ground Truth: " + gtFileName);
			outFile.println("     Results: " + resultsFileName);
			outFile.println(" Eval Params: " + epfFileName);
			outFile.println("         Log: " + logFileName);
			outFile.println("      Output: " + outputFileName);
			outFile.println();
			outFile.println("       Level: " + level);
			outFile.println("       Match: " + CompFilter.matchFilterTitle(match));
			outFile.println("      metric: " + Distances.getDefaultStatistic());
			outFile.println(
				"   tolerance: " + Distances.getDefaultSTolerance());
		}
		if (rawFile != null) {
			rawFile.println("// ");
			rawFile.println("// ");
			rawFile.println("// ");
			rawFile.println("#BEGIN_PARAMETERS");
			rawFile.println("gt_file = " + gtFileName);
			rawFile.println("result_file = " + resultsFileName);
			rawFile.println("epf_file = " + epfFileName);
			rawFile.println("log_file = " + logFileName);
			rawFile.println("output_file = " + outputFileName);
			rawFile.println("level = " + level);
			rawFile.println("#END_PARAMETERS");
		}

		try {
			//Read in the data
			Vector files = new Vector();
			//A list of filenames to pass the Reader.

			/* ------------------------------------------------------------------------ * 
			  Parse the GROUND TRUTH CONFIG (gtc or gt)
			    - first check for the config file, otherwise look at the top of GT file
			 * ------------------------------------------------------------------------ */
			DescriptorConfigs gtCfgs = null;
			try {
				printLog(
					logFile,
					"Parsing Target Config information.",
					timeNow,
					timeFmt);
				gtCfgs = parseDescriptorConfig(gtcFileName, gtFileName);
			} catch (IOException iox) {
				printLog(
					logFile,
					"Error in Target Config file: " + iox.getMessage(),
					timeNow,
					timeFmt);
				printUsage(ViperPE.BAD_GTF_DATA, iox);
			} catch (BadDataException bdx) {
				printLog(
					logFile,
					"Error in Target Config: " + bdx.getMessage(),
					timeNow,
					timeFmt);
				printUsage(ViperPE.BAD_GTF_DATA, bdx);
			}

			DescriptorConfigs rdCfgs = null;
			try {
				printLog(
					logFile,
					"Parsing Candidate Config information.",
					timeNow,
					timeFmt);
				rdCfgs =
					parseDescriptorConfig(resultscFileName, resultsFileName);
			} catch (IOException iox) {
				printLog(
					logFile,
					"Error in Candidate Config file: " + iox.getMessage(),
					timeNow,
					timeFmt);
				printUsage(ViperPE.BAD_GTF_DATA, iox);
			} catch (BadDataException bdx) {
				printLog(
					logFile,
					"Error in Candidate Config: " + bdx.getMessage(),
					timeNow,
					timeFmt);
				printUsage(ViperPE.BAD_GTF_DATA, bdx);
			}

			/* ------------------------------------------------------------------------ * 
			    Parse the EVALUATION PARAMETERS FILE (epf)
			      - First look for Equivalences
			      - Second set up Evaluations
			 * ------------------------------------------------------------------------ */
			printLog(
				logFile,
				"Parsing Evaluation Parameter information.",
				timeNow,
				timeFmt);
			EvaluationParameters epf = new EvaluationParameters(gtCfgs);
			if ((epfFileName != null) && ((new File(epfFileName)).canRead())) {
				files.removeAllElements();
				files.addElement(epfFileName);
				VReader epfReader = new VReader(files);
				try {
					epf.parse(epfReader, level, match);
				} finally {
					try {
						epfReader.close();
					} catch (IOException iox) {
						printLog(
							logFile,
							"I/O error with EPF file: " + epfFileName,
							timeNow,
							timeFmt);
						printUsage();
					}
				}
			}
			files.removeAllElements();

			/* ------------------------------------------------------------------------ * 
			  Parse the GROUND TRUTH (gt)
			  * ------------------------------------------------------------------------ */
			printLog(logFile, "Parsing target data.", timeNow, timeFmt);
			DescriptorData data = null;
			try {
				data = parseDescriptorData(gtCfgs, gtFileName, epf, logFile);
			} catch (IOException iox) {
				printLog(logFile, iox.getMessage(), timeNow, timeFmt);
				printUsage(ViperPE.BAD_XML_DATA, iox);
			} catch (BadDataException bdx) {
				printLog(logFile, bdx.getMessage(), timeNow, timeFmt);
				printUsage(ViperPE.BAD_XML_DATA, bdx);
			}

			/* ------------------------------------------------------------------------ *
			   Parse the RESULTS DATA FILE (rdf)
			 * ------------------------------------------------------------------------ */
			printLog(logFile, "Parsing candidate data.", timeNow, timeFmt);
			DescriptorData inputData = null;
			try {
				inputData =
					parseDescriptorData(rdCfgs, resultsFileName, epf, logFile);
			} catch (IOException iox) {
				printLog(logFile, iox.getMessage(), timeNow, timeFmt);
				printUsage(ViperPE.BAD_GTF_DATA, iox);
			} catch (BadDataException bdx) {
				printLog(logFile, bdx.getMessage(), timeNow, timeFmt);
				printUsage(ViperPE.BAD_GTF_DATA, bdx);
			}

			/* ------------------------------------------------------------------------ *
			   Generating comparison Matrix
			 * ------------------------------------------------------------------------ */
			for (Iterator evas = epf.getEvas(); evas.hasNext();) {
				Evaluation eva = (Evaluation) evas.next();
				printLog(
					logFile,
					"Generating comparison matrix for " + eva.getName(),
					timeNow,
					timeFmt);
				double lev3tol = Distances.getDefaultSTolerance();
				CompEvaluator comps =
					new CompEvaluator(
						data,
						inputData,
						lev3tol,
						match,
						epf.getScopeRulesFor(eva),
						logFile);

				if (outFile != null) {
					epf.printVerboseParameters(outFile);
				}
				if (rawFile != null) {
					epf.printTerseParameters(rawFile);

					rawFile.println("#BEGIN_GTF_INFORMATION\n");
					rawFile.println(data.getInformation());
					rawFile.println("#END_GTF_INFORMATION\n");

					rawFile.println("#BEGIN_RDF_INFORMATION\n");
					rawFile.println(inputData.getInformation());
					rawFile.println("#END_RDF_INFORMATION\n");
				}

				printLog(logFile, "Performing evaluation.", timeNow, timeFmt);
				eva.setOutput(outFile);
				eva.setRaw(rawFile);
				if (graphFileNameTemplate != null) {
					Map datasets =
						comps.printEvaluationResults(eva).getDatasets(
							"frame-all");
					int offset = graphFileNameTemplate.indexOf("{}");
					System.err.println(" gfnt = " + graphFileNameTemplate);
					String prefix =
						(offset < 0)
							? ""
							: graphFileNameTemplate.substring(0, offset);
					String suffix =
						(offset < 0)
							? graphFileNameTemplate
							: graphFileNameTemplate.substring(offset + 2);
					printGraphs(datasets, prefix, suffix);
				} else {
					comps.printEvaluationResults(eva);
				}
			} // for each evaluation
			printLog(logFile, "Cleaning up...", timeNow, timeFmt);
		} finally { //close the files
			if (rawFile != null) {
				rawFile.close();
			}
			if (outFile != null) {
				outFile.close();
			}
			if (logFile != null) {
				logFile.close();
			}
			System.err.flush();
			System.out.flush();
		}
	} // run()

	private static void printLog(
		PrintWriter logFile,
		String msg,
		Date timeNow,
		DateFormat timeFmt) {
		if (logFile != null) {
			if (timeNow != null && timeFmt != null) {
				timeNow.setTime(System.currentTimeMillis());
				logFile.print(timeFmt.format(timeNow));
			}
			logFile.println("\t " + msg);
			logFile.flush();
		}
	}

	/**
	 * Prints out all the graphs for the given set of data.
	 * @param data the data sets
	 * @param prefix the prefix for each chart filename
	 * @param suffix the suffix for each chart filename
	 */
	public static void printGraphs(Map data, String prefix, String suffix) {
		for (Iterator iter = data.entrySet().iterator(); iter.hasNext();) {
			Map.Entry currEntry = (Map.Entry) iter.next();
			String name = currEntry.getKey().toString();
			// Distances or strings
			Dataset currSet = (Dataset) currEntry.getValue();

			JFreeChart currChart = createChart(name, currSet);
			if (currChart == null)
				continue;
			try {
				if (suffix.endsWith(".svg")) {
					printChartSvg(prefix + name + suffix, currChart, 352, 240);
				} else if (suffix.endsWith(".png")) {
					printChartPng(prefix + name + suffix, currChart, 352, 240);
				} else {
					System.err.println(
						"Do not know how to print out files of type: "
							+ suffix);
				}
			} catch (IOException iox) {
				System.err.println("Error while printing graph: " + name);
				System.err.println("\t I/O Error: " + iox.getMessage());
			}
		}
	}
	
	/**
	 * Prints out the results as an svg chart.
	 * @param fileName the file to save the results
	 * @param chart the chart
	 * @param width the width to give the output file
	 * @param height the height for the output file
	 * @throws IOException
	 */
	public static void printChartSvg(
		String fileName,
		JFreeChart chart,
		int width,
		int height)
		throws IOException {
		OutputStreamWriter writer = new FileWriter(fileName);

		try {
			// Get a DOMImplementation
			DOMImplementation domImpl =
				DOMImplementationImpl.getDOMImplementation();
			// Create an instance of org.w3c.dom.Document
			org.w3c.dom.Document document =
				domImpl.createDocument(null, "svg", null);

			// Create an SVG Context to customise
			SVGGeneratorContext ctx =
				SVGGeneratorContext.createDefault(document);
			ctx.setComment(
				"Generated by JFreeServlet using Batik SVG Generator");

			// Create an instance of the SVG Generator
			SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);
			svgGenerator.setSVGCanvasSize(new Dimension(width, height));
			chart.draw(
				svgGenerator,
				new Rectangle2D.Double(0, 0, width, height),
				null);
			svgGenerator.stream(writer, false);
		} finally {
			writer.close();
		}
	}
	
	/**
	 * Saves the given chart a PNG file.
	 * @param fileName the file to save to
	 * @param chart the chart to save
	 * @param width the width of the chart
	 * @param height the height of the chart
	 * @throws IOException
	 */
	public static void printChartPng(
		String fileName,
		JFreeChart chart,
		int width,
		int height)
		throws IOException {
		ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
	}

	/**
	 * Creates a new chart for the given data set
	 * @param name the name of the chart
	 * @param data the data set
	 * @return the new chart
	 */
	public static JFreeChart createChart(String name, Dataset data) {
		if (data instanceof XYDataset) {
			return ChartFactory.createXYChart(
				name,
				"X-Axis",
				"Y-Axis",
				(XYDataset) data,
				true);
		} else if (data instanceof CategoryDataset) {
			return ChartFactory.createVerticalBarChart(
				name,
				"X-Axis",
				"Y-Axis",
				(CategoryDataset) data,
				true);
		} else {
			System.err.println("Do not know how to render graph for: " + name);
			return null;
		}
	}
}
