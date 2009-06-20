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
 * ExceptIterator.java
 *
 * Created on May 29, 2002, 12:33 PM
 */

package edu.umd.cfar.lamp.viper.util;

import java.util.*;

/**
 * Wraps an Iterator to only return those elements
 * that meet the requirements specified by the 
 * given ExceptFunctor function object.
 * @author  davidm
 */
public class ExceptIterator implements java.util.Iterator {
    private boolean dirtyCache = true;
    private Iterator iter;
    private Object cache;
    private ExceptFunctor except;
    
    /**
     * Function object for testing to see if 
     * the ExceptIterator should return them.
     */
    public static interface ExceptFunctor {
        /**
         * This is the method that the ExceptIterator runs on each object in
         * its internal Iterator to decide whether to return the object or not.
         * @param o An element from the Iterator
         * @return <code>true</code> if you want the object from the iterator
         *         to be passed back via the next method, and <code>false</code>
         *         if you wish for it to be ignored.
         */
        public boolean check (Object o);
    }
    
    /**
     * Creates a new instance of ExceptIterator.
     * @param ex the function object to use for determining which elements to skip.
     * @param i the iterator to wrap
     */
    public ExceptIterator(ExceptFunctor ex, Iterator i) {
        iter = i;
        except = ex;
        helpCacheNext();
    }
    /**
     * Tests to see if another element matching 
     * the criteria exists in the iterator.
     * @return <code>true</code> if <code>hasNext</code> will
     * return an element
     */
    public boolean hasNext() {
    	if (dirtyCache) {
			helpCacheNext();
    	}
        return cache != null;
    }
    
    /**
     * Gets the next valid element of the wrapped iterator.
     * @return the next element that matches the criteria
     * @throws NoSuchElementException if no element remains that matches
     */
    public Object next() {
		if (dirtyCache) {
			helpCacheNext();
		}
        if (cache != null) {
			dirtyCache = true;
            return cache;
        } else {
            throw new NoSuchElementException ();
        }
    }
    
    /**
     * This must be called directly after the call to next - calling 
     * hasNext may call next() on the underlying iterator.
     * @throws UnsupportedOperationException if the underlying iterator
     * doesn't support removal
     * @throws IllegalStateException when invoked after a call
     * to hasNext(), before a call to next(), or after another call to remove
     */
    public void remove() {
    	if (!dirtyCache) {
    		throw new IllegalStateException();
    	} else {
    		iter.remove();
			dirtyCache = false;
    		helpCacheNext();
    	}
    }

    private void helpCacheNext() {
        boolean passed = false;
        dirtyCache = false;
        while (!passed && iter.hasNext()) {
			cache = iter.next();
            passed = except.check (cache);
        }
        if (!passed) {
            cache = null;
        }
    }
}
