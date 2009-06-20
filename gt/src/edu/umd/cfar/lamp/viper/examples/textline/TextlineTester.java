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

package edu.umd.cfar.lamp.viper.examples.textline;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Feb 16, 2005
 *
 * Used to test the various components of the examples.textline package
 */
public class TextlineTester {

	public static void main(String[] args) {
		//testIntPair();
		testTextlineModel();
		
	}
	
	private static void testTextlineModel() {
		TextlineModel tm1 = new TextlineModel();
		System.out.println("TM1: " + tm1);
		tm1.set(3,3,56,76,21);
		System.out.println("TM1: " + tm1);
		
		TextlineModel tm2 = new TextlineModel(5,5,10,10,0);
		System.out.println("TM2: " + tm2);
		tm2.setText("Daniel Ramsbrock", null);
		tm2.addOcclusion(34,26);
		System.out.println("TM2: " + tm2);
		
		TextlineModel tm3 = new TextlineModel(5,5,13,13,0,"This is a test");
		tm3.addWordOffset(4);
		tm3.addWordOffset(17);
		tm3.addOcclusion(23,56);
		tm3.addOcclusion(new IntPair(45,87));
		System.out.println("TM3: " + tm3);
		System.out.print("TM3 words: ");
		String[] words = tm3.getWords(null);
		for(int i = 0; i < words.length; i++) {
			System.out.print(words[i] + " # ");
		}
	}
	
	private static void testIntPair() {
		IntPair ip1 = new IntPair(6,11);
		IntPair ip2 = new IntPair();
		System.out.println("IP1: ("+ip1.getOne()+", "+ip1.getTwo()+")");
		System.out.println("IP2: " + ip2);
		ip2.setOne(6);
		System.out.println("IP2: ("+ip2.getOne()+", "+ip2.getTwo()+")");
		ip2.setTwo(11);
		System.out.println("IP2: " + ip2);
		System.out.println("IP1 == IP2: "+ip1.equals(ip2));		
	}
}
