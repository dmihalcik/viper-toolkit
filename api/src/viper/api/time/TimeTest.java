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

package viper.api.time;

import java.util.*;

import junit.framework.*;
import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Set of test cases for the viper.api.time package. Tests the
 * Set functionality of Span and Range objects, as well as the 
 * Range object's ability to properly congeal adjacent Spans into a 
 * single Span.
 */
public class TimeTest extends TestCase {

	private Span timeOne;
	private Span timeTwo;
	private Span frameOne;
	private Span frameTwo;
	private Time t1, t6, t30, t31, t32;
	private Frame f1, f5, f6, f10, f30, f31, f32;
	
	
	/**
	 * Constructor for FrameTest.
	 * @param arg0
	 */
	public TimeTest(String arg0) {
		super(arg0);
		f1 = new Frame(1); f5 = new Frame(5); f6 = new Frame(6); f10 = new Frame(10);
		f30 = new Frame(30); f31 = new Frame(31); f32 = new Frame(32);
		frameOne = new Span(f1, new Frame(6));
		frameTwo = new Span(f6, new Frame(11));

		t1 = new Time(1); t6 = new Time(6); 
		t30 = new Time(30); t31 = new Time(31); t32 = new Time(32);
		timeOne = new Span(t1, new Time(6));
		timeTwo = new Span(t6, new Time(11));
	}

	void testFrame() {
		helpTestInstants(f30, f31, f32);
	}

	void testTime() {
		helpTestInstants(t30, t31, t32);
	}

	private void helpTestInstants(Instant f1, Instant f2, Instant f3) {
		// Test equality, next, previous
		assertEquals(f1.next(), f2);
		assertEquals(f1.next(), f3.previous());
		assertEquals(f2, f3.previous());
		assertEquals(f1.next().next(), f3);
		assertEquals(f1.next().next(), f3.next().previous());

		// Test minus
		assertTrue(f1.minus(f2) == -1);
		assertTrue(f3.minus(f1) == 2);

		// Test comparisons
		assertTrue(f1.compareTo(f1) == 0);
		assertTrue(f1.equals(f1));
		assertTrue(f1.compareTo(f2) < 0);
		assertTrue(f2.compareTo(f1) == -f1.compareTo(f2));
		assertTrue(f3.compareTo(f1) == -f1.compareTo(f3));
		assertTrue(f2.compareTo(f1) > 0);

		// Test toString
		assertEquals("30", f1.toString());
		assertEquals("31", f2.toString());
		assertEquals("32", f3.toString());
	}

	void testSpansEquality() {
		helpTestSpansEquality (frameOne, frameTwo);
		helpTestSpansEquality (timeOne, timeTwo);
	}
	void helpTestSpansEquality(Span one, Interval two) {
		assertEquals(one, one);
		assertTrue(!one.equals(two));
	}

	void testSpansToString () {
		helpTestSpansToString(frameOne, frameTwo);
		helpTestSpansToString(timeOne, timeTwo);
	}
	private void helpTestSpansToString(Span one, Span two) {
		assertEquals("1:5", one.toString());
		assertEquals("6:10", two.toString());
	}

	void testSpansSet () {
		helpTestSpansSet(frameOne, frameTwo, f1, f6);
		helpTestSpansSet(timeOne, timeTwo, t1, t6);
	}
	private void helpTestSpansSet(Span one, Span two, Instant i1, Instant i6) {
		assertTrue(one.contains(i1));
		assertTrue(two.contains(i6));
		assertTrue(one.contains(i1.next()));
		assertTrue(one.contains(i6.previous()));
		assertTrue(!one.contains(i1.previous()));
		assertTrue(!one.contains(i6));
		assertTrue(!two.contains(i6.previous()));
		assertTrue(!two.contains(i6.next().next().next().next().next()));
	}

 	void testRange() {
		Range r = new Range();
		
		// Test one frame in a range.
		assertTrue(r.isEmpty());
		r.add(new SimpleInterval(f6.next(), f6.next().next())); // Add frame 7
		assertTrue(!r.isEmpty());
		assertTrue(r + " contains " + new Frame(7), r.contains(new Frame(7)));
		assertTrue(!r.contains(f6));
		assertTrue(!r.contains(new Frame(8)));
		assertTrue(!r.contains(frameOne)); // Doesn't contain 1:5
		assertTrue(!r.contains(frameTwo)); // Doesn't contain 6:10
		r.clear();
		assertTrue(!r.contains(f6.next()));
		assertTrue(r.isEmpty());
		
		// Test a span in a range
		r.add(frameOne);
		assertTrue(r.contains(frameOne));
		assertTrue(r.contains(f1));
		assertTrue(r.contains(f6.previous()));
		assertTrue(!frameOne.contains((Instant) f6));
		assertTrue(!r.contains(new Frame(7))); 
		assertTrue(!r.contains(f6));
		assertTrue(!r.contains(frameTwo));
		assertTrue(!r.contains(f1.previous()));
		
		// Test a span and a contiguous instant
		r.add (f6);
		assertTrue(r.contains(frameOne));
		assertTrue(r.contains(f1));
		assertTrue(r.contains(f6.previous()));
		assertTrue(r.contains(f6));
		assertTrue(!r.contains(frameTwo));
		assertTrue(!r.contains(f1.previous()));
		Iterator iter = r.iterator();
		assertEquals(new Span(f1, new Frame(7)), iter.next());
		assertTrue(!iter.hasNext());
		
		// Test a span and a discontiguous instant
		r.add(f30);
		assertTrue(r.contains(frameOne));
		assertTrue(r.contains(f1));
		assertTrue(r.contains(f30));
		assertTrue(r.contains(f6.previous()));
		assertTrue(r.contains(f6));
		assertTrue(!r.contains(frameTwo));
		assertTrue(!r.contains(f1.previous()));
		iter = r.iterator();
		assertEquals(new Span(f1, new Frame(7)), iter.next());
		Interval tSpan = (Interval) iter.next();
		assertEquals (f30, tSpan.getStart());
		assertTrue(!iter.hasNext());
		
		// Test clearing
		Instant f5 = (Instant) f6.previous();
		r.remove(f5);
		assertTrue(r.contains(f1));
		assertTrue(r.contains(f30));
		assertTrue(r.contains(f6));
		assertTrue(r.contains(f5.previous()));
		assertTrue(!r.contains(frameOne));
		assertTrue(!r.contains(frameTwo));
		assertTrue(!r.contains(f1.previous()));
		assertTrue(!r.contains(f5));
		iter = r.iterator();
		assertEquals(new Span(f1, f5), iter.next());
		tSpan = (Interval) iter.next();
		assertEquals (f6, tSpan.getStart());
		tSpan = (Interval) iter.next();
		assertEquals (f30, tSpan.getStart());
		assertTrue(!iter.hasNext());
	}

	void testParsing() {
		String range = "1:9";
		InstantRange frameRange = InstantRange.parseFrameRange(range);
		InstantRange c = new InstantRange();

		c.add(f1, f10);
		assertEquals(c, frameRange);
		assertEquals(frameRange, c);
		
		c.add(f30, f32);
		range += ", 30:31";
		frameRange = InstantRange.parseFrameRange(range);
		assertEquals(c, frameRange);
		assertEquals(frameRange, c);
		
		frameRange = InstantRange.parseFrameRange("40:131, 341:652");
	}

	void testContainsSpan() {
	}

	void testContainsInstant() {
	}
	
	void testIntegerVector() {
		TimeEncodedIntegerVector a = new TimeEncodedIntegerVector();
		TimeEncodedIntegerVector b = new TimeEncodedIntegerVector();
		TimeEncodedIntegerVector c = new TimeEncodedIntegerVector();
		// tests work as follows: init a, init b, init c, a.plus b == c, b.plus a == c
		
		assertEquals(a, b);
		assertEquals(a, a);
		
		a.set(f1, f10, new Integer(1));
		c.set(f1, f10, new Integer(1));
		assertEquals(a, a);
		assertEquals(a, c);
		assertTrue(!a.equals(b));
		assertTrue(!b.equals(a));
		b.plus(a);
		assertEquals(b, a);
		assertEquals(b, c);
		assertEquals(c, b);

		b.remove(f1, f10);
		b.set(f5, f6, new Integer(9));
		c.set(f5, f6, new Integer(10));
		b.plus(a);
		assertEquals(b,c);
		assertEquals(c,b);
		
		a.remove(f1, f10);
		a.set(f5.previous(), f10.next(), new Integer(5));
		b.plus(a);
		c.set(f5.previous(), f5, new Integer(6));
		c.set(f5,f6, new Integer(15));
		c.set(f6,f10, new Integer(6));
		c.set(f10, f10.next(), new Integer(5));
		assertEquals(b, c);
		assertEquals(c, b);

		a.remove(f1, f30);
		b.remove(f1, f30);
		c.remove(f1, f30);
		a.set(f1, f10, new Integer(1));
		b.set(f6, f10, new Integer(1));
		c.set(f1, f6, new Integer(1));
		c.set(f6, f10, new Integer(2));

		b.plus(a);
		assertEquals(b,c);
		assertEquals(c,b);


		a.remove(f1, f30);
		b.remove(f1, f30);
		c.remove(f1, f30);
		a.set(f1, f10, new Integer(1));
		b.set(f6, f10, new Integer(1));
		c.set(f1, f6, new Integer(1));
		c.set(f6, f10, new Integer(2));

		a.plus(b);
		assertEquals(a,c);
		assertEquals(c,a);
	}

	void testMultipleRange() {
		TimeEncodedIntegerVector a = new TimeEncodedIntegerVector();
		TimeEncodedIntegerVector b = new TimeEncodedIntegerVector();
		TimeEncodedIntegerVector c = new TimeEncodedIntegerVector();

		MultipleRange R = new MultipleRange(new TemporalRange[] {a,b,c});

		DynamicAttributeValue vspan;
		Object[] A;
		Iterator iter = R.iterator();
		assertTrue(!iter.hasNext());
		
		a.set(f1, f10, new Integer(1));
		c.set(f1, f10, new Integer(1));
		
		iter = R.iterator();
		assertTrue(R.iterator().hasNext());
		vspan = (DynamicAttributeValue) iter.next();
		A = (Object[]) vspan.getValue();
		assertTrue(A[0].equals(new Integer(1)));
		assertTrue(A[1] == null);
		assertTrue(A[2].equals(new Integer(1)));
		assertTrue(!iter.hasNext());
		


		a.set(f6, f10, new Integer(2));
		c.set(f1, f10, new Integer(1));
		iter = R.iterator();
		assertTrue(R.iterator().hasNext());

		vspan = (DynamicAttributeValue) iter.next();
		assertTrue(iter.hasNext());
		A = (Object[]) vspan.getValue();
		assertTrue(A[0].equals(new Integer(1)));
		assertTrue(A[1] == null);
		assertTrue(A[2].equals(new Integer(1)));
		assertEquals(vspan.getStart(), f1);
		assertEquals(vspan.getEnd(), f6);
		assertTrue(iter.hasNext());

		vspan = (DynamicAttributeValue) iter.next();
		A = (Object[]) vspan.getValue();
		assertEquals(A[0], new Integer(2));
		assertTrue(A[1] == null);
		assertEquals(A[2], new Integer(1));
		assertEquals(vspan.getStart(), f6);
		assertEquals(vspan.getEnd(), f10);
		assertTrue(!iter.hasNext());
	}
}
