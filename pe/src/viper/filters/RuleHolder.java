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

/************************************************************************* *
 ************************************************************************* *
 * File:        RuleHolder.java
 * Purpose:     Holds the rules for the specific descriptors and their
 *              attributes
 * Written by:  Felix Sukhenko
 * Date:        12 1998
 * Notes:	(any usage or compilation notes)
 * ************************************************************************ *
 * Modification Log:
 *
 * DATE       WHO                MODIFICATION    
 *
 * ************************************************************************ *
 *      Copyright (C) 1997 by the University of Maryland, College Park
 *
 *		Laboratory for Language and Media Processing
 *		   Institute for Advanced Computer Studies
 *	       University of Maryland, College Park, MD  20742
 *
 *  email: lamp@cfar.umd.edu               http: documents.cfar.umd.edu/LAMP
 * ************************************************************************ *
 * ************************************************************************ */
package viper.filters;

import java.util.*;

import viper.descriptors.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Holds the rules for the specific descriptors and their 
 * attributes. The rules define what sort of descriptor data
 * to ignore, thus allowing the comparison program to focus
 * on a subset of data from a video clip, for example, only data
 * containing a certain word or a certain set of frames.
 * 
 * @author Felix Sukhenko
 * @author David Mihalcik
 * @since 12 1998
 */
public class RuleHolder 
{
  
  /**
   * The descriptor to attribute map.
   *
   * This contains a bunch of hashtables, mapped by a descriptor
   * identifier, that contain a bunch of rules, mapped by attribute
   * name. A rule is defined in the Filterable.Rule.
   */
  Hashtable m_d_a_map; // descriptor to attribute map

  /******************
   * CONSTRUCTOR(S) *
   ******************/
  
  public RuleHolder() {
    m_d_a_map = new Hashtable();
  }

  /********************
   * PUBLIC FUNCTIONS *
   ********************/

  /**
   * Adds a place for this descriptor
   * @param descriptor a unique string representation
   *       of the descriptor and its category 
   *       <Descriptor Category><Descriptor Name>,
   *       for example: OBJECTFace
   */
  public void addDescriptor( String descriptor) {
    m_d_a_map.put(descriptor, new Hashtable());
  }

  /**
   * Adds the attribute and the rule for that attribute to the descriptors list
   * of attributes/rules.
   * @param descriptor the descriptor to which the attribute/rule will be added
   * @param attribute_name the name of the attribute to which the rule belongs
   * @param rule the limitations rule that describes the limitations criteria
   * @throws BadDataException
   */
  public void addRule (String descriptor, String attribute_name, Filterable.Rule rule) 
       throws BadDataException
  {
    if (!m_d_a_map.containsKey(descriptor))
      addDescriptor (descriptor);
    Hashtable attribute_rule_map = (Hashtable)m_d_a_map.get(descriptor);
    if (!attribute_rule_map.containsKey(attribute_name))
      attribute_rule_map.put(attribute_name, rule);    
  }

  /**
   * Checks if the descriptor meets the criteria set by the limitation rules
   * @param to_be_tested the descriptor that has to be tested
   * @return true if the parameter passes the criteria defined by the rule
   */
  public boolean meetsCriteria (Descriptor to_be_tested) {
    Hashtable attributes_rule_map = (Hashtable)m_d_a_map.get(to_be_tested.getCategory() + to_be_tested.getName());
    if (attributes_rule_map == null) return true; // doesn't exist in list
    if (attributes_rule_map.size() == 0) return true; // no rules exist for it

    Enumeration enumeration = attributes_rule_map.keys();
    boolean allPass = true;
    while (enumeration.hasMoreElements()) {
      // for each attribute in the list, get the attribute and 
      // the associated rule.
      String attributeName = (String) enumeration.nextElement();
      Filterable f = to_be_tested.getFilterable (attributeName);
      Filterable.Rule rule = (Filterable.Rule) attributes_rule_map.get (attributeName);

      allPass = allPass && f.passes (rule);
      if (!allPass)
	return false;
    }
    return allPass;
  }

  /**
   * Function used to print this object in RAW format.
   * return the string raw representation of this object
   * @return the rule holder, as should be printed into the raw output file
 */
  public String toRawFormat() {
    String s = "";
    Enumeration d_keys = m_d_a_map.keys();
    while (d_keys.hasMoreElements()) {
      String key = (String)d_keys.nextElement();
      if (key.startsWith("OBJECT"))
	s = s+key.substring(6, key.length())+"\n";
      else if (key.startsWith("CONTENT"))
	s = s+key.substring(7, key.length())+"\n";
      else
	s = s+key+"\n";
      Hashtable a_r_map = (Hashtable)m_d_a_map.get(key);
      Enumeration a_keys = a_r_map.keys();
      while (a_keys.hasMoreElements()) {
	key = (String)a_keys.nextElement();
	s = s+"\t* "+key+" "+a_r_map.get(key)+"\n";
      }
    }
    return s;
  }

  /**
   * Function used to print this object in a string format.
   * Invoked whenever you do ""+this_object
   * @return the filter, as can be read in
   */
  public String toString() {
    String s = "";
    Enumeration d_keys = m_d_a_map.keys();
    /*    if (m_d_a_map.isEmpty())
      System.out.println("FILTER EMPTY");
    else
      System.out.println("FILTER NOT EMPTY");*/
    while (d_keys.hasMoreElements()) {
      String key = (String)d_keys.nextElement();
      if (key.startsWith("OBJECT"))
	s = "OBJECT "+key.substring(6, key.length())+"\n";
      else if (key.startsWith("CONTENT"))
	s = "CONTENT "+key.substring(7, key.length())+"\n";
      else
	s = s+key+"\n";
      Hashtable a_r_map = (Hashtable)m_d_a_map.get(key);
      Enumeration a_keys = a_r_map.keys();
      while (a_keys.hasMoreElements()) {
	key = (String)a_keys.nextElement();
	s = s+"\t"+key+" with rule: "+a_r_map.get(key)+"\n";
      }
    }
    return s;
  }
}
