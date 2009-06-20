/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.comparison;

import java.io.*;
import java.util.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * Evaluates keyed descriptor pairs, as described
 * by Kasturi et. al. in their paper.
 */
public class TrackingEvaluation implements Evaluation {
	private Map descriptors = new TreeMap(
			EvaluationParameters.descriptorComparator);

	/**
	 * Gets all key attrName / TrackingMeasure pairs.
	 * 
	 * @param desc
	 *            The descriptor whose keys you want.
	 * @return java.util.Set of java.util.Map.Entrys
	 */
	private Set getKeys(Descriptor desc) {
		Map M = scope.getAllMeasuresFor(desc);
		Set allKeys = new HashSet();
		for (Iterator iter = M.entrySet().iterator(); iter.hasNext();) {
			Map.Entry curr = (Map.Entry) iter.next();
			TrackingMeasure meas = (TrackingMeasure) curr.getValue();
			if (meas.isKey()) {
				allKeys.add(curr);
			}
		}
		return allKeys;
	}

	private void helpPrintMetricsTo(Descriptor D, Map M, PrintWriter output) {
		output.print("\n" + D.getCategory() + " " + D.getName());
		TrackingMeasure fspanM = (TrackingMeasure) M.get(" framespan");
		if (fspanM != null) {
			output.print("\t[" + fspanM + "]");
		}
		output.println();
		for (Iterator iter = M.entrySet().iterator(); iter.hasNext();) {
			Map.Entry curr = (Map.Entry) iter.next();
			String attrib = (String) curr.getKey();
			if (!attrib.equals(" framespan")) {
				TrackingMeasure meas = (TrackingMeasure) curr.getValue();
				output.print("\t");
				if (meas.isKey()) {
					output.print("* ");
				} else {
					output.print("  ");
				}
				output.print(attrib + "\t");
				for (Iterator children = meas.getChildDistances().iterator(); children
						.hasNext();) {
					output.print(" " + children.next());
				}
				if (meas.getMetric() != null) {
					output.print(" [" + meas + "]");
				}
				output.println();
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void printMetricsTo(PrintWriter output) {
		for (Iterator iter = descriptors.entrySet().iterator(); iter.hasNext();) {
			Map.Entry curr = (Map.Entry) iter.next();
			helpPrintMetricsTo((Descriptor) curr.getKey(), (Map) curr
					.getValue(), output);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void printRawMetricsTo(PrintWriter raw) {
		printMetricsTo(raw);
	}

	private EvaluationParameters epf;
	private EvaluationParameters.ScopeRules scope;
	
	/**
	 * Constructs a new tracking evaluation from the given set 
	 * of paremters.  
	 * @param epf rules for the evaluation
	 */
	public TrackingEvaluation(EvaluationParameters epf) {
		this.epf = epf;
		scope = epf.getScopeRulesFor(this);
	}

	/*
	 * It would be nice to have an xml epf format For example: <pre> <tracking
	 * name="faceTrack"> <target type="object" name="Face" filter=""> <attribute
	 * name="bbox" rname="box"/> <attribute name="Key" rname="key"/> </target>
	 * <candidate type="object" name="HEAD"> <attribute name="POSITION"
	 * rname="box"/> <attribute name="KEY" rname="key"/> </candidate>
	 * 
	 * <key rname="key" fallback="none"/>
	 * 
	 * <result> <attribute rname="box" metric="dice"/> <attribute rname="box"
	 * metric="rotation"/> <attribute rname="box" metric="distance"/> </result>
	 * 
	 * </tracking> </pre>
	 */

	/**
	 * @inheritDoc
	 */
	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	/**
	 * @inheritDoc
	 */
	public PrintWriter getOutput() {
		return output;
	}

	/**
	 * @inheritDoc
	 */
	public void setRaw(PrintWriter raw) {
		this.raw = raw;
	}
	
	/**
	 * Gets the raw output stream.
	 * @return the raw output stream
	 */
	public PrintWriter getRaw() {
		return raw;
	}

	/**
	 * @inheritDoc
	 */
	public void setPrintingHeaders(boolean on) {
		this.printHeaders = on;
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean isPrintingHeaders() {
		return this.printHeaders;
	}

	/**
	 * @inheritDoc
	 */
	public void setTicker(Revealer ticker) {
		this.ticker = ticker;
	}
	
	/**
	 * Gets the current ticker UI object.
	 * @return the ticker
	 */
	public Revealer getTicker() {
		return ticker;
	}

	/**
	 * @inheritDoc
	 */
	public void setMatrix(CompMatrix cm) {
		this.mat = cm;
	}
	
	/**
	 * Gets the current map from candidate descriptors
	 * to targets.
	 * @return the comparison matrix
	 */
	public CompMatrix getMatrix() {
		return this.mat;
	}

	private PrintWriter output;
	private PrintWriter raw;
	private boolean printHeaders;
	private Revealer ticker;
	private CompMatrix mat;

	/**
	 * @inheritDoc
	 */
	public int getTickerSize() {
		return mat.T.size();
	}

	private TrackingInformation[] getTrackingInformation(Revealer ticker) {
		// First, need to set up the data so that the truth objects
		// are lined up with the supposed candidates
		List matched = new LinkedList();
		Set missed = new HashSet();
		Set falsed = new HashSet();
		falsed.addAll(mat.C);

		for (Iterator targets = mat.T.iterator(); targets.hasNext();) {
			Descriptor targ = (Descriptor) targets.next();
			boolean found = false;
			Set keys = getKeys(targ);
			if (keys.size() == 0) {
				missed.add(targ);
				continue;
			}
			for (Iterator candidates = falsed.iterator(); candidates.hasNext();) {
				Descriptor cand = (Descriptor) candidates.next();
				boolean thisPair = true;
				for (Iterator measures = keys.iterator(); measures.hasNext();) {
					String attribName = (String) ((Map.Entry) measures.next())
							.getKey();
					Attribute targAttr = targ.getAttribute(attribName, epf
							.getMap());
					Attribute candAttr = cand.getAttribute(attribName, epf
							.getMap());
					try {
						Measurable.Difference D = Distances.helpGetDiff(
								targAttr, targ.getFrameSpan(), candAttr, cand
										.getFrameSpan(), null, null, null,
								null, //blackout & ignore
								targ.getFrameSpan().beginning(), null, null); // cfd
																			  // &
																			  // old
																			  // difference
						if (Distances.getEqualityDistance().getDistance(D)
								.doubleValue() != 0.0) {
							thisPair = false;
							break;
						}
					} catch (IgnoredValueException ivx) {
						// ignore
					}
				} // for each key
				if (thisPair) {
					found = true;
					matched.add(new Comparison(targ, cand, epf.getMap()));
					candidates.remove();
				}
			} // For each candidate
			if (!found) {
				missed.add(targ);
			}
		} // for each target descriptor

		if (matched.size() == 0 && missed.size() == 0 && falsed.size() == 0) {
			if (ticker != null) {
				ticker.tick(mat.T.size());
			}
			return new TrackingInformation[0];
		}

		TrackingInformation[] counts = new TrackingInformation[matched.size()
				+ missed.size()];

		DescVector skippedTargets = new DescVector(mat.T.getParent());
		skippedTargets.addAll(missed);
		DescVector skippedCandidates = new DescVector(mat.T.getParent());
		skippedCandidates.addAll(falsed);

		int nextPlace = 0;
		for (Iterator foundComps = matched.iterator(); foundComps.hasNext();) {
			Comparison curr = (Comparison) foundComps.next();
			FrameSpan tSpan = curr.T.getFrameSpan();
			if (tSpan.size() > 1 && curr.C.getFrameSpan().size() > 1) {
				counts[nextPlace++] = helpTrack(curr.T, curr.C, scope
						.getAllMeasuresFor(curr.T), tSpan.beginning() + 1,
						tSpan.ending());
			} else {
				counts[nextPlace++] = skipTrack(curr.T, curr.C, scope
						.getAllMeasuresFor(curr.T));
			}
			if (ticker != null)
				ticker.tick();
		}

		if (skippedTargets.size() > 0 && skippedCandidates.size() > 0) {
			if (ticker != null)
				ticker.sendError("Creating tracking fallback matrix");
			int i = 0;
			mat.initializeMatrix(scope, Comparison.DETECTED, null);
			for (MatrixIterator mi = mat.getMatrixIterator(); mi.hasNextRow();) {
				int currentRowIndex = mi.nextRow();

				// If candidate was skipped-it has been removed
				if (i < currentRowIndex) {
					for (; i < currentRowIndex; i++) {
						Descriptor cand = (Descriptor) mat.C.get(i);
						if (scope.inScope(cand)) {
							skippedCandidates.add(cand);
						}
					}
				} else {
					boolean fine = false;
					while (mi.hasNextInRow()) {
						Comparison currentComp = (Comparison) mi.nextInRow();
						if (mat.goodComp(currentComp)) {
							fine = true;
						}
					}
					if (!fine) {
						Descriptor cand = (Descriptor) mat.C.get(i);
						if (scope.inScope(cand)) {
							skippedCandidates.add(cand);
						}
					}
				}
			}

			Map objectEvalMap = new TreeMap(
					EvaluationParameters.descriptorComparator);
			for (Iterator descPairs = descriptors.entrySet().iterator(); descPairs
					.hasNext();) {
				Map.Entry currEntry = (Map.Entry) descPairs.next();
				Map objEval = new TreeMap();
				Map trackEval = (Map) currEntry.getValue();
				for (Iterator attrPairs = trackEval.entrySet().iterator(); attrPairs
						.hasNext();) {
					Map.Entry currAttrPair = (Map.Entry) attrPairs.next();
					AttrMeasure meas = (AttrMeasure) currAttrPair.getValue();
					if (meas.getMetric() != null) {
						objEval.put(currAttrPair.getKey(), meas);
					}
				}
				if (objEval.size() > 0) {
					objectEvalMap.put(currEntry.getKey(), objEval);
				}
			}
			EvaluationParameters.ScopeRules objScope = epf
					.getScopeRulesFor(objectEvalMap);
			CompMatrix simpleOne = new CompMatrix(skippedTargets,
					skippedCandidates, mat.getFileInformation(), objScope,
					output);
			if (simpleOne.isContinuable()) {
				simpleOne.statistical(0.99);
				simpleOne.removeDuplicates(CompFilter.SINGLE_GREEDY);

				i = 0;
				for (MatrixIterator mi = simpleOne.getMatrixIterator(); mi
						.hasNextColumn();) {
					// Scan for the next target descriptor that has a match.
					int currentColumnIndex = mi.nextColumn();
					// fill in the skipped targets.
					if (ticker != null && i < currentColumnIndex) {
						ticker.tick(currentColumnIndex - i);
					}
					for (; i < currentColumnIndex; i++) {
						counts[nextPlace] = new TrackingInformation(
								((Descriptor) simpleOne.T.get(i)).getID());
						nextPlace++;
					}

					Descriptor currentDesc = (Descriptor) simpleOne.T
							.get(currentColumnIndex);
					boolean fine = false;
					while (mi.hasNextInColumn()) {
						Comparison currentComp = (Comparison) mi.nextInColumn();
						if (!simpleOne.goodComp(currentComp)) {
							continue;
						} else if (fine) {
							throw new RuntimeException(
									"Single Greedy matching left multiple matches!");
						}
						fine = true;
						counts[nextPlace++] = helpTrack(currentComp.T,
								currentComp.C, scope
										.getAllMeasuresFor(currentComp.T));
					}
					if (!fine) {
						counts[nextPlace++] = new TrackingInformation(
								currentDesc.getID());
					}
					i++;
					if (ticker != null) {
						ticker.tick();
					}
				}
			}
		} else {

		}

		return counts;
	}

	private TrackingInformation helpTrack(Descriptor targ, Descriptor cand,
			Map measures) {
		return helpTrack(targ, cand, measures, targ.getFrameSpan().beginning(),
				targ.getFrameSpan().ending());
	}

	private TrackingInformation skipTrack(Descriptor targ, Descriptor cand,
			Map measures) {
		return null;
	}

	private TrackingInformation helpTrack(Descriptor targ, Descriptor cand,
			Map measures, int startFrame, int endFrame) {
		FrameSpan truthSpan = targ.getFrameSpan();
		FrameSpan candSpan = cand.getFrameSpan();
		FrameSpan matchSpan = truthSpan.intersect(candSpan);
		TrackingInformation info = new TrackingInformation(targ.getID());
		FrameSpan ignoreSpan = null;
		if (startFrame > truthSpan.beginning()) {
			ignoreSpan = new FrameSpan(truthSpan.beginning(), startFrame - 1);
		}
		for (Iterator tmIter = measures.entrySet().iterator(); tmIter.hasNext();) {
			Map.Entry currPair = (Map.Entry) tmIter.next();
			String currAttrName = (String) currPair.getKey();
			TrackingMeasure currMeasure = (TrackingMeasure) currPair.getValue();
			List distances = currMeasure.getChildDistances();
			if (distances.size() > 0) {
				try {
					if (" framespan".equals(currAttrName)) {
						Measurable.Difference diff = truthSpan.getDifference(
								candSpan, null, ignoreSpan, null);
						for (Iterator distIter = distances.iterator(); distIter
								.hasNext();) {
							Distance D = (Distance) distIter.next();
							info.setValue(currAttrName, D, D.getDistance(diff));
						}
					} else {
						Attribute t = targ.getAttribute(currAttrName, scope
								.getMap());
						Attribute c = cand.getAttribute(currAttrName, scope
								.getMap());
						double[] sums = new double[distances.size()];
						int numberOfTruthFrames = 0;
						for (int currFrame = startFrame; currFrame <= endFrame; currFrame++) {
							if (matchSpan.containsFrame(currFrame)) {
								numberOfTruthFrames++;
								Measurable.Difference diff;
								try {
									diff = Distances
											.helpGetDiff(t, truthSpan, c,
													candSpan, null, null, null,
													null, currFrame,
													mat.getFileInformation(),
													null);
								} catch (IgnoredValueException ivx) {
									throw new RuntimeException(
											"Unexpected ivx: "
													+ ivx.getMessage());
								}
								int whichDist = 0;
								for (Iterator distIter = distances.iterator(); distIter
										.hasNext();) {
									Distance D = (Distance) distIter.next();
									sums[whichDist++] += D.getDistance(diff)
											.doubleValue();
								}
							}
						} // for each frame
						for (int i = 0; i < sums.length; i++) {
							double average = sums[i] / numberOfTruthFrames;
							info.setValue(currAttrName, (Distance) distances
									.get(i), average);
						}
					} // if an attribute
				} catch (IgnoredValueException ivx) {
					for (Iterator distIter = distances.iterator(); distIter
							.hasNext();) {
						Distance D = (Distance) distIter.next();
						info.setValue(currAttrName, D, Double.NaN);
					}
				}
			} // if has child distances
		} // for each tracking measure
		return info;
	}

	/**
	 * @inheritDoc
	 */
	public void printHeader() {
		if (output != null) {
			output.print(StringHelp.banner("TRACKING RESULTS", 53));
		}
		if (raw != null) {
			raw.println("\n\n\n#BEGIN_TRACKING_RESULTS");
		}
	}

	/**
	 * @inheritDoc
	 */
	public void printFooter(Evaluation.Information total) {
		if (output != null) {
			output.println("\nTotal for all Descriptors");
			if (total == null) {
				output.println("No matches found");
			} else {
				output.println(total.toVerbose());
			}
		}
		if (raw != null) {
			if (total != null) {
				raw.println(total.toString());
			}
			raw.println("\n#END_TRACKING_RESULTS");
		}
	}

	/**
	 * @inheritDoc
	 */
	public Evaluation.Information printEvaluation() {
		if (printHeaders) {
			printHeader();
		}

		TrackingInformation[] counts = getTrackingInformation(ticker);
		TrackingInformation sum = null;
		int i = 0;
		while (i < counts.length && counts[i] == null) {
			i++;
		}
		if (i < counts.length) {
			sum = counts[i];
			if (output != null) {
				output.println();
				output.println(sum.toVerbose());
			}
			if (raw != null) {
				raw.println(sum.toString());
			}
			i++;

			while (i < counts.length) {
				if (counts[i] != null) {
					sum.add(counts[i]);
					if (output != null) {
						output.println(counts[i].toVerbose());
					}
					if (raw != null) {
						raw.println(counts[i]);
					}
				}
				i++;
			}
		}
		if (printHeaders && sum != null) {
			printFooter(sum);
		}
		return sum;
	}

	/**
	 * @see Evaluation#parseEvaluation(VReader, DescriptorConfigs)
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
				Iterator relevantIter = dcfgs.getNodesByType(type, name);
				if (!relevantIter.hasNext()) {
					reader.printError("Not a Descriptor type specified "
							+ "in the Configuration " + endDescCol,
							startDescCol, endDescCol);
					reader.gotoNextRealLine();
				} else {
					DescPrototype relevant = (DescPrototype) relevantIter
							.next();
					descriptors.put(relevant, helpParseAttribMap(reader,
							relevant));
				}
			}
		}
	}

	/**
	 * A measure specifically designed for tracking, this allows 
	 * multiple child measures.
	 */
	public static class TrackingMeasure extends AttrMeasure {
		private List childMeasures = new LinkedList();
		boolean key = false;
		
		/**
		 * Gets the child measures.
		 * @return the child measures
		 */
		public List getChildDistances() {
			return childMeasures;
		}
		
		/**
		 * Determines if this measure is a key measure, meaning
		 * that only perfect matches should count, and that it
		 * should be used to line up the tracking matches.
		 * @return
		 */
		public boolean isKey() {
			return key;
		}
		
		/**
		 * Constructs a new tracking measure
		 * from the given information.
		 * @param attr the attribute to associate with
		 * @param st the string describing the measure to parse
		 * @param key if the measure is a key measure
		 * @throws ImproperMetricException if there is an error found
		 * in the description
		 */
		public TrackingMeasure(String attr, StringTokenizer st, boolean key)
				throws ImproperMetricException {
			super(attr);
			this.key = key;
			String S;
			boolean objEval = false;
			try {
				while (st.hasMoreTokens()) {
					S = st.nextToken();
					if (S.charAt(0) == '[') {
						if (objEval) {
							// FIXME - two [- -] segments, so uses second one
						}
						objEval = true;
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
								setTolerance(Double.parseDouble(S));
							} else if (!"-".equals(S)) {
								setMetric(Distances.getDistanceFunctor(attr, S));
							}
						}
						S = st.nextToken();
						if (S.charAt(S.length() - 1) == ']') {
							S = S.substring(0, S.length() - 1);
						}

						if (S.length() > 0) {
							if (!S.equals("-")) {
								setTolerance(Double.parseDouble(S));
							}
						}
					} else {
						childMeasures
								.add(Distances.getDistanceFunctor(attr, S));
					}
				} // while has more tokens
				if (!objEval) {
					setMetric(null);
					setTolerance(Double.NaN);
				}
			} catch (NoSuchElementException nsex) {
				setMetric(null);
				setTolerance(Double.NaN);
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
	 *            the reader to use
	 * @param proto
	 *            the descriptor type to use
	 * @return the attribute map for the descriptor type
	 * @throws IOException
	 */
	private Map helpParseAttribMap(VReader reader, DescPrototype proto)
			throws IOException {
		TreeMap attribMap = new TreeMap();

		StringTokenizer st = new StringTokenizer(reader.getCurrentLine());
		try {
			st.nextToken();
			st.nextToken(); // skip name
			attribMap.put(" framespan", new TrackingMeasure(" framespan", st,
					false));
		} catch (ImproperMetricException imx) {
			reader.printError(imx.getMessage());
		} catch (NoSuchElementException nsex) {
			reader.printWarning("Evaluation not properly treating metric");
		}

		reader.gotoNextRealLine();

		while (!Descriptor.startsWithCategory(reader.getCurrentLine())
				&& !reader.currentLineIsEndDirective()) {
			boolean key = false;
			st = new StringTokenizer(reader.getCurrentLine());
			String attribName = st.nextToken();
			if (attribName.startsWith("*")) {
				if (attribName.length() == 1) {
					attribName = st.nextToken();
				} else {
					attribName = attribName.substring(1);
				}
				key = true;
			}
			AttributePrototype curr = proto.getAttributePrototype(attribName);
			if (st.hasMoreTokens()) {
				try {
					if (!st.nextToken().equals(":")) {
						reader.printError("Improper placement of colon");
					} else {
						attribMap.put(attribName, new TrackingMeasure(curr
								.getType(), st, key));
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
				try {
					attribMap.put(attribName, new TrackingMeasure(curr
							.getType(), st, key));
				} catch (ImproperMetricException imx) {
					reader.printWarning(imx.getMessage());
				}
			}
			reader.gotoNextRealLine();
		}
		return attribMap;
	}

	/**
	 * Return a map of DescPrototypes to their evaluations.
	 * 
	 * @see Evaluation#getMeasureMap()
	 */
	public Map getMeasureMap() {
		return descriptors;
	}

	/**
	 * {@inheritDoc}
	 * @return <q>Tracking Evaluation</q>
	 */
	public String getName() {
		return "Tracking Evaluation";
	}
}

