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

package edu.umd.cfar.lamp.viper.geometry;

//see http://members.pingnet.ch/gamma/junit.htm
import junit.framework.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Testsuite for the geometry package.
 * <UL>
 * Still to add:
 * <LI>Load and size testing of Rationals</LI>
 * <LI>Testing of shapes</LI>
 * <LI>Testing of points</LI>
 * <LI>Accuracy of circle intersection</LI>
 * </UL>
 */
public class GeometryTest extends TestCase {
	Rational zero;
	Rational one;
	Rational two;
	Rational tempRational1;
	Rational tempRational2;

	ConvexPolygon unitSquare;
	ConvexPolygon triangle1;
	ConvexPolygon triangle2;
	ConvexPolygon bigSquare;

	PolyList tempPoly;

	/**
	 * Constructs a new test of the geometry package.
	 * 
	 * @param name
	 *            the name of the test
	 */
	public GeometryTest(String name) {
		super(name);
		setUp();
	}

	protected void setUp() {
		// Init Rational stuff
		zero = new Rational(0);
		one = new Rational(1);
		two = new Rational(2);

		tempRational1 = new Rational(0);
		tempRational2 = new Rational(0);

		// Init polygon stuff
		int[] points = { 0, 0, 1, 0, 1, 1, 0, 1 };
		unitSquare = new ConvexPolygon(points);

		points = new int[] { 0, 0, 200, 0, 100, 100 };
		triangle1 = new ConvexPolygon(points);

		points = new int[] { 0, 100, 100, 0, 200, 100 };
		triangle2 = new ConvexPolygon(points);

		points = new int[] { 0, 0, 200, 0, 200, 200, 0, 200 };
		bigSquare = new ConvexPolygon(points);

		tempPoly = new ConvexPolygon();
	}

	/**
	 * Creates the test suite
	 * 
	 * @return the Test to run
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new GeometryTest("testRationalEquals"));
		suite.addTest(new GeometryTest("testRationalInequality"));
		suite.addTest(new GeometryTest("testRationalInfinity"));
		suite.addTest(new GeometryTest("testRationalMath"));
		suite.addTest(new GeometryTest("testRationalOverflow"));

		suite.addTest(new GeometryTest("testCPolyEquals"));
		suite.addTest(new GeometryTest("testCPolyArea"));
		suite.addTest(new GeometryTest("testCPolyIntersects"));
		suite.addTest(new GeometryTest("testCPolyIntersection"));
		suite.addTest(new GeometryTest("testCPolyClip"));
		suite.addTest(new GeometryTest("testCPolySubtract"));
		suite.addTest(new GeometryTest("testCPolyAdd"));

		suite.addTest(new GeometryTest("testPolyArea"));
		//suite.addTest(new GeometryTest("testPolyIntersection"));

		suite.addTest(new GeometryTest("testBBoxEquals"));
		suite.addTest(new GeometryTest("testOBoxEquals"));
		suite.addTest(new GeometryTest("testGetNearIntersection"));

		suite.addTest(new GeometryTest("testCPolyChildren"));
		suite.addTest(new GeometryTest("testCPolyUnion"));

		suite.addTest(new GeometryTest("testOBoxNegativeDistanceError"));

		suite.addTest(new GeometryTest("testCircles"));

		return suite;
	}

	/**
	 * Exercises the Circle type.
	 */
	public void testCircles() {
		// First do some sanity tests
		Circle first = new Circle(0, 0, 10);
		Circle second = new Circle(0, 0, 10);
		assertEquals(first, second);
		assertTrue(
			first + " area is reported as " + first.area(),
			first.area() == Math.PI * 100);
		assertTrue(
			"Testing coincident circles -- area should be PI * 100, is "
				+ first.intersectArea(second),
			first.intersectArea(second) == first.area());

		second = new Circle(100, 100, 10);
		assertTrue(
			"Testing circles that do not intersect. area is reported as "
				+ first.intersectArea(second),
			first.intersectArea(second) == 0);

		second = new Circle(0, 20, 10);
		assertTrue(
			"Testing circles that don't intersect: "
				+ first.intersectArea(second),
			first.intersectArea(second) == 0);

		second = new Circle(10, 0, 10);
		assertTrue(
			"Testing circles that intersect: "
				+ first
				+ " & "
				+ second
				+ " area == "
				+ first.intersectArea(second),
			(first.intersectArea(second) > 0)
				&& (first.intersectArea(second) < 200));

		second = new Circle(10, 10, 100);
		assertTrue(
			"Inscribed circle area is said to be "
				+ first.intersectArea(second),
			first.intersectArea(second) == Math.PI * 100);

		second = new Circle(10, 00, 15);
		assertTrue(
			"Circles with odd sizes: " + first.intersectArea(second),
			first.intersectArea(second) < Math.PI * 100);
	}

	/**
	 * Tests the Rational data type's equality methods.
	 */
	public void testRationalEquals() {
		assertTrue(zero.equals(0));
		assertTrue(one.equals(1));

		tempRational1.setTo(9, 9);
		assertEquals(one, tempRational1);
		assertTrue(tempRational1.equals(1));

		Rational.minus(zero, one, tempRational2);
		tempRational1.negate();
		assertEquals(tempRational1, tempRational2);
		assertTrue(tempRational1.equals(-1));
		assertTrue(tempRational2.equals(-1));

		tempRational1 = new Rational(two);
		tempRational1.reciprocate();
		assertTrue(two.equals(2));
		assertTrue(!tempRational1.equals(two));
		assertTrue(!tempRational1.equals(zero));
	}

	/**
	 * Tests the Rational data type's inequality methods.
	 */
	public void testRationalInequality() {
		assertTrue(zero.lessThan(one));
		assertTrue(zero.lessThan(1));
		assertTrue(!zero.greaterThan(0));
		assertTrue(!zero.greaterThan(zero));
		assertTrue(!one.lessThan(0));
		assertTrue(!one.lessThan(one));
		assertTrue(one.greaterThan(0));
		assertTrue(one.greaterThan(zero));

		tempRational1.setTo(11, 10);
		assertTrue(tempRational1.greaterThan(1));
		assertTrue(one.lessThan(tempRational1));
		assertTrue(!one.equals(tempRational1));
		assertTrue(tempRational1.lessThan(2));

		tempRational1.negate();
		tempRational2.setTo(-1);
		assertTrue(tempRational1.lessThan(tempRational2));
		assertTrue(tempRational1.lessThan(-1));
		assertTrue(tempRational1.greaterThan(-2));
	}

	/**
	 * Tests the Rational data type's ability to handle values outside the
	 * finite.
	 */
	public void testRationalInfinity() {
		tempRational1.setTo(100, 0);
		assertTrue(tempRational1.greaterThan(1000));
		assertTrue(tempRational1.greaterThan(one));

		tempRational1.negate();
		assertTrue(
			"-inf < 0 :: " + tempRational1 + " < " + zero,
			tempRational1.lessThan(zero));
		assertTrue(
			"-inf < -1000 :: " + tempRational1 + " < -1000",
			tempRational1.lessThan(-1000));

		boolean passedXTest = false;
		tempRational1.setTo(0, 0);
		try {
			one.lessThan(tempRational1);
		} catch (ArithmeticException ax) {
			passedXTest = true;
		}
		assertTrue(passedXTest);
	}

	/**
	 * Tests the Rational static math methods (plus, multiply, etc.).
	 */
	public void testRationalMath() {
		Rational.multiply(one, zero, tempRational1);
		assertTrue("1 * 0 == 0", tempRational1.equals(0));

		tempRational1.setTo(-1);
		tempRational2.setTo(-1);
		Rational.multiply(tempRational1, tempRational2, tempRational1);
		assertTrue("-1 * -1 == 1", tempRational1.equals(one));

		tempRational1.setTo(-1);
		tempRational2.setTo(-1);
		Rational.divide(tempRational1, tempRational2, tempRational1);
		assertTrue("-1 / -1 == 1", tempRational1.equals(one));

		Rational.plus(one, one, tempRational1);
		assertTrue("1 + 1 == 2", tempRational1.equals(2));
		tempRational1.reciprocate();
		tempRational2.setTo(6, 12);
		assertTrue(
			"Reciprocal of 2 == 6/12",
			tempRational1.equals(tempRational2));

		tempRational2.setTo(6, 7);
		Rational.multiply(tempRational1, tempRational2, tempRational1);
		tempRational2.setTo(3, 7);
		assertTrue("1/2 * 6/7 == 3/7", tempRational1.equals(tempRational2));
	}

	/**
	 * Tests the Rational data type's ability to handle numbers larger than 32
	 * or 64 bits.
	 */
	public void testRationalOverflow() {
		tempRational1.setTo(74165, 781);
		tempRational2.setTo(-396250, 13799);
		Rational.multiply(tempRational1, tempRational2, tempRational1);
		tempRational2.setTo(-29387881250L, 10777019);
		assertEquals(
			"(74165 / 781)*(-396250 / 13799)"
				+ " == (-29387881250 / 10777019)"
				+ ", not "
				+ tempRational1,
			tempRational1,
			tempRational2);
		assertTrue(
			"(74165 / 781)*(-396250 / 13799)"
				+ " == (-29387881250 / 10777019)"
				+ ", not "
				+ tempRational1,
			tempRational1.lessThan(0));

		tempRational1.setTo(1423, 49);
		tempRational2.setTo(318);
		Rational.multiply(tempRational1, tempRational2, tempRational1);
		assertTrue(Math.round(tempRational1.doubleValue()) == 9235);

		Rational total = new Rational(0);
		Rational.plus(total, tempRational1, total);
		assertTrue(
			"Testing total = " + total,
			Math.round(total.doubleValue()) == 9235);
	}

	/**
	 * Tests the ConvexPolygon's equality methods.
	 */
	public void testCPolyEquals() {
		assertEquals(unitSquare, unitSquare);
		assertEquals(bigSquare, bigSquare);
		assertTrue(!bigSquare.equals(unitSquare));
		assertTrue(!unitSquare.equals(bigSquare));
	}

	/**
	 * Tests polygon area measurement.
	 * XXX: add self-intersecting polygons
	 */
	public void testPolyArea() {
		Polygon p, orthoSnake, snake, farfalle, empty, octogon;
		try {
			p = new Polygon("(0 0)(4 0)(0 3)");
			orthoSnake = new Polygon("(0 0)(2 0)(2 1)(3 1)(3 0)(4 0)(4 2)(1 2)(1 1)(0 1)");
			empty = new Polygon();
			farfalle = new Polygon("(0 0)(3 1)(6 0)(6 3)(3 2)(0 3)");
			snake = new Polygon("(0 0)(1 1)(2 0)(3 1)(4 0)(5 0)(3 2)(2 1)(1 2)(0 2)");
			octogon = new Polygon("(0 1)(1 0)(2 0)(3 1)(3 3)(2 4)(1 4)(0 3)");
		} catch (BadDataException e) {
			e.printStackTrace();
			assertTrue("bad polygon construction: " + e.getLocalizedMessage(), false);
			throw new RuntimeException(e);
		}
		assertTrue("empty.area() = " + empty.area(), empty.area().equals(0));
		assertTrue("orthoSnake.area() = " + orthoSnake.area(), orthoSnake.area().equals(6));
		assertTrue("p.area() = " + p.area(), p.area().equals(6));
		assertTrue("snake.area() = " + snake.area(), snake.area().equals(5));
		assertTrue("farfalle.area() = " + farfalle.area(), farfalle.area().equals(12));
		assertTrue("octogon.area() = " + octogon.area(), octogon.area().equals(10));
		
		PolyList snaked = PolyList.intersection(snake, orthoSnake);
		assertTrue("snaked.area() = " + snaked.area(), snaked.area().equals(3));
	}
	
	/**
	 * Tests to see if two doubles are within some epsilon.
	 * @param alpha
	 * @param beta
	 * @return if alpha is within an epsilon of beta
	 */
	public static boolean closeEnough(double alpha, double beta) {
		return Math.abs(alpha - beta) < .0005;
	}
	

	/**
	 * Tests the ConvexPolygon's area methods.
	 */
	public void testCPolyArea() {
		assertTrue(
			"unitSquare.area == " + unitSquare.area(),
			unitSquare.area().equals(1));
		assertTrue(
			"bigSquare.area == " + bigSquare.area(),
			bigSquare.area().equals(40000));
		assertTrue(
			"triangle1.area == traingle2.area ("
				+ triangle1.area()
				+ " == "
				+ triangle2.area()
				+ ")",
			triangle1.area().equals(triangle2.area()));
		assertTrue(
			"triangle1.area == " + triangle1.area(),
			triangle1.area().equals(10000));
		assertTrue(
			"triangle2.area == " + triangle2.area(),
			triangle2.area().equals(10000));

		int[] points = { 0, 50, 100, -50, 200, 50 };
		tempPoly = new ConvexPolygon(points);
		assertTrue(
			tempPoly + ".area equals 10000 == " + tempPoly.area(),
			tempPoly.area().equals(10000));

		Component firstPoint =
			new Component(
				new Rational(2226, 11),
				new Rational(74),
				new Rational(1));
		Component lastPoint = new Component(138, 74, 1);
		Component newPoint = new Component(138, 74, 1);
		Rational areaSign = Util.areaSign(newPoint, firstPoint, lastPoint);
		assertEquals(areaSign, new Rational(0));

		points = new int[] { 0, 50, 100, -50, 200, 50, 300, 100 };
		boolean catchesConcavities = false;
		try {
			tempPoly = new ConvexPolygon(points);
		} catch (IllegalStateException bdx) {
			catchesConcavities = true;
		}
		assertTrue("added a concave point: " + tempPoly, catchesConcavities);
	}

	/**
	 * Tests the ConvexPolygon's intersection tests and computations.
	 */
	public void testCPolyIntersects() {
		assertTrue(
			"Does unitSquare.bbox ("
				+ unitSquare.bbox
				+ ") intersect bigSquare.bbox ("
				+ bigSquare.bbox
				+ ") ?",
			unitSquare.getBoundingBox().intersects(bigSquare.getBoundingBox()));
		assertTrue(
			"Does bigSquare.bbox ("
				+ bigSquare.getBoundingBox()
				+ ") intersect unitSquare.bbox ("
				+ unitSquare.getBoundingBox()
				+ ") ?",
			bigSquare.getBoundingBox().intersects(unitSquare.getBoundingBox()));
		assertTrue(
			"Does triangle1.bbox ("
				+ triangle1.getBoundingBox()
				+ ") intersect unitSquare.bbox ("
				+ unitSquare.getBoundingBox()
				+ ") ?",
			triangle1.getBoundingBox().intersects(unitSquare.getBoundingBox()));
		assertTrue(
			"Does triangle2.bbox ("
				+ triangle2.getBoundingBox()
				+ ") intersect unitSquare.bbox ("
				+ unitSquare.getBoundingBox()
				+ ") ?",
			triangle2.getBoundingBox().intersects(unitSquare.getBoundingBox()));
		assertTrue(
			"Does triangle1.bbox ("
				+ triangle1.getBoundingBox()
				+ ") intersect triangle2.bbox ("
				+ triangle2.getBoundingBox()
				+ ") ?",
			triangle1.getBoundingBox().intersects(triangle2.getBoundingBox()));
		assertTrue(
			"Does triangle1.bbox ("
				+ triangle1.getBoundingBox()
				+ ") intersect bigSquare.bbox ("
				+ bigSquare.getBoundingBox()
				+ ") ?",
			triangle1.getBoundingBox().intersects(bigSquare.getBoundingBox()));
	}

	/**
	 * Tests the ConvexPolygon's subtraction methods.
	 */
	public void testCPolySubtract() {
		ConvexPolygon cutter =
			new ConvexPolygon(new int[] { 0, 0, 100, 0, 0, 100 });
		BoundingBox central = new BoundingBox(0, 0, 50, 50);
		ConvexPolygon top =
			new ConvexPolygon(new int[] { 0, 50, 50, 50, 0, 100 });
		ConvexPolygon left =
			new ConvexPolygon(new int[] { 50, 0, 100, 0, 50, 50 });

		tempPoly = ConvexPolygon.subtract(cutter, central);
		assertTrue(
			"cutter - central == " + tempPoly,
			tempPoly.equals(PolyList.union(top, left)));
		assertTrue(
			"(cutter - central).area() == " + tempPoly.area(),
			tempPoly.area().equals(50 * 50));
		assertTrue(
			"(cutter - central) ^ central == "
				+ PolyList.intersection(tempPoly, central),
			PolyList.intersection(tempPoly, central).area().equals(0));

		central = new BoundingBox(10, 10, 10, 10);
		tempPoly = ConvexPolygon.subtract(cutter, central);
		Rational goal = new Rational(cutter.area());
		Rational.minus(goal, new Rational(100), goal);
		assertTrue(
			"(cutter - central).area() == " + tempPoly.area(),
			tempPoly.area().equals(goal));
	}

	/**
	 * Tests the ConvexPolygon's clipping methods.
	 */
	public void testCPolyClip() {
		ConvexPolygon cutter =
			new ConvexPolygon(new int[] { 0, 0, 100, 0, 0, 100 });
		BoundingBox central = new BoundingBox(0, 0, 50, 50);
		ConvexPolygon top =
			new ConvexPolygon(new int[] { 0, 50, 50, 50, 0, 100 });
		ConvexPolygon left =
			new ConvexPolygon(new int[] { 50, 0, 100, 0, 50, 50 });

		// cutter is a right triangle in the first quadrant. Split it into
		// three pieces using the lines through ab and mn
		Pnt a = new Pnt(50, 300);
		Pnt b = new Pnt(50, 301);
		Pnt m = new Pnt(-10, 50);
		Pnt n = new Pnt(100, 50);

		ConvexPolygon[] horizontal = ConvexPolygon.clip(cutter, m, n);
		assertTrue(
			"should split into two, instead split into: " + horizontal.length,
			horizontal.length == 2);
		Rational temp = new Rational();
		Rational.plus(horizontal[0].area(), horizontal[1].area(), temp);
		assertTrue(
			"sums to same area: "
				+ horizontal[0].area()
				+ " + "
				+ horizontal[1].area()
				+ " == "
				+ cutter.area(),
			temp.equals(cutter.area()));
		assertEquals(horizontal[0], top);
		PolyList pentagon = PolyList.union(central, left);
		assertEquals(horizontal[1].area(), pentagon.area());
		tempPoly = PolyList.intersection(pentagon, horizontal[1]);
		assertEquals(pentagon, horizontal[1]);
		assertEquals(horizontal[1], pentagon);

		ConvexPolygon[] vertical1 = ConvexPolygon.clip(horizontal[0], a, b);
		ConvexPolygon[] vertical2 = ConvexPolygon.clip(horizontal[1], a, b);
		if (vertical1.length == 2) {
			assertTrue(
				"The triangle split into:\n\t"
					+ vertical1[0]
					+ "\n\t"
					+ vertical1[1],
				false);
		}
		assertTrue(
			"should split into three, instead split into: "
				+ (vertical1.length + vertical2.length),
			(vertical1.length + vertical2.length) == 3);

		assertEquals(central, vertical2[0]);
		assertEquals(top, vertical1[0]);
		assertEquals(left, vertical2[1]);
	}

	/**
	 * Tests the ConvexPolygon's intersection methods.
	 */
	public void testCPolyIntersection() {
		int[] points = { 75, -100, 125, -200, 125, 150, 75, 200 };
		ConvexPolygon cutter = new ConvexPolygon(points);

		points = new int[] { 75, 0, 125, 0, 125, 75, 100, 100, 75, 75 };
		ConvexPolygon correct = new ConvexPolygon(points);

		tempPoly = ConvexPolygon.intersection(triangle1, cutter);
		assertTrue(
			"Simple cutter: " + tempPoly.area() + " == " + correct.area(),
			tempPoly.area().equals(correct.area()));
		assertEquals(correct, tempPoly);

		cutter = new BoundingBox(75, -100, 50, 300);
		tempPoly = ConvexPolygon.intersection(triangle1, cutter);
		assertTrue(
			"Bbox cutter 0 ("
				+ triangle1
				+ " & "
				+ cutter
				+ " = "
				+ tempPoly
				+ ")",
			tempPoly.area().equals(correct.area()));
		assertEquals(correct, tempPoly);

		cutter = new BoundingBox(75, -100, 50, 200);
		tempPoly = ConvexPolygon.intersection(triangle1, cutter);
		assertTrue(
			"Bbox cutter 1 ("
				+ triangle1
				+ " & "
				+ cutter
				+ " = "
				+ tempPoly
				+ ")",
			tempPoly.area().equals(correct.area()));
		assertEquals(correct, tempPoly);

		cutter = new BoundingBox(75, 0, 50, 200);
		tempPoly = ConvexPolygon.intersection(triangle1, cutter);
		assertTrue(
			"Bbox cutter 2 ("
				+ triangle1
				+ " & "
				+ cutter
				+ " = "
				+ tempPoly
				+ ")",
			tempPoly.area().equals(correct.area()));
		assertEquals(correct, tempPoly);

		cutter = new BoundingBox(75, 0, 50, 75);
		points = new int[] { 75, 75, 75, 0, 125, 0, 125, 75 };
		correct = new ConvexPolygon(points);
		tempPoly = ConvexPolygon.intersection(triangle1, cutter);
		assertTrue(
			"Bbox cutter 3 ("
				+ triangle1
				+ " & "
				+ cutter
				+ " = "
				+ tempPoly
				+ ")",
			tempPoly.area().equals(correct.area()));
		assertEquals(correct, tempPoly);

		cutter = new BoundingBox(75, 10, 50, 40);
		points = new int[] { 75, 50, 75, 10, 125, 10, 125, 50 };
		correct = new ConvexPolygon(points);
		tempPoly = ConvexPolygon.intersection(triangle1, cutter);
		assertTrue(
			"Bbox cutter 4 (interior) ("
				+ triangle1
				+ " & "
				+ cutter
				+ " = "
				+ tempPoly
				+ ")",
			tempPoly.area().equals(correct.area()));
		assertEquals(correct, tempPoly);
		assertEquals(tempPoly, correct);

		tempPoly = ConvexPolygon.intersection(unitSquare, unitSquare);
		assertTrue("Unit self test", tempPoly.area().equals(1));
		assertEquals(unitSquare, tempPoly);

		cutter = new OrientedBox(65, 37, 134, 59, -4);
		ConvexPolygon another = new BoundingBox(104, 48, 71, 39);
		tempPoly = ConvexPolygon.intersection(another, cutter);
		assertEquals(tempPoly, ConvexPolygon.intersection(cutter, another));
		Rational tempPolyArea = tempPoly.area();
		Rational cutterArea = cutter.area();
		Rational anotherArea = another.area();
		assertTrue(tempPolyArea.lessThanEqualTo(cutterArea));
		assertTrue(tempPolyArea.lessThanEqualTo(anotherArea));

		cutter = new OrientedBox(0, 100, 141, 141, -45);
		another = new OrientedBox(10, 10, 180, 180, 0);
		tempPoly = ConvexPolygon.intersection(cutter, another);
		assertTrue(
			"number of verteces should be 8: "
				+ ((ConvexPolygon) tempPoly).getNumberOfVerteces(),
			((ConvexPolygon) tempPoly).getNumberOfVerteces() == 8);
		tempPolyArea = tempPoly.area();
		assertTrue(
			"This should be between not much less than 19600 "
				+ tempPolyArea,
				tempPolyArea.equals(new Rational(214711, 11)));
		//FIXME:: Figure out what the math should give as the right answer.
		//FIXME:: Insert some more checks
	}

	/**
	 * Tests the getNearIntersection methods.
	 */
	public void testGetNearIntersection() {
		Pnt p1, p2, p3, q;

		BoundingBox b = new BoundingBox(0, 0, 100, 100);
		assertEquals("bbox centroid", b.getCentroid(), new Pnt(50, 50));
		p1 = new Pnt(0, 50);
		q = b.getNearIntersection(p1);
		assertEquals("Point on west boundary", q, p1);
		p2 = new Pnt(5, 50);
		q = b.getNearIntersection(p2);
		assertEquals("Point near west boundary", q, p1);
		p3 = new Pnt(50, 105);
		q = b.getNearIntersection(p3);
		assertEquals("Point north of boundary", q, new Pnt(50, 100));

		OrientedBox o = new OrientedBox(0, 0, 100, 100, 0);
		assertEquals("obox centroid", o.getCentroid(), new Pnt(50, 50));
		p1 = new Pnt(0, 50);
		q = o.getNearIntersection(p1);
		assertEquals("Point on west boundary", q, p1);
		p2 = new Pnt(5, 50);
		q = o.getNearIntersection(p2);
		assertEquals("Point near west boundary", q, p1);
		p3 = new Pnt(50, 105);
		q = o.getNearIntersection(p3);
		assertEquals("Point north of boundary", q, new Pnt(50, 100));
	}

	/**
	 * Tests the BoundingBox's equality methods.
	 */
	public void testBBoxEquals() {
		tempPoly = new BoundingBox(0, 0, 1, 1);
		assertEquals(unitSquare, tempPoly);
		assertEquals(tempPoly, unitSquare);
		assertTrue(!tempPoly.equals(bigSquare));
		assertTrue(!bigSquare.equals(tempPoly));
	}

	/**
	 * Test intersection of bbox and obox
	 */
	public void testCPolyChildren() {
		BoundingBox B = new BoundingBox(0, 0, 100, 100);
		OrientedBox O = new OrientedBox(0, 0, 100, 100, 0);
		int[] points = { 0, 0, 100, 0, 100, 100, 0, 100 };
		ConvexPolygon correct = new ConvexPolygon(points);
		tempPoly = ConvexPolygon.intersection(B, O);
		assertTrue(
			"Same shape test 1: " + tempPoly + " == " + correct,
			tempPoly.area().equals(correct.area()));
		assertEquals(tempPoly, correct);

		O = new OrientedBox(100, 0, 100, 100, 90);
		tempPoly = ConvexPolygon.intersection(B, O);
		assertTrue("Same shape test 2", tempPoly.area().equals(correct.area()));
		assertEquals(tempPoly, correct);

		O = new OrientedBox(50, -25, 106, 106, 45);
		points =
			new int[] {
				25,
				0,
				75,
				0,
				100,
				25,
				100,
				75,
				75,
				100,
				25,
				100,
				0,
				75,
				0,
				25 };
		correct = new ConvexPolygon(points);
		tempPoly = ConvexPolygon.intersection(B, O);
		assertTrue(
			"Diamond test. " + B + " & " + O + " = " + tempPoly,
			tempPoly.area().equals(correct.area()));
		assertEquals(tempPoly, correct);
	}

	/**
	 * Tests the OrientedBox's equality methods.
	 */
	public void testOBoxEquals() {
		try {
			int[] params = { 0, 0, 1, 1, 0 };
			tempPoly = new OrientedBox(params);
			assertEquals(unitSquare, tempPoly);
			assertEquals(tempPoly, unitSquare);
			assertTrue(!tempPoly.equals(bigSquare));
			assertTrue(!bigSquare.equals(tempPoly));

			params = new int[] { 1, 1, 1, 1, 180 };
			tempPoly = new OrientedBox(params);
			assertEquals(unitSquare, tempPoly);
			assertEquals(tempPoly, unitSquare);
			assertTrue(!tempPoly.equals(bigSquare));
			assertTrue(!bigSquare.equals(tempPoly));
		} catch (IllegalArgumentException bdx) {
			assertTrue(bdx.getMessage(), false);
		}
	}

	/**
	 * Tests the ConvexPolygon's union methods.
	 */
	public void testCPolyUnion() {
		tempPoly = PolyList.union(bigSquare, unitSquare);
		assertTrue(
			"Square union: ("
				+ bigSquare
				+ " U "
				+ unitSquare
				+ ").area() == "
				+ tempPoly.area(),
			tempPoly.area().equals(bigSquare.area()));
		assertEquals(
			"Square union: ("
				+ bigSquare
				+ " U "
				+ unitSquare
				+ ") == "
				+ tempPoly,
			tempPoly, bigSquare);

		tempPoly = PolyList.intersection(tempPoly, unitSquare);
		assertTrue(
			"Square union intersection test: ("
				+ bigSquare
				+ " U "
				+ unitSquare
				+ ") & "
				+ unitSquare
				+ " == "
				+ tempPoly,
			tempPoly.equals(unitSquare));

		BoundingBox leftRectangle = new BoundingBox(0, 0, 100, 200);
		BoundingBox rightRectangle = new BoundingBox(100, 0, 100, 200);
		BoundingBox topRectangle = new BoundingBox(0, 100, 200, 100);
		BoundingBox bottomRectangle = new BoundingBox(0, 0, 200, 100);

		BoundingBox tempVertical =
			BoundingBox.union(leftRectangle, rightRectangle);
		assertTrue(
			" " + tempVertical + " = " + leftRectangle + " U " + rightRectangle,
			tempVertical.equals(bigSquare));

		BoundingBox tempHorizontal =
			BoundingBox.union(bottomRectangle, topRectangle);
		assertTrue(
			" "
				+ tempHorizontal
				+ " = "
				+ bottomRectangle
				+ " U "
				+ topRectangle,
			tempHorizontal.equals(bigSquare));

		tempPoly = BoundingBox.intersection(tempVertical, tempHorizontal);
		assertTrue(
			"Intersection of 2 unions 1: "
				+ tempPoly
				+ " = "
				+ tempVertical
				+ " U "
				+ tempHorizontal,
			tempPoly.equals(bigSquare));

		OrientedBox obox = new OrientedBox(200, 0, 200, 200, 90);
		tempPoly = PolyList.intersection(obox, tempHorizontal);
		assertTrue(
			"Intersection of bbox union with obox 1 : "
				+ obox
				+ " & "
				+ tempHorizontal
				+ " = "
				+ tempPoly,
			tempPoly.equals(bigSquare));

		obox = new OrientedBox(1, 1, 6, 7, 0);
		topRectangle = new BoundingBox(2, 5, 12, 3);
		bottomRectangle = new BoundingBox(2, 1, 12, 3);
		tempHorizontal = BoundingBox.union(topRectangle, bottomRectangle);
		assertTrue(
			tempHorizontal + " = " + topRectangle + " U " + bottomRectangle,
			tempHorizontal.area().equals(72));

		tempPoly = PolyList.intersection(obox, tempHorizontal);
		assertTrue(
			tempPoly + " = " + tempHorizontal + " & " + obox,
			tempPoly != null);
		assertTrue(
			tempPoly
				+ " = "
				+ tempHorizontal
				+ " & "
				+ obox
				+ ".area == "
				+ tempPoly.area(),
			tempPoly.area().equals(30));

		tempPoly = obox.getIntersection(tempHorizontal);
		assertTrue(
			tempPoly + " = " + tempHorizontal + " & " + obox,
			tempPoly != null);
		assertTrue(
			tempPoly
				+ " = "
				+ tempHorizontal
				+ " & "
				+ obox
				+ ".area == "
				+ tempPoly.area(),
			tempPoly.area().equals(30));

		obox = new OrientedBox(159, 59, 135, 78, 0);

		rightRectangle = new BoundingBox(184, 56, 79, 47);
		leftRectangle = new BoundingBox(152, 100, 87, 35);
		tempHorizontal = BoundingBox.union(rightRectangle, leftRectangle);

		tempPoly = obox.getIntersection(tempHorizontal);
		assertTrue(tempPoly != null);
		assertTrue(!tempPoly.area().lessThan(0));

		obox = new OrientedBox(307, 94, 70, 25, -51);
		tempHorizontal = new BoundingBox(288, 61, 20, 12);
		tempPoly = obox.getIntersection(tempHorizontal);
		assertTrue(tempPoly != null);
		assertTrue(tempPoly.area().equals(0));

		obox = new OrientedBox(99, 82, 199, 23, -5);
		OrientedBox obox2 = new OrientedBox(115, 55, 97, 25, 31);

		tempPoly = obox.getIntersection(obox2);
		assertTrue(tempPoly != null);
		assertTrue(tempPoly.area().greaterThan(0));

		OrientedBox[] truthboxes = null;
		BoundingBox[] candboxes = null;
		try {
			truthboxes =
				new OrientedBox[] {
					new OrientedBox("116 148 106 9 0"),
					new OrientedBox("132 39 74 10 0"),
					new OrientedBox("138 74 64 11 -4"),
					new OrientedBox("136 88 66 10 -2"),
					new OrientedBox("103 136 128 9 0"),
					new OrientedBox("128 3 84 11 0"),
					new OrientedBox("94 27 84 11 0"),
					new OrientedBox("69 63 201 10 0"),
					new OrientedBox("59 111 216 10 0")};
			candboxes =
				new BoundingBox[] {
					new BoundingBox("129 0 81 18"),
					new BoundingBox("95 21 152 22"),
					new BoundingBox("71 45 197 29"),
					new BoundingBox("70 84 202 42"),
					new BoundingBox("132 129 88 37"),
					new BoundingBox("292 185 22 14")};
		} catch (BadDataException bdx) {
			assertTrue(bdx.getMessage(), false);
		}
		PolyList datruth = new PolyList();
		PolyList dacands = new PolyList();
		for (int i = 1; i < truthboxes.length; i++) {
			datruth = PolyList.union(datruth, truthboxes[i]);
		}
		for (int i = 1; i < candboxes.length; i++) {
			dacands = PolyList.union(dacands, candboxes[i]);
		}
		assertTrue("datruth.area() == " + datruth.area(), datruth.area().greaterThan(0));
		assertTrue("dacands.area() == " + dacands.area(), dacands.area().greaterThan(0));
		tempPoly = PolyList.intersection(datruth, dacands);
		assertTrue(
			"daintersection.area == " + tempPoly.area(),
			tempPoly.area().greaterThan(0));
	}

	/**
	 * Tests the ConvexPolygon's add methods.
	 */
	public void testCPolyAdd() {
		ConvexPolygon[] sumResult;
		sumResult = ConvexPolygon.add(bigSquare, unitSquare);
		assertTrue(
			"Square add: ("
				+ bigSquare
				+ " U "
				+ unitSquare
				+ ").area() == cp["
				+ sumResult.length
				+ "]",
			sumResult.length == 1);
		assertTrue(
			"Square add: ("
				+ bigSquare
				+ " U "
				+ unitSquare
				+ ").area() == "
				+ sumResult[0].area(),
			sumResult[0].area().equals(bigSquare.area()));
		assertTrue(
			"Square union: ("
				+ bigSquare
				+ " U "
				+ unitSquare
				+ ") == "
				+ sumResult[0],
			sumResult[0].equals(bigSquare));

		BoundingBox leftRectangle = new BoundingBox(0, 0, 100, 200);
		BoundingBox rightRectangle = new BoundingBox(100, 0, 100, 200);
		sumResult = ConvexPolygon.add(leftRectangle, rightRectangle);
		assertTrue(
			"Rectangle add: ("
				+ leftRectangle
				+ " U "
				+ rightRectangle
				+ ").area() == cp["
				+ sumResult.length
				+ "]",
			sumResult.length == 2);
		Rational tempR = new Rational();
		Rational.plus(sumResult[0].area(), sumResult[1].area(), tempR);
		assertTrue(
			"Square add: ("
				+ leftRectangle
				+ " U "
				+ rightRectangle
				+ ").area() == "
				+ sumResult[0].area()
				+ " + "
				+ sumResult[1].area(),
			tempR.equals(bigSquare.area()));
		assertTrue(
			"Square union: ("
				+ leftRectangle
				+ " U "
				+ rightRectangle
				+ ") == "
				+ sumResult[0]
				+ " ][ "
				+ sumResult[1],
			(sumResult[0].equals(leftRectangle)
				&& sumResult[1].equals(rightRectangle))
				|| (sumResult[1].equals(leftRectangle)
					&& sumResult[0].equals(rightRectangle)));

		OrientedBox obox = new OrientedBox(200, 0, 200, 200, 45);
		sumResult = ConvexPolygon.add(obox, bigSquare);
		String S = "obox " + obox.toStringListOfPoints() + " U " + bigSquare;
		ConvexPolygon split = ConvexPolygon.intersection(obox, bigSquare);
		S += "\n split = " + split;
		S += "\n sumResult[0] = " + sumResult[0];
	}

	/**
	 * Tests the for a specific error that used to occur when
	 * computing the area shared by multiple boxes.
	 */
	public void testOBoxNegativeDistanceError() {
		OrientedBox obox = new OrientedBox(259, 206, 29, 49, 178);
		BoundingBox bbox = new BoundingBox(200, 100, 127, 59);
		PolyList cpoly1 = obox.getIntersection(bbox);

		String S =
			obox.toStringListOfPoints()
				+ " intesects with "
				+ bbox
				+ "\n to get "
				+ cpoly1
				+ "\n  with areas "
				+ bbox.area()
				+ ", "
				+ obox.area()
				+ ", and "
				+ cpoly1.area()
				+ ", respectively.";
		assertTrue(S, cpoly1.area().greaterThan(0));
		assertTrue(S, cpoly1.area().lessThan(1421));
	}
}
