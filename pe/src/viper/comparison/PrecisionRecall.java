/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.comparison;

class PrecisionRecall {
	int targetsMissed = 0;
	int targetsHit = 0;
	int candidatesMissed = 0;
	int candidatesHit = 0;

	void addThis(PrecisionRecall other) {
		targetsMissed += other.targetsMissed;
		targetsHit += other.targetsHit;
		candidatesMissed += other.candidatesMissed;
		candidatesHit += other.candidatesHit;
	}

	/**
	 * Gets a string description of the current precision/recall rates.
	 * @return <code>totalTruth totalCands precision recall</code>
	 */
	public String toString() {
		double p = 0;
		if ((candidatesMissed + candidatesHit) > 0)
			p = (candidatesHit * 100 / (candidatesMissed + candidatesHit));

		double r = 0;
		if ((targetsHit + targetsMissed) > 0)
			r = ((targetsHit * 100) / (targetsHit + targetsMissed));

		return (targetsHit + targetsMissed) + " "
				+ (candidatesMissed + candidatesHit) + " " + p + " " + r;
	}

}

