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

package edu.umd.cfar.lamp.viper.util;

import java.util.*;

import junit.framework.*;

/**
 * Testsuite for the geometry package.
 * <UL>
 * Still to add:
 * <LI>Testing of Permuter and Combinator</LI>
 * <LI>Testing of StringHelp</LI>
 * </UL>
 * 
 * @see <a href="http://members.pingnet.ch/gamma/junit.htm">Test Infected</a>
 */
public class UtilTest extends TestCase {
	/**
	 * Constructs a new UnitTest of the given name.
	 * 
	 * @param name
	 *            The name of the test.
	 */
	public UtilTest(String name) {
		super(name);
	}

	/**
	 * This code is invoked before running any tests.
	 */
	protected void setUp() {
	}

	/**
	 * Constructs the Test object that points to the test.
	 * 
	 * @return All the tests to run on this package.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new UtilTest("testSparseMatrix"));
		suite.addTest(new UtilTest("testMatrixIterators"));

		suite.addTest(new UtilTest("testAssignment"));
		suite.addTest(new UtilTest("testRLE"));

		return suite;
	}

	/**
	 * Test the {@link SparseMatrix}class.
	 */
	public void testSparseMatrix() {
		SparseMatrix one = new SparseMatrix();

		String str1 = "Hello,";

		one.set(5, 5, str1);
		assertEquals("Adding something", one.get(5, 5), str1);

		one.set(5, 5, null);
		assertTrue(
			"Removing something: " + one.get(5, 5),
			one.get(5, 5) == null);
		assertTrue("Sanity Check", one.get(10000, 10000) == null);

		//Test Exceptions for negative indeces
		boolean success = false;
		try {
			one.get(-1, -1);
		} catch (MatrixIndexOutOfBoundsException mioobx) {
			success = true;
		}
		assertTrue("Index OOB Exception Test Failed for [-1, -1]", success);
	}

	/**
	 * Test the implementors of the {@link MatrixIterator}interface.
	 */
	public void testMatrixIterators() {
		SparseMatrix one = new SparseMatrix(100000, 100000);
		Object[][] check = new Object[10][10];
		PackedMatrix checkMatrix = new PackedMatrix(check);
		String str1 = "Hello";
		String str2 = ", World";
		String str3 = "!";

		assertTrue(one.equals(checkMatrix));

		one.set(5, 4, str1);
		check[5][4] = str1;
		one.set(6, 4, str2);
		check[6][4] = str2;
		one.set(7, 4, str3);
		check[7][4] = str3;
		checkHelloWorld(one.getMatrixIterator());
		checkHelloWorld(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));

		one.remove(6, 4);
		check[6][4] = null;
		checkHello(one.getMatrixIterator());
		checkHello(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));
		one.remove(5, 4);
		check[5][4] = null;
		one.remove(7, 4);
		check[7][4] = null;
		checkClean(one.getMatrixIterator());
		checkClean(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));

		one.set(5, 5, str1);
		check[5][5] = str1;
		one.set(7, 5, str3);
		check[7][5] = str3;
		one.set(6, 5, str2);
		check[6][5] = str2;
		checkHelloWorld(one.getMatrixIterator());
		checkHelloWorld(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));
		one.remove(6, 5);
		check[6][5] = null;
		checkHello(one.getMatrixIterator());
		checkHello(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));
		one.remove(5, 5);
		check[5][5] = null;
		one.remove(7, 5);
		check[7][5] = null;
		checkClean(one.getMatrixIterator());
		checkClean(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));

		one.set(6, 5, str2);
		one.set(5, 5, str1);
		one.set(7, 5, str3);
		checkHelloWorld(one.getMatrixIterator());
		one.remove(6, 5);
		checkHello(one.getMatrixIterator());
		one.remove(7, 5);
		one.remove(5, 5);
		one.remove(1000, 10000);
		checkClean(one.getMatrixIterator());

		one.set(5, 5, str1);
		check[5][5] = str1;
		one.set(5, 7, str3);
		check[5][7] = str3;
		checkHello(one.getMatrixIterator());
		checkHello(checkMatrix.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));
		one.set(5, 6, str2);
		check[5][6] = str2;
		checkHelloWorld(one.getMatrixIterator());
		checkHelloWorld(checkMatrix.getMatrixIterator());
		one.remove(5, 6);
		check[5][6] = null;
		checkHello(one.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));
		one.remove(5, 5);
		check[5][5] = null;
		one.remove(5, 7);
		check[5][7] = null;
		checkClean(one.getMatrixIterator());
		assertTrue(one.equals(checkMatrix));

		one.set(5, 5, str1);
		one.set(6, 6, str3);
		checkHello(one.getMatrixIterator());
		one.set(5, 6, str2);
		checkHelloWorld(one.getMatrixIterator());
		one.remove(5, 6);
		checkHello(one.getMatrixIterator());
		one.remove(5, 5);
		one.remove(6, 6);
		checkClean(one.getMatrixIterator());
	}

	/**
	 * A step in running the matrix iterator test. Checks that it contains
	 * "Hello, World!".
	 * 
	 * @param mi
	 *            A MatrixIterator being tested.
	 */
	private void checkHelloWorld(MatrixIterator mi) {
		String temp = getConcatenatedColumnwise(mi);
		assertTrue(
			"Testing concatenation (Hello, World!): " + temp,
			temp.equals("Hello, World!"));
	}

	/**
	 * A step in running the matrix iterator test. Check that it contains
	 * "Hello!".
	 * 
	 * @param mi
	 *            A MatrixIterator being tested.
	 */
	private void checkHello(MatrixIterator mi) {
		String temp = getConcatenatedColumnwise(mi);
		assertTrue(
			"Testing concatenation (Hello!): " + temp,
			temp.equals("Hello!"));
	}

	/**
	 * A step in running the matrix iterator test. Checks that it is empty.
	 * 
	 * @param mi
	 *            A MatrixIterator being tested.
	 */
	private void checkClean(MatrixIterator mi) {
		String temp = getConcatenatedColumnwise(mi);
		assertTrue("Testing concatenation null: " + temp, temp.equals(""));
	}

	/**
	 * A step in running the matrix iterator test.
	 * 
	 * @param mi
	 *            A MatrixIterator being tested.
	 * @return All of the data concatenated together, column-by-column.
	 */
	private String getConcatenatedColumnwise(MatrixIterator mi) {
		String temp = new String();
		int prevCol = -1;
		int prevRow = -1;
		while (mi.hasNextColumn()) {
			int x = mi.nextColumn();
			assertTrue(x + " > " + prevCol + "\n", x > prevCol);
			while (mi.hasNextInColumn()) {
				temp += (String) mi.nextInColumn();

				int y = mi.currRow();
				assertTrue(y + " > " + prevRow + "\n", y > prevRow);
				prevRow = y;
			}
			prevRow = -1;
			prevCol = x;
		}

		return temp;
	}


	/**
	 * Tests the hungarian assignment algorithm.
	 */
	public void testAssignment() {
		long[][] data // example from Papadimitriou and Steiglitz
		= { { 7, 2, 1, 9, 4 }, {
				9, 6, 9, 5, 5 }, {
				3, 8, 3, 1, 8 }, {
				7, 9, 4, 2, 2 }, {
				8, 4, 7, 4, 8 }
		};

		assertTrue("Testing transpose. ", testTranspose(data));
		assertTrue("Testing Assignment... ", testAssignment(data, 15));
	}

	/**
	 * Helps test hungarian assigment.
	 * @param data the bipartite graph weights
	 * @param sum the optimal sum
	 * @return if found a matching that equals the optimal sum
	 */
	public static boolean testAssignment(long[][] data, long sum) {
		Long[][] mat =
			new Long[data.length][data.length > 0 ? data[0].length : 0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				mat[i][j] = new Long(data[i][j]);
			}
		}

		DataMatrices.GetCost costFunctor = new DataMatrices.PassThrough();
		List output = DataMatrices.assign(new PackedMatrix(mat), costFunctor);

		long accumSum = 0;
		for (Iterator iter = output.iterator(); iter.hasNext();) {
			accumSum += costFunctor.cost(iter.next());
		}
		return accumSum == sum;
	}

	/**
	 * Makes sure the transpose function works.
	 * 
	 * @param data
	 *            the data to transpose
	 * @return <code>true</code> if the transpose worked.
	 */
	public static boolean testTranspose(long[][] data) {
		Long[][] mat =
			new Long[data.length][data.length > 0 ? data[0].length : 0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				mat[i][j] = new Long(data[i][j]);
			}
		}

		DataMatrix2d M = new PackedMatrix(mat);
		M = DataMatrices.transpose(M);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (!mat[i][j].equals(M.get(j, i))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Test run-length-encoded lists.
	 */
	public void testRLE() {
		Integer ZED = new Integer(0);
		Integer ONE = new Integer(1);
		Integer FIVE = new Integer(5);
		Integer SIX = new Integer(6);
		Integer TEN = new Integer(10);
		Integer ELEVEN = new Integer(11);
		Integer TWELVE = new Integer(12);

		String[] values = { "value 0", "value 1", "value 2", "value 3" };

		LengthwiseEncodedList lel = new LengthwiseEncodedList();
		assertTrue(lel.get(ONE) == null);

		lel.set(ONE, TEN, values[1]);

		assertTrue(
			"Finding value at 1 in " + lel,
			lel.get(ONE).equals("value 1"));
		assertTrue("Finding value at 5 in " + lel, lel.get(FIVE) == values[1]);
		assertTrue(lel.get(TEN) == null);
		assertTrue(lel.get(ZED) == null);

		lel.set(TEN, TWELVE, values[2]);
		assertTrue(
			"Finding value at 1 in " + lel,
			lel.get(ONE).equals("value 1"));
		assertTrue("Finding value at 5 in " + lel, lel.get(FIVE) == values[1]);
		assertTrue(lel.get(TEN) == values[2]);
		assertTrue(lel.get(ELEVEN) == values[2]);
		assertTrue(lel.get(TWELVE) == null);
		assertTrue(lel.get(ZED) == null);

		lel.remove(SIX, ELEVEN);
		assertTrue(lel.get(ZED) == null);
		assertTrue(
			"Finding value at 1 in " + lel,
			lel.get(ONE).equals("value 1"));
		assertTrue("Finding value at 5 in " + lel, lel.get(FIVE) == values[1]);
		assertTrue(lel.get(SIX) == null);
		assertTrue(lel.get(TEN) == null);
		assertTrue(lel.get(ELEVEN) == values[2]);
		assertTrue(lel.get(TWELVE) == null);

		lel.set(ZED, TEN, values[0]);
		assertTrue(lel.get(ZED) == values[0]);
		assertTrue(
			"Finding value at 1 in " + lel,
			lel.get(ONE).equals("value 0"));
		assertTrue("Finding value at 5 in " + lel, lel.get(FIVE) == values[0]);
		assertTrue(lel.get(TEN) == null);
		assertTrue(lel.get(ELEVEN) == values[2]);
		assertTrue(lel.get(TWELVE) == null);

		lel.remove(ONE, FIVE);
		assertTrue(lel.get(ZED) == values[0]);
		assertTrue("Finding value at 1 in " + lel, lel.get(ONE) == null);
		assertTrue("Finding value at 5 in " + lel, lel.get(FIVE) == values[0]);
		assertTrue(lel.get(TEN) == null);
		assertTrue(lel.get(ELEVEN) == values[2]);
		assertTrue(lel.get(TWELVE) == null);

		lel.set(ONE, SIX, values[3]);
		Iterator iter = lel.iterator();
		DynamicValue curr;
		curr = (DynamicValue) iter.next();
		assertTrue(
			curr.getStart().equals(ZED)
				&& curr.getEnd().equals(ONE)
				&& curr.getValue().equals(values[0]));
		curr = (DynamicValue) iter.next();
		assertTrue(
			curr.getStart().equals(ONE)
				&& curr.getEnd().equals(SIX)
				&& curr.getValue().equals(values[3]));
		curr = (DynamicValue) iter.next();
		assertTrue(
			curr.getStart().equals(SIX)
				&& curr.getEnd().equals(TEN)
				&& curr.getValue().equals(values[0]));
		curr = (DynamicValue) iter.next();
		assertTrue(
			curr.getStart().equals(ELEVEN)
				&& curr.getEnd().equals(TWELVE)
				&& curr.getValue().equals(values[2]));
		assertTrue(!iter.hasNext());
	}
}
