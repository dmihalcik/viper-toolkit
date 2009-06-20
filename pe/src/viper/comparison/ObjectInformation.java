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

/*
 * ObjectInformation.java
 *
 * Created on May 10, 2002, 5:00 PM
 */

package viper.comparison;

import java.util.*;

/**
 * Stores precision/recall information for object evaluation.
 * @author  davidm
 */
public class ObjectInformation implements Evaluation.Information {
    private Map prs;
    /** Creates a new instance of ObjectInformation */
    public ObjectInformation() {
        prs = new HashMap();
    }
    
    /**
     * Sums another information to this one, usually from two different CompMatrices.
     * @param other the information to add to this
     */
    public void add(Evaluation.Information other) {
        ObjectInformation o = (ObjectInformation) other;
        for (Iterator iter = o.prs.entrySet().iterator(); iter.hasNext();) {
            Map.Entry curr = (Map.Entry) iter.next();
            PrecisionRecall addTo = (PrecisionRecall) prs.get (curr.getKey());
            if (addTo != null) {
                addTo.addThis ((PrecisionRecall) curr.getValue());
            } else {
                prs.put (curr.getKey(), curr.getValue());
            }
        }
    }
    
    /**
     * Gets the layout for the toString format.
     * @return the layout of the raw file
     */
    public String getLayout() {
        return "DESC_NAME TARGET_COUNT CANDIDATE_COUNT PRECISION RECALL";
    }
    
    /**
     * Indicates if anything was found. Useful for avoiding 0/0 errors.
     * @return if this contains any useful information.
     */
    public boolean hasInformation() {
        return prs.size() > 0;
    }
    

    /**
     * Prints out P/R information for a single object.
     * @param sb The string buffer to receive output.
     * @param descName the name of the descriptor
     * @param counts the P/R information
     */
    private void helpToVerbose (StringBuffer sb, String descName, PrecisionRecall counts) {
        // Print out Precision
        sb.append ("\nFor ").append (descName).append (": Precision is ");
        if ((counts.candidatesMissed + counts.candidatesHit) > 0) {
            sb.append (counts.candidatesHit * 100 /
                       (counts.candidatesMissed + counts.candidatesHit));
        } else {
            sb.append ("-");
        }
        sb.append (" %  (").append (counts.candidatesHit).append ("/");
        sb.append (counts.candidatesHit + counts.candidatesMissed);
        sb.append (")\n");

        // Print out Recall
        sb.append ("For ").append (descName).append (": Recall is ");
        if ((counts.targetsHit + counts.targetsMissed) > 0) {
            sb.append (counts.targetsHit * 100 /
                       (counts.targetsHit + counts.targetsMissed));
        } else {
            sb.append ("-");
        }
        sb.append (" %  (").append (counts.targetsHit).append ("/");
        sb.append (counts.targetsHit + counts.targetsMissed);
        sb.append (")\n");
    }
    /**
     * Get output suitable for .out files; may include new lines.
     * @return the verbose string
     */
    public String toVerbose() {
        StringBuffer sb = new StringBuffer();
        PrecisionRecall total = new PrecisionRecall();
        for (Iterator iter = prs.entrySet().iterator(); iter.hasNext();) {
            Map.Entry curr = (Map.Entry) iter.next();
            PrecisionRecall counts = (PrecisionRecall) curr.getValue();
            total.addThis (counts);
            String descName = (String) curr.getKey();
            helpToVerbose (sb, descName, counts);
        }
        helpToVerbose (sb, "TOTAL", total);
        return sb.toString();
    }
    
    /**
     * Gets the information about the evaluation.
     * @return all the attribute distances and the computed total
     */
    public String toString () {
        StringBuffer sb = new StringBuffer();
        PrecisionRecall total = new PrecisionRecall();
        for (Iterator iter = prs.entrySet().iterator(); iter.hasNext();) {
            Map.Entry curr = (Map.Entry) iter.next();
            total.addThis ((PrecisionRecall) curr.getValue());
            sb.append (curr.getKey()).append (" ").append (curr.getValue()).append ("\n");
        }
        sb.append ("TOTAL").append (" ").append (total).append ("\n");
        return sb.toString();
    }
    
    /**
     * Sets the precision and recall information assicated with this
     * evaluation result object.
     * @param desc the descriptor
     * @param pr the pr for the descriptor
     */
    public void setPrecisionRecall (String desc, PrecisionRecall pr) {
        prs.put (desc, pr);
    }
    
    /**
     * {@inheritDoc}
     * @return an empty map
     */
    public Map getDatasets(String name) {
        return new HashMap();
    }
    
}
