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

import java.io.*;
import java.util.*;

import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * This allows equivalency between two different Strings. Unlike a Map,
 * it can take Many-Many mappings.
 *
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 */
public class Equivalencies
{
  /** A set mapping individual keys (targets)
   * to java Sets of values (candidates). */
  private Map keysToValues = new HashMap();

  /** A set mapping individual values (candidates)
   * to java Sets of keys (targets). */
  private Map valuesToKeys = new HashMap();

  /**
   * Creates a new object of the Equivalency class.
   */
  public Equivalencies()
  {
  }

  /**
   * Determines if the specified key / right Object is in the list of Pairings.
   *
   * @param key The key to search for.
   * @return <code>true</code> iff this list holds the key
   */
  public boolean containsKey (Object key)
  {
    return keysToValues.containsKey (key);
  }

  /**
   * Determines if the specified Value / left Object is in the list of Pairings.
   *
   * @param value - the value to search for
   * @return true iff the list holds the value
   */
  public boolean containsValue (Object value)
  {
    return valuesToKeys.containsKey (value);
  }

  /**
   * Generates a hash value for this object.
   * @return an integer that any other Equivalencies class
   *      with the same pairings would generate
   */
  public int hashCode()
  {
    return (valuesToKeys.hashCode() ^ keysToValues.hashCode());
  }

  /**
   * Adds a new entry saying that target is equivalent 
   * to candidate.
   *
   * @param target The key / left Object
   * @param candidate The value / right Object
   */
  public void addToList (Object target, Object candidate)
  {
    Set similarKeys = (Set) valuesToKeys.get (candidate);
    Set similarValues = (Set) keysToValues.get (target);

    if (similarKeys == null) {
      similarKeys = Collections.singleton (target);
    } else {
      if (similarKeys.size() == 1) {
	Set temp = similarKeys;
	similarKeys = new HashSet();
	similarKeys.addAll (temp);
      }
      similarKeys.add (target);
    }

    if (similarValues == null) {
      similarValues = Collections.singleton (candidate);
    } else {
      if (similarValues.size() == 1) {
	Set temp = similarValues;
	similarValues = new HashSet();
	similarValues.addAll (temp);
      }
      similarValues.add (candidate);
    }

    for (Iterator iter = similarKeys.iterator(); iter.hasNext(); )
      keysToValues.put (iter.next(), similarValues);
    for (Iterator iter = similarValues.iterator(); iter.hasNext(); )
      valuesToKeys.put (iter.next(), similarKeys);
  }

  /**
   * Returns all values associated with the key.
   * @param target The key to find.
   * @return An array containing all Objects equivalent to the key.
   */
  public Object [] findMatches (Object target)
  {
    Set v = (Set) keysToValues.get (target);
    if (null == v)
      return new Object[0];
    return v.toArray ();
  }

  /**
   * Determines if the given key and value are equivalent 
   * under this set of Equivalencies
   * @param target The key / left Object.
   * @param candidate The value / right Object.
   * @return <code>true</code> iff the pairing is found.
   */
  public boolean eq (Object target, Object candidate)
  {
    Set values =  (Set) keysToValues.get (target);
    if (null == values)
      return false;
    return values.contains (candidate);
  }

/* ------------------------------------------------------------------------ *
   Print Mapping
 * ------------------------------------------------------------------------ */

  /**
   * Prints the Equivalencies to a PrintStream in classic ViPER format.
   * @param out A <code>PrintWriter</code> to write into.
   */
  public void printMapping (PrintWriter out)
  {
    out.println( "#BEGIN_EQUIVALENCE\n" );
    Iterator targets = keysToValues.entrySet().iterator();
    while (targets.hasNext()) {
      Map.Entry curr = (Map.Entry) targets.next();
      out.print (curr.getKey() + " :");
      Iterator candidates = ((Set) curr.getValue()).iterator();
      while (candidates.hasNext())
	out.print (" " + candidates.next());
      out.println ();
    }
    out.println( "#END_EQUIVALENCE" );    
    out.flush();
  }

/* ------------------------------------------------------------------------ *
   Parse Mapping
 * ------------------------------------------------------------------------ */
  /**
   * Parses in an Equivalency class in ViPER format.
   * @param paths A <code>Vector</code> containing the names
   *    of any files containing Equivalency information
   * @return <code>true</code> if parsing is successful
   */
  public boolean parseMapping (Vector paths)
  {
    VReader reader = new VReader (paths, "EQUIVALENCE");
    return parseMapping (reader);
  }

  /**
   * Parses in an Equivalency class in ViPER format.
   *
   * @param reader A {@link VReader} containing Equivalency data.
   * @return <code>true</code> if Parsing is successful.
   */
  public boolean parseMapping (VReader reader)
  {
    /// Advance to beginning in file.
    boolean status = reader.advanceToBeginDirective ("EQUIVALENCE");

    if (!status) {
      reader.printGeneralError ("Unable to find BEGIN_EQUIVALENCE directive");
      return false;
    }
    try {
      reader.gotoNextRealLine();
    } catch (IOException iox) {
      reader.printGeneralError ("Equivalence Parser: "
				+ "I/O Exception: " + iox.getMessage());
      return false;
    }

    while (!reader.currentLineIsEndDirective()) {
      StringTokenizer st = new StringTokenizer (reader.getCurrentLine());
      try {
	String target = st.nextToken();
	if (st.nextToken().equals (":")) {
	  do {
	    addToList (target, st.nextToken());
	  } while (st.hasMoreTokens());
	} else
	  reader.printError ("Missing ':' in equivalency");
	try {
	  reader.gotoNextRealLine();
	} catch (IOException iox) {
	  reader.printGeneralError ("Equivalence Parser: "
				    + "I/O Exception: " + iox.getMessage());
	  return false;
	}
      } catch (NoSuchElementException nsex) {
	reader.printError ("Incomplete equivalency");
      }
    }
    reader.printErrorTotals();
    return true;
  } 
}





