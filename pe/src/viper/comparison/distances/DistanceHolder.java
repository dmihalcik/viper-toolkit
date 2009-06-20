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

package viper.comparison.distances;

import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;



/**
 * 
 */
public class DistanceHolder implements Cloneable {
    private LengthwiseEncodedList distances;
    private boolean dirty = false;
    private double average;
    private double low;
    private double high;
    private double median;

    /**
     * Creates a new instance of DistanceHolder. 
     */
    public DistanceHolder() {
        distances = new LengthwiseEncodedList();
    }
    
    /**
     * Gets a space-delimited list of the distances.
     * @return the distances, so far
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = distances.iterator(); iter.hasNext(); ) {
			DynamicValue curr = (DynamicValue) iter.next();
            Long start = (Long) curr.getStart();
            Long end = (Long) curr.getEnd();
            long length = end.longValue() - start.longValue();
            while (length-- > 0) {
                sb.append (curr.getValue()).append (' ');
            }
        }
        return sb.toString();
    }
    
    /**
     * @inheritDoc
     */
    public Object clone () {
        try {
            DistanceHolder copy = (DistanceHolder) super.clone();
            copy.distances = (LengthwiseEncodedList) distances.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }
    
    /**
     * Gets the specified statistic.
     * @param stat a statistic, e.g. {@link Distances#MEAN}
     * @return the value of the specified statistic
     */
    public double getStat (int stat) {
        switch (stat) {
            case Distances.MEAN:
                return getAverage();
            case Distances.MINIMUM:
                return getMinimum();
            case Distances.MEDIAN:
                return getMedian();
            case Distances.MAXIMUM:
                return getMaximum();
            default:
                throw new IllegalStateException ("Not a recognized statistic: " + stat);
        }
    }
    
    /**
     * Gets the average statistic.
     * @return the mean
     */
    public double getAverage() {
        refreshStats ();
        return average;
    }

    /**
     * Gets the median statistic.
     * @return the median
     */
    public double getMedian() {
        refreshStats ();
        return median;
    }
    
    /**
     * Gets the max distance.
     * @return the maximum
     */
    public double getMaximum() {
        refreshStats ();
        return high;
    }
    
    /**
     * Gets the minimum distance.
     * @return the minimum
     */
    public double getMinimum() {
        refreshStats ();
        return low;
    }

    /**
     * Sets the value for the given range.
     * @param start the first instant
     * @param end the end instant
     * @param value the distance value
     * @throws IllegalArgumentException if start is not less than end
     */
    public void set(long start, long end, double value) {
        if (start >= end) {
            throw new IllegalArgumentException ("Start not strictly less than stop: " + start + " !< " + end);
        }
        dirty = true;
        distances.set (new Long (start), new Long (end), new Double (value));
    }

    /**
     * Gets the distance at the given frame.
     * @param i the frame
     * @return the distance value
     * @throws IndexOutOfBoundsException if the value is not set
     */
    public double get(long i) {
        Double d = (Double) distances.get (new Long (i));
        if (d == null) {
            throw new IndexOutOfBoundsException ("Cannot access distance at time/frame " + i);
        }
        return d.doubleValue();
    }
    
    private void refreshStats () {
        if (dirty) {
            high = Double.NEGATIVE_INFINITY;
            low = Double.POSITIVE_INFINITY;
            double accumulator = 0;
            
            // XXX
            // I am such a slacker - there is a log (n) way to do this
            TreeMap medianTree = new TreeMap();
            long totalCount = 0;
            for (Iterator iter = distances.iterator(); iter.hasNext(); ) {
				DynamicValue curr = (DynamicValue) iter.next();
                long weight = ((Long) curr.getEnd()).longValue() - ((Long) curr.getStart()).longValue();
                totalCount += weight;
                Double val = (Double) curr.getValue();
                double v = val.doubleValue();
                accumulator += weight * v;
                high = Math.max (v, high);
                low = Math.min (v, low);

                Long keyWeight = (Long) medianTree.get (val);
                if (keyWeight == null) {
                    keyWeight = new Long (weight);
                } else {
                    keyWeight = new Long (keyWeight.longValue() + weight);
                }
                medianTree.put (val, keyWeight);
            }
            average = accumulator / totalCount;

            long end = totalCount / 2;
            boolean avgMed = (totalCount & 1) == 0;
            totalCount = 0;
            median = 0;
            for (Iterator iter = medianTree.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry curr = (Map.Entry) iter.next();
                Double val = (Double) curr.getKey();
                Long weight = (Long) curr.getValue();

                long stop = totalCount + weight.longValue();
                if (avgMed) {
                    if (totalCount < end && end < stop) { 
                        // within boundary
                        median = val.doubleValue();
                        break;
                    } else if (totalCount < end) {
                        median = val.doubleValue() * .5;
                    } else if (end < stop) {
                        median += val.doubleValue() * .5;
                        break;
                    }
                } else if (totalCount <= end && end < stop) { 
                    // within boundary
                    median = val.doubleValue();
                    break;
                }
                totalCount = stop;
            }
            dirty = false;
        }
    }
}
