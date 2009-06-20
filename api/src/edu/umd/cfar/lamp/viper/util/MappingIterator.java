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
 * Wraps an Iterator to return the given values after having been
 * run through a 'map' function.
 */
public class MappingIterator implements java.util.Iterator {
    private Iterator iter;
    private MappingFunctor map;
    
    /**
     * Function object for converting from the domain
     * to range of the map relation.
     */
    public static interface MappingFunctor {
        /**
         * This is the method that the MappingIterator runs on each object in
         * its internal Iterator to return the element in the range of the 
         * mapping relation.
         * @param o An element from the Iterator
         * @return the value corresponding to o in the map relation
         */
        public Object map (Object o);
    }
    
    /**
     * Creates a new instance of ExceptIterator.
     * @param ex the function object to use for determining which elements to skip.
     * @param i the iterator to wrap
     */
    public MappingIterator(MappingFunctor map, Iterator i) {
        iter = i;
        this.map = map;
    }

    /**
     * Tests to see if another element matching 
     * the criteria exists in the iterator.
     * @return <code>true</code> if <code>hasNext</code> will
     * return an element
     */
    public boolean hasNext() {
        return iter.hasNext();
    }
    
    /**
     * Gets the next object corresponding to the next element of the 
     * wrapped iterator.
     * @return the next mapped element
     * @throws NoSuchElementException if no element remains that matches
     */
    public Object next() {
    	return map.map(iter.next());
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
   		iter.remove();
    }
}
