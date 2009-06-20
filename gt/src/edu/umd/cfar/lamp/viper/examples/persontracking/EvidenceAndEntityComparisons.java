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


package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.util.*;

public class EvidenceAndEntityComparisons {
	public static final Comparator EVIDENCE_BY_SIMILARITY = new Comparator() {
		public int compare(Object row1, Object row2) {
			int r1 = ((GalleryEvidence) row1).getSimilarity();
			int r2 = ((GalleryEvidence) row2).getSimilarity();
			return r2 - r1;
		}
	};
	public static final Comparator EVIDENCE_BY_PRIORITY = new Comparator() {
		public int compare(Object row1, Object row2) {
			int r1 = ((GalleryEvidence) row1).getPriority();
			int r2 = ((GalleryEvidence) row2).getPriority();
			return r2 - r1;
		}
	};
}
