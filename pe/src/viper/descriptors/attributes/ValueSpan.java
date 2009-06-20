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

package viper.descriptors.attributes;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Encodes a dynamic attribute value.
 */
public class ValueSpan {
    /** the attribute value */
    private AttributeValue value;

    /** the first frame/nano this value is valid */
    private long start;

    /** the last frame/nano this value is valid */
    private long end;
    
    /**
     * Creates a new instance of ValueSpan.
     * @param v  the value for the span
     * @param s  the first valid frame/nano
     * @param e  the last valid frame/nano
     * @throws IllegalArgumentException when s > e
     */
    public ValueSpan (AttributeValue v, long s, long e) {
        if (s > e) {
            throw new IllegalArgumentException ("Start cannot be after end: (" + s + ", " + e + ")");
        }
        value = v;
        start = s;
        end = e;
    }

    /**
     * Checks that the value and times are equals.
     * @param obj  the Object with which to compare this 
     * @return <code>false</code> if not a ValueSpan with the same
     *         value and span
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ValueSpan) {
            ValueSpan that = (ValueSpan) obj;
            return start == that.start && end == that.end 
                && ((value == null || that.value == null) 
                    ? value == that.value
                    : value.equals (that.value));
        } else {
            return false;
        }
    }
    
    /**
     * @inheritDoc
     * @return the xor of the start and end frames, with the value itself
     */
    public int hashCode() {
        long n = start ^ end;
        return ((int)(n ^ (n >>> 32))) ^ (value == null ? 0 : value.hashCode());
    }
    
    /**
     * Prints the value in .gtf format.
     * @return the value in .gtf format
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        long factor = end - (start - 1);
        if (true) {
            buf.append (start).append (':').append (end).append ("*(");
        } else if (factor > 1) {
            buf.append (factor).append ("*(");
        }
        if (value == null) {
            buf.append ("NULL");
        } else {
            buf.append ('"').append (StringHelp.backslashify (value.toString())).append ('"');
        }
        if (true || factor > 1) {
            buf.append (')');
        }
        return buf.toString();
    }

    /**
     * Gets the start of the value.
     * @return the value
     */
    public AttributeValue getValue() {
        return value;
    }
    
    /**
     * Gets the first frame of the value.
     * @return the first frame/nano
     */
    public long getStart() {
        return start;
    }
    
    /**
     * Gets the last frame.
     * @return the last frame
     */
    public long getEnd() {
        return end;
    }
    
}
