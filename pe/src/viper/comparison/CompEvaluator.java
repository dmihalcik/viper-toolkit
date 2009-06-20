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

/**
 * A collection of Descriptor Objects that supports various operations on them,
 * such as comparison between two DescVectors, collection of statistics, reading
 * and printing from files, and various others.
 * 
 * @author Felix Sukhenko
 * @author David Mihalcik
 * @author David Doermann
 * @version %I%, %G%
 */
public class CompEvaluator {
	DescriptorData targets;
	DescriptorData candidates;

	BitSet someLeft = new BitSet();

	String[] names;
	CompMatrix[] complete;

	double statThreshold = 1.0;
	int matchType = CompFilter.NONE;

	int filtered = CompFilter.NONE;
	int currentLevel = -1;

	PrintWriter log = null;

	private EvaluationParameters.ScopeRules epf;

	/**
	 * Creates a new evaluator, which compares several
	 * files and descriptor types, making it somewhat more
	 * general than a {@link CompMatrix}.
	 * @param targets the target data set
	 * @param candidates the candidate data set
	 * @param statThreshold the statistic threshold
	 * @param matchType the type of frame range matching to use
	 * @param epf the metric and rule information
	 * @param log the error log
	 */
	public CompEvaluator(DescriptorData targets, DescriptorData candidates,
			double statThreshold, int matchType,
			EvaluationParameters.ScopeRules epf, PrintWriter log) {
		this.targets = targets;
		this.candidates = candidates;
		this.statThreshold = statThreshold;
		this.matchType = matchType;
		this.log = log;
		this.epf = epf;

		Revealer ticker = null;

		if (!targets.isMultifile() || !candidates.isMultifile()) {
			DescriptorList T = targets.getFlattenedData();
			DescriptorList C = candidates.getFlattenedData();
			if (log != null) {
				ticker = new Revealer(T.size() * C.size(), 40, ".", log);
			}
			complete = new CompMatrix[1];
			complete[0] = new CompMatrix(T, C, T.getFileInformation(), epf,
					log, ticker);
			someLeft.set(0);
			names = new String[]{"the only file specified in the data."};
			return;
		}

		Iterator targetFiles = targets.getFileNames();
		Collection fileNames = new HashSet();
		// First, find all the filenames that match.
		while (targetFiles.hasNext()) {
			String currentFileName = (String) targetFiles.next();

			if (currentFileName.length() > 0) {
				// If there is a filename, try to find its mate.
				if (fileNames.contains(currentFileName)) {
					System.err.println("Duplicate file name! \""
							+ currentFileName + '"');
				}
				fileNames.add(currentFileName);
			}

			/*
			 * This is the case for a lot of the data, that there is one unnamed
			 * <file> element. Basically, a lot of this work is then for
			 * nothing. ;-)
			 */
			else {
				fileNames = null;
				complete = new CompMatrix[1];
				DescriptorList nullFileTargets = targets.getForFile("");
				DescriptorList nullFileCandidates = candidates.getForFile("");

				if (nullFileTargets == null) {
					nullFileTargets = new DescVector(targets);
				}
				if (nullFileCandidates == null) {
					nullFileCandidates = new DescVector(candidates);
				}

				if (log != null) {
					ticker = new Revealer(nullFileTargets.size()
							* nullFileCandidates.size(), 40, ".", log);
				}
				complete[0] = new CompMatrix(nullFileTargets,
						nullFileCandidates, nullFileTargets
								.getFileInformation(), epf, log, ticker);
				someLeft.set(0);
				if (targetFiles.hasNext())
					System.err.println("<file> element missing 'name=' attr."
							+ "Assuming it is the only one of interest....");
				names = new String[]{"the only file specified in the data."};
				return;
			}
		}
		for (Iterator iter = candidates.getFileNames(); iter.hasNext();) {
			fileNames.add(iter.next());
		}

		names = (String[]) fileNames.toArray(new String[0]);
		complete = new CompMatrix[names.length];
		if (complete.length == 0) {
			System.err.println("There are no matching files!");
		}

		if (log != null) {
			ticker = new Revealer(names.length, 40, ".", log);
		}

		for (int i = 0; i < names.length; i++) {
			DescriptorList currFileTargets = targets.getForFile(names[i]);
			DescriptorList currFileCandidates = candidates.getForFile(names[i]);

			if (currFileTargets == null) {
				currFileTargets = new DescVector(targets);
				if (log != null) {
					log.println("Missing truth for file: \"" + names[i] + '"');
				}
			}
			if (currFileCandidates == null) {
				currFileCandidates = new DescVector(candidates);
				if (log != null) {
					log
							.println("Missing results for file: \"" + names[i]
									+ '"');
				}
			}

			complete[i] = new CompMatrix(currFileTargets, currFileCandidates,
					currFileTargets.getFileInformation(), epf, log, null);
			someLeft.set(i);
			if (ticker != null) {
				ticker.tick();
			}
		}
	}

	/**
	 * Print the table of matches. This is the 
	 * {@link CompMatrix#printOverallMatchTable() overall 
	 * match table}, if any matches exist.
	 * @return the formatted match distances
	 */
	public String printMatchTables() {
		String S = new String();
		for (int i = 0; i < complete.length; i++) {
			if ((complete[i].C.size() == 0) && (complete[i].T.size() == 0)) {
				System.err.println("CompMatrix " + i + " is an empty matrix");
			} else if (complete[i].C.size() == 0) {
				System.err.println("CompMatrix " + i
						+ " is has no candidates to match " + complete[i].T);
			} else if (complete[i].T.size() == 0) {
				System.err.println("CompMatrix " + i
						+ " is has no targets to match " + complete[i].C);
			} else {
				S += complete[i].printOverallMatchTable();
			}
		}
		return S;
	}

	/**
	 * Print the current false/missed information to the given
	 * raw and human-readable outputs.
	 * @param output the human-readable output
	 * @param raw the raw formatted output
	 * @param targetConfigs the target descriptors to print out
	 */
	public void printCurrentFM(PrintWriter output, PrintWriter raw,
			DescriptorConfigs targetConfigs) {
		String l = null;
		if (output != null) {
			StringBuffer level = new StringBuffer().append("LEVEL ").append(
					currentLevel);
			if (filtered != CompFilter.NONE) {
				level.append("C");
			}
			l = level.toString();
		}
		if (output != null) {
			output.print(StringHelp.banner(l + "\nFALSE DETECTIONS", 53));
		}
		for (int i = 0; i < complete.length; i++) {
			complete[i].printCurrentFalse(output, raw, targetConfigs);
		}

		if (output != null) {
			output.print(StringHelp.banner(l + "\nMISSED DETECTIONS", 53));
		}
		for (int i = 0; i < complete.length; i++)
			complete[i].printCurrentMissed(output, raw, targetConfigs);
	}

	/**
	 * Print the current surviving candidate information to the given
	 * raw and human-readable outputs.
	 * @param output the human-readable output
	 * @param raw the raw formatted output
	 * @param targetConfigs the target descriptors to print out
	 */
	public void printCandidates(PrintWriter output, PrintWriter raw,
			DescriptorConfigs targetConfigs) {
		for (int i = 0; i < complete.length; i++)
			complete[i].printCandidates(output, raw, targetConfigs);
	}

	/**
	 * Runs an evaluation using the given Evaluation object on all of the
	 * comparison matrices, summing the results, and returning the sum. Note
	 * that the return value may be null if there were no matches, or if that is
	 * what the Evaluation returns from its printEvaluation method.
	 * 
	 * @param eva
	 *            The evaluation to run.
	 * @return The sum of the results (each added with
	 *         Evaluation.Information.add()).
	 */
	public Evaluation.Information printEvaluationResults(Evaluation eva) {
		eva.printHeader();
		eva.setPrintingHeaders(false);

		Evaluation.Information sum = null;
		if (log != null) {
			int tickersize = 0;
			for (int j = 0; j < complete.length; j++) {
				eva.setMatrix(complete[j]);
				tickersize += eva.getTickerSize();
			}
			eva.setTicker(new Revealer(tickersize, 40, ".", log));
		}

		for (int j = 0; j < complete.length; j++) {
			if (eva.getOutput() != null) {
				eva.getOutput().println("\n\tFor file \"" + names[j] + "\"");
			}
			eva.setMatrix(complete[j]);
			if (sum == null) {
				sum = eva.printEvaluation();
			} else {
				sum.add(eva.printEvaluation());
			}
		}

		eva.printFooter(sum);
		if (log != null) {
			log.println();
		}

		return sum;
	}

	/**
	 * Print out precision and recall information.
	 * @param output the human-readable output
	 * @param raw the raw formatted output
	 * @param targetConfigs the target descriptors to print out
	 */
	public void printPR(PrintWriter output, PrintWriter raw,
			DescriptorConfigs targetConfigs) {
		if (output != null) {
			output.print(StringHelp.banner("SUMMARY RESULTS", 53));
		}
		if (raw != null) {
			raw.println("\n\n// \n// \n// \n#BEGIN_SUMMARY");
		}

		PrecisionRecall[] counts = new PrecisionRecall[targetConfigs.size()];
		for (int i = 0; i < counts.length; i++)
			counts[i] = new PrecisionRecall();
		PrecisionRecall total = new PrecisionRecall();
		Iterator descs = targetConfigs.iterator();
		int i = 0;
		while (descs.hasNext()) {
			Descriptor currConfigDesc = (Descriptor) descs.next();
			if (epf.inScope(currConfigDesc)) {
				for (int j = 0; j < complete.length; j++)
					complete[j].addPRInfo(currConfigDesc, counts[i]);
				if (raw != null)
					helpPrintRawPR(currConfigDesc.getName(), counts[i], raw);
				if (output != null)
					helpPrintOutPR(currConfigDesc.getName(), counts[i], output);
				total.addThis(counts[i]);
				i++;
			}
		}

		if ((total.targetsMissed + total.targetsHit) == 0) {
			if (output != null) {
				output.println("\nNo information found in files.");
			}
		} else {
			if (raw != null)
				helpPrintRawPR("TOTAL", total, raw);
			if (output != null)
				helpPrintOutPR("TOTAL", total, output);
		}
		if (raw != null)
			raw.println("\n#END_SUMMARY");
	}

	private void helpPrintOutPR(String descName, PrecisionRecall counts,
			PrintWriter output) {
		if ((counts.candidatesMissed + counts.candidatesHit) != 0) {
			output
					.println("\nFor "
							+ descName
							+ ": Precision is "
							+ (counts.candidatesHit * 100 / (counts.candidatesMissed + counts.candidatesHit))
							+ " %  (" + counts.candidatesHit + "/"
							+ (counts.candidatesHit + counts.candidatesMissed)
							+ ")");
		} else {
			output.println("\nFor " + descName + ": Precision is - % (0/0)");
		}

		if ((counts.targetsHit + counts.targetsMissed) != 0) {
			output
					.println("For "
							+ descName
							+ ": Recall is "
							+ (counts.targetsHit * 100 / (counts.targetsMissed + counts.targetsHit))
							+ " %  (" + counts.targetsHit + "/"
							+ (counts.targetsHit + counts.targetsMissed) + ")");
		} else {
			output.println("For " + descName + ": Recall is - % (0/0)");
		}
	}

	private void helpPrintRawPR(String descName, PrecisionRecall counts,
			PrintWriter raw) {
		double p = 0;
		if ((counts.candidatesMissed + counts.candidatesHit) > 0)
			p = (counts.candidatesHit * 100 / (counts.candidatesMissed + counts.candidatesHit));

		double r = 0;
		if ((counts.targetsHit + counts.targetsMissed) > 0)
			r = ((counts.targetsHit * 100) / (counts.targetsHit + counts.targetsMissed));

		raw.println(descName + " " + (counts.targetsHit + counts.targetsMissed)
				+ " " + (counts.candidatesMissed + counts.candidatesHit) + " "
				+ p + " " + r);
	}
}