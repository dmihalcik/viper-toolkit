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

package viper.descriptors;

import java.io.*;
import java.util.*;

import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import viper.descriptors.attributes.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Class for converting from old .gtf format to new XML
 * format.
 */
public class Converter {
	
	/**
	 * Prints the usage statement for the gtf file conversion
	 * application.
	 * @param output the stream to receive the usage statement.
	 */
	public static void printUsage(PrintWriter output) {
		output.println("Usage: Converter -i[x/g] -o[x/g] <files>");
		output.println("    -i      input format. x=xml, g=old format");
		output.println("    -o      output format. x=xml, g=old format");
		output.println(
			"    file    the input files to be merged and converted");
		output.println(
			"            containing the ground truth (defaults to std in)");
		output.println(
			"    -clip   only print out the first frame of each object");
		output.println(
			"    -split  splits descriptors into framewise contiguous objects");
		output.println(
			"    -cropN  removes all frames greater than or equal to N. Called before shift.");
		output.println(
			"    -shiftN shift the frames by N. Negative frames are dropped (but not 0)");
		output.println(
			"    -defwxh add width and height if not found, eg -def320x240 or -def640x480");
		output.println("    -filter use the specified filter file");
		output.close();
	}

	/**
	 * Converts a GTF to an XML file.  This method is called from Viper GT's
	 * ViperFrame class when "Save as XML" menu item is chosen in Viper's GUI.
	 * @param gtfName the name of the file to be saved as XML
	 * @param out the stream to which the method will write the XML output.
	 * @throws IOException
	 */
	public static void saveAsXml(String gtfName, PrintWriter out)
		throws IOException {
		DescHolder data = new DescHolder();
		try {
			Vector files = new Vector(1);
			files.add(gtfName);

			DescriptorConfigs cfgs = new DescriptorConfigs(null);
			cfgs.parseConfig(files, false, true, true, true, false);
			data.parseData(
				files,
				cfgs,
				new RuleHolder(),
				new Equivalencies(),
				false,
				true,
				true,
				true,
				false);
		} catch (Exception x) {
			x.printStackTrace();
		}
		Converter con = new Converter(data, null);
		con.writeXml(out, true);
		out.close();
	}
	
	/**
	 * Converts some gtf files into another format.
	 * @param args see the usage statement
	 * @throws IOException if there is an error while reading from 
	 * or writing to the input or output files
	 */
	public static void main(String[] args) throws IOException {
		try {
			if (2 > args.length) {
				printUsage(new PrintWriter(System.err));
				return;
			}

			DescHolder data = new DescHolder();
			boolean clip = false;
			boolean split = false;
			boolean deform = false;
			int shift = 0;
			boolean isCropping = false;
			int crop = 0;
			boolean inXml = false;
			boolean outXml = false;
			String filterName = null;
			RuleHolder filter = new RuleHolder();
			int fnameStart;

			int[] newDimensions = null; // width, height

			for (fnameStart = 0;
				fnameStart < args.length && args[fnameStart].startsWith("-");
				fnameStart++) {
				if (args[fnameStart].equals("-clip")) {
					clip = true;
				} else if (args[fnameStart].equals("-split")) {
					split = true;
				} else if (args[fnameStart].startsWith("-shift")) {
					String shiftString =
						args[fnameStart].substring("-shift".length());
					try {
						shift = Integer.parseInt(shiftString);
					} catch (NumberFormatException nfx) {
						System.err.println(
							"Invalid shift (must be integer): '"
								+ shiftString
								+ "'");
						printUsage(new PrintWriter(System.err));
						return;
					}
				} else if (args[fnameStart].startsWith("-crop")) {
					String cropString =
						args[fnameStart].substring("-crop".length());
					try {
						crop = Integer.parseInt(cropString);
						isCropping = true;
					} catch (NumberFormatException nfx) {
						System.err.println(
							"Invalid shift (must be integer): '"
								+ cropString
								+ "'");
						printUsage(new PrintWriter(System.err));
						return;
					}
				} else if (args[fnameStart].equals("-deform")) {
					deform = true;
				} else if (args[fnameStart].startsWith("-def")) {
					String dims =
						args[fnameStart]
							.substring("-def".length())
							.toLowerCase();
					try {
						int xLocation = dims.indexOf("x");
						newDimensions =
							new int[] {
								Integer.parseInt(dims.substring(0, xLocation)),
								Integer.parseInt(
									dims.substring(xLocation + 1))};
						if (newDimensions[0] <= 0 || newDimensions[1] <= 0) {
							throw new NumberFormatException();
						}
					} catch (StringIndexOutOfBoundsException sioobx) {
						System.err.println(
							"Malformed frame size dimensions: '" + dims + "'");
						printUsage(new PrintWriter(System.err));
						return;
					} catch (NumberFormatException nfx) {
						System.err.println(
							"Invalid default image size: '" + dims + "'");
						printUsage(new PrintWriter(System.err));
						return;
					}
				} else if (args[fnameStart].equals("-ix")) {
					inXml = true;
				} else if (args[fnameStart].equals("-ox")) {
					outXml = true;
				} else if (args[fnameStart].equals("-filter")) {
					if (args.length == ++fnameStart) {
						System.err.println(
							"Must specify a file name for the filter");
						printUsage(new PrintWriter(System.err));
						return;
					}
					filterName = args[fnameStart];
				} else if (
					!args[fnameStart].equals("-ig")
						&& !args[fnameStart].equals("-og")) {
					System.err.println(
						"Unrecognized parameter: " + args[fnameStart]);
					printUsage(new PrintWriter(System.err));
					return;
				}
			}

			try {
				if (inXml) {
					try {
						InputSource input;
						if (args.length > fnameStart) {
							input = new InputSource(args[fnameStart]);
							Document root = getRoot(input);
							DescriptorConfigs cfgs = null;
							if (filterName != null) {
								cfgs = getConfigs(root);
								filter = getRuleHolder(cfgs, filterName);
							}
							data.parseData(
								root,
								new Equivalencies(),
								filter,
								cfgs,
								null,
								args[fnameStart]);
							for (int i = fnameStart + 1;
								i < args.length;
								i++) {
								DescHolder next = new DescHolder();
								input = new InputSource(args[i]);
								input.setPublicId(args[i]);
								next.parseData(
									input,
									new Equivalencies(),
									filter,
									cfgs,
									null);
								data.merge(next);
							}
							if (args.length > fnameStart + 1) {
								data.resetIds();
							}
						} else {
							input = new InputSource(System.in);
							input.setPublicId("standard input");
							input.setSystemId("standard input");
							Document root = getRoot(input);
							DescriptorConfigs cfgs = null;
							if (filterName != null) {
								cfgs = getConfigs(root);
								filter = getRuleHolder(cfgs, filterName);
							}
							data.parseData(
								root,
								new Equivalencies(),
								filter,
								cfgs,
								null,
								"standard input");
						}
					} catch (IOException iox) {
						System.err.println(iox.getMessage());
						return;
					} catch (BadDataException bdx) {
						System.err.println(bdx.getMessage());
						return;
					}
				} else {
					try {
						Vector files = new Vector(1);
						File tempFile = null;
						if (args.length > fnameStart) {
							files.add(args[fnameStart]);
							DescriptorConfigs cfgs =
								new DescriptorConfigs(null);
							cfgs.parseConfig(
								files,
								false,
								true,
								true,
								true,
								false);
							if (filterName != null) {
								filter = getRuleHolder(cfgs, filterName);
							}
							data.parseData(
								files,
								cfgs,
								filter,
								new Equivalencies(),
								false,
								true,
								true,
								true,
								false);
							for (int i = fnameStart + 1;
								i < args.length;
								i++) {
								DescHolder next = new DescHolder();
								files.set(0, args[i]);
								cfgs = new DescriptorConfigs(null);
								cfgs.parseConfig(
									files,
									false,
									true,
									true,
									true,
									false);
								next.parseData(
									files,
									cfgs,
									filter,
									new Equivalencies(),
									false,
									true,
									true,
									true,
									false);
								data.merge(next);
							}
						} else {
							// Parser requires a RandomAccessFile. Convert System.in into one.
							tempFile = File.createTempFile("temp", "gtf");
							byte[] input = new byte[512];
							int bytesRead = 0;
							OutputStream output =
								new FileOutputStream(tempFile);
							while ((bytesRead = System.in.read(input)) > 0) {
								output.write(input, 0, bytesRead);
							}
							output.close();
							System.err.println(
								"Creating file: " + tempFile.getAbsolutePath());
							files.add(tempFile.getAbsolutePath());
							try {
								DescriptorConfigs cfgs =
									new DescriptorConfigs(null);
								cfgs.parseConfig(
									files,
									false,
									true,
									true,
									true,
									false);
								if (filterName != null) {
									filter = getRuleHolder(cfgs, filterName);
								}
								data.parseData(
									files,
									cfgs,
									filter,
									new Equivalencies(),
									false,
									true,
									true,
									true,
									false);
							} finally {
								if (args.length == 2 && tempFile != null) {
									tempFile.delete();
								}
							}
						}
					} catch (IOException iox) {
						System.err.println(
							"Error while converting standard input to a file: "
								+ iox.getMessage());
						System.exit(-2);
					}
				}
			} catch (IllegalArgumentException iax) {
				iax.printStackTrace();
				System.err.println(iax.getMessage());
				System.err.println(
					"Please modify your input files accordingly.");
			}
			Converter con = new Converter(data, null);

			if (newDimensions != null) {
				con.setDimensions(newDimensions);
			}
			if (split) {
				con.split();
			}
			if (clip) {
				con.clip();
			}
			if (isCropping) {
				con.cropFrames(crop);
			}
			if (shift != 0) {
				con.shift(shift);
			}
			if (deform == true) {
				con.deform();
			}

			PrintWriter out = new PrintWriter(System.out);
			if (outXml) {
				con.writeXml(out, true);
			} else {
				con.writeGtf(out);
			}
			out.close();
		} catch (NoClassDefFoundError ncdfe) {
			System.err.println(
				"Error in configuration: Cannot find java class '"
					+ ncdfe
					+ "'");
			System.err.println(
				"This is most likely due to a missing Java archive");
			System.err.println(
				"file in your CLASSPATH variable.  To fix this,");
			System.err.println(
				"make sure to run 'perl config.pl' (or 'cscript ");
			System.err.println(
				"config.js' on Windows) from your viper directory.");
			System.err.println(
				"On Unix, you will have to source 'viper.config'");
			System.err.println("before trying to run the software again.");
		}
	}

	/**
	 * Create a new converter object for the given set of descriptor lists.
	 *
	 * @param descriptors The data to manipulate and write out.
	 * @param props the java properties to use while construction the 
	 * converter
	 */
	public Converter(DescriptorData descriptors, Properties props) {
		data = descriptors;
		if (props != null) {
			oddsDropDescriptor =
				helpGetProperty(props, "descriptor.drop", oddsDropDescriptor);
			oddsSplitDescriptor =
				helpGetProperty(props, "descriptor.split", oddsSplitDescriptor);
			oddsSplitBBox =
				helpGetProperty(props, "attribute.bbox.split", oddsSplitBBox);
			oddsShrinkShape =
				helpGetProperty(
					props,
					"attribute.shape.shrink",
					oddsShrinkShape);
			oddsGrowShape =
				helpGetProperty(props, "attribute.shape.grow", oddsGrowShape);
		}
	}

	/**
	 * Writes the current data in the ViPER XML format.
	 *
	 * @param out The output stream.
	 * @param header <code>true</code> will include the xml header string.
	 * @throws IOException
	 */
	public void writeXml(PrintWriter out, boolean header) throws IOException {
		DOMImplementation domI = DOMImplementationImpl.getDOMImplementation();
		DocumentType dtd =
			domI.createDocumentType(
				"viper",
				"viper",
				DescriptorData.NAMESPACE_URI);
		Document root =
			domI.createDocument(DescriptorData.NAMESPACE_URI, "viper", dtd);

		OutputFormat format = new OutputFormat();
		format.setOmitXMLDeclaration(!header);
		format.setIndenting(true);
		new XMLSerializer(out, format).serialize(data.getXMLFormat(root));
	}

	/**
	 * Writes the current data in the ViPER GTF format
	 *
	 * @param out The output stream.
	 */
	public void writeGtf(PrintWriter out) {
		out.println(data);
	}

	private DescriptorData data;

	/**
	 * Modifies the dimension of all the input canonical
	 * file descriptors.
	 * @param dims the new width and height (in that order)
	 */
	public void setDimensions(int[] dims) {
		for (Iterator fnames = data.getFileNames(); fnames.hasNext();) {
			String currFile = (String) fnames.next();
			data.getForFile(currFile).getFileInformation().setDimensions(
				dims[0],
				dims[1]);
		}
	}

	/**
	 * Splits descriptors into contiguous objects.
	 * <br />
	 * <em>N.B.</em> This will reset the ids of the objects.
	 */
	public void split() {
		for (Iterator fnames = data.getFileNames(); fnames.hasNext();) {
			String fname = (String) fnames.next();
			DescriptorList dl = data.getForFile(fname);
			List fragments = new LinkedList();
			boolean hasASplit = false;
			for (Iterator descObjects = dl.getAllDescriptors().iterator();
				descObjects.hasNext();
				) {
				DescSingle curr = (DescSingle) descObjects.next();
				FrameSpan currSpan = curr.getBrokenFrameSpan();
				if (currSpan.size() > 0) {
					fragments.add(curr);
					if (!currSpan.isContiguous()) {
						hasASplit = true;
						Iterator spans = currSpan.split().iterator();
						DescSingle copy = (DescSingle) curr.clone();
						FrameSpan nextSpan = (FrameSpan) spans.next();
						curr.setFrameSpan(nextSpan);
						curr = copy;
						while (spans.hasNext()) {
							copy = (DescSingle) curr.clone();
							nextSpan = (FrameSpan) spans.next();
							copy.setFrameSpan(nextSpan);
							fragments.add(copy);
						}
					}
				} else {
					hasASplit = true;
					System.err.println(
						"Encountered descriptor with no data; removing");
				}
			}
			if (hasASplit) {
				dl.clear();
				dl.addAll(fragments);
			}
		}
		data.resetIds();
	}

	/**
	 * Sets each descriptor to its first frame. Useful for distributing
	 * first frame ground truth data.
	 */
	public void clip() {
		for (Iterator fnames = data.getFileNames(); fnames.hasNext();) {
			String fname = (String) fnames.next();
			DescriptorList dl = data.getForFile(fname);
			for (Iterator descObjects = dl.iterator();
				descObjects.hasNext();
				) {
				Descriptor curr = (Descriptor) descObjects.next();
				FrameSpan currSpan = curr.getFrameSpan();
				curr.setFrameSpan(
					new FrameSpan(currSpan.beginning(), currSpan.beginning()));
			}
		}
	}

	/**
	 * Shifts each framespan by n.
	 * @param n the number of frames to shift by
	 */
	public void shift(int n) {
		for (Iterator fnames = data.getFileNames(); fnames.hasNext();) {
			String fname = (String) fnames.next();
			DescriptorList dl = data.getForFile(fname);
			for (Iterator descObjects = dl.iterator();
				descObjects.hasNext();
				) {
				((Descriptor) descObjects.next()).moveFrame(n);
			}
		}
	}

	/**
	 * Crops all frames >= n
	 * @param n one after the last frame in the output
	 */
	public void cropFrames(int n) {
		FrameSpan cropper = new FrameSpan(0, n - 1);
		for (Iterator fnames = data.getFileNames(); fnames.hasNext();) {
			String fname = (String) fnames.next();
			DescriptorList dl = data.getForFile(fname);
			List fragments = new LinkedList();
			for (Iterator descObjects = dl.getAllDescriptors().iterator();
				descObjects.hasNext();
				) {
				Descriptor curr = (Descriptor) descObjects.next();
				FrameSpan currSpan = curr.getFrameSpan();
				if (currSpan.beginning() < n) {
					curr.setFrameSpan(currSpan.intersect(cropper));
					fragments.add(curr);
				}
			}
			dl.clear();
			dl.addAll(fragments);
		}
	}

	private static Document getRoot(InputSource input)
		throws BadDataException, IOException {
		DOMParser parser = new DOMParser();
		try {
			parser.setFeature(
				"http://apache.org/xml/features/validation/schema",
				false);
		} catch (SAXException snrx) {
			// parser does not support schemata
		}
		try {
			parser.parse(input);
		} catch (SAXException saxx) {
			String fname = input.getPublicId();
			if (fname == null) {
				throw new BadDataException(
					"Problem in parsing XML: " + saxx.getMessage());
			} else {
				throw new BadDataException(
					"Problem in parsing " + fname + ": " + saxx.getMessage());
			}
		}
		return parser.getDocument();
	}

	/**
	 * Gets the descriptor schema described in the give viper xml file. 
	 * @param root the viper xml data
	 * @return the configs, if any, found within
	 * @throws BadDataException if there is an error in the configs,
	 * or no configs node.
	 */
	public static DescriptorConfigs getConfigs(Document root)
		throws BadDataException {
		DescriptorConfigs gtCfgs = new DescriptorConfigs(null);
		NodeList nodes =
			root.getElementsByTagNameNS(DescriptorData.NAMESPACE_URI, "config");
		if (nodes.getLength() < 1) {
			throw new BadDataException("config element missing.");
		} else if (nodes.getLength() > 1) {
			throw new BadDataException("Too many config elements.");
		}
		gtCfgs.addDesConfig((Element) nodes.item(0));
		return gtCfgs;
	}

	/**
	 * Parses in the given rules file.
	 * @param gtCfgs the associated descriptor schema
	 * @param filterName the name of the filter file
	 * @return the filter described in the file
	 * @throws IOException if there is an error while reading
	 * from the given file, or if the file does not exist
	 */
	public static RuleHolder getRuleHolder(
		DescriptorConfigs gtCfgs,
		String filterName)
		throws IOException {
		return new LimitationsParser().parseFile(filterName, "FILTER", gtCfgs);
	}

	/**
	 * Used with the deform option, this is the probability of 
	 * dropping a descriptor.
	 */
	public static final double DROP_DESCRIPTOR = .01;

	/**
	 * Used with the deform option, this is the probability of 
	 * splitting a descriptor over time.
	 */
	public static final double SPLIT_DESCRIPTOR = .1;

	/**
	 * Used with the deform option, this is the probability of 
	 * splitting any bounding box.
	 */
	public static final double SPLIT_BBOX = .05;

	/**
	 * Used with the deform option, this is the probability of 
	 * increasing the size of a shape attribute.
	 */
	public static final double GROW_SHAPE = .3;

	/**
	 * Used with the deform option, this is the probability of 
	 * shrinking a shape attribute.
	 */
	public static final double SHRINK_SHAPE = .1;

	/**
	 * Takes in a list of properties to set various
	 * probabilities of deformations occuring.
	 * So far, these include:
	 * <dl>
	 *   <dt>descriptor.drop</dt>
	 *      <dd>Odds that a descriptor will be dropped</dd>
	 *   <dt>descriptor.split</dt>
	 *      <dd>Odds that a descriptor will be split frame-wise.
	 *          A gap sometimes occurs.</dd>
	 *   <dt>attribute.bbox.split</dt>
	 *      <dd>breaks the descriptor that has the bbox.</dd>
	 *   <dt>attribute.shape.shrink</dt>
	 *      <dd></dd>
	 *   <dt>attribute.shape.grow</dt>
	 *      <dd></dd>
	 *   <dt>attribute.polygon.jitter</dt>
	 *      <dd></dd>
	 *   <dt>attribute.obox.rotate</dt>
	 *      <dd></dd>
	 * </dl>
	 * @param props the properties to list
	 * @param name the double valued property to get
	 * @param value the default value
	 * @return the value of the property
	 */
	private static final double helpGetProperty(
		Properties props,
		String name,
		double value) {
		return Double.parseDouble(
			props.getProperty(name, Double.toString(value)));
	}

	private double oddsDropDescriptor = Converter.DROP_DESCRIPTOR;
	private double oddsSplitDescriptor = Converter.SPLIT_DESCRIPTOR;
	private double oddsSplitBBox = Converter.SPLIT_BBOX;
	private double oddsShrinkShape = Converter.SHRINK_SHAPE;
	private double oddsGrowShape = Converter.GROW_SHAPE;

	/**
	 * Deforms the data.
	 */
	public void deform() {
		for (Iterator fnames = data.getFileNames(); fnames.hasNext();) {
			String fname = (String) fnames.next();
			DescriptorList dl = data.getForFile(fname);
			for (Iterator descObjects = dl.iterator();
				descObjects.hasNext();
				) {
				Descriptor curr = (Descriptor) descObjects.next();
				FrameSpan currSpan = curr.getFrameSpan();
				curr.setFrameSpan(
					new FrameSpan(currSpan.beginning(), currSpan.beginning()));
			}
		}
	}

	/**
	 * Deformer works by applying list-level, descriptor-level,
	 * and attribute-level deformations: the list level ones basically
	 * consist of dropping random descriptors. I suppose one day I
	 * could figure out how to make up new ones at random...
	 * @param data the data to deform
	 */
	protected void deformList(DescriptorList data) {
		List nl = new LinkedList();
		for (Iterator iter = data.iterator(); iter.hasNext();) {
			DescSingle currDesc = (DescSingle) iter.next();
			if (oddsDropDescriptor > Math.random()) {
			} else {
				nl.addAll(deformDescriptor((DescSingle) currDesc.clone()));
			}
		}
		data.clear();
		data.addAll(nl);
	}

	/**
	 * Note: These operate directly on the object, so make sure to pass
	 * a clone if you want to keep it. This one splits by
	 * framespan, if possible, and then passes it to applyToAttributes.
	 * @param data the descriptor to deform
	 * @return a list of descriptors generated by applying
	 * the deformations to the specified Descriptor
	 */
	private List deformDescriptor(DescSingle data) {
		FrameSpan frames = data.getFrameSpan();
		if ((data.getCategory() != "FILE")
			&& (frames.size() > 1)
			&& (Math.random() < oddsSplitDescriptor)) {
			// Split the descriptor into multiple descriptors
			int cutFrame =
				frames.beginning()
					+ (int) Math.round(Math.random() * frames.size());
			if (cutFrame == frames.beginning()
				|| cutFrame == frames.ending()) {
				return deformAttributes(data);
			} else {
				String S = data.toString();
				int i = S.indexOf("\n");
				System.err.println(
					"-- Splitting "
						+ S.substring(0, i)
						+ " at frame "
						+ cutFrame);

				List deformed = new LinkedList();
				if (Math.random() >= oddsDropDescriptor) {
					DescSingle firstHalf = (DescSingle) data.clone();
					firstHalf.setFrameSpan(
						new FrameSpan(frames.beginning(), cutFrame - 1));
					deformed.addAll(deformAttributes(firstHalf));
				}
				if (Math.random() >= oddsDropDescriptor) {
					DescSingle secondHalf = data;
					secondHalf.setFrameSpan(
						new FrameSpan(cutFrame, frames.ending()));
					deformed.addAll(deformAttributes(secondHalf));
				}
				return deformed;
			}
		} else {
			return deformAttributes(data);
		}
	}

	/**
	 * Apply the deformations to the attributes. 
	 * Currently not implemented.
	 * @param data the descriptor to deform
	 * @return the descriptors generated by deforming
	 * the attributes
	 */
	private List deformAttributes(DescSingle data) {
		List deformed = new LinkedList();
		deformed.add(data);

		return deformed;
	}

}