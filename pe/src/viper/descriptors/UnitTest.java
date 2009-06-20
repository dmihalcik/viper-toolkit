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

package viper.descriptors;

//see http://members.pingnet.ch/gamma/junit.htm
import junit.framework.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Testsuite for the geometry descriptors.
 * <UL>
 * Still to add:
 * <LI>Testing of parsing</LI>
 * </UL>
 */
public class UnitTest extends TestCase {

	/**
	 * Constructs a new unit test with the given name.
	 * @param name the test name
	 */
	public UnitTest(String name) {
		super(name);
	}

	protected void setUp() {
	}

	/**
	 * Constructs a new suite for testing the descriptors.
	 * @return the descriptor package test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new UnitTest("testDescriptors"));
		suite.addTest(new UnitTest("testDescHolders"));

		return suite;
	}

	DescPrototype fileInfoProto;
	DescPrototype textBoxProto;
	DescPrototype textProto;

	DescHolder targets;
	DescHolder candidates;

	void testDescriptors() {
		try {
			fileInfoProto = new DescPrototype("FILE", "Information");
			fileInfoProto.addAttribute("SOURCEDIR", "svalue");
			fileInfoProto.addAttribute("SOURCEFILES", "svalue");
			fileInfoProto.addAttribute("SOURCETYPE", "lvalue",
					"SEQUENCE FRAMES");
			fileInfoProto.addAttribute("NUMFRAMES", "dvalue");

			Descriptor info = fileInfoProto.create();
			info.getAttribute("SOURCEDIR").setValue("/fs/www");
			info.getAttribute("SOURCEFILES").setValue("0 news.gif 1 news2.gif");
			info.getAttribute("SOURCETYPE").setValue("SEQUENCE");
			info.getAttribute("NUMFRAMES").setValue(String.valueOf(2));

		} catch (BadDataException bdx) {
			bdx.printStackTrace();
			assertTrue(bdx.getMessage(), false);
		}
	}

	void testDescHolders() {
	}
}