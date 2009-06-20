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

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.descriptors.attributes.*;

import com.jrefinery.data.*;

import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * A framewise evaluation views each frame individually, computing aggregate
 * statisitcs on a frame-by-frame or even pixel-by-pixel level, disregarding the
 * continuity of descriptors. This is great for computing things like pixel and
 * frame counts.
 */
public class FramewiseEvaluation implements Evaluation {
	private static final class DescRules extends Triple {
		private static String[] getKeys(Map attrMap) {
			String[] names = new String[attrMap.size()];
			int i = 0;
			for (Iterator iter = attrMap.keySet().iterator(); iter.hasNext(); i++) {
				names[i] = (String) iter.next();
			}
			return names;
		}
		private static FramewiseEvaluation.FrameMeasure[] getValues(Map attrMap) {
			FramewiseEvaluation.FrameMeasure[] m = new FramewiseEvaluation.FrameMeasure[attrMap
					.size()];
			int i = 0;
			for (Iterator iter = attrMap.values().iterator(); iter.hasNext(); i++) {
				m[i] = (FramewiseEvaluation.FrameMeasure) iter.next();
			}
			return m;
		}
		DescRules(Descriptor d, Map attrMap) {
			super(d, getKeys(attrMap), getValues(attrMap));
		}
		DescRules(Descriptor d, String[] attrs,
				FramewiseEvaluation.FrameMeasure[] m) {
			super(d, attrs, m);
		}
		Descriptor getDesc() {
			return (Descriptor) getFirst();
		}
		int getLength() {
			return ((String[]) getSecond()).length;
		}
		String getAttr(int i) {
			return ((String[]) getSecond())[i];
		}
		FramewiseEvaluation.FrameMeasure getMeasure(int i) {
			return ((FramewiseEvaluation.FrameMeasure[]) getThird())[i];
		}
	}

	private Map descriptors = new HashMap();
	private EvaluationParameters.ScopeRules scope;
	private DescRules[] metrics = new DescRules[0];
	private int numberOfMetrics = 0;

	/**
	 * Constructs a new framewise evaluation from the given parameters
	 * 
	 * @param epf
	 *            the evaluation parameters
	 */
	public FramewiseEvaluation(EvaluationParameters epf) {
		scope = epf.getScopeRulesFor(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void printMetricsTo(PrintWriter out) {
		out.println();
		for (int i = 0; i < metrics.length; i++) {
			out.println(metrics[i].getDesc().getFullName());
			for (int j = 0; j < metrics[i].getLength(); j++) {
				out.println("    " + metrics[i].getAttr(j) + " : "
						+ metrics[i].getMeasure(j));
			}
			out.println();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void printRawMetricsTo(PrintWriter raw) {
		printMetricsTo(raw);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	/**
	 * {@inheritDoc}
	 */
	public PrintWriter getOutput() {
		return output;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRaw(PrintWriter raw) {
		this.raw = raw;
	}

	PrintWriter getRaw() {
		return raw;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrintingHeaders(boolean on) {
		this.printHeaders = on;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPrintingHeaders() {
		return this.printHeaders;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTicker(Revealer ticker) {
		this.ticker = ticker;
	}
	Revealer getTicker() {
		return ticker;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMatrix(CompMatrix cm) {
		this.mat = cm;
	}

	/**
	 * Gets the comparisons associated with this evaluation.
	 * 
	 * @return the matrix of target to candidate comparisons
	 */
	CompMatrix getMatrix() {
		return this.mat;
	}

	private PrintWriter output;
	private PrintWriter raw;
	private boolean printHeaders;
	private Revealer ticker;
	private CompMatrix mat;

	/**
	 * Gets the number of elements in the ticker.
	 * 
	 * @return the maximum size of the ticker.
	 */
	public int getTickerSize() {
		return 1 + Math.max(mat.T.getHighestFrame(), mat.C.getHighestFrame());
	}

	/**
	 * Combine a descriptor with another that might be null. Since this is
	 * happening in pixelwise evaluation, assume that the data allows comparison
	 * and is well formed.
	 * 
	 * @param single
	 *            the single one to add
	 * @param soFar
	 *            the aggreate
	 * @param scope
	 *            the scoping rules
	 * @return the combined descriptor
	 */
	private Descriptor helpCombine(Descriptor single, Descriptor soFar,
			EvaluationParameters.ScopeRules scope) {
		try {
			if (soFar == null) {
				return single;
			} else if (soFar instanceof DescSingle) {
				soFar = new DescAggregate((DescSingle) soFar);
			}
			try {
				soFar = soFar.compose(single, scope);
				return soFar;
			} catch (UncomposableException ux) {
				System.err
						.println("Error: Evaluation must not be set for pixel evaluation:\n"
								+ "\t" + ux.getMessage());
				return soFar;
			}
		} catch (BadDataException bdx) {
			throw new IllegalStateException(
					"Bad Data Exception while converting to aggregate:\n"
							+ bdx.getMessage());
		}
	}

	/**
	 * The basic idea is to merge the targets and candidates, and operate on the
	 * aggregate halves. However, there are reasons for complicating this
	 * somewhat. The first it object/aggregate measures - comparing a single
	 * target or candidate descriptor to the complete opposing set from the
	 * frame. The other is smart ignorance - adding don't care candidates if
	 * they match a given don't care target above a certain threshold. So the
	 * order of operations is:
	 * 
	 * <ol>
	 * <li>quadratic time search for smart ignorance</li>
	 * <li>merge targets for candidate evaluation</li>
	 * <li>merge candidates for target evaluation</li>
	 * <li>use merged target/candidate sets to do overall metrics</li>
	 * </ol>
	 * 
	 * @param currSpan
	 *            one-frame FrameSpan for the current frame
	 * @return the evaluation information associated with the frame
	 */
	private FramewiseInformation getFramewiseForFrame(FrameSpan currSpan) {
		FramewiseInformation info = new FramewiseInformation();
		int targetObjectCount = 0;
		int candidateObjectCount = 0;

		for (int i = 0; i < metrics.length; i++) {
			DescPrototype currDescType = (DescPrototype) metrics[i].getDesc();

			Descriptor targetDesc = null;
			Descriptor candidateDesc = null;
			Descriptor dontCareDesc = null;

			LinkedList targs = new LinkedList();
			LinkedList cands = new LinkedList();
			LinkedList dontCares = new LinkedList();

			for (Iterator iter = mat.T.cropNodesToSpan(currSpan); iter
					.hasNext();) {
				Descriptor curr = (Descriptor) iter.next();
				if (curr.sameCategoryAs(currDescType, scope.getMap())) {
					if (scope.isOutputableTarget(curr)) {
						targetObjectCount++;
						targs.add(curr);
						targetDesc = helpCombine(curr, targetDesc, scope);
					} else {
						dontCares.add(curr);
						dontCareDesc = helpCombine(curr, dontCareDesc, mat
								.getScopeRules());
					}
				}
			}

			for (Iterator iter = mat.C.cropNodesToSpan(currSpan); iter
					.hasNext();) {
				Descriptor curr = (Descriptor) iter.next();
				if (curr.sameCategoryAs(currDescType, scope.getMap())) {
					if (scope.isOutputableCandidate(curr)) {
						boolean care = true;
						dc : for (int j = 0; j < metrics[i].getLength(); j++) {
							AttrMeasure dcMeas = metrics[i].getMeasure(j)
									.getDontCareMeasure();
							if (dcMeas != null) {
								String attrName = metrics[i].getAttr(j);
								Attribute dcCandAttr = curr.getAttribute(
										attrName, scope.getMap());
								Measurable.Difference D = null;
								for (Iterator dcIter = dontCares.iterator(); dcIter
										.hasNext();) {
									Descriptor dcTarg = (Descriptor) dcIter
											.next();
									Attribute dcTargAttr = dcTarg.getAttribute(
											attrName, scope.getMap());
									try {
										D = Distances.helpGetDiff(dcTargAttr,
												currSpan, dcCandAttr, currSpan,
												null, null, null, null,
												// no blackout or ignore
												currSpan.beginning(), mat
														.getFileInformation(),
												D);
										if (dcMeas.thresh(dcMeas.getMetric()
												.getDistance(D).doubleValue())) {
											// if some dcTarg matches curr
											// within the threshold, it should
											// be ignored
											care = false;
											break dc;
										}
									} catch (IgnoredValueException ivx) {
										// ignore these
									}
								} // for each ignored target
							} // if attribute has don't care measure
						} // for each attribute
						if (care) {
							candidateObjectCount++;
							cands.add(curr);
							candidateDesc = helpCombine(curr, candidateDesc,
									mat.getScopeRules());
						}
					} else {
						dontCareDesc = helpCombine(curr, dontCareDesc, mat
								.getScopeRules());
					}
				}
			}

			if (targetDesc == null && candidateDesc == null) {
				continue;
			}
			info.setFrameCount(1);
			for (int j = 0; j < metrics[i].getLength(); j++) {
				String attrName = metrics[i].getAttr(j);
				Measurable.Difference D = null;
				Measurable.Difference tD = null;

				AttributeValue t = helpGetAttrValue(targetDesc, attrName,
						currSpan, currSpan.beginning(), scope.getMap());
				AttributeValue c = helpGetAttrValue(candidateDesc, attrName,
						currSpan, currSpan.beginning(), scope.getMap());
				AttributeValue dc = helpGetAttrValue(dontCareDesc, attrName,
						currSpan, currSpan.beginning(), scope.getMap());

				try {
					D = Distances.helpGetDiff(t, c, null, dc, mat
							.getFileInformation(), null);
					info.setOverallsFor(currDescType, attrName, D);
					// sets matched, false, and missed
				} catch (IgnoredValueException ivx) {
					// Do nothing for now...
					System.err.println("Ignored all data in a frame.");
				}

				for (Iterator targetIter = targs.iterator(); targetIter
						.hasNext();) {
					AttributeValue tCurr = helpGetAttrValue(
							(Descriptor) targetIter.next(), attrName, currSpan,
							currSpan.beginning(), scope.getMap());
					if (tCurr != null || c != null) {
						try {
							tD = Distances.helpGetDiff(tCurr, c, null, dc, mat
									.getFileInformation(), D);
							info.addTargVCands(currDescType, attrName, tD);
							// adds fragments and recalls
						} catch (IgnoredValueException ivx) {
							// nothing matched...
						}
					}
				}

				for (Iterator candidateIter = cands.iterator(); candidateIter
						.hasNext();) {
					AttributeValue cCurr = helpGetAttrValue(
							(Descriptor) candidateIter.next(), attrName,
							currSpan, currSpan.beginning(), scope.getMap());
					if (cCurr != null || t != null) {
						try {
							tD = Distances.helpGetDiff(t, cCurr, null, dc, mat
									.getFileInformation(), D);
							info.addCandVTargs(currDescType, attrName, tD);
							// adds to precisions
						} catch (IgnoredValueException ivx) {
							// nothing matched...
						}
					}
				}
			} // for each attribute
		} // for each descriptor type
		info.setTargetObjectCount(targetObjectCount);
		info.setCandidateObjectCount(candidateObjectCount);

		return info;
	}
	private static AttributeValue helpGetAttrValue(Descriptor desc,
			String attrName, FrameSpan span, int frame, Equivalencies map) {
		if (desc == null) {
			return null;
		} else {
			Attribute temp = desc.getAttribute(attrName, map);
			return temp == null ? null : temp.getValue(span, frame);
		}
	}

	/**
	 * Count, on a frameXframe basis, the number of pixels hit/missed for all
	 * shape descriptors.
	 * 
	 * @param ticker
	 *            the ui ticker
	 * @return the information for all the frames
	 */
	private FramewiseInformation[] getFramewiseInformation(Revealer ticker) {
		int maxFrameNum = Math.max(mat.T.getHighestFrame(), mat.C
				.getHighestFrame());
		FramewiseInformation[] counts = new FramewiseInformation[++maxFrameNum];

		// Add it up!
		int i = 0;
		FrameSpan cspan = new FrameSpan(0, 0);
		try {
			for (i = 0; i < maxFrameNum; i++, cspan.shift(1)) {
				counts[i] = getFramewiseForFrame(cspan);
				if (ticker != null) {
					ticker.tick();
				}
			}
		} catch (ArithmeticException ax) {
			System.err.println("\n error while accessing frame " + i);
			throw ax;
		}
		return counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public void printHeader() {
		if (output != null) {
			output.print(StringHelp.banner("PIXEL RESULTS", 53));
		}
		if (raw != null) {
			raw.println("\n\n// " + new FramewiseInformation().getLayout()
					+ " \n#BEGIN_PIXEL_RESULTS");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void printFooter(Evaluation.Information total) {
		if (output != null) {
			output.println("Total for all frames");
			if (total == null) {
				output.println("No frames coincided");
			} else {
				output.println(total.toVerbose());
			}
			output.println();
		}
		if (raw != null) {
			if (total != null) {
				raw.println("Total " + total);
			}
			raw.println("\n#END_PIXEL_RESULTS");
		}
	}

	/**
	 * Prints out the evaluation information after a successful evaluation.
	 * 
	 * @return the sum over all of the frames.
	 */
	public Evaluation.Information printEvaluation() {
		if (printHeaders) {
			printHeader();
		}

		FramewiseInformation[] counts = getFramewiseInformation(ticker);
		FramewiseInformation sum = new FramewiseInformation();
		for (int i = 0; i < counts.length; i++) {
			sum.add(counts[i]);
			if (counts[i].hasInformation()) {
				if (output != null) {
					output.println("For frame " + i);
					output.println(counts[i].toVerbose());
					output.println();
				}
				if (raw != null) {
					raw.println(i + " " + counts[i]);
				}
			}
		}

		if (printHeaders) {
			printFooter(sum);
		}
		return sum;
	}

	/**
	 * {@inheritDoc}
	 */
	public void parseEvaluation(VReader reader, DescriptorConfigs dcfgs)
			throws IOException {
		//  Parse Each line in the EVALUATION section
		//  For each descriptor, we get the descriptor element (eg OBJECT TEXT)
		//  Set it to true if it is found
		while (!reader.currentLineIsEndDirective()) {
			CountingStringTokenizer st = new CountingStringTokenizer(reader
					.getCurrentLine());
			// The category of descriptor (ie FILE, OBJECT, etc.)
			String type = st.nextToken();
			if (!Descriptor.isCategory(type)) {
				reader.printError(type + " is not a Descriptor category.", st
						.getStart(), st.getEnd());
				reader.gotoNextRealLine();
			} else {
				int startDescCol = st.getStart();
				String name = st.nextToken();
				int endDescCol = st.getEnd();
				// The descriptor that this line refers to.
				Iterator iter = dcfgs.getNodesByType(type, name);
				if (iter.hasNext()) {
					DescPrototype relevant = (DescPrototype) iter.next();
					descriptors.put(relevant, helpParseAttribMap(reader,
							relevant));
				} else {
					reader.printError("Not a Descriptor type specified "
							+ "in the Configuration " + endDescCol,
							startDescCol, endDescCol);
					reader.gotoNextRealLine();
				}
			}
		}
		metrics = new DescRules[descriptors.size()];
		int i = 0;
		numberOfMetrics = 0;
		for (Iterator iter = descriptors.entrySet().iterator(); iter.hasNext(); i++) {
			Map.Entry curr = (Map.Entry) iter.next();
			metrics[i] = new DescRules((Descriptor) curr.getKey(), (Map) curr
					.getValue());
			for (int j = 0; j < metrics[i].getLength(); j++) {
				metrics[i].getMeasure(j).setOffset(numberOfMetrics);
				numberOfMetrics += metrics[i].getMeasure(j).getLength();
			}
		}
	}

	/**
	 * A measure for a given frame.
	 */
	public static class FrameMeasure extends AttrMeasure {
		private List childMeasures = new LinkedList();

		/**
		 * Gets the localizers (measures associated with thresholds).
		 * 
		 * @return the list of localizing measures
		 */
		public List getLocalizations() {
			return childMeasures;
		}

		/**
		 * Gets the localizers as an iterator.
		 * 
		 * @return the localizing measures
		 */
		public Iterator getLocalizers() {
			return childMeasures.iterator();
		}

		private int offset = 0;

		/**
		 * Gets the frame offset to evaluate
		 * 
		 * @return the offset into the video where the measure is applied
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * Sets the frame offset.
		 * 
		 * @param o
		 *            the new frame offset.
		 */
		public void setOffset(int o) {
			offset = o;
		}

		/**
		 * Gets the total count of metrics and localizers.
		 * 
		 * @return the total number of measures
		 */
		public int getLength() {
			return childMeasures.size() + distances.size();
		}

		private AttrMeasure dcMeas = null;

		/**
		 * Gets the don't care measure.
		 * 
		 * @return the don't care measure
		 */
		public AttrMeasure getDontCareMeasure() {
			return dcMeas;
		}

		private List distances = new LinkedList();

		/**
		 * Gets all the distance functions.
		 * 
		 * @return the distance functions
		 */
		public List getDistanceFunctors() {
			return distances;
		}

		/**
		 * Gets distane functions that are not symmetric.
		 * 
		 * @return all distance functions where the distance from candidate to
		 *         target is not the same as the distance from target to
		 *         candidate
		 */
		public Iterator getAsymetricDistances() {
			return new ExceptIterator(OA, distances.iterator());
		}
		private static final OnlyAsymetrics OA = new OnlyAsymetrics();
		private static class OnlyAsymetrics
				implements
					ExceptIterator.ExceptFunctor {
			/**
			 * Checks to see that the object is an assymetric attribute distance
			 * function object.
			 * 
			 * @return {@inheritDoc}
			 */
			public boolean check(Object o) {
				int type = ((Distance) o).getType();
				return type == Distance.CAND_V_TARGS
						|| type == Distance.TARG_V_CANDS;
			}
		}

		/**
		 * Gets the raw formatted information about localizers and metrics.
		 * {@inheritDoc}
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (Iterator overalls = getDistanceFunctors().iterator(); overalls
					.hasNext();) {
				sb.append(overalls.next()).append(" ");
			}
			for (Iterator locals = getLocalizers(); locals.hasNext();) {
				sb.append("[").append(locals.next().toString()).append("] ");
			}
			if (dcMeas != null) {
				sb.append("<").append(dcMeas.toString()).append(">");
			}
			return sb.toString();
		}

		FrameMeasure(String attr, StringTokenizer st, ErrorWriter err)
				throws ImproperMetricException {
			super(attr);
			String S = "";
			try {
				while (st.hasMoreTokens()) {
					try {
						S = st.nextToken();
						if (S.equals("]") || S.equals(">")) {
							if (st.hasMoreTokens()) {
								S = st.nextToken();
							} else {
								break;
							}
						}
						if (S.charAt(0) == '[') {
							AttrMeasure tempMeas = new AttrMeasure(attr);
							if (S.length() == 1) {
								S = st.nextToken();
							} else {
								S = S.substring(1);
							}
							if (S.charAt(S.length() - 1) == ']') {
								S = S.substring(0, S.length() - 1);
							}

							if (S.length() > 0) {
								if ((Character.isDigit(S.charAt(0)))
										|| ((S.length() > 1)
												&& (S.charAt(0) == '.') && (Character
												.isDigit(S.charAt(1))))) {
									tempMeas
											.setTolerance(Double.parseDouble(S));
								} else if (!"-".equals(S)) {
									tempMeas.setMetric(Distances
											.getDistanceFunctor(attr, S));
								}
							}
							S = st.nextToken();
							if (S.charAt(S.length() - 1) == ']') {
								S = S.substring(0, S.length() - 1);
							}

							if (S.length() > 0) {
								if (!S.equals("-")) {
									tempMeas
											.setTolerance(Double.parseDouble(S));
								}
							}
							childMeasures.add(tempMeas);
						} else if (S.charAt(0) == '<') {
							dcMeas = new AttrMeasure(attr);
							if (S.length() == 1) {
								S = st.nextToken();
							} else {
								S = S.substring(1);
							}
							if (S.charAt(S.length() - 1) == '>') {
								S = S.substring(0, S.length() - 1);
							}

							if (S.length() > 0) {
								if ((Character.isDigit(S.charAt(0)))
										|| ((S.length() > 1)
												&& (S.charAt(0) == '.') && (Character
												.isDigit(S.charAt(1))))) {
									dcMeas.setTolerance(Double.parseDouble(S));
								} else if (!"-".equals(S)) {
									dcMeas.setMetric(Distances
											.getDistanceFunctor(attr, S));
								}
							}
							S = st.nextToken();
							if (S.charAt(S.length() - 1) == '>') {
								S = S.substring(0, S.length() - 1);
							}

							if (S.length() > 0) {
								if (!S.equals("-")) {
									dcMeas.setTolerance(Double.parseDouble(S));
								}
							}
						} else {
							distances
									.add(Distances.getDistanceFunctor(attr, S));
						}
					} catch (UnknownDistanceException udx) {
						err.printError(udx.getMessage());
						if (S.startsWith("<") || S.startsWith("]")) {
							while (!(S.endsWith(">") || S.endsWith("]"))) {
								S = st.nextToken();
							}
						}
					}
				} // while st.hasMoreTokens
			} catch (NoSuchElementException nsex) {
				setMetric(null);
				setTolerance(Double.NaN);
				err.printError("Error in format of measure list");
			}
		}
	}

	/**
	 * Get attrib map - maps attrib names to lists of measures. Also gets one
	 * AttrMeasure, used for excess evaluation. Has the form:
	 * 
	 * <pre>
	 * 
	 *    OBJECT DescType [framespanMetric threshold] 
	 *         Attr1 : metric1 metric2 [stdmet threshold]
	 *         Attr2 : metricA // will be ignored during excess evaluation
	 *  
	 * </pre>
	 * 
	 * @param reader
	 *            the reader to parse from
	 * @param proto
	 *            the descriptor type to parse using
	 * @return the attribute map
	 * @throws IOException
	 * @throws NoSuchElementException
	 */
	private Map helpParseAttribMap(VReader reader, DescPrototype proto)
			throws IOException {
		HashMap attribMap = new HashMap();

		StringTokenizer st = new StringTokenizer(reader.getCurrentLine());
		st.nextToken();
		st.nextToken(); // no framespan measure for
		if (st.hasMoreTokens()) {
			reader.printWarning("Ignored extraneous info at end of line");
		}

		reader.gotoNextRealLine();

		while (!Descriptor.startsWithCategory(reader.getCurrentLine())
				&& !reader.currentLineIsEndDirective()) {
			st = new StringTokenizer(reader.getCurrentLine());
			String attribName = st.nextToken();
			AttributePrototype curr = proto.getAttributePrototype(attribName);
			if (st.hasMoreTokens()) {
				try {
					if (!st.nextToken().equals(":")) {
						reader.printError("Improper placement of colon");
					} else {
						attribMap.put(attribName, new FrameMeasure(curr
								.getLocalType(), st, reader));
						if (st.hasMoreTokens()) {
							throw (new BadDataException(
									"Unparsed data at end of line"));
						}
					}
				} catch (BadDataException bdx) {
					reader.printWarning(bdx.getMessage());
				} catch (ImproperMetricException imx) {
					reader.printWarning(imx.getMessage());
				}
			} else {
				attribMap.put(attribName, new AttrMeasure(curr.getLocalType()));
			}
			reader.gotoNextRealLine();
		}
		return attribMap;
	}

	/**
	 * Return a map of DescPrototypes to their evaluations. {@inheritDoc}
	 */
	public Map getMeasureMap() {
		return descriptors;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return "Framewise Evaluation"
	 */
	public String getName() {
		return "Framewise Evaluation";
	}

	private class FramewiseInformation implements Evaluation.Information {
		private int truthObjects = 0;
		private int resultObjects = 0;
		private int frameCount = 0;
		private int detectedFrames = 0;
		private int missedFrames = 0;
		private int falseFrames = 0;

		private Object[] values = new Object[numberOfMetrics];

		/**
		 * {@inheritDoc}
		 */
		public boolean hasInformation() {
			return frameCount > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public String toVerbose() {
			return toString(true);
		}

		/**
		 * Gets a string representation of the evaluation results.
		 * 
		 * @param verbose
		 *            set this to get human readable output. Otherwise, it gets
		 *            the data in raw format.
		 * @return the results of the evaluation
		 */
		public String toString(boolean verbose) {
			StringBuffer sb = new StringBuffer();
			if (verbose)
				sb.append("Detection Accuracy: ");
			sb.append(getObjectCountAccuracy());

			// These are listed as tracking metrics in the penn state document,
			if (verbose)
				sb.append("\nObject Count Recall:");
			sb.append(" ").append(getObjectCountRecall());

			if (verbose)
				sb.append("\nObject Count Precision:");
			sb.append(" ").append(getObjectCountPrecision());
			if (verbose)
				sb.append("\n");

			int valOffset = 0;
			for (int i = 0; i < metrics.length; i++) {
				for (int j = 0; j < metrics[i].getLength(); j++) {
					FrameMeasure curr = metrics[i].getMeasure(j);
					for (Iterator distIter = curr.getDistanceFunctors()
							.iterator(); distIter.hasNext();) {
						Distance dist = (Distance) distIter.next();
						Object value = values[valOffset++];
						if (verbose)
							sb.append(dist.getExplanation()).append(": ");
						else
							sb.append(" ");
						if (value == null) {
							sb.append("undefined");
							if (verbose)
								sb.append("\n");
							continue;
						}
						switch (dist.getType()) {
							case Distance.OVERALL_SUM :
								sb.append(value.toString());
								break;
							case Distance.OVERALL_MEAN :
								sb.append(((Number) value).doubleValue()
										/ frameCount);
								break;
							case Distance.BALANCED :
							case Distance.TARG_V_CANDS :
							case Distance.CAND_V_TARGS :
								sb
										.append(getAverageOfListOfNumbers((List) value));
								break;
						}
						if (verbose)
							sb.append("\n");
					}
					for (Iterator locIter = curr.getLocalizers(); locIter
							.hasNext();) {
						AttrMeasure meas = (AttrMeasure) locIter.next();
						PartialSum value = (PartialSum) values[valOffset++];
						if (verbose)
							sb.append("Localized ").append(
									meas.getMetric().getExplanation()).append(
									": ");
						else
							sb.append(" ");
						if (value == null) {
							sb.append("undefined");
						} else {
							sb.append(value);
						}
						if (verbose)
							sb.append("\n");
					}
				}
			}
			return sb.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return toString(false);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getLayout() {
			// object acc, recall, precision -> always
			// pixels matched/missed/false -> overall
			// avgObjRecall/precision -> {targ v cand, cand v targ} averages
			// localizations -> {targ v cand, cand v targ} localizations
			StringBuffer sb = new StringBuffer();
			sb.append("OBJ_COUNT_ACC OBJ_COUNT_RECALL OBJ_COUNT_PRECISION");

			for (int i = 0; i < metrics.length; i++) {
				for (int j = 0; j < metrics[i].getLength(); j++) {
					FrameMeasure curr = metrics[i].getMeasure(j);
					for (Iterator distIter = curr.getDistanceFunctors()
							.iterator(); distIter.hasNext();) {
						Distance dist = (Distance) distIter.next();
						sb.append(" ").append(dist);
					}
					for (Iterator locIter = curr.getLocalizers(); locIter
							.hasNext();) {
						AttrMeasure meas = (AttrMeasure) locIter.next();
						sb.append(" ").append(meas);
					}
				}
			}
			return sb.toString();
		}

		/**
		 * Gets the accuracy of the object counts,
		 * based on number of objects in each frame.
		 * @return the object count accuracy
		 */
		public String getObjectCountAccuracy() {
			if (truthObjects + resultObjects == 0) {
				return "undefined";
			} else {
				return String.valueOf(2.0
						* Math.min(truthObjects, resultObjects)
						/ (truthObjects + resultObjects));
			}
		}

		/**
		 * Gets the precision of the object counts,
		 * based on number of objects in each frame.
		 * @return the object count precision
		 */
		public String getObjectCountPrecision() {
			if (resultObjects == 0) {
				return "undefined";
			} else if (truthObjects < resultObjects) {
				return String.valueOf((double) truthObjects / resultObjects);
			} else {
				return "1";
			}
		}

		/**
		 * Gets the recall of the object counts,
		 * based on number of objects in each frame.
		 * @return the object count recall
		 */
		public String getObjectCountRecall() {
			if (truthObjects == 0) {
				return "undefined";
			} else if (resultObjects < truthObjects) {
				return String.valueOf((double) resultObjects / truthObjects);
			} else {
				return "1";
			}
		}

		private void checkLevels() {
			if (detectedFrames + missedFrames + falseFrames <= 0
					&& truthObjects + resultObjects > 0) {
				if (truthObjects > 0 && resultObjects > 0) {
					detectedFrames += frameCount;
				} else if (truthObjects > 0) {
					missedFrames += frameCount;
				} else {
					falseFrames += frameCount;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void add(Evaluation.Information other) {
			FramewiseInformation pci = (FramewiseInformation) other;

			pci.checkLevels();
			this.checkLevels();

			frameCount += pci.frameCount;
			truthObjects += pci.truthObjects;
			resultObjects += pci.resultObjects;

			for (int i = 0; i < values.length; i++) {
				if (pci.values[i] == null) {
					// nothing to add
				} else if (values[i] == null) {
					values[i] = pci.values[i];
				} else {
					if (values[i] instanceof Number) {
						values[i] = new Double(((Number) values[i])
								.doubleValue()
								+ ((Number) pci.values[i]).doubleValue());
					} else if (values[i] instanceof PartialSum) {
						((PartialSum) values[i])
								.add((PartialSum) pci.values[i]);
					} else {
						((List) values[i]).addAll((List) pci.values[i]);
					}
				}
			}
		}

		/**
		 * Calculates all of the OVERALL_SUM or OVERALL_MEAN metrics for a
		 * specific frame. Call this once, using the Measurable.Difference
		 * between the aggregates of the entire target and candidate frames.
		 * 
		 * @param desc
		 *            The (target) descriptor to compare.
		 * @param attr
		 *            String name of the attribute
		 * @param D
		 *            the Measurable.Difference between all the non-ignored
		 *            candidates and non-ignored targets in a frame for the
		 *            given attribute
		 */
		public void setOverallsFor(Descriptor desc, String attr,
				Measurable.Difference D) {
			FrameMeasure meas = (FrameMeasure) scope.getMeasure(desc, attr);
			int offset = meas.getOffset();
			for (Iterator iter = meas.getDistanceFunctors().iterator(); iter
					.hasNext(); offset++) {
				Distance dist = (Distance) iter.next();
				if (dist.getType() == Distance.OVERALL_MEAN
						|| dist.getType() == Distance.OVERALL_SUM) {
					values[offset] = dist.getDistance(D);
				}
			}
		}

		/**
		 * Computes the distances for a single target versus the aggregation of
		 * all the candidates in a given frame. This includes all distances
		 * marked as TARG_V_CANDS and BALANCED. Run this for each target in a
		 * given frame.
		 * 
		 * @param desc
		 *            The (target) descriptor to compare.
		 * @param attr
		 *            String name of the attribute
		 * @param D
		 *            the Measurable.Difference between all the non-ignored
		 *            candidates in a frame and a single target
		 */
		public void addTargVCands(Descriptor desc, String attr,
				Measurable.Difference D) {
			addTypeOrBalanced(desc, attr, D, Distance.TARG_V_CANDS);
		}

		/**
		 * Computes the distances for a single candidate versus the aggregation
		 * of all the targets in a given frame. This includes all distances
		 * marked as CAND_V_TARGS and BALANCED. Run this for each candidate in a
		 * given frame.
		 * 
		 * @param desc
		 *            The (target) descriptor to compare.
		 * @param attr
		 *            String name of the attribute
		 * @param D
		 *            the Measurable.Difference between all the non-ignored
		 *            targets in a frame and a single candidate descriptor.
		 */
		public void addCandVTargs(Descriptor desc, String attr,
				Measurable.Difference D) {
			addTypeOrBalanced(desc, attr, D, Distance.CAND_V_TARGS);
		}

		private void addTypeOrBalanced(Descriptor desc, String attr,
				Measurable.Difference D, int type) {
			FrameMeasure meas = (FrameMeasure) scope.getMeasure(desc, attr);
			int offset = meas.getOffset();
			for (Iterator iter = meas.getDistanceFunctors().iterator(); iter
					.hasNext(); offset++) {
				Distance dist = (Distance) iter.next();
				if (dist.getType() == type
						|| dist.getType() == Distance.BALANCED) {
					List L = (List) values[offset];
					Number value = dist.getDistance(D);
					if (value.doubleValue() != Double.NaN) {
						if (L != null) {
							L.add(value);
						} else {
							L = new LinkedList();
							L.add(value);
							values[offset] = L;
						}
					}
				}
			}

			for (Iterator iter = meas.getLocalizers(); iter.hasNext(); offset++) {
				AttrMeasure curr = (AttrMeasure) iter.next();
				if (curr.getMetric().getType() == type) {
					PartialSum val = (PartialSum) values[offset];
					if (val == null) {
						val = new PartialSum();
					}
					Number num = curr.getMetric().getDistance(D);
					if ((num instanceof Double && ((Double) num).isNaN())
							|| (num instanceof Float && ((Float) num).isNaN())) {
						continue;
					}
					double N = num.doubleValue();
					if (curr.thresh(N)) {
						val.add(1, 1);
					} else {
						val.add(0, 1);
					}
					values[offset] = val;
				}
			}
		}

		/**
		 * Sets the number of target objects.
		 * @param i the count of target objects
		 */
		public void setTargetObjectCount(int i) {
			truthObjects = i;
		}

		/**
		 * Sets the number of candidate objects.
		 * @param i the count of candidate objects
		 */
		public void setCandidateObjectCount(int i) {
			resultObjects = i;
		}

		/**
		 * Sets the number of frames in the video.
		 * @param i the number of frames
		 */
		public void setFrameCount(int i) {
			frameCount = i;
		}

		/**
		 * {@inheritDoc}
		 */
		public Map getDatasets(String name) {
			HashMap data = new HashMap();

			String[] seriesName = new String[]{name};

			double[][] dimf = new double[][]{{detectedFrames,
					frameCount - (detectedFrames + missedFrames + falseFrames),
					missedFrames, falseFrames}};
			DefaultCategoryDataset frameMeasure = new DefaultCategoryDataset(
					dimf);
			frameMeasure.setSeriesNames(seriesName);
			frameMeasure.setCategories(frameMeasureNames);
			data.put("Frame Measure", frameMeasure);

			int valOffset = 0;
			for (int i = 0; i < metrics.length; i++) {
				for (int j = 0; j < metrics[i].getLength(); j++) {
					FrameMeasure curr = metrics[i].getMeasure(j);
					for (Iterator distIter = curr.getDistanceFunctors()
							.iterator(); distIter.hasNext();) {
						Distance dist = (Distance) distIter.next();
						Object value = values[valOffset++];
						String title = dist.getExplanation();
						Number[] dataVal = new Number[1];
						switch (dist.getType()) {
							case Distance.OVERALL_SUM :
								dataVal[0] = (Number) value;
								break;
							case Distance.OVERALL_MEAN :
								dataVal[0] = new Double(((Number) value)
										.doubleValue()
										/ frameCount);
								break;
							case Distance.BALANCED :
							case Distance.TARG_V_CANDS :
							case Distance.CAND_V_TARGS :
								dataVal[0] = new Double(
										getAverageOfListOfNumbers((List) value));
								break;
						}
						CategoryDataset temp = new DefaultCategoryDataset(
								seriesName, new Object[]{title},
								new Number[][]{dataVal});
						data.put(dist, temp);
					}
				}
			}

			return data;
		}
	} // FramewiseInformation

	private static final String[] frameMeasureNames = new String[]{"DETECTED",
			"IGNORED", "MISSED", "FALSE"};

	private static final String getAverageOfListOfNumbers(List l) {
		if ((l != null) && (l.size() > 0)) {
			double sum = 0.0;
			int count = 0;
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				Number curr = (Number) iter.next();
				if (curr instanceof Double) {
					Double d = (Double) curr;
					if (!d.isNaN()) {
						sum += d.doubleValue();
						count++;
					}
				} else if (curr instanceof Float) {
					Float d = (Float) curr;
					if (!d.isNaN()) {
						sum += d.doubleValue();
						count++;
					}
				} else {
					sum += curr.doubleValue();
					count++;
				}
			}
			if (count > 0) {
				return String.valueOf(sum / count);
			}
		}
		return "undefined";
	}
}

class PartialSum {
	private double localized = 0;
	private double count = 0;
	double getAverage() {
		return localized / count;
	}
	void add(double numer, double denom) {
		localized += numer;
		count += denom;
	}
	void add(PartialSum o) {
		localized += o.localized;
		count += o.count;
	}
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		if (count == 0) {
			return "undefined";
		} else {
			return String.valueOf(localized / count);
		}
	}
}