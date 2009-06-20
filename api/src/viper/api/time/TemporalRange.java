package viper.api.time;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * A temporal range is a set of frames that may not be contiguous.
 * It is composed of intervals.
 */
public interface TemporalRange extends IntervalIndexList, Cloneable {
	/**
	 * Tests to see if the range uses Frame instants.
	 * @return if the range is specified in terms of frames
	 */
	public boolean isFrameBased();
	
	/**
	 * Tests to see if the range uses Time instants.
	 * @return if the range is specified in terms of time
	 */
	public boolean isTimeBased();
	
	/**
	 * Copies the range. 
	 * {@inheritDoc}
	 */
	public Object clone();
	
	/**
	 * Test to see if this range intersects another.
	 * Implementations will likely assume that the two
	 * use compatable instant types.
	 * @param other the range to check
	 * @return if the ranges share an instant
	 */
	public boolean intersects(TemporalRange other);
	
	/**
	 * Gets the smallest interval that includes all of
	 * this range.
	 * @return
	 */
	public Interval getExtrema();
	

	/**
	 * Shifts the range by the given amount of time.
	 * @param amount the number of frames to shift
	 */
	public void shift(Instant amount);
	
	/**
	 * 
	 * @param o Could be an Instant, an Interval, or even a set of these?
	 * @return
	 */
	public boolean contains(Object o);
}
